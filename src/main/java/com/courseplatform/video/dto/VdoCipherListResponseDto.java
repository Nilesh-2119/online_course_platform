package com.courseplatform.video.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VdoCipherListResponseDto {

    private Integer count;
    private List<VdoCipherVideoItemDto> rows;

    public VdoCipherListResponseDto() {
    }

    public VdoCipherListResponseDto(Integer count, List<VdoCipherVideoItemDto> rows) {
        this.count = count;
        this.rows = rows;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<VdoCipherVideoItemDto> getRows() {
        return rows != null ? rows : Collections.emptyList();
    }

    public void setRows(List<VdoCipherVideoItemDto> rows) {
        this.rows = rows;
    }
}
