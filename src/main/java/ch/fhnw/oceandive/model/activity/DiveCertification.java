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

  // Method to convert a string to a DiveCertification enum.
  // TODO : to be check later if it will be used
  public static DiveCertification fromString(String certification) {
    for (DiveCertification cert : DiveCertification.values()) {
      if (cert.displayName.equalsIgnoreCase(certification)) {
        return cert;
      }
    }
    return null; // or throw an exception if preferred
  }
}
