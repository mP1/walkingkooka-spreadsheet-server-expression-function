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
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.expression.function.booleann.BooleanExpressionFunctions;

import java.util.function.Consumer;

/**
 * Collection of static factory methods for numerous {@link ExpressionFunction}.
 */
public final class SpreadsheetServerExpressionFunctions implements PublicStaticHelper {

    /**
     * Visit all {@link ExpressionFunction functions}.
     */
    public static void visit(final Consumer<ExpressionFunction<?, ?>> functions) {
        Lists.of(
                falseFunction(),
                not(),
                trueFunction()
        ).forEach(functions::accept);
    }

    /**
     * {@see BooleanExpressionFunctions#falseFunction}
     */
    public static <C extends ExpressionFunctionContext> ExpressionFunction<Boolean, C> falseFunction() {
        return BooleanExpressionFunctions.falseFunction();
    }

    /**
     * {@see BooleanExpressionFunctions#not}
     */
    public static <C extends ExpressionFunctionContext> ExpressionFunction<Boolean, C> not() {
        return BooleanExpressionFunctions.not();
    }

    /**
     * {@see BooleanExpressionFunctions#trueFunction}
     */
    public static <C extends ExpressionFunctionContext> ExpressionFunction<Boolean, C> trueFunction() {
        return BooleanExpressionFunctions.trueFunction();
    }

    /**
     * Stops creation
     */
    private SpreadsheetServerExpressionFunctions() {
        throw new UnsupportedOperationException();
    }
}
