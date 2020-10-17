create table Screen
(
	SCREEN_ID bigint auto_increment
		primary key,
	TYPE varchar(20) not null,
	TIMING varchar(10) not null
)
comment 'movie show';

