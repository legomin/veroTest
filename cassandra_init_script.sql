
CREATE KEYSPACE main WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

USE main;

CREATE TABLE entry (
     body    text,
     title   text,
     expires timestamp,
     privateEntry boolean,
     secret uuid,
     creationDate timestamp,
     PRIMARY KEY (privateEntry, creationDate))
     WITH CLUSTERING ORDER BY (creationDate DESC);