package com.example.shift.shift.repository;

import com.example.shift.shift.domain.Shift;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByWorkDateBetweenOrderByWorkDateAscStartTimeAsc(LocalDate from, LocalDate to);

    List<Shift> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(Long employeeId, LocalDate from, LocalDate to);

    @Query("""
        select s from Shift s
        where s.employee.id = :employeeId
          and s.workDate = :workDate
          and s.startTime < :endTime
          and :startTime < s.endTime
        """)
    List<Shift> findOverlaps(@Param("employeeId") Long employeeId,
                             @Param("workDate") LocalDate workDate,
                             @Param("startTime") LocalTime startTime,
                             @Param("endTime") LocalTime endTime);
}
