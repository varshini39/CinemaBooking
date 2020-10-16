create table Hall
(
	HALL_ID bigint auto_increment
		primary key,
	NAME varchar(100) null,
	TOTAL_SEATS int null,
	SEATS_BOOKED int default 0 null,
	RATE int null,
	constraint Hall_NAME_TOTAL_SEATS_RATE_uindex
		unique (NAME, TOTAL_SEATS, RATE)
)
comment 'Theatre hall details';

