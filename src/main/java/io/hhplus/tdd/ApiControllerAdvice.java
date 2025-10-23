package io.hhplus.tdd;

import io.hhplus.tdd.exception.PointException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = PointException.InvalidChargeAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChargeAmountException(PointException.InvalidChargeAmountException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = PointException.InvalidUseAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUseAmountException(PointException.InvalidUseAmountException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = PointException.InsufficientPointException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPointException(PointException.InsufficientPointException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }
}
