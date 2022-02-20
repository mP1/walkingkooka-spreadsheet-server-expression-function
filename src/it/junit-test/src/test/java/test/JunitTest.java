/*
 * Copyright © 2020 Miroslav Pokorny
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
 */
package test;


import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Assert;
import org.junit.Test;

import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.expression.function.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.function.FakeSpreadsheetExpressionFunctionContext;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.expression.ExpressionNumberKind;

@J2clTestInput(JunitTest.class)
public class JunitTest {

    @Test
    public void testColumnE1() {
        final ExpressionNumberKind kind = ExpressionNumberKind.DOUBLE;

        Assert.assertEquals(
                kind.create(5),
                SpreadsheetExpressionFunctions.column()
                        .apply(
                                Lists.of(SpreadsheetSelection.parseCell("E1")),
                                new FakeSpreadsheetExpressionFunctionContext() {

                                    @Override
                                    public ExpressionNumberKind expressionNumberKind() {
                                        return kind;
                                    }
                                }
                        )
        );
    }
}
