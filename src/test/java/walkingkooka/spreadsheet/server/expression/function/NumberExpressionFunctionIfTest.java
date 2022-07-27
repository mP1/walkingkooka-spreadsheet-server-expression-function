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
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.Converters;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContexts;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.tree.expression.ExpressionNumberKind;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public final class NumberExpressionFunctionIfTest extends NumberExpressionFunctionTestCase<NumberExpressionFunctionIf> {

    // countIf.........................................................................................................

    @Test
    public void testCountIf() {
        this.countIfAndCheck(
                Lists.of(
                        1,
                        2
                ),
                1,
                1
        );
    }

    @Test
    public void testCountIfHalfFiltered() {
        this.countIfAndCheck(
                Lists.of(
                        1,
                        2,
                        100,
                        200
                ),
                "> 80 + 10",
                2
        );
    }

    private void countIfAndCheck(final List<Object> values,
                                 final Object condition,
                                 final Number expected) {
        this.applyIfAndCheck(
                NumberExpressionFunctionIf.countIf(),
                values,
                condition,
                expected
        );
    }

    // sumIf.........................................................................................................

    @Test
    public void testSumIf() {
        this.sumIfAndCheck(
                Lists.of(
                        1,
                        2
                ),
                1,
                1
        );
    }

    @Test
    public void testSumIfHalfFiltered() {
        this.sumIfAndCheck(
                Lists.of(
                        1,
                        2,
                        100,
                        200
                ),
                "> 80 + 10",
                100 + 200
        );
    }

    private void sumIfAndCheck(final List<Object> values,
                                 final Object condition,
                                 final Number expected) {
        this.applyIfAndCheck(
                NumberExpressionFunctionIf.sumIf(),
                values,
                condition,
                expected
        );
    }

    // NumberExpressionFunctionIf......................................................................................

    private void applyIfAndCheck(final NumberExpressionFunctionIf function,
                                 final Object value,
                                 final Object condition,
                                 final Number expected) {
        this.applyAndCheck(
                function,
                List.of(
                        value instanceof List ?
                                prepareList((List<?>) value) :
                                wrapIfNumber(value),
                        condition
                ),
                this.createContext(),
                EXPRESSION_NUMBER_KIND.create(expected)
        );
    }

    private List<Object> prepareList(final List<?> list) {
        return list.stream()
                .map(this::wrapIfNumber)
                .collect(Collectors.toList());
    }

    private Object wrapIfNumber(final Object value) {
        return value instanceof Number ?
                EXPRESSION_NUMBER_KIND.create((Number) value) :
                value;
    }


    @Override
    public NumberExpressionFunctionIf createBiFunction() {
        return NumberExpressionFunctionIf.countIf();
    }

    @Override
    public int minimumParameterCount() {
        return 1;
    }

    @Override
    SpreadsheetExpressionEvaluationContext createContext(final ExpressionNumberKind kind) {
        return SpreadsheetExpressionEvaluationContexts.basic(
                Optional.empty(),
                SpreadsheetCellStores.fake(),
                Url.parseAbsolute("https://example.com/server"),
                SpreadsheetMetadata.EMPTY
                        .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.parse("1234"))
                        .set(SpreadsheetMetadataPropertyName.SPREADSHEET_NAME, SpreadsheetName.with("Untitled5678"))
                        .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                        .loadFromLocale()
                        .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                        .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.now())
                        .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified@example.com"))
                        .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.now())
                        .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                        .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, Converters.EXCEL_1904_DATE_SYSTEM_OFFSET)
                        .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
                        .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, kind)
                        .set(SpreadsheetMetadataPropertyName.PRECISION, MathContext.DECIMAL32.getPrecision())
                        .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                        .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetPattern.parseNumberFormatPattern("#.###"))
                        .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@@"))
                        .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20),
                (n) -> {
                    throw new UnsupportedOperationException();
                },
                (r) -> {
                    throw new UnsupportedOperationException();
                },
                (s) -> {
                    throw new UnsupportedOperationException();
                },
                LocalDateTime::now
        );
    }

    @Override
    public Class<NumberExpressionFunctionIf> type() {
        return NumberExpressionFunctionIf.class;
    }
}
