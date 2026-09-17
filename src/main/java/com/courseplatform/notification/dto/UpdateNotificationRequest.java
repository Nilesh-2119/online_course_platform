package com.courseplatform.notification.dto;

import jakarta.validation.constraints.Size;

public class UpdateNotificationRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String message;

    @Size(max = 50, message = "Tag must not exceed 50 characters")
    private String tag;

    @Size(max = 500, message = "Link URL must not exceed 500 characters")
    private String linkUrl;

    private Boolean active;

    private Boolean isNew;

    public UpdateNotificationRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }
}
