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
