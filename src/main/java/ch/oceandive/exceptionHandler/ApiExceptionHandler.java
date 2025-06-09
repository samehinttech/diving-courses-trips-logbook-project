package ch.oceandive.exceptionHandler;

import ch.oceandive.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse handleValidation(ValidationException e) {
    return new ApiResponse(false, e.getMessage(), null);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse handleDuplicate(DuplicateResourceException e) {
    return new ApiResponse(false, e.getMessage(), null);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse handleAll(Exception e) {
    return new ApiResponse(false, "Server error: " + e.getMessage(), null);
  }
}
