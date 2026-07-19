CREATE TABLE "films" (
  "film_id" integer PRIMARY KEY,
  "name" varchar(200) NOT NULL,
  "description" varchar(200) NOT NULL,
  "release_date" date NOT NULL,
  "duration" integer NOT NULL,
  "mpa_id" integer NOT NULL
);

CREATE TABLE "users" (
  "user_id" integer PRIMARY KEY,
  "email" varchar(100) UNIQUE NOT NULL,
  "login" varchar(100) UNIQUE NOT NULL,
  "name" varchar(100),
  "birthday" date
);

CREATE TABLE "genres" (
  "genre_id" integer PRIMARY KEY,
  "name" varchar(100) UNIQUE NOT NULL
);

CREATE TABLE "mpa" (
  "mpa_id" integer PRIMARY KEY,
  "name" varchar(100) UNIQUE NOT NULL
);

CREATE TABLE "film_likes" (
  "film_id" integer,
  "user_id" integer,
  PRIMARY KEY ("film_id", "user_id")
);

CREATE TABLE "film_genres" (
  "film_id" integer,
  "genre_id" integer,
  PRIMARY KEY ("film_id", "genre_id")
);

CREATE TABLE "user_friendships" (
  "user_id" integer,
  "friend_id" integer,
  "status" varchar(20) NOT NULL,
  PRIMARY KEY ("user_id", "friend_id")
);

ALTER TABLE "films" ADD FOREIGN KEY ("mpa_id") REFERENCES "mpa" ("mpa_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "film_likes" ADD FOREIGN KEY ("film_id") REFERENCES "films" ("film_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "film_likes" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "film_genres" ADD FOREIGN KEY ("film_id") REFERENCES "films" ("film_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "film_genres" ADD FOREIGN KEY ("genre_id") REFERENCES "genres" ("genre_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "user_friendships" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "user_friendships" ADD FOREIGN KEY ("friend_id") REFERENCES "users" ("user_id") DEFERRABLE INITIALLY IMMEDIATE;
