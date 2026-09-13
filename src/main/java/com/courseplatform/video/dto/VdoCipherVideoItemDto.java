package com.courseplatform.video.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VdoCipherVideoItemDto {

    private String id;
    private String title;
    private String description;
    private Integer length;
    private String status;
    private String poster;
    private List<String> tags;

    @JsonProperty("upload_time")
    private Long uploadTime;

    public VdoCipherVideoItemDto() {
    }

    public VdoCipherVideoItemDto(String id, String title, String description, Integer length, String status, String poster, List<String> tags, Long uploadTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.length = length;
        this.status = status;
        this.poster = poster;
        this.tags = tags;
        this.uploadTime = uploadTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }
}
