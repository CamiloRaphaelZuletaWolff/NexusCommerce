package com.nexuscommerce.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/** Client request context (IP + user-agent) captured for audit logging. */
public record RequestMetadata(String ipAddress, String userAgent) {

    public static RequestMetadata from(HttpServletRequest request) {
        if (request == null) {
            return new RequestMetadata(null, null);
        }
        return new RequestMetadata(clientIp(request), trim(request.getHeader("User-Agent"), 255));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            // First entry is the originating client.
            return trim(forwarded.split(",")[0].trim(), 64);
        }
        return trim(request.getRemoteAddr(), 64);
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
