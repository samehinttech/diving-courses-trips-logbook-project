package ch.fhnw.oceandive.model;

/**
 * Interface for entities that hold a dive certification.
 * This interface is used to create generic booking methods in BookingService.
 */
public interface DiveCertificationHolder {
    DiveCertification getDiveCertification(); // Get the dive certification level
    String getFirstName(); // Get the first name of the holder
}