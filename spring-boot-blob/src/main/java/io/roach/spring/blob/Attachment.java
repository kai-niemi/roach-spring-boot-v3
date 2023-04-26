package io.roach.spring.blob;

import io.roach.spring.blob.support.AbstractPersistentEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "attachment")
public class Attachment extends AbstractPersistentEntity<Long> {
    /**
     * Surrogate database identity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Attachment display name.
     */
    @Column(length = 64, nullable = false)
    @NotBlank
    private String name;

    /**
     * Data content type, e.g JPG, GIF or other.
     */
    @Column(name = "content_type", length = 15)
    @NotBlank
    private String contentType;

    /**
     * Data size in bytes.
     */
    @Column(name = "content_length", nullable = false)
    private long contentLength;

    /**
     * Checksum value.
     */
    @Column(nullable = false)
    private long checksum;

    /**
     * Blob type to use streaming between the data tier and presentation tier.
     */
    @Column(name = "content")
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @JsonIgnore
    private Blob content;

    /**
     * Optional attachment or image description.
     */
    @Column(length = 256)
    private String description;

    /**
     * Date of creation.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdTime = new Date();

    protected Attachment() {
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Image name which usually is the filename.
     *
     * @return the image name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long size) {
        this.contentLength = size;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Blob getContent() {
        return content;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Attachment that = (Attachment) o;

        if (checksum != that.checksum) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (checksum ^ (checksum >>> 32));
        return result;
    }
}
