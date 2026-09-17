package com.courseplatform.video;

import com.courseplatform.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Tracks individual video watch/view events for analytics (Landing Page VSL & Student Dashboard Welcome Video).
 */
@Entity
@Table(
        name = "video_views",
        indexes = {
                @Index(name = "idx_video_views_type", columnList = "video_type"),
                @Index(name = "idx_video_views_viewed_at", columnList = "viewed_at"),
                @Index(name = "idx_video_views_user", columnList = "user_id"),
                @Index(name = "idx_video_views_type_viewed_at", columnList = "video_type, viewed_at")
        }
)
public class VideoViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_type", nullable = false, length = 50)
    private String videoType;

    @Column(name = "video_id", length = 100)
    private String videoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt = Instant.now();

    public VideoViewEntity() {
    }

    public VideoViewEntity(String videoType, String videoId, UserEntity user, String ipAddress, String userAgent) {
        this.videoType = videoType;
        this.videoId = videoId;
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.viewedAt = Instant.now();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Instant viewedAt) {
        this.viewedAt = viewedAt;
    }
}
