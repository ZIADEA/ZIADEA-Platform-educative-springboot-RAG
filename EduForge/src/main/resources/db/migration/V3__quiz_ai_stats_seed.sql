-- 1) colonne seuil de validation (par cours)
alter table course add column if not exists pass_threshold int default 70;

-- 2) quiz tables
create table if not exists quiz (
  id bigserial primary key,
  course_id bigint not null,
  difficulty varchar(20) not null,
  question_count int not null,
  created_at timestamptz not null default now(),
  constraint fk_quiz_course foreign key (course_id) references course(id) on delete cascade
);

create table if not exists quiz_question (
  id bigserial primary key,
  quiz_id bigint not null,
  q_index int not null,
  question_text text not null,
  a_text text not null,
  b_text text not null,
  c_text text not null,
  d_text text not null,
  correct_choice varchar(1) not null,
  explanation text not null,
  constraint fk_qq_quiz foreign key (quiz_id) references quiz(id) on delete cascade
);

create index if not exists idx_q_qz on quiz_question(quiz_id);

create table if not exists quiz_attempt (
  id bigserial primary key,
  quiz_id bigint not null,
  course_id bigint not null,
  student_id bigint not null,
  score_percent int not null,
  correct_count int not null,
  total_count int not null,
  status varchar(20) not null,
  created_at timestamptz not null default now(),
  constraint fk_attempt_quiz foreign key (quiz_id) references quiz(id) on delete cascade,
  constraint fk_attempt_course foreign key (course_id) references course(id) on delete cascade,
  constraint fk_attempt_student foreign key (student_id) references app_user(id) on delete cascade
);

create index if not exists idx_attempt_student_course on quiz_attempt(student_id, course_id);
create index if not exists idx_attempt_quiz on quiz_attempt(quiz_id);

create table if not exists quiz_attempt_answer (
  id bigserial primary key,
  attempt_id bigint not null,
  question_id bigint not null,
  chosen_choice varchar(1) not null,
  is_correct boolean not null,
  constraint fk_ans_attempt foreign key (attempt_id) references quiz_attempt(id) on delete cascade,
  constraint fk_ans_question foreign key (question_id) references quiz_question(id) on delete cascade
);

create index if not exists idx_ans_attempt on quiz_attempt_answer(attempt_id);

-- 3) seed (optionnel): si tu as déjà V1 seed users/classes, garde-le.
-- Ici on ne force rien, on laisse la seed de V1 gérer users.
