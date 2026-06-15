package com.nexuscommerce.stores.domain;

/** Lifecycle status of a store (mirrored by a DB CHECK constraint). */
public enum StoreStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
