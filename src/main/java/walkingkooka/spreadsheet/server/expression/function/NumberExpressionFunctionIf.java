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
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionPurityContext;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterName;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class NumberExpressionFunctionIf extends NumberExpressionFunction {

    static NumberExpressionFunctionIf countIf() {
        return new NumberExpressionFunctionIf(
                "countIf",
                SpreadsheetServerExpressionFunctions.count(),
                ExpressionFunctionParameter.VALUE
        );
    }

    private final static ExpressionFunctionParameter<Object> CRITERIA = ExpressionFunctionParameterName.with("criteria")
            .required(Object.class);

    private NumberExpressionFunctionIf(final String name,
                                       final ExpressionFunction<ExpressionNumber, SpreadsheetExpressionEvaluationContext> function,
                                       final ExpressionFunctionParameter<?> value) {
        super(name);
        this.function = function;

        this.parameters = Lists.of(
                value,
                CRITERIA
        );
    }

    @Override
    public Set<ExpressionFunctionKind> kinds() {
        return EnumSet.of(
                ExpressionFunctionKind.CONVERT_PARAMETERS,
                ExpressionFunctionKind.EVALUATE_PARAMETERS,
                ExpressionFunctionKind.RESOLVE_REFERENCES
        );
    }

    @Override
    public List<ExpressionFunctionParameter<?>> parameters() {
        return this.parameters;
    }

    private final List<ExpressionFunctionParameter<?>> parameters;

    @Override
    public ExpressionNumber apply(final List<Object> parameters,
                                  final SpreadsheetExpressionEvaluationContext context) {
        final Object value = ExpressionFunctionParameter.VALUE.getOrFail(parameters, 0);

        final Predicate<Object> criteria = this.criteria(
                CRITERIA.getOrFail(parameters, 1),
                context
        );

        return this.function.apply(
                this.filterParameters(
                        value,
                        criteria,
                        context
                ),
                context
        );
    }

    /**
     * If the value is text is maybe criteria and it starts with an operator then an binary expression is constructed,
     * otherwise the value will be the RHS of an equals expression.
     */
    private Predicate<Object> criteria(final Object value,
                                       final SpreadsheetExpressionEvaluationContext context) {
        return context.isText(value) ?
                this.filterExpression(
                        context.convertOrFail(value, String.class),
                        context
                ) :
                this.equalsExpression(value, context);
    }

    /**
     * First tests if the {@link String} begins with one of the operators and then parses the remainder as an
     * {@link Expression}.
     */
    private Predicate<Object> filterExpression(final String value,
                                               final SpreadsheetExpressionEvaluationContext context) {
        return value.startsWith("<>") ?
                this.notEqualsExpression(
                        value.substring(2),
                        context
                ) :
                value.startsWith("<=") ?
                        this.lessThanEquals(
                                value.substring(2),
                                context
                        ) :
                        value.startsWith("<") ?
                                this.lessThan(
                                        value.substring(1),
                                        context
                                ) :
                                value.startsWith(">=") ?
                                        this.greaterThanEquals(
                                                value.substring(2),
                                                context
                                        ) :
                                        value.startsWith(">") ?
                                                this.greaterThan(
                                                        value.substring(1),
                                                        context
                                                ) :
                                                value.startsWith("=") ?
                                                        this.equalsExpression(
                                                                value.substring(1),
                                                                context
                                                        ) :
                                                        this.equalsString(value, context);
    }

    private Predicate<Object> notEqualsExpression(final String value,
                                                  final SpreadsheetExpressionEvaluationContext context) {
        return this.criteriaFilter(
                value,
                Expression::notEquals,
                context
        );
    }

    private Predicate<Object> lessThanEquals(final String value,
                                             final SpreadsheetExpressionEvaluationContext context) {
        return this.criteriaFilter(
                value,
                Expression::lessThanEquals,
                context
        );
    }

    private Predicate<Object> lessThan(final String value,
                                       final SpreadsheetExpressionEvaluationContext context) {
        return this.criteriaFilter(
                value,
                Expression::lessThan,
                context
        );
    }

    private Predicate<Object> greaterThanEquals(final String value,
                                                final SpreadsheetExpressionEvaluationContext context) {
        return this.criteriaFilter(
                value,
                Expression::greaterThanEquals,
                context
        );
    }

    private Predicate<Object> greaterThan(final String value,
                                          final SpreadsheetExpressionEvaluationContext context) {
        return this.criteriaFilter(
                value,
                Expression::greaterThan,
                context
        );
    }

    private Predicate<Object> equalsExpression(final String value,
                                               final SpreadsheetExpressionEvaluationContext context) {
        return this.criteriaFilter(
                value,
                Expression::equalsExpression,
                context
        );
    }

    private Predicate<Object> criteriaFilter(final String value,
                                             final BiFunction<Expression, Expression, Expression> condition,
                                             final SpreadsheetExpressionEvaluationContext context) {

        return (v) -> (Boolean) context.evaluate(
                condition.apply(
                        Expression.value(v),
                        context.parseExpression(
                                TextCursors.charSequence(value)
                        ).toExpression(context)
                                .orElse(null)
                )
        );
    }

    /**
     * Creates a {@link Predicate} which will accept the value and testing that using the glob pattern {@link String} value.
     */
    private Predicate<Object> equalsString(final String expression,
                                           final SpreadsheetExpressionEvaluationContext context) {
        return (v) ->
                CaseSensitivity.SENSITIVE.globPattern(expression, '~')
                        .test(
                                context.convertOrFail(
                                        context.evaluate(
                                                Expression.value(v)
                                        ),
                                        String.class
                                )
                        );
    }

    private Predicate<Object> equalsExpression(final Object value,
                                               final SpreadsheetExpressionEvaluationContext context) {
        return (v) -> (Boolean) context.evaluate(
                Expression.equalsExpression(
                        Expression.value(v),
                        Expression.value(value)
                )
        );
    }

    /**
     * Filters the parameter values using the filter.
     */
    private List<Object> filterParameters(final Object value,
                                          final Predicate<Object> filter,
                                          final SpreadsheetExpressionEvaluationContext context) {
        return this.filterParameters0(
                value instanceof List ?
                        Cast.to(value) :
                        Lists.of(value),
                filter
        );
    }

    /**
     * Filter the parameters using the filter created using the criteria.
     */
    private List<Object> filterParameters0(final List<Object> value,
                                           final Predicate<Object> filter) {
        return value.stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPure(final ExpressionPurityContext context) {
        return this.function.isPure(context);
    }

    private final ExpressionFunction<ExpressionNumber, SpreadsheetExpressionEvaluationContext> function;
}
