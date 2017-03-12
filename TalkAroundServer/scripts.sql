CREATE USER talkaround WITH PASSWORD 'talkaround';
CREATE DATABASE talkaroundbase OWNER talkaround;

CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE EXTENSION postgis_tiger_geocoder;

CREATE TABLE talk
(
    id SERIAL PRIMARY KEY NOT NULL,
    creationdate timestamp NOT NULL,
    title VARCHAR(64) NOT NULL,
    text VARCHAR(2048),
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL
);
ALTER TABLE Talk
 ADD CONSTRAINT unique_id UNIQUE (id);

CREATE TABLE answer
(
    id SERIAL PRIMARY KEY NOT NULL,
    talkid NUMERIC(1000) NOT NULL,
    ordernumber INT NOT NULL,
    answerdate timestamp  NOT NULL,
    message VARCHAR(2048) NOT NULL,
    attachment VARCHAR(256),
    FOREIGN KEY (talkid) REFERENCES talk (id)
);
ALTER TABLE public.Answer
 ADD CONSTRAINT unique_id UNIQUE (id);