CREATE 
	TABLE vehicle_parking 
	( 
		id int(11) NOT NULL AUTO_INCREMENT, 
		in_date_time datetime, 
		out_date_time datetime, 
		price int, 
		status tinyint DEFAULT '0' NOT NULL, 
		license_no VARCHAR(5) NOT NULL, 
		checkin_device_id VARCHAR(20),
		checkout_device_id VARCHAR(20),
		gate_in_name VARCHAR(100),
		gate_out_name VARCHAR(100),
		reason_no_scan tinyint,
		PRIMARY KEY (id)
	) 
	ENGINE=InnoDB DEFAULT CHARSET=utf8;

	
	
CREATE
    TABLE conf
    (
        conf_key VARCHAR(30),
        conf_value VARCHAR(100),
        PRIMARY KEY (conf_key)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
insert into conf (conf_key, conf_value) values ('after.price.rate', '30');
insert into conf (conf_key, conf_value) values ('before.hour', '1');
insert into conf (conf_key, conf_value) values ('before.hour.price.rate', '10');
insert into conf (conf_key, conf_value) values ('minute.to.hour', '1');
insert into conf (conf_key, conf_value) values ('price.per.time', '20');
insert into conf (conf_key, conf_value) values ('unlimted.time', 'false');
insert into conf (conf_key, conf_value) values ('parking.size', '-1');