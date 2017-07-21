package com.test.drive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DriveRepository extends JpaRepository<Drive, Long> {

    @Query("select d.id from Drive d where d.fromTown=:from and d.toTown=:to and d.date<=:dateT and date>=:dateF")
    List<Long> find(@Param("from") Long from,
                           @Param("to") Long to,
                           @Param("dateF") Long dateF,
                           @Param("dateT") Long dateT);

}
