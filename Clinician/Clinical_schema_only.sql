--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: alert; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE alert (
    alert_id bigint NOT NULL,
    patient_uuid character varying(100) NOT NULL,
    alert_time timestamp with time zone NOT NULL,
    alert_text character varying(5000) NOT NULL,
    data_id bigint NOT NULL,
    transmit_success_date timestamp with time zone,
    retry_attempts integer NOT NULL
);


ALTER TABLE alert OWNER TO postgres;

--
-- Name: alert_alert_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE alert_alert_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert_alert_id_seq OWNER TO postgres;

--
-- Name: alert_alert_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE alert_alert_id_seq OWNED BY alert.alert_id;


--
-- Name: attribute_threshold; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE attribute_threshold (
    attribute_threshold_id integer NOT NULL,
    attribute_id integer NOT NULL,
    patient_uuid character varying(100) NOT NULL,
    threshold_type character varying(100) NOT NULL,
    threshold_low_value character varying(100) NOT NULL,
    threshold_high_value character varying(100) NOT NULL,
    effective_date timestamp with time zone NOT NULL
);


ALTER TABLE attribute_threshold OWNER TO postgres;

--
-- Name: attribute_threshold_attribute_threshold_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE attribute_threshold_attribute_threshold_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE attribute_threshold_attribute_threshold_id_seq OWNER TO postgres;

--
-- Name: attribute_threshold_attribute_threshold_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE attribute_threshold_attribute_threshold_id_seq OWNED BY attribute_threshold.attribute_threshold_id;


--
-- Name: patient; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE patient (
    patient_uuid character varying(100) NOT NULL,
    patient_group_uuid character varying(100)
);


ALTER TABLE patient OWNER TO postgres;

--
-- Name: patient_details; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE patient_details (
    patient_uuid character varying(100) NOT NULL,
    nhs_number character varying(100) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    dob timestamp with time zone NOT NULL
);


ALTER TABLE patient_details OWNER TO postgres;

--
-- Name: patient_group; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE patient_group (
    patient_group_uuid character varying(100) NOT NULL,
    patient_group_name character varying(100)
);


ALTER TABLE patient_group OWNER TO postgres;

--
-- Name: recording_device_attribute; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE recording_device_attribute (
    attribute_id integer NOT NULL,
    attribute_name character varying(100) NOT NULL,
    type_id integer NOT NULL,
    attribute_units character varying(100),
    attribute_type character varying(100) NOT NULL
);


ALTER TABLE recording_device_attribute OWNER TO postgres;

--
-- Name: recording_device_attribute_attribute_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE recording_device_attribute_attribute_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE recording_device_attribute_attribute_id_seq OWNER TO postgres;

--
-- Name: recording_device_attribute_attribute_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE recording_device_attribute_attribute_id_seq OWNED BY recording_device_attribute.attribute_id;


--
-- Name: recording_device_data; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE recording_device_data (
    data_id bigint NOT NULL,
    attribute_id integer NOT NULL,
    data_value character varying(1000) NOT NULL,
    patient_uuid character varying(100) NOT NULL,
    data_value_time timestamp with time zone NOT NULL,
    downloaded_time timestamp with time zone NOT NULL,
    schedule_effective_time timestamp with time zone,
    schedule_expiry_time timestamp with time zone,
    alert_status character varying(100)
);


ALTER TABLE recording_device_data OWNER TO postgres;

--
-- Name: recording_device_data_data_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE recording_device_data_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE recording_device_data_data_id_seq OWNER TO postgres;

--
-- Name: recording_device_data_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE recording_device_data_data_id_seq OWNED BY recording_device_data.data_id;


--
-- Name: recording_device_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE recording_device_type (
    type_id integer NOT NULL,
    type character varying(100) NOT NULL,
    make character varying(100) NOT NULL,
    model character varying(100) NOT NULL,
    display_name character varying(1000) NOT NULL
);


ALTER TABLE recording_device_type OWNER TO postgres;

--
-- Name: recording_device_type_type_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE recording_device_type_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE recording_device_type_type_id_seq OWNER TO postgres;

--
-- Name: recording_device_type_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE recording_device_type_type_id_seq OWNED BY recording_device_type.type_id;


--
-- Name: alert_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY alert ALTER COLUMN alert_id SET DEFAULT nextval('alert_alert_id_seq'::regclass);


--
-- Name: attribute_threshold_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY attribute_threshold ALTER COLUMN attribute_threshold_id SET DEFAULT nextval('attribute_threshold_attribute_threshold_id_seq'::regclass);


--
-- Name: attribute_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY recording_device_attribute ALTER COLUMN attribute_id SET DEFAULT nextval('recording_device_attribute_attribute_id_seq'::regclass);


--
-- Name: data_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY recording_device_data ALTER COLUMN data_id SET DEFAULT nextval('recording_device_data_data_id_seq'::regclass);


--
-- Name: type_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY recording_device_type ALTER COLUMN type_id SET DEFAULT nextval('recording_device_type_type_id_seq'::regclass);


--
-- Name: alert_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY alert
    ADD CONSTRAINT alert_id PRIMARY KEY (alert_id);


--
-- Name: attribute_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY recording_device_attribute
    ADD CONSTRAINT attribute_id PRIMARY KEY (attribute_id);


--
-- Name: attribute_threshold_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY attribute_threshold
    ADD CONSTRAINT attribute_threshold_id PRIMARY KEY (attribute_threshold_id);


--
-- Name: data_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY recording_device_data
    ADD CONSTRAINT data_id PRIMARY KEY (data_id);


--
-- Name: patient_details_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY patient_details
    ADD CONSTRAINT patient_details_pk PRIMARY KEY (patient_uuid);


--
-- Name: patient_group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY patient_group
    ADD CONSTRAINT patient_group_pk PRIMARY KEY (patient_group_uuid);


--
-- Name: patient_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY patient
    ADD CONSTRAINT patient_id PRIMARY KEY (patient_uuid);


--
-- Name: type_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY recording_device_type
    ADD CONSTRAINT type_id PRIMARY KEY (type_id);


--
-- Name: patient_alert_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY alert
    ADD CONSTRAINT patient_alert_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);


--
-- Name: patient_attribute_threshold_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY attribute_threshold
    ADD CONSTRAINT patient_attribute_threshold_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);


--
-- Name: patient_group_patient_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY patient
    ADD CONSTRAINT patient_group_patient_fk FOREIGN KEY (patient_group_uuid) REFERENCES patient_group(patient_group_uuid);


--
-- Name: patient_patient_details_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY patient_details
    ADD CONSTRAINT patient_patient_details_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);


--
-- Name: patient_recording_device_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY recording_device_data
    ADD CONSTRAINT patient_recording_device_data_fk FOREIGN KEY (patient_uuid) REFERENCES patient(patient_uuid);


--
-- Name: recording_device_attribute_attribute_threshold_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY attribute_threshold
    ADD CONSTRAINT recording_device_attribute_attribute_threshold_fk FOREIGN KEY (attribute_id) REFERENCES recording_device_attribute(attribute_id);


--
-- Name: recording_device_attribute_recording_device_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY recording_device_data
    ADD CONSTRAINT recording_device_attribute_recording_device_data_fk FOREIGN KEY (attribute_id) REFERENCES recording_device_attribute(attribute_id);


--
-- Name: recording_device_data_alert_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY alert
    ADD CONSTRAINT recording_device_data_alert_fk FOREIGN KEY (data_id) REFERENCES recording_device_data(data_id);


--
-- Name: recording_device_type_recording_device_attribute_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY recording_device_attribute
    ADD CONSTRAINT recording_device_type_recording_device_attribute_fk FOREIGN KEY (type_id) REFERENCES recording_device_type(type_id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

