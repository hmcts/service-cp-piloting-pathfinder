package uk.gov.hmcts.cp.filters.auth;

import java.io.Serial;

public class InvalidJWTException extends Exception {

    @Serial
    private static final long serialVersionUID = 4719620548546239143L;

    public InvalidJWTException(final String message) {
        super(message);
    }
}
