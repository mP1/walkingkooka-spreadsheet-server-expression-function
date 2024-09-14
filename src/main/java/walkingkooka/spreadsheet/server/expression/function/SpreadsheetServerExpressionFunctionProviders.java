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

import walkingkooka.Cast;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.Url;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;

/**
 * Provider getter.
 */
public final class SpreadsheetServerExpressionFunctionProviders implements PublicStaticHelper {

    /**
     * An {@link ExpressionFunctionProvider} with all the functions in this project.
     */
    public static ExpressionFunctionProvider expressionFunctionProvider(final CaseSensitivity nameCaseSensitivity) {
        return ExpressionFunctionProviders.basic(
                Url.parseAbsolute("https://github.com/mP1/walkingkooka-spreadsheet-server-expression-function/"),
                nameCaseSensitivity,
                Cast.to(
                        Sets.of(
                                SpreadsheetServerExpressionFunctions.abs(),
                                SpreadsheetServerExpressionFunctions.acos(),
                                SpreadsheetServerExpressionFunctions.address(),
                                SpreadsheetServerExpressionFunctions.and(),
                                SpreadsheetServerExpressionFunctions.asin(),
                                SpreadsheetServerExpressionFunctions.atan(),
                                SpreadsheetServerExpressionFunctions.average(),
                                SpreadsheetServerExpressionFunctions.averageIf(),
                                SpreadsheetServerExpressionFunctions.base(),
                                SpreadsheetServerExpressionFunctions.bin2dec(),
                                SpreadsheetServerExpressionFunctions.bin2hex(),
                                SpreadsheetServerExpressionFunctions.bin2oct(),
                                SpreadsheetServerExpressionFunctions.bitAnd(),
                                SpreadsheetServerExpressionFunctions.bitOr(),
                                SpreadsheetServerExpressionFunctions.bitXor(),
                                SpreadsheetServerExpressionFunctions.ceil(),
                                SpreadsheetServerExpressionFunctions.cell(),
                                SpreadsheetServerExpressionFunctions.charFunction(),
                                SpreadsheetServerExpressionFunctions.choose(),
                                SpreadsheetServerExpressionFunctions.clean(),
                                SpreadsheetServerExpressionFunctions.code(),
                                SpreadsheetServerExpressionFunctions.column(),
                                SpreadsheetServerExpressionFunctions.columns(),
                                SpreadsheetServerExpressionFunctions.concat(),
                                SpreadsheetServerExpressionFunctions.cos(),
                                SpreadsheetServerExpressionFunctions.count(),
                                SpreadsheetServerExpressionFunctions.countA(),
                                SpreadsheetServerExpressionFunctions.countBlank(),
                                SpreadsheetServerExpressionFunctions.countIf(),
                                SpreadsheetServerExpressionFunctions.date(),
                                SpreadsheetServerExpressionFunctions.day(),
                                SpreadsheetServerExpressionFunctions.days(),
                                SpreadsheetServerExpressionFunctions.decimal(),
                                SpreadsheetServerExpressionFunctions.dec2bin(),
                                SpreadsheetServerExpressionFunctions.dec2hex(),
                                SpreadsheetServerExpressionFunctions.dec2oct(),
                                SpreadsheetServerExpressionFunctions.degrees(),
                                SpreadsheetServerExpressionFunctions.delta(),
                                SpreadsheetServerExpressionFunctions.dollar(),
                                SpreadsheetServerExpressionFunctions.e(),
                                SpreadsheetServerExpressionFunctions.error(),
                                SpreadsheetServerExpressionFunctions.even(),
                                SpreadsheetServerExpressionFunctions.exact(),
                                SpreadsheetServerExpressionFunctions.exp(),
                                SpreadsheetServerExpressionFunctions.falseFunction(),
                                SpreadsheetServerExpressionFunctions.find(),
                                SpreadsheetServerExpressionFunctions.fixed(),
                                SpreadsheetServerExpressionFunctions.floor(),
                                SpreadsheetServerExpressionFunctions.formulaText(),
                                SpreadsheetServerExpressionFunctions.hex2bin(),
                                SpreadsheetServerExpressionFunctions.hex2dec(),
                                SpreadsheetServerExpressionFunctions.hex2oct(),
                                SpreadsheetServerExpressionFunctions.hour(),
                                SpreadsheetServerExpressionFunctions.ifFunction(),
                                SpreadsheetServerExpressionFunctions.ifs(),
                                SpreadsheetServerExpressionFunctions.indirect(),
                                SpreadsheetServerExpressionFunctions.intFunction(),
                                SpreadsheetServerExpressionFunctions.isBlank(),
                                SpreadsheetServerExpressionFunctions.isDate(),
                                SpreadsheetServerExpressionFunctions.isErr(),
                                SpreadsheetServerExpressionFunctions.isError(),
                                SpreadsheetServerExpressionFunctions.isEven(),
                                SpreadsheetServerExpressionFunctions.isFormula(),
                                SpreadsheetServerExpressionFunctions.isLogical(),
                                SpreadsheetServerExpressionFunctions.isNa(),
                                SpreadsheetServerExpressionFunctions.isNonText(),
                                SpreadsheetServerExpressionFunctions.isNumber(),
                                SpreadsheetServerExpressionFunctions.isOdd(),
                                SpreadsheetServerExpressionFunctions.isoWeekNum(),
                                SpreadsheetServerExpressionFunctions.isText(),
                                SpreadsheetServerExpressionFunctions.lambda(),
                                SpreadsheetServerExpressionFunctions.left(),
                                SpreadsheetServerExpressionFunctions.len(),
                                SpreadsheetServerExpressionFunctions.let(),
                                SpreadsheetServerExpressionFunctions.ln(),
                                SpreadsheetServerExpressionFunctions.log(),
                                SpreadsheetServerExpressionFunctions.log10(),
                                SpreadsheetServerExpressionFunctions.lower(),
                                SpreadsheetServerExpressionFunctions.max(),
                                SpreadsheetServerExpressionFunctions.maxIf(),
                                SpreadsheetServerExpressionFunctions.mid(),
                                SpreadsheetServerExpressionFunctions.min(),
                                SpreadsheetServerExpressionFunctions.minIf(),
                                SpreadsheetServerExpressionFunctions.minute(),
                                SpreadsheetServerExpressionFunctions.mod(),
                                SpreadsheetServerExpressionFunctions.month(),
                                SpreadsheetServerExpressionFunctions.not(),
                                SpreadsheetServerExpressionFunctions.now(),
                                SpreadsheetServerExpressionFunctions.numberValue(),
                                SpreadsheetServerExpressionFunctions.oct2bin(),
                                SpreadsheetServerExpressionFunctions.oct2dec(),
                                SpreadsheetServerExpressionFunctions.oct2hex(),
                                SpreadsheetServerExpressionFunctions.odd(),
                                SpreadsheetServerExpressionFunctions.offset(),
                                SpreadsheetServerExpressionFunctions.or(),
                                SpreadsheetServerExpressionFunctions.pi(),
                                SpreadsheetServerExpressionFunctions.product(),
                                SpreadsheetServerExpressionFunctions.proper(),
                                SpreadsheetServerExpressionFunctions.quotient(),
                                SpreadsheetServerExpressionFunctions.radians(),
                                SpreadsheetServerExpressionFunctions.rand(),
                                SpreadsheetServerExpressionFunctions.randBetween(),
                                SpreadsheetServerExpressionFunctions.replace(),
                                SpreadsheetServerExpressionFunctions.rept(),
                                SpreadsheetServerExpressionFunctions.right(),
                                SpreadsheetServerExpressionFunctions.roman(),
                                SpreadsheetServerExpressionFunctions.round(),
                                SpreadsheetServerExpressionFunctions.roundDown(),
                                SpreadsheetServerExpressionFunctions.roundUp(),
                                SpreadsheetServerExpressionFunctions.row(),
                                SpreadsheetServerExpressionFunctions.rows(),
                                SpreadsheetServerExpressionFunctions.search(),
                                SpreadsheetServerExpressionFunctions.second(),
                                SpreadsheetServerExpressionFunctions.sign(),
                                SpreadsheetServerExpressionFunctions.sin(),
                                SpreadsheetServerExpressionFunctions.sinh(),
                                SpreadsheetServerExpressionFunctions.sqrt(),
                                SpreadsheetServerExpressionFunctions.substitute(),
                                SpreadsheetServerExpressionFunctions.sum(),
                                SpreadsheetServerExpressionFunctions.sumIf(),
                                SpreadsheetServerExpressionFunctions.switchFunction(),
                                SpreadsheetServerExpressionFunctions.t(),
                                SpreadsheetServerExpressionFunctions.tan(),
                                SpreadsheetServerExpressionFunctions.tanh(),
                                SpreadsheetServerExpressionFunctions.text(),
                                SpreadsheetServerExpressionFunctions.textJoin(),
                                SpreadsheetServerExpressionFunctions.time(),
                                SpreadsheetServerExpressionFunctions.today(),
                                SpreadsheetServerExpressionFunctions.trim(),
                                SpreadsheetServerExpressionFunctions.trueFunction(),
                                SpreadsheetServerExpressionFunctions.trunc(),
                                SpreadsheetServerExpressionFunctions.type(),
                                SpreadsheetServerExpressionFunctions.unichar(),
                                SpreadsheetServerExpressionFunctions.unicode(),
                                SpreadsheetServerExpressionFunctions.upper(),
                                SpreadsheetServerExpressionFunctions.value(),
                                SpreadsheetServerExpressionFunctions.weekDay(),
                                SpreadsheetServerExpressionFunctions.weekNum(),
                                SpreadsheetServerExpressionFunctions.year(),
                                SpreadsheetServerExpressionFunctions.xor()
                        )
                )
        );
    }

    /**
     * Stop creation
     */
    private SpreadsheetServerExpressionFunctionProviders() {
        throw new UnsupportedOperationException();
    }
}
