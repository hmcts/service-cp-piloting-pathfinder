package uk.gov.hmcts.cp.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static uk.gov.hmcts.cp.filter.AuthFilter.JWT_TOKEN_HEADER;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AuthFilterIntegrationTest {

    @Resource
    MockMvc mockMvc;

    @Test
    void authFilter_should_throw_error() throws Exception {
        log.info("");
        assertThrows(HttpClientErrorException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders.get("/"))
                        .andDo(print()));
    }

    @Test
    void authFilter_should_accept_dummy_jwt() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header(JWT_TOKEN_HEADER, "dummy-jwt"))
                .andDo(print())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Demo Authorised User");
    }
}
