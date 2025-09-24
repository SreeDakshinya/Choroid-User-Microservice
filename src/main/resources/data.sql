--CREATE TABLE USERS (
--    id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    Name VARCHAR(255),
--    Email VARCHAR(255),
--    Skills JSON,
--    Qualifications JSON,
--    ResumeLink VARCHAR(512),
--    TopicsToTeach JSON,
--    TopicsToLearn JSON
--);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(255),
    Email VARCHAR(255),
    Skills TEXT,
    Qualifications TEXT,
    ResumeLink VARCHAR(255),
    TopicsToTeach TEXT,
    TopicsToLearn TEXT
--    self_access BOOLEAN DEFAULT false
);

INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Alice', 'alice@example.com', '["Java","Spring"]', '["BSc"]', 'http://resume.com/alice', '["Math"]', '["Physics"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Bob', 'bob@example.com', '["Kotlin","SQL"]', '["MSc"]', 'http://resume.com/bob', '["Chemistry"]', '["Biology"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Alice', 'alice@example.com', '["Java","Spring"]', '["BSc"]', 'http://resume.com/alice', '["Math"]', '["Physics"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Charlie', 'charlie@example.com', '["Python"]', '["PhD"]', 'http://resume.com/charlie', '["English"]', '["History"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('David', 'david@example.com', '["C++"]', '["BTech"]', 'http://resume.com/david', '["Geography"]', '["Math"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Eve', 'eve@example.com', '["JavaScript"]', '["BA"]', 'http://resume.com/eve', '["Art"]', '["Music"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Frank', 'frank@example.com', '["Go"]', '["MBA"]', 'http://resume.com/frank', '["Economics"]', '["Finance"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Grace', 'grace@example.com', '["Ruby"]', '["BCom"]', 'http://resume.com/grace', '["Law"]', '["Political Science"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Heidi', 'heidi@example.com', '["Swift"]', '["MCA"]', 'http://resume.com/heidi', '["Computer Science"]', '["Data Science"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Ivan', 'ivan@example.com', '["PHP"]', '["BSc IT"]', 'http://resume.com/ivan', '["Networking"]', '["Security"]');
INSERT INTO USERS(Name, Email, Skills, Qualifications, ResumeLink, TopicsToTeach, TopicsToLearn)
VALUES
('Judy', 'judy@example.com', '["Scala"]', '["MTech"]', 'http://resume.com/judy', '["AI"]', '["ML"]');

