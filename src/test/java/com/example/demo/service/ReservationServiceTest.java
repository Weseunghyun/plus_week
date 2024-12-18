package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.RentalLog;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.ReservationStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.SearchReservationsQueryRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RentalLogService rentalLogService;
    @Mock
    private SearchReservationsQueryRepository searchReservationsQueryRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Item testItem;
    private User testUser;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @BeforeEach
    void setUp() {
        testItem = new Item("테스트 아이템", "아이템 설명");
        testUser = new User("user", "test@example.com", "테스트 사용자", "password");
        startAt = LocalDateTime.now().withNano(0);
        endAt = startAt.plusDays(1);
    }

    @Nested
    @DisplayName("예약 생성 테스트")
    class CreateReservationTests {
        @Test
        @DisplayName("정상적인 예약 생성")
        void createReservationSuccess() {
            // given
            Long itemId = 1L;
            Long userId = 1L;

            List<Reservation> emptyReservations = new ArrayList<>();
            Reservation expectedReservation = new Reservation(testItem, testUser, ReservationStatus.PENDING, startAt, endAt);

            when(reservationRepository.findConflictingReservations(itemId, startAt, endAt)).thenReturn(emptyReservations);
            when(itemRepository.findByIdOrElseThrow(itemId)).thenReturn(testItem);
            when(userRepository.findByIdOrElseThrow(userId)).thenReturn(testUser);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(expectedReservation);

            // when
            ReservationResponseDto responseDto = reservationService.createReservation(itemId, userId, startAt, endAt);

            // then
            assertAll(
                () -> assertEquals(testItem.getName(), responseDto.getItemName()),
                () -> assertEquals(testUser.getNickname(), responseDto.getNickname()),
                () -> assertEquals(ReservationStatus.PENDING, expectedReservation.getStatus())
            );

            verify(reservationRepository).save(any(Reservation.class));
            verify(rentalLogService).save(any(RentalLog.class));
        }

        @Test
        @DisplayName("중복된 예약 시도 시 예외 발생")
        void createReservationWithConflict() {
            // given
            Long itemId = 1L;
            Long userId = 1L;
            Reservation existingReservation = new Reservation(testItem, testUser, ReservationStatus.PENDING, startAt, endAt);
            List<Reservation> conflictingReservations = Collections.singletonList(existingReservation);

            when(reservationRepository.findConflictingReservations(itemId, startAt, endAt)).thenReturn(conflictingReservations);

            // when, then
            assertThrows(ReservationConflictException.class, () ->
                reservationService.createReservation(itemId, userId, startAt, endAt)
            );
        }
    }

    @Nested
    @DisplayName("예약 상태 변경 테스트")
    class UpdateReservationStatusTests {
        @Test
        @DisplayName("PENDING에서 APPROVED로 상태 변경")
        void updateReservationStatusToPending() {
            // given
            Long reservationId = 1L;
            Reservation pendingReservation = new Reservation(testItem, testUser, ReservationStatus.PENDING, startAt, endAt);

            when(reservationRepository.findByIdOrElseThrow(reservationId)).thenReturn(pendingReservation);
            when(reservationRepository.save(pendingReservation)).thenReturn(pendingReservation);

            // when
            ReservationResponseDto responseDto = reservationService.updateReservationStatus(reservationId, "APPROVED");

            // then
            assertAll(
                () -> assertEquals(ReservationStatus.APPROVED, pendingReservation.getStatus()),
                () -> assertEquals(testItem.getName(), responseDto.getItemName()),
                () -> assertEquals(testUser.getNickname(), responseDto.getNickname())
            );
        }

        @Test
        @DisplayName("잘못된 상태 변경 시 예외 발생")
        void updateReservationStatusWithInvalidTransition() {
            // given
            Long reservationId = 1L;
            Reservation approvedReservation = new Reservation(testItem, testUser, ReservationStatus.APPROVED, startAt, endAt);

            when(reservationRepository.findByIdOrElseThrow(reservationId)).thenReturn(approvedReservation);

            // when, then
            assertThrows(IllegalArgumentException.class, () ->
                reservationService.updateReservationStatus(reservationId, "APPROVED")
            );
        }
    }

    @Nested
    @DisplayName("예약 조회 테스트")
    class ReservationQueryTests {
        @Test
        @DisplayName("전체 예약 목록 조회")
        void getReservationsSuccess() {
            // given
            List<Reservation> reservations = List.of(
                new Reservation(testItem, testUser, ReservationStatus.PENDING, startAt, endAt)
            );

            when(reservationRepository.findAllWithUserAndItem()).thenReturn(reservations);

            // when
            List<ReservationResponseDto> getReservations = reservationService.getReservations();

            // then
            assertAll(
                () -> assertEquals(1, getReservations.size()),
                () -> assertEquals(testItem.getName(), getReservations.getFirst().getItemName()),
                () -> assertEquals(testUser.getNickname(), getReservations.getFirst().getNickname())
            );
        }

        @Test
        @DisplayName("조건부 예약 검색")
        void searchAndConvertReservationsSuccess() {
            // given
            Long itemId = 1L;
            Long userId = 1L;
            List<Reservation> reservations = List.of(
                new Reservation(testItem, testUser, ReservationStatus.PENDING, startAt, endAt)
            );

            when(searchReservationsQueryRepository.searchReservations(userId, itemId)).thenReturn(reservations);

            // when
            List<ReservationResponseDto> responseDtos = reservationService.searchAndConvertReservations(userId, itemId);

            // then
            assertAll(
                () -> assertEquals(1, responseDtos.size()),
                () -> assertEquals(testItem.getName(), responseDtos.getFirst().getItemName()),
                () -> assertEquals(testUser.getNickname(), responseDtos.getFirst().getNickname())
            );
        }
    }
}