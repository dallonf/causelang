grammar Cause;

WHITESPACE : (' ' | '\t' | '\r')+ -> skip ;
COMMENT : '//' ~'\n'* -> skip ;
MULTILINE_COMMENT : '/*' .*? '*/' -> skip ;
NEWLINE : '\n';
COMMA : ',' ;
COLON : ':' ;
THICK_ARROW : '=>' ;
EQUALS : '=' ;
PIPELINE : '>>' ;
PAREN_OPEN : '(' ;
PAREN_CLOSE : ')' ;
CURLY_OPEN : '{' ;
CURLY_CLOSE : '}' ;
UNDERSCORE : '_' ;
DOT : '.' ;

STRING_LITERAL : '"' .*? '"' ;
NUMBER_LITERAL : [0-9] [0-9_]* (DOT [0-9]+)? ;

AS : 'as' ;
BRANCH : 'branch' ;
BREAK : 'break' ;
CAUSE : 'cause' ;
EFFECT : 'effect' ;
ELSE : 'else' ;
FN : 'fn' ;
FOR : 'for' ;
FUNCTION : 'function' ;
FUNCTION_CAMEL: 'Function' ;
IF : 'if' ;
IS : 'is' ;
IMPORT : 'import' ;
LET : 'let' ;
LOOP : 'loop' ;
OBJECT : 'object' ;
OPTION : 'option' ;
RETURN : 'return' ;
SET : 'set' ;
SIGNAL : 'signal' ;
VARIABLE : 'variable' ;
WITH : 'with' ;
PATH : [a-zA-Z0-9_\-.]+ '/' [a-zA-Z_\-/.]+ ;
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]* ; // TODO: need moar emoji

file : NEWLINE* (declaration (NEWLINE+ declaration)*)? NEWLINE* EOF ;

typeReference : functionTypeReference | identifierTypeReference  ;
identifierTypeReference : IDENTIFIER ;
functionTypeReference : FUNCTION_CAMEL NEWLINE* PAREN_OPEN NEWLINE*
    (functionSignatureParam NEWLINE* (COMMA NEWLINE* functionSignatureParam NEWLINE*)* COMMA?)?
    NEWLINE* PAREN_CLOSE NEWLINE* functionTypeReferenceReturnValue
;
    functionTypeReferenceReturnValue : COLON NEWLINE* typeReference ;

functionSignatureParam : IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference)? ;

declaration : importDeclaration | functionDeclaration | namedValueDeclaration | objectDeclaration | signalDeclaration | optionDeclaration ;

importDeclaration : IMPORT NEWLINE* PATH NEWLINE* PAREN_OPEN NEWLINE* importMappings NEWLINE* PAREN_CLOSE ;
importMappings : importMapping NEWLINE* (COMMA NEWLINE* importMapping NEWLINE*)* COMMA? ;
importMapping : IDENTIFIER (NEWLINE* AS NEWLINE* IDENTIFIER)? ;

functionDeclaration : FUNCTION NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE*
    (functionSignatureParam NEWLINE* (COMMA NEWLINE* functionSignatureParam NEWLINE*)* COMMA?)?
    NEWLINE* PAREN_CLOSE NEWLINE* functionReturnValue?
    NEWLINE* body ;
functionReturnValue : COLON NEWLINE* typeReference ;

namedValueDeclaration : LET NEWLINE* VARIABLE? NEWLINE* IDENTIFIER NEWLINE* (COLON NEWLINE* typeReference NEWLINE*)? EQUALS NEWLINE* expression ;

objectDeclaration : OBJECT NEWLINE* IDENTIFIER NEWLINE* objectFields? ;
signalDeclaration : SIGNAL NEWLINE* IDENTIFIER NEWLINE* objectFields? NEWLINE* COLON NEWLINE* typeReference;
objectFields : (PAREN_OPEN NEWLINE* (objectField NEWLINE* (COMMA NEWLINE* objectField NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE) ;
objectField : IDENTIFIER NEWLINE* COLON NEWLINE* typeReference ;

optionDeclaration : OPTION NEWLINE* IDENTIFIER NEWLINE* PAREN_OPEN NEWLINE* (typeReference NEWLINE* (COMMA NEWLINE* typeReference NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;

body : block | singleStatementBody ;

block : CURLY_OPEN NEWLINE* (statement (NEWLINE+ statement)*)? NEWLINE* CURLY_CLOSE ;
singleStatementBody : THICK_ARROW NEWLINE* statement ;

statement : effectStatement | setStatement | declarationStatement | expressionStatement  ;

expressionStatement : expression ;
declarationStatement : declaration ;
effectStatement : EFFECT NEWLINE* FOR NEWLINE* pattern NEWLINE* body ;
setStatement : SET NEWLINE* IDENTIFIER NEWLINE* EQUALS NEWLINE* expression ;

expression : (groupExpression | blockExpression | functionExpression | branchExpression | loopExpression | causeExpression | returnExpression | breakExpression | stringLiteralExpression | numberLiteralExpression | identifierExpression)
    expressionSuffix* ;
groupExpression : PAREN_OPEN NEWLINE* expression NEWLINE* PAREN_CLOSE ;
blockExpression : block ;
functionExpression : FN NEWLINE* PAREN_OPEN NEWLINE*
                        (functionSignatureParam NEWLINE* (COMMA NEWLINE* functionSignatureParam NEWLINE*)* COMMA?)?
                        NEWLINE* PAREN_CLOSE NEWLINE* functionReturnValue? NEWLINE* expression ;
branchExpression : BRANCH NEWLINE* branchWith? NEWLINE* CURLY_OPEN NEWLINE* (branchOption (NEWLINE+ branchOption)*)? NEWLINE* CURLY_CLOSE ;
    branchWith: WITH NEWLINE* expression ;
loopExpression : LOOP NEWLINE* body ;
causeExpression : CAUSE NEWLINE* expression ;
returnExpression : RETURN expression? ; // no newline supported here
breakExpression : BREAK (WITH NEWLINE* expression)?;
stringLiteralExpression : STRING_LITERAL ;
numberLiteralExpression : NUMBER_LITERAL ;
identifierExpression : IDENTIFIER ;

expressionSuffix : callExpressionSuffix | memberExpressionSuffix | pipeCallExpressionSuffix ;

callExpressionSuffix : PAREN_OPEN NEWLINE* (callParam NEWLINE* (COMMA NEWLINE* callParam NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;
callParam : callPositionalParameter ;
callPositionalParameter : expression ;

memberExpressionSuffix : NEWLINE* DOT NEWLINE* IDENTIFIER ;

pipeCallExpressionSuffix : NEWLINE* PIPELINE NEWLINE* expression NEWLINE* PAREN_OPEN NEWLINE*
    (callParam NEWLINE* (COMMA NEWLINE* callParam NEWLINE*)* COMMA?)? NEWLINE* PAREN_CLOSE ;

branchOption : ifBranchOption | isBranchOption | elseBranchOption ;
ifBranchOption : IF expression body ;
isBranchOption : IS pattern body ;
elseBranchOption : ELSE body ;

pattern : captureValuePattern | typeReferencePattern ;
captureValuePattern : typeReference NEWLINE* AS NEWLINE* IDENTIFIER ;
typeReferencePattern : typeReference ;
