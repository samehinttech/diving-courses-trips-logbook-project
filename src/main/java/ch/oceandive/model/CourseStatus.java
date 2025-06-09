package ch.oceandive.model;

/**
 * Enum for course status management
 */
public enum CourseStatus {
  DRAFT("Draft"),
  PUBLISHED("Published"),
  ARCHIVED("Archived");

  private final String displayName;

  CourseStatus(String displayName) {
    this.displayName = displayName;
  }

  // Get a user-friendly display name for the status
  public String getDisplayName() {
    return displayName;
  }

  // the same display name in the lowercase
  public String getDisplayNameLowercase() {
    return displayName.toLowerCase();
  }

 // The same display name in uppercase
  public String getDisplayNameUppercase() {
    return displayName.toUpperCase();
  }
// Check if it is published to be visible to the public
  public boolean isPublicVisible() {
    return this == PUBLISHED;
  }

  // Check if the course is in a state that allows editing
  public boolean isEditable() {
    return this != ARCHIVED;
  }
  // Convert a string from CourseStatus enum
  public static CourseStatus fromString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    String normalizedValue = value.trim().toUpperCase();
    try {
      return CourseStatus.valueOf(normalizedValue);
    } catch (IllegalArgumentException e) {
      // Try display name matching
      for (CourseStatus status : values()) {
        if (status.getDisplayName().equalsIgnoreCase(value)) {
          return status;
        }
      }
    }

    return null;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }
}