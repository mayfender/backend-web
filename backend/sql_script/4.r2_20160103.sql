CREATE TABLE sub_menu ( 
	id int NOT NULL AUTO_INCREMENT, 
	name varchar(100) NOT NULL, 
	price double NOT NULL, 
	menu_id int NOT NULL, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE order_submenu ( 
	order_menu_id int NOT NULL, 
	sub_menu_id int NOT NULL 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
