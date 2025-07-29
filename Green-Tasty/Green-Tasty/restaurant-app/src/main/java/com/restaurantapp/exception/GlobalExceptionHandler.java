package com.restaurantapp.exception;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleSecurityException(Exception exception, WebRequest request) {

        LoggerFactory.getLogger(GlobalExceptionHandler.class).error(Arrays.toString(exception.getStackTrace()));
        exception.printStackTrace();

        if (exception instanceof LocationNotFoundException
                || (exception instanceof TableNotAvailableException tableNotAvailableException && tableNotAvailableException.getHttpMethod().equalsIgnoreCase("GET"))
                || exception instanceof DishNotFoundException
                || exception instanceof WaiterNotFoundException
                || exception instanceof FeedbackNotFoundException
                || exception instanceof UserDoesNotExistsException
                || exception instanceof ReservationNotFoundException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(404),
                    request
            );

        }

        if (exception instanceof IllegalArgumentException
                || (exception instanceof IllegalStateException && !exception.getMessage().contains("currently 'on stop'"))
                || exception instanceof ValidationException
                || exception instanceof PasswordMismatchException
                || exception instanceof ReservationBookingTimeException
                || exception instanceof TimeSlotNotFoundException
                || exception instanceof PreOrderStateChangeException
                || exception instanceof AdminCheckException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(400),
                    request
            );
        }

        if (exception instanceof UnauthorizedException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(401),
                    request
            );
        }

        if (exception instanceof ReservationCancellationOrModificationException
                || exception instanceof WaiterNotAuthorizedException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(403),
                    request
            );
        }

        if (exception instanceof ClientTypeNotFoundException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(405),
                    request
            );
        }

        if (exception instanceof UserAlreadyExistsException
                || exception instanceof ReservationAlreadyCancelledException
                || (exception instanceof IllegalStateException illegalStateException && exception.getMessage().contains("currently 'on stop'"))
                || exception instanceof FeedbackAlreadyExistException
                || (exception instanceof TableNotAvailableException tableNotAvailableException && tableNotAvailableException.getHttpMethod().equalsIgnoreCase("PATCH"))
                || exception instanceof TableAlreadyReservedException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(409),
                    request
            );

        }

        if (exception instanceof PreOrderDishQuantityLimitException) {
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(422),
                    request
            );

        }

        if(exception instanceof HttpMessageNotReadableException){
            return handleExceptionInternal(exception, Map.of("message", exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatusCode.valueOf(400),
                    request
            );
        }

        return handleExceptionInternal(exception, Map.of("message", "Unknown internal server error"),
                new HttpHeaders(),
                HttpStatusCode.valueOf(500),
                request
        );
    }
}