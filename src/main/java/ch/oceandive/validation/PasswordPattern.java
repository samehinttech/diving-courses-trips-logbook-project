package ch.oceandive.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordPatternValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordPattern {
  String message() default "Password must be at least 8 characters and contain uppercase, lowercase, digit and special character";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}