package uk.gov.hmcts.cp.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.filter.AuthDetails;

@Service
@AllArgsConstructor
@Slf4j
public class DummyService {

    AuthDetails authDetails;

    public String dummyMethod() {
        log.info("dummyService can access authDetails with userName:{}", authDetails.getUserName());
        return "Welcome to service-hmcts-marketplace-piloting-pathfinder";
    }
}
