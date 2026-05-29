package org.tamtamcatworks.auction.server.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            NotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleNotFound(
            NotFoundException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(

                        new ApiErrorResponse(

                                404,

                                ex.getMessage(),

                                LocalDateTime.now()
                        )
                );
    }

    @ExceptionHandler(
            ForbiddenException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleForbidden(
            ForbiddenException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(

                        new ApiErrorResponse(

                                403,

                                ex.getMessage(),

                                LocalDateTime.now()
                        )
                );
    }

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleGeneric(
            Exception ex
    ) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(

                        new ApiErrorResponse(

                                500,

                                ex.getMessage(),

                                LocalDateTime.now()
                        )
                );
    }
}