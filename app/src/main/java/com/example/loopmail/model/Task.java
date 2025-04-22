package com.example.loopmail.model;

import java.io.Serializable;
import java.util.List;

public class Task  implements Serializable {
    private String taskId;
    private String email;
    private String emailPass;
    private List<String> recipients;
    private String mailBody;
    private String mailSubject;
    private String userId;
    private String createdAt;
    private String senderName;
    private String taskName;

    public Task(String taskId, String email, String emailPass, List<String> recipients, String mailBody, String mailSubject, String userId, String createdAt, String senderName, String taskName) {
        this.taskId = taskId;
        this.email = email;
        this.emailPass = emailPass;
        this.recipients = recipients;
        this.mailBody = mailBody;
        this.mailSubject = mailSubject;
        this.userId = userId;
        this.createdAt = createdAt;
        this.senderName = senderName;
        this.taskName = taskName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailPass() {
        return emailPass;
    }

    public void setEmailPass(String emailPass) {
        this.emailPass = emailPass;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
