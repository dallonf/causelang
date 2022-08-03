grammar Cause;

WHITESPACE : (' ' | '\t' | '\r')+ -> skip ;
NEWLINE : '\n';
COMMA : ',' ;
PAREN_OPEN : '(' ;
PAREN_CLOSE : ')' ;
CURLY_OPEN : '{' ;
CURLY_CLOSE : '}' ;

IMPORT : 'import' ;
AS : 'as' ;
PATH : [a-zA-Z0-9]+ '/' [a-zA-Z/]+ ;
IDENTIFIER : [a-zA-Z]+ ;

file : NEWLINE* (declaration (NEWLINE+ declaration)*)? NEWLINE* EOF ;

declaration : importDeclaration ;

importDeclaration : IMPORT PATH NEWLINE* PAREN_OPEN NEWLINE* importMappings NEWLINE* PAREN_CLOSE ;
importMappings : importMapping NEWLINE* (COMMA NEWLINE* importMapping NEWLINE*)* ;
importMapping : IDENTIFIER (NEWLINE* AS NEWLINE* IDENTIFIER)? ;
