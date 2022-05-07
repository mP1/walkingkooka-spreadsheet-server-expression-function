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
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionKind;
import walkingkooka.tree.expression.function.booleann.BooleanExpressionFunctions;
import walkingkooka.tree.expression.function.string.StringExpressionFunctions;

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
                address(),
                and(),
                charFunction(),
                choose(),
                clean(),
                code(),
                column(),
                columns(),
                concat(),
                falseFunction(),
                formulaText(),
                ifFunction(),
                ifs(),
                isBlank(),
                isErr(),
                isError(),
                isNa(),
                left(),
                len(),
                lower(),
                mid(),
                not(),
                or(),
                right(),
                row(),
                rows(),
                switchFunction(),
                trueFunction(),
                unichar(),
                unicode(),
                upper(),
                xor()
        ).forEach(functions::accept);
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
     * {@see BooleanExpressionFunctions#falseFunction}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> falseFunction() {
        return BooleanExpressionFunctions.falseFunction();
    }

    /**
     * {@see SpreadsheetExpressionFunctions#formulaText}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> formulaText() {
        return SpreadsheetExpressionFunctions.formulaText();
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
     * {@see SpreadsheetExpressionFunctions#isNa}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> isNa() {
        return SpreadsheetExpressionFunctions.isNa();
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
     * {@see BooleanExpressionFunctions#not}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> not() {
        return BooleanExpressionFunctions.not();
    }

    /**
     * {@see BooleanExpressionFunctions#or}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> or() {
        return BooleanExpressionFunctions.or();
    }

    /**
     * {@see StringExpressionFunctions#right}
     */
    public static ExpressionFunction<String, SpreadsheetExpressionFunctionContext> right() {
        return StringExpressionFunctions.right();
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
     * {@see BooleanExpressionFunctions#switchFunction}
     */
    public static ExpressionFunction<Object, SpreadsheetExpressionFunctionContext> switchFunction() {
        return BooleanExpressionFunctions.switchFunction();
    }
    
    /**
     * {@see BooleanExpressionFunctions#trueFunction}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> trueFunction() {
        return BooleanExpressionFunctions.<SpreadsheetExpressionFunctionContext>trueFunction();
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
