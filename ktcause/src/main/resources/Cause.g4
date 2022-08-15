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
INT_LITERAL : [0-9][0-9_]* ;

AS : 'as' ;
BRANCH : 'branch' ;
CAUSE : 'cause' ;
EFFECT : 'effect' ;
ELSE : 'else' ;
FN : 'fn' ;
FUNCTION : 'function' ;
IF : 'if' ;
IMPORT : 'import' ;
LET : 'let' ;
OBJECT : 'object' ;
PATH : [a-zA-Z0-9_\-]+ '/' [a-zA-Z_\-/]+ ;
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]* ; // TODO: need moar emoji

file : NEWLINE* (declaration (NEWLINE+ declaration)*)? NEWLINE* EOF ;

typeReference : IDENTIFIER ;

declaration : importDeclaration | functionDeclaration | namedValueDeclaration | objectDeclaration ;

importDeclaration : IMPORT NEWLINE* PATH NEWLINE* PAREN_OPEN NEWLINE* importMappings NEWLINE* PAREN_CLOSE ;
importMappings : importMapping NEWLINE* (COMMA NEWLINE* importMapping NEWLINE*)* COMMA? ;
importMapping : IDENTIFIER (NEWLINE* AS NEWLINE* IDENTIFIER)? ;

functionDeclaration : FUNCTION NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE*
    (functionParam NEWLINE* (COMMA NEWLINE* functionParam NEWLINE*)* COMMA?)?
    NEWLINE* PAREN_CLOSE NEWLINE* functionReturnValue?
    NEWLINE* body ;
functionParam : IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference)? ;
functionReturnValue : COLON NEWLINE* typeReference ;

namedValueDeclaration : LET NEWLINE* IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference NEWLINE*)? EQUALS NEWLINE* expression ;

objectDeclaration : OBJECT NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE* (objectField NEWLINE* (COMMA NEWLINE* objectField NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;
objectField : IDENTIFIER NEWLINE* COLON NEWLINE* typeReference ;

body : block | singleExpressionBody ;

block : CURLY_OPEN NEWLINE* (statement (NEWLINE+ statement)*)? NEWLINE* CURLY_CLOSE ;
singleExpressionBody : THICK_ARROW NEWLINE* expression ;

statement : effectStatement | declarationStatement | expressionStatement ;

expressionStatement : expression ;
declarationStatement : declaration ;
effectStatement : EFFECT NEWLINE* PAREN_OPEN NEWLINE* pattern NEWLINE* PAREN_CLOSE body ;

expression : (blockExpression | branchExpression | causeExpression | stringLiteralExpression | integerLiteralExpression | identifierExpression)
    expressionSuffix* ;

blockExpression : block ;
branchExpression : BRANCH NEWLINE* CURLY_OPEN NEWLINE* (branchOption (NEWLINE+ branchOption)*)? NEWLINE* CURLY_CLOSE ;
causeExpression : CAUSE NEWLINE* expression ;
stringLiteralExpression : STRING_LITERAL ;
integerLiteralExpression : INT_LITERAL ;
identifierExpression : IDENTIFIER ;

expressionSuffix : callExpressionSuffix | memberExpressionSuffix ;

callExpressionSuffix : PAREN_OPEN NEWLINE* (callParam NEWLINE* (COMMA NEWLINE* callParam NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;
callParam : callPositionalParameter ;
callPositionalParameter : expression ;

memberExpressionSuffix : DOT NEWLINE* IDENTIFIER ;

branchOption : ifBranchOption | elseBranchOption ;
ifBranchOption : IF expression body ;
elseBranchOption : ELSE body ;

pattern : UNDERSCORE NEWLINE* COLON NEWLINE* typeReference ;