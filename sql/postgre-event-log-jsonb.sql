CREATE TABLE EVENT_LOG (
   ID SERIAL NOT NULL PRIMARY KEY,
   ORIGINAL_TABLE_NAME VARCHAR(100),
   OPERATION VARCHAR(20),
   FILTER JSONB,
   NEW_DATA JSONB,
   OLD_DATA JSONB,
   CREATE_DATE_TIME TIMESTAMP,
   STATUS VARCHAR(20)
);

INSERT INTO EVENT_LOG (ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS)
VALUES('worker', 'update', '{ "uuid": "no-uuid"}', '{ "name": "nomad"}', '{ "name": "tulip"}', '2019-01-20 01:01:01', 'active');

INSERT INTO EVENT_LOG (ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS)
VALUES('worker', 'update', '{ "uuid": "12345"}', '{ "name": "nomad", "id": "XYZ"}', '{ "name": "tulip", "id": "ABC"}', '2019-01-20 01:01:01', 'active');

INSERT INTO EVENT_LOG (ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS)
VALUES('worker', 'update', '{ "uuid": "123"}', '{ "name": "tulip", "id": "XYZASD"}', '{ "name": "tulip", "id": "ABCQW"}', '2019-01-20 01:01:01', 'active');

INSERT INTO EVENT_LOG (ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS)
VALUES('worker', 'update', '{ "uuid": "123"}', '{ "name": "tulip", "id": "XYZASD", "role": ["Neta", "Chamcha"]}', '{ "name": "tulip", "id": "ABCQW"}', '2019-01-20 01:01:01', 'active');

INSERT INTO EVENT_LOG (ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS)
VALUES('worker', 'update', '{ "uuid": "5"}', '{ "name": "tulip", "id": "XYZASD", "role": ["Boro-Neta", "Choto-Chamcha"]}', '{ "name": "tulip", "id": "ABCQW"}', '2019-01-20 01:01:01', 'active');

SELECT * FROM EVENT_LOG;

SELECT * FROM EVENT_LOG WHERE NEW_DATA->>'name' = 'nomad';

SELECT NEW_DATA -> 'id' AS ID FROM EVENT_LOG;

SELECT NEW_DATA ->> 'id' AS ID FROM EVENT_LOG;

SELECT count(*) FROM EVENT_LOG WHERE NEW_DATA?'name';

SELECT jsonb_array_elements_text(NEW_DATA->'role') as DT FROM EVENT_LOG;