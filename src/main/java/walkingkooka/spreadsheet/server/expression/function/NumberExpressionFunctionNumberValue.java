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

import walkingkooka.Cast;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionPurityContext;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterName;
import walkingkooka.tree.expression.function.string.StringExpressionFunctions;

import java.util.List;
import java.util.Optional;

/**
 * The excel numbervalue fnction which converts text to a {@link ExpressionNumber}. This is achieved by converting
 * the first parameter using a context that uses the decimal-separator and group-separator parameters.
 */
final class NumberExpressionFunctionNumberValue extends NumberExpressionFunction {

    /**
     * Singleton
     */
    final static NumberExpressionFunctionNumberValue INSTANCE = new NumberExpressionFunctionNumberValue();

    private NumberExpressionFunctionNumberValue() {
        super("numberValue");
        this.function = StringExpressionFunctions.value();
    }

    @Override
    public ExpressionNumber apply(final List<Object> parameters,
                                  final SpreadsheetExpressionEvaluationContext context) {
        this.checkParameterCount(parameters);

        return this.apply0(
                parameters.subList(0, 1),
                NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext.with(
                        DECIMAL_SEPARATOR.get(parameters, 1)
                                .orElseGet(
                                        () -> Optional.of(
                                                context.decimalSeparator()
                                        )
                                ).get(),
                        GROUP_SEPARATOR.get(parameters, 2)
                                .orElseGet(
                                        () -> Optional.of(
                                                context.groupingSeparator()
                                        )
                                ).get(),
                context
            )
        );
    }

    private final static ExpressionFunctionParameter<String> TEXT = ExpressionFunctionParameterName.with("text")
            .required(String.class)
            .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);

    private final static ExpressionFunctionParameter<Character> DECIMAL_SEPARATOR = ExpressionFunctionParameterName.with("decimal-separator")
            .optional(Character.class)
            .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);

    private final static ExpressionFunctionParameter<Character> GROUP_SEPARATOR = ExpressionFunctionParameterName.with("group-separator")
            .optional(Character.class)
            .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);

    private ExpressionNumber apply0(final List<Object> parameters,
                                    final SpreadsheetExpressionEvaluationContext context) {
        final ExpressionFunction<ExpressionNumber, SpreadsheetExpressionEvaluationContext> function = this.function;
        return function.apply(
                context.prepareParameters(
                        Cast.to(function),
                        parameters
                ),
                context
        );
    }

    private final ExpressionFunction<ExpressionNumber, SpreadsheetExpressionEvaluationContext> function;

    @Override
    public List<ExpressionFunctionParameter<?>> parameters(final int count) {
        return PARAMETERS;
    }

    private final static List<ExpressionFunctionParameter<?>> PARAMETERS = ExpressionFunctionParameter.list(
            TEXT,
            DECIMAL_SEPARATOR,
            GROUP_SEPARATOR
    );

    @Override
    public boolean isPure(final ExpressionPurityContext context) {
        return true; // need to test parameters.
    }
}
