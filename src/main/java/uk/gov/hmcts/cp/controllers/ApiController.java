package uk.gov.hmcts.cp.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.service.ExampleService;

import static org.springframework.http.ResponseEntity.ok;

@RestController()
@AllArgsConstructor
@RequestMapping(path = "api", produces = MediaType.TEXT_PLAIN_VALUE)
public class ApiController {

    private ExampleService exampleService;

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ok(exampleService.getData());
    }

}
