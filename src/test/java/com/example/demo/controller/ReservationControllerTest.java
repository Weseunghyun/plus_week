package com.example.demo.controller;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.dto.Authentication;
import com.example.demo.dto.ReservationRequestDto;
import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        Authentication auth = new Authentication(1L, Role.USER);
        session.setAttribute(GlobalConstants.USER_AUTH, auth);

        startAt = LocalDateTime.now().withNano(0);
        endAt = startAt.plusDays(1);
    }

    // 공통 테스트 데이터 생성 메서드
    private ReservationRequestDto createRequestDto() {
        return new ReservationRequestDto(1L, 1L, startAt, endAt);
    }

    private ReservationResponseDto createResponseDto(Long id, String nickname, String itemName) {
        return new ReservationResponseDto(id, nickname, itemName, startAt, endAt);
    }

    @Nested
    @DisplayName("예약 생성 테스트")
    class CreateReservation {
        @Test
        @DisplayName("예약 생성 성공")
        void createReservation_Success() throws Exception {
            // given
            ReservationRequestDto requestDto = createRequestDto();
            ReservationResponseDto responseDto = createResponseDto(1L, "이름", "아이템이름");

            given(reservationService.createReservation(1L, 1L, startAt, endAt))
                .willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/reservations")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("이름"))
                .andExpect(jsonPath("$.itemName").value("아이템이름"));
        }
    }

    @Nested
    @DisplayName("예약 상태 업데이트 테스트")
    class UpdateReservation {
        @Test
        @DisplayName("예약 상태 업데이트 성공")
        void updateReservation_Success() throws Exception {
            // given
            Long reservationId = 1L;
            String status = "APPROVED";
            ReservationResponseDto responseDto = createResponseDto(1L, "이름", "아이템이름");

            given(reservationService.updateReservationStatus(reservationId, status))
                .willReturn(responseDto);

            // when & then
            mockMvc.perform(patch("/reservations/{id}/update-status", reservationId)
                    .session(session)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(status))
                .andDo(print())
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("예약 조회 테스트")
    class GetReservations {
        @Test
        @DisplayName("전체 예약 조회 성공")
        void findAll_Success() throws Exception {
            // given
            List<ReservationResponseDto> responseDtos = List.of(
                createResponseDto(1L, "이름1", "아이템1"),
                createResponseDto(2L, "이름2", "아이템2")
            );

            given(reservationService.getReservations()).willReturn(responseDtos);

            // when & then
            mockMvc.perform(get("/reservations")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
        }

        @Test
        @DisplayName("검색 조건으로 예약 조회 성공")
        void searchAll_Success() throws Exception {
            // given
            Long userId = 1L;
            Long itemId = 1L;
            List<ReservationResponseDto> responseDtos = List.of(
                createResponseDto(1L, "이름", "아이템")
            );

            given(reservationService.searchAndConvertReservations(userId, itemId))
                .willReturn(responseDtos);

            // when & then
            mockMvc.perform(get("/reservations/search")
                    .session(session)
                    .param("userId", userId.toString())
                    .param("itemId", itemId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        }
    }
}
