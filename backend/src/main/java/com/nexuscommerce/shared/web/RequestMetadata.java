package com.nexuscommerce.shared.web;

import jakarta.servlet.http.HttpServletRequest;

/** Client request context (IP + user-agent) captured for audit logging. */
public record RequestMetadata(String ipAddress, String userAgent) {

    public static RequestMetadata from(HttpServletRequest request) {
        if (request == null) {
            return new RequestMetadata(null, null);
        }
        return new RequestMetadata(clientIp(request), trim(request.getHeader("User-Agent"), 255));
    }

    /**
     * The client IP as resolved by Spring's {@code ForwardedHeaderFilter}
     * ({@code server.forward-headers-strategy: framework}). Never read
     * {@code X-Forwarded-For} here directly: it is client-controlled, so trusting it
     * would let anyone spoof the audit-log IP and dodge per-IP rate limits.
     */
    private static String clientIp(HttpServletRequest request) {
        return trim(request.getRemoteAddr(), 64);
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
