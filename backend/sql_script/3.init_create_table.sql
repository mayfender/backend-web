CREATE TABLE service_data ( 
	id int NOT NULL AUTO_INCREMENT, 
	created_date_time datetime, 
	updated_date_time datetime, 
	doc_no varchar(20), 
	receiver varchar(100), 
	sender varchar(100), 
	post_dest varchar(100), 
	fee double, 
	other_service_price double, 
	service_type_id int NOT NULL, 
	service_price double, 
	acc_name varchar(100), 
	bank_name varchar(100), 
	acc_no varchar(50), 
	tel varchar(20), 
	status tinyint, 
	amount double, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE service_type ( 
	id int NOT NULL, 
	name varchar(100) NOT NULL, 
	created_date date, 
	updated_date date, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
