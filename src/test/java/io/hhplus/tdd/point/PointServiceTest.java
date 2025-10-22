package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
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
        pointService = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());
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
    }

    @Test
    @DisplayName("처음 포인트를 충전하면 반환된 UserPoint 금액이 충전 금액과 동일하다")
    void chargeUserPointIsNotNull() {
        // given
        long userId = 1L;
        long amount = 500L;
        // when
        UserPoint userPoint = pointService.chargeUserPoint(userId, amount);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(amount);
    }
}