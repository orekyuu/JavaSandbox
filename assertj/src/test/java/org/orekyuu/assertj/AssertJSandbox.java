package org.orekyuu.assertj;

import org.assertj.core.api.*;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
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

        // ↑をもう少し手軽に書くならgetを使って以下のように書ける
        optionalAssert
                .map(String::length)
                // 引数を渡さなければObjectAssertionsが変えるがInstanceOfAssertFactoriesの定数を渡すと型を変えられる
                .get(Assertions.as(InstanceOfAssertFactories.INTEGER))
                .isGreaterThan(5);

        Assertions.assertThat(Optional.empty())
                .as("値が空であるか")
                .isEmpty().isNotPresent();
    }

    @Test
    void listAssertions() {
        List<String> list = List.of("aa", "bb", "cccc");
        ListAssert<String> assertion = Assertions.assertThat(list);

        // 引数のものすべてを含むか(順序を考慮しない
        assertion.contains("cccc", "aa");
        // ↑のコレクション版
        assertion.containsAll(List.of("aa", "bb"));

        // 与えられた要素のうち1つでも含むか
        assertion.containsAnyOf("aa", "not contain");
        // ↑のコレクション版
        assertion.containsAnyElementsOf(List.of("aa", "not contain"));

        // 順序を含む要素の完全一致
        assertion.containsExactly("aa", "bb", "cccc");
        // ↑のコレクション版
        assertion.containsExactlyElementsOf(List.of("aa", "bb", "cccc"));

        // 順序を考慮しない要素の完全一致
        assertion.containsExactlyInAnyOrder("bb", "aa", "cccc");
        // ↑のコレクション版
        assertion.containsExactlyInAnyOrderElementsOf(List.of("bb", "aa", "cccc"));

        // 要素が一回だけ含まれているか
        assertion.containsOnlyOnce("aa", "bb");

        // 与えられた要素間に余分なものが含まれてない かつ、同じ並びのものが一回でも現れる
        assertion.containsSequence("bb", "cccc");
        // ↑と同じだが、要素間に余分なものがあっても良い
        assertion.containsSubsequence("aa", "cccc");

        // 要素のn番目のassertionを返す。第2引数にInstanceOfAssertFactoryを渡すと型変換できる。デフォルトはObjectAssert
        assertion.element(0, Assertions.as(InstanceOfAssertFactories.STRING)).isEqualTo("aa");
        // もしくはfirstとかlastが使える
        assertion.first(Assertions.as(InstanceOfAssertFactories.STRING)).isEqualTo("aa");
        assertion.last(Assertions.as(InstanceOfAssertFactories.STRING)).isEqualTo("cccc");

        // hasSizeでサイズチェック betweenやGreaterThan系もある
        assertion.hasSize(3);
    }

    @Test
    void mapAssertion() {
        Map<String, Class<?>> map = Map.of("string", String.class, "int", Integer.class, "boolean", Boolean.class);
        MapAssert<String, Class<?>> assertion = Assertions.assertThat(map);
        // 引数のすべてのキーを含むか
        assertion.containsKeys("string", "boolean");
        // 引数のすべての値を含むか
        assertion.containsValues(String.class, Integer.class);

        // key-valueペアの組み合わせを含むか
        assertion.containsEntry("string", String.class);

        // サイズチェック
        assertion.hasSize(3);

        // サイズチェックのassertionを作ってからreturnToMapでmapのassertionに戻す
        assertion
                .size().isEqualTo(3)
                .returnToMap().doesNotContainKey("hoge");

        // 特定のキーを使って取り出した値のassertion
        // 第2引数にInstanceOfAssertFactoryを渡すと型変換できる。
        assertion
                .extractingByKey("string", Assertions.as(InstanceOfAssertFactories.CLASS))
                .isEqualTo(String.class);
    }

    @Test
    void urlAssertion() throws MalformedURLException {
        AbstractUrlAssert<?> assertion = Assertions.assertThat(new URL("https://twitter.com/orekyuu?param1=a&param2=hoge#fragment"));

        // パスのチェック
        assertion.hasPath("/orekyuu");
        // ホスト名チェック
        assertion.hasHost("twitter.com");
        // クエリパラメータ
        assertion.hasParameter("param2", "hoge").hasParameter("param1", "a");
        // プロトコル
        assertion.hasProtocol("https");
        // フラグメントのチェック
        assertion.hasAnchor("fragment");
    }
}
