package com.fourmen.meetingplatform.config.csrf;

import com.fourmen.meetingplatform.config.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CsrfProtectionFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final String[] permitAllUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final Set<String> SAFE_METHODS = new HashSet<>(Arrays.asList("GET", "HEAD", "OPTIONS"));

    public CsrfProtectionFilter(JwtTokenProvider jwtTokenProvider, String[] permitAllUrls) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.permitAllUrls = permitAllUrls;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();
        return Arrays.stream(permitAllUrls).anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String csrfTokenFromHeader = request.getHeader(CSRF_HEADER_NAME);

        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (csrfTokenFromHeader == null || accessToken == null) {
            throw new AccessDeniedException("CSRF 토큰이 없습니다.");
        }

        final String csrfTokenFromJwt = jwtTokenProvider.getCsrfToken(accessToken);

        if (!csrfTokenFromHeader.equals(csrfTokenFromJwt)) {
            throw new AccessDeniedException("CSRF 토큰이 일치하지 않습니다.");
        }

        filterChain.doFilter(request, response);
    }
}