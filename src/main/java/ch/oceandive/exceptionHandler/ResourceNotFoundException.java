package ch.oceandive.exceptionHandler;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L; // for serialization (Note: L is casting for the int type LONG)

  // ResourceNotFoundException with the specified detail message.
  public ResourceNotFoundException(String message) {
    super(message);
  }

  // ResourceNotFoundException with the specified detail message and cause.
  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  // ResourceNotFoundException for a resource of the specified type with the specified ID.
  public static ResourceNotFoundException create(String resourceName, String fieldName,
      Object fieldValue) {
    return new ResourceNotFoundException(
        String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
  }
}
