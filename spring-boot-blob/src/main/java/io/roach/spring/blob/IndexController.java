package io.roach.spring.blob;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public ResponseEntity<RepresentationModel<?>> getApiIndex()
            throws IOException {
        RepresentationModel<?> index = new RepresentationModel<>();

        index.add(WebMvcLinkBuilder
                .linkTo(methodOn(AttachmentController.class)
                        .findAttachments())
                .withRel("attachment")
                .withTitle("Attachment collection resource"));

        return ResponseEntity.ok(index);
    }
}
