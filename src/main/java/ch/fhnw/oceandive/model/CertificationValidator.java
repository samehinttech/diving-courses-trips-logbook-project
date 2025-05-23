package ch.fhnw.oceandive.model;

public class CertificationValidator {

    private DiveCertification userCertification;
    private DiveCertification requiredCertification;
    private boolean valid = false;
    
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
}
  

