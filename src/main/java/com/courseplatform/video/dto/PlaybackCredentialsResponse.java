package com.courseplatform.video.dto;

public class PlaybackCredentialsResponse {

    private Long videoId;
    private String otp;
    private String playbackInfo;

    public PlaybackCredentialsResponse() {
    }

    public PlaybackCredentialsResponse(Long videoId, String otp, String playbackInfo) {
        this.videoId = videoId;
        this.otp = otp;
        this.playbackInfo = playbackInfo;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPlaybackInfo() {
        return playbackInfo;
    }

    public void setPlaybackInfo(String playbackInfo) {
        this.playbackInfo = playbackInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long videoId;
        private String otp;
        private String playbackInfo;

        public Builder videoId(Long videoId) {
            this.videoId = videoId;
            return this;
        }

        public Builder otp(String otp) {
            this.otp = otp;
            return this;
        }

        public Builder playbackInfo(String playbackInfo) {
            this.playbackInfo = playbackInfo;
            return this;
        }

        public PlaybackCredentialsResponse build() {
            return new PlaybackCredentialsResponse(videoId, otp, playbackInfo);
        }
    }
}
