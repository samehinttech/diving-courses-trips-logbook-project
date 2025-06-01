package ch.fhnw.oceandive.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordPatternValidator implements ConstraintValidator<PasswordPattern, String> {

  private static final String PASSWORD_REGEX =
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

  private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
  private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
  private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
  private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
  private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return false;
    }

    if (!PASSWORD_PATTERN.matcher(password).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(buildDetailedErrorMessage(password))
          .addConstraintViolation();
      return false;
    }

    return true;
  }

  private String buildDetailedErrorMessage(String password) {
    StringBuilder message = new StringBuilder("Password must ");
    boolean needsAnd = false;

    if (password.length() < 8) {
      message.append("be at least 8 characters long");
      needsAnd = true;
    }

    if (!UPPERCASE_PATTERN.matcher(password).matches()) {
      if (needsAnd) message.append(" and ");
      message.append("contain at least one uppercase letter");
      needsAnd = true;
    }

    if (!LOWERCASE_PATTERN.matcher(password).matches()) {
      if (needsAnd) message.append(" and ");
      message.append("contain at least one lowercase letter");
      needsAnd = true;
    }

    if (!DIGIT_PATTERN.matcher(password).matches()) {
      if (needsAnd) message.append(" and ");
      message.append("contain at least one digit");
      needsAnd = true;
    }

    if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
      if (needsAnd) message.append(" and ");
      message.append("contain at least one special character");
    }

    return message.toString();
  }
}
