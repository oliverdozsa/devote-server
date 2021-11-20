package validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidateWithConfig.List.class)
@Constraint(validatedBy = ValidateWithConfigValidator.class)
public @interface ValidateWithConfig {
    String message() default "error.invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ValidateWithConfig[] value();
    }
}
