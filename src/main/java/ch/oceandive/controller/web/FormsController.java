package ch.oceandive.controller.web;

import ch.oceandive.model.ContactForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.Year;

@Controller
public class FormsController {

  private static final String PAGE_TITLE_CONTACT = "Contact - OceanDive";
  private static final String COMPANY_NAME = "OceanDive";

  // Adds common attributes to the model for all handlers in this controller
  @ModelAttribute
  public void addCommonAttributes(Model model) {
    model.addAttribute("currentYear", Year.now().getValue());
    model.addAttribute("companyName", COMPANY_NAME);
  }

  @GetMapping("/contact")
  public String contact(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_CONTACT);
    model.addAttribute("contactForm", new ContactForm());
    return "contact";
  }

  @PostMapping("/contact")
  public String submitContact(@Valid @ModelAttribute ContactForm contactForm,
      BindingResult bindingResult,
      Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_CONTACT);

    if (bindingResult.hasErrors()) {
      return "contact"; // Return to the contact page with errors
    }

    try {
      // Here we would send an email or save it to the database; however, this is only a placeholder
      processContactForm(contactForm);
      model.addAttribute("successMessage",
          "Thank you for your message! We'll get back to you within 24 hours.");
      model.addAttribute("contactForm", new ContactForm()); // Reset form
    } catch (Exception e) {
      model.addAttribute("errorMessage",
          "There was an error processing your request. Please try again later.");
    }

    return "contact";
  }

  private void processContactForm(ContactForm contactForm) {
    System.out.println("Contact form submitted:");
    System.out.println("Name: " + contactForm.getName());
    System.out.println("Email: " + contactForm.getEmail());
    System.out.println("Subject: " + contactForm.getSubject());
    System.out.println("Message: " + contactForm.getMessage());
  }
}