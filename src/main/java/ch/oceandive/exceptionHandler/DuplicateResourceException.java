package ch.oceandive.exceptionHandler;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a duplicate resource is encountered.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public DuplicateResourceException(String message) {
    super(message);
  }

  public DuplicateResourceException(String message, Throwable cause) {
    super(message, cause);
  }
}