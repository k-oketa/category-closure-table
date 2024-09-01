drop table if exists category cascade;
drop table if exists category_path cascade;

create table category (
    category_id   bigserial   not null,
    category_name varchar(32) not null,
    primary key (category_id)
);

create table category_path (
    ancestor   bigint not null,
    descendant bigint not null,
    primary key (ancestor, descendant),
    foreign key (ancestor) references category (category_id),
    foreign key (descendant) references category (category_id)
);