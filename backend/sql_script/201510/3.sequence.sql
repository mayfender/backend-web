CREATE TABLE `hibernate_sequences` (
  `sequence_name` varchar(255) default NULL,
  `sequence_next_hi_value` int(11) default NULL
);
INSERT INTO hibernate_sequences VALUES ('users.id', 2);
INSERT INTO hibernate_sequences VALUES ('roles.id', 2);
INSERT INTO hibernate_sequences VALUES ('vehicle_parking.id', 0);