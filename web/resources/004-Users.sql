create table Users
(
	USER_ID bigint auto_increment
		primary key,
	NAME varchar(100) not null,
	MOBILE_NUMBER bigint not null,
	SEATS_BOOKED varchar(100) null,
	SHOW_ID bigint null,
	constraint Users_NAME_MOBILE_NUMBER_uindex
		unique (NAME, MOBILE_NUMBER),
	constraint Users_Screening_SCREEN_ID_fk
		foreign key (SHOW_ID) references Shows (SHOW_ID)
			on delete cascade
)
comment 'Users booking for movie';

