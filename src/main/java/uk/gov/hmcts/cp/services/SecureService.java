package uk.gov.hmcts.cp.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class SecureService {
    @PreAuthorize("isAuthenticated()")
    public String accessUserData() {
        return "Accessible to only user or admin";
    }
}
