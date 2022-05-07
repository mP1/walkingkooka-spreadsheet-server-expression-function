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
import walkingkooka.Cast;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converters;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.function.SpreadsheetExpressionFunctionContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;

import java.lang.reflect.Method;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SpreadsheetServerExpressionFunctionsTest implements PublicStaticHelperTesting<SpreadsheetServerExpressionFunctions> {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");
    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("http://server.example.com");

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.BIG_DECIMAL;

    @Test
    public void testVisit() {
        final Set<FunctionExpressionName> names = Sets.sorted();
        SpreadsheetServerExpressionFunctions.visit((e) -> names.add(e.name()));

        this.checkEquals(
                Arrays.stream(
                                SpreadsheetServerExpressionFunctions.class.getDeclaredMethods()
                        )
                        .filter(m -> m.getReturnType() == ExpressionFunction.class)
                        .map(Method::getName)
                        .collect(Collectors.toCollection(Sets::sorted))
                        .size(),
                names.size());
    }

    // evaluateAndCheck tests...........................................................................................

    @Test
    public void testMathExpression() {
        this.evaluateAndCheck(
                "=1+2+3",
                EXPRESSION_NUMBER_KIND.create(1 + 2 + 3)
        );
    }

    @Test
    public void testMathExpressionWithReferences() {
        this.evaluateAndCheck(
                "=1+A2+A3",
                Maps.of(
                        "A2", "=2",
                        "A3", "=3"
                ),
                EXPRESSION_NUMBER_KIND.create(1 + 2 + 3)
        );
    }

    @Test
    public void testMathExpressionEvaluationFailureDivideByZero() {
        this.evaluateAndCheck(
                "=1/0",
                SpreadsheetErrorKind.DIV0.setMessage("Division by zero")
        );
    }

    @Test
    public void testMathExpressionEvaluationFailureReferenceNotFound() {
        this.evaluateAndCheck(
                "=1+A2+A3",
                Maps.of(
                        "A2", "=2"
                ),
                SpreadsheetErrorKind.REF.setMessage("Reference not found: A3")
        );
    }

    @Test
    public void testFunctionNameCaseInsensitive() {
        this.evaluateAndCheck(
                "=TRUE()",
                true
        );
    }

    // function tests...................................................................................................

    @Test
    public void testAddress() {
        this.evaluateAndCheck(
                "=address(1, 2)",
                SpreadsheetSelection.parseCell("$B$1")
        );
    }

    @Test
    public void testAndTrueTrue() {
        this.evaluateAndCheck(
                "=and(true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testAndTrue1() {
        this.evaluateAndCheck(
                "=and(true(), 1)",
                Boolean.TRUE
        );
    }

    @Test
    public void testAndTrue0() {
        this.evaluateAndCheck(
                "=and(true(), 0)",
                Boolean.FALSE
        );
    }

    @Test
    public void testAndTrueTrueTrue() {
        this.evaluateAndCheck(
                "=and(true(), true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testChar65() {
        this.evaluateAndCheck(
                "=char(65)",
                'A'
        );
    }

    @Test
    public void testChooseFirst() {
        this.evaluateAndCheck(
                "=choose(1, 111, 222, 333)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testChooseSecond() {
        this.evaluateAndCheck(
                "=choose(2, 111, 222, 333)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testChooseThird() {
        this.evaluateAndCheck(
                "=choose(3, 111, true(), \"Third\")",
                "Third"
        );
    }

    @Test
    public void testClean() {
        this.evaluateAndCheck(
                "=clean(\"\t\nNeeds cleaning \r\")",
                "Needs cleaning "
        );
    }

    @Test
    public void testCodeCapitalA() {
        this.evaluateAndCheck(
                "=code(\"A\")",
                EXPRESSION_NUMBER_KIND.create(65)
        );
    }

    @Test
    public void testConcatSingleValues() {
        this.evaluateAndCheck(
                "=concat(A2,A3)",
                Maps.of("A2", "'abc", "A3", "'123"),
                "abc123"
        );
    }

    @Test
    public void testColumn() {
        this.evaluateAndCheck(
                "=column(C1)",
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testColumns() {
        this.evaluateAndCheck(
                "=columns(Z99)",
                EXPRESSION_NUMBER_KIND.create(1)
        );
    }

    @Test
    public void testColumnsWithRange() {
        this.evaluateAndCheck(
                "=columns(B1:D1)",
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testConcatRange() {
        this.evaluateAndCheck(
                "=concat(A2:A3)",
                Maps.of("A2", "'abc", "A3", "'123"),
                "abc123"
        );
    }

    @Test
    public void testConcatSingleValuesAndRange() {
        this.evaluateAndCheck(
                "=concat(\"First\",A2:A3,\"!!!\",B1:B2)",
                Maps.of(
                        "A2", "'abc",
                        "A3", "'123",
                        "B1", "'SecondLast",
                        "B2", "'Last"
                ),
                "Firstabc123!!!SecondLastLast"
        );
    }

    @Test
    public void testFalse() {
        this.evaluateAndCheck(
                "=false()",
                Boolean.FALSE
        );
    }

    @Test
    public void testFormulaText() {
        this.evaluateAndCheck(
                "=formulatext(A2)",
                Maps.of("A2", "=1+2+3"),
                "=1+2+3"
        );
    }

    @Test
    public void testIfTrue() {
        this.evaluateAndCheck(
                "=if(true(), 111, 222)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testIfTrueCaseInsensitiveStringCompare() {
        this.evaluateAndCheck(
                "=if(\"abc\" = \"ABC\", 111, 222)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testIfTrueCaseInsensitiveStringCompareDifferent() {
        this.evaluateAndCheck(
                "=if(\"abc\" = \"different\", 111, 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIfFalse() {
        this.evaluateAndCheck(
                "=if(false(), 111, 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIfsFirst() {
        this.evaluateAndCheck(
                "=ifs(true(), 111, false(), 222)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testIfsSecond() {
        this.evaluateAndCheck(
                "=ifs(\"abc\"=\"different\", 111, true(), 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIfsSecondStringCaseInsensitiveEquals() {
        this.evaluateAndCheck(
                "=ifs(\"abc\"=\"different\", 111, \"same\"=\"SAME\", 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIsBlankNoCell() {
        this.evaluateAndCheck(
                "=isBlank(B2)",
                true
        );
    }

    @Test
    public void testIsBlankCell() {
        this.evaluateAndCheck(
                "=isBlank(B2)",
                Maps.of("B2", "'NotBlank"),
                false
        );
    }

    @Test
    public void testIsErrWithError() {
        this.evaluateAndCheck(
                "=isErr(1/0)",
                true
        );
    }

    @Test
    public void testIsErrWithNumber() {
        this.evaluateAndCheck(
                "=isErr(123)",
                false
        );
    }

    @Test
    public void testIsErrorWithErroror() {
        this.evaluateAndCheck(
                "=isError(1/0)",
                true
        );
    }

    @Test
    public void testIsErrorWithNumber() {
        this.evaluateAndCheck(
                "=isError(123)",
                false
        );
    }

    @Test
    public void testIsNaWithError() {
        this.evaluateAndCheck(
                "=isNa(1/0)",
                false
        );
    }

    @Test
    public void testIsNaWithNumber() {
        this.evaluateAndCheck(
                "=isNa(123)",
                false
        );
    }
    
    @Test
    public void testLeft() {
        this.evaluateAndCheck(
                "=left(\"abc\")",
                "a"
        );
    }

    @Test
    public void testLeft2() {
        this.evaluateAndCheck(
                "=left(\"abc\", 2)",
                "ab"
        );
    }

    @Test
    public void testLen() {
        this.evaluateAndCheck(
                "=len(\"hello\")",
                EXPRESSION_NUMBER_KIND.create(5)
        );
    }

    @Test
    public void testLower() {
        this.evaluateAndCheck(
                "=lower(\"ABCxyz\")",
                "abcxyz"
        );
    }

    @Test
    public void testMid() {
        this.evaluateAndCheck(
                "=mid(\"apple\", 2, 3)",
                "ppl"
        );
    }

    @Test
    public void testNotFalse() {
        this.evaluateAndCheck(
                "=not(false())",
                Boolean.TRUE
        );
    }

    @Test
    public void testNotTrue() {
        this.evaluateAndCheck(
                "=not(true())",
                Boolean.FALSE
        );
    }

    @Test
    public void testNotZero() {
        this.evaluateAndCheck(
                "=not(0)",
                Boolean.TRUE
        );
    }

    @Test
    public void testOrTrueTrueTrue() {
        this.evaluateAndCheck(
                "=or(true(), true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testOrFalseFalseTrue() {
        this.evaluateAndCheck(
                "=or(false(), false(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testOrFalseFalseFalseFalse() {
        this.evaluateAndCheck(
                "=or(false(), false(), false(), false())",
                Boolean.FALSE
        );
    }

    @Test
    public void testRight() {
        this.evaluateAndCheck(
                "=right(\"abc\")",
                "c"
        );
    }

    @Test
    public void testRight2() {
        this.evaluateAndCheck(
                "=right(\"abc\", 2)",
                "bc"
        );
    }
    
    @Test
    public void testSwitchFirst() {
        this.evaluateAndCheck(
                "=switch(1, 1, \"One\", 2, \"Two\", 3, 333)",
                "One"
        );
    }

    @Test
    public void testSwitchSecond() {
        this.evaluateAndCheck(
                "=switch(\"TWO22\", 1, \"One\", \"Two22\", \"Two\", 3, 333, \"switch-default\")",
                "Two"
        );
    }

    @Test
    public void testSwitchDefaults() {
        this.evaluateAndCheck(
                "=switch(999, 1, \"One\", 22, \"Two\", 3, 333, \"switch-default\")",
                "switch-default"
        );
    }

    @Test
    public void testTrue() {
        this.evaluateAndCheck(
                "=true()",
                Boolean.TRUE
        );
    }

    @Test
    public void testTrue2() {
        this.evaluateAndCheck(
                "=true()",
                Maps.of("A2", "=true()"),
                Boolean.TRUE
        );
    }

    @Test
    public void testUnichar97() {
        this.evaluateAndCheck(
                "=unichar(97)",
                'a'
        );
    }

    @Test
    public void testUnichar1000() {
        this.evaluateAndCheck(
                "=unichar(1000)",
                Character.valueOf((char) 1000)
        );
    }

    @Test
    public void testUnicodeA() {
        this.evaluateAndCheck(
                "=unicode(\"A\")",
                EXPRESSION_NUMBER_KIND.create((int) 'A')
        );
    }

    @Test
    public void testUnicodeChar1000() {
        final char c = 1000;
        this.evaluateAndCheck(
                "=unicode(\"" + c + "\")",
                EXPRESSION_NUMBER_KIND.create((int) c)
        );
    }

    @Test
    public void testUpper() {
        this.evaluateAndCheck(
                "=upper(\"ABCxyz\")",
                "ABCXYZ"
        );
    }

    @Test
    public void testXorTrueTrueTrue() {
        this.evaluateAndCheck(
                "=xor(true(), true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testXorFalseFalseTrue() {
        this.evaluateAndCheck(
                "=xor(true(), false(), true())",
                Boolean.FALSE
        );
    }

    @Test
    public void testXorFalseFalseFalseFalse() {
        this.evaluateAndCheck(
                "=xor(false(), false(), false(), false())",
                Boolean.FALSE
        );
    }
    
    private void evaluateAndCheck(final String cellFormula,
                                  final Object expectedResult) {
        this.evaluateAndCheck(
                cellFormula,
                Maps.empty(),
                expectedResult
        );
    }

    private void evaluateAndCheck(final String cellFormula,
                                  final Map<String, String> preload,
                                  final Object expectedResult) {
        this.evaluateAndCheck(
                SpreadsheetSelection.parseCell("A1"),
                cellFormula,
                preload,
                Optional.ofNullable(expectedResult)
        );
    }

    private void evaluateAndCheck(final SpreadsheetCellReference cellReference,
                                  final String cellFormula,
                                  final Map<String, String> preload,
                                  final Object expectedResult) {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.parse("1234"))
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_NAME, SpreadsheetName.with("Untitled5678"))
                .set(SpreadsheetMetadataPropertyName.LOCALE, LOCALE)
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.now())
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified@example.com"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.now())
                .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, Converters.EXCEL_1904_DATE_SYSTEM_OFFSET)
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, EXPRESSION_NUMBER_KIND)
                .set(SpreadsheetMetadataPropertyName.PRECISION, MathContext.UNLIMITED.getPrecision())
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@"))
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20);

        final SpreadsheetEngine engine = SpreadsheetEngines.basic(
                metadata
        );

        final Map<String, ExpressionFunction<?, SpreadsheetExpressionFunctionContext>> nameToFunctions = Maps.sorted(String.CASE_INSENSITIVE_ORDER);
        SpreadsheetServerExpressionFunctions.visit(
                (f -> nameToFunctions.put(f.name().value(), f))
        );

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();
        metadataStore.save(metadata);

        final SpreadsheetStoreRepository repo = SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetColumnStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                metadataStore,
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetRowStores.treeMap(),
                SpreadsheetUserStores.treeMap()
        );

        final SpreadsheetEngineContext context = SpreadsheetEngineContexts.basic(
                metadata,
                (n) -> {
                    Objects.requireNonNull(n, "name");
                    final ExpressionFunction<?, ?> function = nameToFunctions.get(n.value());
                    if (null == function) {
                        throw new IllegalArgumentException("Unknown function " + n);
                    }
                    return Cast.to(function);
                },
                engine,
                (b) -> {
                    throw new UnsupportedOperationException();
                },
                repo,
                SERVER_URL
        );

        // save all the preload cells, these will contain references in the test cell.
        for (final Map.Entry<String, String> referenceToExpression : preload.entrySet()) {
            final String reference = referenceToExpression.getKey();
            final String formula = referenceToExpression.getValue();

            engine.saveCell(
                    SpreadsheetSelection.parseCell(reference)
                            .setFormula(SpreadsheetFormula.EMPTY.setText(formula)),
                    context
            );
        }

        final SpreadsheetCell saved = engine.saveCell(
                        cellReference.setFormula(
                                SpreadsheetFormula.EMPTY.setText(cellFormula)
                        ),
                        context
                ).cell(cellReference)
                .orElseThrow(() -> new AssertionError("Missing " + cellReference + " after saving " + cellFormula));

        this.checkEquals(
                expectedResult,
                saved.formula().value(),
                cellReference + "=" + cellFormula + "\n" +
                        preload.entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue())
                                .collect(Collectors.joining("\n"))
        );
    }

    // PublicStaticHelperTesting........................................................................................

    @Test
    public void testPublicStaticMethodsWithoutMathContextParameter() {
        this.publicStaticMethodParametersTypeCheck(MathContext.class);
    }

    @Override
    public Class<SpreadsheetServerExpressionFunctions> type() {
        return SpreadsheetServerExpressionFunctions.class;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
