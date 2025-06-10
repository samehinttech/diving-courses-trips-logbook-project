package ch.oceandive.exceptionHandler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.NoHandlerFoundException;

// This handles exceptions for @Controller (web pages)
@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {

  @ExceptionHandler(NoHandlerFoundException.class)
  public String handleNoHandlerFound(NoHandlerFoundException ex, Model model) {
    model.addAttribute("error", "Page not found");
    model.addAttribute("message", ex.getMessage());
    return "error/404";
  }

  @ExceptionHandler(Exception.class)
  public String handleGeneralError(Exception ex, Model model) {
    model.addAttribute("error", "An error occurred");
    model.addAttribute("message", ex.getMessage());
    return "error/500"; // TODO I  need to create this error page
  }
}