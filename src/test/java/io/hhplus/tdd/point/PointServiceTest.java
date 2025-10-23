package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.PointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.hhplus.tdd.exception.PointException.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 요구 기능
 * 1. 사용자 id로 포인트 조회
 * 2. 충전
 * 3. 포인트 사용
 * 4. 포인트 충전/사용 내역 조회
 *
 * - 기능 요구사항
 *     - 포인트 내역 조회 테스트를 위해, 데이터를 셋업하는 로직이 구현되어야 함
 *     - 잔고가 부족할 경우, 포인트 사용은 실패하여야 함
 *

 포인트 서비스 정책
 [사용 정책]
 #1. 100원 미만의 포인트를 사용할 수 없다.
 [충전 정책]
 #2. 100원 미만의 포인트를 충전할 수 없다.
 #3. 포인트 충전은 최소 10원 단위로 수행한다.

 *
 * ### **`STEP1 - TDD 기본`**
 * - /point 패키지(디렉토리) 내에 PointService 기본 기능 작성
 * - /database 패키지의 구현체는 수정하지 않고, 이를 활용해 기능 구현
 * - 각 기능에 대한 단위 테스트 작성
 * - 총 4가지 기본 기능 구현 (포인트 조회, 충전, 사용, 내역 조회)
 *
 * ### **`STEP2 - TDD 심화`**
 *
 * - 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
 * - 동일한 사용자에 대한 동시 요청이 정상적으로 처리될 수 있도록 개선
 * - 주어진 4가지 기능에 대한 통합 테스트 작성
 * - 선택한 언어에 대한 동시성 제어 방식 및 장/단점을 기술한 보고서 작성 (README.md)
 **/
@DisplayName("PointService 단위 테스트")
public class PointServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        pointService = new PointServiceImpl(userPointTable, pointHistoryTable);
    }

    @Nested
    @DisplayName("포인트 조회")
    class GetUserPoint {

        @Test
        @DisplayName("사용자의 현재 포인트 잔액을 조회할 수 있다")
        void getUserPoint() {
            // given
            long userId = 1L;
            long currentAmount = 100L;
            UserPoint mockUserPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());

            when(userPointTable.selectById(userId))
                    .thenReturn(mockUserPoint);

            // when
            UserPoint userPoint = pointService.getUserPoint(userId);

            // then
            assertThat(userPoint).isNotNull();
            assertThat(userPoint.id()).isEqualTo(userId);
            assertThat(userPoint.point()).isEqualTo(currentAmount);
        }
    }
    /*
     [충전 정책]
       #2. 100원 미만의 포인트를 충전할 수 없다.
       #3. 포인트 충전은 10원 단위로 수행한다.
     */
    @Nested
    @DisplayName("포인트 충전")
    class ChargeUserPoint {

        @Test
        @DisplayName("100원 미만의 포인트를 충전 시 예외가 발생한다")
        void cantChargeLessThan100() {
            // given
            long userId = 1L;
            long invalidAmount = 99L;
            when(userPointTable.selectById(userId))
                    .thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));

            // when & then
            assertThatThrownBy(() -> pointService.chargeUserPoint(userId, invalidAmount))
                    .isInstanceOf(InvalidChargeAmountException.class);

            verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        }

        @Test
        @DisplayName("10원 단위가 아닌 포인트를 충전 시 예외가 발생한다")
        void cantChargeNotMultipleOf10() {
            // given
            long userId = 1L;
            long invalidAmount = 105L;
            when(userPointTable.selectById(userId))
                    .thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));

            // when & then
            assertThatThrownBy(() -> pointService.chargeUserPoint(userId, invalidAmount))
                    .isInstanceOf(InvalidChargeAmountException.class);

            verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        }

        @Test
        @DisplayName("포인트를 충전 후 조회하면, 포인트는 충전 금액만큼 증가한다")
        void chargePoint() {
            // given
            long userId = 1L;
            long amount = 500L;
            UserPoint currentPoint = new UserPoint(userId, 0L, System.currentTimeMillis());
            UserPoint chargedPoint = new UserPoint(userId, amount, System.currentTimeMillis());

            when(userPointTable.selectById(userId))
                    .thenReturn(currentPoint);
            when(userPointTable.insertOrUpdate(userId, amount))
                    .thenReturn(chargedPoint);

            // when
            UserPoint userPoint = pointService.chargeUserPoint(userId, amount);

            // then
            assertThat(userPoint).isNotNull();
            assertThat(userPoint.id()).isEqualTo(userId);
            assertThat(userPoint.point()).isEqualTo(amount);
        }

        @Test
        @DisplayName("사용자가 포인트를 충전하면, 충전 내역이 히스토리에 기록된다")
        void chargeUserPoint_ThenCreatePointHistory() {
            // given
            long userId = 1L;
            long currentAmount = 200L;
            long chargeAmount = 1000L;

            UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
            UserPoint chargedPoint = new UserPoint(userId, currentAmount + chargeAmount, System.currentTimeMillis());

            when(userPointTable.selectById(userId))
                    .thenReturn(currentPoint);
            when(userPointTable.insertOrUpdate(userId, currentAmount + chargeAmount))
                    .thenReturn(chargedPoint);

            // when
            pointService.chargeUserPoint(userId, chargeAmount);

            // then
            verify(pointHistoryTable, times(1))
                    .insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
        }
    }

//     [사용 정책]
//    #1. 100원 미만의 포인트를 사용할 수 없다.
    @Nested
    @DisplayName("포인트 사용")
    class UseUserPoint {

        @Test
        @DisplayName("100원 미만의 포인트를 사용할 수 없다.")
        void cantUsePointLessThan100() {
            // given
            long userId = 1L;
            long invalidAmount = 99L;
            when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 500, System.currentTimeMillis()));

            // when, then
            assertThatThrownBy(() -> pointService.useUserPoint(userId, invalidAmount))
                    .isInstanceOf(InvalidUseAmountException.class);

            verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());

        }

        @Test
        @DisplayName("포인트를 사용하면 사용 금액만큼 차감된다")
        void usePoint() {
            // given
            long userId = 1L;
            long currentAmount = 500L;
            long useAmount = 100L;

            UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
            UserPoint expectedPoint = new UserPoint(userId, currentAmount - useAmount, System.currentTimeMillis());

            when(userPointTable.selectById(userId))
                    .thenReturn(currentPoint);
            when(userPointTable.insertOrUpdate(currentPoint.id(), currentAmount - useAmount))
                    .thenReturn(expectedPoint);

            // when
            UserPoint pointAfterUse = pointService.useUserPoint(userId, useAmount);

            // then
            assertThat(pointAfterUse.point()).isEqualTo(currentAmount - useAmount);
        }

        @Test
        @DisplayName("잔액이 부족하면 포인트 사용에 실패하고 예외가 발생한다")
        void usePointWithInsufficientBalance() {
            // given
            long userId = 1L;
            long currentAmount = 500L;
            long useAmount = 501L;

            UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
            when(userPointTable.selectById(userId)).thenReturn(currentPoint);

            // when & then
            assertThatThrownBy(() -> pointService.useUserPoint(userId, useAmount))
                    .isInstanceOf(InsufficientPointException.class)
                    .hasMessageContaining("잔액이 부족합니다");

            verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        }

        @Test
        @DisplayName("포인트를 사용하면, 사용 내역이 히스토리에 기록된다")
        void usePoint_thenCreateHistory() {
            // given
            long userId = 1L;
            long currentAmount = 1000L;
            long toUse = 100;

            UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
            UserPoint usedPoint = new UserPoint(userId, currentAmount - toUse, System.currentTimeMillis());

            when(userPointTable.selectById(userId))
                    .thenReturn(currentPoint);
            when(userPointTable.insertOrUpdate(userId, currentAmount - toUse))
                    .thenReturn(usedPoint);

            // when
            UserPoint userPoint = pointService.useUserPoint(userId, toUse);

            // then
            verify(pointHistoryTable, times(1))
                    .insert(eq(userId), eq(toUse), eq(TransactionType.USE), anyLong());
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회")
    class GetUserPointHistories {

        @Test
        @DisplayName("사용자의 포인트 충전/사용 내역을 조회할 수 있다")
        void getPointHistories() {
            // given
            long userId = 1L;
            long pointHistoryId1 = 1L;
            long chargeAmount = 500L;
            long pointHistoryId2 = 2L;
            long useAmount = 100L;

            PointHistory chargeHistory = new PointHistory(
                    pointHistoryId1,
                    userId,
                    chargeAmount,
                    TransactionType.CHARGE,
                    System.currentTimeMillis()
            );
            PointHistory useHistory = new PointHistory(
                    pointHistoryId2,
                    userId,
                    useAmount,
                    TransactionType.USE,
                    System.currentTimeMillis()
            );

            List<PointHistory> mockHistories = List.of(chargeHistory, useHistory);

            when(pointHistoryTable.selectAllByUserId(userId))
                    .thenReturn(mockHistories);

            // when
            List<PointHistory> userPointHistories = pointService.getUserPointHistories(userId);

            // then
            assertThat(userPointHistories).hasSize(2);

            assertThat(userPointHistories.get(0).id()).isEqualTo(pointHistoryId1);
            assertThat(userPointHistories.get(0).userId()).isEqualTo(userId);
            assertThat(userPointHistories.get(0).amount()).isEqualTo(chargeAmount);
            assertThat(userPointHistories.get(0).type()).isEqualTo(TransactionType.CHARGE);

            assertThat(userPointHistories.get(1).id()).isEqualTo(pointHistoryId2);
            assertThat(userPointHistories.get(1).type()).isEqualTo(TransactionType.USE);
        }
    }
}