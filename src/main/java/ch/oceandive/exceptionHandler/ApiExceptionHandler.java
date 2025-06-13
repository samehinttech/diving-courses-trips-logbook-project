package ch.oceandive.exceptionHandler;

import ch.oceandive.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Response handleValidation(ValidationException e) {
    return new Response(false, e.getMessage(), null);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Response handleDuplicate(DuplicateResourceException e) {
    return new Response(false, e.getMessage(), null);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Response handleAll(Exception e) {
    return new Response(false, "Server error: " + e.getMessage(), null);
  }
}