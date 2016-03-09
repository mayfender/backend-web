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
