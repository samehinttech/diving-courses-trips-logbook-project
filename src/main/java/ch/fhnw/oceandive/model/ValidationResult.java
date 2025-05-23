package ch.fhnw.oceandive.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store validation results with detailed error messages.
 * Used by the rules engine to provide more context about validation failures.
 */
public class ValidationResult {
  private boolean valid = false;
  private List<String> messages = new ArrayList<>();

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public List<String> getMessages() {
    return messages;
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  // Returns the first error message, or empty string if no messages

  public String getFirstMessage() {
    return messages.isEmpty() ? "" : messages.get(0);
  }

  // Returns all messages as a combined string

  public String getAllMessages() {
    return String.join("; ", messages);
  }
}