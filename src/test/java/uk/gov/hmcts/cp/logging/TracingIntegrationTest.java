package uk.gov.hmcts.cp.logging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {"filter.enable=false"})
@Slf4j
class TracingIntegrationTest {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String LOGGER_NAME = "logger_name";
    public static final String MESSAGE = "message";
    @Value("${spring.application.name}")
    private String springApplicationName;

    @Resource
    private MockMvc mockMvc;

    private final PrintStream originalStdOut = System.out;

    @AfterEach
    void afterEach() {
        System.setOut(originalStdOut);
    }

    @Test
    void incoming_request_should_add_new_tracing() throws Exception {
        final ByteArrayOutputStream capturedStdOut = captureStdOut();
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        final String logMessage = capturedStdOut.toString(Charset.defaultCharset());
        final Map<String, Object> capturedFields = new ObjectMapper().readValue(logMessage, new TypeReference<>() {
        });
        assertThat(capturedFields.get(TRACE_ID)).isNotNull();
        assertThat(capturedFields.get(SPAN_ID)).isNotNull();
        assertThat(capturedFields.get(LOGGER_NAME)).isEqualTo("uk.gov.hmcts.cp.controllers.RootController");
        assertThat(capturedFields.get(MESSAGE)).isEqualTo("START");
    }

    @Test
    void incoming_request_with_traceId_should_pass_through() throws Exception {
        final ByteArrayOutputStream capturedStdOut = captureStdOut();
        final MvcResult result = mockMvc.perform(get("/")
                        .header(TRACE_ID, "1234-1234")
                        .header(SPAN_ID, "567-567"))
                .andExpect(status().isOk())
                .andReturn();

        final String logMessage = capturedStdOut.toString(Charset.defaultCharset());
        final Map<String, Object> capturedFields = new ObjectMapper().readValue(logMessage, new TypeReference<>() {
        });
        assertThat(capturedFields.get(TRACE_ID)).isEqualTo("1234-1234");
        assertThat(capturedFields.get(SPAN_ID)).isEqualTo("567-567");
        assertThat(capturedFields.get("applicationName")).isEqualTo(springApplicationName);

        assertThat(result.getResponse().getHeader(TRACE_ID)).isEqualTo(capturedFields.get(TRACE_ID));
        assertThat(result.getResponse().getHeader(SPAN_ID)).isEqualTo(capturedFields.get(SPAN_ID));
    }

    private ByteArrayOutputStream captureStdOut() {
        final ByteArrayOutputStream capturedStdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdOut, false, Charset.defaultCharset()));
        return capturedStdOut;
    }
}
