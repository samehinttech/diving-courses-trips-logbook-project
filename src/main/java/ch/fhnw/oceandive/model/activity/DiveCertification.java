package ch.fhnw.oceandive.model.activity;

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
   * 
   * @param certification the display name of the certification
   * @return the corresponding DiveCertification enum value
   * @throws IllegalArgumentException if no matching certification is found
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

    throw new IllegalArgumentException("Unknown certification: " + certification);
  }

  /**
   * Safely converts a string to a DiveCertification enum.
   * Returns NON_DIVER if no matching certification is found.
   * 
   * @param certification the display name of the certification
   * @return the corresponding DiveCertification enum value, or NON_DIVER if not found
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
