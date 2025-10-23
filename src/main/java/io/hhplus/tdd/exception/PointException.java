package io.hhplus.tdd.exception;

public abstract class PointException extends RuntimeException {
    protected PointException(String message) {
        super(message);
    }

    /**
     * 포인트 충전 금액이 유효하지 않을 때 발생하는 예외
     * <p>
     * 다음 경우에 발생합니다:
     * <ul>
     *   <li>충전 금액이 최소 금액 미만인 경우</li>
     *   <li>충전 금액이 최소 단위의 배수가 아닌 경우</li>
     * </ul>
     */
    public static class InvalidChargeAmountException extends PointException {
        public InvalidChargeAmountException(String message) {
            super(message);
        }
    }

    /**
     * 포인트 사용 금액이 유효하지 않을 때 발생하는 예외
     * <p>
     * 다음 경우에 발생합니다:
     * <ul>
     *   <li>사용 금액이 0 이하인 경우</li>
     *   <li>사용 금액이 최소 금액 미만인 경우</li>
     * </ul>
     */
    public static class InvalidUseAmountException extends PointException {
        public InvalidUseAmountException(String message) {
            super(message);
        }
    }

    /**
     * 사용자의 포인트 잔액이 부족할 때 발생하는 예외
     * <p>
     * 사용 요청 금액이 현재 보유한 포인트보다 클 때 발생합니다.
     */
    public static class InsufficientPointException extends PointException {
        public InsufficientPointException(String message) {
            super(message);
        }
    }
}
