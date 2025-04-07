package co.com.thechaoscompany.api;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
@Log4j2
public class ControllerAdvice {

    private ResponseEntity<ErrorModel> processException(
            ErrorModel errorModel, Exception exception, HttpStatus status) {

        log.error(errorModel.toString());

        List<StackTraceElement> list = Arrays.stream(exception.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("bancolombia"))
                .toList();
        if (list.isEmpty()) {
            list = Arrays.asList(Arrays.stream(exception.getStackTrace()).limit(5)
                    .toArray(StackTraceElement[]::new));
        }
        list.forEach(log::error);

        return new ResponseEntity<>(errorModel, status);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class, ListenerExecutionFailedException.class})
    public final ResponseEntity<ErrorModel> exception(Exception exception) {
        ErrorModel errorModel = ErrorModel.builder()
                .code("Error-666")
                .exception(exception.getClass().getName())
                .message(exception.getMessage())
                .build();
        return processException(errorModel, exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}