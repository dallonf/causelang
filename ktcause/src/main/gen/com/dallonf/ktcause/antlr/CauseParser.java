// Generated from C:/Users/dallo/devroot/causelang/ktcause/src/main/resources/Cause.g4 by ANTLR 4.13.2
package com.dallonf.ktcause.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CauseParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WHITESPACE=1, COMMENT=2, MULTILINE_COMMENT=3, NEWLINE=4, COMMA=5, COLON=6, 
		THICK_ARROW=7, EQUALS=8, PIPELINE=9, PAREN_OPEN=10, PAREN_CLOSE=11, CURLY_OPEN=12, 
		CURLY_CLOSE=13, UNDERSCORE=14, DOT=15, CARET=16, STRING_LITERAL=17, NUMBER_LITERAL=18, 
		AS=19, BRANCH=20, BREAK=21, CAUSE=22, EFFECT=23, ELSE=24, FN=25, FOR=26, 
		FUNCTION=27, FUNCTION_CAMEL=28, IF=29, IS=30, IMPORT=31, LET=32, LOOP=33, 
		OBJECT=34, OPTION=35, RETURN=36, RETURNS=37, SET=38, SIGNAL=39, VARIABLE=40, 
		WITH=41, PATH=42, IDENTIFIER=43;
	public static final int
		RULE_file = 0, RULE_typeReference = 1, RULE_identifierTypeReference = 2, 
		RULE_functionTypeReference = 3, RULE_functionTypeReferenceReturnValue = 4, 
		RULE_functionSignatureParam = 5, RULE_declaration = 6, RULE_importDeclaration = 7, 
		RULE_importMappings = 8, RULE_importMapping = 9, RULE_functionDeclaration = 10, 
		RULE_functionReturnValue = 11, RULE_namedValueDeclaration = 12, RULE_objectDeclaration = 13, 
		RULE_signalDeclaration = 14, RULE_objectFields = 15, RULE_objectField = 16, 
		RULE_optionDeclaration = 17, RULE_body = 18, RULE_block = 19, RULE_blockResult = 20, 
		RULE_singleExpressionBody = 21, RULE_statement = 22, RULE_expressionStatement = 23, 
		RULE_declarationStatement = 24, RULE_effectStatement = 25, RULE_setStatement = 26, 
		RULE_expression = 27, RULE_groupExpression = 28, RULE_blockExpression = 29, 
		RULE_functionExpression = 30, RULE_branchExpression = 31, RULE_branchWith = 32, 
		RULE_loopExpression = 33, RULE_causeExpression = 34, RULE_returnExpression = 35, 
		RULE_breakExpression = 36, RULE_stringLiteralExpression = 37, RULE_numberLiteralExpression = 38, 
		RULE_identifierExpression = 39, RULE_expressionSuffix = 40, RULE_callExpressionSuffix = 41, 
		RULE_callParam = 42, RULE_callPositionalParameter = 43, RULE_memberExpressionSuffix = 44, 
		RULE_pipeCallExpressionSuffix = 45, RULE_branchOption = 46, RULE_ifBranchOption = 47, 
		RULE_isBranchOption = 48, RULE_elseBranchOption = 49, RULE_pattern = 50, 
		RULE_captureValuePattern = 51, RULE_typeReferencePattern = 52;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "typeReference", "identifierTypeReference", "functionTypeReference", 
			"functionTypeReferenceReturnValue", "functionSignatureParam", "declaration", 
			"importDeclaration", "importMappings", "importMapping", "functionDeclaration", 
			"functionReturnValue", "namedValueDeclaration", "objectDeclaration", 
			"signalDeclaration", "objectFields", "objectField", "optionDeclaration", 
			"body", "block", "blockResult", "singleExpressionBody", "statement", 
			"expressionStatement", "declarationStatement", "effectStatement", "setStatement", 
			"expression", "groupExpression", "blockExpression", "functionExpression", 
			"branchExpression", "branchWith", "loopExpression", "causeExpression", 
			"returnExpression", "breakExpression", "stringLiteralExpression", "numberLiteralExpression", 
			"identifierExpression", "expressionSuffix", "callExpressionSuffix", "callParam", 
			"callPositionalParameter", "memberExpressionSuffix", "pipeCallExpressionSuffix", 
			"branchOption", "ifBranchOption", "isBranchOption", "elseBranchOption", 
			"pattern", "captureValuePattern", "typeReferencePattern"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'\\n'", "','", "':'", "'=>'", "'='", "'>>'", 
			"'('", "')'", "'{'", "'}'", "'_'", "'.'", "'^'", null, null, "'as'", 
			"'branch'", "'break'", "'cause'", "'effect'", "'else'", "'fn'", "'for'", 
			"'function'", "'Function'", "'if'", "'is'", "'import'", "'let'", "'loop'", 
			"'object'", "'option'", "'return'", "'returns'", "'set'", "'signal'", 
			"'variable'", "'with'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WHITESPACE", "COMMENT", "MULTILINE_COMMENT", "NEWLINE", "COMMA", 
			"COLON", "THICK_ARROW", "EQUALS", "PIPELINE", "PAREN_OPEN", "PAREN_CLOSE", 
			"CURLY_OPEN", "CURLY_CLOSE", "UNDERSCORE", "DOT", "CARET", "STRING_LITERAL", 
			"NUMBER_LITERAL", "AS", "BRANCH", "BREAK", "CAUSE", "EFFECT", "ELSE", 
			"FN", "FOR", "FUNCTION", "FUNCTION_CAMEL", "IF", "IS", "IMPORT", "LET", 
			"LOOP", "OBJECT", "OPTION", "RETURN", "RETURNS", "SET", "SIGNAL", "VARIABLE", 
			"WITH", "PATH", "IDENTIFIER"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Cause.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CauseParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CauseParser.EOF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFile(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(106);
					match(NEWLINE);
					}
					} 
				}
				setState(111);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 607872090112L) != 0)) {
				{
				setState(112);
				declaration();
				setState(121);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(114); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(113);
							match(NEWLINE);
							}
							}
							setState(116); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==NEWLINE );
						setState(118);
						declaration();
						}
						} 
					}
					setState(123);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				}
			}

			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(126);
				match(NEWLINE);
				}
				}
				setState(131);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(132);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeReferenceContext extends ParserRuleContext {
		public FunctionTypeReferenceContext functionTypeReference() {
			return getRuleContext(FunctionTypeReferenceContext.class,0);
		}
		public IdentifierTypeReferenceContext identifierTypeReference() {
			return getRuleContext(IdentifierTypeReferenceContext.class,0);
		}
		public TypeReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterTypeReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitTypeReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitTypeReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeReferenceContext typeReference() throws RecognitionException {
		TypeReferenceContext _localctx = new TypeReferenceContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_typeReference);
		try {
			setState(136);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FUNCTION_CAMEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(134);
				functionTypeReference();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(135);
				identifierTypeReference();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierTypeReferenceContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public IdentifierTypeReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierTypeReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterIdentifierTypeReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitIdentifierTypeReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitIdentifierTypeReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierTypeReferenceContext identifierTypeReference() throws RecognitionException {
		IdentifierTypeReferenceContext _localctx = new IdentifierTypeReferenceContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_identifierTypeReference);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionTypeReferenceContext extends ParserRuleContext {
		public TerminalNode FUNCTION_CAMEL() { return getToken(CauseParser.FUNCTION_CAMEL, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public FunctionTypeReferenceReturnValueContext functionTypeReferenceReturnValue() {
			return getRuleContext(FunctionTypeReferenceReturnValueContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<FunctionSignatureParamContext> functionSignatureParam() {
			return getRuleContexts(FunctionSignatureParamContext.class);
		}
		public FunctionSignatureParamContext functionSignatureParam(int i) {
			return getRuleContext(FunctionSignatureParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public FunctionTypeReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTypeReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFunctionTypeReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFunctionTypeReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFunctionTypeReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTypeReferenceContext functionTypeReference() throws RecognitionException {
		FunctionTypeReferenceContext _localctx = new FunctionTypeReferenceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_functionTypeReference);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(FUNCTION_CAMEL);
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(141);
				match(NEWLINE);
				}
				}
				setState(146);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(147);
			match(PAREN_OPEN);
			setState(151);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(148);
					match(NEWLINE);
					}
					} 
				}
				setState(153);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(154);
				functionSignatureParam();
				setState(158);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(155);
						match(NEWLINE);
						}
						} 
					}
					setState(160);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				}
				setState(177);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(161);
						match(COMMA);
						setState(165);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(162);
							match(NEWLINE);
							}
							}
							setState(167);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(168);
						functionSignatureParam();
						setState(172);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(169);
								match(NEWLINE);
								}
								} 
							}
							setState(174);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
						}
						}
						} 
					}
					setState(179);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(180);
					match(COMMA);
					}
				}

				}
			}

			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(185);
				match(NEWLINE);
				}
				}
				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(191);
			match(PAREN_CLOSE);
			setState(195);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(192);
				match(NEWLINE);
				}
				}
				setState(197);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(198);
			functionTypeReferenceReturnValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionTypeReferenceReturnValueContext extends ParserRuleContext {
		public TerminalNode RETURNS() { return getToken(CauseParser.RETURNS, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public FunctionTypeReferenceReturnValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTypeReferenceReturnValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFunctionTypeReferenceReturnValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFunctionTypeReferenceReturnValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFunctionTypeReferenceReturnValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTypeReferenceReturnValueContext functionTypeReferenceReturnValue() throws RecognitionException {
		FunctionTypeReferenceReturnValueContext _localctx = new FunctionTypeReferenceReturnValueContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_functionTypeReferenceReturnValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(RETURNS);
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(201);
				match(NEWLINE);
				}
				}
				setState(206);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(207);
			typeReference();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionSignatureParamContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public TerminalNode COLON() { return getToken(CauseParser.COLON, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public FunctionSignatureParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionSignatureParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFunctionSignatureParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFunctionSignatureParam(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFunctionSignatureParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionSignatureParamContext functionSignatureParam() throws RecognitionException {
		FunctionSignatureParamContext _localctx = new FunctionSignatureParamContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionSignatureParam);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			match(IDENTIFIER);
			setState(213);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(210);
					match(NEWLINE);
					}
					} 
				}
				setState(215);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			}
			setState(224);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(216);
				match(COLON);
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(217);
					match(NEWLINE);
					}
					}
					setState(222);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(223);
				typeReference();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclarationContext extends ParserRuleContext {
		public ImportDeclarationContext importDeclaration() {
			return getRuleContext(ImportDeclarationContext.class,0);
		}
		public FunctionDeclarationContext functionDeclaration() {
			return getRuleContext(FunctionDeclarationContext.class,0);
		}
		public NamedValueDeclarationContext namedValueDeclaration() {
			return getRuleContext(NamedValueDeclarationContext.class,0);
		}
		public ObjectDeclarationContext objectDeclaration() {
			return getRuleContext(ObjectDeclarationContext.class,0);
		}
		public SignalDeclarationContext signalDeclaration() {
			return getRuleContext(SignalDeclarationContext.class,0);
		}
		public OptionDeclarationContext optionDeclaration() {
			return getRuleContext(OptionDeclarationContext.class,0);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_declaration);
		try {
			setState(232);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IMPORT:
				enterOuterAlt(_localctx, 1);
				{
				setState(226);
				importDeclaration();
				}
				break;
			case FUNCTION:
				enterOuterAlt(_localctx, 2);
				{
				setState(227);
				functionDeclaration();
				}
				break;
			case LET:
				enterOuterAlt(_localctx, 3);
				{
				setState(228);
				namedValueDeclaration();
				}
				break;
			case OBJECT:
				enterOuterAlt(_localctx, 4);
				{
				setState(229);
				objectDeclaration();
				}
				break;
			case SIGNAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(230);
				signalDeclaration();
				}
				break;
			case OPTION:
				enterOuterAlt(_localctx, 6);
				{
				setState(231);
				optionDeclaration();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportDeclarationContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(CauseParser.IMPORT, 0); }
		public TerminalNode PATH() { return getToken(CauseParser.PATH, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public ImportMappingsContext importMappings() {
			return getRuleContext(ImportMappingsContext.class,0);
		}
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterImportDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitImportDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitImportDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportDeclarationContext importDeclaration() throws RecognitionException {
		ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_importDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(IMPORT);
			setState(238);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(235);
				match(NEWLINE);
				}
				}
				setState(240);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(241);
			match(PATH);
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(242);
				match(NEWLINE);
				}
				}
				setState(247);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(248);
			match(PAREN_OPEN);
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(249);
				match(NEWLINE);
				}
				}
				setState(254);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(255);
			importMappings();
			setState(259);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(256);
				match(NEWLINE);
				}
				}
				setState(261);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(262);
			match(PAREN_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportMappingsContext extends ParserRuleContext {
		public List<ImportMappingContext> importMapping() {
			return getRuleContexts(ImportMappingContext.class);
		}
		public ImportMappingContext importMapping(int i) {
			return getRuleContext(ImportMappingContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public ImportMappingsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importMappings; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterImportMappings(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitImportMappings(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitImportMappings(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportMappingsContext importMappings() throws RecognitionException {
		ImportMappingsContext _localctx = new ImportMappingsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_importMappings);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
			importMapping();
			setState(268);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(265);
					match(NEWLINE);
					}
					} 
				}
				setState(270);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			}
			setState(287);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(271);
					match(COMMA);
					setState(275);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==NEWLINE) {
						{
						{
						setState(272);
						match(NEWLINE);
						}
						}
						setState(277);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(278);
					importMapping();
					setState(282);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(279);
							match(NEWLINE);
							}
							} 
						}
						setState(284);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
					}
					}
					} 
				}
				setState(289);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			setState(291);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(290);
				match(COMMA);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportMappingContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(CauseParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(CauseParser.IDENTIFIER, i);
		}
		public TerminalNode AS() { return getToken(CauseParser.AS, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public ImportMappingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importMapping; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterImportMapping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitImportMapping(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitImportMapping(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportMappingContext importMapping() throws RecognitionException {
		ImportMappingContext _localctx = new ImportMappingContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_importMapping);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			match(IDENTIFIER);
			setState(308);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(297);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(294);
					match(NEWLINE);
					}
					}
					setState(299);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(300);
				match(AS);
				setState(304);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(301);
					match(NEWLINE);
					}
					}
					setState(306);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(307);
				match(IDENTIFIER);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionDeclarationContext extends ParserRuleContext {
		public TerminalNode FUNCTION() { return getToken(CauseParser.FUNCTION, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<FunctionSignatureParamContext> functionSignatureParam() {
			return getRuleContexts(FunctionSignatureParamContext.class);
		}
		public FunctionSignatureParamContext functionSignatureParam(int i) {
			return getRuleContext(FunctionSignatureParamContext.class,i);
		}
		public FunctionReturnValueContext functionReturnValue() {
			return getRuleContext(FunctionReturnValueContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public FunctionDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFunctionDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFunctionDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFunctionDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionDeclarationContext functionDeclaration() throws RecognitionException {
		FunctionDeclarationContext _localctx = new FunctionDeclarationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_functionDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			match(FUNCTION);
			setState(314);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(311);
				match(NEWLINE);
				}
				}
				setState(316);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(317);
			match(IDENTIFIER);
			setState(321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(318);
				match(NEWLINE);
				}
				}
				setState(323);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(324);
			match(PAREN_OPEN);
			setState(328);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(325);
					match(NEWLINE);
					}
					} 
				}
				setState(330);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			}
			setState(360);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(331);
				functionSignatureParam();
				setState(335);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(332);
						match(NEWLINE);
						}
						} 
					}
					setState(337);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
				}
				setState(354);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(338);
						match(COMMA);
						setState(342);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(339);
							match(NEWLINE);
							}
							}
							setState(344);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(345);
						functionSignatureParam();
						setState(349);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(346);
								match(NEWLINE);
								}
								} 
							}
							setState(351);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
						}
						}
						} 
					}
					setState(356);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
				}
				setState(358);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(357);
					match(COMMA);
					}
				}

				}
			}

			setState(365);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(362);
				match(NEWLINE);
				}
				}
				setState(367);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(368);
			match(PAREN_CLOSE);
			setState(372);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(369);
					match(NEWLINE);
					}
					} 
				}
				setState(374);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			}
			setState(376);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNS) {
				{
				setState(375);
				functionReturnValue();
				}
			}

			setState(381);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(378);
				match(NEWLINE);
				}
				}
				setState(383);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(384);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionReturnValueContext extends ParserRuleContext {
		public TerminalNode RETURNS() { return getToken(CauseParser.RETURNS, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public FunctionReturnValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionReturnValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFunctionReturnValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFunctionReturnValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFunctionReturnValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionReturnValueContext functionReturnValue() throws RecognitionException {
		FunctionReturnValueContext _localctx = new FunctionReturnValueContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_functionReturnValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			match(RETURNS);
			setState(390);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(387);
				match(NEWLINE);
				}
				}
				setState(392);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(393);
			typeReference();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NamedValueDeclarationContext extends ParserRuleContext {
		public TerminalNode LET() { return getToken(CauseParser.LET, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public TerminalNode EQUALS() { return getToken(CauseParser.EQUALS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public TerminalNode VARIABLE() { return getToken(CauseParser.VARIABLE, 0); }
		public TerminalNode COLON() { return getToken(CauseParser.COLON, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public NamedValueDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedValueDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterNamedValueDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitNamedValueDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitNamedValueDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedValueDeclarationContext namedValueDeclaration() throws RecognitionException {
		NamedValueDeclarationContext _localctx = new NamedValueDeclarationContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_namedValueDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			match(LET);
			setState(399);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(396);
					match(NEWLINE);
					}
					} 
				}
				setState(401);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			}
			setState(403);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VARIABLE) {
				{
				setState(402);
				match(VARIABLE);
				}
			}

			setState(408);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(405);
				match(NEWLINE);
				}
				}
				setState(410);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(411);
			match(IDENTIFIER);
			setState(415);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(412);
				match(NEWLINE);
				}
				}
				setState(417);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(432);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(418);
				match(COLON);
				setState(422);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(419);
					match(NEWLINE);
					}
					}
					setState(424);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(425);
				typeReference();
				setState(429);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(426);
					match(NEWLINE);
					}
					}
					setState(431);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(434);
			match(EQUALS);
			setState(438);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(435);
				match(NEWLINE);
				}
				}
				setState(440);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(441);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectDeclarationContext extends ParserRuleContext {
		public TerminalNode OBJECT() { return getToken(CauseParser.OBJECT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public ObjectFieldsContext objectFields() {
			return getRuleContext(ObjectFieldsContext.class,0);
		}
		public ObjectDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterObjectDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitObjectDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitObjectDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectDeclarationContext objectDeclaration() throws RecognitionException {
		ObjectDeclarationContext _localctx = new ObjectDeclarationContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_objectDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
			match(OBJECT);
			setState(447);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(444);
				match(NEWLINE);
				}
				}
				setState(449);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(450);
			match(IDENTIFIER);
			setState(454);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(451);
					match(NEWLINE);
					}
					} 
				}
				setState(456);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
			}
			setState(458);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PAREN_OPEN) {
				{
				setState(457);
				objectFields();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignalDeclarationContext extends ParserRuleContext {
		public TerminalNode SIGNAL() { return getToken(CauseParser.SIGNAL, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public ObjectFieldsContext objectFields() {
			return getRuleContext(ObjectFieldsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(CauseParser.COLON, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public SignalDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signalDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterSignalDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitSignalDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitSignalDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignalDeclarationContext signalDeclaration() throws RecognitionException {
		SignalDeclarationContext _localctx = new SignalDeclarationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_signalDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(460);
			match(SIGNAL);
			setState(464);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(461);
				match(NEWLINE);
				}
				}
				setState(466);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(467);
			match(IDENTIFIER);
			setState(471);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(468);
					match(NEWLINE);
					}
					} 
				}
				setState(473);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			}
			setState(475);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PAREN_OPEN) {
				{
				setState(474);
				objectFields();
				}
			}

			setState(480);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(477);
					match(NEWLINE);
					}
					} 
				}
				setState(482);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			}
			setState(491);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(483);
				match(COLON);
				setState(487);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(484);
					match(NEWLINE);
					}
					}
					setState(489);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(490);
				typeReference();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectFieldsContext extends ParserRuleContext {
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<ObjectFieldContext> objectField() {
			return getRuleContexts(ObjectFieldContext.class);
		}
		public ObjectFieldContext objectField(int i) {
			return getRuleContext(ObjectFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public ObjectFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterObjectFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitObjectFields(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitObjectFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectFieldsContext objectFields() throws RecognitionException {
		ObjectFieldsContext _localctx = new ObjectFieldsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_objectFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(493);
			match(PAREN_OPEN);
			setState(497);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(494);
					match(NEWLINE);
					}
					} 
				}
				setState(499);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
			}
			setState(529);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(500);
				objectField();
				setState(504);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(501);
						match(NEWLINE);
						}
						} 
					}
					setState(506);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
				}
				setState(523);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(507);
						match(COMMA);
						setState(511);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(508);
							match(NEWLINE);
							}
							}
							setState(513);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(514);
						objectField();
						setState(518);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(515);
								match(NEWLINE);
								}
								} 
							}
							setState(520);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
						}
						}
						} 
					}
					setState(525);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
				}
				setState(527);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(526);
					match(COMMA);
					}
				}

				}
			}

			setState(534);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(531);
				match(NEWLINE);
				}
				}
				setState(536);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(537);
			match(PAREN_CLOSE);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectFieldContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public TerminalNode COLON() { return getToken(CauseParser.COLON, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public ObjectFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterObjectField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitObjectField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitObjectField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectFieldContext objectField() throws RecognitionException {
		ObjectFieldContext _localctx = new ObjectFieldContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_objectField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(539);
			match(IDENTIFIER);
			setState(543);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(540);
				match(NEWLINE);
				}
				}
				setState(545);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(546);
			match(COLON);
			setState(550);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(547);
				match(NEWLINE);
				}
				}
				setState(552);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(553);
			typeReference();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OptionDeclarationContext extends ParserRuleContext {
		public TerminalNode OPTION() { return getToken(CauseParser.OPTION, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<TypeReferenceContext> typeReference() {
			return getRuleContexts(TypeReferenceContext.class);
		}
		public TypeReferenceContext typeReference(int i) {
			return getRuleContext(TypeReferenceContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public OptionDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optionDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterOptionDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitOptionDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitOptionDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptionDeclarationContext optionDeclaration() throws RecognitionException {
		OptionDeclarationContext _localctx = new OptionDeclarationContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_optionDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(555);
			match(OPTION);
			setState(559);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(556);
				match(NEWLINE);
				}
				}
				setState(561);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(562);
			match(IDENTIFIER);
			setState(566);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(563);
				match(NEWLINE);
				}
				}
				setState(568);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(569);
			match(PAREN_OPEN);
			setState(573);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(570);
					match(NEWLINE);
					}
					} 
				}
				setState(575);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			}
			setState(605);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FUNCTION_CAMEL || _la==IDENTIFIER) {
				{
				setState(576);
				typeReference();
				setState(580);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(577);
						match(NEWLINE);
						}
						} 
					}
					setState(582);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
				}
				setState(599);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(583);
						match(COMMA);
						setState(587);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(584);
							match(NEWLINE);
							}
							}
							setState(589);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(590);
						typeReference();
						setState(594);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(591);
								match(NEWLINE);
								}
								} 
							}
							setState(596);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
						}
						}
						} 
					}
					setState(601);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				}
				setState(603);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(602);
					match(COMMA);
					}
				}

				}
			}

			setState(610);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(607);
				match(NEWLINE);
				}
				}
				setState(612);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(613);
			match(PAREN_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public SingleExpressionBodyContext singleExpressionBody() {
			return getRuleContext(SingleExpressionBodyContext.class,0);
		}
		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_body; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BodyContext body() throws RecognitionException {
		BodyContext _localctx = new BodyContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_body);
		try {
			setState(617);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CURLY_OPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(615);
				block();
				}
				break;
			case THICK_ARROW:
				enterOuterAlt(_localctx, 2);
				{
				setState(616);
				singleExpressionBody();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode CURLY_OPEN() { return getToken(CauseParser.CURLY_OPEN, 0); }
		public TerminalNode CURLY_CLOSE() { return getToken(CauseParser.CURLY_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BlockResultContext blockResult() {
			return getRuleContext(BlockResultContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_block);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(619);
			match(CURLY_OPEN);
			setState(623);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(620);
					match(NEWLINE);
					}
					} 
				}
				setState(625);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
			}
			setState(638);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9756202112000L) != 0)) {
				{
				setState(626);
				statement();
				setState(635);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(628); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(627);
							match(NEWLINE);
							}
							}
							setState(630); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==NEWLINE );
						setState(632);
						statement();
						}
						} 
					}
					setState(637);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
				}
				}
			}

			setState(643);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(640);
					match(NEWLINE);
					}
					} 
				}
				setState(645);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
			}
			setState(647);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CARET) {
				{
				setState(646);
				blockResult();
				}
			}

			setState(652);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(649);
				match(NEWLINE);
				}
				}
				setState(654);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(655);
			match(CURLY_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockResultContext extends ParserRuleContext {
		public TerminalNode CARET() { return getToken(CauseParser.CARET, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockResultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockResult; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBlockResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBlockResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBlockResult(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockResultContext blockResult() throws RecognitionException {
		BlockResultContext _localctx = new BlockResultContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_blockResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(657);
			match(CARET);
			setState(658);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SingleExpressionBodyContext extends ParserRuleContext {
		public TerminalNode THICK_ARROW() { return getToken(CauseParser.THICK_ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public SingleExpressionBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleExpressionBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterSingleExpressionBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitSingleExpressionBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitSingleExpressionBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleExpressionBodyContext singleExpressionBody() throws RecognitionException {
		SingleExpressionBodyContext _localctx = new SingleExpressionBodyContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_singleExpressionBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(660);
			match(THICK_ARROW);
			setState(664);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(661);
				match(NEWLINE);
				}
				}
				setState(666);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(667);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public EffectStatementContext effectStatement() {
			return getRuleContext(EffectStatementContext.class,0);
		}
		public SetStatementContext setStatement() {
			return getRuleContext(SetStatementContext.class,0);
		}
		public DeclarationStatementContext declarationStatement() {
			return getRuleContext(DeclarationStatementContext.class,0);
		}
		public ExpressionStatementContext expressionStatement() {
			return getRuleContext(ExpressionStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_statement);
		try {
			setState(673);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EFFECT:
				enterOuterAlt(_localctx, 1);
				{
				setState(669);
				effectStatement();
				}
				break;
			case SET:
				enterOuterAlt(_localctx, 2);
				{
				setState(670);
				setStatement();
				}
				break;
			case FUNCTION:
			case IMPORT:
			case LET:
			case OBJECT:
			case OPTION:
			case SIGNAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(671);
				declarationStatement();
				}
				break;
			case PAREN_OPEN:
			case CURLY_OPEN:
			case STRING_LITERAL:
			case NUMBER_LITERAL:
			case BRANCH:
			case BREAK:
			case CAUSE:
			case FN:
			case LOOP:
			case RETURN:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 4);
				{
				setState(672);
				expressionStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionStatementContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterExpressionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitExpressionStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitExpressionStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionStatementContext expressionStatement() throws RecognitionException {
		ExpressionStatementContext _localctx = new ExpressionStatementContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_expressionStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(675);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclarationStatementContext extends ParserRuleContext {
		public DeclarationContext declaration() {
			return getRuleContext(DeclarationContext.class,0);
		}
		public DeclarationStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarationStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterDeclarationStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitDeclarationStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitDeclarationStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationStatementContext declarationStatement() throws RecognitionException {
		DeclarationStatementContext _localctx = new DeclarationStatementContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_declarationStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(677);
			declaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EffectStatementContext extends ParserRuleContext {
		public TerminalNode EFFECT() { return getToken(CauseParser.EFFECT, 0); }
		public TerminalNode FOR() { return getToken(CauseParser.FOR, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public EffectStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterEffectStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitEffectStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitEffectStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EffectStatementContext effectStatement() throws RecognitionException {
		EffectStatementContext _localctx = new EffectStatementContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_effectStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(679);
			match(EFFECT);
			setState(683);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(680);
				match(NEWLINE);
				}
				}
				setState(685);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(686);
			match(FOR);
			setState(690);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(687);
				match(NEWLINE);
				}
				}
				setState(692);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(693);
			pattern();
			setState(697);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(694);
				match(NEWLINE);
				}
				}
				setState(699);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(700);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SetStatementContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(CauseParser.SET, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public TerminalNode EQUALS() { return getToken(CauseParser.EQUALS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public SetStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterSetStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitSetStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitSetStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetStatementContext setStatement() throws RecognitionException {
		SetStatementContext _localctx = new SetStatementContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_setStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(702);
			match(SET);
			setState(706);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(703);
				match(NEWLINE);
				}
				}
				setState(708);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(709);
			match(IDENTIFIER);
			setState(713);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(710);
				match(NEWLINE);
				}
				}
				setState(715);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(716);
			match(EQUALS);
			setState(720);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(717);
				match(NEWLINE);
				}
				}
				setState(722);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(723);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public GroupExpressionContext groupExpression() {
			return getRuleContext(GroupExpressionContext.class,0);
		}
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public FunctionExpressionContext functionExpression() {
			return getRuleContext(FunctionExpressionContext.class,0);
		}
		public BranchExpressionContext branchExpression() {
			return getRuleContext(BranchExpressionContext.class,0);
		}
		public LoopExpressionContext loopExpression() {
			return getRuleContext(LoopExpressionContext.class,0);
		}
		public CauseExpressionContext causeExpression() {
			return getRuleContext(CauseExpressionContext.class,0);
		}
		public ReturnExpressionContext returnExpression() {
			return getRuleContext(ReturnExpressionContext.class,0);
		}
		public BreakExpressionContext breakExpression() {
			return getRuleContext(BreakExpressionContext.class,0);
		}
		public StringLiteralExpressionContext stringLiteralExpression() {
			return getRuleContext(StringLiteralExpressionContext.class,0);
		}
		public NumberLiteralExpressionContext numberLiteralExpression() {
			return getRuleContext(NumberLiteralExpressionContext.class,0);
		}
		public IdentifierExpressionContext identifierExpression() {
			return getRuleContext(IdentifierExpressionContext.class,0);
		}
		public List<ExpressionSuffixContext> expressionSuffix() {
			return getRuleContexts(ExpressionSuffixContext.class);
		}
		public ExpressionSuffixContext expressionSuffix(int i) {
			return getRuleContext(ExpressionSuffixContext.class,i);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_expression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(736);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PAREN_OPEN:
				{
				setState(725);
				groupExpression();
				}
				break;
			case CURLY_OPEN:
				{
				setState(726);
				blockExpression();
				}
				break;
			case FN:
				{
				setState(727);
				functionExpression();
				}
				break;
			case BRANCH:
				{
				setState(728);
				branchExpression();
				}
				break;
			case LOOP:
				{
				setState(729);
				loopExpression();
				}
				break;
			case CAUSE:
				{
				setState(730);
				causeExpression();
				}
				break;
			case RETURN:
				{
				setState(731);
				returnExpression();
				}
				break;
			case BREAK:
				{
				setState(732);
				breakExpression();
				}
				break;
			case STRING_LITERAL:
				{
				setState(733);
				stringLiteralExpression();
				}
				break;
			case NUMBER_LITERAL:
				{
				setState(734);
				numberLiteralExpression();
				}
				break;
			case IDENTIFIER:
				{
				setState(735);
				identifierExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(741);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,101,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(738);
					expressionSuffix();
					}
					} 
				}
				setState(743);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,101,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupExpressionContext extends ParserRuleContext {
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public GroupExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterGroupExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitGroupExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitGroupExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupExpressionContext groupExpression() throws RecognitionException {
		GroupExpressionContext _localctx = new GroupExpressionContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_groupExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(744);
			match(PAREN_OPEN);
			setState(748);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(745);
				match(NEWLINE);
				}
				}
				setState(750);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(751);
			expression();
			setState(755);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(752);
				match(NEWLINE);
				}
				}
				setState(757);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(758);
			match(PAREN_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockExpressionContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public BlockExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBlockExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBlockExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBlockExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockExpressionContext blockExpression() throws RecognitionException {
		BlockExpressionContext _localctx = new BlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_blockExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(760);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionExpressionContext extends ParserRuleContext {
		public TerminalNode FN() { return getToken(CauseParser.FN, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<FunctionSignatureParamContext> functionSignatureParam() {
			return getRuleContexts(FunctionSignatureParamContext.class);
		}
		public FunctionSignatureParamContext functionSignatureParam(int i) {
			return getRuleContext(FunctionSignatureParamContext.class,i);
		}
		public FunctionReturnValueContext functionReturnValue() {
			return getRuleContext(FunctionReturnValueContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public FunctionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterFunctionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitFunctionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitFunctionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionExpressionContext functionExpression() throws RecognitionException {
		FunctionExpressionContext _localctx = new FunctionExpressionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_functionExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(762);
			match(FN);
			setState(766);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(763);
				match(NEWLINE);
				}
				}
				setState(768);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(769);
			match(PAREN_OPEN);
			setState(773);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(770);
					match(NEWLINE);
					}
					} 
				}
				setState(775);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
			}
			setState(805);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(776);
				functionSignatureParam();
				setState(780);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(777);
						match(NEWLINE);
						}
						} 
					}
					setState(782);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				}
				setState(799);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(783);
						match(COMMA);
						setState(787);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(784);
							match(NEWLINE);
							}
							}
							setState(789);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(790);
						functionSignatureParam();
						setState(794);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(791);
								match(NEWLINE);
								}
								} 
							}
							setState(796);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
						}
						}
						} 
					}
					setState(801);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				}
				setState(803);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(802);
					match(COMMA);
					}
				}

				}
			}

			setState(810);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(807);
				match(NEWLINE);
				}
				}
				setState(812);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(813);
			match(PAREN_CLOSE);
			setState(817);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,113,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(814);
					match(NEWLINE);
					}
					} 
				}
				setState(819);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,113,_ctx);
			}
			setState(821);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNS) {
				{
				setState(820);
				functionReturnValue();
				}
			}

			setState(826);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(823);
				match(NEWLINE);
				}
				}
				setState(828);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(829);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BranchExpressionContext extends ParserRuleContext {
		public TerminalNode BRANCH() { return getToken(CauseParser.BRANCH, 0); }
		public TerminalNode CURLY_OPEN() { return getToken(CauseParser.CURLY_OPEN, 0); }
		public TerminalNode CURLY_CLOSE() { return getToken(CauseParser.CURLY_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public BranchWithContext branchWith() {
			return getRuleContext(BranchWithContext.class,0);
		}
		public List<BranchOptionContext> branchOption() {
			return getRuleContexts(BranchOptionContext.class);
		}
		public BranchOptionContext branchOption(int i) {
			return getRuleContext(BranchOptionContext.class,i);
		}
		public BranchExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_branchExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBranchExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBranchExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBranchExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BranchExpressionContext branchExpression() throws RecognitionException {
		BranchExpressionContext _localctx = new BranchExpressionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_branchExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			match(BRANCH);
			setState(835);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(832);
					match(NEWLINE);
					}
					} 
				}
				setState(837);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
			}
			setState(839);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(838);
				branchWith();
				}
			}

			setState(844);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(841);
				match(NEWLINE);
				}
				}
				setState(846);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(847);
			match(CURLY_OPEN);
			setState(851);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,119,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(848);
					match(NEWLINE);
					}
					} 
				}
				setState(853);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,119,_ctx);
			}
			setState(866);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1627389952L) != 0)) {
				{
				setState(854);
				branchOption();
				setState(863);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,121,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(856); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(855);
							match(NEWLINE);
							}
							}
							setState(858); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==NEWLINE );
						setState(860);
						branchOption();
						}
						} 
					}
					setState(865);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,121,_ctx);
				}
				}
			}

			setState(871);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(868);
				match(NEWLINE);
				}
				}
				setState(873);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(874);
			match(CURLY_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BranchWithContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(CauseParser.WITH, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public BranchWithContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_branchWith; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBranchWith(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBranchWith(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBranchWith(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BranchWithContext branchWith() throws RecognitionException {
		BranchWithContext _localctx = new BranchWithContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_branchWith);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(876);
			match(WITH);
			setState(880);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(877);
				match(NEWLINE);
				}
				}
				setState(882);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(883);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopExpressionContext extends ParserRuleContext {
		public TerminalNode LOOP() { return getToken(CauseParser.LOOP, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public LoopExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterLoopExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitLoopExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitLoopExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopExpressionContext loopExpression() throws RecognitionException {
		LoopExpressionContext _localctx = new LoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_loopExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(885);
			match(LOOP);
			setState(889);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(886);
				match(NEWLINE);
				}
				}
				setState(891);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(892);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CauseExpressionContext extends ParserRuleContext {
		public TerminalNode CAUSE() { return getToken(CauseParser.CAUSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public CauseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_causeExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterCauseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitCauseExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitCauseExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CauseExpressionContext causeExpression() throws RecognitionException {
		CauseExpressionContext _localctx = new CauseExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_causeExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(894);
			match(CAUSE);
			setState(898);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(895);
				match(NEWLINE);
				}
				}
				setState(900);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(901);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReturnExpressionContext extends ParserRuleContext {
		public TerminalNode RETURN() { return getToken(CauseParser.RETURN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ReturnExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterReturnExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitReturnExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitReturnExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReturnExpressionContext returnExpression() throws RecognitionException {
		ReturnExpressionContext _localctx = new ReturnExpressionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_returnExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(903);
			match(RETURN);
			setState(905);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
			case 1:
				{
				setState(904);
				expression();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BreakExpressionContext extends ParserRuleContext {
		public TerminalNode BREAK() { return getToken(CauseParser.BREAK, 0); }
		public TerminalNode WITH() { return getToken(CauseParser.WITH, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public BreakExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_breakExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBreakExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBreakExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBreakExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BreakExpressionContext breakExpression() throws RecognitionException {
		BreakExpressionContext _localctx = new BreakExpressionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_breakExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(907);
			match(BREAK);
			setState(916);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(908);
				match(WITH);
				setState(912);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(909);
					match(NEWLINE);
					}
					}
					setState(914);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(915);
				expression();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringLiteralExpressionContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(CauseParser.STRING_LITERAL, 0); }
		public StringLiteralExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteralExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterStringLiteralExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitStringLiteralExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitStringLiteralExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralExpressionContext stringLiteralExpression() throws RecognitionException {
		StringLiteralExpressionContext _localctx = new StringLiteralExpressionContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_stringLiteralExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(918);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumberLiteralExpressionContext extends ParserRuleContext {
		public TerminalNode NUMBER_LITERAL() { return getToken(CauseParser.NUMBER_LITERAL, 0); }
		public NumberLiteralExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberLiteralExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterNumberLiteralExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitNumberLiteralExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitNumberLiteralExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberLiteralExpressionContext numberLiteralExpression() throws RecognitionException {
		NumberLiteralExpressionContext _localctx = new NumberLiteralExpressionContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_numberLiteralExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(920);
			match(NUMBER_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierExpressionContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public IdentifierExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterIdentifierExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitIdentifierExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitIdentifierExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierExpressionContext identifierExpression() throws RecognitionException {
		IdentifierExpressionContext _localctx = new IdentifierExpressionContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_identifierExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(922);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionSuffixContext extends ParserRuleContext {
		public CallExpressionSuffixContext callExpressionSuffix() {
			return getRuleContext(CallExpressionSuffixContext.class,0);
		}
		public MemberExpressionSuffixContext memberExpressionSuffix() {
			return getRuleContext(MemberExpressionSuffixContext.class,0);
		}
		public PipeCallExpressionSuffixContext pipeCallExpressionSuffix() {
			return getRuleContext(PipeCallExpressionSuffixContext.class,0);
		}
		public ExpressionSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterExpressionSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitExpressionSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitExpressionSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionSuffixContext expressionSuffix() throws RecognitionException {
		ExpressionSuffixContext _localctx = new ExpressionSuffixContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_expressionSuffix);
		try {
			setState(927);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(924);
				callExpressionSuffix();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(925);
				memberExpressionSuffix();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(926);
				pipeCallExpressionSuffix();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallExpressionSuffixContext extends ParserRuleContext {
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<CallParamContext> callParam() {
			return getRuleContexts(CallParamContext.class);
		}
		public CallParamContext callParam(int i) {
			return getRuleContext(CallParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public CallExpressionSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callExpressionSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterCallExpressionSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitCallExpressionSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitCallExpressionSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallExpressionSuffixContext callExpressionSuffix() throws RecognitionException {
		CallExpressionSuffixContext _localctx = new CallExpressionSuffixContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_callExpressionSuffix);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(929);
			match(PAREN_OPEN);
			setState(933);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(930);
					match(NEWLINE);
					}
					} 
				}
				setState(935);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
			}
			setState(965);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8873443726336L) != 0)) {
				{
				setState(936);
				callParam();
				setState(940);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,132,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(937);
						match(NEWLINE);
						}
						} 
					}
					setState(942);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,132,_ctx);
				}
				setState(959);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(943);
						match(COMMA);
						setState(947);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(944);
							match(NEWLINE);
							}
							}
							setState(949);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(950);
						callParam();
						setState(954);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(951);
								match(NEWLINE);
								}
								} 
							}
							setState(956);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
						}
						}
						} 
					}
					setState(961);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
				}
				setState(963);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(962);
					match(COMMA);
					}
				}

				}
			}

			setState(970);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(967);
				match(NEWLINE);
				}
				}
				setState(972);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(973);
			match(PAREN_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallParamContext extends ParserRuleContext {
		public CallPositionalParameterContext callPositionalParameter() {
			return getRuleContext(CallPositionalParameterContext.class,0);
		}
		public CallParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterCallParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitCallParam(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitCallParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallParamContext callParam() throws RecognitionException {
		CallParamContext _localctx = new CallParamContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_callParam);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(975);
			callPositionalParameter();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallPositionalParameterContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CallPositionalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callPositionalParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterCallPositionalParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitCallPositionalParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitCallPositionalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallPositionalParameterContext callPositionalParameter() throws RecognitionException {
		CallPositionalParameterContext _localctx = new CallPositionalParameterContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_callPositionalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(977);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberExpressionSuffixContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(CauseParser.DOT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public MemberExpressionSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberExpressionSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterMemberExpressionSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitMemberExpressionSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitMemberExpressionSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberExpressionSuffixContext memberExpressionSuffix() throws RecognitionException {
		MemberExpressionSuffixContext _localctx = new MemberExpressionSuffixContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_memberExpressionSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(982);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(979);
				match(NEWLINE);
				}
				}
				setState(984);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(985);
			match(DOT);
			setState(989);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(986);
				match(NEWLINE);
				}
				}
				setState(991);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(992);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PipeCallExpressionSuffixContext extends ParserRuleContext {
		public TerminalNode PIPELINE() { return getToken(CauseParser.PIPELINE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PAREN_OPEN() { return getToken(CauseParser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(CauseParser.PAREN_CLOSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public List<CallParamContext> callParam() {
			return getRuleContexts(CallParamContext.class);
		}
		public CallParamContext callParam(int i) {
			return getRuleContext(CallParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CauseParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CauseParser.COMMA, i);
		}
		public PipeCallExpressionSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pipeCallExpressionSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterPipeCallExpressionSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitPipeCallExpressionSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitPipeCallExpressionSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PipeCallExpressionSuffixContext pipeCallExpressionSuffix() throws RecognitionException {
		PipeCallExpressionSuffixContext _localctx = new PipeCallExpressionSuffixContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_pipeCallExpressionSuffix);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(997);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(994);
				match(NEWLINE);
				}
				}
				setState(999);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1000);
			match(PIPELINE);
			setState(1004);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1001);
				match(NEWLINE);
				}
				}
				setState(1006);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1007);
			expression();
			setState(1011);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1008);
				match(NEWLINE);
				}
				}
				setState(1013);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1014);
			match(PAREN_OPEN);
			setState(1018);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1015);
					match(NEWLINE);
					}
					} 
				}
				setState(1020);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			}
			setState(1050);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8873443726336L) != 0)) {
				{
				setState(1021);
				callParam();
				setState(1025);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,145,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1022);
						match(NEWLINE);
						}
						} 
					}
					setState(1027);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,145,_ctx);
				}
				setState(1044);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,148,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1028);
						match(COMMA);
						setState(1032);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(1029);
							match(NEWLINE);
							}
							}
							setState(1034);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(1035);
						callParam();
						setState(1039);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(1036);
								match(NEWLINE);
								}
								} 
							}
							setState(1041);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
						}
						}
						} 
					}
					setState(1046);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,148,_ctx);
				}
				setState(1048);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1047);
					match(COMMA);
					}
				}

				}
			}

			setState(1055);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1052);
				match(NEWLINE);
				}
				}
				setState(1057);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1058);
			match(PAREN_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BranchOptionContext extends ParserRuleContext {
		public IfBranchOptionContext ifBranchOption() {
			return getRuleContext(IfBranchOptionContext.class,0);
		}
		public IsBranchOptionContext isBranchOption() {
			return getRuleContext(IsBranchOptionContext.class,0);
		}
		public ElseBranchOptionContext elseBranchOption() {
			return getRuleContext(ElseBranchOptionContext.class,0);
		}
		public BranchOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_branchOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterBranchOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitBranchOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitBranchOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BranchOptionContext branchOption() throws RecognitionException {
		BranchOptionContext _localctx = new BranchOptionContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_branchOption);
		try {
			setState(1063);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1060);
				ifBranchOption();
				}
				break;
			case IS:
				enterOuterAlt(_localctx, 2);
				{
				setState(1061);
				isBranchOption();
				}
				break;
			case ELSE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1062);
				elseBranchOption();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfBranchOptionContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(CauseParser.IF, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public IfBranchOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifBranchOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterIfBranchOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitIfBranchOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitIfBranchOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfBranchOptionContext ifBranchOption() throws RecognitionException {
		IfBranchOptionContext _localctx = new IfBranchOptionContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_ifBranchOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1065);
			match(IF);
			setState(1066);
			expression();
			setState(1067);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IsBranchOptionContext extends ParserRuleContext {
		public TerminalNode IS() { return getToken(CauseParser.IS, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public IsBranchOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isBranchOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterIsBranchOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitIsBranchOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitIsBranchOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IsBranchOptionContext isBranchOption() throws RecognitionException {
		IsBranchOptionContext _localctx = new IsBranchOptionContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_isBranchOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1069);
			match(IS);
			setState(1070);
			pattern();
			setState(1071);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseBranchOptionContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(CauseParser.ELSE, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ElseBranchOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elseBranchOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterElseBranchOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitElseBranchOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitElseBranchOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElseBranchOptionContext elseBranchOption() throws RecognitionException {
		ElseBranchOptionContext _localctx = new ElseBranchOptionContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_elseBranchOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1073);
			match(ELSE);
			setState(1074);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PatternContext extends ParserRuleContext {
		public CaptureValuePatternContext captureValuePattern() {
			return getRuleContext(CaptureValuePatternContext.class,0);
		}
		public TypeReferencePatternContext typeReferencePattern() {
			return getRuleContext(TypeReferencePatternContext.class,0);
		}
		public PatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_pattern);
		try {
			setState(1078);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,153,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1076);
				captureValuePattern();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1077);
				typeReferencePattern();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CaptureValuePatternContext extends ParserRuleContext {
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public TerminalNode AS() { return getToken(CauseParser.AS, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public CaptureValuePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_captureValuePattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterCaptureValuePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitCaptureValuePattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitCaptureValuePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaptureValuePatternContext captureValuePattern() throws RecognitionException {
		CaptureValuePatternContext _localctx = new CaptureValuePatternContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_captureValuePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1080);
			typeReference();
			setState(1084);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1081);
				match(NEWLINE);
				}
				}
				setState(1086);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1087);
			match(AS);
			setState(1091);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1088);
				match(NEWLINE);
				}
				}
				setState(1093);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1094);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeReferencePatternContext extends ParserRuleContext {
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public TypeReferencePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeReferencePattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterTypeReferencePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitTypeReferencePattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitTypeReferencePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeReferencePatternContext typeReferencePattern() throws RecognitionException {
		TypeReferencePatternContext _localctx = new TypeReferencePatternContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_typeReferencePattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1096);
			typeReference();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001+\u044b\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u00023\u00073\u00024\u00074\u0001\u0000\u0005\u0000l\b\u0000"+
		"\n\u0000\f\u0000o\t\u0000\u0001\u0000\u0001\u0000\u0004\u0000s\b\u0000"+
		"\u000b\u0000\f\u0000t\u0001\u0000\u0005\u0000x\b\u0000\n\u0000\f\u0000"+
		"{\t\u0000\u0003\u0000}\b\u0000\u0001\u0000\u0005\u0000\u0080\b\u0000\n"+
		"\u0000\f\u0000\u0083\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0003\u0001\u0089\b\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0005\u0003\u008f\b\u0003\n\u0003\f\u0003\u0092\t\u0003\u0001\u0003"+
		"\u0001\u0003\u0005\u0003\u0096\b\u0003\n\u0003\f\u0003\u0099\t\u0003\u0001"+
		"\u0003\u0001\u0003\u0005\u0003\u009d\b\u0003\n\u0003\f\u0003\u00a0\t\u0003"+
		"\u0001\u0003\u0001\u0003\u0005\u0003\u00a4\b\u0003\n\u0003\f\u0003\u00a7"+
		"\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00ab\b\u0003\n\u0003\f\u0003"+
		"\u00ae\t\u0003\u0005\u0003\u00b0\b\u0003\n\u0003\f\u0003\u00b3\t\u0003"+
		"\u0001\u0003\u0003\u0003\u00b6\b\u0003\u0003\u0003\u00b8\b\u0003\u0001"+
		"\u0003\u0005\u0003\u00bb\b\u0003\n\u0003\f\u0003\u00be\t\u0003\u0001\u0003"+
		"\u0001\u0003\u0005\u0003\u00c2\b\u0003\n\u0003\f\u0003\u00c5\t\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0005\u0004\u00cb\b\u0004\n"+
		"\u0004\f\u0004\u00ce\t\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0005\u0005\u00d4\b\u0005\n\u0005\f\u0005\u00d7\t\u0005\u0001\u0005"+
		"\u0001\u0005\u0005\u0005\u00db\b\u0005\n\u0005\f\u0005\u00de\t\u0005\u0001"+
		"\u0005\u0003\u0005\u00e1\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u00e9\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0005\u0007\u00ed\b\u0007\n\u0007\f\u0007\u00f0\t\u0007\u0001\u0007"+
		"\u0001\u0007\u0005\u0007\u00f4\b\u0007\n\u0007\f\u0007\u00f7\t\u0007\u0001"+
		"\u0007\u0001\u0007\u0005\u0007\u00fb\b\u0007\n\u0007\f\u0007\u00fe\t\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007\u0102\b\u0007\n\u0007\f\u0007\u0105"+
		"\t\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0005\b\u010b\b\b\n\b"+
		"\f\b\u010e\t\b\u0001\b\u0001\b\u0005\b\u0112\b\b\n\b\f\b\u0115\t\b\u0001"+
		"\b\u0001\b\u0005\b\u0119\b\b\n\b\f\b\u011c\t\b\u0005\b\u011e\b\b\n\b\f"+
		"\b\u0121\t\b\u0001\b\u0003\b\u0124\b\b\u0001\t\u0001\t\u0005\t\u0128\b"+
		"\t\n\t\f\t\u012b\t\t\u0001\t\u0001\t\u0005\t\u012f\b\t\n\t\f\t\u0132\t"+
		"\t\u0001\t\u0003\t\u0135\b\t\u0001\n\u0001\n\u0005\n\u0139\b\n\n\n\f\n"+
		"\u013c\t\n\u0001\n\u0001\n\u0005\n\u0140\b\n\n\n\f\n\u0143\t\n\u0001\n"+
		"\u0001\n\u0005\n\u0147\b\n\n\n\f\n\u014a\t\n\u0001\n\u0001\n\u0005\n\u014e"+
		"\b\n\n\n\f\n\u0151\t\n\u0001\n\u0001\n\u0005\n\u0155\b\n\n\n\f\n\u0158"+
		"\t\n\u0001\n\u0001\n\u0005\n\u015c\b\n\n\n\f\n\u015f\t\n\u0005\n\u0161"+
		"\b\n\n\n\f\n\u0164\t\n\u0001\n\u0003\n\u0167\b\n\u0003\n\u0169\b\n\u0001"+
		"\n\u0005\n\u016c\b\n\n\n\f\n\u016f\t\n\u0001\n\u0001\n\u0005\n\u0173\b"+
		"\n\n\n\f\n\u0176\t\n\u0001\n\u0003\n\u0179\b\n\u0001\n\u0005\n\u017c\b"+
		"\n\n\n\f\n\u017f\t\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0005\u000b"+
		"\u0185\b\u000b\n\u000b\f\u000b\u0188\t\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\f\u0001\f\u0005\f\u018e\b\f\n\f\f\f\u0191\t\f\u0001\f\u0003\f\u0194\b"+
		"\f\u0001\f\u0005\f\u0197\b\f\n\f\f\f\u019a\t\f\u0001\f\u0001\f\u0005\f"+
		"\u019e\b\f\n\f\f\f\u01a1\t\f\u0001\f\u0001\f\u0005\f\u01a5\b\f\n\f\f\f"+
		"\u01a8\t\f\u0001\f\u0001\f\u0005\f\u01ac\b\f\n\f\f\f\u01af\t\f\u0003\f"+
		"\u01b1\b\f\u0001\f\u0001\f\u0005\f\u01b5\b\f\n\f\f\f\u01b8\t\f\u0001\f"+
		"\u0001\f\u0001\r\u0001\r\u0005\r\u01be\b\r\n\r\f\r\u01c1\t\r\u0001\r\u0001"+
		"\r\u0005\r\u01c5\b\r\n\r\f\r\u01c8\t\r\u0001\r\u0003\r\u01cb\b\r\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u01cf\b\u000e\n\u000e\f\u000e\u01d2\t\u000e"+
		"\u0001\u000e\u0001\u000e\u0005\u000e\u01d6\b\u000e\n\u000e\f\u000e\u01d9"+
		"\t\u000e\u0001\u000e\u0003\u000e\u01dc\b\u000e\u0001\u000e\u0005\u000e"+
		"\u01df\b\u000e\n\u000e\f\u000e\u01e2\t\u000e\u0001\u000e\u0001\u000e\u0005"+
		"\u000e\u01e6\b\u000e\n\u000e\f\u000e\u01e9\t\u000e\u0001\u000e\u0003\u000e"+
		"\u01ec\b\u000e\u0001\u000f\u0001\u000f\u0005\u000f\u01f0\b\u000f\n\u000f"+
		"\f\u000f\u01f3\t\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u01f7\b\u000f"+
		"\n\u000f\f\u000f\u01fa\t\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u01fe"+
		"\b\u000f\n\u000f\f\u000f\u0201\t\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u0205\b\u000f\n\u000f\f\u000f\u0208\t\u000f\u0005\u000f\u020a\b\u000f"+
		"\n\u000f\f\u000f\u020d\t\u000f\u0001\u000f\u0003\u000f\u0210\b\u000f\u0003"+
		"\u000f\u0212\b\u000f\u0001\u000f\u0005\u000f\u0215\b\u000f\n\u000f\f\u000f"+
		"\u0218\t\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0005\u0010"+
		"\u021e\b\u0010\n\u0010\f\u0010\u0221\t\u0010\u0001\u0010\u0001\u0010\u0005"+
		"\u0010\u0225\b\u0010\n\u0010\f\u0010\u0228\t\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0001\u0011\u0005\u0011\u022e\b\u0011\n\u0011\f\u0011\u0231"+
		"\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u0235\b\u0011\n\u0011\f\u0011"+
		"\u0238\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u023c\b\u0011\n\u0011"+
		"\f\u0011\u023f\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u0243\b\u0011"+
		"\n\u0011\f\u0011\u0246\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u024a"+
		"\b\u0011\n\u0011\f\u0011\u024d\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011"+
		"\u0251\b\u0011\n\u0011\f\u0011\u0254\t\u0011\u0005\u0011\u0256\b\u0011"+
		"\n\u0011\f\u0011\u0259\t\u0011\u0001\u0011\u0003\u0011\u025c\b\u0011\u0003"+
		"\u0011\u025e\b\u0011\u0001\u0011\u0005\u0011\u0261\b\u0011\n\u0011\f\u0011"+
		"\u0264\t\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0003\u0012"+
		"\u026a\b\u0012\u0001\u0013\u0001\u0013\u0005\u0013\u026e\b\u0013\n\u0013"+
		"\f\u0013\u0271\t\u0013\u0001\u0013\u0001\u0013\u0004\u0013\u0275\b\u0013"+
		"\u000b\u0013\f\u0013\u0276\u0001\u0013\u0005\u0013\u027a\b\u0013\n\u0013"+
		"\f\u0013\u027d\t\u0013\u0003\u0013\u027f\b\u0013\u0001\u0013\u0005\u0013"+
		"\u0282\b\u0013\n\u0013\f\u0013\u0285\t\u0013\u0001\u0013\u0003\u0013\u0288"+
		"\b\u0013\u0001\u0013\u0005\u0013\u028b\b\u0013\n\u0013\f\u0013\u028e\t"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0015\u0001\u0015\u0005\u0015\u0297\b\u0015\n\u0015\f\u0015\u029a\t\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u02a2\b\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0019\u0001\u0019\u0005\u0019\u02aa\b\u0019\n\u0019\f\u0019\u02ad"+
		"\t\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u02b1\b\u0019\n\u0019\f\u0019"+
		"\u02b4\t\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u02b8\b\u0019\n\u0019"+
		"\f\u0019\u02bb\t\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a"+
		"\u0005\u001a\u02c1\b\u001a\n\u001a\f\u001a\u02c4\t\u001a\u0001\u001a\u0001"+
		"\u001a\u0005\u001a\u02c8\b\u001a\n\u001a\f\u001a\u02cb\t\u001a\u0001\u001a"+
		"\u0001\u001a\u0005\u001a\u02cf\b\u001a\n\u001a\f\u001a\u02d2\t\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0003\u001b\u02e1\b\u001b\u0001\u001b\u0005\u001b\u02e4\b\u001b"+
		"\n\u001b\f\u001b\u02e7\t\u001b\u0001\u001c\u0001\u001c\u0005\u001c\u02eb"+
		"\b\u001c\n\u001c\f\u001c\u02ee\t\u001c\u0001\u001c\u0001\u001c\u0005\u001c"+
		"\u02f2\b\u001c\n\u001c\f\u001c\u02f5\t\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0005\u001e\u02fd\b\u001e\n"+
		"\u001e\f\u001e\u0300\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0304"+
		"\b\u001e\n\u001e\f\u001e\u0307\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e"+
		"\u030b\b\u001e\n\u001e\f\u001e\u030e\t\u001e\u0001\u001e\u0001\u001e\u0005"+
		"\u001e\u0312\b\u001e\n\u001e\f\u001e\u0315\t\u001e\u0001\u001e\u0001\u001e"+
		"\u0005\u001e\u0319\b\u001e\n\u001e\f\u001e\u031c\t\u001e\u0005\u001e\u031e"+
		"\b\u001e\n\u001e\f\u001e\u0321\t\u001e\u0001\u001e\u0003\u001e\u0324\b"+
		"\u001e\u0003\u001e\u0326\b\u001e\u0001\u001e\u0005\u001e\u0329\b\u001e"+
		"\n\u001e\f\u001e\u032c\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0330"+
		"\b\u001e\n\u001e\f\u001e\u0333\t\u001e\u0001\u001e\u0003\u001e\u0336\b"+
		"\u001e\u0001\u001e\u0005\u001e\u0339\b\u001e\n\u001e\f\u001e\u033c\t\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f\u0005\u001f\u0342\b\u001f"+
		"\n\u001f\f\u001f\u0345\t\u001f\u0001\u001f\u0003\u001f\u0348\b\u001f\u0001"+
		"\u001f\u0005\u001f\u034b\b\u001f\n\u001f\f\u001f\u034e\t\u001f\u0001\u001f"+
		"\u0001\u001f\u0005\u001f\u0352\b\u001f\n\u001f\f\u001f\u0355\t\u001f\u0001"+
		"\u001f\u0001\u001f\u0004\u001f\u0359\b\u001f\u000b\u001f\f\u001f\u035a"+
		"\u0001\u001f\u0005\u001f\u035e\b\u001f\n\u001f\f\u001f\u0361\t\u001f\u0003"+
		"\u001f\u0363\b\u001f\u0001\u001f\u0005\u001f\u0366\b\u001f\n\u001f\f\u001f"+
		"\u0369\t\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0005 \u036f\b \n"+
		" \f \u0372\t \u0001 \u0001 \u0001!\u0001!\u0005!\u0378\b!\n!\f!\u037b"+
		"\t!\u0001!\u0001!\u0001\"\u0001\"\u0005\"\u0381\b\"\n\"\f\"\u0384\t\""+
		"\u0001\"\u0001\"\u0001#\u0001#\u0003#\u038a\b#\u0001$\u0001$\u0001$\u0005"+
		"$\u038f\b$\n$\f$\u0392\t$\u0001$\u0003$\u0395\b$\u0001%\u0001%\u0001&"+
		"\u0001&\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0003(\u03a0\b(\u0001)\u0001"+
		")\u0005)\u03a4\b)\n)\f)\u03a7\t)\u0001)\u0001)\u0005)\u03ab\b)\n)\f)\u03ae"+
		"\t)\u0001)\u0001)\u0005)\u03b2\b)\n)\f)\u03b5\t)\u0001)\u0001)\u0005)"+
		"\u03b9\b)\n)\f)\u03bc\t)\u0005)\u03be\b)\n)\f)\u03c1\t)\u0001)\u0003)"+
		"\u03c4\b)\u0003)\u03c6\b)\u0001)\u0005)\u03c9\b)\n)\f)\u03cc\t)\u0001"+
		")\u0001)\u0001*\u0001*\u0001+\u0001+\u0001,\u0005,\u03d5\b,\n,\f,\u03d8"+
		"\t,\u0001,\u0001,\u0005,\u03dc\b,\n,\f,\u03df\t,\u0001,\u0001,\u0001-"+
		"\u0005-\u03e4\b-\n-\f-\u03e7\t-\u0001-\u0001-\u0005-\u03eb\b-\n-\f-\u03ee"+
		"\t-\u0001-\u0001-\u0005-\u03f2\b-\n-\f-\u03f5\t-\u0001-\u0001-\u0005-"+
		"\u03f9\b-\n-\f-\u03fc\t-\u0001-\u0001-\u0005-\u0400\b-\n-\f-\u0403\t-"+
		"\u0001-\u0001-\u0005-\u0407\b-\n-\f-\u040a\t-\u0001-\u0001-\u0005-\u040e"+
		"\b-\n-\f-\u0411\t-\u0005-\u0413\b-\n-\f-\u0416\t-\u0001-\u0003-\u0419"+
		"\b-\u0003-\u041b\b-\u0001-\u0005-\u041e\b-\n-\f-\u0421\t-\u0001-\u0001"+
		"-\u0001.\u0001.\u0001.\u0003.\u0428\b.\u0001/\u0001/\u0001/\u0001/\u0001"+
		"0\u00010\u00010\u00010\u00011\u00011\u00011\u00012\u00012\u00032\u0437"+
		"\b2\u00013\u00013\u00053\u043b\b3\n3\f3\u043e\t3\u00013\u00013\u00053"+
		"\u0442\b3\n3\f3\u0445\t3\u00013\u00013\u00014\u00014\u00014\u0000\u0000"+
		"5\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfh\u0000\u0000\u04c2"+
		"\u0000m\u0001\u0000\u0000\u0000\u0002\u0088\u0001\u0000\u0000\u0000\u0004"+
		"\u008a\u0001\u0000\u0000\u0000\u0006\u008c\u0001\u0000\u0000\u0000\b\u00c8"+
		"\u0001\u0000\u0000\u0000\n\u00d1\u0001\u0000\u0000\u0000\f\u00e8\u0001"+
		"\u0000\u0000\u0000\u000e\u00ea\u0001\u0000\u0000\u0000\u0010\u0108\u0001"+
		"\u0000\u0000\u0000\u0012\u0125\u0001\u0000\u0000\u0000\u0014\u0136\u0001"+
		"\u0000\u0000\u0000\u0016\u0182\u0001\u0000\u0000\u0000\u0018\u018b\u0001"+
		"\u0000\u0000\u0000\u001a\u01bb\u0001\u0000\u0000\u0000\u001c\u01cc\u0001"+
		"\u0000\u0000\u0000\u001e\u01ed\u0001\u0000\u0000\u0000 \u021b\u0001\u0000"+
		"\u0000\u0000\"\u022b\u0001\u0000\u0000\u0000$\u0269\u0001\u0000\u0000"+
		"\u0000&\u026b\u0001\u0000\u0000\u0000(\u0291\u0001\u0000\u0000\u0000*"+
		"\u0294\u0001\u0000\u0000\u0000,\u02a1\u0001\u0000\u0000\u0000.\u02a3\u0001"+
		"\u0000\u0000\u00000\u02a5\u0001\u0000\u0000\u00002\u02a7\u0001\u0000\u0000"+
		"\u00004\u02be\u0001\u0000\u0000\u00006\u02e0\u0001\u0000\u0000\u00008"+
		"\u02e8\u0001\u0000\u0000\u0000:\u02f8\u0001\u0000\u0000\u0000<\u02fa\u0001"+
		"\u0000\u0000\u0000>\u033f\u0001\u0000\u0000\u0000@\u036c\u0001\u0000\u0000"+
		"\u0000B\u0375\u0001\u0000\u0000\u0000D\u037e\u0001\u0000\u0000\u0000F"+
		"\u0387\u0001\u0000\u0000\u0000H\u038b\u0001\u0000\u0000\u0000J\u0396\u0001"+
		"\u0000\u0000\u0000L\u0398\u0001\u0000\u0000\u0000N\u039a\u0001\u0000\u0000"+
		"\u0000P\u039f\u0001\u0000\u0000\u0000R\u03a1\u0001\u0000\u0000\u0000T"+
		"\u03cf\u0001\u0000\u0000\u0000V\u03d1\u0001\u0000\u0000\u0000X\u03d6\u0001"+
		"\u0000\u0000\u0000Z\u03e5\u0001\u0000\u0000\u0000\\\u0427\u0001\u0000"+
		"\u0000\u0000^\u0429\u0001\u0000\u0000\u0000`\u042d\u0001\u0000\u0000\u0000"+
		"b\u0431\u0001\u0000\u0000\u0000d\u0436\u0001\u0000\u0000\u0000f\u0438"+
		"\u0001\u0000\u0000\u0000h\u0448\u0001\u0000\u0000\u0000jl\u0005\u0004"+
		"\u0000\u0000kj\u0001\u0000\u0000\u0000lo\u0001\u0000\u0000\u0000mk\u0001"+
		"\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000n|\u0001\u0000\u0000\u0000"+
		"om\u0001\u0000\u0000\u0000py\u0003\f\u0006\u0000qs\u0005\u0004\u0000\u0000"+
		"rq\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000"+
		"\u0000tu\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vx\u0003\f\u0006"+
		"\u0000wr\u0001\u0000\u0000\u0000x{\u0001\u0000\u0000\u0000yw\u0001\u0000"+
		"\u0000\u0000yz\u0001\u0000\u0000\u0000z}\u0001\u0000\u0000\u0000{y\u0001"+
		"\u0000\u0000\u0000|p\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000"+
		"}\u0081\u0001\u0000\u0000\u0000~\u0080\u0005\u0004\u0000\u0000\u007f~"+
		"\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000\u0000\u0081\u007f"+
		"\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000\u0082\u0084"+
		"\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0084\u0085"+
		"\u0005\u0000\u0000\u0001\u0085\u0001\u0001\u0000\u0000\u0000\u0086\u0089"+
		"\u0003\u0006\u0003\u0000\u0087\u0089\u0003\u0004\u0002\u0000\u0088\u0086"+
		"\u0001\u0000\u0000\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0089\u0003"+
		"\u0001\u0000\u0000\u0000\u008a\u008b\u0005+\u0000\u0000\u008b\u0005\u0001"+
		"\u0000\u0000\u0000\u008c\u0090\u0005\u001c\u0000\u0000\u008d\u008f\u0005"+
		"\u0004\u0000\u0000\u008e\u008d\u0001\u0000\u0000\u0000\u008f\u0092\u0001"+
		"\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000\u0090\u0091\u0001"+
		"\u0000\u0000\u0000\u0091\u0093\u0001\u0000\u0000\u0000\u0092\u0090\u0001"+
		"\u0000\u0000\u0000\u0093\u0097\u0005\n\u0000\u0000\u0094\u0096\u0005\u0004"+
		"\u0000\u0000\u0095\u0094\u0001\u0000\u0000\u0000\u0096\u0099\u0001\u0000"+
		"\u0000\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000"+
		"\u0000\u0000\u0098\u00b7\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000"+
		"\u0000\u0000\u009a\u009e\u0003\n\u0005\u0000\u009b\u009d\u0005\u0004\u0000"+
		"\u0000\u009c\u009b\u0001\u0000\u0000\u0000\u009d\u00a0\u0001\u0000\u0000"+
		"\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000"+
		"\u0000\u009f\u00b1\u0001\u0000\u0000\u0000\u00a0\u009e\u0001\u0000\u0000"+
		"\u0000\u00a1\u00a5\u0005\u0005\u0000\u0000\u00a2\u00a4\u0005\u0004\u0000"+
		"\u0000\u00a3\u00a2\u0001\u0000\u0000\u0000\u00a4\u00a7\u0001\u0000\u0000"+
		"\u0000\u00a5\u00a3\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000\u0000"+
		"\u0000\u00a6\u00a8\u0001\u0000\u0000\u0000\u00a7\u00a5\u0001\u0000\u0000"+
		"\u0000\u00a8\u00ac\u0003\n\u0005\u0000\u00a9\u00ab\u0005\u0004\u0000\u0000"+
		"\u00aa\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ae\u0001\u0000\u0000\u0000"+
		"\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000"+
		"\u00ad\u00b0\u0001\u0000\u0000\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000"+
		"\u00af\u00a1\u0001\u0000\u0000\u0000\u00b0\u00b3\u0001\u0000\u0000\u0000"+
		"\u00b1\u00af\u0001\u0000\u0000\u0000\u00b1\u00b2\u0001\u0000\u0000\u0000"+
		"\u00b2\u00b5\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000"+
		"\u00b4\u00b6\u0005\u0005\u0000\u0000\u00b5\u00b4\u0001\u0000\u0000\u0000"+
		"\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6\u00b8\u0001\u0000\u0000\u0000"+
		"\u00b7\u009a\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000"+
		"\u00b8\u00bc\u0001\u0000\u0000\u0000\u00b9\u00bb\u0005\u0004\u0000\u0000"+
		"\u00ba\u00b9\u0001\u0000\u0000\u0000\u00bb\u00be\u0001\u0000\u0000\u0000"+
		"\u00bc\u00ba\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000"+
		"\u00bd\u00bf\u0001\u0000\u0000\u0000\u00be\u00bc\u0001\u0000\u0000\u0000"+
		"\u00bf\u00c3\u0005\u000b\u0000\u0000\u00c0\u00c2\u0005\u0004\u0000\u0000"+
		"\u00c1\u00c0\u0001\u0000\u0000\u0000\u00c2\u00c5\u0001\u0000\u0000\u0000"+
		"\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000"+
		"\u00c4\u00c6\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000"+
		"\u00c6\u00c7\u0003\b\u0004\u0000\u00c7\u0007\u0001\u0000\u0000\u0000\u00c8"+
		"\u00cc\u0005%\u0000\u0000\u00c9\u00cb\u0005\u0004\u0000\u0000\u00ca\u00c9"+
		"\u0001\u0000\u0000\u0000\u00cb\u00ce\u0001\u0000\u0000\u0000\u00cc\u00ca"+
		"\u0001\u0000\u0000\u0000\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00cf"+
		"\u0001\u0000\u0000\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00cf\u00d0"+
		"\u0003\u0002\u0001\u0000\u00d0\t\u0001\u0000\u0000\u0000\u00d1\u00d5\u0005"+
		"+\u0000\u0000\u00d2\u00d4\u0005\u0004\u0000\u0000\u00d3\u00d2\u0001\u0000"+
		"\u0000\u0000\u00d4\u00d7\u0001\u0000\u0000\u0000\u00d5\u00d3\u0001\u0000"+
		"\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000\u0000\u00d6\u00e0\u0001\u0000"+
		"\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000\u00d8\u00dc\u0005\u0006"+
		"\u0000\u0000\u00d9\u00db\u0005\u0004\u0000\u0000\u00da\u00d9\u0001\u0000"+
		"\u0000\u0000\u00db\u00de\u0001\u0000\u0000\u0000\u00dc\u00da\u0001\u0000"+
		"\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00df\u0001\u0000"+
		"\u0000\u0000\u00de\u00dc\u0001\u0000\u0000\u0000\u00df\u00e1\u0003\u0002"+
		"\u0001\u0000\u00e0\u00d8\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000"+
		"\u0000\u0000\u00e1\u000b\u0001\u0000\u0000\u0000\u00e2\u00e9\u0003\u000e"+
		"\u0007\u0000\u00e3\u00e9\u0003\u0014\n\u0000\u00e4\u00e9\u0003\u0018\f"+
		"\u0000\u00e5\u00e9\u0003\u001a\r\u0000\u00e6\u00e9\u0003\u001c\u000e\u0000"+
		"\u00e7\u00e9\u0003\"\u0011\u0000\u00e8\u00e2\u0001\u0000\u0000\u0000\u00e8"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e8\u00e4\u0001\u0000\u0000\u0000\u00e8"+
		"\u00e5\u0001\u0000\u0000\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8"+
		"\u00e7\u0001\u0000\u0000\u0000\u00e9\r\u0001\u0000\u0000\u0000\u00ea\u00ee"+
		"\u0005\u001f\u0000\u0000\u00eb\u00ed\u0005\u0004\u0000\u0000\u00ec\u00eb"+
		"\u0001\u0000\u0000\u0000\u00ed\u00f0\u0001\u0000\u0000\u0000\u00ee\u00ec"+
		"\u0001\u0000\u0000\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000\u00ef\u00f1"+
		"\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f1\u00f5"+
		"\u0005*\u0000\u0000\u00f2\u00f4\u0005\u0004\u0000\u0000\u00f3\u00f2\u0001"+
		"\u0000\u0000\u0000\u00f4\u00f7\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001"+
		"\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f8\u0001"+
		"\u0000\u0000\u0000\u00f7\u00f5\u0001\u0000\u0000\u0000\u00f8\u00fc\u0005"+
		"\n\u0000\u0000\u00f9\u00fb\u0005\u0004\u0000\u0000\u00fa\u00f9\u0001\u0000"+
		"\u0000\u0000\u00fb\u00fe\u0001\u0000\u0000\u0000\u00fc\u00fa\u0001\u0000"+
		"\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd\u00ff\u0001\u0000"+
		"\u0000\u0000\u00fe\u00fc\u0001\u0000\u0000\u0000\u00ff\u0103\u0003\u0010"+
		"\b\u0000\u0100\u0102\u0005\u0004\u0000\u0000\u0101\u0100\u0001\u0000\u0000"+
		"\u0000\u0102\u0105\u0001\u0000\u0000\u0000\u0103\u0101\u0001\u0000\u0000"+
		"\u0000\u0103\u0104\u0001\u0000\u0000\u0000\u0104\u0106\u0001\u0000\u0000"+
		"\u0000\u0105\u0103\u0001\u0000\u0000\u0000\u0106\u0107\u0005\u000b\u0000"+
		"\u0000\u0107\u000f\u0001\u0000\u0000\u0000\u0108\u010c\u0003\u0012\t\u0000"+
		"\u0109\u010b\u0005\u0004\u0000\u0000\u010a\u0109\u0001\u0000\u0000\u0000"+
		"\u010b\u010e\u0001\u0000\u0000\u0000\u010c\u010a\u0001\u0000\u0000\u0000"+
		"\u010c\u010d\u0001\u0000\u0000\u0000\u010d\u011f\u0001\u0000\u0000\u0000"+
		"\u010e\u010c\u0001\u0000\u0000\u0000\u010f\u0113\u0005\u0005\u0000\u0000"+
		"\u0110\u0112\u0005\u0004\u0000\u0000\u0111\u0110\u0001\u0000\u0000\u0000"+
		"\u0112\u0115\u0001\u0000\u0000\u0000\u0113\u0111\u0001\u0000\u0000\u0000"+
		"\u0113\u0114\u0001\u0000\u0000\u0000\u0114\u0116\u0001\u0000\u0000\u0000"+
		"\u0115\u0113\u0001\u0000\u0000\u0000\u0116\u011a\u0003\u0012\t\u0000\u0117"+
		"\u0119\u0005\u0004\u0000\u0000\u0118\u0117\u0001\u0000\u0000\u0000\u0119"+
		"\u011c\u0001\u0000\u0000\u0000\u011a\u0118\u0001\u0000\u0000\u0000\u011a"+
		"\u011b\u0001\u0000\u0000\u0000\u011b\u011e\u0001\u0000\u0000\u0000\u011c"+
		"\u011a\u0001\u0000\u0000\u0000\u011d\u010f\u0001\u0000\u0000\u0000\u011e"+
		"\u0121\u0001\u0000\u0000\u0000\u011f\u011d\u0001\u0000\u0000\u0000\u011f"+
		"\u0120\u0001\u0000\u0000\u0000\u0120\u0123\u0001\u0000\u0000\u0000\u0121"+
		"\u011f\u0001\u0000\u0000\u0000\u0122\u0124\u0005\u0005\u0000\u0000\u0123"+
		"\u0122\u0001\u0000\u0000\u0000\u0123\u0124\u0001\u0000\u0000\u0000\u0124"+
		"\u0011\u0001\u0000\u0000\u0000\u0125\u0134\u0005+\u0000\u0000\u0126\u0128"+
		"\u0005\u0004\u0000\u0000\u0127\u0126\u0001\u0000\u0000\u0000\u0128\u012b"+
		"\u0001\u0000\u0000\u0000\u0129\u0127\u0001\u0000\u0000\u0000\u0129\u012a"+
		"\u0001\u0000\u0000\u0000\u012a\u012c\u0001\u0000\u0000\u0000\u012b\u0129"+
		"\u0001\u0000\u0000\u0000\u012c\u0130\u0005\u0013\u0000\u0000\u012d\u012f"+
		"\u0005\u0004\u0000\u0000\u012e\u012d\u0001\u0000\u0000\u0000\u012f\u0132"+
		"\u0001\u0000\u0000\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0130\u0131"+
		"\u0001\u0000\u0000\u0000\u0131\u0133\u0001\u0000\u0000\u0000\u0132\u0130"+
		"\u0001\u0000\u0000\u0000\u0133\u0135\u0005+\u0000\u0000\u0134\u0129\u0001"+
		"\u0000\u0000\u0000\u0134\u0135\u0001\u0000\u0000\u0000\u0135\u0013\u0001"+
		"\u0000\u0000\u0000\u0136\u013a\u0005\u001b\u0000\u0000\u0137\u0139\u0005"+
		"\u0004\u0000\u0000\u0138\u0137\u0001\u0000\u0000\u0000\u0139\u013c\u0001"+
		"\u0000\u0000\u0000\u013a\u0138\u0001\u0000\u0000\u0000\u013a\u013b\u0001"+
		"\u0000\u0000\u0000\u013b\u013d\u0001\u0000\u0000\u0000\u013c\u013a\u0001"+
		"\u0000\u0000\u0000\u013d\u0141\u0005+\u0000\u0000\u013e\u0140\u0005\u0004"+
		"\u0000\u0000\u013f\u013e\u0001\u0000\u0000\u0000\u0140\u0143\u0001\u0000"+
		"\u0000\u0000\u0141\u013f\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000"+
		"\u0000\u0000\u0142\u0144\u0001\u0000\u0000\u0000\u0143\u0141\u0001\u0000"+
		"\u0000\u0000\u0144\u0148\u0005\n\u0000\u0000\u0145\u0147\u0005\u0004\u0000"+
		"\u0000\u0146\u0145\u0001\u0000\u0000\u0000\u0147\u014a\u0001\u0000\u0000"+
		"\u0000\u0148\u0146\u0001\u0000\u0000\u0000\u0148\u0149\u0001\u0000\u0000"+
		"\u0000\u0149\u0168\u0001\u0000\u0000\u0000\u014a\u0148\u0001\u0000\u0000"+
		"\u0000\u014b\u014f\u0003\n\u0005\u0000\u014c\u014e\u0005\u0004\u0000\u0000"+
		"\u014d\u014c\u0001\u0000\u0000\u0000\u014e\u0151\u0001\u0000\u0000\u0000"+
		"\u014f\u014d\u0001\u0000\u0000\u0000\u014f\u0150\u0001\u0000\u0000\u0000"+
		"\u0150\u0162\u0001\u0000\u0000\u0000\u0151\u014f\u0001\u0000\u0000\u0000"+
		"\u0152\u0156\u0005\u0005\u0000\u0000\u0153\u0155\u0005\u0004\u0000\u0000"+
		"\u0154\u0153\u0001\u0000\u0000\u0000\u0155\u0158\u0001\u0000\u0000\u0000"+
		"\u0156\u0154\u0001\u0000\u0000\u0000\u0156\u0157\u0001\u0000\u0000\u0000"+
		"\u0157\u0159\u0001\u0000\u0000\u0000\u0158\u0156\u0001\u0000\u0000\u0000"+
		"\u0159\u015d\u0003\n\u0005\u0000\u015a\u015c\u0005\u0004\u0000\u0000\u015b"+
		"\u015a\u0001\u0000\u0000\u0000\u015c\u015f\u0001\u0000\u0000\u0000\u015d"+
		"\u015b\u0001\u0000\u0000\u0000\u015d\u015e\u0001\u0000\u0000\u0000\u015e"+
		"\u0161\u0001\u0000\u0000\u0000\u015f\u015d\u0001\u0000\u0000\u0000\u0160"+
		"\u0152\u0001\u0000\u0000\u0000\u0161\u0164\u0001\u0000\u0000\u0000\u0162"+
		"\u0160\u0001\u0000\u0000\u0000\u0162\u0163\u0001\u0000\u0000\u0000\u0163"+
		"\u0166\u0001\u0000\u0000\u0000\u0164\u0162\u0001\u0000\u0000\u0000\u0165"+
		"\u0167\u0005\u0005\u0000\u0000\u0166\u0165\u0001\u0000\u0000\u0000\u0166"+
		"\u0167\u0001\u0000\u0000\u0000\u0167\u0169\u0001\u0000\u0000\u0000\u0168"+
		"\u014b\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169"+
		"\u016d\u0001\u0000\u0000\u0000\u016a\u016c\u0005\u0004\u0000\u0000\u016b"+
		"\u016a\u0001\u0000\u0000\u0000\u016c\u016f\u0001\u0000\u0000\u0000\u016d"+
		"\u016b\u0001\u0000\u0000\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016e"+
		"\u0170\u0001\u0000\u0000\u0000\u016f\u016d\u0001\u0000\u0000\u0000\u0170"+
		"\u0174\u0005\u000b\u0000\u0000\u0171\u0173\u0005\u0004\u0000\u0000\u0172"+
		"\u0171\u0001\u0000\u0000\u0000\u0173\u0176\u0001\u0000\u0000\u0000\u0174"+
		"\u0172\u0001\u0000\u0000\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175"+
		"\u0178\u0001\u0000\u0000\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0177"+
		"\u0179\u0003\u0016\u000b\u0000\u0178\u0177\u0001\u0000\u0000\u0000\u0178"+
		"\u0179\u0001\u0000\u0000\u0000\u0179\u017d\u0001\u0000\u0000\u0000\u017a"+
		"\u017c\u0005\u0004\u0000\u0000\u017b\u017a\u0001\u0000\u0000\u0000\u017c"+
		"\u017f\u0001\u0000\u0000\u0000\u017d\u017b\u0001\u0000\u0000\u0000\u017d"+
		"\u017e\u0001\u0000\u0000\u0000\u017e\u0180\u0001\u0000\u0000\u0000\u017f"+
		"\u017d\u0001\u0000\u0000\u0000\u0180\u0181\u0003$\u0012\u0000\u0181\u0015"+
		"\u0001\u0000\u0000\u0000\u0182\u0186\u0005%\u0000\u0000\u0183\u0185\u0005"+
		"\u0004\u0000\u0000\u0184\u0183\u0001\u0000\u0000\u0000\u0185\u0188\u0001"+
		"\u0000\u0000\u0000\u0186\u0184\u0001\u0000\u0000\u0000\u0186\u0187\u0001"+
		"\u0000\u0000\u0000\u0187\u0189\u0001\u0000\u0000\u0000\u0188\u0186\u0001"+
		"\u0000\u0000\u0000\u0189\u018a\u0003\u0002\u0001\u0000\u018a\u0017\u0001"+
		"\u0000\u0000\u0000\u018b\u018f\u0005 \u0000\u0000\u018c\u018e\u0005\u0004"+
		"\u0000\u0000\u018d\u018c\u0001\u0000\u0000\u0000\u018e\u0191\u0001\u0000"+
		"\u0000\u0000\u018f\u018d\u0001\u0000\u0000\u0000\u018f\u0190\u0001\u0000"+
		"\u0000\u0000\u0190\u0193\u0001\u0000\u0000\u0000\u0191\u018f\u0001\u0000"+
		"\u0000\u0000\u0192\u0194\u0005(\u0000\u0000\u0193\u0192\u0001\u0000\u0000"+
		"\u0000\u0193\u0194\u0001\u0000\u0000\u0000\u0194\u0198\u0001\u0000\u0000"+
		"\u0000\u0195\u0197\u0005\u0004\u0000\u0000\u0196\u0195\u0001\u0000\u0000"+
		"\u0000\u0197\u019a\u0001\u0000\u0000\u0000\u0198\u0196\u0001\u0000\u0000"+
		"\u0000\u0198\u0199\u0001\u0000\u0000\u0000\u0199\u019b\u0001\u0000\u0000"+
		"\u0000\u019a\u0198\u0001\u0000\u0000\u0000\u019b\u019f\u0005+\u0000\u0000"+
		"\u019c\u019e\u0005\u0004\u0000\u0000\u019d\u019c\u0001\u0000\u0000\u0000"+
		"\u019e\u01a1\u0001\u0000\u0000\u0000\u019f\u019d\u0001\u0000\u0000\u0000"+
		"\u019f\u01a0\u0001\u0000\u0000\u0000\u01a0\u01b0\u0001\u0000\u0000\u0000"+
		"\u01a1\u019f\u0001\u0000\u0000\u0000\u01a2\u01a6\u0005\u0006\u0000\u0000"+
		"\u01a3\u01a5\u0005\u0004\u0000\u0000\u01a4\u01a3\u0001\u0000\u0000\u0000"+
		"\u01a5\u01a8\u0001\u0000\u0000\u0000\u01a6\u01a4\u0001\u0000\u0000\u0000"+
		"\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7\u01a9\u0001\u0000\u0000\u0000"+
		"\u01a8\u01a6\u0001\u0000\u0000\u0000\u01a9\u01ad\u0003\u0002\u0001\u0000"+
		"\u01aa\u01ac\u0005\u0004\u0000\u0000\u01ab\u01aa\u0001\u0000\u0000\u0000"+
		"\u01ac\u01af\u0001\u0000\u0000\u0000\u01ad\u01ab\u0001\u0000\u0000\u0000"+
		"\u01ad\u01ae\u0001\u0000\u0000\u0000\u01ae\u01b1\u0001\u0000\u0000\u0000"+
		"\u01af\u01ad\u0001\u0000\u0000\u0000\u01b0\u01a2\u0001\u0000\u0000\u0000"+
		"\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1\u01b2\u0001\u0000\u0000\u0000"+
		"\u01b2\u01b6\u0005\b\u0000\u0000\u01b3\u01b5\u0005\u0004\u0000\u0000\u01b4"+
		"\u01b3\u0001\u0000\u0000\u0000\u01b5\u01b8\u0001\u0000\u0000\u0000\u01b6"+
		"\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b7\u0001\u0000\u0000\u0000\u01b7"+
		"\u01b9\u0001\u0000\u0000\u0000\u01b8\u01b6\u0001\u0000\u0000\u0000\u01b9"+
		"\u01ba\u00036\u001b\u0000\u01ba\u0019\u0001\u0000\u0000\u0000\u01bb\u01bf"+
		"\u0005\"\u0000\u0000\u01bc\u01be\u0005\u0004\u0000\u0000\u01bd\u01bc\u0001"+
		"\u0000\u0000\u0000\u01be\u01c1\u0001\u0000\u0000\u0000\u01bf\u01bd\u0001"+
		"\u0000\u0000\u0000\u01bf\u01c0\u0001\u0000\u0000\u0000\u01c0\u01c2\u0001"+
		"\u0000\u0000\u0000\u01c1\u01bf\u0001\u0000\u0000\u0000\u01c2\u01c6\u0005"+
		"+\u0000\u0000\u01c3\u01c5\u0005\u0004\u0000\u0000\u01c4\u01c3\u0001\u0000"+
		"\u0000\u0000\u01c5\u01c8\u0001\u0000\u0000\u0000\u01c6\u01c4\u0001\u0000"+
		"\u0000\u0000\u01c6\u01c7\u0001\u0000\u0000\u0000\u01c7\u01ca\u0001\u0000"+
		"\u0000\u0000\u01c8\u01c6\u0001\u0000\u0000\u0000\u01c9\u01cb\u0003\u001e"+
		"\u000f\u0000\u01ca\u01c9\u0001\u0000\u0000\u0000\u01ca\u01cb\u0001\u0000"+
		"\u0000\u0000\u01cb\u001b\u0001\u0000\u0000\u0000\u01cc\u01d0\u0005\'\u0000"+
		"\u0000\u01cd\u01cf\u0005\u0004\u0000\u0000\u01ce\u01cd\u0001\u0000\u0000"+
		"\u0000\u01cf\u01d2\u0001\u0000\u0000\u0000\u01d0\u01ce\u0001\u0000\u0000"+
		"\u0000\u01d0\u01d1\u0001\u0000\u0000\u0000\u01d1\u01d3\u0001\u0000\u0000"+
		"\u0000\u01d2\u01d0\u0001\u0000\u0000\u0000\u01d3\u01d7\u0005+\u0000\u0000"+
		"\u01d4\u01d6\u0005\u0004\u0000\u0000\u01d5\u01d4\u0001\u0000\u0000\u0000"+
		"\u01d6\u01d9\u0001\u0000\u0000\u0000\u01d7\u01d5\u0001\u0000\u0000\u0000"+
		"\u01d7\u01d8\u0001\u0000\u0000\u0000\u01d8\u01db\u0001\u0000\u0000\u0000"+
		"\u01d9\u01d7\u0001\u0000\u0000\u0000\u01da\u01dc\u0003\u001e\u000f\u0000"+
		"\u01db\u01da\u0001\u0000\u0000\u0000\u01db\u01dc\u0001\u0000\u0000\u0000"+
		"\u01dc\u01e0\u0001\u0000\u0000\u0000\u01dd\u01df\u0005\u0004\u0000\u0000"+
		"\u01de\u01dd\u0001\u0000\u0000\u0000\u01df\u01e2\u0001\u0000\u0000\u0000"+
		"\u01e0\u01de\u0001\u0000\u0000\u0000\u01e0\u01e1\u0001\u0000\u0000\u0000"+
		"\u01e1\u01eb\u0001\u0000\u0000\u0000\u01e2\u01e0\u0001\u0000\u0000\u0000"+
		"\u01e3\u01e7\u0005\u0006\u0000\u0000\u01e4\u01e6\u0005\u0004\u0000\u0000"+
		"\u01e5\u01e4\u0001\u0000\u0000\u0000\u01e6\u01e9\u0001\u0000\u0000\u0000"+
		"\u01e7\u01e5\u0001\u0000\u0000\u0000\u01e7\u01e8\u0001\u0000\u0000\u0000"+
		"\u01e8\u01ea\u0001\u0000\u0000\u0000\u01e9\u01e7\u0001\u0000\u0000\u0000"+
		"\u01ea\u01ec\u0003\u0002\u0001\u0000\u01eb\u01e3\u0001\u0000\u0000\u0000"+
		"\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u001d\u0001\u0000\u0000\u0000"+
		"\u01ed\u01f1\u0005\n\u0000\u0000\u01ee\u01f0\u0005\u0004\u0000\u0000\u01ef"+
		"\u01ee\u0001\u0000\u0000\u0000\u01f0\u01f3\u0001\u0000\u0000\u0000\u01f1"+
		"\u01ef\u0001\u0000\u0000\u0000\u01f1\u01f2\u0001\u0000\u0000\u0000\u01f2"+
		"\u0211\u0001\u0000\u0000\u0000\u01f3\u01f1\u0001\u0000\u0000\u0000\u01f4"+
		"\u01f8\u0003 \u0010\u0000\u01f5\u01f7\u0005\u0004\u0000\u0000\u01f6\u01f5"+
		"\u0001\u0000\u0000\u0000\u01f7\u01fa\u0001\u0000\u0000\u0000\u01f8\u01f6"+
		"\u0001\u0000\u0000\u0000\u01f8\u01f9\u0001\u0000\u0000\u0000\u01f9\u020b"+
		"\u0001\u0000\u0000\u0000\u01fa\u01f8\u0001\u0000\u0000\u0000\u01fb\u01ff"+
		"\u0005\u0005\u0000\u0000\u01fc\u01fe\u0005\u0004\u0000\u0000\u01fd\u01fc"+
		"\u0001\u0000\u0000\u0000\u01fe\u0201\u0001\u0000\u0000\u0000\u01ff\u01fd"+
		"\u0001\u0000\u0000\u0000\u01ff\u0200\u0001\u0000\u0000\u0000\u0200\u0202"+
		"\u0001\u0000\u0000\u0000\u0201\u01ff\u0001\u0000\u0000\u0000\u0202\u0206"+
		"\u0003 \u0010\u0000\u0203\u0205\u0005\u0004\u0000\u0000\u0204\u0203\u0001"+
		"\u0000\u0000\u0000\u0205\u0208\u0001\u0000\u0000\u0000\u0206\u0204\u0001"+
		"\u0000\u0000\u0000\u0206\u0207\u0001\u0000\u0000\u0000\u0207\u020a\u0001"+
		"\u0000\u0000\u0000\u0208\u0206\u0001\u0000\u0000\u0000\u0209\u01fb\u0001"+
		"\u0000\u0000\u0000\u020a\u020d\u0001\u0000\u0000\u0000\u020b\u0209\u0001"+
		"\u0000\u0000\u0000\u020b\u020c\u0001\u0000\u0000\u0000\u020c\u020f\u0001"+
		"\u0000\u0000\u0000\u020d\u020b\u0001\u0000\u0000\u0000\u020e\u0210\u0005"+
		"\u0005\u0000\u0000\u020f\u020e\u0001\u0000\u0000\u0000\u020f\u0210\u0001"+
		"\u0000\u0000\u0000\u0210\u0212\u0001\u0000\u0000\u0000\u0211\u01f4\u0001"+
		"\u0000\u0000\u0000\u0211\u0212\u0001\u0000\u0000\u0000\u0212\u0216\u0001"+
		"\u0000\u0000\u0000\u0213\u0215\u0005\u0004\u0000\u0000\u0214\u0213\u0001"+
		"\u0000\u0000\u0000\u0215\u0218\u0001\u0000\u0000\u0000\u0216\u0214\u0001"+
		"\u0000\u0000\u0000\u0216\u0217\u0001\u0000\u0000\u0000\u0217\u0219\u0001"+
		"\u0000\u0000\u0000\u0218\u0216\u0001\u0000\u0000\u0000\u0219\u021a\u0005"+
		"\u000b\u0000\u0000\u021a\u001f\u0001\u0000\u0000\u0000\u021b\u021f\u0005"+
		"+\u0000\u0000\u021c\u021e\u0005\u0004\u0000\u0000\u021d\u021c\u0001\u0000"+
		"\u0000\u0000\u021e\u0221\u0001\u0000\u0000\u0000\u021f\u021d\u0001\u0000"+
		"\u0000\u0000\u021f\u0220\u0001\u0000\u0000\u0000\u0220\u0222\u0001\u0000"+
		"\u0000\u0000\u0221\u021f\u0001\u0000\u0000\u0000\u0222\u0226\u0005\u0006"+
		"\u0000\u0000\u0223\u0225\u0005\u0004\u0000\u0000\u0224\u0223\u0001\u0000"+
		"\u0000\u0000\u0225\u0228\u0001\u0000\u0000\u0000\u0226\u0224\u0001\u0000"+
		"\u0000\u0000\u0226\u0227\u0001\u0000\u0000\u0000\u0227\u0229\u0001\u0000"+
		"\u0000\u0000\u0228\u0226\u0001\u0000\u0000\u0000\u0229\u022a\u0003\u0002"+
		"\u0001\u0000\u022a!\u0001\u0000\u0000\u0000\u022b\u022f\u0005#\u0000\u0000"+
		"\u022c\u022e\u0005\u0004\u0000\u0000\u022d\u022c\u0001\u0000\u0000\u0000"+
		"\u022e\u0231\u0001\u0000\u0000\u0000\u022f\u022d\u0001\u0000\u0000\u0000"+
		"\u022f\u0230\u0001\u0000\u0000\u0000\u0230\u0232\u0001\u0000\u0000\u0000"+
		"\u0231\u022f\u0001\u0000\u0000\u0000\u0232\u0236\u0005+\u0000\u0000\u0233"+
		"\u0235\u0005\u0004\u0000\u0000\u0234\u0233\u0001\u0000\u0000\u0000\u0235"+
		"\u0238\u0001\u0000\u0000\u0000\u0236\u0234\u0001\u0000\u0000\u0000\u0236"+
		"\u0237\u0001\u0000\u0000\u0000\u0237\u0239\u0001\u0000\u0000\u0000\u0238"+
		"\u0236\u0001\u0000\u0000\u0000\u0239\u023d\u0005\n\u0000\u0000\u023a\u023c"+
		"\u0005\u0004\u0000\u0000\u023b\u023a\u0001\u0000\u0000\u0000\u023c\u023f"+
		"\u0001\u0000\u0000\u0000\u023d\u023b\u0001\u0000\u0000\u0000\u023d\u023e"+
		"\u0001\u0000\u0000\u0000\u023e\u025d\u0001\u0000\u0000\u0000\u023f\u023d"+
		"\u0001\u0000\u0000\u0000\u0240\u0244\u0003\u0002\u0001\u0000\u0241\u0243"+
		"\u0005\u0004\u0000\u0000\u0242\u0241\u0001\u0000\u0000\u0000\u0243\u0246"+
		"\u0001\u0000\u0000\u0000\u0244\u0242\u0001\u0000\u0000\u0000\u0244\u0245"+
		"\u0001\u0000\u0000\u0000\u0245\u0257\u0001\u0000\u0000\u0000\u0246\u0244"+
		"\u0001\u0000\u0000\u0000\u0247\u024b\u0005\u0005\u0000\u0000\u0248\u024a"+
		"\u0005\u0004\u0000\u0000\u0249\u0248\u0001\u0000\u0000\u0000\u024a\u024d"+
		"\u0001\u0000\u0000\u0000\u024b\u0249\u0001\u0000\u0000\u0000\u024b\u024c"+
		"\u0001\u0000\u0000\u0000\u024c\u024e\u0001\u0000\u0000\u0000\u024d\u024b"+
		"\u0001\u0000\u0000\u0000\u024e\u0252\u0003\u0002\u0001\u0000\u024f\u0251"+
		"\u0005\u0004\u0000\u0000\u0250\u024f\u0001\u0000\u0000\u0000\u0251\u0254"+
		"\u0001\u0000\u0000\u0000\u0252\u0250\u0001\u0000\u0000\u0000\u0252\u0253"+
		"\u0001\u0000\u0000\u0000\u0253\u0256\u0001\u0000\u0000\u0000\u0254\u0252"+
		"\u0001\u0000\u0000\u0000\u0255\u0247\u0001\u0000\u0000\u0000\u0256\u0259"+
		"\u0001\u0000\u0000\u0000\u0257\u0255\u0001\u0000\u0000\u0000\u0257\u0258"+
		"\u0001\u0000\u0000\u0000\u0258\u025b\u0001\u0000\u0000\u0000\u0259\u0257"+
		"\u0001\u0000\u0000\u0000\u025a\u025c\u0005\u0005\u0000\u0000\u025b\u025a"+
		"\u0001\u0000\u0000\u0000\u025b\u025c\u0001\u0000\u0000\u0000\u025c\u025e"+
		"\u0001\u0000\u0000\u0000\u025d\u0240\u0001\u0000\u0000\u0000\u025d\u025e"+
		"\u0001\u0000\u0000\u0000\u025e\u0262\u0001\u0000\u0000\u0000\u025f\u0261"+
		"\u0005\u0004\u0000\u0000\u0260\u025f\u0001\u0000\u0000\u0000\u0261\u0264"+
		"\u0001\u0000\u0000\u0000\u0262\u0260\u0001\u0000\u0000\u0000\u0262\u0263"+
		"\u0001\u0000\u0000\u0000\u0263\u0265\u0001\u0000\u0000\u0000\u0264\u0262"+
		"\u0001\u0000\u0000\u0000\u0265\u0266\u0005\u000b\u0000\u0000\u0266#\u0001"+
		"\u0000\u0000\u0000\u0267\u026a\u0003&\u0013\u0000\u0268\u026a\u0003*\u0015"+
		"\u0000\u0269\u0267\u0001\u0000\u0000\u0000\u0269\u0268\u0001\u0000\u0000"+
		"\u0000\u026a%\u0001\u0000\u0000\u0000\u026b\u026f\u0005\f\u0000\u0000"+
		"\u026c\u026e\u0005\u0004\u0000\u0000\u026d\u026c\u0001\u0000\u0000\u0000"+
		"\u026e\u0271\u0001\u0000\u0000\u0000\u026f\u026d\u0001\u0000\u0000\u0000"+
		"\u026f\u0270\u0001\u0000\u0000\u0000\u0270\u027e\u0001\u0000\u0000\u0000"+
		"\u0271\u026f\u0001\u0000\u0000\u0000\u0272\u027b\u0003,\u0016\u0000\u0273"+
		"\u0275\u0005\u0004\u0000\u0000\u0274\u0273\u0001\u0000\u0000\u0000\u0275"+
		"\u0276\u0001\u0000\u0000\u0000\u0276\u0274\u0001\u0000\u0000\u0000\u0276"+
		"\u0277\u0001\u0000\u0000\u0000\u0277\u0278\u0001\u0000\u0000\u0000\u0278"+
		"\u027a\u0003,\u0016\u0000\u0279\u0274\u0001\u0000\u0000\u0000\u027a\u027d"+
		"\u0001\u0000\u0000\u0000\u027b\u0279\u0001\u0000\u0000\u0000\u027b\u027c"+
		"\u0001\u0000\u0000\u0000\u027c\u027f\u0001\u0000\u0000\u0000\u027d\u027b"+
		"\u0001\u0000\u0000\u0000\u027e\u0272\u0001\u0000\u0000\u0000\u027e\u027f"+
		"\u0001\u0000\u0000\u0000\u027f\u0283\u0001\u0000\u0000\u0000\u0280\u0282"+
		"\u0005\u0004\u0000\u0000\u0281\u0280\u0001\u0000\u0000\u0000\u0282\u0285"+
		"\u0001\u0000\u0000\u0000\u0283\u0281\u0001\u0000\u0000\u0000\u0283\u0284"+
		"\u0001\u0000\u0000\u0000\u0284\u0287\u0001\u0000\u0000\u0000\u0285\u0283"+
		"\u0001\u0000\u0000\u0000\u0286\u0288\u0003(\u0014\u0000\u0287\u0286\u0001"+
		"\u0000\u0000\u0000\u0287\u0288\u0001\u0000\u0000\u0000\u0288\u028c\u0001"+
		"\u0000\u0000\u0000\u0289\u028b\u0005\u0004\u0000\u0000\u028a\u0289\u0001"+
		"\u0000\u0000\u0000\u028b\u028e\u0001\u0000\u0000\u0000\u028c\u028a\u0001"+
		"\u0000\u0000\u0000\u028c\u028d\u0001\u0000\u0000\u0000\u028d\u028f\u0001"+
		"\u0000\u0000\u0000\u028e\u028c\u0001\u0000\u0000\u0000\u028f\u0290\u0005"+
		"\r\u0000\u0000\u0290\'\u0001\u0000\u0000\u0000\u0291\u0292\u0005\u0010"+
		"\u0000\u0000\u0292\u0293\u00036\u001b\u0000\u0293)\u0001\u0000\u0000\u0000"+
		"\u0294\u0298\u0005\u0007\u0000\u0000\u0295\u0297\u0005\u0004\u0000\u0000"+
		"\u0296\u0295\u0001\u0000\u0000\u0000\u0297\u029a\u0001\u0000\u0000\u0000"+
		"\u0298\u0296\u0001\u0000\u0000\u0000\u0298\u0299\u0001\u0000\u0000\u0000"+
		"\u0299\u029b\u0001\u0000\u0000\u0000\u029a\u0298\u0001\u0000\u0000\u0000"+
		"\u029b\u029c\u00036\u001b\u0000\u029c+\u0001\u0000\u0000\u0000\u029d\u02a2"+
		"\u00032\u0019\u0000\u029e\u02a2\u00034\u001a\u0000\u029f\u02a2\u00030"+
		"\u0018\u0000\u02a0\u02a2\u0003.\u0017\u0000\u02a1\u029d\u0001\u0000\u0000"+
		"\u0000\u02a1\u029e\u0001\u0000\u0000\u0000\u02a1\u029f\u0001\u0000\u0000"+
		"\u0000\u02a1\u02a0\u0001\u0000\u0000\u0000\u02a2-\u0001\u0000\u0000\u0000"+
		"\u02a3\u02a4\u00036\u001b\u0000\u02a4/\u0001\u0000\u0000\u0000\u02a5\u02a6"+
		"\u0003\f\u0006\u0000\u02a61\u0001\u0000\u0000\u0000\u02a7\u02ab\u0005"+
		"\u0017\u0000\u0000\u02a8\u02aa\u0005\u0004\u0000\u0000\u02a9\u02a8\u0001"+
		"\u0000\u0000\u0000\u02aa\u02ad\u0001\u0000\u0000\u0000\u02ab\u02a9\u0001"+
		"\u0000\u0000\u0000\u02ab\u02ac\u0001\u0000\u0000\u0000\u02ac\u02ae\u0001"+
		"\u0000\u0000\u0000\u02ad\u02ab\u0001\u0000\u0000\u0000\u02ae\u02b2\u0005"+
		"\u001a\u0000\u0000\u02af\u02b1\u0005\u0004\u0000\u0000\u02b0\u02af\u0001"+
		"\u0000\u0000\u0000\u02b1\u02b4\u0001\u0000\u0000\u0000\u02b2\u02b0\u0001"+
		"\u0000\u0000\u0000\u02b2\u02b3\u0001\u0000\u0000\u0000\u02b3\u02b5\u0001"+
		"\u0000\u0000\u0000\u02b4\u02b2\u0001\u0000\u0000\u0000\u02b5\u02b9\u0003"+
		"d2\u0000\u02b6\u02b8\u0005\u0004\u0000\u0000\u02b7\u02b6\u0001\u0000\u0000"+
		"\u0000\u02b8\u02bb\u0001\u0000\u0000\u0000\u02b9\u02b7\u0001\u0000\u0000"+
		"\u0000\u02b9\u02ba\u0001\u0000\u0000\u0000\u02ba\u02bc\u0001\u0000\u0000"+
		"\u0000\u02bb\u02b9\u0001\u0000\u0000\u0000\u02bc\u02bd\u0003$\u0012\u0000"+
		"\u02bd3\u0001\u0000\u0000\u0000\u02be\u02c2\u0005&\u0000\u0000\u02bf\u02c1"+
		"\u0005\u0004\u0000\u0000\u02c0\u02bf\u0001\u0000\u0000\u0000\u02c1\u02c4"+
		"\u0001\u0000\u0000\u0000\u02c2\u02c0\u0001\u0000\u0000\u0000\u02c2\u02c3"+
		"\u0001\u0000\u0000\u0000\u02c3\u02c5\u0001\u0000\u0000\u0000\u02c4\u02c2"+
		"\u0001\u0000\u0000\u0000\u02c5\u02c9\u0005+\u0000\u0000\u02c6\u02c8\u0005"+
		"\u0004\u0000\u0000\u02c7\u02c6\u0001\u0000\u0000\u0000\u02c8\u02cb\u0001"+
		"\u0000\u0000\u0000\u02c9\u02c7\u0001\u0000\u0000\u0000\u02c9\u02ca\u0001"+
		"\u0000\u0000\u0000\u02ca\u02cc\u0001\u0000\u0000\u0000\u02cb\u02c9\u0001"+
		"\u0000\u0000\u0000\u02cc\u02d0\u0005\b\u0000\u0000\u02cd\u02cf\u0005\u0004"+
		"\u0000\u0000\u02ce\u02cd\u0001\u0000\u0000\u0000\u02cf\u02d2\u0001\u0000"+
		"\u0000\u0000\u02d0\u02ce\u0001\u0000\u0000\u0000\u02d0\u02d1\u0001\u0000"+
		"\u0000\u0000\u02d1\u02d3\u0001\u0000\u0000\u0000\u02d2\u02d0\u0001\u0000"+
		"\u0000\u0000\u02d3\u02d4\u00036\u001b\u0000\u02d45\u0001\u0000\u0000\u0000"+
		"\u02d5\u02e1\u00038\u001c\u0000\u02d6\u02e1\u0003:\u001d\u0000\u02d7\u02e1"+
		"\u0003<\u001e\u0000\u02d8\u02e1\u0003>\u001f\u0000\u02d9\u02e1\u0003B"+
		"!\u0000\u02da\u02e1\u0003D\"\u0000\u02db\u02e1\u0003F#\u0000\u02dc\u02e1"+
		"\u0003H$\u0000\u02dd\u02e1\u0003J%\u0000\u02de\u02e1\u0003L&\u0000\u02df"+
		"\u02e1\u0003N\'\u0000\u02e0\u02d5\u0001\u0000\u0000\u0000\u02e0\u02d6"+
		"\u0001\u0000\u0000\u0000\u02e0\u02d7\u0001\u0000\u0000\u0000\u02e0\u02d8"+
		"\u0001\u0000\u0000\u0000\u02e0\u02d9\u0001\u0000\u0000\u0000\u02e0\u02da"+
		"\u0001\u0000\u0000\u0000\u02e0\u02db\u0001\u0000\u0000\u0000\u02e0\u02dc"+
		"\u0001\u0000\u0000\u0000\u02e0\u02dd\u0001\u0000\u0000\u0000\u02e0\u02de"+
		"\u0001\u0000\u0000\u0000\u02e0\u02df\u0001\u0000\u0000\u0000\u02e1\u02e5"+
		"\u0001\u0000\u0000\u0000\u02e2\u02e4\u0003P(\u0000\u02e3\u02e2\u0001\u0000"+
		"\u0000\u0000\u02e4\u02e7\u0001\u0000\u0000\u0000\u02e5\u02e3\u0001\u0000"+
		"\u0000\u0000\u02e5\u02e6\u0001\u0000\u0000\u0000\u02e67\u0001\u0000\u0000"+
		"\u0000\u02e7\u02e5\u0001\u0000\u0000\u0000\u02e8\u02ec\u0005\n\u0000\u0000"+
		"\u02e9\u02eb\u0005\u0004\u0000\u0000\u02ea\u02e9\u0001\u0000\u0000\u0000"+
		"\u02eb\u02ee\u0001\u0000\u0000\u0000\u02ec\u02ea\u0001\u0000\u0000\u0000"+
		"\u02ec\u02ed\u0001\u0000\u0000\u0000\u02ed\u02ef\u0001\u0000\u0000\u0000"+
		"\u02ee\u02ec\u0001\u0000\u0000\u0000\u02ef\u02f3\u00036\u001b\u0000\u02f0"+
		"\u02f2\u0005\u0004\u0000\u0000\u02f1\u02f0\u0001\u0000\u0000\u0000\u02f2"+
		"\u02f5\u0001\u0000\u0000\u0000\u02f3\u02f1\u0001\u0000\u0000\u0000\u02f3"+
		"\u02f4\u0001\u0000\u0000\u0000\u02f4\u02f6\u0001\u0000\u0000\u0000\u02f5"+
		"\u02f3\u0001\u0000\u0000\u0000\u02f6\u02f7\u0005\u000b\u0000\u0000\u02f7"+
		"9\u0001\u0000\u0000\u0000\u02f8\u02f9\u0003&\u0013\u0000\u02f9;\u0001"+
		"\u0000\u0000\u0000\u02fa\u02fe\u0005\u0019\u0000\u0000\u02fb\u02fd\u0005"+
		"\u0004\u0000\u0000\u02fc\u02fb\u0001\u0000\u0000\u0000\u02fd\u0300\u0001"+
		"\u0000\u0000\u0000\u02fe\u02fc\u0001\u0000\u0000\u0000\u02fe\u02ff\u0001"+
		"\u0000\u0000\u0000\u02ff\u0301\u0001\u0000\u0000\u0000\u0300\u02fe\u0001"+
		"\u0000\u0000\u0000\u0301\u0305\u0005\n\u0000\u0000\u0302\u0304\u0005\u0004"+
		"\u0000\u0000\u0303\u0302\u0001\u0000\u0000\u0000\u0304\u0307\u0001\u0000"+
		"\u0000\u0000\u0305\u0303\u0001\u0000\u0000\u0000\u0305\u0306\u0001\u0000"+
		"\u0000\u0000\u0306\u0325\u0001\u0000\u0000\u0000\u0307\u0305\u0001\u0000"+
		"\u0000\u0000\u0308\u030c\u0003\n\u0005\u0000\u0309\u030b\u0005\u0004\u0000"+
		"\u0000\u030a\u0309\u0001\u0000\u0000\u0000\u030b\u030e\u0001\u0000\u0000"+
		"\u0000\u030c\u030a\u0001\u0000\u0000\u0000\u030c\u030d\u0001\u0000\u0000"+
		"\u0000\u030d\u031f\u0001\u0000\u0000\u0000\u030e\u030c\u0001\u0000\u0000"+
		"\u0000\u030f\u0313\u0005\u0005\u0000\u0000\u0310\u0312\u0005\u0004\u0000"+
		"\u0000\u0311\u0310\u0001\u0000\u0000\u0000\u0312\u0315\u0001\u0000\u0000"+
		"\u0000\u0313\u0311\u0001\u0000\u0000\u0000\u0313\u0314\u0001\u0000\u0000"+
		"\u0000\u0314\u0316\u0001\u0000\u0000\u0000\u0315\u0313\u0001\u0000\u0000"+
		"\u0000\u0316\u031a\u0003\n\u0005\u0000\u0317\u0319\u0005\u0004\u0000\u0000"+
		"\u0318\u0317\u0001\u0000\u0000\u0000\u0319\u031c\u0001\u0000\u0000\u0000"+
		"\u031a\u0318\u0001\u0000\u0000\u0000\u031a\u031b\u0001\u0000\u0000\u0000"+
		"\u031b\u031e\u0001\u0000\u0000\u0000\u031c\u031a\u0001\u0000\u0000\u0000"+
		"\u031d\u030f\u0001\u0000\u0000\u0000\u031e\u0321\u0001\u0000\u0000\u0000"+
		"\u031f\u031d\u0001\u0000\u0000\u0000\u031f\u0320\u0001\u0000\u0000\u0000"+
		"\u0320\u0323\u0001\u0000\u0000\u0000\u0321\u031f\u0001\u0000\u0000\u0000"+
		"\u0322\u0324\u0005\u0005\u0000\u0000\u0323\u0322\u0001\u0000\u0000\u0000"+
		"\u0323\u0324\u0001\u0000\u0000\u0000\u0324\u0326\u0001\u0000\u0000\u0000"+
		"\u0325\u0308\u0001\u0000\u0000\u0000\u0325\u0326\u0001\u0000\u0000\u0000"+
		"\u0326\u032a\u0001\u0000\u0000\u0000\u0327\u0329\u0005\u0004\u0000\u0000"+
		"\u0328\u0327\u0001\u0000\u0000\u0000\u0329\u032c\u0001\u0000\u0000\u0000"+
		"\u032a\u0328\u0001\u0000\u0000\u0000\u032a\u032b\u0001\u0000\u0000\u0000"+
		"\u032b\u032d\u0001\u0000\u0000\u0000\u032c\u032a\u0001\u0000\u0000\u0000"+
		"\u032d\u0331\u0005\u000b\u0000\u0000\u032e\u0330\u0005\u0004\u0000\u0000"+
		"\u032f\u032e\u0001\u0000\u0000\u0000\u0330\u0333\u0001\u0000\u0000\u0000"+
		"\u0331\u032f\u0001\u0000\u0000\u0000\u0331\u0332\u0001\u0000\u0000\u0000"+
		"\u0332\u0335\u0001\u0000\u0000\u0000\u0333\u0331\u0001\u0000\u0000\u0000"+
		"\u0334\u0336\u0003\u0016\u000b\u0000\u0335\u0334\u0001\u0000\u0000\u0000"+
		"\u0335\u0336\u0001\u0000\u0000\u0000\u0336\u033a\u0001\u0000\u0000\u0000"+
		"\u0337\u0339\u0005\u0004\u0000\u0000\u0338\u0337\u0001\u0000\u0000\u0000"+
		"\u0339\u033c\u0001\u0000\u0000\u0000\u033a\u0338\u0001\u0000\u0000\u0000"+
		"\u033a\u033b\u0001\u0000\u0000\u0000\u033b\u033d\u0001\u0000\u0000\u0000"+
		"\u033c\u033a\u0001\u0000\u0000\u0000\u033d\u033e\u00036\u001b\u0000\u033e"+
		"=\u0001\u0000\u0000\u0000\u033f\u0343\u0005\u0014\u0000\u0000\u0340\u0342"+
		"\u0005\u0004\u0000\u0000\u0341\u0340\u0001\u0000\u0000\u0000\u0342\u0345"+
		"\u0001\u0000\u0000\u0000\u0343\u0341\u0001\u0000\u0000\u0000\u0343\u0344"+
		"\u0001\u0000\u0000\u0000\u0344\u0347\u0001\u0000\u0000\u0000\u0345\u0343"+
		"\u0001\u0000\u0000\u0000\u0346\u0348\u0003@ \u0000\u0347\u0346\u0001\u0000"+
		"\u0000\u0000\u0347\u0348\u0001\u0000\u0000\u0000\u0348\u034c\u0001\u0000"+
		"\u0000\u0000\u0349\u034b\u0005\u0004\u0000\u0000\u034a\u0349\u0001\u0000"+
		"\u0000\u0000\u034b\u034e\u0001\u0000\u0000\u0000\u034c\u034a\u0001\u0000"+
		"\u0000\u0000\u034c\u034d\u0001\u0000\u0000\u0000\u034d\u034f\u0001\u0000"+
		"\u0000\u0000\u034e\u034c\u0001\u0000\u0000\u0000\u034f\u0353\u0005\f\u0000"+
		"\u0000\u0350\u0352\u0005\u0004\u0000\u0000\u0351\u0350\u0001\u0000\u0000"+
		"\u0000\u0352\u0355\u0001\u0000\u0000\u0000\u0353\u0351\u0001\u0000\u0000"+
		"\u0000\u0353\u0354\u0001\u0000\u0000\u0000\u0354\u0362\u0001\u0000\u0000"+
		"\u0000\u0355\u0353\u0001\u0000\u0000\u0000\u0356\u035f\u0003\\.\u0000"+
		"\u0357\u0359\u0005\u0004\u0000\u0000\u0358\u0357\u0001\u0000\u0000\u0000"+
		"\u0359\u035a\u0001\u0000\u0000\u0000\u035a\u0358\u0001\u0000\u0000\u0000"+
		"\u035a\u035b\u0001\u0000\u0000\u0000\u035b\u035c\u0001\u0000\u0000\u0000"+
		"\u035c\u035e\u0003\\.\u0000\u035d\u0358\u0001\u0000\u0000\u0000\u035e"+
		"\u0361\u0001\u0000\u0000\u0000\u035f\u035d\u0001\u0000\u0000\u0000\u035f"+
		"\u0360\u0001\u0000\u0000\u0000\u0360\u0363\u0001\u0000\u0000\u0000\u0361"+
		"\u035f\u0001\u0000\u0000\u0000\u0362\u0356\u0001\u0000\u0000\u0000\u0362"+
		"\u0363\u0001\u0000\u0000\u0000\u0363\u0367\u0001\u0000\u0000\u0000\u0364"+
		"\u0366\u0005\u0004\u0000\u0000\u0365\u0364\u0001\u0000\u0000\u0000\u0366"+
		"\u0369\u0001\u0000\u0000\u0000\u0367\u0365\u0001\u0000\u0000\u0000\u0367"+
		"\u0368\u0001\u0000\u0000\u0000\u0368\u036a\u0001\u0000\u0000\u0000\u0369"+
		"\u0367\u0001\u0000\u0000\u0000\u036a\u036b\u0005\r\u0000\u0000\u036b?"+
		"\u0001\u0000\u0000\u0000\u036c\u0370\u0005)\u0000\u0000\u036d\u036f\u0005"+
		"\u0004\u0000\u0000\u036e\u036d\u0001\u0000\u0000\u0000\u036f\u0372\u0001"+
		"\u0000\u0000\u0000\u0370\u036e\u0001\u0000\u0000\u0000\u0370\u0371\u0001"+
		"\u0000\u0000\u0000\u0371\u0373\u0001\u0000\u0000\u0000\u0372\u0370\u0001"+
		"\u0000\u0000\u0000\u0373\u0374\u00036\u001b\u0000\u0374A\u0001\u0000\u0000"+
		"\u0000\u0375\u0379\u0005!\u0000\u0000\u0376\u0378\u0005\u0004\u0000\u0000"+
		"\u0377\u0376\u0001\u0000\u0000\u0000\u0378\u037b\u0001\u0000\u0000\u0000"+
		"\u0379\u0377\u0001\u0000\u0000\u0000\u0379\u037a\u0001\u0000\u0000\u0000"+
		"\u037a\u037c\u0001\u0000\u0000\u0000\u037b\u0379\u0001\u0000\u0000\u0000"+
		"\u037c\u037d\u0003$\u0012\u0000\u037dC\u0001\u0000\u0000\u0000\u037e\u0382"+
		"\u0005\u0016\u0000\u0000\u037f\u0381\u0005\u0004\u0000\u0000\u0380\u037f"+
		"\u0001\u0000\u0000\u0000\u0381\u0384\u0001\u0000\u0000\u0000\u0382\u0380"+
		"\u0001\u0000\u0000\u0000\u0382\u0383\u0001\u0000\u0000\u0000\u0383\u0385"+
		"\u0001\u0000\u0000\u0000\u0384\u0382\u0001\u0000\u0000\u0000\u0385\u0386"+
		"\u00036\u001b\u0000\u0386E\u0001\u0000\u0000\u0000\u0387\u0389\u0005$"+
		"\u0000\u0000\u0388\u038a\u00036\u001b\u0000\u0389\u0388\u0001\u0000\u0000"+
		"\u0000\u0389\u038a\u0001\u0000\u0000\u0000\u038aG\u0001\u0000\u0000\u0000"+
		"\u038b\u0394\u0005\u0015\u0000\u0000\u038c\u0390\u0005)\u0000\u0000\u038d"+
		"\u038f\u0005\u0004\u0000\u0000\u038e\u038d\u0001\u0000\u0000\u0000\u038f"+
		"\u0392\u0001\u0000\u0000\u0000\u0390\u038e\u0001\u0000\u0000\u0000\u0390"+
		"\u0391\u0001\u0000\u0000\u0000\u0391\u0393\u0001\u0000\u0000\u0000\u0392"+
		"\u0390\u0001\u0000\u0000\u0000\u0393\u0395\u00036\u001b\u0000\u0394\u038c"+
		"\u0001\u0000\u0000\u0000\u0394\u0395\u0001\u0000\u0000\u0000\u0395I\u0001"+
		"\u0000\u0000\u0000\u0396\u0397\u0005\u0011\u0000\u0000\u0397K\u0001\u0000"+
		"\u0000\u0000\u0398\u0399\u0005\u0012\u0000\u0000\u0399M\u0001\u0000\u0000"+
		"\u0000\u039a\u039b\u0005+\u0000\u0000\u039bO\u0001\u0000\u0000\u0000\u039c"+
		"\u03a0\u0003R)\u0000\u039d\u03a0\u0003X,\u0000\u039e\u03a0\u0003Z-\u0000"+
		"\u039f\u039c\u0001\u0000\u0000\u0000\u039f\u039d\u0001\u0000\u0000\u0000"+
		"\u039f\u039e\u0001\u0000\u0000\u0000\u03a0Q\u0001\u0000\u0000\u0000\u03a1"+
		"\u03a5\u0005\n\u0000\u0000\u03a2\u03a4\u0005\u0004\u0000\u0000\u03a3\u03a2"+
		"\u0001\u0000\u0000\u0000\u03a4\u03a7\u0001\u0000\u0000\u0000\u03a5\u03a3"+
		"\u0001\u0000\u0000\u0000\u03a5\u03a6\u0001\u0000\u0000\u0000\u03a6\u03c5"+
		"\u0001\u0000\u0000\u0000\u03a7\u03a5\u0001\u0000\u0000\u0000\u03a8\u03ac"+
		"\u0003T*\u0000\u03a9\u03ab\u0005\u0004\u0000\u0000\u03aa\u03a9\u0001\u0000"+
		"\u0000\u0000\u03ab\u03ae\u0001\u0000\u0000\u0000\u03ac\u03aa\u0001\u0000"+
		"\u0000\u0000\u03ac\u03ad\u0001\u0000\u0000\u0000\u03ad\u03bf\u0001\u0000"+
		"\u0000\u0000\u03ae\u03ac\u0001\u0000\u0000\u0000\u03af\u03b3\u0005\u0005"+
		"\u0000\u0000\u03b0\u03b2\u0005\u0004\u0000\u0000\u03b1\u03b0\u0001\u0000"+
		"\u0000\u0000\u03b2\u03b5\u0001\u0000\u0000\u0000\u03b3\u03b1\u0001\u0000"+
		"\u0000\u0000\u03b3\u03b4\u0001\u0000\u0000\u0000\u03b4\u03b6\u0001\u0000"+
		"\u0000\u0000\u03b5\u03b3\u0001\u0000\u0000\u0000\u03b6\u03ba\u0003T*\u0000"+
		"\u03b7\u03b9\u0005\u0004\u0000\u0000\u03b8\u03b7\u0001\u0000\u0000\u0000"+
		"\u03b9\u03bc\u0001\u0000\u0000\u0000\u03ba\u03b8\u0001\u0000\u0000\u0000"+
		"\u03ba\u03bb\u0001\u0000\u0000\u0000\u03bb\u03be\u0001\u0000\u0000\u0000"+
		"\u03bc\u03ba\u0001\u0000\u0000\u0000\u03bd\u03af\u0001\u0000\u0000\u0000"+
		"\u03be\u03c1\u0001\u0000\u0000\u0000\u03bf\u03bd\u0001\u0000\u0000\u0000"+
		"\u03bf\u03c0\u0001\u0000\u0000\u0000\u03c0\u03c3\u0001\u0000\u0000\u0000"+
		"\u03c1\u03bf\u0001\u0000\u0000\u0000\u03c2\u03c4\u0005\u0005\u0000\u0000"+
		"\u03c3\u03c2\u0001\u0000\u0000\u0000\u03c3\u03c4\u0001\u0000\u0000\u0000"+
		"\u03c4\u03c6\u0001\u0000\u0000\u0000\u03c5\u03a8\u0001\u0000\u0000\u0000"+
		"\u03c5\u03c6\u0001\u0000\u0000\u0000\u03c6\u03ca\u0001\u0000\u0000\u0000"+
		"\u03c7\u03c9\u0005\u0004\u0000\u0000\u03c8\u03c7\u0001\u0000\u0000\u0000"+
		"\u03c9\u03cc\u0001\u0000\u0000\u0000\u03ca\u03c8\u0001\u0000\u0000\u0000"+
		"\u03ca\u03cb\u0001\u0000\u0000\u0000\u03cb\u03cd\u0001\u0000\u0000\u0000"+
		"\u03cc\u03ca\u0001\u0000\u0000\u0000\u03cd\u03ce\u0005\u000b\u0000\u0000"+
		"\u03ceS\u0001\u0000\u0000\u0000\u03cf\u03d0\u0003V+\u0000\u03d0U\u0001"+
		"\u0000\u0000\u0000\u03d1\u03d2\u00036\u001b\u0000\u03d2W\u0001\u0000\u0000"+
		"\u0000\u03d3\u03d5\u0005\u0004\u0000\u0000\u03d4\u03d3\u0001\u0000\u0000"+
		"\u0000\u03d5\u03d8\u0001\u0000\u0000\u0000\u03d6\u03d4\u0001\u0000\u0000"+
		"\u0000\u03d6\u03d7\u0001\u0000\u0000\u0000\u03d7\u03d9\u0001\u0000\u0000"+
		"\u0000\u03d8\u03d6\u0001\u0000\u0000\u0000\u03d9\u03dd\u0005\u000f\u0000"+
		"\u0000\u03da\u03dc\u0005\u0004\u0000\u0000\u03db\u03da\u0001\u0000\u0000"+
		"\u0000\u03dc\u03df\u0001\u0000\u0000\u0000\u03dd\u03db\u0001\u0000\u0000"+
		"\u0000\u03dd\u03de\u0001\u0000\u0000\u0000\u03de\u03e0\u0001\u0000\u0000"+
		"\u0000\u03df\u03dd\u0001\u0000\u0000\u0000\u03e0\u03e1\u0005+\u0000\u0000"+
		"\u03e1Y\u0001\u0000\u0000\u0000\u03e2\u03e4\u0005\u0004\u0000\u0000\u03e3"+
		"\u03e2\u0001\u0000\u0000\u0000\u03e4\u03e7\u0001\u0000\u0000\u0000\u03e5"+
		"\u03e3\u0001\u0000\u0000\u0000\u03e5\u03e6\u0001\u0000\u0000\u0000\u03e6"+
		"\u03e8\u0001\u0000\u0000\u0000\u03e7\u03e5\u0001\u0000\u0000\u0000\u03e8"+
		"\u03ec\u0005\t\u0000\u0000\u03e9\u03eb\u0005\u0004\u0000\u0000\u03ea\u03e9"+
		"\u0001\u0000\u0000\u0000\u03eb\u03ee\u0001\u0000\u0000\u0000\u03ec\u03ea"+
		"\u0001\u0000\u0000\u0000\u03ec\u03ed\u0001\u0000\u0000\u0000\u03ed\u03ef"+
		"\u0001\u0000\u0000\u0000\u03ee\u03ec\u0001\u0000\u0000\u0000\u03ef\u03f3"+
		"\u00036\u001b\u0000\u03f0\u03f2\u0005\u0004\u0000\u0000\u03f1\u03f0\u0001"+
		"\u0000\u0000\u0000\u03f2\u03f5\u0001\u0000\u0000\u0000\u03f3\u03f1\u0001"+
		"\u0000\u0000\u0000\u03f3\u03f4\u0001\u0000\u0000\u0000\u03f4\u03f6\u0001"+
		"\u0000\u0000\u0000\u03f5\u03f3\u0001\u0000\u0000\u0000\u03f6\u03fa\u0005"+
		"\n\u0000\u0000\u03f7\u03f9\u0005\u0004\u0000\u0000\u03f8\u03f7\u0001\u0000"+
		"\u0000\u0000\u03f9\u03fc\u0001\u0000\u0000\u0000\u03fa\u03f8\u0001\u0000"+
		"\u0000\u0000\u03fa\u03fb\u0001\u0000\u0000\u0000\u03fb\u041a\u0001\u0000"+
		"\u0000\u0000\u03fc\u03fa\u0001\u0000\u0000\u0000\u03fd\u0401\u0003T*\u0000"+
		"\u03fe\u0400\u0005\u0004\u0000\u0000\u03ff\u03fe\u0001\u0000\u0000\u0000"+
		"\u0400\u0403\u0001\u0000\u0000\u0000\u0401\u03ff\u0001\u0000\u0000\u0000"+
		"\u0401\u0402\u0001\u0000\u0000\u0000\u0402\u0414\u0001\u0000\u0000\u0000"+
		"\u0403\u0401\u0001\u0000\u0000\u0000\u0404\u0408\u0005\u0005\u0000\u0000"+
		"\u0405\u0407\u0005\u0004\u0000\u0000\u0406\u0405\u0001\u0000\u0000\u0000"+
		"\u0407\u040a\u0001\u0000\u0000\u0000\u0408\u0406\u0001\u0000\u0000\u0000"+
		"\u0408\u0409\u0001\u0000\u0000\u0000\u0409\u040b\u0001\u0000\u0000\u0000"+
		"\u040a\u0408\u0001\u0000\u0000\u0000\u040b\u040f\u0003T*\u0000\u040c\u040e"+
		"\u0005\u0004\u0000\u0000\u040d\u040c\u0001\u0000\u0000\u0000\u040e\u0411"+
		"\u0001\u0000\u0000\u0000\u040f\u040d\u0001\u0000\u0000\u0000\u040f\u0410"+
		"\u0001\u0000\u0000\u0000\u0410\u0413\u0001\u0000\u0000\u0000\u0411\u040f"+
		"\u0001\u0000\u0000\u0000\u0412\u0404\u0001\u0000\u0000\u0000\u0413\u0416"+
		"\u0001\u0000\u0000\u0000\u0414\u0412\u0001\u0000\u0000\u0000\u0414\u0415"+
		"\u0001\u0000\u0000\u0000\u0415\u0418\u0001\u0000\u0000\u0000\u0416\u0414"+
		"\u0001\u0000\u0000\u0000\u0417\u0419\u0005\u0005\u0000\u0000\u0418\u0417"+
		"\u0001\u0000\u0000\u0000\u0418\u0419\u0001\u0000\u0000\u0000\u0419\u041b"+
		"\u0001\u0000\u0000\u0000\u041a\u03fd\u0001\u0000\u0000\u0000\u041a\u041b"+
		"\u0001\u0000\u0000\u0000\u041b\u041f\u0001\u0000\u0000\u0000\u041c\u041e"+
		"\u0005\u0004\u0000\u0000\u041d\u041c\u0001\u0000\u0000\u0000\u041e\u0421"+
		"\u0001\u0000\u0000\u0000\u041f\u041d\u0001\u0000\u0000\u0000\u041f\u0420"+
		"\u0001\u0000\u0000\u0000\u0420\u0422\u0001\u0000\u0000\u0000\u0421\u041f"+
		"\u0001\u0000\u0000\u0000\u0422\u0423\u0005\u000b\u0000\u0000\u0423[\u0001"+
		"\u0000\u0000\u0000\u0424\u0428\u0003^/\u0000\u0425\u0428\u0003`0\u0000"+
		"\u0426\u0428\u0003b1\u0000\u0427\u0424\u0001\u0000\u0000\u0000\u0427\u0425"+
		"\u0001\u0000\u0000\u0000\u0427\u0426\u0001\u0000\u0000\u0000\u0428]\u0001"+
		"\u0000\u0000\u0000\u0429\u042a\u0005\u001d\u0000\u0000\u042a\u042b\u0003"+
		"6\u001b\u0000\u042b\u042c\u0003$\u0012\u0000\u042c_\u0001\u0000\u0000"+
		"\u0000\u042d\u042e\u0005\u001e\u0000\u0000\u042e\u042f\u0003d2\u0000\u042f"+
		"\u0430\u0003$\u0012\u0000\u0430a\u0001\u0000\u0000\u0000\u0431\u0432\u0005"+
		"\u0018\u0000\u0000\u0432\u0433\u0003$\u0012\u0000\u0433c\u0001\u0000\u0000"+
		"\u0000\u0434\u0437\u0003f3\u0000\u0435\u0437\u0003h4\u0000\u0436\u0434"+
		"\u0001\u0000\u0000\u0000\u0436\u0435\u0001\u0000\u0000\u0000\u0437e\u0001"+
		"\u0000\u0000\u0000\u0438\u043c\u0003\u0002\u0001\u0000\u0439\u043b\u0005"+
		"\u0004\u0000\u0000\u043a\u0439\u0001\u0000\u0000\u0000\u043b\u043e\u0001"+
		"\u0000\u0000\u0000\u043c\u043a\u0001\u0000\u0000\u0000\u043c\u043d\u0001"+
		"\u0000\u0000\u0000\u043d\u043f\u0001\u0000\u0000\u0000\u043e\u043c\u0001"+
		"\u0000\u0000\u0000\u043f\u0443\u0005\u0013\u0000\u0000\u0440\u0442\u0005"+
		"\u0004\u0000\u0000\u0441\u0440\u0001\u0000\u0000\u0000\u0442\u0445\u0001"+
		"\u0000\u0000\u0000\u0443\u0441\u0001\u0000\u0000\u0000\u0443\u0444\u0001"+
		"\u0000\u0000\u0000\u0444\u0446\u0001\u0000\u0000\u0000\u0445\u0443\u0001"+
		"\u0000\u0000\u0000\u0446\u0447\u0005+\u0000\u0000\u0447g\u0001\u0000\u0000"+
		"\u0000\u0448\u0449\u0003\u0002\u0001\u0000\u0449i\u0001\u0000\u0000\u0000"+
		"\u009cmty|\u0081\u0088\u0090\u0097\u009e\u00a5\u00ac\u00b1\u00b5\u00b7"+
		"\u00bc\u00c3\u00cc\u00d5\u00dc\u00e0\u00e8\u00ee\u00f5\u00fc\u0103\u010c"+
		"\u0113\u011a\u011f\u0123\u0129\u0130\u0134\u013a\u0141\u0148\u014f\u0156"+
		"\u015d\u0162\u0166\u0168\u016d\u0174\u0178\u017d\u0186\u018f\u0193\u0198"+
		"\u019f\u01a6\u01ad\u01b0\u01b6\u01bf\u01c6\u01ca\u01d0\u01d7\u01db\u01e0"+
		"\u01e7\u01eb\u01f1\u01f8\u01ff\u0206\u020b\u020f\u0211\u0216\u021f\u0226"+
		"\u022f\u0236\u023d\u0244\u024b\u0252\u0257\u025b\u025d\u0262\u0269\u026f"+
		"\u0276\u027b\u027e\u0283\u0287\u028c\u0298\u02a1\u02ab\u02b2\u02b9\u02c2"+
		"\u02c9\u02d0\u02e0\u02e5\u02ec\u02f3\u02fe\u0305\u030c\u0313\u031a\u031f"+
		"\u0323\u0325\u032a\u0331\u0335\u033a\u0343\u0347\u034c\u0353\u035a\u035f"+
		"\u0362\u0367\u0370\u0379\u0382\u0389\u0390\u0394\u039f\u03a5\u03ac\u03b3"+
		"\u03ba\u03bf\u03c3\u03c5\u03ca\u03d6\u03dd\u03e5\u03ec\u03f3\u03fa\u0401"+
		"\u0408\u040f\u0414\u0418\u041a\u041f\u0427\u0436\u043c\u0443";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}