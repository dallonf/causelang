grammar Cause;

WHITESPACE : (' ' | '\t' | '\r')+ -> skip ;
NEWLINE : '\n';
COMMA : ',' ;
COLON : ':' ;
THICK_ARROW : '=>' ;
EQUALS : '=' ;
PAREN_OPEN : '(' ;
PAREN_CLOSE : ')' ;
CURLY_OPEN : '{' ;
CURLY_CLOSE : '}' ;
UNDERSCORE : '_' ;
DOT : '.' ;

STRING_LITERAL : '"' .*? '"' ;
DECIMAL_LITERAL : [0-9] [0-9_]* DOT [0-9]+ ;
INTEGER_LITERAL : [0-9][0-9_]* ;

AS : 'as' ;
BRANCH : 'branch' ;
CAUSE : 'cause' ;
EFFECT : 'effect' ;
ELSE : 'else' ;
FN : 'fn' ;
FOR : 'for' ;
FUNCTION : 'function' ;
IF : 'if' ;
IS : 'is' ;
IMPORT : 'import' ;
LET : 'let' ;
OBJECT : 'object' ;
OPTION : 'option' ;
RETURN : 'return' ;
SET : 'set' ;
SIGNAL : 'signal' ;
VARIABLE : 'variable' ;
WITH : 'with' ;
PATH : [a-zA-Z0-9_\-]+ '/' [a-zA-Z_\-/]+ ;
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]* ; // TODO: need moar emoji

file : NEWLINE* (declaration (NEWLINE+ declaration)*)? NEWLINE* EOF ;

typeReference : IDENTIFIER ;

declaration : importDeclaration | functionDeclaration | namedValueDeclaration | objectDeclaration | signalDeclaration | optionDeclaration ;

importDeclaration : IMPORT NEWLINE* PATH NEWLINE* PAREN_OPEN NEWLINE* importMappings NEWLINE* PAREN_CLOSE ;
importMappings : importMapping NEWLINE* (COMMA NEWLINE* importMapping NEWLINE*)* COMMA? ;
importMapping : IDENTIFIER (NEWLINE* AS NEWLINE* IDENTIFIER)? ;

functionDeclaration : FUNCTION NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE*
    (functionParam NEWLINE* (COMMA NEWLINE* functionParam NEWLINE*)* COMMA?)?
    NEWLINE* PAREN_CLOSE NEWLINE* functionReturnValue?
    NEWLINE* body ;
functionParam : IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference)? ;
functionReturnValue : COLON NEWLINE* typeReference ;

namedValueDeclaration : LET NEWLINE* VARIABLE? NEWLINE* IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference NEWLINE*)? EQUALS NEWLINE* expression ;

objectDeclaration : OBJECT NEWLINE* IDENTIFIER NEWLINE* objectFields? ;
signalDeclaration : SIGNAL NEWLINE* IDENTIFIER NEWLINE* objectFields? NEWLINE* COLON NEWLINE* typeReference;
objectFields : (PAREN_OPEN NEWLINE* (objectField NEWLINE* (COMMA NEWLINE* objectField NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE) ;
objectField : IDENTIFIER NEWLINE* COLON NEWLINE* typeReference ;

optionDeclaration : OPTION NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE* (typeReference NEWLINE* (COMMA NEWLINE* typeReference NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;

body : block | singleExpressionBody ;

block : CURLY_OPEN NEWLINE* (statement (NEWLINE+ statement)*)? NEWLINE* CURLY_CLOSE ;
singleExpressionBody : THICK_ARROW NEWLINE* expression ;

statement : effectStatement | setStatement | declarationStatement | expressionStatement  ;

expressionStatement : expression ;
declarationStatement : declaration ;
effectStatement : EFFECT NEWLINE* FOR NEWLINE* pattern NEWLINE* body ;
setStatement : SET NEWLINE* IDENTIFIER NEWLINE* EQUALS NEWLINE* expression ;

expression : (blockExpression | branchExpression | causeExpression | returnExpression | stringLiteralExpression | decimalLiteralExpression | integerLiteralExpression | identifierExpression)
    expressionSuffix* ;

blockExpression : block ;
branchExpression : BRANCH NEWLINE* branchWith? NEWLINE* CURLY_OPEN NEWLINE* (branchOption (NEWLINE+ branchOption)*)? NEWLINE* CURLY_CLOSE ;
    branchWith: WITH NEWLINE* expression ;
causeExpression : CAUSE NEWLINE* expression ;
returnExpression : RETURN expression? ; // no newline supported here
stringLiteralExpression : STRING_LITERAL ;
decimalLiteralExpression : DECIMAL_LITERAL ;
integerLiteralExpression : INTEGER_LITERAL ;
identifierExpression : IDENTIFIER ;

expressionSuffix : callExpressionSuffix | memberExpressionSuffix ;

callExpressionSuffix : PAREN_OPEN NEWLINE* (callParam NEWLINE* (COMMA NEWLINE* callParam NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;
callParam : callPositionalParameter ;
callPositionalParameter : expression ;

memberExpressionSuffix : DOT NEWLINE* IDENTIFIER ;

branchOption : ifBranchOption | isBranchOption | elseBranchOption ;
ifBranchOption : IF expression body ;
isBranchOption : IS pattern body ;
elseBranchOption : ELSE body ;

pattern : captureValuePattern | typeReferencePattern ;
captureValuePattern : typeReference NEWLINE* AS NEWLINE* IDENTIFIER ;
typeReferencePattern : typeReference ;
