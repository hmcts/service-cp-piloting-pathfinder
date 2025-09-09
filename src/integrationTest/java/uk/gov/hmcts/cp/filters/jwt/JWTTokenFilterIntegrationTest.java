package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.filters.jwt.JWTTokenFilter.JWT_TOKEN_HEADER;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@AutoConfigureMockMvc
class JWTTokenFilterIntegrationTest {

    @Resource
    MockMvc mockMvc;

    @Resource
    private JwtTokenService jwtTokenService;

    @Test
    void shouldPassWhenTokenIsValid() throws Exception {
        String jwtToken = jwtTokenService.createToken();
        mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/")
                                .header(JWT_TOKEN_HEADER, jwtToken)
                ).andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenTokenIsMissing() {
        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(() -> mockMvc
                        .perform(
                                MockMvcRequestBuilders.get("/")
                        ))
                .withMessageContaining("No jwt token passed");
    }
}