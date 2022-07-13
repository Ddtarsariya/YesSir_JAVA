package com.ddt.attendance.Model;

public class StudentModel {
    int Id;
    int LastIndexOfStudent;
    String StudentName;
    String UniqueId;
    String Number;
    String Notes;
    private boolean expanded;

    public StudentModel() {
    }

    public StudentModel(int id, String uniqueId, String studentName,String number,String notes) {
        UniqueId = uniqueId;
        Id = id;
        Number = number;
        Notes = notes;
        StudentName = studentName;
        this.expanded = false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getNumber() {
        if (Number == null) {
            return "";
        }
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getNotes() {
        if (Notes == null) {
            return "";
        }
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getUniqueId() {
        return UniqueId;
    }

    public void setUniqueId(String uniqueId) {
        UniqueId = uniqueId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getStudentName() {
        return StudentName;
    }

    public void setStudentName(String studentName) {
        StudentName = studentName;
    }
}
