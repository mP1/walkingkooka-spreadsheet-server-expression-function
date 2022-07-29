
/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
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

import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;

import java.util.Optional;

/**
 * Common abstract base class for all Object functions in this package.
 */
abstract class ObjectExpressionFunction implements ExpressionFunction<Object, SpreadsheetExpressionEvaluationContext> {

    ObjectExpressionFunction(final String name) {
        super();
        this.name = Optional.of(
                FunctionExpressionName.with(name)
        );
    }

    @Override
    public final Optional<FunctionExpressionName> name() {
        return this.name;
    }

    private final Optional<FunctionExpressionName> name;

    @Override
    public final Class<Object> returnType() {
        return Object.class;
    }

    @Override
    public final String toString() {
        return this.name()
                .get()
                .toString();
    }
}
