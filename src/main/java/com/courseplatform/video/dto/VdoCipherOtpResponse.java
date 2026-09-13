package com.courseplatform.video.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VdoCipherOtpResponse {

    private String otp;
    private String playbackInfo;

    public VdoCipherOtpResponse() {
    }

    public VdoCipherOtpResponse(String otp, String playbackInfo) {
        this.otp = otp;
        this.playbackInfo = playbackInfo;
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
        private String otp;
        private String playbackInfo;

        public Builder otp(String otp) {
            this.otp = otp;
            return this;
        }

        public Builder playbackInfo(String playbackInfo) {
            this.playbackInfo = playbackInfo;
            return this;
        }

        public VdoCipherOtpResponse build() {
            return new VdoCipherOtpResponse(otp, playbackInfo);
        }
    }
}
