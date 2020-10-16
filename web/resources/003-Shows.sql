create table Shows
(
	SHOW_ID bigint auto_increment
		primary key,
	MOVIE_ID bigint not null,
	SCREEN_ID bigint not null,
	HALL_ID bigint not null,
	constraint Screening_MOVIE_ID_SCREEN_ID_HALL_ID_uindex
		unique (MOVIE_ID, SCREEN_ID, HALL_ID),
	constraint Shows_Hall_HALL_ID_fk
		foreign key (HALL_ID) references Hall (HALL_ID),
	constraint Shows_Movie_MOVIE_ID_fk
		foreign key (MOVIE_ID) references Movie (MOVIE_ID),
	constraint Shows_Screen_SCREEN_ID_fk
		foreign key (SCREEN_ID) references Screen (SCREEN_ID)
);

