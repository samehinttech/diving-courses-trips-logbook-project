package ch.oceandive.model;

/**
 * Represents certifications associated with diving, indicating the user's skill level
 * or qualifications obtained through specific training programs.
 * Dive certifications are typically required for participating in certain trips
 * or courses and may also define the certification provided upon course completion.
 */
public enum DiveCertification {

  NON("Non Diver"),
  OPEN_WATER("Open Water Diver"),
  ADVANCED_OPEN_WATER("Advanced Open Water Diver"),
  RESCUE_DIVER("Rescue Diver"),
  DIVEMASTER("Divemaster"),
  INSTRUCTOR("Instructor");

  // The display name of the certification
  private final String displayName;
  DiveCertification(String displayName) {
    this.displayName = displayName;
  }


  /**
   * @return and display name of the certification
   */
  public String getDisplayName() {
    return displayName;
  }

  public static DiveCertification fromString(String certification) {
    for (DiveCertification cert : DiveCertification.values()) {
      if (cert.displayName.equalsIgnoreCase(certification)) {
        return cert;
      }
    }
    return null; // Return null if not found
  }

}
