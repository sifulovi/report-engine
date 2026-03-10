-- Report metadata schema
-- Compatible with PostgreSQL, MySQL, H2

CREATE TABLE reports (
    code          VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    template_path VARCHAR(500) NOT NULL,
    query         TEXT,
    active        BOOLEAN DEFAULT TRUE
);

CREATE TABLE report_parameters (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code   VARCHAR(50) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(100) NOT NULL DEFAULT 'java.lang.String',
    required      BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(500),
    CONSTRAINT fk_param_report FOREIGN KEY (report_code) REFERENCES reports(code)
);

CREATE TABLE report_subreports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code     VARCHAR(50) NOT NULL,
    parameter_name  VARCHAR(100) NOT NULL,
    template_path   VARCHAR(500) NOT NULL,
    CONSTRAINT fk_sub_report FOREIGN KEY (report_code) REFERENCES reports(code)
);
