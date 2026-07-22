create table users
(
    id       bigint auto_increment
        primary key,
    email    varchar(255) null,
    password varchar(255) null,
    username varchar(255) null
);
create table exercise_definitions
(
    id           bigint auto_increment
        primary key,
    muscle_group varchar(255) null,
    name         varchar(255) null
);
create table workouts
(
    id           bigint auto_increment
        primary key,
    date         date         null,
    workout_name varchar(255) null,
    user_id      bigint       null,
    constraint FKpf8ql3wbw2drijbk1ugfvki3d
        foreign key (user_id) references users (id)
);
create table workout_exercises
(
    id                     bigint auto_increment
        primary key,
    exercise_number        int    null,
    exercise_definition_id bigint null,
    workout_id             bigint null,
    constraint FKd2ychryarm8gp13672lojwr78
        foreign key (workout_id) references workouts (id),
    constraint FKkctac6f358p4nqmtrvsfopjhn
        foreign key (exercise_definition_id) references exercise_definitions (id)
);
create table exercise_sets
(
    id                  bigint auto_increment
        primary key,
    reps                int    null,
    rir                 int    null,
    set_number          int    null,
    weight              double null,
    workout_exercise_id bigint null,
    constraint FK8vmxlwj8qlov72fwgs77hgkal
        foreign key (workout_exercise_id) references workout_exercises (id)
);
create table refresh_tokens
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) null,
    expires_at datetime(6) null,
    revoked_at datetime(6) null,
    token      varchar(64) not null,
    user_id    bigint      not null,
    constraint UKghpmfn23vmxfu3spu3lfg4r2d
        unique (token),
    constraint FK1lih5y2npsf8u5o3vhdb9y0os
        foreign key (user_id) references users (id)
);
