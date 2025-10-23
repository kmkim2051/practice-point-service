package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("포인트 서비스 통합테스트")
public class PointServiceIntegrationTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointServiceImpl(userPointTable, pointHistoryTable);
    }

    @Nested
    @DisplayName("포인트 조회 통합테스트")
    class GetUserPointIntegration {

        @Test
        @DisplayName("사용자의 포인트를 조회할 수 있다")
        void getUserPoint_withRealTable() {
            // given
            long userId = 1L;

            // when
            UserPoint initialPoint = pointService.getUserPoint(userId);

            // then
            assertThat(initialPoint).isNotNull();
            assertThat(initialPoint.id()).isEqualTo(userId);
            assertThat(initialPoint.point()).isEqualTo(0L);
        }

        @Test
        @DisplayName("포인트를 충전한 후 조회하면 충전된 금액이 반영된다")
        void getUserPoint_afterCharge() {
            // given
            long userId = 2L;
            long chargeAmount = 1000L;

            // when
            pointService.chargeUserPoint(userId, chargeAmount);
            UserPoint userPoint = pointService.getUserPoint(userId);

            // then
            assertThat(userPoint.id()).isEqualTo(userId);
            assertThat(userPoint.point()).isEqualTo(chargeAmount);
        }

        @Test
        @DisplayName("여러 번 충전한 후 조회하면 누적된 금액이 조회된다")
        void getUserPoint_afterMultipleCharges() {
            // given
            long userId = 3L;
            long firstCharge = 1000L;
            long secondCharge = 500L;

            // when
            pointService.chargeUserPoint(userId, firstCharge);
            pointService.chargeUserPoint(userId, secondCharge);
            UserPoint userPoint = pointService.getUserPoint(userId);

            // then
            assertThat(userPoint.point()).isEqualTo(firstCharge + secondCharge);
        }
    }

    @Nested
    @DisplayName("포인트 충전 통합테스트")
    class ChargeUserPointIntegration {

        @Test
        @DisplayName("포인트를 충전할 수 있다")
        void chargeUserPoint_withRealTable() {
            // given
            long userId = 10L;
            long chargeAmount = 500L;

            // when
            UserPoint chargedPoint = pointService.chargeUserPoint(userId, chargeAmount);

            // then
            assertThat(chargedPoint).isNotNull();
            assertThat(chargedPoint.id()).isEqualTo(userId);
            assertThat(chargedPoint.point()).isEqualTo(chargeAmount);
        }

        @Test
        @DisplayName("포인트 충전 시 히스토리가 실제로 기록된다")
        void chargeUserPoint_createsHistory() {
            // given
            long userId = 11L;
            long chargeAmount = 1000L;

            // when
            pointService.chargeUserPoint(userId, chargeAmount);

            // then - 히스토리 조회를 통해 검증
            List<PointHistory> histories = pointService.getUserPointHistories(userId);
            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).userId()).isEqualTo(userId);
            assertThat(histories.get(0).amount()).isEqualTo(chargeAmount);
            assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        }

        @Test
        @DisplayName("여러 번 충전하면 포인트가 누적되고 히스토리도 모두 기록된다")
        void chargeUserPoint_multiple() {
            // given
            long userId = 12L;
            long firstCharge = 1000L;
            long secondCharge = 500L;
            long thirdCharge = 2000L;

            // when
            pointService.chargeUserPoint(userId, firstCharge);
            pointService.chargeUserPoint(userId, secondCharge);
            pointService.chargeUserPoint(userId, thirdCharge);

            // then - 포인트 잔액 확인
            UserPoint finalPoint = pointService.getUserPoint(userId);
            assertThat(finalPoint.point()).isEqualTo(firstCharge + secondCharge + thirdCharge);

            // then - 히스토리 확인
            List<PointHistory> histories = pointService.getUserPointHistories(userId);
            assertThat(histories).hasSize(3);
            assertThat(histories.get(0).amount()).isEqualTo(firstCharge);
            assertThat(histories.get(1).amount()).isEqualTo(secondCharge);
            assertThat(histories.get(2).amount()).isEqualTo(thirdCharge);
            assertThat(histories).allMatch(h -> h.type() == TransactionType.CHARGE);
        }

        @Test
        @DisplayName("포인트 충전 후 실제로 Table에 데이터가 저장되어 다시 조회할 수 있다")
        void chargeUserPoint_persistsData() {
            // given
            long userId = 13L;
            long chargeAmount = 300L;

            // when
            UserPoint chargedPoint = pointService.chargeUserPoint(userId, chargeAmount);

            // then - 다시 조회해도 동일한 값
            UserPoint retrievedPoint = pointService.getUserPoint(userId);
            assertThat(retrievedPoint.id()).isEqualTo(chargedPoint.id());
            assertThat(retrievedPoint.point()).isEqualTo(chargedPoint.point());
            assertThat(retrievedPoint.point()).isEqualTo(chargeAmount);
        }
    }

    @Nested
    @DisplayName("포인트 사용 통합테스트")
    class UseUserPointIntegration {

        @Test
        @DisplayName("포인트를 충전 후 사용하면 사용 금액만큼 포인트가 감소한다")
        void usePoint_withRealTable() {
            // given
            long userId = 20L;  // 고유한 userId 사용
            long chargeAmount = 1000L;
            pointService.chargeUserPoint(userId, chargeAmount);
            long useAmount = 500L;

            // when
            UserPoint userPoint = pointService.useUserPoint(userId, useAmount);

            // then
            assertThat(userPoint).isNotNull();
            assertThat(userPoint.id()).isEqualTo(userId);
            assertThat(userPoint.point()).isEqualTo(chargeAmount - useAmount);
        }

        @Test
        @DisplayName("포인트 사용 시 히스토리가 실제로 기록된다")
        void usePoint_createsHistory() {
            // given
            long userId = 21L;
            long chargeAmount = 1000L;
            long useAmount = 300L;
            pointService.chargeUserPoint(userId, chargeAmount);

            // when
            pointService.useUserPoint(userId, useAmount);

            // then - 히스토리 조회를 통해 검증 (충전 1건 + 사용 1건)
            List<PointHistory> histories = pointService.getUserPointHistories(userId);
            assertThat(histories).hasSize(2);

            // 충전 히스토리 검증
            assertThat(histories.get(0).userId()).isEqualTo(userId);
            assertThat(histories.get(0).amount()).isEqualTo(chargeAmount);
            assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);

            // 사용 히스토리 검증
            assertThat(histories.get(1).userId()).isEqualTo(userId);
            assertThat(histories.get(1).amount()).isEqualTo(useAmount);
            assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
        }

        @Test
        @DisplayName("포인트 충전 후 여러번 사용하면 총 사용 금액만큼 포인트가 감소하고, 히스토리도 모두 기록된다")
        void usePoint_multiple() {
            // given
            long userId = 22L;  // 고유한 userId 사용
            long chargeAmount = 1000L;
            pointService.chargeUserPoint(userId, chargeAmount);
            long useAmountFirst = 500L;
            long useAmountSecond = 300L;

            // when
            pointService.useUserPoint(userId, useAmountFirst);
            UserPoint result = pointService.useUserPoint(userId, useAmountSecond);

            // then - 잔액 확인
            assertThat(result.point()).isEqualTo(chargeAmount - (useAmountFirst + useAmountSecond));

            // then - 히스토리 확인 (충전 1건 + 사용 2건)
            List<PointHistory> userPointHistories = pointService.getUserPointHistories(userId);
            assertThat(userPointHistories).hasSize(3);

            assertThat(userPointHistories.get(0).userId()).isEqualTo(userId);
            assertThat(userPointHistories.get(0).amount()).isEqualTo(chargeAmount);
            assertThat(userPointHistories.get(0).type()).isEqualTo(TransactionType.CHARGE);

            assertThat(userPointHistories.get(1).userId()).isEqualTo(userId);
            assertThat(userPointHistories.get(1).amount()).isEqualTo(useAmountFirst);
            assertThat(userPointHistories.get(1).type()).isEqualTo(TransactionType.USE);

            assertThat(userPointHistories.get(2).userId()).isEqualTo(userId);
            assertThat(userPointHistories.get(2).amount()).isEqualTo(useAmountSecond);
            assertThat(userPointHistories.get(2).type()).isEqualTo(TransactionType.USE);
        }

        @Test
        @DisplayName("포인트 사용 후 실제로 Table에 데이터가 저장되어 다시 조회할 수 있다")
        void usePoint_persistsData() {
            // given
            long userId = 23L;
            long chargeAmount = 2000L;
            long useAmount = 700L;
            pointService.chargeUserPoint(userId, chargeAmount);

            // when
            UserPoint usedPoint = pointService.useUserPoint(userId, useAmount);

            // then - 다시 조회해도 동일한 값
            UserPoint retrievedPoint = pointService.getUserPoint(userId);
            assertThat(retrievedPoint.id()).isEqualTo(usedPoint.id());
            assertThat(retrievedPoint.point()).isEqualTo(usedPoint.point());
            assertThat(retrievedPoint.point()).isEqualTo(chargeAmount - useAmount);
        }

        @Test
        @DisplayName("충전 후 전액 사용하면 포인트가 0이 된다")
        void usePoint_allBalance() {
            // given
            long userId = 24L;
            long chargeAmount = 1000L;
            pointService.chargeUserPoint(userId, chargeAmount);

            // when
            UserPoint userPoint = pointService.useUserPoint(userId, chargeAmount);

            // then
            assertThat(userPoint.point()).isEqualTo(0L);

            // 히스토리도 정상 기록
            List<PointHistory> histories = pointService.getUserPointHistories(userId);
            assertThat(histories).hasSize(2);
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회 통합테스트")
    class GetUserPointHistoriesIntegration {

        @Test
        @DisplayName("포인트 내역이 없는 사용자는 빈 리스트를 반환한다")
        void getHistories_emptyForNewUser() {
            // given
            long userId = 30L;

            // when
            List<PointHistory> histories = pointService.getUserPointHistories(userId);

            // then
            assertThat(histories).isNotNull();
            assertThat(histories).isEmpty();
        }

        @Test
        @DisplayName("충전과 사용 내역을 순서대로 조회할 수 있다")
        void getHistories_chargeAndUse() {
            // given
            long userId = 31L;
            long chargeAmount = 1000L;
            long useAmount = 300L;

            // when
            pointService.chargeUserPoint(userId, chargeAmount);
            pointService.useUserPoint(userId, useAmount);
            List<PointHistory> histories = pointService.getUserPointHistories(userId);

            // then
            assertThat(histories).hasSize(2);
            assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
            assertThat(histories.get(0).amount()).isEqualTo(chargeAmount);
            assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
            assertThat(histories.get(1).amount()).isEqualTo(useAmount);
        }

        @Test
        @DisplayName("여러 충전과 사용 내역이 모두 기록되고 조회된다")
        void getHistories_multipleTransactions() {
            // given
            long userId = 32L;
            long charge1 = 1000L;
            long use1 = 300L;
            long charge2 = 500L;
            long use2 = 200L;

            // when - 복잡한 거래 시나리오
            pointService.chargeUserPoint(userId, charge1);
            pointService.useUserPoint(userId, use1);
            pointService.chargeUserPoint(userId, charge2);
            pointService.useUserPoint(userId, use2);

            List<PointHistory> histories = pointService.getUserPointHistories(userId);

            // then
            assertThat(histories).hasSize(4);

            // 순서대로 검증
            assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
            assertThat(histories.get(0).amount()).isEqualTo(charge1);

            assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
            assertThat(histories.get(1).amount()).isEqualTo(use1);

            assertThat(histories.get(2).type()).isEqualTo(TransactionType.CHARGE);
            assertThat(histories.get(2).amount()).isEqualTo(charge2);

            assertThat(histories.get(3).type()).isEqualTo(TransactionType.USE);
            assertThat(histories.get(3).amount()).isEqualTo(use2);
        }

        @Test
        @DisplayName("히스토리 조회 시 각 내역은 고유한 ID를 가진다")
        void getHistories_hasUniqueIds() {
            // given
            long userId = 33L;
            pointService.chargeUserPoint(userId, 1000L);
            pointService.chargeUserPoint(userId, 500L);
            pointService.useUserPoint(userId, 200L);

            // when
            List<PointHistory> histories = pointService.getUserPointHistories(userId);

            // then - 모든 히스토리 ID가 고유하다
            assertThat(histories).hasSize(3);
            assertThat(histories.get(0).id()).isNotEqualTo(histories.get(1).id());
            assertThat(histories.get(1).id()).isNotEqualTo(histories.get(2).id());
            assertThat(histories.get(0).id()).isNotEqualTo(histories.get(2).id());
        }

        @Test
        @DisplayName("다른 사용자의 내역은 조회되지 않는다")
        void getHistories_isolatedByUser() {
            // given
            long userId1 = 34L;
            long userId2 = 35L;

            pointService.chargeUserPoint(userId1, 1000L);
            pointService.chargeUserPoint(userId2, 500L);

            // when
            List<PointHistory> user1Histories = pointService.getUserPointHistories(userId1);
            List<PointHistory> user2Histories = pointService.getUserPointHistories(userId2);

            // then
            assertThat(user1Histories).hasSize(1);
            assertThat(user1Histories.get(0).userId()).isEqualTo(userId1);
            assertThat(user1Histories.get(0).amount()).isEqualTo(1000L);

            assertThat(user2Histories).hasSize(1);
            assertThat(user2Histories.get(0).userId()).isEqualTo(userId2);
            assertThat(user2Histories.get(0).amount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("히스토리는 실제 Table에 영속화되어 여러 번 조회해도 동일한 결과를 반환한다")
        void getHistories_consistent() {
            // given
            long userId = 36L;
            pointService.chargeUserPoint(userId, 1000L);
            pointService.useUserPoint(userId, 300L);

            // when - 여러 번 조회
            List<PointHistory> firstQuery = pointService.getUserPointHistories(userId);
            List<PointHistory> secondQuery = pointService.getUserPointHistories(userId);

            // then - 동일한 결과
            assertThat(firstQuery).hasSize(2);
            assertThat(secondQuery).hasSize(2);

            assertThat(firstQuery.get(0).id()).isEqualTo(secondQuery.get(0).id());
            assertThat(firstQuery.get(0).amount()).isEqualTo(secondQuery.get(0).amount());
            assertThat(firstQuery.get(1).id()).isEqualTo(secondQuery.get(1).id());
            assertThat(firstQuery.get(1).amount()).isEqualTo(secondQuery.get(1).amount());
        }
    }
}
