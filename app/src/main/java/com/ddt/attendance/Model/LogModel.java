package com.ddt.attendance.Model;

public class LogModel {

    String studentName;
    String studentEmail;
    String StudentDisplayName;
    String Date;
    String Id;

    public LogModel() {
    }

    public LogModel(String Id, String studentName, String studentEmail, String studentDisplayName, String date) {
        this.Id = Id;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        StudentDisplayName = studentDisplayName;
        Date = date;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentDisplayName() {
        return StudentDisplayName;
    }

    public void setStudentDisplayName(String studentDisplayName) {
        StudentDisplayName = studentDisplayName;
    }
}
