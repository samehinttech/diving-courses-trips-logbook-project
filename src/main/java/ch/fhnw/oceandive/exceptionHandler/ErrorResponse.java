package ch.fhnw.oceandive.exceptionHandler;

import java.time.LocalDateTime;

/**
 * ErrorResponse is a class that represents the error response returned by the API.
 * It contains information about the error, such as the status code, message, path, and timestamp.
 */
public class ErrorResponse {

  private final int status;
  private final String message;
  private final String path;
  private final LocalDateTime timestamp;


  public ErrorResponse(int status, String message, String path, LocalDateTime timestamp) {
    this.status = status;
    this.message = message;
    this.path = path;
    this.timestamp = timestamp;
  }
  public int getStatus() {
    return status;
  }
  public String getMessage() {
    return message;
  }
  public String getPath() {
    return path;
  }
  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
