CREATE DATABASE estoque_db;
use estoque_db;

create table users (
	id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    psw VARCHAR(100) NOT NULL
);

INSERT INTO users (username, psw)
VALUES('admin','1234');