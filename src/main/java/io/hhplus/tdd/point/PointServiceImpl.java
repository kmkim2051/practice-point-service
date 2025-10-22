package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

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
        pointHistoryTable.insert(userPoint.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        UserPoint currentPoint = userPointTable.selectById(userId);
        UserPoint usedPoint = currentPoint.use(amount);

        UserPoint userPoint = userPointTable.insertOrUpdate(usedPoint.id(), usedPoint.point());
        pointHistoryTable.insert(userPoint.id(), amount, TransactionType.USE, System.currentTimeMillis());
        return userPoint;
    }
}