------------------------------------------------------------------------TABLE DEFINITION:START------------------------------------------------------------------------
CREATE TABLE alert (
    alert_id bigint NOT NULL,
    patient_uuid character varying(100) NOT NULL,
    alert_time timestamp without time zone NOT NULL,
    alert_text character varying(5000) NOT NULL,
    data_id bigint NOT NULL,
    transmit_success_date timestamp without time zone,
    retry_attempts integer NOT NULL
);
CREATE SEQUENCE alert_alert_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE attribute_threshold (
    attribute_threshold_id integer NOT NULL,
    attribute_id integer NOT NULL,
    patient_uuid character varying(100) NOT NULL,
    threshold_type character varying(100) NOT NULL,
    threshold_low_value character varying(100) NOT NULL,
    threshold_high_value character varying(100) NOT NULL,
    effective_date timestamp without time zone NOT NULL
);
CREATE SEQUENCE attribute_threshold_attribute_threshold_id_seq START WITH 921 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE patient (
    patient_uuid character varying(100) NOT NULL,
    patient_group_uuid character varying(100)
);

CREATE TABLE patient_details (
    patient_uuid character varying(100) NOT NULL,
    nhs_number character varying(100) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    dob timestamp without time zone NOT NULL
);

CREATE TABLE patient_group (
    patient_group_uuid character varying(100) NOT NULL,
    patient_group_name character varying(100)
);

CREATE TABLE recording_device_attribute (
    attribute_id integer NOT NULL,
    attribute_name character varying(100) NOT NULL,
    type_id integer NOT NULL,
    attribute_units character varying(100),
    attribute_type character varying(100) NOT NULL
);
CREATE SEQUENCE recording_device_attribute_attribute_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE recording_device_data (
    data_id bigint NOT NULL,
    attribute_id integer NOT NULL,
    data_value character varying(1000) NOT NULL,
    patient_uuid character varying(100) NOT NULL,
    data_value_time timestamp without time zone NOT NULL,
    downloaded_time timestamp without time zone NOT NULL,
    schedule_effective_time timestamp without time zone,
    schedule_expiry_time timestamp without time zone,
    alert_status character varying(100)
);
CREATE SEQUENCE recording_device_data_data_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE recording_device_type (
    type_id integer NOT NULL,
    type character varying(100) NOT NULL,
    subtype character varying(100) NOT NULL
);
CREATE SEQUENCE recording_device_type_type_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
------------------------------------------------------------------------TABLE DEFINITION:END------------------------------------------------------------------------

------------------------------------------------------------------------SEQUNCES LINK:START------------------------------------------------------------------------
ALTER TABLE ONLY alert ALTER COLUMN alert_id SET DEFAULT nextval('alert_alert_id_seq'::regclass);
ALTER TABLE ONLY attribute_threshold ALTER COLUMN attribute_threshold_id SET DEFAULT nextval('attribute_threshold_attribute_threshold_id_seq'::regclass);
ALTER TABLE ONLY recording_device_attribute ALTER COLUMN attribute_id SET DEFAULT nextval('recording_device_attribute_attribute_id_seq'::regclass);
ALTER TABLE ONLY recording_device_data ALTER COLUMN data_id SET DEFAULT nextval('recording_device_data_data_id_seq'::regclass);
ALTER TABLE ONLY recording_device_type ALTER COLUMN type_id SET DEFAULT nextval('recording_device_type_type_id_seq'::regclass);

SELECT pg_catalog.setval('alert_alert_id_seq', 1, false);
SELECT pg_catalog.setval('attribute_threshold_attribute_threshold_id_seq', 1, false);
SELECT pg_catalog.setval('recording_device_attribute_attribute_id_seq', 1, false);
SELECT pg_catalog.setval('recording_device_data_data_id_seq', 1, false);
SELECT pg_catalog.setval('recording_device_type_type_id_seq', 1, false);
------------------------------------------------------------------------SEQUNCES LINK:END------------------------------------------------------------------------

------------------------------------------------------------------------CONSTRAINT:START------------------------------------------------------------------------
ALTER TABLE ONLY alert ADD CONSTRAINT alert_id PRIMARY KEY (alert_id);
ALTER TABLE ONLY recording_device_attribute ADD CONSTRAINT attribute_id PRIMARY KEY (attribute_id);
ALTER TABLE ONLY attribute_threshold ADD CONSTRAINT attribute_threshold_id PRIMARY KEY (attribute_threshold_id);
ALTER TABLE ONLY recording_device_data ADD CONSTRAINT data_id PRIMARY KEY (data_id);
ALTER TABLE ONLY patient_details ADD CONSTRAINT patient_details_pk PRIMARY KEY (patient_uuid);
ALTER TABLE ONLY patient_group ADD CONSTRAINT patient_group_pk PRIMARY KEY (patient_group_uuid);
ALTER TABLE ONLY patient ADD CONSTRAINT patient_id PRIMARY KEY (patient_uuid);
ALTER TABLE ONLY recording_device_type ADD CONSTRAINT type_id PRIMARY KEY (type_id);
ALTER TABLE ONLY alert ADD CONSTRAINT patient_alert_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);
ALTER TABLE ONLY attribute_threshold ADD CONSTRAINT patient_attribute_threshold_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);
ALTER TABLE ONLY patient ADD CONSTRAINT patient_group_patient_fk FOREIGN KEY (patient_group_uuid) REFERENCES patient_group(patient_group_uuid);
ALTER TABLE ONLY patient_details ADD CONSTRAINT patient_patient_details_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);
ALTER TABLE ONLY recording_device_data ADD CONSTRAINT patient_recording_device_data_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);
ALTER TABLE ONLY attribute_threshold ADD CONSTRAINT recording_device_attribute_attribute_threshold_fk FOREIGN KEY (attribute_id) REFERENCES recording_device_attribute(attribute_id);
ALTER TABLE ONLY recording_device_data ADD CONSTRAINT recording_device_attribute_recording_device_data_fk FOREIGN KEY (attribute_id) REFERENCES recording_device_attribute(attribute_id);
ALTER TABLE ONLY alert ADD CONSTRAINT recording_device_data_alert_fk FOREIGN KEY (data_id) REFERENCES recording_device_data(data_id);
ALTER TABLE ONLY recording_device_attribute ADD CONSTRAINT recording_device_type_recording_device_attribute_fk FOREIGN KEY (type_id) REFERENCES recording_device_type(type_id);
------------------------------------------------------------------------CONSTRAINT:END------------------------------------------------------------------------