package com.nexuscommerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuscommerce.orders.domain.Order;
import com.nexuscommerce.orders.repository.OrderRepository;
import com.nexuscommerce.stores.api.dto.StoreCreateRequest;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.domain.StoreType;
import com.nexuscommerce.stores.service.StoreService;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.service.UserService;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end authorization / abuse checks through the real filter chain, controllers,
 * PostgreSQL and MongoDB (Testcontainers). Runs under Failsafe ({@code mvn verify}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SecurityIntegrationIT {

    private static final String PASSWORD = "Sup3r-secret-pw";
    private static final String COOKIE = "nexus_refresh";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("nexuscommerce")
            .withUsername("nexus")
            .withPassword("nexus");

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("app.jwt.secret",
                () -> Base64.getEncoder().encodeToString("integration-test-secret-key-0123456789".getBytes()));
        registry.add("app.encryption.key",
                () -> Base64.getEncoder().encodeToString("integration-test-aes-key-0123456".getBytes()));
        // Every test logs in from 127.0.0.1: keep the per-IP budget out of the way and
        // make the per-account lockout small enough to exercise.
        registry.add("app.auth.rate-limit.login-attempts-per-ip", () -> "1000");
        registry.add("app.auth.rate-limit.registrations-per-ip", () -> "1000");
        registry.add("app.auth.rate-limit.login-failures-per-email", () -> "3");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserService userService;
    @Autowired private StoreService storeService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // ---- helpers ------------------------------------------------------------------

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@it.local";
    }

    private User createUser(String role) {
        return userService.register(uniqueEmail(role.toLowerCase()), role, PASSWORD, role);
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("email", email, "password", password))))
                .andReturn();
    }

    private String bearer(User user) throws Exception {
        MvcResult result = login(user.getEmail(), PASSWORD);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return "Bearer " + body.get("accessToken").asText();
    }

    private StoreResponse storeOwnedBy(User manager) {
        return storeService.create(new StoreCreateRequest(
                "Store " + UUID.randomUUID(), "IT store", StoreType.ELECTRONICS), manager.getId(), null);
    }

    // ---- mass assignment ------------------------------------------------------------

    @Test
    void registrationIgnoresClientSuppliedRoles() throws Exception {
        String body = """
                {"email":"%s","password":"%s","fullName":"Mallory","role":"ADMIN","roles":["ADMIN"]}
                """.formatted(uniqueEmail("mallory"), PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.roles.length()").value(1))
                .andExpect(jsonPath("$.user.roles[0]").value("CUSTOMER"))
                .andReturn();

        // Regression: the USER_REGISTERED audit row (FK to users) is written after the user commits.
        UUID userId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/user/id").asText());
        Integer audits = jdbcTemplate.queryForObject("SELECT count(*) FROM commerce.audit_logs "
                + "WHERE actor_user_id = ? AND event_type = 'USER_REGISTERED'", Integer.class, userId);
        assertThat(audits).isEqualTo(1);
    }

    // ---- IDOR -----------------------------------------------------------------------

    @Test
    void customerCannotReadAnotherCustomersOrder() throws Exception {
        User alice = createUser("CUSTOMER");
        User bob = createUser("CUSTOMER");
        Order order = new Order(alice.getId(), "NX-IT-" + UUID.randomUUID().toString().substring(0, 8), "USD");
        order.applyTotals(new BigDecimal("10.00"), new BigDecimal("0.70"));
        order.markPaid();
        UUID orderId = orderRepository.save(order).getId();

        mockMvc.perform(get("/api/orders/{id}", orderId).header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/{id}", orderId).header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void managerCannotTouchAnotherManagersStoreOrItsAnalytics() throws Exception {
        User manager = createUser("STORE_MANAGER");
        User rival = createUser("STORE_MANAGER");
        StoreResponse own = storeOwnedBy(manager);
        StoreResponse rivals = storeOwnedBy(rival);
        String token = bearer(manager);

        mockMvc.perform(put("/api/stores/{id}", rivals.id())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"pwned\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/analytics/stores/{id}/summary", rivals.id())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/analytics/stores/{id}/summary", own.id())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
        // Cross-customer order feed is admin-only.
        mockMvc.perform(get("/api/analytics/orders-summary").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotUseStaffEndpoints() throws Exception {
        String token = bearer(createUser("CUSTOMER"));

        mockMvc.perform(get("/api/analytics/store-revenue").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/stores").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"storeType\":\"FASHION\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadTheOrderFeed() throws Exception {
        mockMvc.perform(get("/api/analytics/orders-summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser("ADMIN"))))
                .andExpect(status().isOk());
    }

    // ---- tampering / validation ---------------------------------------------------------

    @Test
    void cartRejectsOutOfRangeQuantities() throws Exception {
        String token = bearer(createUser("CUSTOMER"));
        for (int quantity : new int[] {-5, 0, 1_000_000}) {
            mockMvc.perform(post("/api/cart/items").header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"productId\":\"anything\",\"quantity\":" + quantity + "}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void regexMetacharactersInSearchAreTreatedLiterally() throws Exception {
        mockMvc.perform(get("/api/catalog/products").param("q", "(a+)+$|.*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/catalog/products").param("q", "a".repeat(101)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/stores").param("sort", "noSuchField"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonIsA400WithoutInternals() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("Exception", "jackson", "at com.");
    }

    // ---- brute force ------------------------------------------------------------------

    @Test
    void accountIsLockedOutAfterRepeatedFailures() throws Exception {
        User victim = createUser("CUSTOMER");
        for (int i = 0; i < 3; i++) {
            assertThat(login(victim.getEmail(), "wrong-password").getResponse().getStatus()).isEqualTo(401);
        }

        // Even the right password is refused until the window passes.
        MvcResult locked = login(victim.getEmail(), PASSWORD);
        assertThat(locked.getResponse().getStatus()).isEqualTo(429);
        assertThat(locked.getResponse().getHeader(HttpHeaders.RETRY_AFTER)).isNotBlank();
    }

    // ---- refresh token ----------------------------------------------------------------

    @Test
    void refreshCookieIsHardened() throws Exception {
        MvcResult result = login(createUser("CUSTOMER").getEmail(), PASSWORD);
        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).contains(COOKIE + "=", "HttpOnly", "SameSite=Lax", "Path=/api/auth");
        assertThat(result.getResponse().getContentAsString()).doesNotContain(
                result.getResponse().getCookie(COOKIE).getValue());
    }

    @Test
    void reusingARotatedRefreshTokenRevokesTheWholeSession() throws Exception {
        User user = createUser("CUSTOMER");
        Cookie first = login(user.getEmail(), PASSWORD).getResponse().getCookie(COOKIE);

        MvcResult rotated = mockMvc.perform(post("/api/auth/refresh").cookie(first))
                .andExpect(status().isOk()).andReturn();
        Cookie second = rotated.getResponse().getCookie(COOKIE);
        assertThat(second.getValue()).isNotEqualTo(first.getValue());

        // Age the rotation past the concurrent-tab grace period, then replay the old token.
        jdbcTemplate.update("UPDATE commerce.refresh_tokens SET revoked_at = now() - interval '1 minute' "
                + "WHERE user_id = ? AND revoked_at IS NOT NULL", user.getId());
        mockMvc.perform(post("/api/auth/refresh").cookie(first)).andExpect(status().isUnauthorized());

        // The legitimate holder's newer token was revoked too.
        mockMvc.perform(post("/api/auth/refresh").cookie(second)).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        Cookie cookie = login(createUser("CUSTOMER").getEmail(), PASSWORD).getResponse().getCookie(COOKIE);

        mockMvc.perform(post("/api/auth/logout").cookie(cookie)).andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/refresh").cookie(cookie)).andExpect(status().isUnauthorized());
    }

    // ---- headers ------------------------------------------------------------------------

    @Test
    void springSecurityDefaultHeadersAreOn() throws Exception {
        mockMvc.perform(get("/api/stores").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    @Test
    void forgedBearerTokenIsRejected() throws Exception {
        String unsigned = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes())
                + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(
                        ("{\"sub\":\"" + UUID.randomUUID() + "\",\"iss\":\"nexuscommerce\",\"roles\":[\"ADMIN\"]}")
                                .getBytes())
                + ".";
        mockMvc.perform(get("/api/analytics/orders-summary").header(HttpHeaders.AUTHORIZATION, "Bearer " + unsigned))
                .andExpect(status().isUnauthorized());
    }
}
