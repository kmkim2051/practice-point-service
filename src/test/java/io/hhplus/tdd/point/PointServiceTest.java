package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class PointServiceTest {
    /**
     * 요구 기능
     * 1. 포인트 조회
     * 2. 충전
     * 3. 사용
     * 4. 내역 조회
     * */
    private PointService pointService;

    @BeforeEach
    void setUpPointService() {
        pointService = new PointServiceImpl();
    }

    @Test
    @DisplayName("사용자 포인트 조회 결과는 null이 아니다")
    void getUserPointIsNotNull() {
        // given
        long userId = 1L;
        // when
        UserPoint userPoint = pointService.getUserPoint(userId);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(userId);
    }
}