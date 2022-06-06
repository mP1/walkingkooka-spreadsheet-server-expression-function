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

import walkingkooka.reflect.StaticHelper;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.tree.expression.Expression;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Factory that analyzes the predicate or condition parameter for functions such as countif.
 */
final class IfFunctionPredicate implements StaticHelper {

    /**
     * If the value is text is maybe criteria and it starts with an operator then a binary expression is constructed,
     * otherwise the value will be the RHS of an equals expression.
     */
    static Predicate<Object> with(final Object value,
                                  final SpreadsheetExpressionEvaluationContext context) {
        return context.isText(value) ?
                analyzeExpression(
                        context.convertOrFail(value, String.class),
                        context
                ) :
                equalsExpression(value, context);
    }

    /**
     * First tests if the {@link String} begins with one of the operators and then parses the remainder as an
     * {@link Expression}.
     */
    private static Predicate<Object> analyzeExpression(final String value,
                                                       final SpreadsheetExpressionEvaluationContext context) {
        return value.startsWith("<>") ?
                notEqualsExpression(
                        value.substring(2),
                        context
                ) :
                value.startsWith("<=") ?
                        lessThanEquals(
                                value.substring(2),
                                context
                        ) :
                        value.startsWith("<") ?
                                lessThan(
                                        value.substring(1),
                                        context
                                ) :
                                value.startsWith(">=") ?
                                        greaterThanEquals(
                                                value.substring(2),
                                                context
                                        ) :
                                        value.startsWith(">") ?
                                                greaterThan(
                                                        value.substring(1),
                                                        context
                                                ) :
                                                value.startsWith("=") ?
                                                        equalsExpression(
                                                                value.substring(1),
                                                                context
                                                        ) :
                                                        equalsString(value, context);
    }

    private static Predicate<Object> notEqualsExpression(final String value,
                                                         final SpreadsheetExpressionEvaluationContext context) {
        return criteriaFilter(
                value,
                Expression::notEquals,
                context
        );
    }

    private static Predicate<Object> lessThanEquals(final String value,
                                                    final SpreadsheetExpressionEvaluationContext context) {
        return criteriaFilter(
                value,
                Expression::lessThanEquals,
                context
        );
    }

    private static Predicate<Object> lessThan(final String value,
                                              final SpreadsheetExpressionEvaluationContext context) {
        return criteriaFilter(
                value,
                Expression::lessThan,
                context
        );
    }

    private static Predicate<Object> greaterThanEquals(final String value,
                                                       final SpreadsheetExpressionEvaluationContext context) {
        return criteriaFilter(
                value,
                Expression::greaterThanEquals,
                context
        );
    }

    private static Predicate<Object> greaterThan(final String value,
                                                 final SpreadsheetExpressionEvaluationContext context) {
        return criteriaFilter(
                value,
                Expression::greaterThan,
                context
        );
    }

    private static Predicate<Object> equalsExpression(final String value,
                                                      final SpreadsheetExpressionEvaluationContext context) {
        return criteriaFilter(
                value,
                Expression::equalsExpression,
                context
        );
    }

    private static Predicate<Object> criteriaFilter(final String value,
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
    private static Predicate<Object> equalsString(final String expression,
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

    private static Predicate<Object> equalsExpression(final Object value,
                                                      final SpreadsheetExpressionEvaluationContext context) {
        return (v) -> (Boolean) context.evaluate(
                Expression.equalsExpression(
                        Expression.value(v),
                        Expression.value(value)
                )
        );
    }
}
