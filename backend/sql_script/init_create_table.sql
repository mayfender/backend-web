create table spt_master_naming ( 
	naming_id int not null auto_increment, 
	label_name_th varchar(150), 
	label_name_en varchar(150), 
	status tinyint, 
	created_by int, 
	created_date datetime, 
	modified_by int, 
	modified_date datetime, 
	primary key (naming_id) 
) engine=innodb default charset=utf8;


create table spt_master_naming_det ( 
	naming_det_id int not null auto_increment, 
	naming_id int, 
	display_value varchar(150), 
	status tinyint, 
	created_by int, 
	created_date datetime, 
	modified_by int, 
	modified_date datetime, 
	primary key (naming_det_id) 
) engine=innodb default charset=utf8;


CREATE TABLE spt_member_type ( 
	member_type_id int NOT NULL AUTO_INCREMENT, 
	member_type_name varchar(150), 
	duration_type tinyint, 
	duration_qty int, 
	member_price double, 
	status tinyint, 
	created_by int, 
	created_date datetime, 
	modified_by int, 
	modified_date datetime, 
	PRIMARY KEY (member_type_id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE spt_registration ( 
	reg_id int NOT NULL AUTO_INCREMENT, 
	member_id varchar(30), 
	prefix_name varchar(30), 
	firstname varchar(150), 
	lastname varchar(150), 
	citizen_id varchar(13), 
	birthday date, 
	finger_id varchar(50), 
	picture_path varchar(200), 
	user_id int, 
	member_type_id int, 
	register_date date, 
	expire_date date, 
	con_tel_no varchar(50), 
	con_mobile_no varchar(50), 
	con_line_id varchar(50), 
	con_facebook varchar(50), 
	con_email varchar(100), 
	con_address varchar(200), 
	status tinyint, 
	is_active tinyint, 
	created_by int, 
	created_date datetime, 
	modified_by int, 
	modified_date datetime, 
	PRIMARY KEY (reg_id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
