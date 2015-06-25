CREATE TABLE events ( 
  id                    BIGINT       NOT NULL,     -- Sequence generated event ID
  session_id            CHAR(32)     NOT NULL,     -- Variant session ID
  created_on            TIMESTAMP    NOT NULL,     -- Event creation timestamp.
  event_name            VARCHAR(64)  NOT NULL,     -- Event name
  event_value           VARCHAR(512) NOT NULL,     -- Event value
  status                INT,                       -- Event status, where applicable.
  CONSTRAINT events_pk PRIMARY KEY (id)
 );

CREATE SEQUENCE events_id_seq
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;
 
CREATE TABLE events_experiences ( 
  event_id              BIGINT REFERENCES events(id) ON DELETE CASCADE,
  test_name             VARCHAR(128) NOT NULL,     -- Test name
  experience_name       VARCHAR(128) NOT NULL,     -- Experience name
  is_control BOOLEAN NOT NULL,                     -- Is experience control for the test?
  is_view_invariant     BOOLEAN,                   -- If event is a view serve event, is this view invariant for this test?
  view_resolved_path    VARCHAR(256),              -- If event is a view serve event, the view's actual path; null otherwise.
  CONSTRAINT events_experiences_pk PRIMARY KEY (event_id, test_name, experience_name)
 );

CREATE TABLE event_params ( 
  event_id              BIGINT REFERENCES events(id) ON DELETE CASCADE,
  param_name            VARCHAR(64) NOT NULL, 
  param_value           VARCHAR(512) NOT NULL
 );
 