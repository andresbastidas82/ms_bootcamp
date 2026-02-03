CREATE TABLE IF NOT EXISTS bootcamps (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(90) NOT NULL,
  launch_date DATE NOT NULL,
  duration INT NOT NULL
);

CREATE TABLE IF NOT EXISTS bootcamp_capacity (
    bootcamp_id BIGINT NOT NULL,
    capacity_id BIGINT NOT NULL,
    PRIMARY KEY (bootcamp_id, capacity_id),
    CONSTRAINT fk_bootcamp
        FOREIGN KEY (bootcamp_id)
            REFERENCES bootcamps(id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bootcamp_person (
     bootcamp_id BIGINT NOT NULL,
     person_id BIGINT NOT NULL,
     enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (bootcamp_id, person_id),
     CONSTRAINT fk_bootcamp_person
        FOREIGN KEY (bootcamp_id)
            REFERENCES bootcamps(id)
            ON DELETE CASCADE
);

