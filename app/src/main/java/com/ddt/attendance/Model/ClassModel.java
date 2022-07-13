package com.ddt.attendance.Model;

public class ClassModel {
    String Id;
    String SubjectName;
    String Department;
    int LastIndexOfStudent;
    int FirstIndexOfStudent;
    String Standard;
    String Division;
    String SubjectType;

    public ClassModel() {
    }

    public ClassModel(String id, int firstIndexOfStudent, int lastIndexOfStudent, String subjectName, String department, String standard, String division, String subjectType) {
        FirstIndexOfStudent = firstIndexOfStudent;
        LastIndexOfStudent = lastIndexOfStudent;
        Id = id;
        SubjectName = subjectName;
        Department = department;
        Standard = standard;
        Division = division;
        SubjectType = subjectType;
    }

    public int getFirstIndexOfStudent() {
        return FirstIndexOfStudent;
    }

    public void setFirstIndexOfStudent(int firstIndexOfStudent) {
        FirstIndexOfStudent = firstIndexOfStudent;
    }

    public int getLastIndexOfStudent() {
        return LastIndexOfStudent;
    }

    public void setLastIndexOfStudent(int lastIndexOfStudent) {
        LastIndexOfStudent = lastIndexOfStudent;
    }

    public String getDivision() {
        return Division;
    }

    public void setDivision(String division) {
        Division = division;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getSubjectName() {
        return SubjectName;
    }

    public void setSubjectName(String subjectName) {
        SubjectName = subjectName;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
    }

    public String getStandard() {
        return Standard;
    }

    public void setStandard(String standard) {
        Standard = standard;
    }

    public String getSubjectType() {
        return SubjectType;
    }

    public void setSubjectType(String subjectType) {
        SubjectType = subjectType;
    }
}
