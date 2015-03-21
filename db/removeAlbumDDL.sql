-- removeAlbumDDL.sql
--
-- This SQL*Plus script removes tables and other database objects
-- created to store album-related information.
--

--PROMPT DROP SEQUENCE individual_seq
DROP SEQUENCE individual_seq;

--PROMPT DROP TABLE album_genre
DROP TABLE album_genre PURGE;

--PROMPT DROP TABLE album_individual
DROP TABLE album_individual PURGE;

--PROMPT DROP TABLE individual
DROP TABLE individual PURGE;

--PROMPT DROP TABLE role
DROP TABLE role PURGE;

--PROMPT DROP TABLE album
DROP TABLE album PURGE;

--PROMPT DROP TABLE label
DROP TABLE label PURGE;

--PROMPT DROP TABLE rating
DROP TABLE rating PURGE;

--PROMPT DROP TABLE genre
DROP TABLE genre PURGE;
