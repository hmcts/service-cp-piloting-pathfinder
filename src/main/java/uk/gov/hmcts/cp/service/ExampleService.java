package uk.gov.hmcts.cp.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.client.ExampleClient;

/**
 * Simple exists so that we can demonstrate best practice
 */
@Service
@AllArgsConstructor
public class ExampleService {

    private ExampleClient exampleClient;

    public String getData() {
        return exampleClient.getData();
    }
}
