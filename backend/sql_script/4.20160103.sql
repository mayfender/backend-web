CREATE TABLE sub_menu ( 
	id int NOT NULL AUTO_INCREMENT, 
	name varchar(100) NOT NULL, 
	price double NOT NULL, 
	menu_id int NOT NULL, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;