CREATE TABLE government_officials (
    user_id BIGINT PRIMARY KEY,
    role ENUM('REGIONAL_COORDINATOR', 'DISTRICT_OFFICER', 'AGRICULTURAL_EXTENSION_AGENT',
              'VETERINARY_OFFICER', 'INSPECTOR', 'DATA_ANALYST', 'PROGRAM_MANAGER',
              'DIRECTOR') NOT NULL,
    assigned_region ENUM('ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL',
                         'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST') NOT NULL,
    department VARCHAR(255),
    employee_id VARCHAR(100),
    jurisdiction TEXT,
    can_approve_loans BOOLEAN DEFAULT FALSE,
    can_view_statistics BOOLEAN DEFAULT TRUE,
    supervisor VARCHAR(255),

    -- GovernmentDetails (Embedded)
    office_location VARCHAR(255),
    office_phone VARCHAR(25),
    official_email VARCHAR(255),
    official_rank VARCHAR(255),
    employment_date VARCHAR(20),
    responsibilities TEXT,
    projects_managed TEXT,
    reports_to VARCHAR(255),
    extension_number VARCHAR(50),
    is_field_officer BOOLEAN DEFAULT TRUE,
    vehicle_assignment VARCHAR(100),
    assigned_equipment TEXT,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_role (role),
    INDEX idx_assigned_region (assigned_region),
    INDEX idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
