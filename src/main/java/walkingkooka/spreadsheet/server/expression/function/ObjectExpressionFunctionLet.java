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
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.tree.expression.ExpressionPurityContext;
import walkingkooka.tree.expression.ReferenceExpression;
import walkingkooka.tree.expression.function.ExpressionFunctionKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;

import java.util.List;
import java.util.Set;

/**
 * A let function.
 * <br>
 * Expects an odd number of parameters, the last will be an expression executed which may reference the name and values
 * defined in the pairs of parameters before.
 */
final class ObjectExpressionFunctionLet extends ObjectExpressionFunction {

    /**
     * Singleton
     */
    final static ObjectExpressionFunctionLet INSTANCE = new ObjectExpressionFunctionLet();

    private ObjectExpressionFunctionLet() {
        super("let");
    }

    @Override
    public Set<ExpressionFunctionKind> kinds() {
        return NONE;
    }

    private final static Set<ExpressionFunctionKind> NONE = Sets.empty();

    @Override
    public boolean isPure(final ExpressionPurityContext context) {
        return true; // the function itself is pure depending on the parameter values themselves.
    }

    @Override
    public List<ExpressionFunctionParameter<?>> parameters() {
        return VALUES;
    }

    private final static List<ExpressionFunctionParameter<?>> VALUES = Lists.of(
            ExpressionFunctionParameter.VALUE
    );

    @Override
    public Object apply(final List<Object> values,
                        final SpreadsheetExpressionEvaluationContext context) {
        final Object value;

        final int count = values.size();
        switch (count) {
            case 0:
                throw new IllegalArgumentException("Missing computed value/expression"); // TODO verify actual error
            case 1:
                value = context.evaluateIfNecessary(
                        values.get(0)
                );
                break;
            default:
                if (count % 2 == 0) {
                    throw new IllegalArgumentException("Missing final computed value expression"); // TODO verify actual error
                }
                value = this.apply0(
                        values,
                        context
                );
                break;
        }

        return value;
    }

    private Object apply0(final List<Object> values,
                          final SpreadsheetExpressionEvaluationContext context) {
        final int count = values.size();
        final ObjectExpressionFunctionLetNameAndValue[] nameAndValues = new ObjectExpressionFunctionLetNameAndValue[count / 2];

        int i = 0;
        int j = 0;
        Object computedValue = null; // never actually returns this null.

        while (i < count) {
            final Object nameOrComputedValueExpression = values.get(i);
            i++;

            // nameOrComputedValueExpression must be the computed value
            if (count == i) {
                computedValue = ObjectExpressionFunctionLetSpreadsheetExpressionEvaluationContext.with(
                        nameAndValues,
                        context
                ).evaluateIfNecessary(nameOrComputedValueExpression);
                break;
            }

            // must be a label which is the parameter name...
            // verify label name does not contain a DOT
            final SpreadsheetLabelName name = context.convertOrFail(
                    this.resolveIfReference(nameOrComputedValueExpression),
                    SpreadsheetLabelName.class
            );
            if (name.value().indexOf('.') >= 0) {
                throw new IllegalArgumentException("Illegal name \"" + name + "\" contains dot.");
            }

            // check previously declared named values for duplicates.
            for (int k = 0; k < j; k++) {
                if (nameAndValues[k].name.equals(name)) {
                    throw new IllegalArgumentException("Duplicate name \"" + name + "\" in value " + i); // first parameter is called 1
                }
            }

            // evaluate parameter value when it is referenced.
            nameAndValues[j] = ObjectExpressionFunctionLetNameAndValue.with(
                    name,
                    values.get(i)
            );

            i++;
            j++;
        }

        return computedValue;
    }

    private Object resolveIfReference(final Object value) {
        return value instanceof ReferenceExpression ?
                this.resolveIfReference0((ReferenceExpression) value) :
                value;
    }

    private Object resolveIfReference0(final ReferenceExpression reference) {
        return reference.value();
    }
}
