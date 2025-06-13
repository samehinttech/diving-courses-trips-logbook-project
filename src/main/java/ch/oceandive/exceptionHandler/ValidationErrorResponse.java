package ch.oceandive.exceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
  private final Map<String, String> errors;


  public ValidationErrorResponse(int status, String message, String path,
      LocalDateTime timestamp, Map<String, String> errors) {
    super(status, message, path, timestamp);
    this.errors = errors;
  }
  public Map<String, String> getErrors() {
    return errors;
  }
}
