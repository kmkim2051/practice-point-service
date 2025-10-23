package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointService 동시성 테스트")
public class PointServiceConcurrencyTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointServiceImpl(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("동일 사용자에 대한 동시 충전 요청이 정상적으로 처리된다")
    void concurrentCharge_sameUser() throws InterruptedException {
        // given
        long userId = 1L;
        long chargeAmount = 100L;
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when - 10개 스레드가 동시에 100원씩 충전
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 최종 잔액은 1000원이어야 함
        UserPoint finalPoint = pointService.getUserPoint(userId);
        assertThat(finalPoint.point()).isEqualTo(chargeAmount * threadCount);

        // 히스토리도 10건 기록
        List<PointHistory> histories = pointService.getUserPointHistories(userId);
        assertThat(histories).hasSize(threadCount);
    }

    @Test
    @DisplayName("동일 사용자에 대한 동시 사용 요청이 정상적으로 처리된다")
    void concurrentUse_sameUser() throws InterruptedException {
        // given
        long userId = 2L;
        long initialCharge = 10000L;
        long useAmount = 100L;
        int threadCount = 10;

        // 초기 포인트 충전
        pointService.chargeUserPoint(userId, initialCharge);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when - 10개 스레드가 동시에 100원씩 사용
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.useUserPoint(userId, useAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 최종 잔액은 9000원이어야 함
        UserPoint finalPoint = pointService.getUserPoint(userId);
        assertThat(finalPoint.point()).isEqualTo(initialCharge - (useAmount * threadCount));

        // 히스토리는 11건 (충전 1건 + 사용 10건)
        List<PointHistory> histories = pointService.getUserPointHistories(userId);
        assertThat(histories).hasSize(threadCount + 1);
    }

    @Test
    @DisplayName("동일 사용자에 대한 충전과 사용이 동시에 발생해도 정상 처리된다")
    void concurrentChargeAndUse_sameUser() throws InterruptedException {
        // given
        long userId = 3L;
        long chargeAmount = 500L;
        long useAmount = 200L;
        int chargeThreadCount = 5;
        int useThreadCount = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(chargeThreadCount + useThreadCount);
        CountDownLatch latch = new CountDownLatch(chargeThreadCount + useThreadCount);

        // when - 충전 5회, 사용 3회 동시 실행
        for (int i = 0; i < chargeThreadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 0; i < useThreadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.useUserPoint(userId, useAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 최종 잔액: (500 * 5) - (200 * 3) = 1900원
        UserPoint finalPoint = pointService.getUserPoint(userId);
        assertThat(finalPoint.point()).isEqualTo((chargeAmount * chargeThreadCount) - (useAmount * useThreadCount));

        // 히스토리는 8건
        List<PointHistory> histories = pointService.getUserPointHistories(userId);
        assertThat(histories).hasSize(chargeThreadCount + useThreadCount);
    }

    @Test
    @DisplayName("서로 다른 사용자의 동시 요청은 독립적으로 처리된다")
    void concurrentRequests_differentUsers() throws InterruptedException {
        // given
        long user1 = 10L;
        long user2 = 20L;
        long chargeAmount = 1000L;
        int threadCountPerUser = 5;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCountPerUser * 2);
        CountDownLatch latch = new CountDownLatch(threadCountPerUser * 2);

        // when - user1과 user2가 동시에 각각 5번씩 충전
        for (int i = 0; i < threadCountPerUser; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(user1, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });

            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(user2, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 각 사용자는 5000원씩
        UserPoint user1Point = pointService.getUserPoint(user1);
        UserPoint user2Point = pointService.getUserPoint(user2);

        assertThat(user1Point.point()).isEqualTo(chargeAmount * threadCountPerUser);
        assertThat(user2Point.point()).isEqualTo(chargeAmount * threadCountPerUser);
    }

    @Test
    @DisplayName("동시 요청 시 예외가 발생해도 Lock이 정상 해제된다")
    void concurrentRequests_withException() throws InterruptedException {
        // given
        long userId = 200L;
        long chargeAmount = 1000L;
        long invalidUseAmount = 99L; // 100원 미만 사용 불가
        int threadCount = 5;

        // 초기 충전
        pointService.chargeUserPoint(userId, 5000L);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // when - 정상 충전과 예외 발생 사용 요청 혼합
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        pointService.chargeUserPoint(userId, chargeAmount);
                    } else {
                        pointService.useUserPoint(userId, invalidUseAmount); // 예외 발생
                    }
                } catch (Exception e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then - 예외 발생 후에도 정상 요청 처리 가능
        assertThat(exceptionCount.get()).isGreaterThan(0);

        // 추가 요청이 정상 처리되는지 확인 (Lock이 해제되었다면 정상 실행)
        UserPoint afterPoint = pointService.chargeUserPoint(userId, 1000L);
        assertThat(afterPoint).isNotNull();
    }
}
