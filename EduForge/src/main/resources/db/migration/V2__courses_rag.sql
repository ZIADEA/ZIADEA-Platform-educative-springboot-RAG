create table if not exists course (
  id bigserial primary key,
  owner_prof_id bigint not null,
  title varchar(180) not null,
  description varchar(800),
  text_content text,
  status varchar(30) not null,
  indexed_at timestamptz,
  created_at timestamptz not null default now(),
  constraint fk_course_prof foreign key (owner_prof_id) references app_user(id) on delete cascade
);

create table if not exists course_material (
  id bigserial primary key,
  course_id bigint not null,
  type varchar(20) not null,
  original_name varchar(255) not null,
  stored_path varchar(500) not null,
  content_text text,
  created_at timestamptz not null default now(),
  constraint fk_mat_course foreign key (course_id) references course(id) on delete cascade
);

create table if not exists classroom_course (
  id bigserial primary key,
  classroom_id bigint not null,
  course_id bigint not null,
  constraint fk_cc_class foreign key (classroom_id) references classroom(id) on delete cascade,
  constraint fk_cc_course foreign key (course_id) references course(id) on delete cascade,
  constraint uq_class_course unique (classroom_id, course_id)
);

create table if not exists course_chunk (
  id bigserial primary key,
  course_id bigint not null,
  chunk_index int not null,
  chunk_text text not null,
  terms_json text not null,
  created_at timestamptz not null default now(),
  constraint fk_chunk_course foreign key (course_id) references course(id) on delete cascade
);

create index if not exists idx_chunk_course on course_chunk(course_id);
