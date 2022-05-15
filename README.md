[![Build Status](https://github.com/mP1/walkingkooka-spreadsheet-server-expression-function/actions/workflows/build.yaml/badge.svg)](https://github.com/mP1/walkingkooka-spreadsheet-server-expression-function/actions/workflows/build.yaml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server-expression-function/badge.svg?branch=master)](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server-expression-function?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-spreadsheet-server-expression-function.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server-expression-function/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-spreadsheet-server-expression-function.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server-expression-function/alerts/)
[![J2CL compatible](https://img.shields.io/badge/J2CL-compatible-brightgreen.svg)](https://github.com/mP1/j2cl-central)



An assembly of functions that faithfully match their Excel equivalents, and are ready for use by walkingkooka-spreadsheet-server.

This is mostly an assembly of the following repos which concentrate on `ExpressionFunction` implementations. 

- [boolean](https://github.com/mP1/walkingkooka-tree-expression-function-boolean)
- [datetime](https://github.com/mP1/walkingkooka-tree-expression-function-datetime)
- [engineering](https://github.com/mP1/walkingkooka-tree-expression-function-engineering)
- [net](https://github.com/mP1/walkingkooka-tree-expression-function-net)
- [number](https://github.com/mP1/walkingkooka-tree-expression-function-number)
- [number-trigonometry](https://github.com/mP1/walkingkooka-tree-expression-function-number-trigonometry)
- [spreadsheet](https://github.com/mP1/walkingkooka-spreadsheet-expression-function)
- [stat](https://github.com/mP1/walkingkooka-tree-expression-function-stat)
- [string](https://github.com/mP1/walkingkooka-tree-expression-function-string)

All the functions in the above repos have been executed using the default environment which has several important
different semantics that exist within a spreadsheet formula or function evaluation.

- All java exceptions thrown by an expression or function are converted to the correct error, the normal function execution simply throws exception.
  This is important because an expression like `isError(1/0)` needs the divide by error as its argument rather than lettering the exception propogate. 
- The functions include tests that use spreadsheet context that handles value converting using spreadsheet rules and formatters.



# Functions

- abs()
- acos()
- address
- and()
- asin()
- atan()
- average()
- base()
- bin2dec()
- bin2hex()
- bin2oct()
- bitAnd()
- bitOr()
- bitXor()
- ceil()
- cell()
- char()
- choose()
- clean()
- code()
- column()
- columns()
- concat()
- cos()
- date()
- day()
- days()
- dec2bin()
- dec2hex()
- dec2oct()
- decimal()
- degrees(),
- e(),
- even()
- false()
- formulaText(),
- hex2bin()
- hex2dec()
- hex2oct()
- hour()
- int()
- if()
- ifs()
- isBlank(),
- isErr(),
- isError(),
- isEven(),
- isNa(),
- isNonText(),
- isNumber(),
- isOdd()
- isoWeekNum()
- isText(),
- left(),
- len(),
- ln(),
- log(),
- log10(),
- lower(),
- max(),
- mid(),
- min(),
- minutes()
- mod()
- month()
- not()
- oct2bin()
- oct2dec()
- oct2hex()
- odd()
- offset()
- or()
- pi()
- product()
- proper()
- quotient()
- radians()
- rand()
- randBetween()
- replace()
- rept()
- right()
- roman()
- roundDown()
- roundUp()
- row()
- rows()
- search()
- second()
- sign()
- sin()
- sinh()
- sqrt()
- substitute()
- sum(),
- switch()
- t()
- tan()
- tanh()
- text()
- textjoin()
- time()
- trim()
- true()
- trunc()
- unichar()
- unicode()
- upper()
- weeknum()
- year()
- xor()