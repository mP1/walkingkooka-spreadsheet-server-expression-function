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

import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.expression.function.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.function.SpreadsheetExpressionFunctionContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionKind;
import walkingkooka.tree.expression.function.booleann.BooleanExpressionFunctions;
import walkingkooka.tree.expression.function.datetime.DateTimeExpressionFunctions;
import walkingkooka.tree.expression.function.engineering.EngineeringExpressionFunctions;
import walkingkooka.tree.expression.function.number.NumberExpressionFunctions;
import walkingkooka.tree.expression.function.number.trigonometry.NumberTrigonomteryExpressionFunctions;
import walkingkooka.tree.expression.function.string.StringExpressionFunctions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

/**
 * Collection of static factory methods for numerous {@link ExpressionFunction}.
 */
public final class SpreadsheetServerExpressionFunctions implements PublicStaticHelper {

    /**
     * Visit all {@link ExpressionFunction functions}.
     */
    public static void visit(final Consumer<ExpressionFunction<?, SpreadsheetExpressionFunctionContext>> functions) {
        Lists.of(
                abs(),
                acos(),
                address(),
                and(),
                asin(),
                atan(),
                base(),
                bin2dec(),
                bin2hex(),
                bin2oct(),
                bitAnd(),
                bitOr(),
                bitXor(),
                ceil(),
                cell(),
                charFunction(),
                choose(),
                clean(),
                code(),
                column(),
                columns(),
                concat(),
                cos(),
                date(),
                day(),
                days(),
                decimal(),
                dec2bin(),
                dec2hex(),
                dec2oct(),
                degrees(),
                e(),
                even(),
                falseFunction(),
                floor(),
                formulaText(),
                hex2bin(),
                hex2dec(),
                hex2oct(),
                hour(),
                ifFunction(),
                ifs(),
                intFunction(),
                isBlank(),
                isDate(),
                isErr(),
                isError(),
                isEven(),
                isNa(),
                isNonText(),
                isNumber(),
                isOdd(),
                isoWeekNum(),
                isText(),
                left(),
                len(),
                ln(),
                log(),
                log10(),
                lower(),
                mid(),
                minute(),
                mod(),
                month(),
                not(),
                odd(),
                offset(),
                or(),
                pi(),
                product(),
                proper(),
                quotient(),
                radians(),
                rand(),
                randBetween(),
                replace(),
                rept(),
                right(),
                roman(),
                roundDown(),
                roundUp(),
                row(),
                rows(),
                search(),
                second(),
                sign(),
                sin(),
                sinh(),
                sqrt(),
                substitute(),
                switchFunction(),
                t(),
                tan(),
                tanh(),
                text(),
                textJoin(),
                time(),
                today(),
                trim(),
                trueFunction(),
                trunc(),
                unichar(),
                unicode(),
                upper(),
                weekDay(),
                weekNum(),
                year(),
                xor()
        ).forEach(functions::accept);
    }

    /**
     * {@see NumberExpressionFunctions#abs}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> abs() {
        return NumberExpressionFunctions.abs();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#acos}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> acos() {
        return NumberTrigonomteryExpressionFunctions.acos();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#address}
     */
    public static ExpressionFunction<SpreadsheetCellReference, SpreadsheetExpressionFunctionContext> address() {
        return SpreadsheetExpressionFunctions.address();
    }

    /**
     * {@see BooleanExpressionFunctions#and}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> and() {
        return BooleanExpressionFunctions.and();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#asin}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> asin() {
        return NumberTrigonomteryExpressionFunctions.asin();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#atan}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> atan() {
        return NumberTrigonomteryExpressionFunctions.atan();
    }

    /**
     * {@see NumberExpressionFunctions#base}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> base() {
        return NumberExpressionFunctions.base();
    }

    /**
     * {@see EngineeringExpressionFunctions#bin2dec}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> bin2dec() {
        return EngineeringExpressionFunctions.bin2dec();
    }

    /**
     * {@see EngineeringExpressionFunctions#bin2hex}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> bin2hex() {
        return EngineeringExpressionFunctions.bin2hex();
    }

    /**
     * {@see EngineeringExpressionFunctions#bin2oct}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> bin2oct() {
        return EngineeringExpressionFunctions.bin2oct();
    }

    /**
     * {@see EngineeringExpressionFunctions#bitAnd}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> bitAnd() {
        return BITAND;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> BITAND = EngineeringExpressionFunctions.<SpreadsheetExpressionFunctionContext>bitAnd()
            .setName(FunctionExpressionName.with("bitAnd"));

    /**
     * {@see EngineeringExpressionFunctions#bitOr}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> bitOr() {
        return BITOR;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> BITOR = EngineeringExpressionFunctions.<SpreadsheetExpressionFunctionContext>bitOr()
            .setName(FunctionExpressionName.with("bitOr"));

    /**
     * {@see EngineeringExpressionFunctions#bitXor}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> bitXor() {
        return BitXor;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> BitXor = EngineeringExpressionFunctions.<SpreadsheetExpressionFunctionContext>bitXor()
            .setName(FunctionExpressionName.with("bitXor"));

    /**
     * {@see NumberExpressionFunctions#ceil}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> ceil() {
        return NumberExpressionFunctions.ceil();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#cell}
     */
    public static ExpressionFunction<Object, SpreadsheetExpressionFunctionContext> cell() {
        return SpreadsheetExpressionFunctions.cell();
    }

    /**
     * {@see StringExpressionFunctions#ascii}
     */
    public static ExpressionFunction<Character, SpreadsheetExpressionFunctionContext> charFunction() {
        return CHAR_FUNCTION;
    }

    private final static ExpressionFunction<Character, SpreadsheetExpressionFunctionContext> CHAR_FUNCTION = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>ascii()
            .setName(FunctionExpressionName.with("char"));

    /**
     * {@see BooleanExpressionFunctions#choose}
     */
    public static ExpressionFunction<Object, SpreadsheetExpressionFunctionContext> choose() {
        return BooleanExpressionFunctions.choose();
    }

    /**
     * {@see StringExpressionFunctions#clean}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> clean() {
        return StringExpressionFunctions.clean();
    }

    /**
     * {@see StringExpressionFunctions#unicode}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> code() {
        return CODE;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> CODE = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>unicode()
            .setName(FunctionExpressionName.with("code"));

    /**
     * {@see SpreadsheetExpressionFunctions#column}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> column() {
        return SpreadsheetExpressionFunctions.column();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#columns}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> columns() {
        return SpreadsheetExpressionFunctions.columns();
    }
    
    /**
     * {@see StringExpressionFunctions#concat}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> concat() {
        return CONCAT;
    }

    private final static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> CONCAT = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>concat()
            .setKinds(
                    Sets.of(
                            ExpressionFunctionKind.FLATTEN,
                            ExpressionFunctionKind.EVALUATE_PARAMETERS,
                            ExpressionFunctionKind.RESOLVE_REFERENCES
                    )
            );

    /**
     * {@see NumberTrigonomteryExpressionFunctions#cos}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> cos() {
        return NumberTrigonomteryExpressionFunctions.cos();
    }

    /**
     * {@see DateTimeExpressionFunctions#date}
     */
    public static ExpressionFunction<LocalDate, SpreadsheetExpressionFunctionContext> date() {
        return DateTimeExpressionFunctions.date();
    }

    /**
     * {@see DateTimeExpressionFunctions#day}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> day() {
        return DateTimeExpressionFunctions.day();
    }

    /**
     * {@see DateTimeExpressionFunctions#days}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> days() {
        return DateTimeExpressionFunctions.days();
    }

    /**
     * {@see EngineeringExpressionFunctions#dec2bin}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> dec2bin() {
        return EngineeringExpressionFunctions.dec2bin();
    }

    /**
     * {@see EngineeringExpressionFunctions#dec2hex}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> dec2hex() {
        return EngineeringExpressionFunctions.dec2hex();
    }

    /**
     * {@see EngineeringExpressionFunctions#dec2oct}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> dec2oct() {
        return EngineeringExpressionFunctions.dec2oct();
    }

    /**
     * {@see NumberExpressionFunctions#decimal}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> decimal() {
        return NumberExpressionFunctions.decimal();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#degrees}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> degrees() {
        return NumberTrigonomteryExpressionFunctions.degrees();
    }

    /**
     * {@see NumberExpressionFunctions#e}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> e() {
        return NumberExpressionFunctions.e();
    }

    /**
     * {@see NumberExpressionFunctions#even}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> even() {
        return NumberExpressionFunctions.even();
    }

    /**
     * {@see BooleanExpressionFunctions#falseFunction}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> falseFunction() {
        return BooleanExpressionFunctions.falseFunction();
    }

    /**
     * {@see NumberExpressionFunctions#floor}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> floor() {
        return NumberExpressionFunctions.floor();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#formulaText}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> formulaText() {
        return SpreadsheetExpressionFunctions.formulaText();
    }

    /**
     * {@see EngineeringExpressionFunctions#hex2bin}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> hex2bin() {
        return EngineeringExpressionFunctions.hex2bin();
    }

    /**
     * {@see EngineeringExpressionFunctions#hex2dec}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> hex2dec() {
        return EngineeringExpressionFunctions.hex2dec();
    }

    /**
     * {@see EngineeringExpressionFunctions#hex2oct}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> hex2oct() {
        return EngineeringExpressionFunctions.hex2oct();
    }

    /**
     * {@see DateTimeExpressionFunctions#hour}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> hour() {
        return DateTimeExpressionFunctions.hour();
    }

    /**
     * {@see BooleanExpressionFunctions#ifFunction}
     */
    public static ExpressionFunction<Object, SpreadsheetExpressionFunctionContext> ifFunction() {
        return BooleanExpressionFunctions.ifFunction();
    }

    /**
     * {@see BooleanExpressionFunctions#ifs}
     */
    public static ExpressionFunction<Object, SpreadsheetExpressionFunctionContext> ifs() {
        return BooleanExpressionFunctions.ifs();
    }
    
    /**
     * {@see NumberExpressionFunctions#intFunction}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> intFunction() {
        return NumberExpressionFunctions.intFunction();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#isBlank}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isBlank() {
        return IS_BLANK;
    }

    private final static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> IS_BLANK = SpreadsheetExpressionFunctions.isBlank()
            .setKinds(
                    Sets.of(
                            ExpressionFunctionKind.EVALUATE_PARAMETERS
                    )
            );

    /**
     * {@see DateTimeExpressionFunctions#isDate}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isDate() {
        return DateTimeExpressionFunctions.isDate();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#isErr}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isErr() {
        return SpreadsheetExpressionFunctions.isErr();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#isError}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isError() {
        return SpreadsheetExpressionFunctions.isError();
    }

    /**
     * {@see NumberExpressionFunctions#isEven}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isEven() {
        return NumberExpressionFunctions.isEven();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#isNa}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isNa() {
        return SpreadsheetExpressionFunctions.isNa();
    }

    /**
     * {@see StringExpressionFunctions#isNonText}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isNonText() {
        return StringExpressionFunctions.isNonText();
    }

    /**
     * {@see NumberExpressionFunctions#isNumber}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isNumber() {
        return NumberExpressionFunctions.isNumber();
    }

    /**
     * {@see NumberExpressionFunctions#isOdd}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isOdd() {
        return NumberExpressionFunctions.isOdd();
    }
    
    /**
     * {@see DateTimeExpressionFunctions.isoWeekNum}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> isoWeekNum() {
        return DateTimeExpressionFunctions.isoWeekNum();
    }

    /**
     * {@see StringExpressionFunctions#isText}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isText() {
        return StringExpressionFunctions.isText();
    }

    /**
     * {@see StringExpressionFunctions#left}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> left() {
        return StringExpressionFunctions.left();
    }

    /**
     * {@see StringExpressionFunctions#stringLength}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> len() {
        return LEN;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> LEN = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>stringLength()
            .setName(FunctionExpressionName.with("len"));

    /**
     * {@see NumberExpressionFunctions#ln}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> ln() {
        return NumberExpressionFunctions.ln();
    }
    
    /**
     * {@see NumberExpressionFunctions#log}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> log() {
        return NumberExpressionFunctions.log();
    }

    /**
     * {@see NumberExpressionFunctions#log10}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> log10() {
        return NumberExpressionFunctions.log10();
    }
    
    /**
     * {@see StringExpressionFunctions#lower}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> lower() {
        return LOWER;
    }

    private final static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> LOWER = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>lowerCase()
            .setName(FunctionExpressionName.with("lower"));

    /**
     * {@see StringExpressionFunctions#mid}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> mid() {
        return StringExpressionFunctions.mid();
    }

    /**
     * {@see DateTimeExpressionFunctions#minute}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> minute() {
        return DateTimeExpressionFunctions.minute();
    }

    /**
     * {@see NumberExpressionFunctions#mod}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> mod() {
        return NumberExpressionFunctions.mod();
    }

    /**
     * {@see DateTimeExpressionFunctions#month}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> month() {
        return DateTimeExpressionFunctions.month();
    }

    /**
     * {@see BooleanExpressionFunctions#not}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> not() {
        return BooleanExpressionFunctions.not();
    }

    /**
     * {@see NumberExpressionFunctions#odd}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> odd() {
        return NumberExpressionFunctions.odd();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#offset}
     */
    public static ExpressionFunction<SpreadsheetExpressionReference, SpreadsheetExpressionFunctionContext> offset() {
        return SpreadsheetExpressionFunctions.offset();
    }

    /**
     * {@see BooleanExpressionFunctions#or}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> or() {
        return BooleanExpressionFunctions.or();
    }

    /**
     * {@see NumberExpressionFunctions#pi}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> pi() {
        return NumberExpressionFunctions.pi();
    }

    /**
     * {@see NumberExpressionFunctions#product}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> product() {
        return NumberExpressionFunctions.product();
    }

    /**
     * {@see StringExpressionFunctions#proper}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> proper() {
        return StringExpressionFunctions.proper();
    }

    /**
     * {@see NumberExpressionFunctions#quotient}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> quotient() {
        return NumberExpressionFunctions.quotient();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#radians}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> radians() {
        return NumberTrigonomteryExpressionFunctions.radians();
    }
    
    /**
     * {@see NumberExpressionFunctions#random}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> rand() {
        return RAND;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> RAND = NumberExpressionFunctions.<SpreadsheetExpressionFunctionContext>random()
            .setName(FunctionExpressionName.with("rand"));

    /**
     * {@see NumberExpressionFunctions#randomBetween}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> randBetween() {
        return RANDBETWEEN;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> RANDBETWEEN = NumberExpressionFunctions.<SpreadsheetExpressionFunctionContext>randomBetween()
            .setName(FunctionExpressionName.with("randBetween"));
    
    /**
     * {@see StringExpressionFunctions#replace}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> replace() {
        return StringExpressionFunctions.replace();
    }

    /**
     * {@see StringExpressionFunctions#rept}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> rept() {
        return REPT;
    }

    private final static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> REPT = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>repeat()
            .setName(FunctionExpressionName.with("rept"));

    /**
     * {@see StringExpressionFunctions#right}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> right() {
        return StringExpressionFunctions.right();
    }

    /**
     * {@see NumberExpressionFunctions#roman}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> roman() {
        return NumberExpressionFunctions.roman();
    }

    /**
     * {@see NumberExpressionFunctions#roundDown}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> roundDown() {
        return NumberExpressionFunctions.roundDown();
    }

    /**
     * {@see NumberExpressionFunctions#roundUp}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> roundUp() {
        return NumberExpressionFunctions.roundUp();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#row}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> row() {
        return SpreadsheetExpressionFunctions.row();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#rows}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> rows() {
        return SpreadsheetExpressionFunctions.rows();
    }

    /**
     * {@see StringExpressionFunctions#searchCaseInsensitive}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> search() {
        return SEARCH;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> SEARCH = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>searchCaseInsensitive()
            .setName(FunctionExpressionName.with("search"));

    /**
     * {@see DateTimeExpressionFunctions#second}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> second() {
        return DateTimeExpressionFunctions.second();
    }

    /**
     * {@see NumberExpressionFunctions#sign}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> sign() {
        return NumberExpressionFunctions.sign();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#sin}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> sin() {
        return NumberTrigonomteryExpressionFunctions.sin();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#sinh}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> sinh() {
        return NumberTrigonomteryExpressionFunctions.sinh();
    }

    /**
     * {@see NumberExpressionFunctions#sqrt}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> sqrt() {
        return NumberExpressionFunctions.sqrt();
    }
    
    /**
     * {@see StringExpressionFunctions#substitute}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> substitute() {
        return StringExpressionFunctions.substitute();
    }

    /**
     * {@see BooleanExpressionFunctions#switchFunction}
     */
    public static ExpressionFunction<Object, SpreadsheetExpressionFunctionContext> switchFunction() {
        return BooleanExpressionFunctions.switchFunction();
    }

    /**
     * {@see StringExpressionFunctions#t}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> t() {
        return StringExpressionFunctions.t();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#tan}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> tan() {
        return NumberTrigonomteryExpressionFunctions.tan();
    }

    /**
     * {@see NumberTrigonomteryExpressionFunctions#tanh}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> tanh() {
        return NumberTrigonomteryExpressionFunctions.tanh();
    }

    /**
     * {@see StringExpressionFunctions#text}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> text() {
        return StringExpressionFunctions.text();
    }

    /**
     * {@see StringExpressionFunctions#textJoin}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> textJoin() {
        return TEXTJOIN;
    }

    private final static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> TEXTJOIN = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>textJoin()
            .setName(FunctionExpressionName.with("textJoin"));

    /**
     * {@see DateTimeExpressionFunctions#time}
     */
    public static ExpressionFunction<LocalTime, SpreadsheetExpressionFunctionContext> time() {
        return DateTimeExpressionFunctions.time();
    }

    /**
     * {@see DateTimeExpressionFunctions#today}
     */
    public static ExpressionFunction<LocalDate, SpreadsheetExpressionFunctionContext> today() {
        return DateTimeExpressionFunctions.today();
    }

    /**
     * {@see StringExpressionFunctions#trim}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> trim() {
        return TRIM;
    }

    private final static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> TRIM = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>spaceTrim()
            .setName(FunctionExpressionName.with("trim"));
    
    /**
     * {@see BooleanExpressionFunctions#trueFunction}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> trueFunction() {
        return BooleanExpressionFunctions.<SpreadsheetExpressionFunctionContext>trueFunction();
    }

    /**
     * {@see NumberExpressionFunctions#trunc}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> trunc() {
        return NumberExpressionFunctions.trunc();
    }

    /**
     * {@see StringExpressionFunctions#character}
     */
    public static ExpressionFunction<Character, SpreadsheetExpressionFunctionContext> unichar() {
        return UNICHAR;
    }

    private final static ExpressionFunction<Character, SpreadsheetExpressionFunctionContext> UNICHAR = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>character()
            .setName(FunctionExpressionName.with("unichar"));

    /**
     * {@see StringExpressionFunctions#unicode}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> unicode() {
        return UNICODE;
    }

    private final static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> UNICODE = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>unicode()
            .setName(FunctionExpressionName.with("unicode"));

    /**
     * {@see StringExpressionFunctions#upper}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> upper() {
        return UPPER;
    }

    private final static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> UPPER = StringExpressionFunctions.<SpreadsheetExpressionFunctionContext>upperCase()
            .setName(FunctionExpressionName.with("upper"));

    /**
     * {@see DateTimeExpressionFunctions#weekDay}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> weekDay() {
        return DateTimeExpressionFunctions.weekday();
    }

    /**
     * {@see DateTimeExpressionFunctions#weekNum}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> weekNum() {
        return DateTimeExpressionFunctions.weekNum();
    }
    
    /**
     * {@see DateTimeExpressionFunctions#year}
     */
    public static ExpressionFunction<ExpressionNumber, SpreadsheetExpressionFunctionContext> year() {
        return DateTimeExpressionFunctions.year();
    }
    
    /**
     * {@see BooleanExpressionFunctions#xor}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> xor() {
        return BooleanExpressionFunctions.xor();
    }

    /**
     * Stops creation
     */
    private SpreadsheetServerExpressionFunctions() {
        throw new UnsupportedOperationException();
    }
}
