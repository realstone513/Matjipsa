package com.example.loginDemo.chat;

import com.example.loginDemo.domain.Message;
import com.example.loginDemo.domain.User;
import com.example.loginDemo.exception.FlaskCommunicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ChatBotController {

    private static final String LLM_URL = "http://localhost:5002/ask";
    private final ChatBotService chatBotService;

    @PostMapping("/messages")
    public Map<String, String> askToLLM(@RequestBody Map<String, String> payload,
                                           @AuthenticationPrincipal User user) {

        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Map.of("error", "질문이 제공되지 않았습니다.");
        }

        RestTemplate restTemplate = new RestTemplate();

        // 요청 데이터
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("question", question);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Flask 서버로 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(LLM_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String botResponse = response.getBody().get("response").toString();

                // DB에 메시지 저장
                chatBotService.saveMessage(request, response, user);

                return Map.of("response", botResponse);
            } else {
                throw new FlaskCommunicationException("Flask 서버로부터 유효하지 않은 응답이 반환되었습니다.");
            }
        } catch (Exception e) {
            throw new FlaskCommunicationException("Flask 서버와의 통신 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessageHistory(@AuthenticationPrincipal User user,
                                                           @RequestParam(required = false) String filter) {
        List<Message> messages;

        if ("user".equalsIgnoreCase(filter)) {
            messages = chatBotService.getMessagesByUser(user);
        } else {
            messages = chatBotService.getAllMessages();
        }

        return ResponseEntity.ok(messages);
    }

}
