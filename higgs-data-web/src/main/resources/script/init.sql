create table data_configuration
(
  id bigint PRIMARY KEY,
  submit_flag varchar(20) not null,
  current_index int not null,
  max_index int not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


INSERT INTO data_configuration (id, submit_flag, current_index, max_index) VALUES (1,  DATE_FORMAT(NOW(), '%Y-%m-%d'), 1, 31);

INSERT INTO data_configuration (id, submit_flag, current_index, max_index) VALUES (2,  DATE_FORMAT(NOW(), '%Y-%m-%d %H'), 1, 3);