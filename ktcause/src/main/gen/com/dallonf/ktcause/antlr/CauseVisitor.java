// Generated from C:/Users/dallo/devroot/causelang/ktcause/src/main/resources\Cause.g4 by ANTLR 4.10.1
package com.dallonf.ktcause.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CauseParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CauseVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CauseParser#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile(CauseParser.FileContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#typeReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeReference(CauseParser.TypeReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#identifierTypeReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierTypeReference(CauseParser.IdentifierTypeReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#functionTypeReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeReference(CauseParser.FunctionTypeReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#functionTypeReferenceReturnValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeReferenceReturnValue(CauseParser.FunctionTypeReferenceReturnValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#functionSignatureParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionSignatureParam(CauseParser.FunctionSignatureParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(CauseParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(CauseParser.ImportDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#importMappings}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportMappings(CauseParser.ImportMappingsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#importMapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportMapping(CauseParser.ImportMappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(CauseParser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#functionReturnValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionReturnValue(CauseParser.FunctionReturnValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#namedValueDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedValueDeclaration(CauseParser.NamedValueDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#objectDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectDeclaration(CauseParser.ObjectDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#signalDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignalDeclaration(CauseParser.SignalDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#objectFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectFields(CauseParser.ObjectFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#objectField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectField(CauseParser.ObjectFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#optionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionDeclaration(CauseParser.OptionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(CauseParser.BodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(CauseParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#singleStatementBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleStatementBody(CauseParser.SingleStatementBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(CauseParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(CauseParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#declarationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationStatement(CauseParser.DeclarationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#effectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEffectStatement(CauseParser.EffectStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#setStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStatement(CauseParser.SetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(CauseParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#groupExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupExpression(CauseParser.GroupExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#blockExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockExpression(CauseParser.BlockExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#functionExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExpression(CauseParser.FunctionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#branchExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBranchExpression(CauseParser.BranchExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#branchWith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBranchWith(CauseParser.BranchWithContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#loopExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopExpression(CauseParser.LoopExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#causeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCauseExpression(CauseParser.CauseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#returnExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnExpression(CauseParser.ReturnExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#breakExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakExpression(CauseParser.BreakExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#stringLiteralExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteralExpression(CauseParser.StringLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#numberLiteralExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiteralExpression(CauseParser.NumberLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#identifierExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierExpression(CauseParser.IdentifierExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#expressionSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionSuffix(CauseParser.ExpressionSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#callExpressionSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallExpressionSuffix(CauseParser.CallExpressionSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#callParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallParam(CauseParser.CallParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#callPositionalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallPositionalParameter(CauseParser.CallPositionalParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#memberExpressionSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberExpressionSuffix(CauseParser.MemberExpressionSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#pipeCallExpressionSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPipeCallExpressionSuffix(CauseParser.PipeCallExpressionSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#branchOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBranchOption(CauseParser.BranchOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#ifBranchOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfBranchOption(CauseParser.IfBranchOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#isBranchOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsBranchOption(CauseParser.IsBranchOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#elseBranchOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseBranchOption(CauseParser.ElseBranchOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPattern(CauseParser.PatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#captureValuePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaptureValuePattern(CauseParser.CaptureValuePatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link CauseParser#typeReferencePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeReferencePattern(CauseParser.TypeReferencePatternContext ctx);
}