package org.orekyuu.spring.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class ValidationSample {
    Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    static class NotNullForm {
        @NotNull
        String text;
    }

    @Test
    void notNull() {
        Set<ConstraintViolation<NotNullForm>> validate = validator.validate(new NotNullForm());

        Assertions.assertThat(validate).hasSize(1)
                .first().extracting(ConstraintViolation::getMessage).asString()
                .isEqualTo("must not be null");
    }

    static class NumberForm {
        @Max(100)
        @Min(0)
        long a;
    }

    @Test
    void numberForm() {
        {
            NumberForm form = new NumberForm();
            form.a = 1000;
            Set<ConstraintViolation<NumberForm>> validate = validator.validate(form);
            Assertions.assertThat(validate).hasSize(1)
                    .first().extracting(ConstraintViolation::getMessage).asString()
                    .isEqualTo("must be less than or equal to 100");
        }
        {
            NumberForm form = new NumberForm();
            form.a = -100;
            Set<ConstraintViolation<NumberForm>> validate = validator.validate(form);
            Assertions.assertThat(validate).hasSize(1)
                    .first().extracting(ConstraintViolation::getMessage).asString()
                    .isEqualTo("must be greater than or equal to 0");
        }
    }
}
