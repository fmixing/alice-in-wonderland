package com.test.dbclasses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DriveRepository extends JpaRepository<Drive, Long> {

    /**
     * Finds all the drives which starting points are equal to {@code from},
     * destination points are equal to {@code from},
     * dates are greater that or equal to {@code dateF}
     * and less that or equal to {@code dateT}
     */
    @Query("select d from Drive d where d.fromTown = :from and d.toTown = :to and d.date <= :dateT and d.date >= :dateF order by d.date ASC")
    List<Drive> find(@Param("from") Long from,
                    @Param("to") Long to,
                    @Param("dateF") Long dateF,
                    @Param("dateT") Long dateT);

    /**
     * Finds all the drives which dates are greater that or equal to dateF
     * and less that or equal to dateT
     */
    @Query("select d from Drive d where d.date <= :dateT and d.date >= :dateF")
    List<Drive> find(@Param("dateF") Long dateF,
                     @Param("dateT") Long dateT);

}
