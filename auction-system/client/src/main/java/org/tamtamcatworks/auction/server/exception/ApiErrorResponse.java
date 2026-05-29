package org.tamtamcatworks.auction.server.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(

        int status,

        String message,

        LocalDateTime timestamp
) {
}