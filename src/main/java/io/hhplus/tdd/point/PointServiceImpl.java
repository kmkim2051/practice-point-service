package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    private final ReentrantLock tableAccessLock = new ReentrantLock(true);

    /**
     * 사용자별 Lock 객체를 반환합니다.
     * Fair Lock을 사용하여 요청 순서를 보장합니다.
     */
    private ReentrantLock getUserLock(long userId) {
        boolean isFair = true;
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock(isFair));
    }

    @Override
    public UserPoint getUserPoint(long userId) {
        tableAccessLock.lock();
        try {
            return userPointTable.selectById(userId);
        } finally {
            tableAccessLock.unlock();
        }
    }

    @Override
    public List<PointHistory> getUserPointHistories(long userId) {
        tableAccessLock.lock();
        try {
            return pointHistoryTable.selectAllByUserId(userId);
        } finally {
            tableAccessLock.unlock();
        }
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        ReentrantLock lock = getUserLock(userId);
        lock.lock();
        try {
            tableAccessLock.lock();
            try {
                // 존재하지 않는 사용자여도 포인트 충전 가능
                UserPoint userPoint = userPointTable.selectById(userId);
                UserPoint chargedPoint = userPoint.charge(amount);

                UserPoint saved = save(chargedPoint);
                createHistory(saved.id(), amount, TransactionType.CHARGE);
                return saved;
            } finally {
                tableAccessLock.unlock();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        ReentrantLock lock = getUserLock(userId);
        lock.lock();
        try {
            tableAccessLock.lock();
            try {
                UserPoint currentPoint = userPointTable.selectById(userId);
                UserPoint usedPoint = currentPoint.use(amount);

                UserPoint saved = save(usedPoint);
                createHistory(saved.id(), amount, TransactionType.USE);
                return saved;
            } finally {
                tableAccessLock.unlock();
            }
        } finally {
            lock.unlock();
        }
    }

    private void createHistory(long userId, long amount, TransactionType transactionType) {
        pointHistoryTable.insert(userId, amount, transactionType, System.currentTimeMillis());
    }

    private UserPoint save(UserPoint point) {
        return userPointTable.insertOrUpdate(point.id(), point.point());
    }
}