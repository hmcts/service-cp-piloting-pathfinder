package uk.gov.hmcts.cp.filters.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.filters.auth.JWTFilter.JWT_TOKEN_HEADER;

import java.time.Duration;
import java.util.Date;

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
    MockMvc mockMvc;

    @Resource
    private JWTService jwtService;

    @Test
    void should_pass_when_token_is_valid() throws Exception {
        String jwtToken = jwtService.createToken();
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
    void should_reject_missing_token() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().string("{\"error\":\"401\",\"message\":\"No jwt token passed\"}")
                        );
    }

    @Test
    void should_reject_expired_token() throws Exception {
        String expiredJwtToken = jwtService.createToken(Date.from(new Date().toInstant().minus(Duration.ofHours(1))));
        mockMvc.perform(MockMvcRequestBuilders.get("/").header(JWT_TOKEN_HEADER, expiredJwtToken))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().string(containsString("Expired tokens:JWT expired"))
                        );
    }
}