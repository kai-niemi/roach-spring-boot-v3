package io.roach.spring.blob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    static class AttachmentAssembler implements SimpleRepresentationModelAssembler<Attachment> {
        @Override
        public void addLinks(EntityModel<Attachment> resource) {
            resource.add(linkTo(methodOn(AttachmentController.class).getAttachment(resource.getContent().getId())).withSelfRel()
                    .andAffordance(afford(methodOn(AttachmentController.class).deleteAttachment(resource.getContent().getId()))));
            resource.add(linkTo(methodOn(AttachmentController.class).downloadAttachment(resource.getContent().getId())).withRel("download"));
            resource.add(linkTo(methodOn(AttachmentController.class).findAttachments(PageRequest.ofSize(5))).withRel("attachments"));
        }

        @Override
        public void addLinks(CollectionModel<EntityModel<Attachment>> resources) {
            resources
                    .add(linkTo(methodOn(AttachmentController.class).findAttachments(PageRequest.ofSize(5))).withSelfRel()
                            .andAffordance(afford(methodOn(AttachmentController.class).submitAttachment(null, null, null)))
                            .andAffordance(afford(methodOn(AttachmentController.class).deleteAllAttachments())));
        }
    }

    private final AttachmentAssembler attachmentAssembler = new AttachmentAssembler();

    @Autowired
    private PagedResourcesAssembler<Attachment> attachmentPagedResourcesAssembler;

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping
    public PagedModel<EntityModel<Attachment>> findAttachments(@PageableDefault(size = 5, direction = Sort.Direction.ASC) Pageable page) {
        return attachmentPagedResourcesAssembler
                .toModel(attachmentService.findAll(page), attachmentAssembler);
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
    public ResponseEntity<StreamingResponseBody> downloadAttachment(@PathVariable("id") Long id) {
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
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllAttachments() {
        attachmentService.deleteAllAttachments();
        return ResponseEntity.noContent().build();
    }
}
