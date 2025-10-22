package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public boolean canUse(long point) {
        return this.point >= point;
    }

    public UserPoint use(long point) {
        if (!canUse(point)) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        return new UserPoint(id, this.point - point, System.currentTimeMillis());
    }
}
