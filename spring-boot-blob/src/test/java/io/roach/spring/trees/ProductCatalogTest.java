package io.roach.spring.trees;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import io.roach.spring.blob.Attachment;
import io.roach.spring.blob.AttachmentService;

@Disabled
public class ProductCatalogTest extends AbstractIntegrationTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AttachmentService attachmentService;

    @Test
    @Order(1)
    @Transactional
    @Commit
    public void clearTestData() {
    }

    @Test
    @Order(2)
    public void setupTestData() throws FileNotFoundException {
        File f = ResourceUtils.getFile("classpath:test.jpg");
        attachmentService.createAttachment("a.jpg", "image",
                new FileInputStream(f), (int) f.length(), "JPG");
        attachmentService.createAttachment("b.jpg", "image",
                new FileInputStream(f), (int) f.length(), "JPG");
    }

    @Test
    public void findAll() {
        List<Attachment> attachments = attachmentService.findAll();
        Assertions.assertTrue(attachments.size() >= 2);
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
