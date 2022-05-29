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
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.ExpressionFunction;

import java.util.List;
import java.util.stream.Collectors;

public abstract class NumberExpressionFunctionTestCase<F extends ExpressionFunction<ExpressionNumber, SpreadsheetExpressionEvaluationContext>> extends ExpressionFunctionTestCase<F, ExpressionNumber> {

    NumberExpressionFunctionTestCase() {
        super();
    }

    @Override
    public final void applyAndCheck2(final F function,
                                     final List<Object> parameters,
                                     final ExpressionNumber result) {
        this.applyAndCheck2(
                function,
                parameters.stream()
                        .map(i -> EXPRESSION_NUMBER_KIND.create((Number) i))
                        .collect(Collectors.toList()),
                this.createContext(),
                result
        );
    }

    @Override
    public final SpreadsheetExpressionEvaluationContext createContext() {
        return this.createContext(EXPRESSION_NUMBER_KIND);
    }

    abstract SpreadsheetExpressionEvaluationContext createContext(final ExpressionNumberKind kind);

    @Override
    public final String typeNamePrefix() {
        return NumberExpressionFunction.class.getSimpleName();
    }
}
