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
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.function.SpreadsheetExpressionFunctionContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
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
                and(),
                charFunction(),
                choose(),
                falseFunction(),
                ifFunction(),
                ifs(),
                not(),
                or(),
                switchFunction(),
                trueFunction(),
                xor()
        ).forEach(functions::accept);
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
     * {@see BooleanExpressionFunctions#falseFunction}
     */
    public static ExpressionFunction<Boolean, SpreadsheetExpressionFunctionContext> falseFunction() {
        return BooleanExpressionFunctions.falseFunction();
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
