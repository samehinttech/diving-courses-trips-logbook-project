package ch.oceandive.validation;

import java.util.ArrayList;
import java.util.List;


 // class to hold validation results from certification validation

public class ValidationResult {

  private boolean valid = false;
  private List<String> messages = new ArrayList<>();
  private String validationType; // "COURSE", "TRIP", "CERTIFICATION".
  private Object validatedObject; // The course, trip being validated

  // Default constructor
  public ValidationResult() {}

  // Constructor with type
  public ValidationResult(String validationType) {
    this.validationType = validationType;
  }

 // Parameterized constructor
  public ValidationResult(String validationType, Object validatedObject) {
    this.validationType = validationType;
    this.validatedObject = validatedObject;
  }

  // Getters and Setters
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

  public String getValidationType() {
    return validationType;
  }

  public void setValidationType(String validationType) {
    this.validationType = validationType;
  }

  public Object getValidatedObject() {
    return validatedObject;
  }

  public void setValidatedObject(Object validatedObject) {
    this.validatedObject = validatedObject;
  }

  // Helper methods
  public void addMessage(String message) {
    if (message != null && !message.trim().isEmpty()) {
      this.messages.add(message);
    }
  }

  public void addMessages(List<String> messages) {
    if (messages != null) {
      this.messages.addAll(messages);
    }
  }

  public String getFirstMessage() {
    return messages.isEmpty() ? null : messages.getFirst();
  }

  public String getAllMessages() {
    return String.join("; ", messages);
  }

  public boolean hasMessages() {
    return !messages.isEmpty();
  }

  public void clearMessages() {
    this.messages.clear();
  }

  // Convenience methods for validation
  public ValidationResult withMessage(String message) {
    addMessage(message);
    return this;
  }

  public ValidationResult withValid(boolean valid) {
    setValid(valid);
    return this;
  }

  public ValidationResult withType(String type) {
    setValidationType(type);
    return this;
  }

  // Static method to create common validation results
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

  public static ValidationResult forCourse(Object course) {
    return new ValidationResult("COURSE", course);
  }

  public static ValidationResult forTrip(Object trip) {
    return new ValidationResult("TRIP", trip);
  }

  public static ValidationResult forCertification() {
    return new ValidationResult("CERTIFICATION");
  }

  public static ValidationResult forCertification(Object certification) {
    return new ValidationResult("CERTIFICATION", certification);
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
