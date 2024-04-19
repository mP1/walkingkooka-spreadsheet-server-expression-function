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
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converters;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetError;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.compare.SpreadsheetComparators;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.TextNode;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;

import java.lang.reflect.Method;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SpreadsheetServerExpressionFunctionsTest implements PublicStaticHelperTesting<SpreadsheetServerExpressionFunctions>,
        TreePrintableTesting {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");
    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://server.example.com");

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.BIG_DECIMAL;

    private final static Supplier<LocalDateTime> NOW = () -> LocalDateTime.of(1999, 12, 31, 12, 58, 59);

    @Test
    public void testVisit() {
        final Set<FunctionExpressionName> names = Sets.sorted();
        SpreadsheetServerExpressionFunctions.visit(
                (e) -> {
                    final FunctionExpressionName name = e.name()
                            .get();
                    if (!names.add(name)) {
                        throw new IllegalStateException("Duplicate function name: " + name);
                    }
                }
        );

        this.checkEquals(
                Arrays.stream(
                                SpreadsheetServerExpressionFunctions.class.getDeclaredMethods()
                        )
                        .filter(m -> JavaVisibility.of(m) == JavaVisibility.PUBLIC)
                        .filter(m -> m.getReturnType() == ExpressionFunction.class)
                        .map(Method::getName)
                        .collect(Collectors.toCollection(Sets::sorted))
                        .size(),
                names.size()
        );
    }

    // error handling tests............................................................................................

    @Test
    public void testFormulaError() {
        this.evaluateAndValueCheck(
                "=1+",
                SpreadsheetErrorKind.ERROR.setMessage(
                        "End of text at (4,1) \"=1+\" expected BINARY_SUB_EXPRESSION"
                )
        );
    }

    @Test
    public void testFormulaEqMissingCell() {
        this.evaluateAndValueCheck(
                "=Z99",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testFormulaEqUnknownLabel() {
        this.evaluateAndValueCheck(
                "=Label123",
                SpreadsheetError.selectionNotFound(
                        SpreadsheetSelection.labelName("Label123")
                )
        );
    }

    @Test
    public void testFormulaEqUnknownFunction() {
        this.evaluateAndValueCheck(
                "=UnknownFunction123()",
                SpreadsheetError.functionNotFound(
                        FunctionExpressionName.with("UnknownFunction123")
                )
        );
    }

    @Test
    public void testFormulaIncludesMissingCell() {
        this.evaluateAndValueCheck(
                "=123+Z99",
                EXPRESSION_NUMBER_KIND.create(123)
        );
    }

    // evaluateAndValueCheck tests......................................................................................

    @Test
    public void testMathExpression() {
        this.evaluateAndValueCheck(
                "=1+2+3",
                EXPRESSION_NUMBER_KIND.create(1 + 2 + 3)
        );
    }

    @Test
    public void testMathExpressionWithReferences() {
        this.evaluateAndValueCheck(
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
        this.evaluateAndValueCheck(
                "=1/0",
                SpreadsheetErrorKind.DIV0.setMessage("Division by zero")
        );
    }

    @Test
    public void testMathExpressionEvaluation() {
        this.evaluateAndValueCheck(
                "=1+A2+A3",
                Maps.of(
                        "A2", "=2"
                ),
                EXPRESSION_NUMBER_KIND.create(
                        1 + 2 + 0
                )
        );
    }

    @Test
    public void testFunctionNameCaseInsensitive() {
        this.evaluateAndValueCheck(
                "=TRUE()",
                true
        );
    }

    // function tests...................................................................................................

    @Test
    public void testAbs() {
        this.evaluateAndValueCheck(
                "=abs(-1.5)+abs(0.5)",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testAcos() {
        this.evaluateAndValueCheck(
                "=acos(0.5)",
                EXPRESSION_NUMBER_KIND.create(1.047198)
        );
    }

    @Test
    public void testAddress() {
        this.evaluateAndValueCheck(
                "=address(1, 2)",
                SpreadsheetSelection.parseCell("$B$1")
        );
    }

    @Test
    public void testAndTrueTrue() {
        this.evaluateAndValueCheck(
                "=and(true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testAndTrue1() {
        this.evaluateAndValueCheck(
                "=and(true(), 1)",
                Boolean.TRUE
        );
    }

    @Test
    public void testAndTrue0() {
        this.evaluateAndValueCheck(
                "=and(true(), 0)",
                Boolean.FALSE
        );
    }

    @Test
    public void testAndTrueTrueTrue() {
        this.evaluateAndValueCheck(
                "=and(true(), true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testAsin() {
        this.evaluateAndValueCheck(
                "=asin(0.5)",
                EXPRESSION_NUMBER_KIND.create(0.5235988)
        );
    }

    @Test
    public void testAtan() {
        this.evaluateAndValueCheck(
                "=atan(0.5)",
                EXPRESSION_NUMBER_KIND.create(0.4636476)
        );
    }

    @Test
    public void testAverage() {
        this.evaluateAndValueCheck(
                "=average(1,20,300,B1:D1)",
                Maps.of(
                        "B1", "1000",
                        "C1", "2000",
                        "D1", "3000"
                ),
                EXPRESSION_NUMBER_KIND.create(1053.5)
        );
    }

    @Test
    public void testAverageIfSomeValuesFiltered() {
        this.evaluateAndValueCheck(
                "=averageIf(A2:A4, \">100\")",
                Maps.of(
                        "A2", "=1", //
                        "A3", "=200", //
                        "A4", "=\"400\"" // string with number converted
                ),
                EXPRESSION_NUMBER_KIND.create(600 / 2)
        );
    }

    @Test
    public void testBase() {
        this.evaluateAndValueCheck(
                "=base(13, 2)",
                "1101"
        );
    }

    @Test
    public void testBin2Dec() {
        this.evaluateAndValueCheck(
                "=bin2dec(\"1101\")",
                "13"
        );
    }

    @Test
    public void testBin2Hex() {
        this.evaluateAndValueCheck(
                "=bin2hex(\"1101\")",
                "d"
        );
    }

    @Test
    public void testBin2Oct() {
        this.evaluateAndValueCheck(
                "=bin2oct(\"1001\")",
                "11"
        );
    }

    @Test
    public void testBitAnd() {
        this.evaluateAndValueCheck(
                "=bitand(14, 7)",
                EXPRESSION_NUMBER_KIND.create(6)
        );
    }

    @Test
    public void testBitOr() {
        this.evaluateAndValueCheck(
                "=bitor(3, 6)",
                EXPRESSION_NUMBER_KIND.create(7)
        );
    }

    @Test
    public void testBitXor() {
        this.evaluateAndValueCheck(
                "=bitxor(7, 3)",
                EXPRESSION_NUMBER_KIND.create(4)
        );
    }

    @Test
    public void testCeil() {
        this.evaluateAndValueCheck(
                "=ceil(1.75)",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testCell() {
        this.evaluateAndValueCheck(
                "=cell(\"address\", B2)",
                Maps.of("b2", "=1*2"),
                SpreadsheetSelection.parseCell("B2")
        );
    }

    @Test
    public void testChar65() {
        this.evaluateAndValueCheck(
                "=char(65)",
                'A'
        );
    }

    @Test
    public void testChooseFirst() {
        this.evaluateAndValueCheck(
                "=choose(1, 111, 222, 333)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testChooseSecond() {
        this.evaluateAndValueCheck(
                "=choose(2, 111, 222, 333)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testChooseThird() {
        this.evaluateAndValueCheck(
                "=choose(3, 111, true(), \"Third\")",
                "Third"
        );
    }

    @Test
    public void testClean() {
        this.evaluateAndValueCheck(
                "=clean(\"\t\nNeeds cleaning \r\")",
                "Needs cleaning "
        );
    }

    @Test
    public void testCodeCapitalA() {
        this.evaluateAndValueCheck(
                "=code(\"A\")",
                EXPRESSION_NUMBER_KIND.create(65)
        );
    }

    @Test
    public void testColumn() {
        this.evaluateAndValueCheck(
                "=column(C1)",
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testColumns() {
        this.evaluateAndValueCheck(
                "=columns(Z99)",
                EXPRESSION_NUMBER_KIND.create(1)
        );
    }

    @Test
    public void testColumnsWithRange() {
        this.evaluateAndValueCheck(
                "=columns(B1:D1)",
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testConcatNumber() {
        this.evaluateAndValueCheck(
                "=concat(1.25)",
                this.metadataWithStrangeNumberFormatPattern(),
                "1.25"
        );
    }

    @Test
    public void testConcatString() {
        this.evaluateAndValueCheck(
                "=concat(\"abc\")",
                this.metadataWithStrangeNumberFormatPattern(),
                "abc"
        );
    }

    @Test
    public void testConcatSingleValues() {
        this.evaluateAndValueCheck(
                "=concat(A2,A3)",
                Maps.of("A2", "'abc", "A3", "'123"),
                this.metadataWithStrangeNumberFormatPattern(),
                "abc123"
        );
    }

    @Test
    public void testConcatRange() {
        this.evaluateAndValueCheck(
                "=concat(A2:A3)",
                Maps.of("A2", "'abc", "A3", "'123"),
                this.metadataWithStrangeNumberFormatPattern(),
                "abc123"
        );
    }

    @Test
    public void testConcatRangeIncludesNumbers() {
        this.evaluateAndValueCheck(
                "=concat(A2:A3)",
                Maps.of("A2", "'abc", "A3", "=123"),
                this.metadataWithStrangeNumberFormatPattern(),
                "abc123"
        );
    }

    @Test
    public void testConcatRangeMissingCell() {
        this.evaluateAndValueCheck(
                "=concat(A2:A5)",
                Maps.of("A2", "'abc", "A4", "'123"),
                this.metadataWithStrangeNumberFormatPattern(),
                "abc123"
        );
    }

    @Test
    public void testConcatSingleValuesAndRange() {
        this.evaluateAndValueCheck(
                "=concat(\"First\",A2:A3,\"!!!\",B1:B2)",
                Maps.of(
                        "A2", "'abc",
                        "A3", "'123",
                        "B1", "'SecondLast",
                        "B2", "'Last"
                ),
                this.metadataWithStrangeNumberFormatPattern(),
                "Firstabc123!!!SecondLastLast"
        );
    }

    @Test
    public void testCos() {
        this.evaluateAndValueCheck(
                "=cos(1)",
                EXPRESSION_NUMBER_KIND.create(0.5403023)
        );
    }

    @Test
    public void testCountWithDate() {
        this.evaluateAndValueCheck(
                "=count(date(1999, 12, 31))",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountWithDateTime() {
        this.evaluateAndValueCheck(
                "=count(now())",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountWithNumber() {
        this.evaluateAndValueCheck(
                "=count(1)",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountWithString() {
        this.evaluateAndValueCheck(
                "=count(\"abc\")",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testCountWithStringConvertible() {
        this.evaluateAndValueCheck(
                "=count(\"123\")",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testCountWithTime() {
        this.evaluateAndValueCheck(
                "=count(time(12, 58, 59))",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountWithRangeOfNumbers() {
        this.evaluateAndValueCheck(
                "=count(A2:A4)",
                Maps.of(
                        "A2", "=1",
                        "A3", "=20",
                        "A4", "=300"
                ),
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testCountWithRangeIncludesEmptyCells() {
        this.evaluateAndValueCheck(
                "=count(A2:A4)",
                Maps.of(
                        "A2", "=1",
                        "A4", "=300"
                ),
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testCountWithRangeIncludesStrings() {
        this.evaluateAndValueCheck(
                "=count(A2:A4)",
                Maps.of(
                        "A2", "=1",
                        "A3", "=\"2\"", // string not converted
                        "A4", "=\"abc\""
                ),
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountWithRangeMixed() {
        this.evaluateAndValueCheck(
                "=count(A2:A7)",
                Maps.of(
                        "A2", "=1", // 1
                        "A3", "=today()", // 2 LocalDate
                        "A4", "=now()", // 3 LocalDateTime
                        "A5", "=\"2\"", // string with number - doesnt count
                        "A6", "=\"abc\""
                ),
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testCountAEmptyString() {
        this.evaluateAndValueCheck(
                "=countA(\"\")",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountAMissingCell() {
        this.evaluateAndValueCheck(
                "=countA(Z99)", // becomes a #NAME which is ignored
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testCountARangeOfMixedValues() {
        this.evaluateAndValueCheck(
                "=countA(A2:A8)",
                Maps.of(
                        "A2", "=1", // 1
                        "A3", "=today()", // 2 LocalDate
                        "A4", "=now()", // 3 LocalDateTime
                        "A5", "=\"2\"", // 4 string with number
                        "A6", "=\"abc\"", // 5
                        "A7", "\"\"" // 6
                ),
                EXPRESSION_NUMBER_KIND.create(6)
        );
    }

    @Test
    public void testCountBlankEmptyString() {
        this.evaluateAndValueCheck(
                "=countBlank(\"\")",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testCountBlankNotEmptyString() {
        this.evaluateAndValueCheck(
                "=countBlank(\"not-empty\")",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testCountBlankMissingCell() {
        this.evaluateAndValueCheck(
                "=countBlank(Z99)", // becomes a #REF which counts as a non empty cell
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountBlankRangeOfMixedValues() {
        this.evaluateAndValueCheck(
                "=countBlank(A2:A8)",
                Maps.of(
                        "A2", "=1", //
                        "A3", "=today()", // LocalDate
                        "A4", "=now()", // LocalDateTime
                        "A5", "=\"2\"", // string with number
                        "A6", "=\"abc\"", //
                        "A8", "=\"\"" // not blank
                ),
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountIfOne() {
        this.evaluateAndValueCheck(
                "=countIf(123, 123)",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testCountIfZero() {
        this.evaluateAndValueCheck(
                "=countIf(123, 456)",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testCountIfSomeValuesFiltered() {
        this.evaluateAndValueCheck(
                "=countIf(A2:A5, \">99+1\")",
                Maps.of(
                        "A2", "=1", //
                        "A3", "=2", //
                        "A4", "=now()", // will be > 100
                        "A5", "=\"200\"" // string are ignored
                ),
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testDate() {
        this.evaluateAndValueCheck(
                "=date(1999, 12, 31)",
                LocalDate.of(1999, 12, 31)
        );
    }

    @Test
    public void testDay() {
        this.evaluateAndValueCheck(
                "=day(date(1999, 12, 31))",
                EXPRESSION_NUMBER_KIND.create(31)
        );
    }

    @Test
    public void testDays() {
        this.evaluateAndValueCheck(
                "=days(date(2000, 1, 28), date(1999, 12, 31))",
                EXPRESSION_NUMBER_KIND.create(28)
        );
    }

    @Test
    public void testDec2BinFromString() {
        this.evaluateAndValueCheck(
                "=dec2bin(\"14\")",
                "1110"
        );
    }

    @Test
    public void testDec2BinFromNumber() {
        this.evaluateAndValueCheck(
                "=dec2bin(14)",
                "1110"
        );
    }

    @Test
    public void testDec2Hex() {
        this.evaluateAndValueCheck(
                "=dec2hex(\"255\")",
                "ff"
        );
    }

    @Test
    public void testDec2Oct() {
        this.evaluateAndValueCheck(
                "=dec2oct(\"255\")",
                "377"
        );
    }

    @Test
    public void testDegrees() {
        this.evaluateAndValueCheck(
                "=degrees(1.5)",
                EXPRESSION_NUMBER_KIND.create(85.943655)
        );
    }

    @Test
    public void testDecimal() {
        this.evaluateAndValueCheck(
                "=decimal(\"11\", 2)",
                EXPRESSION_NUMBER_KIND.create(3)
        );
    }

    @Test
    public void testDeltaNumbersEquals() {
        this.evaluateAndValueCheck(
                "=delta(1.25, \"1.25\")",
                true
        );
    }

    @Test
    public void testDollarWithNumberAndMissingDecimals() {
        this.evaluateAndValueCheck(
                "=dollar(123.4567)",
                "$123.46"
        );
    }

    @Test
    public void testDollarWithStringAndMissingDecimals() {
        this.evaluateAndValueCheck(
                "=dollar(\"123.4567\")",
                "$123.46"
        );
    }

    @Test
    public void testDollarWithNumberAndPlus2Decimals() {
        this.evaluateAndValueCheck(
                "=dollar(123.4567, 2)",
                "$123.46"
        );
    }

    @Test
    public void testDollarWithNumberAndMinus2Decimals() {
        this.evaluateAndValueCheck(
                "=dollar(123.4567, -2)",
                "$100"
        );
    }

    @Test
    public void testE() {
        this.evaluateAndValueCheck(
                "=e()",
                EXPRESSION_NUMBER_KIND.create(2.718282)
        );
    }

    @Test
    public void testEven() {
        this.evaluateAndValueCheck(
                "=even(1.7)",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testExactDifferentCaseStrings() {
        this.evaluateAndValueCheck(
                "=exact(\"ABC\", \"abc\")",
                false
        );
    }

    @Test
    public void testExactSameStrings() {
        this.evaluateAndValueCheck(
                "=exact(\"ABC\", \"ABC\")",
                true
        );
    }

    @Test
    public void testExactSameString2() {
        this.evaluateAndValueCheck(
                "=exact(\"12.5\",12.5)",
                true
        );
    }

    @Test
    public void testExp() {
        this.evaluateAndValueCheck(
                "=exp(1)",
                EXPRESSION_NUMBER_KIND.create(2.718282)
        );
    }

    @Test
    public void testFalse() {
        this.evaluateAndValueCheck(
                "=false()",
                Boolean.FALSE
        );
    }

    @Test
    public void testFindFound() {
        this.evaluateAndValueCheck(
                "=find(\"abc\", \"before abc\")",
                EXPRESSION_NUMBER_KIND.create(1 + "before ".length())
        );
    }

    @Test
    public void testFindNotFound() {
        this.evaluateAndValueCheck(
                "=find(\"Not found\", \"123\")",
                SpreadsheetErrorKind.VALUE.setMessage("\"Not found\" not found in \"123\"")
        );
    }

    @Test
    public void testFixedWithNumber() {
        this.evaluateAndValueCheck(
                "=fixed(123.456)",
                "123.46"
        );
    }

    @Test
    public void testFixedWithNumberAndDecimals() {
        this.evaluateAndValueCheck(
                "=fixed(123.567, 1)",
                "123.6"
        );
    }

    @Test
    public void testFixedWithNumberAndDecimalsAndCommas() {
        this.evaluateAndValueCheck(
                "=fixed(1234.567, 1, false())",
                "1,234.6"
        );
    }

    @Test
    public void testFloor() {
        this.evaluateAndValueCheck(
                "=floor(1.8)",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testFormulaText() {
        this.evaluateAndValueCheck(
                "=formulatext(A2)",
                Maps.of("A2", "=1+2+3"),
                "=1+2+3"
        );
    }

    @Test
    public void testHex2Bin() {
        this.evaluateAndValueCheck(
                "=hex2bin(\"f\")",
                "1111"
        );
    }

    @Test
    public void testHex2Dec() {
        this.evaluateAndValueCheck(
                "=hex2dec(\"ff\")",
                "255"
        );
    }

    @Test
    public void testHex2Oct() {
        this.evaluateAndValueCheck(
                "=hex2oct(\"ff\")",
                "377"
        );
    }

    @Test
    public void testHour() {
        this.evaluateAndValueCheck(
                "=hour(time(12, 58, 59))",
                EXPRESSION_NUMBER_KIND.create(12)
        );
    }

    @Test
    public void testIfTrue() {
        this.evaluateAndValueCheck(
                "=if(true(), 111, 222)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testIfTrueCaseInsensitiveStringCompare() {
        this.evaluateAndValueCheck(
                "=if(\"abc\" = \"ABC\", 111, 222)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testIfTrueCaseInsensitiveStringCompareDifferent() {
        this.evaluateAndValueCheck(
                "=if(\"abc\" = \"different\", 111, 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIfFalse() {
        this.evaluateAndValueCheck(
                "=if(false(), 111, 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIfsFirst() {
        this.evaluateAndValueCheck(
                "=ifs(true(), 111, false(), 222)",
                EXPRESSION_NUMBER_KIND.create(111)
        );
    }

    @Test
    public void testIfsSecond() {
        this.evaluateAndValueCheck(
                "=ifs(\"abc\"=\"different\", 111, true(), 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIfsSecondStringCaseInsensitiveEquals() {
        this.evaluateAndValueCheck(
                "=ifs(\"abc\"=\"different\", 111, \"same\"=\"SAME\", 222)",
                EXPRESSION_NUMBER_KIND.create(222)
        );
    }

    @Test
    public void testIndirect() {
        this.evaluateAndValueCheck(
                "=indirect(\"Z99\")",
                SpreadsheetSelection.parseCell("Z99")
        );
    }

    @Test
    public void testInt() {
        this.evaluateAndValueCheck(
                "=int(1.8)",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testIsBlankNoCell() {
        this.evaluateAndValueCheck(
                "=isBlank(B2)",
                true
        );
    }

    @Test
    public void testIsBlankCell() {
        this.evaluateAndValueCheck(
                "=isBlank(B2)",
                Maps.of("B2", "'NotBlank"),
                false
        );
    }

    @Test
    public void testIsDateWithDate() {
        this.evaluateAndValueCheck(
                "=isDate(today())",
                true
        );
    }

    @Test
    public void testIsDateWithNumber() {
        this.evaluateAndValueCheck(
                "=isDate(1)",
                true
        );
    }

    @Test
    public void testIsDateWithString() {
        this.evaluateAndValueCheck(
                "=isDate(\"31/12/2000\")",
                true
        );
    }

    @Test
    public void testIsDateWithTime() {
        this.evaluateAndValueCheck(
                "=isDate(time(1,1,1))",
                true
        );
    }

    @Test
    public void testIsErrWithError() {
        this.evaluateAndValueCheck(
                "=isErr(1/0)",
                true
        );
    }

    @Test
    public void testIsErrWithErrorRef() {
        this.evaluateAndValueCheck(
                "=isErr(#REF!)",
                true
        );
    }

    @Test
    public void testIsErrWithNumber() {
        this.evaluateAndValueCheck(
                "=isErr(123)",
                false
        );
    }

    @Test
    public void testIsErrorWithErroror() {
        this.evaluateAndValueCheck(
                "=isError(1/0)",
                true
        );
    }

    @Test
    public void testIsErrorWithNumber() {
        this.evaluateAndValueCheck(
                "=isError(123)",
                false
        );
    }

    @Test
    public void testIsEvenWithEvenNumber() {
        this.evaluateAndValueCheck(
                "=isEven(2)",
                true
        );
    }

    @Test
    public void testIsEvenWithOddNumber() {
        this.evaluateAndValueCheck(
                "=isEven(1)",
                false
        );
    }

    @Test
    public void testIsFormulaWithCellWithFormula() {
        this.evaluateAndValueCheck(
                "=isFormula(B2)",
                Maps.of("B2", "=1"),
                true
        );
    }

    @Test
    public void testIsFormulaWithMissingCell() {
        this.evaluateAndValueCheck(
                "=isFormula(B2)",
                false
        );
    }

    @Test
    public void testIsLogicalWithBooleanTrue() {
        this.evaluateAndValueCheck(
                "=isLogical(true())",
                true
        );
    }

    @Test
    public void testIsLogicalWithBooleanFalse() {
        this.evaluateAndValueCheck(
                "=isLogical(false())",
                true
        );
    }

    @Test
    public void testIsLogicalWithMissingCell() {
        this.evaluateAndValueCheck(
                "=isLogical(B2)",
                false
        );
    }

    @Test
    public void testIsLogicalWithNumber() {
        this.evaluateAndValueCheck(
                "=isLogical(123)",
                false
        );
    }

    @Test
    public void testIsLogicalWithString() {
        this.evaluateAndValueCheck(
                "=isLogical(\"abc\")",
                false
        );
    }

    @Test
    public void testIsNaWithError() {
        this.evaluateAndValueCheck(
                "=isNa(1/0)",
                false
        );
    }

    @Test
    public void testIsNaWithNumber() {
        this.evaluateAndValueCheck(
                "=isNa(123)",
                false
        );
    }

    @Test
    public void testIsNonTextWithEmptyCell() {
        this.evaluateAndValueCheck(
                "=isNonText(Z99)",
                true
        );
    }

    @Test
    public void testIsNonTextWithError() {
        this.evaluateAndValueCheck(
                "=isNonText(1/0)",
                true
        );
    }

    @Test
    public void testIsNonTextWithString() {
        this.evaluateAndValueCheck(
                "=isNonText(\"abc\")",
                false
        );
    }

    @Test
    public void testIsNumberWithNonNumberString() {
        this.evaluateAndValueCheck(
                "=isNumber(\"ABC\")",
                false
        );
    }

    @Test
    public void testIsNumberWithNumber() {
        this.evaluateAndValueCheck(
                "=isNumber(2)",
                true
        );
    }

    @Test
    public void testIsOddWithEvenNumber() {
        this.evaluateAndValueCheck(
                "=isOdd(2)",
                false
        );
    }

    @Test
    public void testIsOddWithOddNumber() {
        this.evaluateAndValueCheck(
                "=isOdd(1)",
                true
        );
    }

    @Test
    public void testIsoWeekNum() {
        this.evaluateAndValueCheck(
                "=isoWeekNum(date(1999,12,31))",
                EXPRESSION_NUMBER_KIND.create(52)
        );
    }

    @Test
    public void testIsTextWithError() {
        this.evaluateAndValueCheck(
                "=isText(1/0)",
                false
        );
    }

    @Test
    public void testIsTextWithNumber() {
        this.evaluateAndValueCheck(
                "=isText(123)",
                false
        );
    }

    @Test
    public void testIsTextWithEmptyString() {
        this.evaluateAndValueCheck(
                "=isText(\"\")",
                true
        );
    }

    @Test
    public void testIsTextWithString() {
        this.evaluateAndValueCheck(
                "=isText(\"abc\")",
                true
        );
    }

    @Test
    public void testLambdaWithParameters() {
        this.evaluateAndValueCheck(
                "=lambda(x,y,x*y)(10,20)",
                EXPRESSION_NUMBER_KIND.create(10 * 20)
        );
    }

    @Test
    public void testLAMBDAWithParameters() {
        this.evaluateAndValueCheck(
                "=LAMBDA(x,y,z,x*y*z)(20,30, 40)",
                EXPRESSION_NUMBER_KIND.create(20 * 30 * 40)
        );
    }

    @Test
    public void testLambdaWithParametersAndCellReference() {
        this.evaluateAndValueCheck(
                "=lambda(x,y,x*y*b2)(10,20)",
                Maps.of("b2", "30"),
                EXPRESSION_NUMBER_KIND.create(10 * 20 * 30)
        );
    }

    // https://www.microsoft.com/en-us/research/blog/lambda-the-ultimatae-excel-worksheet-function/

    @Test
    public void testLambdaWithParametersLet() {
        this.evaluateAndValueCheck(
                "=lambda(x,y,    Let(xs, x*x, ys, y*y, xs+ys))(3, 4)",
                EXPRESSION_NUMBER_KIND.create(25)
        );
    }

    @Test
    public void testLambdaWithParametersLet2() {
        this.evaluateAndValueCheck(
                "=lambda(x,y,    Let(xs, x*x, ys, y*y, sqrt(xs+ys)))(3, 4)",
                EXPRESSION_NUMBER_KIND.create(5)
        );
    }

    @Test
    public void testLeftMissingCellReference() {
        this.evaluateAndValueCheck(
                "=left(Z99)",
                SpreadsheetErrorKind.VALUE.setMessage(
                        "Failed to convert SpreadsheetError to String"
                )
        );
    }

    @Test
    public void testLeft() {
        this.evaluateAndValueCheck(
                "=left(\"abc\")",
                "a"
        );
    }

    @Test
    public void testLeft2() {
        this.evaluateAndValueCheck(
                "=left(\"abc\", 2)",
                "ab"
        );
    }

    @Test
    public void testLenWithNumber() {
        this.evaluateAndValueCheck(
                "=len(1.23)",
                this.metadataWithStrangeNumberFormatPattern(),
                EXPRESSION_NUMBER_KIND.create(4)
        );
    }

    @Test
    public void testLenWithString() {
        this.evaluateAndValueCheck(
                "=len(\"hello\")",
                this.metadataWithStrangeNumberFormatPattern(),
                EXPRESSION_NUMBER_KIND.create(5)
        );
    }

    @Test
    public void testLetOnlyStringLiteral() {
        this.evaluateAndValueCheck(
                "=let(\"hello\")",
                "hello"
        );
    }

    @Test
    public void testLetWithBackReferences() {
        this.evaluateAndValueCheck(
                "=let(x, 2, x * 3)",
                EXPRESSION_NUMBER_KIND.create(2 * 3)
        );
    }

    @Test
    public void testLetWithBackReferences2() {
        this.evaluateAndValueCheck(
                "=let(x, 2, y, 3, x * y * x * y)",
                EXPRESSION_NUMBER_KIND.create(2 * 3 * 2 * 3)
        );
    }

    @Test
    public void testLn() {
        this.evaluateAndValueCheck(
                "=ln(2)",
                EXPRESSION_NUMBER_KIND.create(0.6931472)
        );
    }

    @Test
    public void testLog() {
        this.evaluateAndValueCheck(
                "=log(3, 2)",
                EXPRESSION_NUMBER_KIND.create(1.584962)
        );
    }

    @Test
    public void testLog10() {
        this.evaluateAndValueCheck(
                "=log10(100)",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testLowerWithNumber() {
        this.evaluateAndValueCheck(
                "=lower(1.25)",
                this.metadataWithStrangeNumberFormatPattern(),
                "1.25"
        );
    }

    @Test
    public void testLowerWithString() {
        this.evaluateAndValueCheck(
                "=lower(\"ABCxyz\")",
                this.metadataWithStrangeNumberFormatPattern(),
                "abcxyz"
        );
    }

    @Test
    public void testMax() {
        this.evaluateAndValueCheck(
                "=max(1,20,300,B1:D1)",
                Maps.of(
                        "B1", "1000",
                        "C1", "2000",
                        "D1", "9999"
                ),
                EXPRESSION_NUMBER_KIND.create(9999)
        );
    }

    @Test
    public void testMaxIf() {
        this.evaluateAndValueCheck(
                "=maxIf(A2:A4, \">=19\")",
                Maps.of(
                        "A2", "=1",
                        "A3", "=20",
                        "A4", "=\"400\""
                ),
                EXPRESSION_NUMBER_KIND.create(400)
        );
    }

    @Test
    public void testMid() {
        this.evaluateAndValueCheck(
                "=mid(\"apple\", 2, 3)",
                "ppl"
        );
    }

    @Test
    public void testMin() {
        this.evaluateAndValueCheck(
                "=min(1,20,300,B1:D1)",
                Maps.of(
                        "B1", "1000",
                        "C1", "2000",
                        "D1", "-999"
                ),
                EXPRESSION_NUMBER_KIND.create(-999)
        );
    }

    @Test
    public void testMinIf() {
        this.evaluateAndValueCheck(
                "=minIf(A2:A4, \">=19\")",
                Maps.of(
                        "A2", "=1",
                        "A3", "=20",
                        "A4", "=\"400\""
                ),
                EXPRESSION_NUMBER_KIND.create(20)
        );
    }

    @Test
    public void testMinute() {
        this.evaluateAndValueCheck(
                "=minute(time(12, 58, 59))",
                EXPRESSION_NUMBER_KIND.create(58)
        );
    }

    @Test
    public void testMod() {
        this.evaluateAndValueCheck(
                "=mod(5, 3)",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testMonth() {
        this.evaluateAndValueCheck(
                "=month(date(1999, 12, 31))",
                EXPRESSION_NUMBER_KIND.create(12)
        );
    }

    @Test
    public void testNotFalse() {
        this.evaluateAndValueCheck(
                "=not(false())",
                Boolean.TRUE
        );
    }

    @Test
    public void testNotTrue() {
        this.evaluateAndValueCheck(
                "=not(true())",
                Boolean.FALSE
        );
    }

    @Test
    public void testNotZero() {
        this.evaluateAndValueCheck(
                "=not(0)",
                Boolean.TRUE
        );
    }


    @Test
    public void testNow() {
        this.evaluateAndValueCheck(
                "=now()",
                NOW.get()
        );
    }

    @Test
    public void testNumberValue() {
        this.evaluateAndValueCheck(
                "=numberValue(\"1G234D5\", \"D\", \"G\")",
                EXPRESSION_NUMBER_KIND.create(1234.5)
        );
    }

    @Test
    public void testOct2Bin() {
        this.evaluateAndValueCheck(
                "=oct2bin(\"34\")",
                "11100"
        );
    }

    @Test
    public void testOct2Dec() {
        this.evaluateAndValueCheck(
                "=oct2dec(\"34\")",
                "28"
        );
    }

    @Test
    public void testOct2Hex() {
        this.evaluateAndValueCheck(
                "=oct2hex(\"34\")",
                "1c"
        );
    }

    @Test
    public void testOdd() {
        this.evaluateAndValueCheck(
                "=odd(12.3)",
                EXPRESSION_NUMBER_KIND.create(13)
        );
    }

    @Test
    public void testOffset() {
        this.evaluateAndValueCheck(
                "=offset(B2,1,2,3,3)",
                SpreadsheetSelection.parseCellRange("D3:F5" )
        );
    }

    @Test
    public void testOrTrueTrueTrue() {
        this.evaluateAndValueCheck(
                "=or(true(), true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testOrFalseFalseTrue() {
        this.evaluateAndValueCheck(
                "=or(false(), false(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testOrFalseFalseFalseFalse() {
        this.evaluateAndValueCheck(
                "=or(false(), false(), false(), false())",
                Boolean.FALSE
        );
    }

    @Test
    public void testPi() {
        this.evaluateAndValueCheck(
                "=pi()",
                EXPRESSION_NUMBER_KIND.create(3.141593)
        );
    }

    @Test
    public void testProduct() {
        this.evaluateAndValueCheck(
                "=product(2, 5)",
                EXPRESSION_NUMBER_KIND.create(10)
        );
    }

    @Test
    public void testProper() {
        this.evaluateAndValueCheck(
                "=proper(\"apple\")",
                "Apple"
        );
    }

    @Test
    public void testProper2() {
        this.evaluateAndValueCheck(
                "=proper(\"apple, pears\")",
                "Apple, Pears"
        );
    }

    @Test
    public void testQuotient() {
        this.evaluateAndValueCheck(
                "=quotient(12, 3)",
                EXPRESSION_NUMBER_KIND.create(4)
        );
    }

    @Test
    public void testRadians() {
        this.evaluateAndValueCheck(
                "=radians(90)",
                EXPRESSION_NUMBER_KIND.create(1.5707961)
        );
    }

    @Test
    public void testRand() {
        this.evaluateAndValueCheck(
                "=rand() > 0",
                true
        );
    }

    @Test
    public void testRandBetween() {
        this.evaluateAndValueCheck(
                "=randBetween(2, 34) >= 2",
                true
        );
    }

    @Test
    public void testReplace() {
        this.evaluateAndValueCheck(
                "=replace(\"XYZ123\",4,3,\"456\")",
                "XYZ456"
        );
    }

    @Test
    public void testRept() {
        this.evaluateAndValueCheck(
                "=rept(\"abc\", 3)",
                "abcabcabc"
        );
    }

    @Test
    public void testRight() {
        this.evaluateAndValueCheck(
                "=right(\"abc\")",
                "c"
        );
    }

    @Test
    public void testRight2() {
        this.evaluateAndValueCheck(
                "=right(\"abc\", 2)",
                "bc"
        );
    }

    @Test
    public void testRoman() {
        this.evaluateAndValueCheck(
                "=roman(123)",
                "CXXIII"
        );
    }

    @Test
    public void testRound() {
        this.evaluateAndValueCheck(
                "=round(5.7845, 1)",
                EXPRESSION_NUMBER_KIND.create(5.8)
        );
    }

    @Test
    public void testRoundDown() {
        this.evaluateAndValueCheck(
                "=roundDown(1.25, 1)",
                EXPRESSION_NUMBER_KIND.create(1.2)
        );
    }

    @Test
    public void testRoundUp() {
        this.evaluateAndValueCheck(
                "=roundUp(1.25, 1)",
                EXPRESSION_NUMBER_KIND.create(1.3)
        );
    }

    @Test
    public void testRow() {
        this.evaluateAndValueCheck(
                "=row(A99)",
                EXPRESSION_NUMBER_KIND.create(99)
        );
    }

    @Test
    public void testRowsWithCell() {
        this.evaluateAndValueCheck(
                "=rows(Z99)",
                EXPRESSION_NUMBER_KIND.create(1)
        );
    }

    @Test
    public void testRowsWithRange() {
        this.evaluateAndValueCheck(
                "=rows(B1:D1)",
                EXPRESSION_NUMBER_KIND.create(1)
        );
    }

    @Test
    public void testRowsWithRange2() {
        this.evaluateAndValueCheck(
                "=rows(B3:D6)",
                EXPRESSION_NUMBER_KIND.create(4)
        );
    }

    @Test
    public void testSearchCaseWithInsensitiveFound() {
        this.evaluateAndValueCheck(
                "=search(\"bc\", \"ABCDE\")",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testSearchCaseWithSensitiveFound() {
        this.evaluateAndValueCheck(
                "=search(\"bc\", \"abcde\")",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testSearchCaseWithQuestionMark() {
        this.evaluateAndValueCheck(
                "=search(\"1?3\", \"before 123 after\")",
                EXPRESSION_NUMBER_KIND.create(1 + "before ".length())
        );
    }

    @Test
    public void testSearchCaseWithQuestionMark2() {
        this.evaluateAndValueCheck(
                "=search(\"1?3\", \"before 111 123 after\")",
                EXPRESSION_NUMBER_KIND.create(1 + "before 111 ".length())
        );
    }

    @Test
    public void testSearchCaseWithWildcard() {
        this.evaluateAndValueCheck(
                "=search(\"1*4\", \"before 1234 after\")",
                EXPRESSION_NUMBER_KIND.create(1 + "before ".length())
        );
    }

    @Test
    public void testSearchNotFound() {
        this.evaluateAndValueCheck(
                "=search(\"!\", \"abcde\")",
                EXPRESSION_NUMBER_KIND.create(0)
        );
    }

    @Test
    public void testSecond() {
        this.evaluateAndValueCheck(
                "=second(time(12, 58, 59))",
                EXPRESSION_NUMBER_KIND.create(59)
        );
    }

    @Test
    public void testSignWithNegativeNumber() {
        this.evaluateAndValueCheck(
                "=sign(-123)",
                EXPRESSION_NUMBER_KIND.create(-1)
        );
    }

    @Test
    public void testSignWithZero() {
        this.evaluateAndValueCheck(
                "=sign(0)",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testSignWithPositiveNumber() {
        this.evaluateAndValueCheck(
                "=sign(+123)",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testSin() {
        this.evaluateAndValueCheck(
                "=sin(1)",
                EXPRESSION_NUMBER_KIND.create(0.841471)
        );
    }

    @Test
    public void testSinh() {
        this.evaluateAndValueCheck(
                "=sinh(1)",
                EXPRESSION_NUMBER_KIND.create(1.175201)
        );
    }

    @Test
    public void testSqrtWithNegativeNumber() {
        this.evaluateAndValueCheck(
                "=sqrt(-1)",
                SpreadsheetErrorKind.VALUE.setMessage("Illegal sqrt(x) for x < 0: x = -1")
        );
    }

    @Test
    public void testSqrtWithPositiveNumber() {
        this.evaluateAndValueCheck(
                "=sqrt(100)",
                EXPRESSION_NUMBER_KIND.create(10)
        );
    }

    @Test
    public void testSubstitute() {
        this.evaluateAndValueCheck(
                "=substitute(\"123-456-7890\",\"-\",\"\") ",
                "1234567890"
        );
    }

    @Test
    public void testSum() {
        this.evaluateAndValueCheck(
                "=sum(1,20,300,B1:D1)",
                Maps.of(
                        "B1", "1000",
                        "C1", "2000",
                        "D1", "9999"
                ),
                EXPRESSION_NUMBER_KIND.create(13320)
        );
    }

    @Test
    public void testSumMissingCell() {
        this.evaluateAndValueCheck(
                "=sum(B2)",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testSumMissingCell2() {
        this.evaluateAndValueCheck(
                "=sum(123+B2)",
                EXPRESSION_NUMBER_KIND.create(123)
        );
    }

    @Test
    public void testSumMissingCellRange() {
        this.evaluateAndValueCheck(
                "=sum(B2:B3)",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testSumMissingCellRange2() {
        this.evaluateAndValueCheck(
                "=sum(B2:B3,123)",
                EXPRESSION_NUMBER_KIND.create(123)
        );
    }

    @Test
    public void testSumMissingCellRange3() {
        this.evaluateAndValueCheck(
                "=sum(123,B2:B3)",
                Maps.of(
                        "B2", "1000"
                ),
                EXPRESSION_NUMBER_KIND.create(123 + 1000)
        );
    }

    @Test
    public void testSumIfOne() {
        this.evaluateAndValueCheck(
                "=sumIf(123, 123)",
                EXPRESSION_NUMBER_KIND.create(123)
        );
    }

    @Test
    public void testSumIfZero() {
        this.evaluateAndValueCheck(
                "=sumIf(123, 456)",
                EXPRESSION_NUMBER_KIND.zero()
        );
    }

    @Test
    public void testSumIfSomeValuesFiltered() {
        this.evaluateAndValueCheck(
                "=sumIf(A2:A4, \">100\")",
                Maps.of(
                        "A2", "=1", //
                        "A3", "=200", //
                        "A4", "=\"400\"" // string with number converted
                ),
                EXPRESSION_NUMBER_KIND.create(600)
        );
    }

    @Test
    public void testSwitchFirst() {
        this.evaluateAndValueCheck(
                "=switch(1, 1, \"One\", 2, \"Two\", 3, 333)",
                "One"
        );
    }

    @Test
    public void testSwitchSecond() {
        this.evaluateAndValueCheck(
                "=switch(\"TWO22\", 1, \"One\", \"Two22\", \"Two\", 3, 333, \"switch-default\")",
                "Two"
        );
    }

    @Test
    public void testSwitchDefaults() {
        this.evaluateAndValueCheck(
                "=switch(999, 1, \"One\", 22, \"Two\", 3, 333, \"switch-default\")",
                "switch-default"
        );
    }

    @Test
    public void testTan() {
        this.evaluateAndValueCheck(
                "=tan(2)",
                EXPRESSION_NUMBER_KIND.create(-2.18504)
        );
    }

    @Test
    public void testTanh() {
        this.evaluateAndValueCheck(
                "=tanh(2)",
                EXPRESSION_NUMBER_KIND.create(0.9640276)
        );
    }

    @Test
    public void testTextWithDate() {
        this.evaluateAndValueCheck(
                "=text(date(1999,12,31), \"yyyy mm dd\")",
                "1999 12 31"
        );
    }

    @Test
    public void testTextWithDateTime() {
        this.evaluateAndValueCheck(
                "=text(now(), \"yyyy mm dd hh mm ss\")",
                "1999 12 31 12 58 59"
        );
    }

    @Test
    public void testTextWithNumber() {
        this.evaluateAndValueCheck(
                "=text(123.5, \"$0000.0000$\")",
                "$0123.5000$"
        );
    }

    @Test
    public void testTextWithString() {
        this.evaluateAndValueCheck(
                "=text(\"abc\", \"Ignored-pattern\")",
                "abc"
        );
    }

    @Test
    public void testTextWithTime() {
        this.evaluateAndValueCheck(
                "=text(time(12,58,59), \"ss hh mm\")",
                "59 12 58"
        );
    }

    @Test
    public void testTextJoin() {
        this.evaluateAndFormattedCheck(
                "=textJoin(\",\", true(), \"a\", \"b\", \"\", \"d\")",
                TextNode.text("a,b,da,b,d")
        );
    }

    @Test
    public void testTime() {
        this.evaluateAndValueCheck(
                "=time(12, 58, 59)",
                LocalTime.of(12, 58, 59)
        );
    }

    @Test
    public void testTWithText() {
        this.evaluateAndValueCheck(
                "=t(\"abc123\")",
                "abc123"
        );
    }

    @Test
    public void testToday() {
        this.evaluateAndValueCheck(
                "=today()",
                NOW.get().toLocalDate()
        );
    }

    @Test
    public void testTrim() {
        this.evaluateAndValueCheck(
                "=trim(\"  a  b  c  \")",
                "a b c"
        );
    }

    @Test
    public void testTrue() {
        this.evaluateAndValueCheck(
                "=true()",
                Boolean.TRUE
        );
    }

    @Test
    public void testTrue2() {
        this.evaluateAndValueCheck(
                "=true()",
                Maps.of("A2", "=true()"),
                Boolean.TRUE
        );
    }

    @Test
    public void testTrunc() {
        this.evaluateAndValueCheck(
                "=trunc(999.999,1)",
                EXPRESSION_NUMBER_KIND.create(999.9)
        );
    }

    @Test
    public void testTruncWithNegativePlaces() {
        this.evaluateAndValueCheck(
                "=trunc(999.999,-2)",
                EXPRESSION_NUMBER_KIND.create(900)
        );
    }

    @Test
    public void testTypeWithNumber() {
        this.evaluateAndValueCheck(
                "=type(123)",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testTypeWithDate() {
        this.evaluateAndValueCheck(
                "=type(date(2000, 1, 1))",
                EXPRESSION_NUMBER_KIND.one()
        );
    }

    @Test
    public void testTypeWithErrorDivByZero() {
        this.evaluateAndValueCheck(
                "=type(1/0)",
                EXPRESSION_NUMBER_KIND.create(16)
        );
    }

    @Test
    public void testUnichar97() {
        this.evaluateAndValueCheck(
                "=unichar(97)",
                'a'
        );
    }

    @Test
    public void testUnichar1000() {
        this.evaluateAndValueCheck(
                "=unichar(1000)",
                Character.valueOf((char) 1000)
        );
    }

    @Test
    public void testUnicodeA() {
        this.evaluateAndValueCheck(
                "=unicode(\"A\")",
                EXPRESSION_NUMBER_KIND.create((int) 'A')
        );
    }

    @Test
    public void testUnicodeChar1000() {
        final char c = 1000;
        this.evaluateAndValueCheck(
                "=unicode(\"" + c + "\")",
                EXPRESSION_NUMBER_KIND.create((int) c)
        );
    }

    @Test
    public void testUpperWithNumber() {
        this.evaluateAndValueCheck(
                "=upper(1.25)",
                this.metadataWithStrangeNumberFormatPattern(),
                "1.25"
        );
    }


    @Test
    public void testUpperWithString() {
        this.evaluateAndValueCheck(
                "=upper(\"ABCxyz\")",
                this.metadataWithStrangeNumberFormatPattern(),
                "ABCXYZ"
        );
    }

    @Test
    public void testValueWithNumber() {
        this.evaluateAndValueCheck(
                "=value(123)",
                EXPRESSION_NUMBER_KIND.create(123)
        );
    }

    @Test
    public void testValueWithString() {
        this.evaluateAndValueCheck(
                "=value(\"123\")",
                EXPRESSION_NUMBER_KIND.create(123)
        );
    }

    @Test
    public void testValueWithInvalidString() {
        this.evaluateAndValueCheck(
                "=value(\"abc\")",
                SpreadsheetErrorKind.VALUE.setMessage("Failed to convert SpreadsheetError to ExpressionNumber")
        );
    }

    @Test
    public void testWeekday() {
        this.evaluateAndValueCheck(
                "=weekday(date(2022, 5, 12))",
                EXPRESSION_NUMBER_KIND.create(5)
        );
    }

    @Test
    public void testWeeknum() {
        this.evaluateAndValueCheck(
                "=weeknum(date(2000, 2, 1))",
                EXPRESSION_NUMBER_KIND.create(6)
        );
    }

    @Test
    public void testYear() {
        this.evaluateAndValueCheck(
                "=year(date(1999, 12, 31))",
                EXPRESSION_NUMBER_KIND.create(1999)
        );
    }

    @Test
    public void testXorTrueTrueTrue() {
        this.evaluateAndValueCheck(
                "=xor(true(), true(), true())",
                Boolean.TRUE
        );
    }

    @Test
    public void testXorFalseFalseTrue() {
        this.evaluateAndValueCheck(
                "=xor(true(), false(), true())",
                Boolean.FALSE
        );
    }

    @Test
    public void testXorFalseFalseFalseFalse() {
        this.evaluateAndValueCheck(
                "=xor(false(), false(), false(), false())",
                Boolean.FALSE
        );
    }

    // evaluateAndCheckValue............................................................................................

    private void evaluateAndValueCheck(final String cellFormula,
                                       final Object expectedValue) {
        this.evaluateAndValueCheck(
                cellFormula,
                Maps.empty(),
                expectedValue
        );
    }

    private void evaluateAndValueCheck(final String cellFormula,
                                       final SpreadsheetMetadata metadata,
                                       final Object expectedValue) {
        this.evaluateAndValueCheck(
                cellFormula,
                Maps.empty(),
                metadata,
                expectedValue
        );
    }

    private void evaluateAndValueCheck(final String cellFormula,
                                       final Map<String, String> preload,
                                       final Object expectedValue) {
        this.evaluateAndValueCheck(
                cellFormula,
                preload,
                this.metadata(),
                expectedValue
        );
    }

    private void evaluateAndValueCheck(final String cellFormula,
                                       final Map<String, String> preload,
                                       final SpreadsheetMetadata metadata,
                                       final Object expectedValue) {
        this.evaluateAndCheck(
                SpreadsheetSelection.parseCell("A1"),
                cellFormula,
                preload,
                metadata,
                Optional.ofNullable(expectedValue),
                null // not checking formatted
        );
    }

    private void evaluateAndFormattedCheck(final String cellFormula,
                                           final TextNode expectedValue) {
        this.evaluateAndFormattedCheck(
                cellFormula,
                Maps.empty(),
                expectedValue
        );
    }

    private void evaluateAndFormattedCheck(final String cellFormula,
                                           final Map<String, String> preload,
                                           final TextNode expectedFormatted) {
        this.evaluateAndCheck(
                SpreadsheetSelection.parseCell("A1"),
                cellFormula,
                preload,
                null, // no value
                Optional.ofNullable(expectedFormatted)
        );
    }

    private void evaluateAndCheck(final SpreadsheetCellReference cellReference,
                                  final String cellFormula,
                                  final Map<String, String> preload,
                                  final Optional<?> expectedValue,
                                  final Optional<TextNode> formatted) {
        this.evaluateAndCheck(
                cellReference,
                cellFormula,
                preload,
                this.metadata(),
                expectedValue,
                formatted
        );
    }

    private SpreadsheetMetadata metadata() {
        return SpreadsheetMetadata.EMPTY
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
                .set(SpreadsheetMetadataPropertyName.GENERAL_NUMBER_FORMAT_DIGIT_COUNT, SpreadsheetFormatterContext.DEFAULT_GENERAL_FORMAT_NUMBER_DIGIT_COUNT)
                .set(SpreadsheetMetadataPropertyName.PRECISION, MathContext.DECIMAL32.getPrecision())
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetPattern.parseNumberFormatPattern("#.###"))
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@@"))
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20)
                .set(
                        SpreadsheetMetadataPropertyName.STYLE,
                        TextStyle.EMPTY.set(TextStylePropertyName.WIDTH, Length.pixel(50.0))
                                .set(TextStylePropertyName.HEIGHT, Length.pixel(50.0))
                );
    }

    private SpreadsheetMetadata metadataWithStrangeNumberFormatPattern() {
        return this.metadata()
                .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetPattern.parseNumberFormatPattern("\"Number:\"#.###"));
    }

    private void evaluateAndCheck(final SpreadsheetCellReference cellReference,
                                  final String cellFormula,
                                  final Map<String, String> preload,
                                  final SpreadsheetMetadata metadata,
                                  final Optional<?> expectedValue,
                                  final Optional<TextNode> formatted) {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();

        final Map<String, ExpressionFunction<?, SpreadsheetExpressionEvaluationContext>> nameToFunctions = Maps.sorted(String.CASE_INSENSITIVE_ORDER);
        SpreadsheetServerExpressionFunctions.visit(
                (f -> nameToFunctions.put(
                        f.name()
                                .get()
                                .value(),
                        f
                )
                )
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
                SpreadsheetComparators.nameToSpreadsheetComparator(),
                (n) -> {
                    Objects.requireNonNull(n, "name");
                    final ExpressionFunction<?, ?> function = nameToFunctions.get(n.value());
                    if (null == function) {
                        throw new UnknownExpressionFunctionException(n);
                    }
                    return Cast.to(function);
                },
                engine,
                (b) -> {
                    throw new UnsupportedOperationException();
                },
                repo,
                SERVER_URL,
                NOW
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

        if (null != formatted) {
            this.checkEquals(
                    formatted,
                    saved.formattedValue(),
                    cellReference + "=" + cellFormula + "\n" +
                            preload.entrySet().stream()
                                    .map(e -> e.getKey() + "=" + e.getValue())
                                    .collect(Collectors.joining("\n"))
            );
        }

        if (null != expectedValue) {
            this.checkEquals(
                    expectedValue,
                    saved.formula()
                            .value(),
                    cellReference + "=" + cellFormula + "\n" +
                            preload.entrySet().stream()
                                    .map(e -> e.getKey() + "=" + e.getValue())
                                    .collect(Collectors.joining("\n"))
            );
        }
    }

    // isPure..........................................................................................................

    // NOW()
    // TODAY()
    // RAND()
    // RANDBETWEEN()
    // OFFSET()
    // INDIRECT()
    // CELL() // depends on arguments
    // INFO() // depends on arguments
    @Test
    public void testIsPure() {
        final SpreadsheetExpressionEvaluationContext context = SpreadsheetExpressionEvaluationContexts.fake();

        final List<ExpressionFunction<?, SpreadsheetExpressionEvaluationContext>> functions = Lists.array();
        SpreadsheetServerExpressionFunctions.visit(functions::add);

        final List<ExpressionFunction<?, SpreadsheetExpressionEvaluationContext>> pureFunctions = Lists.array();

        functions.forEach(
                f -> {
                    final boolean pure;

                    final FunctionExpressionName name = f.name()
                            .get();

                    switch (name.value().toLowerCase()) {
                        case "now":
                        case "today":
                        case "rand":
                        case "randbetween":
                        case "offset":
                        case "cell":
                        case "info":
                            pure = false;
                            break;
                        default:
                            pure = true;
                            break;
                    }

                    if (f.isPure(context) != pure) {
                        pureFunctions.add(f);
                    }
                });

        this.checkEquals(
                Lists.empty(),
                pureFunctions,
                () -> "functions"
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
