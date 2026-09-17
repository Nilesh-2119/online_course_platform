package com.courseplatform.video.dto;

import jakarta.validation.constraints.NotBlank;

public class RecordVideoViewRequest {

    @NotBlank(message = "videoType is required (e.g., VSL or WELCOME)")
    private String videoType;

    private String videoId;

    public RecordVideoViewRequest() {
    }

    public RecordVideoViewRequest(String videoType, String videoId) {
        this.videoType = videoType;
        this.videoId = videoId;
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
}
