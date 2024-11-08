package com.example.loginDemo.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Authorization 헤더가 null이거나 "Bearer "로 시작하지 않으면 필터를 계속 진행
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 뒤의 token 추출
        jwt = authHeader.substring("Bearer ".length());
        // Access Token인지 Refresh Token인지 구분하기
        boolean isAccessToken = jwtService.isAccessToken(jwt);
        // 토큰이 Access Token인지를 확인하는 메서드
        if (isAccessToken) {
            userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                //만료기간 검증
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                    response.getWriter().write("JWT token is expired or invalid");
                    return;
                }
                // 인증 성공, SecurityContext에 인증 정보 저장
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            // Refresh Token의 경우 처리 로직 추가
            // refresh 유효한지 확인
            String username = jwtService.extractUsername(jwt);
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 리프레시 토큰이 유효한지 확인
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 리프레시 토큰이 유효하면 새로운 액세스 토큰 발급
                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    response.setHeader("New-Access-Token", newAccessToken); // 새로운 액세스 토큰을 응답 헤더에 추가
                } else {
                    // 리프레시 토큰이 만료된 경우, 클라이언트는 다시 로그인해야 함
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                    response.getWriter().write("Refresh token is expired or invalid");
                    return;
                }
            }
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
