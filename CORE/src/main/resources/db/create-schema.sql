CREATE TABLE events ( 
  id                    BIGINT       NOT NULL,     -- Sequence generated opaque ID
  session_id            CHAR(32)     NOT NULL,     -- Variant session ID
  created_on            TIMESTAMP    NOT NULL,     -- Event creation timestamp.
  event_name            VARCHAR(64)  NOT NULL,     -- Event name
  event_value           VARCHAR(512) NOT NULL,     -- Event value
  CONSTRAINT events_pk PRIMARY KEY (id)
 );

CREATE INDEX events_session_id_ix on events (session_id);

CREATE SEQUENCE events_id_seq
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;
 
CREATE TABLE event_params ( 
  event_id              BIGINT REFERENCES events(id) ON DELETE CASCADE,
  key                   VARCHAR(64) NOT NULL, 
  value                 VARCHAR(512) NOT NULL
 );

CREATE INDEX event_params_ix1 on event_params (event_id);

CREATE TABLE event_experiences ( 
  id                    BIGINT       NOT NULL,     -- Sequence generated opaque ID
  event_id              BIGINT REFERENCES events(id) ON DELETE CASCADE,
  test_name             VARCHAR(128) NOT NULL,     -- Test name
  experience_name       VARCHAR(128) NOT NULL,     -- Experience name
  is_control BOOLEAN NOT NULL,                     -- Is experience control for the test?
  CONSTRAINT events_experiences_pk PRIMARY KEY (id),
  CONSTRAINT events_experiences_ix1 UNIQUE (event_id, test_name, experience_name)
 );

CREATE SEQUENCE event_experiences_id_seq
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;

CREATE TABLE event_experience_params ( 
  event_experience_id   BIGINT REFERENCES event_experiences(id) ON DELETE CASCADE,
  key                   VARCHAR(64) NOT NULL, 
  value                 VARCHAR(512) NOT NULL
 );

CREATE INDEX event_experience_params_ix1 on event_experience_params (event_experience_id);

CREATE VIEW events_v AS
  SELECT e.*, ee.test_name, ee.experience_name, ee.is_control,
         (SELECT string_agg('''' || key || '''=''' || value || '''', ',') FROM event_params where event_id = e.id) event_params,
         (SELECT string_agg('''' || key || '''=''' || value || '''', ',') FROM event_experience_params where event_experience_id = ee.id) event_experience_params
  FROM events e, event_experiences ee 
  WHERE e.id = ee.event_id
  ORDER BY event_id
;
