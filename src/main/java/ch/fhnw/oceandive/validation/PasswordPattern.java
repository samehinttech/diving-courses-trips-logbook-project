package ch.fhnw.oceandive.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordPatternValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordPattern {
  String message() default "Password must be at least 8 characters and contain uppercase, lowercase, digit and special character";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}