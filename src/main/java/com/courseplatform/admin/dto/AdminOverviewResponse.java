package com.courseplatform.admin.dto;

import java.util.List;

public class AdminOverviewResponse {

    private long totalUsers;
    private long freeUsers;
    private long enrolledStudents;
    private long resourcesDownloaded;
    private List<RecentStudentDto> recentEnrolledStudents;

    public AdminOverviewResponse() {
    }

    public AdminOverviewResponse(long totalUsers, long freeUsers, long enrolledStudents, long resourcesDownloaded, List<RecentStudentDto> recentEnrolledStudents) {
        this.totalUsers = totalUsers;
        this.freeUsers = freeUsers;
        this.enrolledStudents = enrolledStudents;
        this.resourcesDownloaded = resourcesDownloaded;
        this.recentEnrolledStudents = recentEnrolledStudents;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getFreeUsers() {
        return freeUsers;
    }

    public void setFreeUsers(long freeUsers) {
        this.freeUsers = freeUsers;
    }

    public long getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(long enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public long getResourcesDownloaded() {
        return resourcesDownloaded;
    }

    public void setResourcesDownloaded(long resourcesDownloaded) {
        this.resourcesDownloaded = resourcesDownloaded;
    }

    public List<RecentStudentDto> getRecentEnrolledStudents() {
        return recentEnrolledStudents;
    }

    public void setRecentEnrolledStudents(List<RecentStudentDto> recentEnrolledStudents) {
        this.recentEnrolledStudents = recentEnrolledStudents;
    }

    public static class RecentStudentDto {
        private String id;
        private String name;
        private String email;
        private String enrolledAt;
        private String courseTitle;

        public RecentStudentDto() {
        }

        public RecentStudentDto(String id, String name, String email, String enrolledAt, String courseTitle) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.enrolledAt = enrolledAt;
            this.courseTitle = courseTitle;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getEnrolledAt() {
            return enrolledAt;
        }

        public void setEnrolledAt(String enrolledAt) {
            this.enrolledAt = enrolledAt;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }
    }
}
