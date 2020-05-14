package org.orekyuu.spring.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.*;
import javax.validation.constraints.*;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

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

    /**
     * https://beanvalidation.org/2.0/spec/#valueextractordefinition-builtinvalueextractors
     */
    static class DefaultExtractorForm {
        List<@NotEmpty String> stringList;

        Map<@NotEmpty String, @Max(100) @Min(0) Integer> map;

        Optional<@NotEmpty String> optional;

        public DefaultExtractorForm(List<@NotEmpty String> stringList, Map<@NotEmpty String, @Max(100) @Min(0) Integer> map, Optional<@NotEmpty String> optional) {
            this.stringList = stringList;
            this.map = map;
            this.optional = optional;
        }
    }

    @Test
    void extractor() {
        DefaultExtractorForm form = new DefaultExtractorForm(List.of("str", ""), Map.of("key", -1), Optional.of(""));
        var validate = validator.validate(form);
        var assertion = Assertions.assertThat(validate);

        assertion.filteredOn(conditionFactory("stringList")).hasSize(1).first()
                .extracting(ConstraintViolation::getMessage).isEqualTo("must not be empty");

        assertion.filteredOn(conditionFactory("map")).hasSize(1).first()
                .extracting(ConstraintViolation::getMessage).isEqualTo("must be greater than or equal to 0");

        assertion.filteredOn(conditionFactory("optional")).hasSize(1).first()
                .extracting(ConstraintViolation::getMessage).isEqualTo("must not be empty");
    }

    private Predicate<ConstraintViolation<?>> conditionFactory(String fieldName) {
        return it -> StreamSupport.stream(it.getPropertyPath().spliterator(), false)
                .map(Path.Node::getName)
                .anyMatch(fieldName::equals);
    }

    static class NestedForm {
        @NotEmpty
        @Valid
        List<NumberForm> forms;

        public NestedForm(List<NumberForm> forms) {
            this.forms = forms;
        }
    }

    @Test
    void nested() {
        NumberForm numberForm = new NumberForm();
        numberForm.a = -1;
        var result = validator.validate(new NestedForm(List.of(numberForm)));

        Assertions.assertThat(result).filteredOn(conditionFactory("a")).first()
                .extracting(ConstraintViolation::getMessage).isEqualTo("must be greater than or equal to 0");
    }

    // see: https://beanvalidation.org/2.0/spec/#builtinconstraints
    static class Annotations {
        @NotNull // どの型でもOK
        String notNull;

        @AssertTrue // trueであるか
        boolean assertTrue;

        @AssertFalse // falseであるか
        boolean assertFalse;

        @Min(10) // 与えられた数以上であるか BigDecimal/BigInteger/short/int/byte/long
        BigInteger min;
        @Max(100) // 与えられた数以下であるか
        int max;
        @Negative // マイナス数値であるか
        int negative;
        @NegativeOrZero // 0以下であるか
        int negativeOrZero;
        @Positive // プラス数値であるか
        int positive;
        @PositiveOrZero // 0以上であるか
        int positiveOrZero;
        // 与えられた数以上/以下であるか　MinMaxの型に加えてCharSequence
        // inclusiveでその数を含むか default: true
        @DecimalMin("0") @DecimalMax(value = "100", inclusive = false)
        String decimal;
        // 整数部(integer)と小数部(fraction)の最大桁数
        // MinMaxの型に加えてCharSequence
        @Digits(integer = 3, fraction = 0)
        String digits;

        // 長さがmin以上max以下であるか
        // CharSequence/Collection/Map/Array
        @Size(min = 0, max = 128)
        String size;

        // 今の時間より昔の値か
        // 今の時間はClockProviderを使って指定できる
        @Past
        LocalDateTime past;
        // 現在と一致もしくは昔か
        @PastOrPresent
        LocalDateTime pastOrPresent;
        // 未来バージョン
        @Future @FutureOrPresent
        LocalDate future;

        // 正規表現に一致するか
        @Pattern(regexp = "\\d+")
        String pattern;
        // 空文字でないこと
        @NotBlank
        String notBlank;
        // メールアドレスとしてvalidな形式か
        // 正規表現のデフォルトは.*なのでそのまま使うことはなさそう
        @Email(regexp = ".*@.*")
        String email;
        // 空ではないこと
        // CharSequence/Collection/Map/Array
        @NotEmpty
        List<String> notEmpty;

    }
}
