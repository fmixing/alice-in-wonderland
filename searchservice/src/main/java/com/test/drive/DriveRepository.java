package com.test.drive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface DriveRepository extends JpaRepository<Drive, Long> {

}
