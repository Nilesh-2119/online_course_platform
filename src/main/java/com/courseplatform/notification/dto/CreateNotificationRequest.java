package com.courseplatform.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateNotificationRequest {

    @NotBlank(message = "Notification title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Notification message is required")
    private String message;

    @Size(max = 50, message = "Tag must not exceed 50 characters")
    private String tag;

    @Size(max = 500, message = "Link URL must not exceed 500 characters")
    private String linkUrl;

    private Boolean active = true;

    private Boolean isNew = true;

    public CreateNotificationRequest() {
    }

    public CreateNotificationRequest(String title, String message, String tag, String linkUrl, Boolean active, Boolean isNew) {
        this.title = title;
        this.message = message;
        this.tag = tag;
        this.linkUrl = linkUrl;
        this.active = active != null ? active : true;
        this.isNew = isNew != null ? isNew : true;
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
        return active != null ? active : true;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsNew() {
        return isNew != null ? isNew : true;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }
}
