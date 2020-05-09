package org.orekyuu.assertj;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class AssertJSandbox {

    @Test
    void isEqual() {
        Assertions.assertThat("test")
                .as("テストの説明").describedAs("もしくはdescribedAsでも同じ")
                .withFailMessage("テストが落ちたときのメッセージ").overridingErrorMessage("もしくはoverridingErrorMessageでも同じ")
                .isEqualTo("test");
    }

    @Test
    void exception() {
        Assertions.assertThatThrownBy(() -> {
            throw new RuntimeException("test");
        })
                .as("assertThatThrownByで例外を投げるかの検査ができる")
                .isExactlyInstanceOf(RuntimeException.class)// 完全に型が一致するか
                .isInstanceOf(RuntimeException.class); // RuntimeExceptionを継承した型であるか
    }

    @Test
    void testOptional() {
        Optional<String> optional = Optional.of("test optional");
        OptionalAssert<String> optionalAssert = Assertions.assertThat(optional);

        optionalAssert.as("値が入っているか").isPresent();
        optionalAssert
                .map(String::length) // mapで値の変換ができる
                .hasValueSatisfying(value -> { // 値をassertしたい場合はhasValueSatisfyingを使う
                    Assertions.assertThat(value).isGreaterThan(5);
                });

        Assertions.assertThat(Optional.empty())
                .as("値が空であるか")
                .isEmpty().isNotPresent();
    }
}
