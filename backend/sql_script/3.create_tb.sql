CREATE TABLE image 
	( 
		id int NOT NULL AUTO_INCREMENT, 
		image_name varchar(100) NOT NULL, 
		image_content longblob NOT NULL, 
		image_type_id int NOT NULL, 
		created_date datetime, 
		updated_date datetime, 
		PRIMARY KEY (id)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
CREATE TABLE image_type 
	( 
		id int NOT NULL, 
		type_name varchar(10) NOT NULL, 
		PRIMARY KEY (id) 
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
CREATE TABLE menu 
	( 
		id int NOT NULL AUTO_INCREMENT, 
		name varchar(255), 
		image_id int, 
		status tinyint, 
		price int, 
		is_recommented TINYINT(1),
		menu_type_id INT NOT NULL,
		created_date DATETIME,
		updated_date DATETIME,		
		PRIMARY KEY (id)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
CREATE
    TABLE restaurant_db.menu_type
    (
        id INT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL
        PRIMARY KEY (id)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
    
CREATE 
	TABLE customer 
	( 
		id int NOT NULL AUTO_INCREMENT, 
		ref varchar(100) NOT NULL, 
		status tinyint NOT NULL, 
		created_date_time datetime, 
		updated_date_time datetime, 
		table_detail varchar(10) NOT NULL, 
		person_amount tinyint,
		total_price int,
		net_price int,
		discount int,
		cash_receive_amount int,
		change_cash int,
		PRIMARY KEY (id) 
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	
CREATE 
	TABLE order_menu 
	( 
		id int NOT NULL AUTO_INCREMENT, 
		menu_id int NOT NULL, 
		cus_id int NOT NULL, 
		amount tinyint NOT NULL,
		updated_date_time datetime, 
		status tinyint NOT NULL, 
		order_round_id INT NOT NULL,
		is_take_home tinyint(1) NOT NULL,
		is_cancel tinyint(1) NOT NULL,
		cancel_reason varchar(256),
		comment varchar(256),
		PRIMARY KEY (id) 
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	
CREATE 
	TABLE order_round 
	( 
		id int NOT NULL AUTO_INCREMENT, 
		created_date_time datetime NOT NULL, 
		PRIMARY KEY (id) 
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;	

    
    