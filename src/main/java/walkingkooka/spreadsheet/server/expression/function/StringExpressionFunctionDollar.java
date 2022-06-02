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

import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.convert.SpreadsheetConverters;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.ExpressionFunctionKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterName;

import java.util.List;
import java.util.Set;

/**
 * The excel dollar function.
 */
final class StringExpressionFunctionDollar extends StringExpressionFunction {

    /**
     * Singleton
     */
    final static StringExpressionFunctionDollar INSTANCE = new StringExpressionFunctionDollar();

    private StringExpressionFunctionDollar() {
        super("dollar");
    }

    @Override
    public Set<ExpressionFunctionKind> kinds() {
        return Sets.of(
                ExpressionFunctionKind.EVALUATE_PARAMETERS,
                ExpressionFunctionKind.RESOLVE_REFERENCES
        );
    }

    private final static ExpressionFunctionParameter<ExpressionNumber> NUMBER = ExpressionFunctionParameterName.NUMBER
            .required(ExpressionNumber.class);

    private final static ExpressionFunctionParameter<ExpressionNumber> DECIMALS = ExpressionFunctionParameterName.with("decimals")
            .optional(ExpressionNumber.class);

    @Override
    public List<ExpressionFunctionParameter<?>> parameters() {
        return PARAMETERS;
    }

    private final static List<ExpressionFunctionParameter<?>> PARAMETERS = ExpressionFunctionParameter.list(
            NUMBER,
            DECIMALS
    );

    @Override
    public String apply(final List<Object> parameters,
                        final SpreadsheetExpressionEvaluationContext context) {
        this.checkParameterCount(parameters);

        ExpressionNumber value = context.prepareParameter(
                NUMBER,
                NUMBER.getOrFail(parameters, 0)
        );

        final int decimals = context.prepareParameter(
                DECIMALS,
                DECIMALS.get(parameters, 1)
                        .orElse(TWO)
        ).intValueExact();

        final String pattern;
        if (decimals >= 0) {
            if (0 == decimals) {
                pattern = "$#,##0";
            } else {
                pattern = "$#,##0." + CharSequences.repeating('0', decimals);
            }
        } else {
            pattern = "$#,##0";

            // do some rounding...
            value = context.expressionNumberKind()
                    .create(
                            value.bigDecimal()
                                    .setScale(
                                            decimals,
                                            context.mathContext().getRoundingMode()
                                    ).stripTrailingZeros()
                    );
        }

        return SpreadsheetConverters.formatPattern(pattern)
                .convertOrFail(
                        value,
                        String.class,
                        context
                );
    }

    private final static ExpressionNumber TWO = ExpressionNumberKind.DEFAULT.create(2);
}
