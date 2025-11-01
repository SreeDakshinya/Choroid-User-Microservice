package com.ddbs.choroid_user_service.repository;

import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.model.UserRow;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.storage.StorageLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.apache.spark.sql.functions.lit;

@Service
@EnableAsync
public class SparkLogic {

    public SparkSession sparkSession;
    private Dataset<User> userDataset;
    private volatile boolean dataReady = false;
    private volatile boolean sparkInitialized = false;

    @Value("${spring.datasource.url}")
    String jdbcUrl;

    @Value("${spring.datasource.username}")
    String dbUsername;

    @Value("${spring.datasource.password}")
    String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    String datasourceDriver;

    @Value("${spark-port}")
    String sparkPort;

    @Value("${spark.master.url}")
    String sparkMasterUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void initSpark() {
            System.out.println("Initializing Spark Session...");
            System.out.println("Java Version: " + System.getProperty("java.version"));
            
            // Minimal Windows compatibility for the driver (not needed by Docker containers)
            System.setProperty("HADOOP_USER_NAME", "spark");
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                System.setProperty("hadoop.home.dir", System.getProperty("java.io.tmpdir"));
            }

            sparkSession = SparkSession.builder()
                    .appName("User Microservice")
                    .master("local[*]")
//                    .master("spark://localhost:7077")  // Connect to cluster
                    .config("spark.driver.host", "host.docker.internal")  // Use Docker's standard host resolution
                    .config("spark.driver.port", "0")  // Let Spark choose port
                    .config("spark.driver.bindAddress", "0.0.0.0")
                    .config("spark.driver.blockManager.port", "0")  // Let Spark choose block manager port
                    .config("spark.port.maxRetries", "100")  // Try many ports to avoid conflicts
                    .config("spark.driver.maxResultSize", "1g")  // Limit result size for small datasets
                    .config("spark.network.timeout", "30s")  // Network timeout
                    .config("spark.storage.blockManagerHeartbeatTimeoutMs", "60000")  // 60s - must be >= network timeout
                    .config("spark.executor.heartbeatInterval", "10s")
                    .config("spark.scheduler.maxRegisteredResourcesWaitingTime", "30s")  // Wait longer for workers to register
                    .config("spark.scheduler.minRegisteredResourcesRatio", "0.3")  // Wait for at least 30% of resources
                    .config("spark.dynamicAllocation.enabled", "false")  // Disable dynamic allocation
                    .config("spark.executor.memory", "1g")
                    .config("spark.executor.cores", "1")
                    .config("spark.executor.instances", "3")  // Request 3 executors explicitly
                    .config("spark.ui.enabled", "true")
                    .config("spark.ui.port", sparkPort)
                    
                    // Use Kryo serializer for ALL serialization operations
                    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                    .config("spark.kryo.registrationRequired", "false")
                    .config("spark.kryo.unsafe", "true")
                    .config("spark.kryo.referenceTracking", "false")
                    .config("spark.kryoserializer.buffer.max", "1024m")
                    .config("spark.kryoserializer.buffer", "64m")
                    
                    // Force Kryo for closure serialization too
                    .config("spark.closure.serializer", "org.apache.spark.serializer.KryoSerializer")
                    
                    // Disable problematic optimizations that can cause serialization issues
                    .config("spark.sql.codegen.wholeStage", "false")
                    .config("spark.sql.codegen.factoryMode", "NO_CODEGEN")
                    .config("spark.sql.adaptive.enabled", "false")
                    .config("spark.sql.adaptive.coalescePartitions.enabled", "false")
                    .config("spark.sql.execution.arrow.pyspark.enabled", "false")
                    
                    // Additional Java 11 compatibility settings
                    .config("spark.driver.memory", "2g")
                    
                    // Force executor allocation and connection
                    .config("spark.cores.max", "6")  // Use all available cores from workers
                    .config("spark.executor.heartbeatInterval", "5s")
                    .config("spark.network.timeout", "120s")
                    .config("spark.rpc.askTimeout", "120s")
                    .config("spark.rpc.lookupTimeout", "120s")
                    
                    // Add H2 driver JAR to Spark - use local path for driver, Docker path for executors
                    .config("spark.jars", "./libs/h2.jar")
                    .config("spark.executor.extraClassPath", "/opt/libs/h2.jar")  // Ensure executors can find H2 driver
                    
                    // Java 11 compatibility - removed invalid security manager flags
                    .config("spark.driver.extraJavaOptions", 
                            "--add-opens=java.base/java.lang=ALL-UNNAMED " +
                            "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED " +
                            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED " +
                            "--add-opens=java.base/java.io=ALL-UNNAMED " +
                            "--add-opens=java.base/java.nio=ALL-UNNAMED " +
                            "--add-opens=java.base/java.util=ALL-UNNAMED " +
                            "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " +
                            "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED " +
                            "--add-opens=java.base/sun.security.util=ALL-UNNAMED")
                    .config("spark.executor.extraJavaOptions", 
                            "--add-opens=java.base/java.lang=ALL-UNNAMED " +
                            "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED " +
                            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED " +
                            "--add-opens=java.base/java.io=ALL-UNNAMED " +
                            "--add-opens=java.base/java.nio=ALL-UNNAMED " +
                            "--add-opens=java.base/java.util=ALL-UNNAMED " +
                            "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " +
                            "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED " +
                            "--add-opens=java.base/sun.security.util=ALL-UNNAMED")
                    
                    // Disable problematic features for Windows/Docker setup
                    .config("spark.sql.adaptive.enabled", "false")
                    .config("spark.sql.adaptive.coalescePartitions.enabled", "false")
                    .getOrCreate();

            System.out.println("✅ Spark Session connected to cluster");
            sparkInitialized = true;

            // Load data asynchronously - don't block application startup
            loadDataAsync();

//        System.out.println("🚀 Initializing Spark Session...");
//        System.out.println("Java Version: " + System.getProperty("java.version"));
//
//        try {
//            // *** ADD A DELAY HERE (e.g., 10 seconds) ***
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

//        // CRITICAL: Set these BEFORE creating SparkSession
//        String hadoopHome = System.getProperty("user.home") + "\\hadoop";
//        System.setProperty("hadoop.home.dir", hadoopHome);
//        System.setProperty("HADOOP_USER_NAME", System.getProperty("user.name"));

//        System.setProperty("HADOOP_USER_NAME", "root"); // or a static user
//        System.setProperty("hadoop.home.dir", "/opt/hadoop"); // not needed but safe
//
//        // Disable Hadoop's UserGroupInformation security features
//        System.setProperty("HADOOP_JAAS_DEBUG", "false");
//        System.setProperty("hadoop.security.authentication", "simple");
//        System.setProperty("hadoop.security.authorization", "false");
//
//        // Force Hadoop to not use Subject
//        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();
//        hadoopConf.set("hadoop.security.authentication", "simple");
//        hadoopConf.set("hadoop.security.authorization", "false");

//        System.setProperty("HADOOP_USER_NAME", "root");  // or any non-empty username
//        System.setProperty("hadoop.security.authentication", "simple");
//        System.setProperty("hadoop.security.authorization", "false");
//
//        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();
//        hadoopConf.set("hadoop.security.authentication", "simple");
//        hadoopConf.set("hadoop.security.authorization", "false");

//        System.setProperty("spark.hadoop.hadoop.security.authentication", "simple");
//        System.setProperty("spark.hadoop.hadoop.security.authorization", "false");
//        System.setProperty("spark.testing", "true");  // disables Hadoop UGI security paths
//        System.setProperty("HADOOP_USER_NAME", "root");

//        try {
//            org.apache.hadoop.security.UserGroupInformation.setConfiguration(hadoopConf);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        System.setProperty("HADOOP_USER_NAME", "root");
//        System.setProperty("spark.hadoop.hadoop.security.authentication", "simple");
//        System.setProperty("spark.hadoop.hadoop.security.authorization", "false");
//
//// the key flag that disables Hadoop’s Subject lookup
//        System.setProperty("spark.testing", "true");
//
//// this prevents Spark from trying to read JAAS subjects at all
//        System.setProperty("java.security.manager", "allow");


//        sparkSession = SparkSession.builder()
//                .appName("User Microservice")
////                .remote("sc://spark-master:15002")
////                .master(sparkMasterUrl)
////                .config("spark.ui.port", sparkPort)
////                .config("spark.ui.enabled", "false") //
////                .config("spark.driver.host", "host.docker.internal")
////                .config("spark.driver.bindAddress", "0.0.0.0")
////                .config("spark.executor.memory", "2g")
////                .config("spark.executor.cores", "2")
////                .config("spark.hadoop.fs.defaultFS", "file:///") // << IMPORTANT
////                .config("spark.sql.warehouse.dir", System.getProperty("java.io.tmpdir") + "/spark-warehouse")
//                .config("spark.connect.uri", "sc://host.docker.internal:15002")
//                .getOrCreate();
//        refreshData();

//        sparkSession = SparkSession.builder()
//                .appName("User Microservice")
//                .master("spark://localhost:7077")  // ← Use traditional master URL
//                .config("spark.driver.host", "host.docker.internal")  // ← So workers can reach your app
//                .config("spark.driver.port", "0")  // ← Auto-assign driver port
//                .config("spark.driver.bindAddress", "0.0.0.0")
//                .config("spark.executor.memory", "2g")
//                .config("spark.executor.cores", "2")
//                .config("spark.ui.enabled", "false")
//                .getOrCreate();
//
//        refreshData();
    }

    @Async
    public CompletableFuture<Void> loadDataAsync() {
        try {
            System.out.println("🚀 Loading data from database asynchronously...");
            refreshData();
            dataReady = true;
            System.out.println("✅ Data loaded successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

//    private Dataset<User> loadFromDatabase() {
//
//        Encoder<UserRow> userRowEncoder = Encoders.bean(UserRow.class);
//        Encoder<User> userEncoder = Encoders.bean(User.class);
//
//        Dataset<Row> tempData = sparkSession.read().format("jdbc")
//                .option("url", jdbcUrl)
//                .option("dbtable", "USERS")
//                .option("user", dbUsername)
//                .option("password", dbPassword)
//                .option("driver", datasourceDriver)
//                .load();
//
//        for (String key: dbToUserMap.keySet())
//            tempData = tempData.withColumnRenamed(key, dbToUserMap.get(key));
//
//        return tempData.withColumn("selfAccess", lit(false)).as(userRowEncoder).map(new UserRowToUserMapper(), userEncoder);
//    }

    private Dataset<User> loadFromDatabase() {
        Encoder<UserRow> userRowEncoder = Encoders.bean(UserRow.class);
        Encoder<User> userEncoder = Encoders.bean(User.class);

        // Use the same TCP H2 database for both driver and executors
//        String executorJdbcUrl = jdbcUrl.replace("localhost", "h2-database");
        String executorJdbcUrl = jdbcUrl;

        Dataset<Row> tempData = sparkSession.read().format("jdbc")
                .option("url", executorJdbcUrl)
                .option("dbtable", "USERS")
                .option("user", dbUsername)
                .option("password", dbPassword)
                .option("driver", datasourceDriver)
                .load();

        // Rename columns
        tempData = tempData.withColumnRenamed("Name", "name");
        tempData = tempData.withColumnRenamed("Username", "username");
        tempData = tempData.withColumnRenamed("Email", "emailId");
        tempData = tempData.withColumnRenamed("Skills", "skillTagList");
        tempData = tempData.withColumnRenamed("Qualifications", "qualificationList");
        tempData = tempData.withColumnRenamed("ResumeLink", "resumeLink");
        tempData = tempData.withColumnRenamed("TopicsToTeach", "teachList");
        tempData = tempData.withColumnRenamed("TopicsToLearn", "learnList");

        // Add selfAccess column
        tempData = tempData.withColumn("selfAccess", lit(false));

        // Original MapFunction approach (commented out due to serialization issues in cluster mode)
        /*
        // Convert to UserRow first
        Dataset<UserRow> userRowDataset = tempData.as(userRowEncoder);

        // Now map to User
        return userRowDataset.map(new UserRowToUserMapper(), userEncoder);
        */
        
        // Alternative approach: avoid distributed MapFunction serialization
        // Convert UserRow to User on driver to avoid serialization issues
        Dataset<UserRow> userRowDataset = tempData.as(userRowEncoder);
        
        // Collect to driver and convert there (avoids executor serialization)
        java.util.List<UserRow> userRows = userRowDataset.collectAsList();
        
        // Convert on the driver side using our helper method
        java.util.List<User> users = new java.util.ArrayList<>();
        for (UserRow row : userRows) {
            users.add(convertUserRowToUser(row));
        }
        
        // Create new Dataset from the converted data
        return sparkSession.createDataset(users, userEncoder);
    }
    
    // Helper method for converting UserRow to User on the driver side
    // Reuses the same logic from UserRowToUserMapper but runs on driver to avoid serialization
    private User convertUserRowToUser(UserRow row) {
        // Create a temporary mapper instance to use its parseStringList method
        UserRowToUserMapper mapper = new UserRowToUserMapper();
        try {
            return mapper.call(row);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert UserRow to User", e);
        }
    }
    
    // Helper method for converting User to UserRow on the driver side
    // Reuses the same logic from UserToUserRowMapper but runs on driver to avoid serialization
    private UserRow convertUserToUserRow(User user) {
        // Create a temporary mapper instance to use its listToJsonString method
        UserToUserRowMapper mapper = new UserToUserRowMapper();
        try {
            return mapper.call(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert User to UserRow", e);
        }
    }

    public synchronized void refreshData() {
        if (userDataset != null)
            userDataset.unpersist();

        userDataset = loadFromDatabase().persist(StorageLevel.MEMORY_AND_DISK());

        userDataset.count(); //just as a trigger
//        return userDataset;
    }

    public Dataset<User> getCachedData() {
        // Wait for Spark to be initialized
        waitForSparkInitialization();
        
        if (userDataset == null)
            refreshData();
        return userDataset;
    }
    
    public Dataset<User> createDatasetFromUser(User user) {
        waitForSparkInitialization();
        return sparkSession.createDataset(Collections.singletonList(user), Encoders.bean(User.class));
    }
    
    private void waitForSparkInitialization() {
        int maxWaitSeconds = 60;
        int waitedSeconds = 0;
        while (!sparkInitialized && waitedSeconds < maxWaitSeconds) {
            try {
                System.out.println("Waiting for Spark initialization...");
                Thread.sleep(1000);
                waitedSeconds++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Spark initialization", e);
            }
        }
        
        if (!sparkInitialized) {
            throw new RuntimeException("Spark failed to initialize within " + maxWaitSeconds + " seconds");
        }
    }

    public void saveToDatabase(Dataset<User> data) {

        Encoder<UserRow> userRowEncoder = Encoders.bean(UserRow.class);

        // Apply the same fix as loadFromDatabase: collect to driver first to avoid serialization issues
        java.util.List<User> users = data.collectAsList();
        
        // Convert on the driver side using our helper method
        java.util.List<UserRow> userRows = new java.util.ArrayList<>();
        for (User user : users) {
            userRows.add(convertUserToUserRow(user));
        }
        
        // Create new Dataset from the converted data
        Dataset<UserRow> dbData = sparkSession.createDataset(userRows, userRowEncoder);

        dbData.write().format("jdbc").option("url", jdbcUrl)
                .option("dbtable", "USERS")
                .option("user", dbUsername)
                .option("password", dbPassword)
                .option("driver", datasourceDriver).mode("Overwrite").save();
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (userDataset != null)
            userDataset.unpersist();
        if (sparkSession != null)
            sparkSession.close();
    }

}

// Original MapFunction with potential serialization issues
/*
class UserRowToUserMapper implements MapFunction<UserRow, User>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public User call(UserRow row) throws Exception {
        StringListConverter stringListConverter = new StringListConverter();
        return new User(row.getName(), row.getUsername(), row.getEmailId(), stringListConverter.convertToEntityAttribute(row.getSkillTagList()), stringListConverter.convertToEntityAttribute(row.getQualificationList()), row.getResumeLink(), stringListConverter.convertToEntityAttribute(row.getTeachList()), stringListConverter.convertToEntityAttribute(row.getLearnList()));
    }
}
*/

// New serialization-safe MapFunction that avoids Jackson dependencies
class UserRowToUserMapper implements MapFunction<UserRow, User>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public User call(UserRow row) throws Exception {
        // Use simple string parsing instead of Jackson to avoid serialization issues
        return new User(
            row.getName(), 
            row.getUsername(), 
            row.getEmailId(), 
            parseStringList(row.getSkillTagList()), 
            parseStringList(row.getQualificationList()), 
            row.getResumeLink(), 
            parseStringList(row.getTeachList()), 
            parseStringList(row.getLearnList())
        );
    }
    
    // Static method to parse JSON string list without Jackson dependencies
    private static java.util.List<String> parseStringList(String jsonString) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return result;
        }
        
        String cleaned = jsonString.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        if (cleaned.trim().isEmpty()) {
            return result;
        }
        
        String[] parts = cleaned.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        
        return result;
    }
}

// Original UserToUserRowMapper with potential serialization issues
/*
class UserToUserRowMapper implements MapFunction<User, UserRow>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public UserRow call(User row) throws Exception {
        StringListConverter stringListConverter = new StringListConverter();
        return new UserRow(row.getName(), row.getUsername(), row.getEmailId(), stringListConverter.convertToDatabaseColumn(row.getSkillTagList()), stringListConverter.convertToDatabaseColumn(row.getQualificationList()), row.getResumeLink(), stringListConverter.convertToDatabaseColumn(row.getTeachList()), stringListConverter.convertToDatabaseColumn(row.getLearnList()));
    }
}
*/

// New serialization-safe UserToUserRowMapper
class UserToUserRowMapper implements MapFunction<User, UserRow>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public UserRow call(User user) throws Exception {
        // Use simple JSON string creation instead of Jackson to avoid serialization issues
        return new UserRow(
            user.getName(), 
            user.getUsername(), 
            user.getEmailId(), 
            listToJsonString(user.getSkillTagList()), 
            listToJsonString(user.getQualificationList()), 
            user.getResumeLink(), 
            listToJsonString(user.getTeachList()), 
            listToJsonString(user.getLearnList())
        );
    }
    
    // Static method to convert List<String> to JSON string without Jackson dependencies
    private static String listToJsonString(java.util.List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < stringList.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(stringList.get(i)).append("\"");
        }
        sb.append("]");
        
        return sb.toString();
    }
}
