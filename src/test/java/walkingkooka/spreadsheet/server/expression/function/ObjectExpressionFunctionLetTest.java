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
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionNumber;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ObjectExpressionFunctionLetTest extends ObjectExpressionFunctionTestCase<ObjectExpressionFunctionLet> {

    @Test
    public void testZeroParametersFails() {
        this.applyFails(
                Lists.empty(),
                "Missing computed value/expression"
        );
    }

    @Test
    public void testEvenNumberParameterCountFails() {
        this.applyFails(
                Lists.of(1, 2),
                "Missing final computed value expression"
        );
    }

    @Test
    public void testInvalidVariableNameFails() {
        this.applyFails(
                Lists.of(
                        "!Label",
                        2,
                        3
                ),
                "Invalid character '!' at 0 in \"!Label\""
        );
    }

    @Test
    public void testDuplicateLabelFails() {
        this.applyFails(
                Lists.of(
                        "Duplicate1",
                        1,
                        "DUPLICATE1",
                        2,
                        999
                ),
                "Duplicate name \"DUPLICATE1\" in value 3"
        );
    }

    @Test
    public void testDuplicateLabelFails2() {
        this.applyFails(
                Lists.of(
                        "Duplicate1",
                        1,
                        "X",
                        2,
                        "DUPLICATE1",
                        3,
                        999
                ),
                "Duplicate name \"DUPLICATE1\" in value 5"
        );
    }

    private void applyFails(final List<Object> parameters,
                            final String message) {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class, () -> {
                    this.createBiFunction().apply(parameters, this.createContext());
                });
        this.checkEquals(
                message,
                thrown.getMessage(),
                "message"
        );
    }

    @Test
    public void testOnlyComputedValueNumberLiteral() {
        this.applyAndCheck2(
                Lists.of(
                        123
                ),
                123
        );
    }

    @Test
    public void testOnlyComputedValueStringLiteral() {
        this.applyAndCheck2(
                Lists.of(
                        "ABC"
                ),
                "ABC"
        );
    }

    @Test
    public void testNamedValueIgnored() {
        this.applyAndCheck2(
                Lists.of(
                        "ABC",
                        123,
                        "DEF"
                ),
                "DEF"
        );
    }

    @Test
    public void testComputedValueReferencesNamedValue() {
        final String name = "x";
        final int value = 23;

        this.applyAndCheck2(
                Lists.of(
                        name,
                        value,
                        Expression.add(
                                Expression.reference(
                                        SpreadsheetSelection.labelName(name)
                                ),
                                Expression.value(100)
                        )
                ),
                EXPRESSION_NUMBER_KIND.create(100 + 23)
        );
    }

    @Test
    public void testComputedValueReferencesNamedValue2() {
        final String name1 = "x";
        final ExpressionNumber value1 = EXPRESSION_NUMBER_KIND.one();

        final String name2 = "y";
        final ExpressionNumber value2 = EXPRESSION_NUMBER_KIND.create(20);

        this.applyAndCheck2(
                Lists.of(
                        name1,
                        value1,
                        name2,
                        value2,
                        Expression.add(
                                Expression.reference(
                                        SpreadsheetSelection.labelName(name1)
                                ),
                                Expression.reference(
                                        SpreadsheetSelection.labelName(name2)
                                )
                        )
                ),
                EXPRESSION_NUMBER_KIND.create(20 + 1)
        );
    }

    @Test
    public void testComputedValueReferencesNamedValue3() {
        final String name1 = "x";
        final ExpressionNumber value1 = EXPRESSION_NUMBER_KIND.one();

        final String name2 = "y";
        final ExpressionNumber value2 = EXPRESSION_NUMBER_KIND.create(20);

        final String name3 = "z";
        final ExpressionNumber value3 = EXPRESSION_NUMBER_KIND.create(300);

        this.applyAndCheck2(
                Lists.of(
                        name1,
                        value1,
                        name2,
                        value2,
                        name3,
                        value3,
                        Expression.add(
                                Expression.reference(
                                        SpreadsheetSelection.labelName(name1)
                                ),
                                Expression.add(
                                        Expression.reference(
                                                SpreadsheetSelection.labelName(name2)
                                        ),
                                        Expression.reference(
                                                SpreadsheetSelection.labelName(name3)
                                        )
                                )
                        )
                ),
                EXPRESSION_NUMBER_KIND.create(300 + 20 + 1)
        );
    }

    // test related factories...........................................................................................

    @Override
    public ObjectExpressionFunctionLet createBiFunction() {
        return ObjectExpressionFunctionLet.INSTANCE;
    }

    @Override
    public SpreadsheetExpressionEvaluationContext createContext() {
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
                        .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, EXPRESSION_NUMBER_KIND)
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
                NOW
        );
    }

    @Override
    public Class<ObjectExpressionFunctionLet> type() {
        return ObjectExpressionFunctionLet.class;
    }
}
