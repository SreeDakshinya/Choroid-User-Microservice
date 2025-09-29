package com.ddbs.choroid_user_service.repository;

import com.ddbs.choroid_user_service.model.DbUserRowMapper;
import com.ddbs.choroid_user_service.model.SearchQueryUser;
import com.ddbs.choroid_user_service.model.StringListConverter;
import com.ddbs.choroid_user_service.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ddbs.choroid_user_service.model.User.convertJavaFieldToDbColumn;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DbUserRowMapper dbUserRowMapper;
    private final StringListConverter stringListConverter = new StringListConverter();

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbUserRowMapper = new DbUserRowMapper();
    }

    public User findUserById(long queryId) {
        User user;
        try {
            user =  jdbcTemplate.queryForObject("SELECT * FROM USERS WHERE ID=?", dbUserRowMapper, queryId);
            if (user==null)
                throw new RuntimeException("User not found with ID: " + queryId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Data access issue in findUserById due to: " + e);
        }
        return user;
    }

    public User updateUserById(long accessorId, User newUser) {
        User updatedUser;
        try {
            Field[] fields = newUser.getClass().getDeclaredFields();
            List<Object> parameters = new ArrayList<>();
            StringBuilder sqlQuery = new StringBuilder("UPDATE USERS SET ");
            for (Field field: fields)
                try {
                    if (field.toString().endsWith("id") || field.toString().endsWith("selfAccess"))
                        continue;
                    field.setAccessible(true);
                    sqlQuery.append(convertJavaFieldToDbColumn(field.getName())).append(" = ?, ");
                    Object value = field.get(newUser);
                    if (value instanceof List)
                        parameters.add(this.stringListConverter.convertToDatabaseColumn((List<String>) value));
                    else
                        parameters.add(value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            sqlQuery.delete(sqlQuery.length()-2, sqlQuery.length());
            sqlQuery.append(" WHERE ID=?");
            parameters.add(accessorId);
            int numOfAffectedRows = jdbcTemplate.update(String.valueOf(sqlQuery), parameters.toArray());
            if (numOfAffectedRows==0)
                throw new RuntimeException("Could not update the user profile.");
            updatedUser = findUserById(accessorId);
            if (updatedUser==null)
                throw new RuntimeException("Updated user not found with ID: " + accessorId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Data access issue in findUserById due to: " + e);
        }
        return updatedUser;
    }

    public User createUser(User newUser) {
        User createdUser;
        try {
            Field[] fields = newUser.getClass().getDeclaredFields();
            List<Object> parameters = new ArrayList<>();
            StringBuilder sqlQuery = new StringBuilder("INSERT INTO USERS (");
            for (Field field: fields)
                try {
                    if (field.toString().endsWith("id") || field.toString().endsWith("selfAccess"))
                        continue;
                    field.setAccessible(true);
                    sqlQuery.append(convertJavaFieldToDbColumn(field.getName())).append(", ");
                    Object value = field.get(newUser);
                    if (value instanceof List)
                        parameters.add(this.stringListConverter.convertToDatabaseColumn((List<String>) value));
                    else
                        parameters.add(value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            sqlQuery.delete(sqlQuery.length()-2, sqlQuery.length());
            sqlQuery.append(") VALUES (");
            sqlQuery.append("?, ".repeat(parameters.size()));
            sqlQuery.delete(sqlQuery.length()-2, sqlQuery.length());
            sqlQuery.append(")");
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlQuery.toString(), Statement.RETURN_GENERATED_KEYS);
                Object[] params = parameters.toArray();
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps;
            }, keyHolder);
            long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            if (generatedId==0)
                throw new RuntimeException("Could not create the user profile.");
            else
                createdUser = findUserById(generatedId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Data access issue in findUserById due to: " + e);
        }
        return createdUser;
    }

    public List<User> listMatchingUsers(SearchQueryUser queryUser) {
        List<User> allUserList;
        List<User> filteredList;
        String sqlQuery = "SELECT * FROM USERS";
        allUserList = jdbcTemplate.query(sqlQuery, dbUserRowMapper);
        filteredList = allUserList.parallelStream().filter(user ->
                        (queryUser.getNameSubstring() == null || (user.getUserName() != null && user.getUserName().toLowerCase().contains(queryUser.getNameSubstring().toLowerCase())))
                        &&
                        (queryUser.getLearnList() == null || (user.getLearnList() != null && user.getLearnList().stream().anyMatch(queryUser.getLearnList()::contains)))
                        &&
                        (queryUser.getTeachList() == null || (user.getTeachList() != null && user.getTeachList().stream().anyMatch(queryUser.getTeachList()::contains)))
                ).toList();
        return filteredList;
    }

    public List<String> getTeachList() {
        String sqlQuery = "SELECT DISTINCT TopicsToTeach FROM USERS";
        return jdbcTemplate.queryForList(sqlQuery, String.class).parallelStream().flatMap(teachItem -> stringListConverter.convertToEntityAttribute(teachItem).stream()).distinct().toList();
    }

    public List<String> getLearnList() {
        String sqlQuery = "SELECT DISTINCT TopicsToLearn FROM USERS";
        return jdbcTemplate.queryForList(sqlQuery, String.class).parallelStream().flatMap(learnItem -> stringListConverter.convertToEntityAttribute(learnItem).stream()).distinct().toList();
    }
}
