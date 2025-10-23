package io.hhplus.tdd;

import io.hhplus.tdd.exception.PointException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 전역 예외 처리를 담당하는 컨트롤러 어드바이스 클래스.
 * 애플리케이션에서 발생하는 다양한 예외를 잡아 적절한 HTTP 응답으로 변환합니다.
 */
@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * 포인트 충전 금액이 유효하지 않을 때 발생하는 예외를 처리합니다.
     *
     * @param e 유효하지 않은 충전 금액 예외
     * @return 400 Bad Request 상태 코드와 에러 메시지를 포함한 응답
     */
    @ExceptionHandler(value = PointException.InvalidChargeAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChargeAmountException(PointException.InvalidChargeAmountException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    /**
     * 포인트 사용 금액이 유효하지 않을 때 발생하는 예외를 처리합니다.
     *
     * @param e 유효하지 않은 사용 금액 예외
     * @return 400 Bad Request 상태 코드와 에러 메시지를 포함한 응답
     */
    @ExceptionHandler(value = PointException.InvalidUseAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUseAmountException(PointException.InvalidUseAmountException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    /**
     * 포인트 잔액이 부족할 때 발생하는 예외를 처리합니다.
     *
     * @param e 포인트 부족 예외
     * @return 400 Bad Request 상태 코드와 에러 메시지를 포함한 응답
     */
    @ExceptionHandler(value = PointException.InsufficientPointException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPointException(PointException.InsufficientPointException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    /**
     * 처리되지 않은 모든 예외를 포착하여 일반적인 에러 응답을 반환합니다.
     *
     * @param e 발생한 예외
     * @return 500 Internal Server Error 상태 코드와 기본 에러 메시지를 포함한 응답
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }
}
