CREATE TABLE customer ( id int NOT NULL AUTO_INCREMENT, ref varchar(100) NOT NULL, status tinyint NOT NULL, created_date_time datetime, updated_date_time datetime, table_detail varchar(10) NOT NULL, total_price double, net_price double, discount double, cash_receive_amount double, change_cash double, person_amount tinyint, PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE image ( id int NOT NULL AUTO_INCREMENT, image_name varchar(100) NOT NULL, image_content longblob NOT NULL, image_type_id int NOT NULL, created_date datetime, updated_date datetime, PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE image_type ( id int NOT NULL, type_name varchar(10) NOT NULL, PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into image_type(id, type_name) value(1, 'JPG');
insert into image_type(id, type_name) value(2, 'PNG');
insert into image_type(id, type_name) value(3, 'GIF');

CREATE TABLE menu ( id int NOT NULL AUTO_INCREMENT, name varchar(255) NOT NULL, image_id int, status tinyint NOT NULL, price double NOT NULL, menu_type_id int NOT NULL, created_date datetime, updated_date datetime, is_recommented tinyint(1) NOT NULL, menu_detail_html text, PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE menu_type ( id int NOT NULL AUTO_INCREMENT, name varchar(100) NOT NULL, PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE order_menu ( id int NOT NULL AUTO_INCREMENT, menu_id int NOT NULL, cus_id int NOT NULL, created_date_time datetime(3), updated_date_time datetime, doing_changed_status_date_time datetime, finished_changed_status_date_time datetime, status tinyint NOT NULL, amount tinyint NOT NULL, comment varchar(256), order_round tinyint, is_take_home tinyint(1) NOT NULL, is_cancel tinyint(1) NOT NULL, cancel_reason varchar(256), PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
