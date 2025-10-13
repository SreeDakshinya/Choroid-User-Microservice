CREATE TABLE IF NOT EXISTS USERS (
    Name VARCHAR(255),
    Username VARCHAR(255) PRIMARY KEY,
    Email VARCHAR(255),
    Skills TEXT,
    Qualifications TEXT,
    ResumeLink VARCHAR(255),
    TopicsToTeach TEXT,
    TopicsToLearn TEXT
);