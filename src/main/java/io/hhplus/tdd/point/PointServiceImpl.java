package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
/*
포인트 서비스 정책
[출금 정책]
 #1. 100원 미만의 포인트를 출금할 수 없다.
[충전 정책]
 #2. 100원 미만의 포인트를 충전할 수 없다.
 #3. 포인트 충전은 10원 단위로 수행한다.
*/

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public List<PointHistory> getUserPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
    /* [충전 정책]
     #2. 100원 미만의 포인트를 충전할 수 없다.
     #3. 포인트 충전은 최소 10원 단위로 수행한다.
    * */
    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        // 존재하지 않는 사용자여도 포인트 충전 가능
        UserPoint userPoint = userPointTable.selectById(userId);
        UserPoint chargedPoint = userPoint.charge(amount);

        UserPoint saved = save(chargedPoint);
        createHistory(saved.id(), amount, TransactionType.CHARGE);
        return saved;
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        UserPoint currentPoint = userPointTable.selectById(userId);
        UserPoint usedPoint = currentPoint.use(amount);

        UserPoint saved = save(usedPoint);
        createHistory(saved.id(), amount, TransactionType.USE);
        return saved;
    }

    private void createHistory(long userId, long amount, TransactionType transactionType) {
        pointHistoryTable.insert(userId, amount, transactionType, System.currentTimeMillis());
    }

    private UserPoint save(UserPoint point) {
        return userPointTable.insertOrUpdate(point.id(), point.point());
    }
}