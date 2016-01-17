CREATE TABLE sub_menu ( 
	id int NOT NULL AUTO_INCREMENT, 
	name varchar(100) NOT NULL, 
	price double NOT NULL, 
	menu_id int NOT NULL, 
	amount_flag tinyint(1) NOT NULL, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE order_sub_menu ( 
	order_menu_id int NOT NULL, 
	sub_menu_id int NOT NULL,
	amount tinyint,
	is_cancel tinyint(1) DEFAULT false NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE
    restaurant_db.menu_type ADD (is_enabled TINYINT(1) NOT NULL);
    
ALTER TABLE
    restaurant_db.menu_type ADD (parent_id INT);
        