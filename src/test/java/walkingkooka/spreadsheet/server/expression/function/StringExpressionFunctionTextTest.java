/*
 * Copyright 2022 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.expression.function;

import org.junit.jupiter.api.Test;
import walkingkooka.Either;
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.Converter;
import walkingkooka.convert.Converters;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.expression.FakeSpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;

import java.math.MathContext;
import java.util.Locale;

public final class StringExpressionFunctionTextTest extends StringExpressionFunctionTestCase<StringExpressionFunctionText> {

    @Test
    public void testCharacter() {
        this.textAndCheck(
                'A'
        );
    }

    @Test
    public void testText() {
        this.textAndCheck(
                "Hello"
        );
    }

    private void textAndCheck(final Object value) {
        this.textAndCheck(
                value,
                "Ignored!",
                value.toString()
        );
    }

    @Test
    public void testNumber() {
        this.textAndCheck(
                EXPRESSION_NUMBER_KIND.create(123.50),
                "$0000.0000$",
                "!0123.5000!"
        );
    }

    private void textAndCheck(final Object value,
                              final String pattern,
                              final String expected) {
        this.applyAndCheck2(
                Lists.of(value, pattern),
                expected
        );
    }


    @Override
    public StringExpressionFunctionText createBiFunction() {
        return StringExpressionFunctionText.INSTANCE;
    }

    @Override
    public int minimumParameterCount() {
        return 2;
    }

    @Override
    public SpreadsheetExpressionEvaluationContext createContext(final ExpressionNumberKind kind) {
        return new FakeSpreadsheetExpressionEvaluationContext() {

            @Override
            public ExpressionNumberKind expressionNumberKind() {
                return kind;
            }

            @Override
            public <T> T prepareParameter(final ExpressionFunctionParameter<T> parameter,
                                          final Object value) {
                return parameter.convertOrFail(value, this);
            }

            @Override
            public <TT> Either<TT, String> convert(final Object value,
                                                   final Class<TT> target) {
                return this.converter()
                        .convert(
                                value,
                                target,
                                this
                        );
            }

            @Override
            public Converter<SpreadsheetConverterContext> converter() {
                return Converters.collection(
                        Lists.of(
                                Converters.simple(),
                                Converters.object(),
                                Converters.localDateLocalDateTime(),
                                Converters.localTimeLocalDateTime(),
                                ExpressionNumber.fromConverter(
                                        Converters.numberNumber()
                                )
                        )
                );
            }

            @Override
            public Locale locale() {
                return Locale.forLanguageTag("DE");
            }

            @Override
            public MathContext mathContext() {
                return MathContext.DECIMAL128;
            }

            @Override
            public String currencySymbol() {
                return "!";
            }

            @Override
            public char decimalSeparator() {
                return '.';
            }

            @Override
            public char groupingSeparator() {
                return ',';
            }

            @Override
            public char negativeSign() {
                return '-';
            }

            @Override
            public char percentageSymbol() {
                return '%';
            }

            @Override
            public char positiveSign() {
                return '+';
            }
        };
    }

    @Override
    public Class<StringExpressionFunctionText> type() {
        return StringExpressionFunctionText.class;
    }
}
