// Generated from C:/Users/dallo/devroot/causelang/ktcause/src/main/resources\Cause.g4 by ANTLR 4.10.1
package com.dallonf.ktcause.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CauseParser}.
 */
public interface CauseListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CauseParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(CauseParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(CauseParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#typeReference}.
	 * @param ctx the parse tree
	 */
	void enterTypeReference(CauseParser.TypeReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#typeReference}.
	 * @param ctx the parse tree
	 */
	void exitTypeReference(CauseParser.TypeReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#identifierTypeReference}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierTypeReference(CauseParser.IdentifierTypeReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#identifierTypeReference}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierTypeReference(CauseParser.IdentifierTypeReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#functionTypeReference}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTypeReference(CauseParser.FunctionTypeReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#functionTypeReference}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTypeReference(CauseParser.FunctionTypeReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#functionTypeReferenceReturnValue}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTypeReferenceReturnValue(CauseParser.FunctionTypeReferenceReturnValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#functionTypeReferenceReturnValue}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTypeReferenceReturnValue(CauseParser.FunctionTypeReferenceReturnValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#functionSignatureParam}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSignatureParam(CauseParser.FunctionSignatureParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#functionSignatureParam}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSignatureParam(CauseParser.FunctionSignatureParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(CauseParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(CauseParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(CauseParser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(CauseParser.ImportDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#importMappings}.
	 * @param ctx the parse tree
	 */
	void enterImportMappings(CauseParser.ImportMappingsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#importMappings}.
	 * @param ctx the parse tree
	 */
	void exitImportMappings(CauseParser.ImportMappingsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#importMapping}.
	 * @param ctx the parse tree
	 */
	void enterImportMapping(CauseParser.ImportMappingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#importMapping}.
	 * @param ctx the parse tree
	 */
	void exitImportMapping(CauseParser.ImportMappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(CauseParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(CauseParser.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#functionReturnValue}.
	 * @param ctx the parse tree
	 */
	void enterFunctionReturnValue(CauseParser.FunctionReturnValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#functionReturnValue}.
	 * @param ctx the parse tree
	 */
	void exitFunctionReturnValue(CauseParser.FunctionReturnValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#namedValueDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterNamedValueDeclaration(CauseParser.NamedValueDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#namedValueDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitNamedValueDeclaration(CauseParser.NamedValueDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#objectDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterObjectDeclaration(CauseParser.ObjectDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#objectDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitObjectDeclaration(CauseParser.ObjectDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#signalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterSignalDeclaration(CauseParser.SignalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#signalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitSignalDeclaration(CauseParser.SignalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#objectFields}.
	 * @param ctx the parse tree
	 */
	void enterObjectFields(CauseParser.ObjectFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#objectFields}.
	 * @param ctx the parse tree
	 */
	void exitObjectFields(CauseParser.ObjectFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#objectField}.
	 * @param ctx the parse tree
	 */
	void enterObjectField(CauseParser.ObjectFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#objectField}.
	 * @param ctx the parse tree
	 */
	void exitObjectField(CauseParser.ObjectFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#optionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterOptionDeclaration(CauseParser.OptionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#optionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitOptionDeclaration(CauseParser.OptionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(CauseParser.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(CauseParser.BodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(CauseParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(CauseParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#singleStatementBody}.
	 * @param ctx the parse tree
	 */
	void enterSingleStatementBody(CauseParser.SingleStatementBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#singleStatementBody}.
	 * @param ctx the parse tree
	 */
	void exitSingleStatementBody(CauseParser.SingleStatementBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(CauseParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(CauseParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(CauseParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(CauseParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#declarationStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationStatement(CauseParser.DeclarationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#declarationStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationStatement(CauseParser.DeclarationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#effectStatement}.
	 * @param ctx the parse tree
	 */
	void enterEffectStatement(CauseParser.EffectStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#effectStatement}.
	 * @param ctx the parse tree
	 */
	void exitEffectStatement(CauseParser.EffectStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetStatement(CauseParser.SetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetStatement(CauseParser.SetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(CauseParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(CauseParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#groupExpression}.
	 * @param ctx the parse tree
	 */
	void enterGroupExpression(CauseParser.GroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#groupExpression}.
	 * @param ctx the parse tree
	 */
	void exitGroupExpression(CauseParser.GroupExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#blockExpression}.
	 * @param ctx the parse tree
	 */
	void enterBlockExpression(CauseParser.BlockExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#blockExpression}.
	 * @param ctx the parse tree
	 */
	void exitBlockExpression(CauseParser.BlockExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#functionExpression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExpression(CauseParser.FunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#functionExpression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExpression(CauseParser.FunctionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#branchExpression}.
	 * @param ctx the parse tree
	 */
	void enterBranchExpression(CauseParser.BranchExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#branchExpression}.
	 * @param ctx the parse tree
	 */
	void exitBranchExpression(CauseParser.BranchExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#branchWith}.
	 * @param ctx the parse tree
	 */
	void enterBranchWith(CauseParser.BranchWithContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#branchWith}.
	 * @param ctx the parse tree
	 */
	void exitBranchWith(CauseParser.BranchWithContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#loopExpression}.
	 * @param ctx the parse tree
	 */
	void enterLoopExpression(CauseParser.LoopExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#loopExpression}.
	 * @param ctx the parse tree
	 */
	void exitLoopExpression(CauseParser.LoopExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#causeExpression}.
	 * @param ctx the parse tree
	 */
	void enterCauseExpression(CauseParser.CauseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#causeExpression}.
	 * @param ctx the parse tree
	 */
	void exitCauseExpression(CauseParser.CauseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#returnExpression}.
	 * @param ctx the parse tree
	 */
	void enterReturnExpression(CauseParser.ReturnExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#returnExpression}.
	 * @param ctx the parse tree
	 */
	void exitReturnExpression(CauseParser.ReturnExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#breakExpression}.
	 * @param ctx the parse tree
	 */
	void enterBreakExpression(CauseParser.BreakExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#breakExpression}.
	 * @param ctx the parse tree
	 */
	void exitBreakExpression(CauseParser.BreakExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#stringLiteralExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteralExpression(CauseParser.StringLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#stringLiteralExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteralExpression(CauseParser.StringLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#numberLiteralExpression}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiteralExpression(CauseParser.NumberLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#numberLiteralExpression}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiteralExpression(CauseParser.NumberLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#identifierExpression}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierExpression(CauseParser.IdentifierExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#identifierExpression}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierExpression(CauseParser.IdentifierExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#expressionSuffix}.
	 * @param ctx the parse tree
	 */
	void enterExpressionSuffix(CauseParser.ExpressionSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#expressionSuffix}.
	 * @param ctx the parse tree
	 */
	void exitExpressionSuffix(CauseParser.ExpressionSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#callExpressionSuffix}.
	 * @param ctx the parse tree
	 */
	void enterCallExpressionSuffix(CauseParser.CallExpressionSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#callExpressionSuffix}.
	 * @param ctx the parse tree
	 */
	void exitCallExpressionSuffix(CauseParser.CallExpressionSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#callParam}.
	 * @param ctx the parse tree
	 */
	void enterCallParam(CauseParser.CallParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#callParam}.
	 * @param ctx the parse tree
	 */
	void exitCallParam(CauseParser.CallParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#callPositionalParameter}.
	 * @param ctx the parse tree
	 */
	void enterCallPositionalParameter(CauseParser.CallPositionalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#callPositionalParameter}.
	 * @param ctx the parse tree
	 */
	void exitCallPositionalParameter(CauseParser.CallPositionalParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#memberExpressionSuffix}.
	 * @param ctx the parse tree
	 */
	void enterMemberExpressionSuffix(CauseParser.MemberExpressionSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#memberExpressionSuffix}.
	 * @param ctx the parse tree
	 */
	void exitMemberExpressionSuffix(CauseParser.MemberExpressionSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#pipeCallExpressionSuffix}.
	 * @param ctx the parse tree
	 */
	void enterPipeCallExpressionSuffix(CauseParser.PipeCallExpressionSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#pipeCallExpressionSuffix}.
	 * @param ctx the parse tree
	 */
	void exitPipeCallExpressionSuffix(CauseParser.PipeCallExpressionSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#branchOption}.
	 * @param ctx the parse tree
	 */
	void enterBranchOption(CauseParser.BranchOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#branchOption}.
	 * @param ctx the parse tree
	 */
	void exitBranchOption(CauseParser.BranchOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#ifBranchOption}.
	 * @param ctx the parse tree
	 */
	void enterIfBranchOption(CauseParser.IfBranchOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#ifBranchOption}.
	 * @param ctx the parse tree
	 */
	void exitIfBranchOption(CauseParser.IfBranchOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#isBranchOption}.
	 * @param ctx the parse tree
	 */
	void enterIsBranchOption(CauseParser.IsBranchOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#isBranchOption}.
	 * @param ctx the parse tree
	 */
	void exitIsBranchOption(CauseParser.IsBranchOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#elseBranchOption}.
	 * @param ctx the parse tree
	 */
	void enterElseBranchOption(CauseParser.ElseBranchOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#elseBranchOption}.
	 * @param ctx the parse tree
	 */
	void exitElseBranchOption(CauseParser.ElseBranchOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(CauseParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(CauseParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#captureValuePattern}.
	 * @param ctx the parse tree
	 */
	void enterCaptureValuePattern(CauseParser.CaptureValuePatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#captureValuePattern}.
	 * @param ctx the parse tree
	 */
	void exitCaptureValuePattern(CauseParser.CaptureValuePatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link CauseParser#typeReferencePattern}.
	 * @param ctx the parse tree
	 */
	void enterTypeReferencePattern(CauseParser.TypeReferencePatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link CauseParser#typeReferencePattern}.
	 * @param ctx the parse tree
	 */
	void exitTypeReferencePattern(CauseParser.TypeReferencePatternContext ctx);
}