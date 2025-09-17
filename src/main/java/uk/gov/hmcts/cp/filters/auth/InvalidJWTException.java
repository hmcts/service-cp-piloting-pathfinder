package uk.gov.hmcts.cp.filters.auth;

public class InvalidJWTException extends Exception {
    public InvalidJWTException(String message) {
        super(message);
    }
}
