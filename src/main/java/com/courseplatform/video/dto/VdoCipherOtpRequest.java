package com.courseplatform.video.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VdoCipherOtpRequest {

    private Long ttl;
    private String userId;
    private String whitelist;

    public VdoCipherOtpRequest() {
    }

    public VdoCipherOtpRequest(Long ttl, String userId, String whitelist) {
        this.ttl = ttl;
        this.userId = userId;
        this.whitelist = whitelist;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = whitelist;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long ttl;
        private String userId;
        private String whitelist;

        public Builder ttl(Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder whitelist(String whitelist) {
            this.whitelist = whitelist;
            return this;
        }

        public VdoCipherOtpRequest build() {
            return new VdoCipherOtpRequest(ttl, userId, whitelist);
        }
    }
}
