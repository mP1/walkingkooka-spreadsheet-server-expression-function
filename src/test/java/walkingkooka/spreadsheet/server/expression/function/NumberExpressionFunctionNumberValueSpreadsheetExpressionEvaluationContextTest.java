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

import walkingkooka.spreadsheet.expression.FakeSpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContextTesting;

import java.math.MathContext;

public final class NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContextTest implements SpreadsheetExpressionEvaluationContextTesting<NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext> {

    private final static String CURRENCY_SYMBOL = "AUD";
    private final static char DECIMAL_SEPARATOR = '/';
    private final static String EXPONENT_SYMBOL = "HELLO";
    private final static char GROUPING_SEPARATOR = '/';
    private final static MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private final static char NEGATIVE_SYMBOL = 'N';
    private final static char PERCENTAGE_SYMBOL = 'R';
    private final static char POSITIVE_SYMBOL = 'P';

    @Override
    public void testEvaluateExpressionUnknownFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testEvaluateUnknownFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFunctionUnknownFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testIsPureNullNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testParseFormulaNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext createContext() {
        return NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext.with(
                DECIMAL_SEPARATOR,
                GROUPING_SEPARATOR,
                new FakeSpreadsheetExpressionEvaluationContext() {

                    @Override
                    public String currencySymbol() {
                        return CURRENCY_SYMBOL;
                    }

                    @Override
                    public char decimalSeparator() {
                        return DECIMAL_SEPARATOR;
                    }

                    @Override
                    public String exponentSymbol() {
                        return EXPONENT_SYMBOL;
                    }

                    @Override
                    public char groupingSeparator() {
                        return GROUPING_SEPARATOR;
                    }

                    @Override
                    public MathContext mathContext() {
                        return MATH_CONTEXT;
                    }

                    @Override
                    public char negativeSign() {
                        return NEGATIVE_SYMBOL;
                    }

                    @Override
                    public char percentageSymbol() {
                        return PERCENTAGE_SYMBOL;
                    }

                    @Override
                    public char positiveSign() {
                        return POSITIVE_SYMBOL;
                    }
                }
        );
    }

    @Override
    public String currencySymbol() {
        return CURRENCY_SYMBOL;
    }

    @Override
    public char decimalSeparator() {
        return DECIMAL_SEPARATOR;
    }

    @Override
    public String exponentSymbol() {
        return EXPONENT_SYMBOL;
    }

    @Override
    public char groupingSeparator() {
        return GROUPING_SEPARATOR;
    }

    @Override
    public MathContext mathContext() {
        return MATH_CONTEXT;
    }

    @Override
    public char negativeSign() {
        return NEGATIVE_SYMBOL;
    }

    @Override
    public char percentageSymbol() {
        return PERCENTAGE_SYMBOL;
    }

    @Override
    public char positiveSign() {
        return POSITIVE_SYMBOL;
    }

    @Override
    public Class<NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext> type() {
        return NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext.class;
    }
}
