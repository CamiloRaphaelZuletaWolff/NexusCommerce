package com.nexuscommerce.audit.domain;

/** Sensitive actions captured in the audit trail (extended in later phases). */
public enum AuditEventType {
    USER_REGISTERED,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    TOKEN_REFRESHED,
    LOGOUT,
    STORE_CREATED,
    PRODUCT_CREATED
}
