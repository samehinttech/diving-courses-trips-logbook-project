package ch.oceandive.model;

import ch.oceandive.utils.DiveCertificationHolder;
import ch.oceandive.utils.DiveCertification;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;


/**
 * Entity representing a customer who does not have an account in the system.
 * Customers can still book courses and trips.
 */
@Entity
@Table(name = "guest_users", indexes = {
    @Index(name = "idx_guest_user_email", columnList = "email"),
    @Index(name = "idx_guest_user_mobile", columnList = "mobile")
})
public class GuestUser extends BaseUser implements DiveCertificationHolder {

    @Enumerated(EnumType.STRING)
    private DiveCertification diveCertification;

    // Default constructor
    public GuestUser() {
    }

    // Parameterized constructor
    public GuestUser(String firstName, String lastName, String email, String mobile,
                   DiveCertification diveCertification, String role) {
        super(firstName, lastName, email, mobile, role);
        this.diveCertification = diveCertification;
    }

    public DiveCertification getDiveCertification() {
        return diveCertification;
    }

    public void setDiveCertification(DiveCertification diveCertification) {
        this.diveCertification = diveCertification;
    }

}
