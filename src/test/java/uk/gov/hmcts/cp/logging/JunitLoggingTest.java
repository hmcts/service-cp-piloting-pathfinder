package uk.gov.hmcts.cp.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class JunitLoggingTest {

    @Test
    void junit_should_log_correct_fields() throws JsonProcessingException {
        MDC.put("traceId", "1234-1234");
        final ByteArrayOutputStream capturedStdOut = captureStdOut();
        log.info("junit test message");

        final Map<String, Object> capturedFields = new ObjectMapper().readValue(capturedStdOut.toString(Charset.defaultCharset()), new TypeReference<>() {
        });

        assertThat(capturedFields.get("traceId")).isEqualTo("1234-1234");
        assertThat(capturedFields.get("timestamp")).isNotNull();
        assertThat(capturedFields.get("logger_name")).isEqualTo("uk.gov.hmcts.cp.logging.JunitLoggingTest");
        assertThat(capturedFields.get("thread_name")).isEqualTo("Test worker");
        assertThat(capturedFields.get("level")).isEqualTo("INFO");
        assertThat(capturedFields.get("message")).isEqualTo("junit test message");
    }

    private ByteArrayOutputStream captureStdOut() {
        final ByteArrayOutputStream capturedStdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdOut, false, Charset.defaultCharset()));
        return capturedStdOut;
    }
}
