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
        return List.of();
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        return null;
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        return null;
    }
}