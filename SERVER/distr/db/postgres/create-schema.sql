CREATE TABLE events ( 
  id                    BIGINT       NOT NULL,     -- Sequence generated opaque ID
  session_id            CHAR(32)     NOT NULL,     -- Variant session ID
  created_on            TIMESTAMP    NOT NULL,     -- Event creation timestamp.
  event_name            VARCHAR(64)  NOT NULL,     -- Event name
  CONSTRAINT events_pk PRIMARY KEY (id)
 );

CREATE INDEX events_session_id_ix on events (session_id);

CREATE SEQUENCE events_id_seq
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;
 
CREATE TABLE event_attributes ( 
  event_id              BIGINT REFERENCES events(id) ON DELETE CASCADE,
  name                  VARCHAR(64) NOT NULL, 
  value                 VARCHAR(512) NOT NULL
 );

CREATE INDEX event_attributes_ix1 on event_attributes (event_id);

CREATE TABLE event_experiences ( 
  id                    BIGINT       NOT NULL, 
  event_id              BIGINT REFERENCES events(id) ON DELETE CASCADE,
  variation_name        VARCHAR(512) NOT NULL,  
  experience_name       VARCHAR(512) NOT NULL,  
  is_control            BOOLEAN NOT NULL,       
  CONSTRAINT event_experiences_pk PRIMARY KEY (id),
  CONSTRAINT event_experiences_ix1 UNIQUE (event_id, variation_name, experience_name)
 );

CREATE SEQUENCE event_experiences_id_seq
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;

CREATE VIEW events_v AS
  SELECT e.id event_id, e.session_id, e.created_on, e.event_name, ev.variation_name, ev.experience_name, ev.is_control,
         (SELECT string_agg('''' || name || '''=''' || value || '''', ',') FROM event_attributes where event_id = e.id) event_attributes
  FROM events e left outer join event_experiences ev ON e.id = ev.event_id
  ORDER BY event_id;
