CREATE TABLE Users (
    username VARCHAR(30) PRIMARY KEY,
    password CHAR(256)
);

CREATE TABLE Friends (
    sender VARCHAR REFERENCES Users(username),
    receiver VARCHAR REFERENCES Users(username),
    state VARCHAR(10),
    PRIMARY KEY (sender, receiver)
);
