package ch.oceandive.utils;

/**
 * Interface representing an entity that holds a dive certification.
 * This is implemented by different user types like PremiumUser and GuestUser.
 * to get the dive certification holder name
 */
public interface DiveCertificationHolder {

    DiveCertification getDiveCertification();

    void setDiveCertification(DiveCertification diveCertification);

    String getFirstName();

}