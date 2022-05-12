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
import walkingkooka.tree.text.TextNode;

import java.lang.reflect.Method;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public void testMathExpressionEvaluationFailureReferenceNotFound() {
        this.evaluateAndValueCheck(
                "=1+A2+A3",
                Maps.of(
                        "A2", "=2"
                ),
                SpreadsheetErrorKind.REF.setMessage("Reference not found: A3")
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
    public void testConcatSingleValues() {
        this.evaluateAndValueCheck(
                "=concat(A2,A3)",
                Maps.of("A2", "'abc", "A3", "'123"),
                "abc123"
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
    public void testConcatRange() {
        this.evaluateAndValueCheck(
                "=concat(A2:A3)",
                Maps.of("A2", "'abc", "A3", "'123"),
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
                "Firstabc123!!!SecondLastLast"
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
    public void testEven() {
        this.evaluateAndValueCheck(
                "=even(1.7)",
                EXPRESSION_NUMBER_KIND.create(2)
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
    public void testLen() {
        this.evaluateAndValueCheck(
                "=len(\"hello\")",
                EXPRESSION_NUMBER_KIND.create(5)
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
    public void testLower() {
        this.evaluateAndValueCheck(
                "=lower(\"ABCxyz\")",
                "abcxyz"
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
    public void testMinute() {
        this.evaluateAndValueCheck(
                "=minute(time(12, 58, 59))",
                EXPRESSION_NUMBER_KIND.create(58)
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
    public void testSearchCaseInsensitiveFound() {
        this.evaluateAndValueCheck(
                "=search(\"bc\", \"ABCDE\")",
                EXPRESSION_NUMBER_KIND.create(2)
        );
    }

    @Test
    public void testSearchCaseSensitiveFound() {
        this.evaluateAndValueCheck(
                "=search(\"bc\", \"abcde\")",
                EXPRESSION_NUMBER_KIND.create(2)
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
    public void testSubstitute() {
        this.evaluateAndValueCheck(
                "=substitute(\"123-456-7890\",\"-\",\"\") ",
                "1234567890"
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
    public void testText() {
        this.evaluateAndFormattedCheck(
                "=text(\"abc\")",
                TextNode.text("abcabc")
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
                "=year(today())",
                EXPRESSION_NUMBER_KIND.create(
                        LocalDateTime.now().getYear()
                )
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
    public void testUpper() {
        this.evaluateAndValueCheck(
                "=upper(\"ABCxyz\")",
                "ABCXYZ"
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
                                       final Map<String, String> preload,
                                       final Object expectedValue) {
        this.evaluateAndCheck(
                SpreadsheetSelection.parseCell("A1"),
                cellFormula,
                preload,
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
                .set(SpreadsheetMetadataPropertyName.PRECISION, MathContext.DECIMAL32.getPrecision())
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@@"))
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

        if (null != formatted) {
            this.checkEquals(
                    formatted,
                    saved.formatted(),
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
