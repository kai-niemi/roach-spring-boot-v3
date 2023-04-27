package io.roach.spring.trees;

import io.roach.spring.blob.Attachment;
import io.roach.spring.blob.AttachmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

import java.io.*;

@Disabled
@ActiveProfiles({"verbose", "dev"})
public class AttachmentTest extends AbstractIntegrationTest {
    @Autowired
    private AttachmentService attachmentService;

    @Test
    @Order(1)
    public void clearTestData() {
        attachmentService.deleteAllAttachments();
    }

    @Test
    @Order(2)
    public void setupTestData() throws FileNotFoundException {
        File f = ResourceUtils.getFile("classpath:test.jpg");
        Assertions.assertTrue(f.exists());
        attachmentService.createAttachment("a.jpg", "image",
                new FileInputStream(f), (int) f.length(), "JPG");
        attachmentService.createAttachment("b.jpg", "image",
                new FileInputStream(f), (int) f.length(), "JPG");
    }

    @Test
    public void findAll() {
        Page<Attachment> attachments = attachmentService.findAll(Pageable.ofSize(5));
        Assertions.assertTrue(attachments.getTotalElements() >= 2);
    }

    @Test
    public void findByName() {
        Assertions.assertNotNull(attachmentService.findByName("a.jpg"));
        Assertions.assertNotNull(attachmentService.findByName("b.jpg"));
    }

    @Test
    public void testUpdating() {
        Attachment a = attachmentService.findByName("a.jpg");
        Assertions.assertNotNull(a);
        a.setDescription("new description");
        attachmentService.updateAttachment(a);

        a = attachmentService.findByName("a.jpg");
        Assertions.assertEquals("new description", a.getDescription());
    }

    @Test
    public void testStreaming() {
        Attachment a = attachmentService.findByName("a.jpg");
        Assertions.assertNotNull(a);

        ByteArrayOutputStream bao = new ByteArrayOutputStream((int) a.getContentLength());
        attachmentService.streamAttachment(a.getId(), bao);

        Assertions.assertEquals(a.getContentLength(), bao.toByteArray().length);

        long cs = attachmentService.calcAttachmentChecksum(new ByteArrayInputStream(bao.toByteArray()));
        Assertions.assertEquals(a.getChecksum(), cs);
    }
}
