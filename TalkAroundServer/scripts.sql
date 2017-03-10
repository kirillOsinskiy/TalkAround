CREATE USER talkaround WITH PASSWORD 'talkaround';
CREATE DATABASE talkaroundbase OWNER talkaround;

CREATE TABLE talk
(
    id SERIAL PRIMARY KEY NOT NULL,
    creationdate DATE NOT NULL,
    title VARCHAR(64) NOT NULL,
    text VARCHAR(128),
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL
);
ALTER TABLE Talk
 ADD CONSTRAINT unique_id UNIQUE (id);

CREATE TABLE answer
(
    id SERIAL PRIMARY KEY NOT NULL,
    talkid NUMERIC(131089) NOT NULL,
    ordernumber INT NOT NULL,
    answerdate DATE NOT NULL,
    message VARCHAR(256) NOT NULL,
    FOREIGN KEY (talkid) REFERENCES talk (id)
);
ALTER TABLE public.Answer
 ADD CONSTRAINT unique_id UNIQUE (id);