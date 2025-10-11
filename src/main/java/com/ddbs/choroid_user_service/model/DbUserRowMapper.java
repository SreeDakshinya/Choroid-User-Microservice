package com.ddbs.choroid_user_service.model;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DbUserRowMapper implements RowMapper<User> {

    private final StringListConverter stringListConverter = new StringListConverter();

    public DbUserRowMapper() {
    }

    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        String name = resultSet.getString("Name");

        String username = resultSet.getString("Username");

        String emailId = resultSet.getString("Email");

        String skillTagString = resultSet.getString("Skills");
        List<String> skillTagList = stringListConverter.convertToEntityAttribute(skillTagString);

        String qualificationString = resultSet.getString("Qualifications");
        List<String> qualificationList = stringListConverter.convertToEntityAttribute(qualificationString);

        String resumeLink = resultSet.getString("ResumeLink");

        String teachString = resultSet.getString("TopicsToTeach");
        List<String> teachList = stringListConverter.convertToEntityAttribute(teachString);

        String learnString = resultSet.getString("TopicsToLearn");
        List<String> learnList = stringListConverter.convertToEntityAttribute(learnString);

        User user = new User(name, username, emailId, skillTagList, qualificationList, resumeLink, teachList, learnList);

        user.setSelfAccess(false);

        return user;
    }
}
