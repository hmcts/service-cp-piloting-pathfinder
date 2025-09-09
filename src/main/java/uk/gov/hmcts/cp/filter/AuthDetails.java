package uk.gov.hmcts.cp.filter;

import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@Resource
public class AuthDetails {

    private String userName;
}
