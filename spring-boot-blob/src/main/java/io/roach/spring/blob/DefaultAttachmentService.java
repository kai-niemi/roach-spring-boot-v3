package io.roach.spring.blob;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class DefaultAttachmentService implements AttachmentService {
    @Autowired
    protected AttachmentRepository attachmentRepository;

    @Override
    public List<Attachment> findAll() {
        return attachmentRepository.findAll();
    }

    @Override
    public Attachment findByName(String name) {
        return attachmentRepository.findByName(name);
    }

    @Override
    public Attachment findById(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAttachment(Attachment att) {
        attachmentRepository.delete(att);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAttachment(Attachment proxy) {
        Attachment a = attachmentRepository.getReferenceById(proxy.getId());
        a.setContent(proxy.getContent());
        a.setContentLength(proxy.getContentLength());
        a.setName(proxy.getName());
        a.setDescription(proxy.getDescription());
        a.setChecksum(proxy.getChecksum());
        attachmentRepository.save(a);
    }

    @Override
    public void streamAttachment(Long id, OutputStream os) {
        streamAttachment(attachmentRepository.getReferenceById(id), os);
    }

    @Override
    public void streamAttachment(Attachment attachment, OutputStream os) {
        try (InputStream in = new BufferedInputStream(
                attachment.getContent().getBinaryStream())) {
            FileCopyUtils.copy(in, os);
        } catch (SQLException | IOException e) {
            throw new DataRetrievalFailureException("Error reading attachment data", e);
        }
    }

    @Override
    public long calcAttachmentChecksum(InputStream in) {
        Checksum checksum = new Adler32();
        OutputStream out = new CheckedOutputStream(new ByteArrayOutputStream(), checksum);
        try {
            FileCopyUtils.copy(in, out);
        } catch (IOException e) {
            throw new BusinessException("Error reading attachment data", e);
        }
        return checksum.getValue();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Attachment createAttachment(String name, String description, InputStream is,
                                       long contentLength,
                                       String contentType) {
        try {
            Blob content = BlobProxy.generateProxy(is, contentLength);
            if (content.length() != contentLength) {
                throw new IllegalStateException("Wrote " + content.length()
                        + " but expected " + contentLength);
            }

            Attachment attachment = new Attachment();
            attachment.setContent(content);
            attachment.setContentType(contentType);
            attachment.setContentLength(contentLength);
            attachment.setChecksum(calcAttachmentChecksum(is));
            attachment.setName(name);
            attachment.setDescription(description);

            attachmentRepository.save(attachment);

            return attachment;
        } catch (SQLException e) {
            throw new BusinessException("Error writing attachment data", e);
        }
    }
}
