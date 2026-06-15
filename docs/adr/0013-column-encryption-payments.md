# ADR-0013: Column-level encryption of payment data

- **Status:** Accepted
- **Date:** 2026-06-15
- **Phase:** 4

## Context

Payment card data is sensitive and must not be stored in clear text. The project
requires demonstrating encryption of sensitive data, while keeping the demo
runnable. (In a real system you would not store the PAN at all — you would hold a
PSP token. This is documented as a deliberate demo simplification.)

## Decision

- **Application-side AES-256-GCM** via a JPA `AttributeConverter`
  (`EncryptedStringConverter`) applied to the `card_holder`, `card_number` and
  `card_expiry` fields of `Payment`. Ciphertext (Base64 of `iv ‖ ciphertext+tag`)
  is stored in `TEXT` columns; values are transparently encrypted on write and
  decrypted on read.
- Only **non-sensitive** fields are stored in clear: `card_last4` and `card_brand`
  (for display). The **CVV is never stored**.
- The key is supplied via `APP_ENCRYPTION_KEY` (env); a dev-only default lives in
  `application-dev.yml`. The key is installed into a static `CryptoSupport` holder
  at startup so the Hibernate-instantiated converter can use it.
- Chosen over pgcrypto so the **key never reaches the database** and rotation is an
  application concern.

## Consequences

- ✅ Card data is unreadable at rest — verified: only Base64 ciphertext + `last4`
  appear in the `payments` table; the raw PAN never does.
- ✅ GCM provides authenticated encryption (tamper detection); a random IV per
  value means identical inputs produce different ciphertext.
- ✅ APIs expose only `brand` + `last4`, never the encrypted fields.
- ⚠️ Single symmetric key; key rotation would require re-encryption (envelope
  encryption/KMS is the production path).
- ⚠️ Storing an (encrypted) PAN is a demo-only choice; production should tokenize
  via a PSP and never persist the PAN.
