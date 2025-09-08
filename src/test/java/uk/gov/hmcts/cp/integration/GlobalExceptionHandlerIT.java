package uk.gov.hmcts.cp.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@TestPropertySource(properties = {"auth.filter.enabled=false"})
public class GlobalExceptionHandlerIT {

  @Resource
  MockMvc mockMvc;

  @Test
  void shouldCreateNewTraceIdAndExposeInLogAndResponse() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/"))
        .andDo(print())
        .andReturn();
    log.info("Completed with result:{}", result.getResponse().getStatus());
  }
}
