package com.courseplatform.admin.dto;

import java.util.List;

public class AdminAnalyticsResponse {

    private List<MonthMetricDto> activeLearners;
    private List<FreeVsEnrolledDto> freeVsEnrolled;
    private List<MonthMetricDto> courseCompletion;
    private List<MonthMetricDto> resourceDownloads;
    private List<MonthMetricDto> couponEngagement;
    private List<MonthMetricDto> vslVideoViews;
    private List<MonthMetricDto> welcomeVideoViews;

    private long totalActiveLearners;
    private long totalFreeUsers;
    private long totalEnrolledStudents;
    private long totalCompletions;
    private long totalResourceDownloads;
    private long totalCouponUsages;
    private long totalVslViews;
    private long totalWelcomeVideoViews;

    public AdminAnalyticsResponse() {
    }

    public AdminAnalyticsResponse(List<MonthMetricDto> activeLearners,
                                  List<FreeVsEnrolledDto> freeVsEnrolled,
                                  List<MonthMetricDto> courseCompletion,
                                  List<MonthMetricDto> resourceDownloads,
                                  List<MonthMetricDto> couponEngagement,
                                  List<MonthMetricDto> vslVideoViews,
                                  List<MonthMetricDto> welcomeVideoViews,
                                  long totalActiveLearners,
                                  long totalFreeUsers,
                                  long totalEnrolledStudents,
                                  long totalCompletions,
                                  long totalResourceDownloads,
                                  long totalCouponUsages,
                                  long totalVslViews,
                                  long totalWelcomeVideoViews) {
        this.activeLearners = activeLearners;
        this.freeVsEnrolled = freeVsEnrolled;
        this.courseCompletion = courseCompletion;
        this.resourceDownloads = resourceDownloads;
        this.couponEngagement = couponEngagement;
        this.vslVideoViews = vslVideoViews;
        this.welcomeVideoViews = welcomeVideoViews;
        this.totalActiveLearners = totalActiveLearners;
        this.totalFreeUsers = totalFreeUsers;
        this.totalEnrolledStudents = totalEnrolledStudents;
        this.totalCompletions = totalCompletions;
        this.totalResourceDownloads = totalResourceDownloads;
        this.totalCouponUsages = totalCouponUsages;
        this.totalVslViews = totalVslViews;
        this.totalWelcomeVideoViews = totalWelcomeVideoViews;
    }

    public List<MonthMetricDto> getActiveLearners() {
        return activeLearners;
    }

    public void setActiveLearners(List<MonthMetricDto> activeLearners) {
        this.activeLearners = activeLearners;
    }

    public List<FreeVsEnrolledDto> getFreeVsEnrolled() {
        return freeVsEnrolled;
    }

    public void setFreeVsEnrolled(List<FreeVsEnrolledDto> freeVsEnrolled) {
        this.freeVsEnrolled = freeVsEnrolled;
    }

    public List<MonthMetricDto> getCourseCompletion() {
        return courseCompletion;
    }

    public void setCourseCompletion(List<MonthMetricDto> courseCompletion) {
        this.courseCompletion = courseCompletion;
    }

    public List<MonthMetricDto> getResourceDownloads() {
        return resourceDownloads;
    }

    public void setResourceDownloads(List<MonthMetricDto> resourceDownloads) {
        this.resourceDownloads = resourceDownloads;
    }

    public List<MonthMetricDto> getCouponEngagement() {
        return couponEngagement;
    }

    public void setCouponEngagement(List<MonthMetricDto> couponEngagement) {
        this.couponEngagement = couponEngagement;
    }

    public long getTotalActiveLearners() {
        return totalActiveLearners;
    }

    public void setTotalActiveLearners(long totalActiveLearners) {
        this.totalActiveLearners = totalActiveLearners;
    }

    public long getTotalFreeUsers() {
        return totalFreeUsers;
    }

    public void setTotalFreeUsers(long totalFreeUsers) {
        this.totalFreeUsers = totalFreeUsers;
    }

    public long getTotalEnrolledStudents() {
        return totalEnrolledStudents;
    }

    public void setTotalEnrolledStudents(long totalEnrolledStudents) {
        this.totalEnrolledStudents = totalEnrolledStudents;
    }

    public long getTotalCompletions() {
        return totalCompletions;
    }

    public void setTotalCompletions(long totalCompletions) {
        this.totalCompletions = totalCompletions;
    }

    public long getTotalResourceDownloads() {
        return totalResourceDownloads;
    }

    public void setTotalResourceDownloads(long totalResourceDownloads) {
        this.totalResourceDownloads = totalResourceDownloads;
    }

    public long getTotalCouponUsages() {
        return totalCouponUsages;
    }

    public void setTotalCouponUsages(long totalCouponUsages) {
        this.totalCouponUsages = totalCouponUsages;
    }

    public List<MonthMetricDto> getVslVideoViews() {
        return vslVideoViews;
    }

    public void setVslVideoViews(List<MonthMetricDto> vslVideoViews) {
        this.vslVideoViews = vslVideoViews;
    }

    public List<MonthMetricDto> getWelcomeVideoViews() {
        return welcomeVideoViews;
    }

    public void setWelcomeVideoViews(List<MonthMetricDto> welcomeVideoViews) {
        this.welcomeVideoViews = welcomeVideoViews;
    }

    public long getTotalVslViews() {
        return totalVslViews;
    }

    public void setTotalVslViews(long totalVslViews) {
        this.totalVslViews = totalVslViews;
    }

    public long getTotalWelcomeVideoViews() {
        return totalWelcomeVideoViews;
    }

    public void setTotalWelcomeVideoViews(long totalWelcomeVideoViews) {
        this.totalWelcomeVideoViews = totalWelcomeVideoViews;
    }

    public static class MonthMetricDto {
        private String month; // e.g. "2026-07"
        private String label; // e.g. "Jul 2026"
        private long value;

        public MonthMetricDto() {
        }

        public MonthMetricDto(String month, String label, long value) {
            this.month = month;
            this.label = label;
            this.value = value;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class FreeVsEnrolledDto {
        private String month;
        private String label;
        private long freeUsers;
        private long enrolledUsers;
        private long total;

        public FreeVsEnrolledDto() {
        }

        public FreeVsEnrolledDto(String month, String label, long freeUsers, long enrolledUsers, long total) {
            this.month = month;
            this.label = label;
            this.freeUsers = freeUsers;
            this.enrolledUsers = enrolledUsers;
            this.total = total;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getFreeUsers() {
            return freeUsers;
        }

        public void setFreeUsers(long freeUsers) {
            this.freeUsers = freeUsers;
        }

        public long getEnrolledUsers() {
            return enrolledUsers;
        }

        public void setEnrolledUsers(long enrolledUsers) {
            this.enrolledUsers = enrolledUsers;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }
}
