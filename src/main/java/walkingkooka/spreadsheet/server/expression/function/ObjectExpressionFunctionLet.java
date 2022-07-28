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
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.tree.expression.ExpressionPurityContext;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterName;

import java.util.List;

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
    public boolean isPure(final ExpressionPurityContext context) {
        return true; // the function itself is pure depending on the parameter values themselves.
    }

    @Override
    public Object apply(final List<Object> values,
                        final SpreadsheetExpressionEvaluationContext context) {

        final Object value;

        final int count = values.size();
        switch (count) {
            case 0:
                throw new IllegalArgumentException(MISSING_COMPUTED_VALUE_EXPRESSION);
            case 1:
                value = COMPUTED.getOrFail(values, 0);
                break;
            default:
                if (count % 2 == 0) {
                    throw new IllegalArgumentException(MISSING_COMPUTED_VALUE_EXPRESSION);
                }
                value = this.apply0(
                        values,
                        context
                );
                break;
        }

        return value;
    }

    /**
     * This message is used when the let function is executed with no values.
     */
    private final static String MISSING_COMPUTED_VALUE_EXPRESSION = "Missing final computed value/expression";  // TODO verify actual error

    private Object apply0(final List<Object> values,
                          final SpreadsheetExpressionEvaluationContext context) {
        final int count = values.size();
        final int labelAndValuePairCount = count / 2;
        final ObjectExpressionFunctionLetNameAndValue[] nameAndValues = new ObjectExpressionFunctionLetNameAndValue[labelAndValuePairCount];

        int valueIndex = 0;

        for (int labelAndValueIndex = 0; labelAndValueIndex < labelAndValuePairCount; labelAndValueIndex++) {
            final SpreadsheetLabelName name = LABEL_NAME.getOrFail(values, valueIndex++);
            if (name.value().indexOf('.') >= 0) {
                throw new IllegalArgumentException("Illegal name \"" + name + "\" contains dot.");
            }

            // check previously declared named values for duplicates.
            for (int i = 0; i < labelAndValueIndex; i++) {
                if (nameAndValues[i].name.equals(name)) {
                    throw new IllegalArgumentException("Duplicate name \"" + name + "\" in value " + valueIndex); // first parameter is called 1
                }
            }

            // evaluate parameter value when it is referenced.
            nameAndValues[labelAndValueIndex] = ObjectExpressionFunctionLetNameAndValue.with(
                    name,
                    LABEL_VALUE.getOrFail(values, valueIndex++)
            );
        }

        // now create the context with the given labels and values.
        final ObjectExpressionFunctionLetSpreadsheetExpressionEvaluationContext context2 = ObjectExpressionFunctionLetSpreadsheetExpressionEvaluationContext.with(
                nameAndValues,
                context
        );

        return context2.evaluateIfNecessary(
                COMPUTED.getOrFail(
                        context2.prepareParameters(
                                this,
                                values
                        ),
                        count - 1
                )
        );
    }

    /**
     * Given the count assembles the parameters with the correct parameter names and types.
     */
    @Override
    public List<ExpressionFunctionParameter<?>> parameters(final int count) {
        List<ExpressionFunctionParameter<?>> parameters;

        switch (count) {
            case 0:
            case 1:
                parameters = COMPUTED_PARAMETERS;
                break;
            default:
                parameters = this.parameters0(count/2*2+1);
                break;
        }

        return parameters;
    }

    private final ExpressionFunctionParameter<Object> COMPUTED = ExpressionFunctionParameterName.with("computed")
            .required(Object.class)
            .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);

    private final List<ExpressionFunctionParameter<?>> COMPUTED_PARAMETERS = Lists.of(COMPUTED);

    private List<ExpressionFunctionParameter<?>> parameters0(final int count) {
        if (count % 2 == 0) {
            throw new IllegalArgumentException(MISSING_COMPUTED_VALUE_EXPRESSION);
        }
        final ExpressionFunctionParameter<?>[] parameters =  new ExpressionFunctionParameter<?>[count];

        int i = 0;
        int j = 0;

        while( i + 1 < count ) {
            parameters[i++] = LABEL_NAME.setName(ExpressionFunctionParameterName.with("label-" + j));
            parameters[i++] = LABEL_VALUE.setName(ExpressionFunctionParameterName.with("value-" + j));

            j++;
        }

        parameters[count - 1] = COMPUTED;

        return Lists.of(parameters);
    }

    // declaring both LABEL_NAME and LABEL_VALUE will mean these parameters will appear as required in the UI.

    private final ExpressionFunctionParameter<SpreadsheetLabelName> LABEL_NAME = ExpressionFunctionParameterName.with("label")
            .required(SpreadsheetLabelName.class)
            .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE);

    private final ExpressionFunctionParameter<Object> LABEL_VALUE = ExpressionFunctionParameterName.with("value")
            .required(Object.class)
            .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);
}
