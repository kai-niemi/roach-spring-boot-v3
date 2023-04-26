package io.roach.spring.blob;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    static class AttachmentAssembler implements SimpleRepresentationModelAssembler<Attachment> {
        @Override
        public void addLinks(EntityModel<Attachment> resource) {
            resource.add(linkTo(methodOn(AttachmentController.class)
                    .getAttachment(resource.getContent().getId()))
                    .withSelfRel()
                    .andAffordance(afford(methodOn(AttachmentController.class)
                            .streamAttachment(resource.getContent().getId())))
                    .withRel("download")
                    .andAffordance(afford(methodOn(AttachmentController.class)
                            .deleteAttachment(resource.getContent().getId()))));
        }

        @Override
        public void addLinks(CollectionModel<EntityModel<Attachment>> resources) {
            resources
                    .add(linkTo(methodOn(AttachmentController.class)
                            .findAttachments())
                            .withSelfRel())
                    .add(linkTo(methodOn(AttachmentController.class)
                            .getFormTemplate(null))
                            .withRel("form")
                            .withTitle("Attachment form template"));
        }
    }

    @Autowired
    private AttachmentService attachmentService;

    private AttachmentAssembler attachmentAssembler = new AttachmentAssembler();

    @GetMapping
    public CollectionModel<EntityModel<Attachment>> findAttachments() {
        return attachmentAssembler.toCollectionModel(attachmentService.findAll());
    }

    @GetMapping(value = "/form")
    public EntityModel<Attachment> getFormTemplate(@RequestParam Map<String, String> requestParams) {
        Attachment form = new Attachment();
        form.setName(requestParams.getOrDefault("name", "name"));

        EntityModel<Attachment> model = EntityModel.of(form);
        model.add(linkTo(methodOn(getClass()).getFormTemplate(requestParams)).withSelfRel()
                .andAffordance(afford(methodOn(getClass()).submitAttachment(null, null, null))));

        return model;
    }

    @PostMapping("/form")
    public ResponseEntity<EntityModel<Attachment>> submitAttachment(@RequestParam("fileName") String filename,
                                                                    @RequestParam("description") String description,
                                                                    @RequestParam("content") MultipartFile content) {
        try {
            Attachment attachment = attachmentService.createAttachment(
                    filename,
                    description,
                    content.getInputStream(),
                    content.getSize(),
                    content.getContentType());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(attachmentAssembler.toModel(attachment));
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    @GetMapping(value = "/{id}")
    public EntityModel<Attachment> getAttachment(@PathVariable("id") Long id) {
        return attachmentAssembler.toModel(attachmentService.findById(id));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<StreamingResponseBody> streamAttachment(@PathVariable("id") Long id) {
        Attachment attachment = attachmentService.findById(id);
        final StreamingResponseBody responseBody = outputStream -> {
            attachmentService.streamAttachment(attachment, outputStream);
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, attachment.getContentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(responseBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable("id") Long id) {
        return ResponseEntity.ok().build();
    }
}
