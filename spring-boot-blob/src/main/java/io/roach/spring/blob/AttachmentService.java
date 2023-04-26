package io.roach.spring.blob;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.io.OutputStream;

public interface AttachmentService {
    /**
     * Create a new attachment with the given attributes.
     */
    Attachment createAttachment(String name, String description,
                                InputStream is, long contentLength,
                                String contentType);

    void updateAttachment(Attachment att);

    /**
     * Makes an attachment transient, e.g deletes it.
     */
    void deleteAttachment(Long id);

    void deleteAllAttachments();

    /**
     * Calculate attachment data checksum.
     */
    long calcAttachmentChecksum(InputStream is);

    /**
     * Stream the given attachment to a target output stream.
     */
    void streamAttachment(Long id, OutputStream os);

    void streamAttachment(Attachment attachment, OutputStream os);

    /**
     * Find all attachments.
     */
    Page<Attachment> findAll(Pageable pageable);

    /**
     * Find an attachment by id.
     */
    Attachment findById(Long id);

    /**
     * Find an attachment by name.
     */
    Attachment findByName(String name);
}
