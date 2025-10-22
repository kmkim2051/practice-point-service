package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
/**
 * 요구 기능
 * 1. 사용자 id로 포인트 조회
 * 2. 충전
 * 3. 포인트 이용
 * 4. 포인트 충전/이용 내역 조회
 *
 * - 기능 요구사항
 *     - 포인트 내역 조회 테스트를 위해, 데이터를 셋업하는 로직이 구현되어야 함
 *     - 잔고가 부족할 경우, 포인트 사용은 실패하여야 함
 *
 * - 직접 추가하는 요구사항
 *     - 비즈니스를 개발하다 보면 여러 가지 정책적인 부분이 결정되어야 함
 *     - 개별적으로 3가지의 요구사항을 정의하고, 이를 구현함
 *         - ex) 포인트 출금은 1000원 이하로 할 수 없다
 *         - ex) 포인트 사용은 100원 단위로만 가능하다
 * */
public class PointServiceTest {

    private PointService pointService;

    @BeforeEach
    void setUpPointService() {
        pointService = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());
    }

    // ---------- 1. 포인트 조회 ----------
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

    // ---------- 2. 충전 ----------
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

    // ---------- 3. 포인트 이용 ----------
    @Test
    @DisplayName("포인트를 이용하면 이용 금액만큼 차감된다")
    void usePoint() {
        // given
        long userId = 1L;
        long amount = 500L;
        pointService.chargeUserPoint(userId, amount);
        // when
        long useAmount = 100;
        UserPoint pointAfterUse = pointService.useUserPoint(userId, useAmount);

        // then
        assertThat(pointAfterUse.point()).isEqualTo(amount - useAmount);
    }
}