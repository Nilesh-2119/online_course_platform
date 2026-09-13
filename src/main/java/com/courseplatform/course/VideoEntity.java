package com.courseplatform.course;

import com.courseplatform.common.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "videos")
public class VideoEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSectionEntity section;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "vdocipher_video_id", length = 100)
    private String vdocipherVideoId;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds = 0;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_free", nullable = false)
    private boolean isFree = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VideoStatus status = VideoStatus.DRAFT;

    public VideoEntity() {
    }

    public VideoEntity(Long id, CourseSectionEntity section, String title, String description, String vdocipherVideoId, Integer durationSeconds, Integer displayOrder, boolean isFree, VideoStatus status) {
        this.id = id;
        this.section = section;
        this.title = title;
        this.description = description;
        this.vdocipherVideoId = vdocipherVideoId;
        this.durationSeconds = durationSeconds != null ? durationSeconds : 0;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isFree = isFree;
        this.status = status != null ? status : VideoStatus.DRAFT;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseSectionEntity getSection() {
        return section;
    }

    public void setSection(CourseSectionEntity section) {
        this.section = section;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVdocipherVideoId() {
        return vdocipherVideoId;
    }

    public void setVdocipherVideoId(String vdocipherVideoId) {
        this.vdocipherVideoId = vdocipherVideoId;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private CourseSectionEntity section;
        private String title;
        private String description;
        private String vdocipherVideoId;
        private Integer durationSeconds = 0;
        private Integer displayOrder = 0;
        private boolean isFree = false;
        private VideoStatus status = VideoStatus.DRAFT;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder section(CourseSectionEntity section) {
            this.section = section;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder vdocipherVideoId(String vdocipherVideoId) {
            this.vdocipherVideoId = vdocipherVideoId;
            return this;
        }

        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Builder isFree(boolean isFree) {
            this.isFree = isFree;
            return this;
        }

        public Builder status(VideoStatus status) {
            this.status = status;
            return this;
        }

        public VideoEntity build() {
            return new VideoEntity(id, section, title, description, vdocipherVideoId, durationSeconds, displayOrder, isFree, status);
        }
    }
}
