-- createAlbumDDL.sql
--
-- This SQL*Plus script creates tables and other necessary objects to store
-- album-related information.
--

--PROMPT CREATE TABLE genre
CREATE TABLE genre
(
   label VARCHAR2(15) CONSTRAINT genre_pk
                      PRIMARY KEY,
   description VARCHAR2(250)
);

--PROMPT CREATE TABLE rating
CREATE TABLE rating
(
   label VARCHAR2(5) CONSTRAINT rating_pk
                     PRIMARY KEY,
   rating_description VARCHAR2(50) NOT NULL,
   num_stars NUMBER(1,0)
);

--PROMPT CREATE TABLE label
CREATE TABLE label
(
   id VARCHAR2(25) CONSTRAINT label_pk
                   PRIMARY KEY
);

--PROMPT CREATE TABLE album
CREATE TABLE album
(
   id NUMBER CONSTRAINT album_pk
             PRIMARY KEY,
   title VARCHAR2(25) NOT NULL,
   band_name VARCHAR2(40) NOT NULL,
   year NUMBER(4) NOT NULL,
   description VARCHAR2(250),
   label VARCHAR2(25) NOT NULL
          CONSTRAINT label_fk
          REFERENCES label(id),
   rating_label VARCHAR2(5) NOT NULL
          CONSTRAINT fk_rating
          REFERENCES rating(label)
);

--PROMPT CREATE TABLE role
CREATE TABLE role
(
   name VARCHAR2(25) CONSTRAINT role_pk
                     PRIMARY KEY
);

--PROMPT CREATE TABLE individual
CREATE TABLE individual
(
   id NUMBER CONSTRAINT individual_pk
             PRIMARY KEY,
   first_name VARCHAR2(25),
   last_name VARCHAR2(25),
   role VARCHAR2(25)
        CONSTRAINT fk_individual_role
        REFERENCES role(name),
   gender VARCHAR2(6) CONSTRAINT gender_cc
        CHECK (gender IN ('FEMALE', 'MALE'))
);

--PROMPT CREATE TABLE album_individual
CREATE TABLE album_individual
(
   movie_id NUMBER CONSTRAINT ai_album_fk
                   REFERENCES album(id),
   person_id NUMBER CONSTRAINT ai_individual_fk
                      REFERENCES individual(id)
);

--PROMPT CREATE TABLE album_genre
CREATE TABLE album_genre
(
   album_id NUMBER CONSTRAINT ag_album_fk
                   REFERENCES album(id),
   genre_label VARCHAR2(15) CONSTRAINT ag_genre_fk
                            REFERENCES genre(label)
);

--PROMPT CREATE SEQUENCE individual_seq
CREATE SEQUENCE individual_seq;
