package uk.gov.hmcts.cp.logging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {"jwt.filter.enabled=false"})
@Slf4j
public class TracingIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    private PrintStream originalStdOut = System.out;

    @AfterEach
    void afterEach() {
        System.setOut(originalStdOut);
    }

    @Test
    void incoming_request_should_add_tracing() throws Exception {
        ByteArrayOutputStream capturedStdOut = captureStdOut();
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String logMessage = capturedStdOut.toString();
        Map<String, Object> capturedFields = new ObjectMapper().readValue(logMessage, new TypeReference<>() {
        });
        assertThat(capturedFields.get("traceId")).isNotNull();
        assertThat(capturedFields.get("spanId")).isNotNull();
        assertThat(capturedFields.get("logger_name")).isEqualTo("uk.gov.hmcts.cp.controllers.RootController");
        assertThat(capturedFields.get("message")).isEqualTo("START");

        // Do we want to ensure that the traceId and spanId are returned on the response headers ?
        // Feels sensible ... tbc
        // MvcResult result = mockMvc.perform(...)
        // Collection<String> headers = result.getResponse().getHeaderNames();
        // assertThat(result.getResponse().getHeader("traceId")).isEqualTo(capturedFields.get("traceId"));
    }

    private ByteArrayOutputStream captureStdOut() {
        final ByteArrayOutputStream capturedStdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdOut));
        return capturedStdOut;
    }
}
