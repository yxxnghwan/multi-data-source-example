drop table if exists example;

create schema exmaple;

create table exmaple.example(
    id bigserial primary key,
    content varchar(255)
);
