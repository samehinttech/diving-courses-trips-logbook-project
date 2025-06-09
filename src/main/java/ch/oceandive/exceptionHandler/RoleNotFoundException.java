package ch.oceandive.exceptionHandler;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a role is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public RoleNotFoundException(String message) {
    super(message);
  }

  public RoleNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}