package com.solusi247.reportmanagement.model;

/**
 * Created by usernames on 22/06/16.
 */
public class ReportData {

    private int report_id;
    private int user_id;
    private String date;
    private String project;
    private String activity;
    private int status;
    private String desc;
    private String attachment;
    private String created_at;


    public ReportData() {
    }

    public ReportData(int report_id, int user_id, String date, String project, String activity, int status, String desc, String image, String created_at) {
        this.report_id = report_id;
        this.user_id = user_id;
        this.date = date;
        this.project = project;
        this.activity = activity;
        this.status = status;
        this.desc = desc;
        this.attachment = image;
        this.created_at = created_at;
    }

    public int getReport_id() {
        return report_id;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
