package ch.fhnw.oceandive.model;

public enum DiveCertification {
  NON_DIVER("Non Diver"),
  OPEN_WATER("Open Water Diver"),
  ADVANCED_OPEN_WATER("Advanced Open Water Diver"),
  RESCUE_DIVER("Rescue Diver"),
  DIVEMASTER("Divemaster"),
  INSTRUCTOR("Instructor");

  private final String displayName;

  DiveCertification(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  /**
   * Converts a string to a DiveCertification enum.
   */
  public static DiveCertification fromString(String certification) {
    if (certification == null || certification.trim().isEmpty()) {
      throw new IllegalArgumentException("Certification cannot be null or empty");
    }

    for (DiveCertification cert : DiveCertification.values()) {
      if (cert.displayName.equalsIgnoreCase(certification)) {
        return cert;
      }
    }

    throw new IllegalArgumentException(certification + "is unknown certification");
  }

  /**
   * Converts a string to a DiveCertification enum.
   */
  public static DiveCertification fromStringOrDefault(String certification) {
    if (certification == null || certification.trim().isEmpty()) {
      return NON_DIVER;
    }
    for (DiveCertification cert : DiveCertification.values()) {
      if (cert.displayName.equalsIgnoreCase(certification)) {
        return cert;
      }
    }
    return NON_DIVER;
  }
}
