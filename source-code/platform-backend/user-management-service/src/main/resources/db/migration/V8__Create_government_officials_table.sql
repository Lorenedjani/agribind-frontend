-- Create ENUM types
CREATE TYPE government_role_enum AS ENUM ('EXTENSION_OFFICER', 'AGRICULTURAL_ADVISOR', 'VETERINARY_OFFICER',
              'LOAN_OFFICER', 'POLICY_MAKER', 'INSPECTOR', 'RESEARCHER',
              'ADMINISTRATOR', 'OTHER');

CREATE TABLE government_officials (
                                      user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                      role government_role_enum NOT NULL,
                                      assigned_region region_type NOT NULL,
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
                                      rank VARCHAR(100),
                                      employment_date VARCHAR(20),
                                      responsibilities TEXT,
                                      projects_managed TEXT,
                                      reports_to VARCHAR(255),
                                      extension_number VARCHAR(50),
                                      is_field_officer BOOLEAN DEFAULT TRUE,
                                      vehicle_assignment VARCHAR(100),
                                      assigned_equipment TEXT
);

-- Create indexes
CREATE INDEX idx_role ON government_officials(role);
CREATE INDEX idx_assigned_region ON government_officials(assigned_region);
CREATE INDEX idx_employee_id ON government_officials(employee_id);