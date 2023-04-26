package io.roach.spring.blob;

import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public ResponseEntity<RepresentationModel<?>> getApiIndex() {
        RepresentationModel<?> index = new RepresentationModel<>();

        index.add(WebMvcLinkBuilder
                .linkTo(methodOn(AttachmentController.class)
                        .findAttachments(PageRequest.ofSize(5)))
                .withRel("attachment")
                .withTitle("Attachment collection resource")
                .withName("attachment collection"));

        String rootUri =
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .buildAndExpand()
                        .toUriString();
        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("browser/index.html")
                        .fragment("theme=Cosmo&uri=" + rootUri)
                        .buildAndExpand()
                        .toUriString())
                .withName("browser")
                .withRel("hal-explorer")
                .withTitle("REST API browser")
                .withType(MediaType.TEXT_HTML_VALUE)
        );

        return ResponseEntity.ok(index);
    }
}
