package com.example.loginDemo.controller;

import com.example.loginDemo.dto.ChatMessageDTO;
import com.example.loginDemo.dto.RecipeResponse;
import com.example.loginDemo.service.ChatBotService;
import com.example.loginDemo.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatBotController {
    private static final String LLM_GENERAL_URL = "http://llm-container:5002/ask/general";
    private static final String LLM_RECIPE_URL = "http://llm-container:5002/ask/recipe";
    private final ChatBotService chatBotService;

    // LLM (레시피 관련 요청)
//    @PostMapping("/recipes/questions")
//    public Map<String, Object> askToFlask(@RequestBody Map<String, Object> payload,
//                                          @RequestHeader("Authorization") String accessToken) {
//
//        String token = extractToken(accessToken);
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("detectedIngredients", payload.get("detectedIngredients"));
//        requestBody.put("selectedStoredIngredients", payload.get("selectedStoredIngredients"));
//        requestBody.put("userPreferences", payload.get("userPreferences"));
//        requestBody.put("additionalRequest", payload.get("additionalRequest"));
//
//        // Flask 서버에 요청 보내기
//        ResponseEntity<Map> response = sendRequestToFlask(LLM_RECIPE_URL, requestBody, token);
//
//        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//            Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");
//            if (responseBody != null && responseBody.containsKey("contents") && responseBody.containsKey("imageLink")) {
//                String contents = (String) responseBody.get("contents");
//                String imageLink = (String) responseBody.get("imageLink");
//                RecipeResponse recipeResponse = new RecipeResponse(contents, imageLink);
//                return Map.of("response", recipeResponse);
//            } else {
//                return Map.of("error", "'response' 필드에서 'contents' 또는 'imageLink' 값을 찾을 수 없습니다.");
//            }
//        } else {
//            return Map.of("error", "Flask 서버에서 유효하지 않은 응답을 받았습니다.");
//        }
//    }

    //LLM2
    @PostMapping("/recipes/questions")
    public Map<String, Object> ask(@RequestBody Map<String, Object> payload,
                                   @RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("detectedIngredients", payload.get("detectedIngredients"));
        requestBody.put("selectedStoredIngredients", payload.get("selectedStoredIngredients"));
        requestBody.put("userPreferences", payload.get("userPreferences"));
        requestBody.put("additionalRequest", payload.get("additionalRequest"));

        String recipeRequest = generateRecipeRequestString(payload);

        // Flask 서버에 요청 보내기
        ResponseEntity<Map> response = sendRequestToFlask(LLM_RECIPE_URL, requestBody, token);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");
            if (responseBody != null && responseBody.containsKey("contents") && responseBody.containsKey("imageLink")) {
                String contents = (String) responseBody.get("contents");
                contents = contents.replace("\n", " ");
                String imageLink = (String) responseBody.get("imageLink");
                RecipeResponse recipeResponse = new RecipeResponse(contents, imageLink);

                chatBotService.saveMessage(token, recipeRequest, contents);

                return Map.of("response", recipeResponse);
            } else {
                return Map.of("error", "'response' 필드에서 'contents' 또는 'imageLink' 값을 찾을 수 없습니다.");
            }
        } else {
            return Map.of("error", "Flask 서버에서 유효하지 않은 응답을 받았습니다.");
        }
    }

    public String generateRecipeRequestString(Map<String, Object> payload) {
        // "detectedIngredients"에서 재료 이름 추출
        List<Map<String, Object>> detectedIngredients = (List<Map<String, Object>>) payload.get("detectedIngredients");
        if (detectedIngredients == null || detectedIngredients.isEmpty()) {
            return "유효한 재료 목록이 없습니다.";
        }

        // 재료 이름을 콤마로 구분하여 문자열로 변환
        String ingredients = detectedIngredients.stream()
                .map(ingredient -> (String) ingredient.get("name"))
                .filter(Objects::nonNull) // 이름이 null인 경우 제외
                .collect(Collectors.joining(", "));

        // 결과 문자열 생성
        return ingredients + "로 만들 수 있는 레시피 알려줘";
    }

    // GPT (일반 질문 관련 요청)
    @PostMapping("/general/questions")
    public ResponseEntity<?> askToGPT(@RequestBody Map<String, String> payload, @RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);

        String question = payload.get("question");
        String searchResults = payload.getOrDefault("search_results", "");

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "질문이 제공되지 않았습니다."));
        }

        Map<String, Object> flaskRequest = new HashMap<>();
        flaskRequest.put("question", question);
        flaskRequest.put("search_results", searchResults);

        // Flask 서버에 요청 보내기
        ResponseEntity<Map> response = sendRequestToFlask(LLM_GENERAL_URL, flaskRequest, accessToken);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String responseContent = (String) response.getBody().get("response");
            chatBotService.saveMessage(token, question, responseContent);
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Flask 서버로부터 유효하지 않은 응답이 반환되었습니다."));
        }
    }

    // 메시지 조회
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessageHistory(@RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);

        List<Message> messages = chatBotService.getAllMessagesByUser(token);

        List<ChatMessageDTO> chatMessageDTOs = messages.stream()
                .map(message -> new ChatMessageDTO(
                        message.getId(),
                        message.getQuestion(),
                        message.getResponse()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatMessageDTOs);
    }

    // 유저별 메시지 삭제
    @DeleteMapping("/messages")
    public ResponseEntity<Void> deleteAllMessages(@RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);
        chatBotService.deleteAllMessagesByUser(token);
        return ResponseEntity.noContent().build();
    }

    // 공통 요청 처리 메서드
    private ResponseEntity<Map> sendRequestToFlask(String url, Map<String, Object> requestBody, String accessToken) {
        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Flask 서버와의 통신 중 오류 발생: " + e.getMessage()));
        }
    }

    // 공통된 헤더 생성 메서드
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        return headers;
    }

    // 토큰 추출 메서드
    private String extractToken(String accessToken) {
        return accessToken.replace("Bearer ", "");
    }
}
