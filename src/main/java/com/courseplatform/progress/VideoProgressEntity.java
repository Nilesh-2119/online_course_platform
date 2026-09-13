package com.courseplatform.progress;

import com.courseplatform.common.BaseAuditableEntity;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "video_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_video_progress_user_video", columnNames = {"user_id", "video_id"})
        }
)
public class VideoProgressEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoEntity video;

    @Column(name = "last_position_seconds", nullable = false)
    private Integer lastPositionSeconds = 0;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    public VideoProgressEntity() {
    }

    public VideoProgressEntity(Long id, UserEntity user, VideoEntity video, Integer lastPositionSeconds, boolean completed) {
        this.id = id;
        this.user = user;
        this.video = video;
        this.lastPositionSeconds = lastPositionSeconds != null ? lastPositionSeconds : 0;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public VideoEntity getVideo() {
        return video;
    }

    public void setVideo(VideoEntity video) {
        this.video = video;
    }

    public Integer getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds) {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private UserEntity user;
        private VideoEntity video;
        private Integer lastPositionSeconds = 0;
        private boolean completed = false;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public Builder video(VideoEntity video) {
            this.video = video;
            return this;
        }

        public Builder lastPositionSeconds(Integer lastPositionSeconds) {
            this.lastPositionSeconds = lastPositionSeconds;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public VideoProgressEntity build() {
            return new VideoProgressEntity(id, user, video, lastPositionSeconds, completed);
        }
    }
}
