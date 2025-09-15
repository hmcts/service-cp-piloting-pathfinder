package uk.gov.hmcts.cp.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
@SpringBootTest(properties = {"jwt.filter.enabled=false"})
@Slf4j
public class TracingIntegrationTest {

    @Value("${spring.application.name}")
    private String springApplicationName;

    @Resource
    private MockMvc mockMvc;

    @Test
    void incoming_request_log_line1_should_be_url() {
        Map<String, String> capturedFields = captureLogMessageFields("/", null, null);
        assertThat(capturedFields.get("applicationName")).isEqualTo(springApplicationName);
        assertThat(capturedFields.get("message")).isEqualTo("Incoming Request: [GET] /");
    }

    @Test
    void incoming_request_log_line1_should_add_new_tracing() {
        Map<String, String> capturedFields = captureLogMessageFields("/", null, null);
        assertThat(capturedFields.get("applicationName")).isEqualTo(springApplicationName);
        assertThat(capturedFields.get("traceId")).isNotEmpty();
        assertThat(capturedFields.get("spanId")).isNotEmpty();
    }

    @Test
    void incoming_request_with_traceId_should_pass_through() {
        Map<String, String> capturedFields = captureLogMessageFields("/", "12-12", "34-34");
        assertThat(capturedFields.get("applicationName")).isEqualTo(springApplicationName);
        assertThat(capturedFields.get("traceId")).isEqualTo("12-12");
        assertThat(capturedFields.get("spanId")).isEqualTo("34-34");
    }

    @Test
    void incoming_request_to_404_should_still_log() {
        Map<String, String> capturedFields = captureLogMessageFields("/bad-url", null, null);
        assertThat(capturedFields.get("applicationName")).isEqualTo(springApplicationName);
        assertThat(capturedFields.get("message")).isEqualTo("Incoming Request: [GET] /bad-url");
    }

    @SneakyThrows
    private Map<String, String> captureLogMessageFields(String url, String traceId, String spanId) {
        final PrintStream originalStdOut = System.out;
        ByteArrayOutputStream capturedStdOut = captureStdOut();
        if (traceId == null) {
            mockMvc.perform(get(url));
        } else {
            mockMvc.perform(get(url)
                    .header("traceId", traceId)
                    .header("spanId", spanId));
        }
        String logMessages = capturedStdOut.toString();
        String logMessageLine1 = logMessages.split("\\n")[0];
        Map<String, String> capturedLogFields = new ObjectMapper().readValue(logMessageLine1, new TypeReference<>() {
        });
        System.setOut(originalStdOut);
        return capturedLogFields;
    }

    private ByteArrayOutputStream captureStdOut() {
        final ByteArrayOutputStream capturedStdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdOut));
        return capturedStdOut;
    }
}
