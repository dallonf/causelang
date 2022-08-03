grammar Cause;

WHITESPACE : (' ' | '\t' | '\r')+ -> skip ;
NEWLINE : '\n';
COMMA : ',' ;
COLON : ':' ;
EQUALS : '=' ;
PAREN_OPEN : '(' ;
PAREN_CLOSE : ')' ;
CURLY_OPEN : '{' ;
CURLY_CLOSE : '}' ;

STRING_LITERAL : '"' .*? '"' ;
INT_LITERAL : [0-9_]+ ;

AS : 'as' ;
CAUSE : 'cause' ;
FN : 'fn' ;
FUNCTION : 'function' ;
IMPORT : 'import' ;
LET : 'let' ;
PATH : [a-zA-Z0-9]+ '/' [a-zA-Z/]+ ;
IDENTIFIER : [a-zA-Z] [a-zA-Z0-9]* ; // TODO: need moar emoji

file : NEWLINE* (declaration (NEWLINE+ declaration)*)? NEWLINE* EOF ;

typeReference : IDENTIFIER ;

declaration : importDeclaration | functionDeclaration | namedValueDeclaration ;

importDeclaration : IMPORT NEWLINE* PATH NEWLINE* PAREN_OPEN NEWLINE* importMappings NEWLINE* PAREN_CLOSE ;
importMappings : importMapping NEWLINE* (COMMA NEWLINE* importMapping NEWLINE*)* COMMA? ;
importMapping : IDENTIFIER (NEWLINE* AS NEWLINE* IDENTIFIER)? ;

functionDeclaration : FUNCTION NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE* PAREN_CLOSE NEWLINE* structureBody ;

namedValueDeclaration : LET NEWLINE* IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference NEWLINE*)? EQUALS NEWLINE* expression ;

structureBody : blockBody ;

blockBody : CURLY_OPEN NEWLINE* (statement (NEWLINE+ statement)*)? NEWLINE* CURLY_CLOSE ;

statement : declarationStatement | expressionStatement ;

expressionStatement : expression ;
declarationStatement : declaration ;

expression : (stringLiteralExpression | integerLiteralExpression | causeExpression | identifierExpression)
    expressionSuffix? ;

stringLiteralExpression : STRING_LITERAL ;
integerLiteralExpression : INT_LITERAL ;
causeExpression : CAUSE NEWLINE* expression ;
identifierExpression : IDENTIFIER ;

expressionSuffix : callExpressionSuffix ;

callExpressionSuffix : PAREN_OPEN NEWLINE* (callParam NEWLINE* (COMMA NEWLINE* callParam NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;
callParam : callPositionalParameter ;
callPositionalParameter : expression ;
