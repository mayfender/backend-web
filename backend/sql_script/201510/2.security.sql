DROP TABLE roles;
CREATE TABLE roles ( id int NOT NULL AUTO_INCREMENT, username varchar(45), authority varchar(45), name varchar(45), PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into roles (id, username, authority, name) values (1, 'may', 'ROLE_ADMIN', 'Admin');
DROP TABLE users;
CREATE TABLE users ( id int NOT NULL AUTO_INCREMENT, username varchar(45) NOT NULL, password varchar(255) NOT NULL, enabled tinyint DEFAULT '1' NOT NULL, created_date_time datetime, updated_date_time datetime, PRIMARY KEY (id), CONSTRAINT ix1 UNIQUE (username) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into users (id, username, password, enabled, created_date_time, updated_date_time) values (1, 'may', '$2a$10$mNHt9RDl.3/ifSOCU44EWeP7lIeMqHy.mYObhX4DRBayPiNvpJU16', 1, null, null);