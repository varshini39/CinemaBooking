create table Movie
(
	MOVIE_ID bigint auto_increment
		primary key,
	NAME varchar(100) not null,
	DURATION double not null,
	GENRE varchar(50) null,
	constraint Movie_NAME_uindex
		unique (NAME)
)
comment 'movie description';

