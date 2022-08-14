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

import walkingkooka.Either;
import walkingkooka.convert.Converter;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.ExpressionReference;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A {@link SpreadsheetExpressionEvaluationContext} that has a main goal of preparing parameters with a converter
 * that uses the provided decimal separator and group separator.
 */
final class NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext implements SpreadsheetExpressionEvaluationContext {

    static NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext with(final char decimalSeparator,
                                                                                          final char groupSeparator,
                                                                                          final SpreadsheetExpressionEvaluationContext context) {
        return new NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext(
                decimalSeparator,
                groupSeparator,
                context
        );
    }

    private NumberExpressionFunctionNumberValueSpreadsheetExpressionEvaluationContext(final char decimalSeparator,
                                                                                      final char groupSeparator,
                                                                                      final SpreadsheetExpressionEvaluationContext context) {
        super();

        this.decimalSeparator = decimalSeparator;
        this.groupSeparator = groupSeparator;
        this.context = context;
    }

    @Override
    public Optional<SpreadsheetCell> cell() {
        return this.context.cell();
    }

    @Override
    public Optional<SpreadsheetCell> loadCell(final SpreadsheetCellReference reference) {
        return this.context.loadCell(reference);
    }

    @Override
    public SpreadsheetParserToken parseExpression(final TextCursor expression) {
        return this.context.parseExpression(expression);
    }

    @Override
    public SpreadsheetSelection resolveIfLabel(SpreadsheetSelection selection) {
        return this.context.resolveIfLabel(selection);
    }

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        return this.context.spreadsheetMetadata();
    }

    @Override
    public AbsoluteUrl serverUrl() {
        return this.context.serverUrl();
    }

    @Override
    public CaseSensitivity caseSensitivity() {
        return this.context.caseSensitivity();
    }

    @Override
    public Locale locale() {
        return this.context.locale();
    }

    @Override
    public Converter<SpreadsheetConverterContext> converter() {
        return this.context.converter();
    }

    @Override
    public Object evaluate(final Expression expression) {
        return this.context.evaluate(expression);
    }

    @Override
    public ExpressionFunction<?, ExpressionEvaluationContext> function(final FunctionExpressionName name) {
        return this.context.function(name);
    }

    @Override
    public <T> T prepareParameter(final ExpressionFunctionParameter<T> parameter,
                                  final Object value) {
        return this.context.prepareParameter(parameter, value);
    }

    @Override
    public Object evaluateFunction(final ExpressionFunction<?, ? extends ExpressionEvaluationContext> function,
                                   final List<Object> parameters) {
        return this.context.evaluateFunction(
                function,
                parameters
        );
    }

    @Override
    public Object handleException(final RuntimeException cause) {
        return this.context.handleException(cause);
    }

    @Override
    public Optional<Optional<Object>> reference(final ExpressionReference reference) {
        return this.context.reference(reference);
    }

    @Override
    public boolean canConvert(final Object value,
                              final Class<?> type) {
        return this.converter()
                .canConvert(
                value,
                type,
                this
        );
    }

    @Override
    public <T> Either<T, String> convert(final Object value,
                                         final Class<T> type) {
        return this.converter()
                .convert(
                value,
                type,
                this
        );
    }

    @Override
    public List<String> ampms() {
        return this.context.ampms();
    }

    @Override
    public int defaultYear() {
        return this.context.defaultYear();
    }

    @Override
    public List<String> monthNames() {
        return this.context.monthNames();
    }

    @Override
    public List<String> monthNameAbbreviations() {
        return this.context.monthNameAbbreviations();
    }

    @Override
    public LocalDateTime now() {
        return this.context.now();
    }

    @Override
    public int twoDigitYear() {
        return this.context.twoDigitYear();
    }

    @Override
    public List<String> weekDayNames() {
        return this.context.weekDayNames();
    }

    @Override
    public List<String> weekDayNameAbbreviations() {
        return this.context.weekDayNameAbbreviations();
    }

    @Override
    public String currencySymbol() {
        return this.context.currencySymbol();
    }

    @Override
    public char decimalSeparator() {
        return this.decimalSeparator;
    }

    private final char decimalSeparator;

    @Override
    public String exponentSymbol() {
        return this.context.exponentSymbol();
    }

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.context.expressionNumberKind();
    }

    @Override
    public char groupingSeparator() {
        return this.groupSeparator;
    }

    private final char groupSeparator;

    @Override
    public MathContext mathContext() {
        return this.context.mathContext();
    }

    @Override
    public char negativeSign() {
        return this.context.negativeSign();
    }

    @Override
    public char percentageSymbol() {
        return this.context.percentageSymbol();
    }

    @Override
    public char positiveSign() {
        return this.context.positiveSign();
    }

    @Override
    public boolean isPure(final FunctionExpressionName name) {
        return this.context.isPure(name);
    }

    private final SpreadsheetExpressionEvaluationContext context;

    @Override
    public String toString() {
        return this.context.toString();
    }
}
