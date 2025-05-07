package ch.fhnw.oceandive.exceptionHandler;
import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation errors occur.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L; // for serialization (Note: L is casting for the int

  private final Map<String, String> errors;

  /**
   * ValidationException with the specified detail message.
   */
  public ValidationException(String message) {
    super(message);
    this.errors = new HashMap<>();
  }

  /**
   * New ValidationException with the specified detail message and errors.
   */
  public ValidationException(String message, Map<String, String> errors) {
    super(message);
    this.errors = errors;
  }

  /**
   * ValidationException from a BindingResult.
   * @param bindingResult the binding result containing validation errors
   */
  public static ValidationException fromBindingResult(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : bindingResult.getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return new ValidationException("Validation failed", errors);
  }

  public Map<String, String> getErrors() {
    return errors;
  }
}

