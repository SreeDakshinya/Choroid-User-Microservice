package com.ddbs.choroid_user_service.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
@Entity
public class UserRow implements Serializable {

    private static final long serialVersionUID = 1L;

//    @Column(name="Name")
    private String name;

    @Id
//    @Column(name="Username")
    private String username;

//    @Column(name="Email")
    private String emailId;

//    @Column(name="Skills", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class) //Converters work only for JPA
    private String skillTagList;

//    @Column(name="Qualifications", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class)
    private String qualificationList;

//    @Column(name="ResumeLink")
    private String resumeLink;

//    @Column(name="TopicsToTeach", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class)
    private String teachList;

//    @Column(name="TopicsToLearn", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class)
    private String learnList;

    private boolean selfAccess;

    public UserRow() {
    }

    public UserRow(String name, String username, String emailId, String skillTagList, String qualificationList, String resumeLink, String teachList, String learnList) {
        this.name = name;
        this.username = username;
        this.emailId = emailId;
        this.skillTagList = skillTagList;
        this.qualificationList = qualificationList;
        this.resumeLink = resumeLink;
        this.teachList = teachList;
        this.learnList = learnList;
    }

    @Override
    public String toString() {
        return "User{" +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", emailId='" + emailId + '\'' +
                ", skillTagList=" + skillTagList +
                ", qualificationList=" + qualificationList +
                ", resumeLink='" + resumeLink + '\'' +
                ", teachList=" + teachList +
                ", learnList=" + learnList +
                ", selfAccess=" + selfAccess +
                '}';
    }

    public static String convertJavaFieldToDbColumn(String fieldName) {
        switch (fieldName) {
            case "name" : return "Name";
            case "username" : return "Username";
            case "emailId" : return "Email";
            case "skillTagList" : return "Skills";
            case "qualificationList" : return "Qualifications";
            case "resumeLink" : return "ResumeLink";
            case "teachList" : return "TopicsToTeach";
            case "learnList" : return "TopicsToLearn";
            case "selfAccess" : return "SelfAccess";
            default : return fieldName;
        }
    }


}

