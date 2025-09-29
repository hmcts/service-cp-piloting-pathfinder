package uk.gov.hmcts.cp.filters.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.filters.auth.JWTFilter.JWT_TOKEN_HEADER;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"filter.enable=true", "auth.provider=jwt",})
@AutoConfigureMockMvc
class JWTFilterIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private JWTService jwtService;

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    void should_pass_when_token_is_valid() throws Exception {
        final String jwtToken = jwtService.createToken();
        mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/")
                                .header(JWT_TOKEN_HEADER, jwtToken)
                ).andExpectAll(
                        status().isOk(),
                        content().string("Welcome to service-hmcts-marketplace-piloting-pathfinder, " + JWTService.USER)
                );
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    void should_reject_missing_token() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().string("{\"error\":\"401\",\"message\":\"No jwt token passed\"}")
                        );
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    void should_reject_expired_token() throws Exception {
        final String expiredJwtToken = jwtService.createToken(Instant.now().minus(1, ChronoUnit.HOURS));
        mockMvc.perform(MockMvcRequestBuilders.get("/").header(JWT_TOKEN_HEADER, expiredJwtToken))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().string(containsString("Expired tokens:JWT expired"))
                        );
    }
}