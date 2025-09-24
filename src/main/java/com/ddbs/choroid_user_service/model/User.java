package com.ddbs.choroid_user_service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name="Name")
    private String userName;

    @Column(name="Email")
    private String emailId;

    @Column(name="Skills", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class) //Converters work only for JPA
    private List<String> skillTagList;

    @Column(name="Qualifications", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class)
    private List<String> qualificationList;

    @Column(name="ResumeLink")
    private String resumeLink;

    @Column(name="TopicsToTeach", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class)
    private List<String> teachList;

    @Column(name="TopicsToLearn", columnDefinition = "json")
//    @Convert(converter = StringListConverter.class)
    private List<String> learnList;

    private boolean selfAccess;

    public User() {
    }

    public User(String userName, String emailId, List<String> skillTagList, List<String> qualificationList, String resumeLink, List<String> teachList, List<String> learnList) {
        this.userName = userName;
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
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", emailId='" + emailId + '\'' +
                ", skillTagList=" + skillTagList.toString() +
                ", qualificationList=" + qualificationList.toString() +
                ", resumeLink='" + resumeLink + '\'' +
                ", teachList=" + teachList.toString() +
                ", learnList=" + learnList.toString() +
                ", selfAccess=" + selfAccess +
                '}';
    }

    public static String convertJavaFieldToDbColumn(String fieldName) {
        return switch (fieldName) {
            case "userName" -> "Name";
            case "emailId" -> "Email";
            case "skillTagList" -> "Skills";
            case "qualificationList" -> "Qualifications";
            case "resumeLink" -> "ResumeLink";
            case "teachList" -> "TopicsToTeach";
            case "learnList" -> "TopicsToLearn";
            case "selfAccess" -> "SelfAccess";
            default -> fieldName;
        };
    }


}

