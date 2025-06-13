package ch.oceandive.validation;

import java.util.ArrayList;
import java.util.List;


 // class to hold validation results from certification validation
// Been Used with Drools for validation of courses, trips, certifications for booking, but bookings are not active

public class ValidationResult {

  private boolean valid = false;
  private List<String> messages = new ArrayList<>();
  private String validationType; // "COURSE", "TRIP", "CERTIFICATION".
  private Object validatedObject; // The course, trip being validated


  public ValidationResult() {}


  public ValidationResult(String validationType) {
    this.validationType = validationType;
  }


  public ValidationResult(String validationType, Object validatedObject) {
    this.validationType = validationType;
    this.validatedObject = validatedObject;
  }


  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public List<String> getMessages() {
    return messages;
  }

  public void setMessages(List<String> messages) {
    this.messages = messages != null ? messages : new ArrayList<>();
  }


  public void addMessage(String message) {
    if (message != null && !message.trim().isEmpty()) {
      this.messages.add(message);
    }
  }

  public String getFirstMessage() {
    return messages.isEmpty() ? null : messages.getFirst();
  }

  public String getAllMessages() {
    return String.join("; ", messages);
  }

  public static ValidationResult valid() {
    ValidationResult result = new ValidationResult();
    result.setValid(true);
    return result;
  }

  public static ValidationResult valid(String message) {
    ValidationResult result = new ValidationResult();
    result.setValid(true);
    result.addMessage(message);
    return result;
  }

  public static ValidationResult invalid(String message) {
    ValidationResult result = new ValidationResult();
    result.setValid(false);
    result.addMessage(message);
    return result;
  }

  @Override
  public String toString() {
    return "ValidationResult{" +
        "valid=" + valid +
        ", messages=" + messages +
        ", validationType='" + validationType + '\'' +
        ", validatedObject=" + validatedObject +
        '}';
  }
}
