package uk.gov.hmcts.cp.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Simple exists so that we can demonstrate best practice
 */
@Service
@Slf4j
@AllArgsConstructor
public class ExampleClient {

    private RestTemplate restTemplate;

    public String getData() {
        final String url = "https://example.com/data";
        log.info("STARTED for url:{}", url);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info("COMPLETED with status:{}", response.getStatusCode());
        return response.getBody();
    }
}
