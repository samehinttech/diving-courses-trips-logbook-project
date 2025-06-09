package ch.oceandive.exceptionHandler;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * {@code GlobalException.java} To handle all exceptions in the application.
 */

@ControllerAdvice
public class GlobalException {

  // To handle ResourceNotFoundException
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  // To handle ValidationException
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
      ValidationException ex, WebRequest request) {
    ValidationErrorResponse errorResponse = new ValidationErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now(),
        ex.getErrors()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // To handle MethodArgumentNotValidException
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, WebRequest request) {
    return getValidationErrorResponseResponseEntity(request, ex.getBindingResult(), ex);
  }

  private ResponseEntity<ValidationErrorResponse> getValidationErrorResponseResponseEntity(
      WebRequest request, BindingResult bindingResult, MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    bindingResult.getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });
    ValidationErrorResponse errorResponse = new ValidationErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Validation failed",
        request.getDescription(false),
        LocalDateTime.now(),
        errors
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // To handle BindException
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ValidationErrorResponse> handleBindException(
      BindException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });
    ValidationErrorResponse errorResponse = new ValidationErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Validation failed",
        request.getDescription(false),
        LocalDateTime.now(),
        errors
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // To handle all other exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(
      Exception ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
