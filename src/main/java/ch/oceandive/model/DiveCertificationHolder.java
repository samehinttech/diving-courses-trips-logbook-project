package ch.oceandive.model;

/**
 * Interface representing an entity that holds a dive certification.
 * This is implemented by different user types like PremiumUser and GuestUser.
 */
public interface DiveCertificationHolder {

    DiveCertification getDiveCertification();

    void setDiveCertification(DiveCertification diveCertification);

    String getFirstName();

}