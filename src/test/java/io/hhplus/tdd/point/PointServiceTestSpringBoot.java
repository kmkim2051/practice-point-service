package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class PointServiceTestSpringBoot {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("특정 유저의 포인트를 조회할 수 있다")
    void getUserPoint() {
        // given
        long userId = 1L;

        // when
        UserPoint userPoint = pointService.getUserPoint(userId);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회할 수 있다")
    void getUserPointHistories() {
        // given
        long userId = 1L;

        // when
        List<PointHistory> histories = pointService.getUserPointHistories(userId);

        // then
        assertThat(histories).isNotNull();
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전할 수 있다")
    void chargeUserPoint() {
        // given
        long userId = 1L;
        long amount = 1000L;

        // when
        UserPoint userPoint = pointService.chargeUserPoint(userId, amount);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isGreaterThanOrEqualTo(amount);
    }

    @Test
    @DisplayName("포인트 충전 시 금액이 0 이하이면 예외가 발생한다")
    void chargeUserPoint_withInvalidAmount() {
        // given
        long userId = 1L;
        long invalidAmount = 0L;

        // when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, invalidAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용할 수 있다")
    void useUserPoint() {
        // given
        long userId = 1L;
        long chargeAmount = 1000L;
        long useAmount = 500L;

        // 먼저 포인트를 충전
        pointService.chargeUserPoint(userId, chargeAmount);

        // when
        UserPoint userPoint = pointService.useUserPoint(userId, useAmount);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(chargeAmount - useAmount);
    }

    @Test
    @DisplayName("포인트 사용 시 금액이 0 이하이면 예외가 발생한다")
    void useUserPoint_withInvalidAmount() {
        // given
        long userId = 1L;
        long invalidAmount = -100L;

        // when & then
        assertThatThrownBy(() -> pointService.useUserPoint(userId, invalidAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 사용 시 잔액이 부족하면 예외가 발생한다")
    void useUserPoint_withInsufficientBalance() {
        // given
        long userId = 1L;
        long useAmount = 10000L;

        // when & then
        assertThatThrownBy(() -> pointService.useUserPoint(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 충전 후 내역을 조회하면 CHARGE 타입의 이력이 있다")
    void chargeUserPoint_createsHistory() {
        // given
        long userId = 2L;
        long amount = 2000L;

        // when
        pointService.chargeUserPoint(userId, amount);
        List<PointHistory> histories = pointService.getUserPointHistories(userId);

        // then
        assertThat(histories).isNotEmpty();
        assertThat(histories).anyMatch(h ->
                h.userId() == userId &&
                h.amount() == amount &&
                h.type() == TransactionType.CHARGE
        );
    }

    @Test
    @DisplayName("포인트 사용 후 내역을 조회하면 USE 타입의 이력이 있다")
    void useUserPoint_createsHistory() {
        // given
        long userId = 3L;
        long chargeAmount = 3000L;
        long useAmount = 1000L;

        // when
        pointService.chargeUserPoint(userId, chargeAmount);
        pointService.useUserPoint(userId, useAmount);
        List<PointHistory> histories = pointService.getUserPointHistories(userId);

        // then
        assertThat(histories).isNotEmpty();
        assertThat(histories).anyMatch(h ->
                h.userId() == userId &&
                h.amount() == useAmount &&
                h.type() == TransactionType.USE
        );
    }
}