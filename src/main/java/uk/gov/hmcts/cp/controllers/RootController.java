package uk.gov.hmcts.cp.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.filter.AuthDetails;
import uk.gov.hmcts.cp.service.DummyService;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@AllArgsConstructor
@RestController
@Slf4j
public class RootController {

    private DummyService dummyService;
    private AuthDetails authDetails;

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on. This is the endpoint to deal with that and therefore silence the
     * unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        log.info("Auth filter added userName:{}", authDetails.getUserName());
        dummyService.dummyMethod();
        return ok(authDetails.getUserName());
    }
}
