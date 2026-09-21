package com.midland.saloon.Utils.Exceptions;

import com.midland.saloon.Utils.Responses.ResponseList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseList<Object>> handleBusinessException(
            BusinessException e) {

        return ResponseEntity
                .badRequest()
                .body(new ResponseList<>(e.getMessage()));
    }
}
