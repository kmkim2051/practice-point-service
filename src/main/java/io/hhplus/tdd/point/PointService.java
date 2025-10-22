package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
    /**
     * 특정 유저의 포인트를 조회합니다.
     * @param userId 유저 아이디
     * @return 특정 유저의 포인트 정보
     * */
    UserPoint getUserPoint(long userId);

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     * @param userId 유저 아이디
     * @return 특정 유저의 포인트 충전/이용 내역
     * */
    List<PointHistory> getUserPointHistories(long userId);

    /**
     * 특정 유저의 포인트를 충전합니다.
     * @param userId 유저 아이디
     * @param amount 충전할 포인트 금액
     * @return 충전 후 유저의 포인트 정보
     * */
    UserPoint chargeUserPoint(long userId, long amount);

    /**
     * 특정 유저의 포인트를 사용합니다.
     * @param userId 유저 아이디
     * @param amount 사용할 포인트 금액
     * @return 사용 후 유저의 포인트 정보
     * */
    UserPoint useUserPoint(long userId, long amount);
}
