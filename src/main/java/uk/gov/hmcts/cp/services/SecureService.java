package uk.gov.hmcts.cp.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class SecureService {

    public static final String AUTHENTICATED_USER_ACCESS_MESSAGE = "Accessible to only authenticated users";

    @PreAuthorize("isAuthenticated()")
    public String accessUserData() {
        return AUTHENTICATED_USER_ACCESS_MESSAGE;
    }


}
