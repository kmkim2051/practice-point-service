package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService {
    @Override
    public UserPoint getUserPoint(long userId) {
        return null;
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