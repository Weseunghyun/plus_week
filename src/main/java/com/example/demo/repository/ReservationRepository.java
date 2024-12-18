package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.item")
    List<Reservation> findAllWithUserAndItem();

    @EntityGraph(attributePaths = {"user", "item"})
    List<Reservation> findAll();

    default Reservation findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new IllegalStateException("존재하지 않는 예약입니다")
        );
    }

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.item.id = :id " +
            "AND NOT (r.endAt <= :startAt OR r.startAt >= :endAt) " +
            "AND r.status = 'APPROVED'")
    List<Reservation> findConflictingReservations(
            @Param("id") Long id,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
