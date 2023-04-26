package io.roach.spring.blob;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface AttachmentService {
    /**
     * Create a new attachment with the given attributes.
     *
     * @param name          a system unique resource name
     * @param description   informative text (optional, can be null)
     * @param is            the content stream
     * @param contentLength length of content stream
     * @param contentType   content type, e.g jpg/gif/png (optional, can be null)
     * @return the new attachment persisted in the data store
     */
    Attachment createAttachment(String name, String description,
                                InputStream is, long contentLength,
                                String contentType);

    /**
     * Calculate attachment data checksum.
     *
     * @param is data input stream
     * @return the checksum, never 0
     */
    long calcAttachmentChecksum(InputStream is);

    /**
     * Stream the given attachment to a target output stream.
     *
     * @param id the attachment id
     * @param os the output stream
     */
    void streamAttachment(Long id, OutputStream os);

    void streamAttachment(Attachment attachment, OutputStream os);

    /**
     * Find all Attachments.
     *
     * @return list of attachments
     */
    List<Attachment> findAll();

    /**
     * Find an attachment by id.
     *
     * @param id the id
     * @return the Attachment or null if it doesnt exist
     */
    Attachment findById(Long id);

    /**
     * Find an attachment by name.
     *
     * @param name the name
     * @return the Attachment or null if it doesnt exist
     */
    Attachment findByName(String name);

    void updateAttachment(Attachment att);

    /**
     * Makes an attachment transient, e.g deletes it.
     *
     * @param att the entity to delete
     */
    void deleteAttachment(Attachment att);
}
