package validation;

import com.typesafe.config.Config;
import play.data.validation.Constraints;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;

public class ValidateWithConfigValidator implements
        Constraints.PlayConstraintValidator<ValidateWithConfig, ValidatableWithConfig<?>> {
    private final Config config;

    @Inject
    public ValidateWithConfigValidator(Config config) {
        this.config = config;
    }

    @Override
    public boolean isValid(ValidatableWithConfig<?> value, ConstraintValidatorContext context) {
        return reportValidationStatus(value.validate(config), context);
    }

    @Override
    public void initialize(ValidateWithConfig constraintAnnotation) {
    }
}
