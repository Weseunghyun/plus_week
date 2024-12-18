package com.example.demo.service;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.LogType;
import com.example.demo.entity.RentalLog;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.ReservationStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.SearchReservationsQueryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RentalLogService rentalLogService;
    private final SearchReservationsQueryRepository searchReservationsQueryRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ItemRepository itemRepository,
        UserRepository userRepository,
        RentalLogService rentalLogService,
        SearchReservationsQueryRepository searchReservationsQueryRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.rentalLogService = rentalLogService;
        this.searchReservationsQueryRepository = searchReservationsQueryRepository;
    }

    // TODO: 1. 트랜잭션 이해
    @Transactional
    public ReservationResponseDto createReservation(Long itemId, Long userId, LocalDateTime startAt, LocalDateTime endAt) {
        // 쉽게 데이터를 생성하려면 아래 유효성검사 주석 처리
        List<Reservation> haveReservations = reservationRepository.findConflictingReservations(itemId, startAt, endAt);
        if(!haveReservations.isEmpty()) {
            throw new ReservationConflictException("해당 물건은 이미 그 시간에 예약이 있습니다.");
        }

        Item item = itemRepository.findByIdOrElseThrow(itemId);
        User user = userRepository.findByIdOrElseThrow(userId);

        Reservation reservation = new Reservation(item, user, ReservationStatus.PENDING, startAt, endAt);
        Reservation savedReservation = reservationRepository.save(reservation);

        RentalLog rentalLog = new RentalLog(savedReservation, "로그 메세지", LogType.SUCCESS);
        rentalLogService.save(rentalLog);

        return ReservationResponseDto.toDto(savedReservation);
    }

    // TODO: 3. N+1 문제
    public List<ReservationResponseDto> getReservations() {
        List<Reservation> reservations = reservationRepository.findAllWithUserAndItem();

        return ReservationResponseDto.toListDto(reservations);
    }

    // TODO: 5. QueryDSL 검색 개선
    public List<ReservationResponseDto> searchAndConvertReservations(Long userId, Long itemId) {

        List<Reservation> reservations = searchReservationsQueryRepository.searchReservations(userId, itemId);

        return ReservationResponseDto.toListDto(reservations);
    }


    // TODO: 7. 리팩토링
    @Transactional
    public ReservationResponseDto updateReservationStatus(Long reservationId, String status) {
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

        Reservation updateReservation = validateReservationStatus(reservation, status);

        return ReservationResponseDto.toDto(updateReservation);
    }


    private Reservation validateReservationStatus(Reservation reservation, String status) {
        ReservationStatus reservationStatus = ReservationStatus.valueOf(status);

        switch (reservationStatus) {
            case APPROVED:
                if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
                    throw new IllegalArgumentException("PENDING 상태만 APPROVED로 변경 가능합니다.");
                }
                reservation.updateStatus(ReservationStatus.APPROVED);
                break;

            case CANCELLED:
                if (!reservation.getStatus().equals(ReservationStatus.EXPIRED)) {
                    throw new IllegalArgumentException("EXPIRED 상태인 예약은 취소할 수 없습니다.");
                }
                reservation.updateStatus(ReservationStatus.CANCELLED);
                break;

            case EXPIRED:
                if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
                    throw new IllegalArgumentException("PENDING 상태만 EXPIRED로 변경 가능합니다.");
                }
                reservation.updateStatus(ReservationStatus.EXPIRED);
                break;

            default:
                throw new IllegalArgumentException("올바르지 않은 상태: " + status);
        }

        // 상태 변경 후 return 해주기 위한 save 메서드 호출
        return reservationRepository.save(reservation);
    }


}
