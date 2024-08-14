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
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.ExpressionPurityContext;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterName;

import java.util.List;
import java.util.Optional;

/**
 * A lambda function.
 */
final class LambdaExpressionFunction implements ExpressionFunction<ExpressionFunction<?, SpreadsheetExpressionEvaluationContext>, SpreadsheetExpressionEvaluationContext> {

    /**
     * Singleton
     */
    final static LambdaExpressionFunction INSTANCE = new LambdaExpressionFunction();

    private LambdaExpressionFunction() {
        super();
    }

    @Override
    public Optional<ExpressionFunctionName> name() {
        return NAME;
    }

    private final static Optional<ExpressionFunctionName> NAME = Optional.of(
            ExpressionFunctionName.with("lambda")
    );

    @Override
    public Class<ExpressionFunction<?, SpreadsheetExpressionEvaluationContext>> returnType() {
        return Cast.to(LambdaExpressionFunction.class);
    }

    @Override
    public boolean isPure(final ExpressionPurityContext context) {
        return true; // the function itself is pure depending on the parameter values themselves.
    }

    private final static String MISSING_EXPRESSION = "Missing last parameter with expression";

    @Override
    public ExpressionFunction<?, SpreadsheetExpressionEvaluationContext> apply(final List<Object> values,
                                                                               final SpreadsheetExpressionEvaluationContext context) {
        final int count = values.size();
        if (0 == count) {
            throw new IllegalArgumentException(MISSING_EXPRESSION);
        }

        final int parameterNamesCount = count - 1;
        final ExpressionFunctionParameter<?>[] parameters = new ExpressionFunctionParameter[parameterNamesCount];
        for (int i = 0; i < parameterNamesCount; i++) {
            // the parameter must be a label(a label holds the parameter name).
            final SpreadsheetLabelName label = this.parameter(i, SpreadsheetLabelName.class)
                    .getOrFail(
                            values,
                            i
                    );

            // create a ExpressionFunctionParameterName with the label (parameter name) and type of object.
            parameters[i] = this.parameter(
                    ExpressionFunctionParameterName.with(label.value()),
                    Object.class
            );
        }

        return context.lambdaFunction(
                Lists.of(parameters),
                Object.class,
                EXPRESSION.getOrFail(values, count - 1)
        );
    }

    /**
     * Given the count assembles the parameters with the correct parameter names and types.
     */
    @Override
    public List<ExpressionFunctionParameter<?>> parameters(final int count) {
        List<ExpressionFunctionParameter<?>> parameters;

        switch (count) {
            case 0:
                throw new IllegalArgumentException(MISSING_EXPRESSION);
            case 1:
                parameters = COMPUTED_PARAMETERS;
                break;
            default:
                parameters = this.parameters0(count);
                break;
        }

        return parameters;
    }

    private final ExpressionFunctionParameter<Expression> EXPRESSION = ExpressionFunctionParameterName.with("expression")
            .required(Expression.class)
            .setKinds(ExpressionFunctionParameter.NO_KINDS);

    private final List<ExpressionFunctionParameter<?>> COMPUTED_PARAMETERS = Lists.of(EXPRESSION);

    /**
     * Creates the parameter list of parameters named parameters except for the last which is {@link #EXPRESSION}.
     */
    private List<ExpressionFunctionParameter<?>> parameters0(final int count) {
        final ExpressionFunctionParameter<?>[] parameters = new ExpressionFunctionParameter<?>[count];

        final int last = count - 1;
        for (int i = 0; i < last; i++) {
            parameters[i] = this.parameter(i + 1, SpreadsheetLabelName.class);
        }
        parameters[last] = EXPRESSION;

        return Lists.of(parameters);
    }

    private <T> ExpressionFunctionParameter<T> parameter(final int suffix,
                                                         final Class<T> type) {
        return this.parameter(
                this.parameterName(suffix),
                type
        );
    }

    private <T> ExpressionFunctionParameter<T> parameter(final ExpressionFunctionParameterName name,
                                                         final Class<T> type) {
        return name.required(type)
                .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE);
    }

    private ExpressionFunctionParameterName parameterName(final int suffix) {
        return ExpressionFunctionParameterName.with("parameter-" + suffix);
    }

    @Override
    public String toString() {
        return this.name()
                .get()
                .toString();
    }
}
