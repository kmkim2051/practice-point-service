package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    private static final long MINIMUM_USE_AMOUNT = 100L;
    private static final long MINIMUM_CHARGE_AMOUNT = 100L;
    private static final long MINIMUM_CHARGE_UNIT = 10L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public boolean canUse(long amount) {
        if (!isPositive(amount)) {
            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다.");
        }
        if (amount < MINIMUM_USE_AMOUNT) {
            throw new IllegalArgumentException("최소 사용 금액은 %d원 입니다.".formatted(MINIMUM_USE_AMOUNT));
        }
        return (this.point >= amount);
    }

    public UserPoint use(long amount) {
        if (!canUse(amount)) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        return new UserPoint(id, this.point - amount, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        validateChargeAmount(amount);
        return new UserPoint(id, this.point + amount, System.currentTimeMillis());
    }

    private static boolean isPositive(long point) {
        return point > 0;
    }

    private void validateChargeAmount(long amount) {
        if (!isPositive(amount)) {
            throw new IllegalArgumentException("포인트는 0보다 커야 합니다.");
        }
        if (amount < MINIMUM_CHARGE_AMOUNT) {
            throw new IllegalArgumentException("최소 충전 금액은 %d원 입니다.".formatted(MINIMUM_CHARGE_AMOUNT));
        }
        if (isInvalidUnit(amount)) {
            throw new IllegalArgumentException("포인트는 %d원 단위로만 충전할 수 있습니다.".formatted(MINIMUM_CHARGE_UNIT));
        }
    }

    private static boolean isInvalidUnit(long amount) {
        return amount % MINIMUM_CHARGE_UNIT != 0;
    }
}
