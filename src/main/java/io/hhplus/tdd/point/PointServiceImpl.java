package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        UserPoint userPoint = userPointTable.insertOrUpdate(userId, amount);
        createHistory(userPoint.id(), amount, TransactionType.CHARGE);
        return userPoint;
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        UserPoint currentPoint = userPointTable.selectById(userId);
        UserPoint usedPoint = currentPoint.use(amount);

        UserPoint userPoint = userPointTable.insertOrUpdate(usedPoint.id(), usedPoint.point());
        createHistory(userPoint.id(), amount, TransactionType.USE);
        return userPoint;
    }

    private void createHistory(long userId, long amount, TransactionType transactionType) {
        pointHistoryTable.insert(userId, amount, transactionType, System.currentTimeMillis());
    }
}