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
	img_id int, 
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
	created_by int, 
	modified_by int, 
	PRIMARY KEY (reg_id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE image ( id int NOT NULL AUTO_INCREMENT, 
	image_name varchar(100) NOT NULL, 
	image_content longblob NOT NULL, 
	image_type_id int NOT NULL, 
	created_date datetime, 
	updated_date datetime, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE image_type ( 
	id int NOT NULL, 
	type_name varchar(10) NOT NULL, 
	PRIMARY KEY (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into image_type (id, type_name) values (1, 'JPG');
insert into image_type (id, type_name) values (2, 'PNG');
insert into image_type (id, type_name) values (3, 'GIF');



CREATE TABLE spt_import_finger_det ( 
	finger_det_id int NOT NULL AUTO_INCREMENT, 
	finger_id varchar(10), 
	date_time datetime, 
	in_out char(1), 
	finger_file_id int, 
	PRIMARY KEY (finger_det_id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE spt_import_finger_file ( 
	finger_file_id int NOT NULL AUTO_INCREMENT, 
	file_name varchar(100), 
	created_date_time datetime, 
	started_date_time datetime, 
	ended_date_time datetime, 
	PRIMARY KEY (finger_file_id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


