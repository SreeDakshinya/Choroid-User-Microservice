package com.ddbs.choroid_user_service.repository;

import com.ddbs.choroid_user_service.model.DbUserRowMapper;
import com.ddbs.choroid_user_service.model.SearchQueryUser;
import com.ddbs.choroid_user_service.model.StringListConverter;
import com.ddbs.choroid_user_service.model.User;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.apache.spark.sql.functions.*;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DbUserRowMapper dbUserRowMapper;
    private final StringListConverter stringListConverter = new StringListConverter();
    private SparkLogic sparkLogic;

    public UserRepository(JdbcTemplate jdbcTemplate, SparkLogic sparkLogic) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbUserRowMapper = new DbUserRowMapper();
        this.sparkLogic = sparkLogic;
    }

    public User findUserByUsername(String queryUsername) {
        Dataset<User> userData = sparkLogic.getCachedData();
        Dataset<User> filtered = userData.filter(userData.col("username").eqNullSafe(queryUsername));
        
        // Check if any results exist before calling head()
        if (filtered.count() == 0) {
            return null;
        }
        return filtered.head();
    }

    public User updateUserByUsername(String accessorUsername, User newUser) {
        Dataset<User> userData = sparkLogic.getCachedData();
        Field[] fields = newUser.getClass().getDeclaredFields();
        Column condition = col("username").equalTo(lit(accessorUsername));
        Map<String, Column> updationMap = new HashMap<>();

        for (Field field: fields) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("selfAccess"))
                continue;
            try {
                Column existing = col(field.getName());
                Object newValue = field.get(newUser);
                Column updated;
                if (newValue instanceof List) {
                    List<String> list = (List<String>) newValue;
                    Column[] litValues = new Column[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        litValues[i] = lit(list.get(i));
                    }
                    Column arrayCol = array(litValues);
                    updated = when(condition, arrayCol).otherwise(existing);
                }
                else
                    updated = when(condition, newValue).otherwise(existing);
                updationMap.put(field.getName(), updated);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        if (!updationMap.isEmpty())
            userData = userData.withColumns(updationMap).as(Encoders.bean(User.class));

        sparkLogic.saveToDatabase(userData);
        sparkLogic.refreshData();
        return findUserByUsername(accessorUsername);
    }

    public User createUser(User newUser) {
        Dataset<User> userData = sparkLogic.getCachedData();
        Dataset<User> newRow = sparkLogic.createDatasetFromUser(newUser);
        userData = userData.unionByName(newRow, true);
        sparkLogic.saveToDatabase(userData);
        sparkLogic.refreshData();
        return findUserByUsername(newUser.getUsername());
    }

    public List<User> listMatchingUsers(SearchQueryUser queryUser) {
        Dataset<User> userData = sparkLogic.getCachedData();
        if (queryUser.getNameSubstring()!=null)
            userData = userData.filter(new NameSubstringFilter(queryUser.getNameSubstring()));
        if (queryUser.getLearnList()!=null)
            userData = userData.filter(new LearnListFilter(queryUser.getLearnList()));
        if (queryUser.getTeachList()!=null)
            userData = userData.filter(new TeachListFilter(queryUser.getTeachList()));
        return userData.collectAsList();
    }

    public List<String> getTeachList() {
        Dataset<User> userData = sparkLogic.getCachedData();
        return userData.flatMap(new TeachListFlatMapper(), Encoders.STRING()).distinct().collectAsList();
    }

    public List<String> getLearnList() {
        Dataset<User> userData = sparkLogic.getCachedData();
        return userData.flatMap(new LearnListFlatMapper(), Encoders.STRING()).distinct().collectAsList();
    }

//    public User findUserByUsername(String queryUsername) {
//        User user;
//        try {
//            user =  jdbcTemplate.queryForObject("SELECT * FROM USERS WHERE USERNAME=?", dbUserRowMapper, queryUsername);
//
//        } catch (EmptyResultDataAccessException e) {
//            return null;
//        }
//        catch (DataAccessException e) {
//            throw new RuntimeException("Data access issue in findUserByUsername due to: " + e);
//        }
//        return user;
//    }
}

class NameSubstringFilter implements FilterFunction<User>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String nameSubstring;

    public NameSubstringFilter(String nameSubstring) {
        this.nameSubstring = nameSubstring.toLowerCase();
    }

    @Override
    public boolean call(User user) throws Exception {
        return user.getName().toLowerCase().contains(nameSubstring);
    }
}

class LearnListFilter implements FilterFunction<User>, Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> learnList;

    public LearnListFilter(List<String> learnList) {
        this.learnList = new ArrayList<>(learnList);
    }

    @Override
    public boolean call(User user) throws Exception {
//        return user.getLearnList().stream().anyMatch(learnList::contains);
        for (String item : user.getLearnList()) {
            if (learnList.contains(item)) {
                return true;
            }
        }
        return false;
    }
}

class TeachListFilter implements FilterFunction<User>, Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> teachList;

    public TeachListFilter(List<String> teachList) {
        this.teachList = new ArrayList<>(teachList);
    }

    @Override
    public boolean call(User user) throws Exception {
//        return user.getTeachList().stream().anyMatch(teachList::contains);
        for (String item : user.getTeachList()) {
            if (teachList.contains(item)) {
                return true;
            }
        }
        return false;
    }
}

class TeachListFlatMapper implements FlatMapFunction<User, String>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Iterator<String> call(User user) throws Exception {
        return user.getTeachList().iterator();
    }
}

class LearnListFlatMapper implements FlatMapFunction<User, String>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public Iterator<String> call(User user) throws Exception {
        return user.getLearnList().iterator();
    }
}
