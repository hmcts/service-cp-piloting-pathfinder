# HMCTS API Marketplace Logging


# Example Tests
See JunitLoggingTest.java
See SpringLoggingIntegrationTest.java
These tests illustrates the fields that we add to our logging 


# Using logback.xml
Configuring logging with logback.xml is the simplest approach
We can add any global logging fields by adding to MDC in any request filter
i.e. In TracingFilter.java we have these lines to add applicationName to every log entry 
```
        public TracingFilter(@Value("${spring.application.name}") String applicationName)
        ...
        MDC.put(APPLICATION_NAME, applicationName);
```


# Using logback-spring.xml
Currently, configuring logging with logback-spring.xml is more complex and seems to be quite fragile.
Bringing in dependencies that include logback.xml or commons-logging can cause the logging to revert to basic logging
It is imperative that any usage of logback spring is covered with a strong integration test such as the example SpringLoggingIntegrationTest.java


# Collecting logs in Azure
We want to produce logging as json with fields as detailed below
We currently output to stdout and expect that the k8s containers will be wired to capture the logging into azure logging 
Once we work closer with Azure ops teams we expect to implement an spring boot azure plugin to directly inject logs to azure


# Fields
See logback.xml for master list. Or better still, run the unit test or integration to see the actual logged entries.
mdc - Logs any objects added to MDC e.g. io.micrometer adds traceId and spanId to MDC. We can add any custom fields to this.


# Testing
We have strong unit and spring boot integration tests that assert the individual fields in log entries
We dont currently have a test to prove the main Application logging is correct
This may need to be done outside the app test suite maybe as part of a docker test
