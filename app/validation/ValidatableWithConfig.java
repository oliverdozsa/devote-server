package validation;

import com.typesafe.config.Config;

public interface ValidatableWithConfig<T> {
    T validate(Config config);
}
