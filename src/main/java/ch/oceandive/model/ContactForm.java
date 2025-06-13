package ch.oceandive.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * This class is not fully implemented yet, only for the UI to display the contact form.
 */
public class ContactForm {

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Please enter a valid email address")
  private String email;

  @Size(max = 20, message = "Phone number must be less than 20 characters")
  private String phone;

  @NotBlank(message = "Subject is required")
  @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
  private String subject;

  @NotBlank(message = "Message is required")
  @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
  private String message;

  // Default constructor
  public ContactForm() {}

  // Constructor with all fields
  public ContactForm(String name, String email, String phone, String subject, String message) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.subject = subject;
    this.message = message;
  }

  // Getters and Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "ContactForm{" +
        "name='" + name + '\'' +
        ", email='" + email + '\'' +
        ", phone='" + phone + '\'' +
        ", subject='" + subject + '\'' +
        ", message='" + message + '\'' +
        '}';
  }
}