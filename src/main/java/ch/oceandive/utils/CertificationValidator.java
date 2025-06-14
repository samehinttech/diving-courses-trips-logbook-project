package ch.oceandive.utils;

/**
 * CertificationValidator is a utility class that validates if a user's dive certification meets the requirements for a specific dive activity.
 * It is not used as the booking is not active
 */
public class CertificationValidator {

    private DiveCertification userCertification;
    private DiveCertification requiredCertification;

    private boolean valid = false;


    public CertificationValidator() {
        this.valid = false;
    }

    public CertificationValidator(DiveCertification userCertification, DiveCertification requiredCertification) {
        this.userCertification = userCertification;
        this.requiredCertification = requiredCertification;
        this.valid = false; // Always start as invalid, rules will validate
    }

    public DiveCertification getUserCertification() {
        return userCertification;
    }

    public void setUserCertification(DiveCertification userCertification) {
        this.userCertification = userCertification;
    }

    public DiveCertification getRequiredCertification() {
        return requiredCertification;
    }

    public void setRequiredCertification(DiveCertification requiredCertification) {
        this.requiredCertification = requiredCertification;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationMessage() {
        if (userCertification == null) {
            return "User certification is not specified";
        }
        if (requiredCertification == null) {
            return "Required certification is not specified";
        }

        if (valid) {
            return String.format("User with %s certification can proceed with %s requirement",
                userCertification.getDisplayName(),
                requiredCertification.getDisplayName());
        } else {
            return String.format("User with %s certification cannot proceed - %s certification required",
                userCertification.getDisplayName(),
                requiredCertification.getDisplayName());
        }
    }


    public void reset() {
        this.userCertification = null;
        this.requiredCertification = null;
        this.valid = false;
    }

    public boolean isComplete() {
        return userCertification != null && requiredCertification != null;
    }

    @Override
    public String toString() {
        return String.format("CertificationValidator{user=%s, required=%s, valid=%s}",
            userCertification != null ? userCertification.getDisplayName() : "null",
            requiredCertification != null ? requiredCertification.getDisplayName() : "null",
            valid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CertificationValidator that = (CertificationValidator) obj;
        return valid == that.valid &&
            userCertification == that.userCertification &&
            requiredCertification == that.requiredCertification;
    }

    @Override
    public int hashCode() {
        int result = userCertification != null ? userCertification.hashCode() : 0;
        result = 31 * result + (requiredCertification != null ? requiredCertification.hashCode() : 0);
        result = 31 * result + (valid ? 1 : 0);
        return result;
    }
}