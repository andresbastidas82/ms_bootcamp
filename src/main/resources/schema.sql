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