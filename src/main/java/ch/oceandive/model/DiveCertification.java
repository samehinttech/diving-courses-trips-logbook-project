package ch.oceandive.model;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public enum DiveCertification {
    NON_DIVER("Non Diver", "Beginner (No Certification)"),
    OPEN_WATER("Open Water", "Open Water Diver"),
    ADVANCED_OPEN_WATER("Advanced Open Water", "Advanced Open Water Diver"),
    RESCUE_DIVER("Rescue Diver", "Rescue Diver"),
    DIVEMASTER("Divemaster", "Dive Master"),
    INSTRUCTOR("Instructor", "Instructor");

    private final String displayName;
    private final String fullDisplayName;

    DiveCertification(String displayName, String fullDisplayName) {
        this.displayName = displayName;
        this.fullDisplayName = fullDisplayName;
    }

    //Get the user-friendly display name
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the full display name (e.g., "Open Water Diver")
     */
    public String getFullDisplayName() {
        return fullDisplayName;
    }


     //Get the display name in the lowercase
    public String getDisplayNameLowercase() {
        return displayName.toLowerCase();
    }

    /**
     * Get the display name in uppercase
     * @return uppercase display name (e.g., "OPEN WATER")
     */
    public String getDisplayNameUppercase() {
        return displayName.toUpperCase();
    }


    public String getFullDisplayNameLowercase() {
        return fullDisplayName.toLowerCase();
    }


    public String getFullDisplayNameUppercase() {
        return fullDisplayName.toUpperCase();
    }


    public String getEnumNameTitleCase() {
        String formattedName = name().replace("_", " ").toLowerCase(); // Replace underscores and make lowercase
        StringBuilder result = new StringBuilder();
        Matcher matcher = Pattern.compile("\\b[a-z]").matcher(formattedName); // Match first letter of each word
        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group().toUpperCase()); // Capitalize the first letter
        }
        matcher.appendTail(result);
        return result.toString();
    }


    /**
     * Convert string to DiveCertification enum
     * Handles various input formats (enum name, display name, etc.)
     * @param value the string value to convert
     * @return the corresponding DiveCertification enum, or null if not found
     */
    public static DiveCertification fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim().toUpperCase().replace(" ", "_");

        // Try direct enum name match first
        try {
            return DiveCertification.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            // If a direct match fails, try display name matching
            for (DiveCertification cert : values()) {
                if (cert.getDisplayName().equalsIgnoreCase(value) ||
                    cert.getFullDisplayName().equalsIgnoreCase(value)) {
                    return cert;
                }
            }
        }

        return null;
    }


    public static String getAllDisplayNames() {
        return Arrays.stream(values())
            .map(DiveCertification::getDisplayName)
            .collect(java.util.stream.Collectors.joining(", "));
    }

     // Check if this certification is advanced (Rescue Diver or higher)
    public boolean isAdvanced() {
        return this == RESCUE_DIVER || this == DIVEMASTER || this == INSTRUCTOR;
    }

   // Check if this certification is professional (Divemaster or Instructor)
    public boolean isProfessional() {
        return this == DIVEMASTER || this == INSTRUCTOR;
    }

    /**
     * Get the certification level as an integer (for ordering/comparison)
     * @return certification level (0 = NON_DIVER, 5 = INSTRUCTOR)
     */
    public int getLevel() {
        return ordinal();
    }

    @Override
    public String toString() {
        return getFullDisplayName();
    }
}