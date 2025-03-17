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
		OBJECT=34, OPTION=35, ONE_OF_CAMELCASE=36, RETURN=37, RETURNS=38, SET=39, 
		SIGNAL=40, TYPE=41, VARIABLE=42, WITH=43, PATH=44, IDENTIFIER=45;
	public static final int
		RULE_file = 0, RULE_typeReference = 1, RULE_identifierTypeReference = 2, 
		RULE_functionTypeReference = 3, RULE_functionTypeReferenceReturnValue = 4, 
		RULE_oneOfTypeReference = 5, RULE_functionSignatureParam = 6, RULE_declaration = 7, 
		RULE_importDeclaration = 8, RULE_importMappings = 9, RULE_importMapping = 10, 
		RULE_functionDeclaration = 11, RULE_functionReturnValue = 12, RULE_namedValueDeclaration = 13, 
		RULE_typeAliasDeclaration = 14, RULE_objectDeclaration = 15, RULE_signalDeclaration = 16, 
		RULE_objectFields = 17, RULE_objectField = 18, RULE_body = 19, RULE_block = 20, 
		RULE_blockResult = 21, RULE_singleExpressionBody = 22, RULE_statement = 23, 
		RULE_expressionStatement = 24, RULE_declarationStatement = 25, RULE_effectStatement = 26, 
		RULE_expression = 27, RULE_groupExpression = 28, RULE_blockExpression = 29, 
		RULE_functionExpression = 30, RULE_branchExpression = 31, RULE_branchWith = 32, 
		RULE_loopExpression = 33, RULE_setExpression = 34, RULE_causeExpression = 35, 
		RULE_returnExpression = 36, RULE_breakExpression = 37, RULE_stringLiteralExpression = 38, 
		RULE_numberLiteralExpression = 39, RULE_identifierExpression = 40, RULE_expressionSuffix = 41, 
		RULE_callExpressionSuffix = 42, RULE_callParam = 43, RULE_callPositionalParameter = 44, 
		RULE_memberExpressionSuffix = 45, RULE_pipeCallExpressionSuffix = 46, 
		RULE_branchOption = 47, RULE_ifBranchOption = 48, RULE_isBranchOption = 49, 
		RULE_elseBranchOption = 50, RULE_pattern = 51, RULE_captureValuePattern = 52, 
		RULE_typeReferencePattern = 53;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "typeReference", "identifierTypeReference", "functionTypeReference", 
			"functionTypeReferenceReturnValue", "oneOfTypeReference", "functionSignatureParam", 
			"declaration", "importDeclaration", "importMappings", "importMapping", 
			"functionDeclaration", "functionReturnValue", "namedValueDeclaration", 
			"typeAliasDeclaration", "objectDeclaration", "signalDeclaration", "objectFields", 
			"objectField", "body", "block", "blockResult", "singleExpressionBody", 
			"statement", "expressionStatement", "declarationStatement", "effectStatement", 
			"expression", "groupExpression", "blockExpression", "functionExpression", 
			"branchExpression", "branchWith", "loopExpression", "setExpression", 
			"causeExpression", "returnExpression", "breakExpression", "stringLiteralExpression", 
			"numberLiteralExpression", "identifierExpression", "expressionSuffix", 
			"callExpressionSuffix", "callParam", "callPositionalParameter", "memberExpressionSuffix", 
			"pipeCallExpressionSuffix", "branchOption", "ifBranchOption", "isBranchOption", 
			"elseBranchOption", "pattern", "captureValuePattern", "typeReferencePattern"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'\\n'", "','", "':'", "'=>'", "'='", "'>>'", 
			"'('", "')'", "'{'", "'}'", "'_'", "'.'", "'^'", null, null, "'as'", 
			"'branch'", "'break'", "'cause'", "'effect'", "'else'", "'fn'", "'for'", 
			"'function'", "'Function'", "'if'", "'is'", "'import'", "'let'", "'loop'", 
			"'object'", "'option'", "'OneOf'", "'return'", "'returns'", "'set'", 
			"'signal'", "'type'", "'variable'", "'with'"
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
			"LOOP", "OBJECT", "OPTION", "ONE_OF_CAMELCASE", "RETURN", "RETURNS", 
			"SET", "SIGNAL", "TYPE", "VARIABLE", "WITH", "PATH", "IDENTIFIER"
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
			setState(111);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(108);
					match(NEWLINE);
					}
					} 
				}
				setState(113);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(126);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3322291421184L) != 0)) {
				{
				setState(114);
				declaration();
				setState(123);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(116); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(115);
							match(NEWLINE);
							}
							}
							setState(118); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==NEWLINE );
						setState(120);
						declaration();
						}
						} 
					}
					setState(125);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				}
			}

			setState(131);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(128);
				match(NEWLINE);
				}
				}
				setState(133);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(134);
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
		public OneOfTypeReferenceContext oneOfTypeReference() {
			return getRuleContext(OneOfTypeReferenceContext.class,0);
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
			setState(139);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FUNCTION_CAMEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				functionTypeReference();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(137);
				identifierTypeReference();
				}
				break;
			case ONE_OF_CAMELCASE:
				enterOuterAlt(_localctx, 3);
				{
				setState(138);
				oneOfTypeReference();
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
			setState(141);
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
			setState(143);
			match(FUNCTION_CAMEL);
			setState(147);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(144);
				match(NEWLINE);
				}
				}
				setState(149);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(150);
			match(PAREN_OPEN);
			setState(154);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(151);
					match(NEWLINE);
					}
					} 
				}
				setState(156);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			setState(186);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(157);
				functionSignatureParam();
				setState(161);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(158);
						match(NEWLINE);
						}
						} 
					}
					setState(163);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				}
				setState(180);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(164);
						match(COMMA);
						setState(168);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(165);
							match(NEWLINE);
							}
							}
							setState(170);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(171);
						functionSignatureParam();
						setState(175);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(172);
								match(NEWLINE);
								}
								} 
							}
							setState(177);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
						}
						}
						} 
					}
					setState(182);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(183);
					match(COMMA);
					}
				}

				}
			}

			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(188);
				match(NEWLINE);
				}
				}
				setState(193);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(194);
			match(PAREN_CLOSE);
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(195);
				match(NEWLINE);
				}
				}
				setState(200);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(201);
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
			setState(203);
			match(RETURNS);
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(204);
				match(NEWLINE);
				}
				}
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(210);
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
	public static class OneOfTypeReferenceContext extends ParserRuleContext {
		public TerminalNode ONE_OF_CAMELCASE() { return getToken(CauseParser.ONE_OF_CAMELCASE, 0); }
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
		public OneOfTypeReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_oneOfTypeReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterOneOfTypeReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitOneOfTypeReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitOneOfTypeReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OneOfTypeReferenceContext oneOfTypeReference() throws RecognitionException {
		OneOfTypeReferenceContext _localctx = new OneOfTypeReferenceContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_oneOfTypeReference);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			match(ONE_OF_CAMELCASE);
			setState(216);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(213);
				match(NEWLINE);
				}
				}
				setState(218);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(219);
			match(PAREN_OPEN);
			setState(223);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(220);
					match(NEWLINE);
					}
					} 
				}
				setState(225);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			setState(255);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35253360001024L) != 0)) {
				{
				setState(226);
				typeReference();
				setState(230);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(227);
						match(NEWLINE);
						}
						} 
					}
					setState(232);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				setState(249);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(233);
						match(COMMA);
						setState(237);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(234);
							match(NEWLINE);
							}
							}
							setState(239);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(240);
						typeReference();
						setState(244);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(241);
								match(NEWLINE);
								}
								} 
							}
							setState(246);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
						}
						}
						} 
					}
					setState(251);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				}
				setState(253);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(252);
					match(COMMA);
					}
				}

				}
			}

			setState(260);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(257);
				match(NEWLINE);
				}
				}
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(263);
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
		enterRule(_localctx, 12, RULE_functionSignatureParam);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
			match(IDENTIFIER);
			setState(269);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(266);
					match(NEWLINE);
					}
					} 
				}
				setState(271);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			}
			setState(280);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(272);
				match(COLON);
				setState(276);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(273);
					match(NEWLINE);
					}
					}
					setState(278);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(279);
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
		public TypeAliasDeclarationContext typeAliasDeclaration() {
			return getRuleContext(TypeAliasDeclarationContext.class,0);
		}
		public ObjectDeclarationContext objectDeclaration() {
			return getRuleContext(ObjectDeclarationContext.class,0);
		}
		public SignalDeclarationContext signalDeclaration() {
			return getRuleContext(SignalDeclarationContext.class,0);
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
		enterRule(_localctx, 14, RULE_declaration);
		try {
			setState(288);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IMPORT:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				importDeclaration();
				}
				break;
			case FUNCTION:
				enterOuterAlt(_localctx, 2);
				{
				setState(283);
				functionDeclaration();
				}
				break;
			case LET:
				enterOuterAlt(_localctx, 3);
				{
				setState(284);
				namedValueDeclaration();
				}
				break;
			case TYPE:
				enterOuterAlt(_localctx, 4);
				{
				setState(285);
				typeAliasDeclaration();
				}
				break;
			case OBJECT:
				enterOuterAlt(_localctx, 5);
				{
				setState(286);
				objectDeclaration();
				}
				break;
			case SIGNAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(287);
				signalDeclaration();
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
		enterRule(_localctx, 16, RULE_importDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(290);
			match(IMPORT);
			setState(294);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(291);
				match(NEWLINE);
				}
				}
				setState(296);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(297);
			match(PATH);
			setState(301);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(298);
				match(NEWLINE);
				}
				}
				setState(303);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(304);
			match(PAREN_OPEN);
			setState(308);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(305);
				match(NEWLINE);
				}
				}
				setState(310);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(311);
			importMappings();
			setState(315);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(312);
				match(NEWLINE);
				}
				}
				setState(317);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(318);
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
		enterRule(_localctx, 18, RULE_importMappings);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			importMapping();
			setState(324);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(321);
					match(NEWLINE);
					}
					} 
				}
				setState(326);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			}
			setState(343);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(327);
					match(COMMA);
					setState(331);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==NEWLINE) {
						{
						{
						setState(328);
						match(NEWLINE);
						}
						}
						setState(333);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(334);
					importMapping();
					setState(338);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(335);
							match(NEWLINE);
							}
							} 
						}
						setState(340);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
					}
					}
					} 
				}
				setState(345);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
			}
			setState(347);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(346);
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
		enterRule(_localctx, 20, RULE_importMapping);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(349);
			match(IDENTIFIER);
			setState(364);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(353);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(350);
					match(NEWLINE);
					}
					}
					setState(355);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(356);
				match(AS);
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(357);
					match(NEWLINE);
					}
					}
					setState(362);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(363);
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
		enterRule(_localctx, 22, RULE_functionDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			match(FUNCTION);
			setState(370);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(367);
				match(NEWLINE);
				}
				}
				setState(372);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(373);
			match(IDENTIFIER);
			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(374);
				match(NEWLINE);
				}
				}
				setState(379);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(380);
			match(PAREN_OPEN);
			setState(384);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(381);
					match(NEWLINE);
					}
					} 
				}
				setState(386);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			}
			setState(416);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(387);
				functionSignatureParam();
				setState(391);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(388);
						match(NEWLINE);
						}
						} 
					}
					setState(393);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
				}
				setState(410);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(394);
						match(COMMA);
						setState(398);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(395);
							match(NEWLINE);
							}
							}
							setState(400);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(401);
						functionSignatureParam();
						setState(405);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(402);
								match(NEWLINE);
								}
								} 
							}
							setState(407);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
						}
						}
						} 
					}
					setState(412);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				}
				setState(414);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(413);
					match(COMMA);
					}
				}

				}
			}

			setState(421);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(418);
				match(NEWLINE);
				}
				}
				setState(423);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(424);
			match(PAREN_CLOSE);
			setState(428);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(425);
					match(NEWLINE);
					}
					} 
				}
				setState(430);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
			}
			setState(432);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNS) {
				{
				setState(431);
				functionReturnValue();
				}
			}

			setState(437);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(434);
				match(NEWLINE);
				}
				}
				setState(439);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(440);
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
		enterRule(_localctx, 24, RULE_functionReturnValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(442);
			match(RETURNS);
			setState(446);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(443);
				match(NEWLINE);
				}
				}
				setState(448);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(449);
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
		enterRule(_localctx, 26, RULE_namedValueDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			match(LET);
			setState(455);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(452);
					match(NEWLINE);
					}
					} 
				}
				setState(457);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
			}
			setState(459);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VARIABLE) {
				{
				setState(458);
				match(VARIABLE);
				}
			}

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
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(468);
				match(NEWLINE);
				}
				}
				setState(473);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(488);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(474);
				match(COLON);
				setState(478);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(475);
					match(NEWLINE);
					}
					}
					setState(480);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(481);
				typeReference();
				setState(485);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(482);
					match(NEWLINE);
					}
					}
					setState(487);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(490);
			match(EQUALS);
			setState(494);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(491);
				match(NEWLINE);
				}
				}
				setState(496);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(497);
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
	public static class TypeAliasDeclarationContext extends ParserRuleContext {
		public TerminalNode TYPE() { return getToken(CauseParser.TYPE, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CauseParser.IDENTIFIER, 0); }
		public TerminalNode EQUALS() { return getToken(CauseParser.EQUALS, 0); }
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CauseParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CauseParser.NEWLINE, i);
		}
		public TypeAliasDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeAliasDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterTypeAliasDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitTypeAliasDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitTypeAliasDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeAliasDeclarationContext typeAliasDeclaration() throws RecognitionException {
		TypeAliasDeclarationContext _localctx = new TypeAliasDeclarationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_typeAliasDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(499);
			match(TYPE);
			setState(503);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(500);
				match(NEWLINE);
				}
				}
				setState(505);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(506);
			match(IDENTIFIER);
			setState(510);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(507);
				match(NEWLINE);
				}
				}
				setState(512);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(513);
			match(EQUALS);
			setState(517);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(514);
				match(NEWLINE);
				}
				}
				setState(519);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(520);
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
		enterRule(_localctx, 30, RULE_objectDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			match(OBJECT);
			setState(526);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(523);
				match(NEWLINE);
				}
				}
				setState(528);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(529);
			match(IDENTIFIER);
			setState(533);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(530);
					match(NEWLINE);
					}
					} 
				}
				setState(535);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
			}
			setState(537);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PAREN_OPEN) {
				{
				setState(536);
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
		enterRule(_localctx, 32, RULE_signalDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(539);
			match(SIGNAL);
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
			match(IDENTIFIER);
			setState(550);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(547);
					match(NEWLINE);
					}
					} 
				}
				setState(552);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			}
			setState(554);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PAREN_OPEN) {
				{
				setState(553);
				objectFields();
				}
			}

			setState(559);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(556);
					match(NEWLINE);
					}
					} 
				}
				setState(561);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			}
			setState(570);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(562);
				match(COLON);
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
		enterRule(_localctx, 34, RULE_objectFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(572);
			match(PAREN_OPEN);
			setState(576);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(573);
					match(NEWLINE);
					}
					} 
				}
				setState(578);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			}
			setState(608);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(579);
				objectField();
				setState(583);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(580);
						match(NEWLINE);
						}
						} 
					}
					setState(585);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
				}
				setState(602);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(586);
						match(COMMA);
						setState(590);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(587);
							match(NEWLINE);
							}
							}
							setState(592);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(593);
						objectField();
						setState(597);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(594);
								match(NEWLINE);
								}
								} 
							}
							setState(599);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
						}
						}
						} 
					}
					setState(604);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				}
				setState(606);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(605);
					match(COMMA);
					}
				}

				}
			}

			setState(613);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(610);
				match(NEWLINE);
				}
				}
				setState(615);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(616);
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
		enterRule(_localctx, 36, RULE_objectField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(618);
			match(IDENTIFIER);
			setState(622);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(619);
				match(NEWLINE);
				}
				}
				setState(624);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(625);
			match(COLON);
			setState(629);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(626);
				match(NEWLINE);
				}
				}
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(632);
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
		enterRule(_localctx, 38, RULE_body);
		try {
			setState(636);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CURLY_OPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(634);
				block();
				}
				break;
			case THICK_ARROW:
				enterOuterAlt(_localctx, 2);
				{
				setState(635);
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
		enterRule(_localctx, 40, RULE_block);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(638);
			match(CURLY_OPEN);
			setState(642);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(639);
					match(NEWLINE);
					}
					} 
				}
				setState(644);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			}
			setState(657);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 39202497893376L) != 0)) {
				{
				setState(645);
				statement();
				setState(654);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(647); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(646);
							match(NEWLINE);
							}
							}
							setState(649); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==NEWLINE );
						setState(651);
						statement();
						}
						} 
					}
					setState(656);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
				}
				}
			}

			setState(662);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,91,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(659);
					match(NEWLINE);
					}
					} 
				}
				setState(664);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,91,_ctx);
			}
			setState(666);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CARET) {
				{
				setState(665);
				blockResult();
				}
			}

			setState(671);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(668);
				match(NEWLINE);
				}
				}
				setState(673);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(674);
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
		enterRule(_localctx, 42, RULE_blockResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(676);
			match(CARET);
			setState(677);
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
		enterRule(_localctx, 44, RULE_singleExpressionBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(679);
			match(THICK_ARROW);
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
		enterRule(_localctx, 46, RULE_statement);
		try {
			setState(691);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EFFECT:
				enterOuterAlt(_localctx, 1);
				{
				setState(688);
				effectStatement();
				}
				break;
			case FUNCTION:
			case IMPORT:
			case LET:
			case OBJECT:
			case SIGNAL:
			case TYPE:
				enterOuterAlt(_localctx, 2);
				{
				setState(689);
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
			case SET:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 3);
				{
				setState(690);
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
		enterRule(_localctx, 48, RULE_expressionStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(693);
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
		enterRule(_localctx, 50, RULE_declarationStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(695);
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
		enterRule(_localctx, 52, RULE_effectStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(697);
			match(EFFECT);
			setState(701);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(698);
				match(NEWLINE);
				}
				}
				setState(703);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(704);
			match(FOR);
			setState(708);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(705);
				match(NEWLINE);
				}
				}
				setState(710);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(711);
			pattern();
			setState(715);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(712);
				match(NEWLINE);
				}
				}
				setState(717);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(718);
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
		public SetExpressionContext setExpression() {
			return getRuleContext(SetExpressionContext.class,0);
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
			setState(732);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PAREN_OPEN:
				{
				setState(720);
				groupExpression();
				}
				break;
			case CURLY_OPEN:
				{
				setState(721);
				blockExpression();
				}
				break;
			case FN:
				{
				setState(722);
				functionExpression();
				}
				break;
			case BRANCH:
				{
				setState(723);
				branchExpression();
				}
				break;
			case LOOP:
				{
				setState(724);
				loopExpression();
				}
				break;
			case SET:
				{
				setState(725);
				setExpression();
				}
				break;
			case CAUSE:
				{
				setState(726);
				causeExpression();
				}
				break;
			case RETURN:
				{
				setState(727);
				returnExpression();
				}
				break;
			case BREAK:
				{
				setState(728);
				breakExpression();
				}
				break;
			case STRING_LITERAL:
				{
				setState(729);
				stringLiteralExpression();
				}
				break;
			case NUMBER_LITERAL:
				{
				setState(730);
				numberLiteralExpression();
				}
				break;
			case IDENTIFIER:
				{
				setState(731);
				identifierExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(737);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,100,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(734);
					expressionSuffix();
					}
					} 
				}
				setState(739);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,100,_ctx);
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
			setState(740);
			match(PAREN_OPEN);
			setState(744);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(741);
				match(NEWLINE);
				}
				}
				setState(746);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(747);
			expression();
			setState(751);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(748);
				match(NEWLINE);
				}
				}
				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(754);
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
			setState(756);
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
			setState(758);
			match(FN);
			setState(762);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(759);
				match(NEWLINE);
				}
				}
				setState(764);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(765);
			match(PAREN_OPEN);
			setState(769);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(766);
					match(NEWLINE);
					}
					} 
				}
				setState(771);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
			}
			setState(801);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(772);
				functionSignatureParam();
				setState(776);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(773);
						match(NEWLINE);
						}
						} 
					}
					setState(778);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				}
				setState(795);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(779);
						match(COMMA);
						setState(783);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(780);
							match(NEWLINE);
							}
							}
							setState(785);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(786);
						functionSignatureParam();
						setState(790);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(787);
								match(NEWLINE);
								}
								} 
							}
							setState(792);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
						}
						}
						} 
					}
					setState(797);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
				}
				setState(799);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(798);
					match(COMMA);
					}
				}

				}
			}

			setState(806);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(803);
				match(NEWLINE);
				}
				}
				setState(808);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(809);
			match(PAREN_CLOSE);
			setState(813);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(810);
					match(NEWLINE);
					}
					} 
				}
				setState(815);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			}
			setState(817);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNS) {
				{
				setState(816);
				functionReturnValue();
				}
			}

			setState(822);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(819);
				match(NEWLINE);
				}
				}
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(825);
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
			setState(827);
			match(BRANCH);
			setState(831);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,115,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(828);
					match(NEWLINE);
					}
					} 
				}
				setState(833);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,115,_ctx);
			}
			setState(835);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(834);
				branchWith();
				}
			}

			setState(840);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(837);
				match(NEWLINE);
				}
				}
				setState(842);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(843);
			match(CURLY_OPEN);
			setState(847);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,118,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(844);
					match(NEWLINE);
					}
					} 
				}
				setState(849);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,118,_ctx);
			}
			setState(862);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1627389952L) != 0)) {
				{
				setState(850);
				branchOption();
				setState(859);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,120,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(852); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(851);
							match(NEWLINE);
							}
							}
							setState(854); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==NEWLINE );
						setState(856);
						branchOption();
						}
						} 
					}
					setState(861);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,120,_ctx);
				}
				}
			}

			setState(867);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(864);
				match(NEWLINE);
				}
				}
				setState(869);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(870);
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
			setState(872);
			match(WITH);
			setState(876);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(873);
				match(NEWLINE);
				}
				}
				setState(878);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(879);
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
			setState(881);
			match(LOOP);
			setState(885);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(882);
				match(NEWLINE);
				}
				}
				setState(887);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(888);
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
	public static class SetExpressionContext extends ParserRuleContext {
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
		public SetExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).enterSetExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CauseListener ) ((CauseListener)listener).exitSetExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CauseVisitor ) return ((CauseVisitor<? extends T>)visitor).visitSetExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetExpressionContext setExpression() throws RecognitionException {
		SetExpressionContext _localctx = new SetExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_setExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(890);
			match(SET);
			setState(894);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(891);
				match(NEWLINE);
				}
				}
				setState(896);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(897);
			match(IDENTIFIER);
			setState(901);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(898);
				match(NEWLINE);
				}
				}
				setState(903);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(904);
			match(EQUALS);
			setState(908);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(905);
				match(NEWLINE);
				}
				}
				setState(910);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(911);
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
		enterRule(_localctx, 70, RULE_causeExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(913);
			match(CAUSE);
			setState(917);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(914);
				match(NEWLINE);
				}
				}
				setState(919);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(920);
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
		enterRule(_localctx, 72, RULE_returnExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(922);
			match(RETURN);
			setState(924);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
			case 1:
				{
				setState(923);
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
		enterRule(_localctx, 74, RULE_breakExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(926);
			match(BREAK);
			setState(935);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(927);
				match(WITH);
				setState(931);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(928);
					match(NEWLINE);
					}
					}
					setState(933);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(934);
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
		enterRule(_localctx, 76, RULE_stringLiteralExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(937);
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
		enterRule(_localctx, 78, RULE_numberLiteralExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(939);
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
		enterRule(_localctx, 80, RULE_identifierExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(941);
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
		enterRule(_localctx, 82, RULE_expressionSuffix);
		try {
			setState(946);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(943);
				callExpressionSuffix();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(944);
				memberExpressionSuffix();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(945);
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
		enterRule(_localctx, 84, RULE_callExpressionSuffix);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(948);
			match(PAREN_OPEN);
			setState(952);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(949);
					match(NEWLINE);
					}
					} 
				}
				setState(954);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
			}
			setState(984);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35880198083584L) != 0)) {
				{
				setState(955);
				callParam();
				setState(959);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(956);
						match(NEWLINE);
						}
						} 
					}
					setState(961);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
				}
				setState(978);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,137,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(962);
						match(COMMA);
						setState(966);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(963);
							match(NEWLINE);
							}
							}
							setState(968);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(969);
						callParam();
						setState(973);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(970);
								match(NEWLINE);
								}
								} 
							}
							setState(975);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
						}
						}
						} 
					}
					setState(980);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,137,_ctx);
				}
				setState(982);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(981);
					match(COMMA);
					}
				}

				}
			}

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
		enterRule(_localctx, 86, RULE_callParam);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(994);
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
		enterRule(_localctx, 88, RULE_callPositionalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(996);
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
		enterRule(_localctx, 90, RULE_memberExpressionSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1001);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(998);
				match(NEWLINE);
				}
				}
				setState(1003);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1004);
			match(DOT);
			setState(1008);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1005);
				match(NEWLINE);
				}
				}
				setState(1010);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1011);
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
		enterRule(_localctx, 92, RULE_pipeCallExpressionSuffix);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1016);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1013);
				match(NEWLINE);
				}
				}
				setState(1018);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1019);
			match(PIPELINE);
			setState(1023);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1020);
				match(NEWLINE);
				}
				}
				setState(1025);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1026);
			expression();
			setState(1030);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1027);
				match(NEWLINE);
				}
				}
				setState(1032);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1033);
			match(PAREN_OPEN);
			setState(1037);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1034);
					match(NEWLINE);
					}
					} 
				}
				setState(1039);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
			}
			setState(1069);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35880198083584L) != 0)) {
				{
				setState(1040);
				callParam();
				setState(1044);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1041);
						match(NEWLINE);
						}
						} 
					}
					setState(1046);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
				}
				setState(1063);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,150,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1047);
						match(COMMA);
						setState(1051);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(1048);
							match(NEWLINE);
							}
							}
							setState(1053);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(1054);
						callParam();
						setState(1058);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,149,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(1055);
								match(NEWLINE);
								}
								} 
							}
							setState(1060);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,149,_ctx);
						}
						}
						} 
					}
					setState(1065);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,150,_ctx);
				}
				setState(1067);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1066);
					match(COMMA);
					}
				}

				}
			}

			setState(1074);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1071);
				match(NEWLINE);
				}
				}
				setState(1076);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1077);
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
		enterRule(_localctx, 94, RULE_branchOption);
		try {
			setState(1082);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1079);
				ifBranchOption();
				}
				break;
			case IS:
				enterOuterAlt(_localctx, 2);
				{
				setState(1080);
				isBranchOption();
				}
				break;
			case ELSE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1081);
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
		enterRule(_localctx, 96, RULE_ifBranchOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1084);
			match(IF);
			setState(1085);
			expression();
			setState(1086);
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
		enterRule(_localctx, 98, RULE_isBranchOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1088);
			match(IS);
			setState(1089);
			pattern();
			setState(1090);
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
		enterRule(_localctx, 100, RULE_elseBranchOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1092);
			match(ELSE);
			setState(1093);
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
		enterRule(_localctx, 102, RULE_pattern);
		try {
			setState(1097);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1095);
				captureValuePattern();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1096);
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
		enterRule(_localctx, 104, RULE_captureValuePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1099);
			typeReference();
			setState(1103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1100);
				match(NEWLINE);
				}
				}
				setState(1105);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1106);
			match(AS);
			setState(1110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(1107);
				match(NEWLINE);
				}
				}
				setState(1112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1113);
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
		enterRule(_localctx, 106, RULE_typeReferencePattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1115);
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
		"\u0004\u0001-\u045e\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u0001\u0000\u0005\u0000"+
		"n\b\u0000\n\u0000\f\u0000q\t\u0000\u0001\u0000\u0001\u0000\u0004\u0000"+
		"u\b\u0000\u000b\u0000\f\u0000v\u0001\u0000\u0005\u0000z\b\u0000\n\u0000"+
		"\f\u0000}\t\u0000\u0003\u0000\u007f\b\u0000\u0001\u0000\u0005\u0000\u0082"+
		"\b\u0000\n\u0000\f\u0000\u0085\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001\u008c\b\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0005\u0003\u0092\b\u0003\n\u0003\f\u0003\u0095"+
		"\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u0099\b\u0003\n\u0003\f\u0003"+
		"\u009c\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00a0\b\u0003\n\u0003"+
		"\f\u0003\u00a3\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00a7\b\u0003"+
		"\n\u0003\f\u0003\u00aa\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00ae"+
		"\b\u0003\n\u0003\f\u0003\u00b1\t\u0003\u0005\u0003\u00b3\b\u0003\n\u0003"+
		"\f\u0003\u00b6\t\u0003\u0001\u0003\u0003\u0003\u00b9\b\u0003\u0003\u0003"+
		"\u00bb\b\u0003\u0001\u0003\u0005\u0003\u00be\b\u0003\n\u0003\f\u0003\u00c1"+
		"\t\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00c5\b\u0003\n\u0003\f\u0003"+
		"\u00c8\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0005\u0004"+
		"\u00ce\b\u0004\n\u0004\f\u0004\u00d1\t\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0005\u0005\u00d7\b\u0005\n\u0005\f\u0005\u00da\t\u0005"+
		"\u0001\u0005\u0001\u0005\u0005\u0005\u00de\b\u0005\n\u0005\f\u0005\u00e1"+
		"\t\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u00e5\b\u0005\n\u0005\f\u0005"+
		"\u00e8\t\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u00ec\b\u0005\n\u0005"+
		"\f\u0005\u00ef\t\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u00f3\b\u0005"+
		"\n\u0005\f\u0005\u00f6\t\u0005\u0005\u0005\u00f8\b\u0005\n\u0005\f\u0005"+
		"\u00fb\t\u0005\u0001\u0005\u0003\u0005\u00fe\b\u0005\u0003\u0005\u0100"+
		"\b\u0005\u0001\u0005\u0005\u0005\u0103\b\u0005\n\u0005\f\u0005\u0106\t"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0005\u0006\u010c"+
		"\b\u0006\n\u0006\f\u0006\u010f\t\u0006\u0001\u0006\u0001\u0006\u0005\u0006"+
		"\u0113\b\u0006\n\u0006\f\u0006\u0116\t\u0006\u0001\u0006\u0003\u0006\u0119"+
		"\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u0121\b\u0007\u0001\b\u0001\b\u0005\b\u0125\b\b\n\b"+
		"\f\b\u0128\t\b\u0001\b\u0001\b\u0005\b\u012c\b\b\n\b\f\b\u012f\t\b\u0001"+
		"\b\u0001\b\u0005\b\u0133\b\b\n\b\f\b\u0136\t\b\u0001\b\u0001\b\u0005\b"+
		"\u013a\b\b\n\b\f\b\u013d\t\b\u0001\b\u0001\b\u0001\t\u0001\t\u0005\t\u0143"+
		"\b\t\n\t\f\t\u0146\t\t\u0001\t\u0001\t\u0005\t\u014a\b\t\n\t\f\t\u014d"+
		"\t\t\u0001\t\u0001\t\u0005\t\u0151\b\t\n\t\f\t\u0154\t\t\u0005\t\u0156"+
		"\b\t\n\t\f\t\u0159\t\t\u0001\t\u0003\t\u015c\b\t\u0001\n\u0001\n\u0005"+
		"\n\u0160\b\n\n\n\f\n\u0163\t\n\u0001\n\u0001\n\u0005\n\u0167\b\n\n\n\f"+
		"\n\u016a\t\n\u0001\n\u0003\n\u016d\b\n\u0001\u000b\u0001\u000b\u0005\u000b"+
		"\u0171\b\u000b\n\u000b\f\u000b\u0174\t\u000b\u0001\u000b\u0001\u000b\u0005"+
		"\u000b\u0178\b\u000b\n\u000b\f\u000b\u017b\t\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u017f\b\u000b\n\u000b\f\u000b\u0182\t\u000b\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u0186\b\u000b\n\u000b\f\u000b\u0189\t\u000b\u0001\u000b"+
		"\u0001\u000b\u0005\u000b\u018d\b\u000b\n\u000b\f\u000b\u0190\t\u000b\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u0194\b\u000b\n\u000b\f\u000b\u0197\t\u000b"+
		"\u0005\u000b\u0199\b\u000b\n\u000b\f\u000b\u019c\t\u000b\u0001\u000b\u0003"+
		"\u000b\u019f\b\u000b\u0003\u000b\u01a1\b\u000b\u0001\u000b\u0005\u000b"+
		"\u01a4\b\u000b\n\u000b\f\u000b\u01a7\t\u000b\u0001\u000b\u0001\u000b\u0005"+
		"\u000b\u01ab\b\u000b\n\u000b\f\u000b\u01ae\t\u000b\u0001\u000b\u0003\u000b"+
		"\u01b1\b\u000b\u0001\u000b\u0005\u000b\u01b4\b\u000b\n\u000b\f\u000b\u01b7"+
		"\t\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0005\f\u01bd\b\f\n\f"+
		"\f\f\u01c0\t\f\u0001\f\u0001\f\u0001\r\u0001\r\u0005\r\u01c6\b\r\n\r\f"+
		"\r\u01c9\t\r\u0001\r\u0003\r\u01cc\b\r\u0001\r\u0005\r\u01cf\b\r\n\r\f"+
		"\r\u01d2\t\r\u0001\r\u0001\r\u0005\r\u01d6\b\r\n\r\f\r\u01d9\t\r\u0001"+
		"\r\u0001\r\u0005\r\u01dd\b\r\n\r\f\r\u01e0\t\r\u0001\r\u0001\r\u0005\r"+
		"\u01e4\b\r\n\r\f\r\u01e7\t\r\u0003\r\u01e9\b\r\u0001\r\u0001\r\u0005\r"+
		"\u01ed\b\r\n\r\f\r\u01f0\t\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0005"+
		"\u000e\u01f6\b\u000e\n\u000e\f\u000e\u01f9\t\u000e\u0001\u000e\u0001\u000e"+
		"\u0005\u000e\u01fd\b\u000e\n\u000e\f\u000e\u0200\t\u000e\u0001\u000e\u0001"+
		"\u000e\u0005\u000e\u0204\b\u000e\n\u000e\f\u000e\u0207\t\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0001\u000f\u0005\u000f\u020d\b\u000f\n\u000f"+
		"\f\u000f\u0210\t\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u0214\b\u000f"+
		"\n\u000f\f\u000f\u0217\t\u000f\u0001\u000f\u0003\u000f\u021a\b\u000f\u0001"+
		"\u0010\u0001\u0010\u0005\u0010\u021e\b\u0010\n\u0010\f\u0010\u0221\t\u0010"+
		"\u0001\u0010\u0001\u0010\u0005\u0010\u0225\b\u0010\n\u0010\f\u0010\u0228"+
		"\t\u0010\u0001\u0010\u0003\u0010\u022b\b\u0010\u0001\u0010\u0005\u0010"+
		"\u022e\b\u0010\n\u0010\f\u0010\u0231\t\u0010\u0001\u0010\u0001\u0010\u0005"+
		"\u0010\u0235\b\u0010\n\u0010\f\u0010\u0238\t\u0010\u0001\u0010\u0003\u0010"+
		"\u023b\b\u0010\u0001\u0011\u0001\u0011\u0005\u0011\u023f\b\u0011\n\u0011"+
		"\f\u0011\u0242\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u0246\b\u0011"+
		"\n\u0011\f\u0011\u0249\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u024d"+
		"\b\u0011\n\u0011\f\u0011\u0250\t\u0011\u0001\u0011\u0001\u0011\u0005\u0011"+
		"\u0254\b\u0011\n\u0011\f\u0011\u0257\t\u0011\u0005\u0011\u0259\b\u0011"+
		"\n\u0011\f\u0011\u025c\t\u0011\u0001\u0011\u0003\u0011\u025f\b\u0011\u0003"+
		"\u0011\u0261\b\u0011\u0001\u0011\u0005\u0011\u0264\b\u0011\n\u0011\f\u0011"+
		"\u0267\t\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0005\u0012"+
		"\u026d\b\u0012\n\u0012\f\u0012\u0270\t\u0012\u0001\u0012\u0001\u0012\u0005"+
		"\u0012\u0274\b\u0012\n\u0012\f\u0012\u0277\t\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u027d\b\u0013\u0001\u0014\u0001\u0014"+
		"\u0005\u0014\u0281\b\u0014\n\u0014\f\u0014\u0284\t\u0014\u0001\u0014\u0001"+
		"\u0014\u0004\u0014\u0288\b\u0014\u000b\u0014\f\u0014\u0289\u0001\u0014"+
		"\u0005\u0014\u028d\b\u0014\n\u0014\f\u0014\u0290\t\u0014\u0003\u0014\u0292"+
		"\b\u0014\u0001\u0014\u0005\u0014\u0295\b\u0014\n\u0014\f\u0014\u0298\t"+
		"\u0014\u0001\u0014\u0003\u0014\u029b\b\u0014\u0001\u0014\u0005\u0014\u029e"+
		"\b\u0014\n\u0014\f\u0014\u02a1\t\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0005\u0016\u02aa\b\u0016"+
		"\n\u0016\f\u0016\u02ad\t\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0003\u0017\u02b4\b\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0005\u001a\u02bc\b\u001a\n"+
		"\u001a\f\u001a\u02bf\t\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u02c3"+
		"\b\u001a\n\u001a\f\u001a\u02c6\t\u001a\u0001\u001a\u0001\u001a\u0005\u001a"+
		"\u02ca\b\u001a\n\u001a\f\u001a\u02cd\t\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u02dd\b\u001b\u0001\u001b\u0005\u001b\u02e0\b\u001b\n\u001b\f\u001b"+
		"\u02e3\t\u001b\u0001\u001c\u0001\u001c\u0005\u001c\u02e7\b\u001c\n\u001c"+
		"\f\u001c\u02ea\t\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u02ee\b\u001c"+
		"\n\u001c\f\u001c\u02f1\t\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001"+
		"\u001d\u0001\u001e\u0001\u001e\u0005\u001e\u02f9\b\u001e\n\u001e\f\u001e"+
		"\u02fc\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0300\b\u001e\n\u001e"+
		"\f\u001e\u0303\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0307\b\u001e"+
		"\n\u001e\f\u001e\u030a\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u030e"+
		"\b\u001e\n\u001e\f\u001e\u0311\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e"+
		"\u0315\b\u001e\n\u001e\f\u001e\u0318\t\u001e\u0005\u001e\u031a\b\u001e"+
		"\n\u001e\f\u001e\u031d\t\u001e\u0001\u001e\u0003\u001e\u0320\b\u001e\u0003"+
		"\u001e\u0322\b\u001e\u0001\u001e\u0005\u001e\u0325\b\u001e\n\u001e\f\u001e"+
		"\u0328\t\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u032c\b\u001e\n\u001e"+
		"\f\u001e\u032f\t\u001e\u0001\u001e\u0003\u001e\u0332\b\u001e\u0001\u001e"+
		"\u0005\u001e\u0335\b\u001e\n\u001e\f\u001e\u0338\t\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001f\u0001\u001f\u0005\u001f\u033e\b\u001f\n\u001f\f\u001f"+
		"\u0341\t\u001f\u0001\u001f\u0003\u001f\u0344\b\u001f\u0001\u001f\u0005"+
		"\u001f\u0347\b\u001f\n\u001f\f\u001f\u034a\t\u001f\u0001\u001f\u0001\u001f"+
		"\u0005\u001f\u034e\b\u001f\n\u001f\f\u001f\u0351\t\u001f\u0001\u001f\u0001"+
		"\u001f\u0004\u001f\u0355\b\u001f\u000b\u001f\f\u001f\u0356\u0001\u001f"+
		"\u0005\u001f\u035a\b\u001f\n\u001f\f\u001f\u035d\t\u001f\u0003\u001f\u035f"+
		"\b\u001f\u0001\u001f\u0005\u001f\u0362\b\u001f\n\u001f\f\u001f\u0365\t"+
		"\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0005 \u036b\b \n \f \u036e"+
		"\t \u0001 \u0001 \u0001!\u0001!\u0005!\u0374\b!\n!\f!\u0377\t!\u0001!"+
		"\u0001!\u0001\"\u0001\"\u0005\"\u037d\b\"\n\"\f\"\u0380\t\"\u0001\"\u0001"+
		"\"\u0005\"\u0384\b\"\n\"\f\"\u0387\t\"\u0001\"\u0001\"\u0005\"\u038b\b"+
		"\"\n\"\f\"\u038e\t\"\u0001\"\u0001\"\u0001#\u0001#\u0005#\u0394\b#\n#"+
		"\f#\u0397\t#\u0001#\u0001#\u0001$\u0001$\u0003$\u039d\b$\u0001%\u0001"+
		"%\u0001%\u0005%\u03a2\b%\n%\f%\u03a5\t%\u0001%\u0003%\u03a8\b%\u0001&"+
		"\u0001&\u0001\'\u0001\'\u0001(\u0001(\u0001)\u0001)\u0001)\u0003)\u03b3"+
		"\b)\u0001*\u0001*\u0005*\u03b7\b*\n*\f*\u03ba\t*\u0001*\u0001*\u0005*"+
		"\u03be\b*\n*\f*\u03c1\t*\u0001*\u0001*\u0005*\u03c5\b*\n*\f*\u03c8\t*"+
		"\u0001*\u0001*\u0005*\u03cc\b*\n*\f*\u03cf\t*\u0005*\u03d1\b*\n*\f*\u03d4"+
		"\t*\u0001*\u0003*\u03d7\b*\u0003*\u03d9\b*\u0001*\u0005*\u03dc\b*\n*\f"+
		"*\u03df\t*\u0001*\u0001*\u0001+\u0001+\u0001,\u0001,\u0001-\u0005-\u03e8"+
		"\b-\n-\f-\u03eb\t-\u0001-\u0001-\u0005-\u03ef\b-\n-\f-\u03f2\t-\u0001"+
		"-\u0001-\u0001.\u0005.\u03f7\b.\n.\f.\u03fa\t.\u0001.\u0001.\u0005.\u03fe"+
		"\b.\n.\f.\u0401\t.\u0001.\u0001.\u0005.\u0405\b.\n.\f.\u0408\t.\u0001"+
		".\u0001.\u0005.\u040c\b.\n.\f.\u040f\t.\u0001.\u0001.\u0005.\u0413\b."+
		"\n.\f.\u0416\t.\u0001.\u0001.\u0005.\u041a\b.\n.\f.\u041d\t.\u0001.\u0001"+
		".\u0005.\u0421\b.\n.\f.\u0424\t.\u0005.\u0426\b.\n.\f.\u0429\t.\u0001"+
		".\u0003.\u042c\b.\u0003.\u042e\b.\u0001.\u0005.\u0431\b.\n.\f.\u0434\t"+
		".\u0001.\u0001.\u0001/\u0001/\u0001/\u0003/\u043b\b/\u00010\u00010\u0001"+
		"0\u00010\u00011\u00011\u00011\u00011\u00012\u00012\u00012\u00013\u0001"+
		"3\u00033\u044a\b3\u00014\u00014\u00054\u044e\b4\n4\f4\u0451\t4\u00014"+
		"\u00014\u00054\u0455\b4\n4\f4\u0458\t4\u00014\u00014\u00015\u00015\u0001"+
		"5\u0000\u00006\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhj\u0000"+
		"\u0000\u04d7\u0000o\u0001\u0000\u0000\u0000\u0002\u008b\u0001\u0000\u0000"+
		"\u0000\u0004\u008d\u0001\u0000\u0000\u0000\u0006\u008f\u0001\u0000\u0000"+
		"\u0000\b\u00cb\u0001\u0000\u0000\u0000\n\u00d4\u0001\u0000\u0000\u0000"+
		"\f\u0109\u0001\u0000\u0000\u0000\u000e\u0120\u0001\u0000\u0000\u0000\u0010"+
		"\u0122\u0001\u0000\u0000\u0000\u0012\u0140\u0001\u0000\u0000\u0000\u0014"+
		"\u015d\u0001\u0000\u0000\u0000\u0016\u016e\u0001\u0000\u0000\u0000\u0018"+
		"\u01ba\u0001\u0000\u0000\u0000\u001a\u01c3\u0001\u0000\u0000\u0000\u001c"+
		"\u01f3\u0001\u0000\u0000\u0000\u001e\u020a\u0001\u0000\u0000\u0000 \u021b"+
		"\u0001\u0000\u0000\u0000\"\u023c\u0001\u0000\u0000\u0000$\u026a\u0001"+
		"\u0000\u0000\u0000&\u027c\u0001\u0000\u0000\u0000(\u027e\u0001\u0000\u0000"+
		"\u0000*\u02a4\u0001\u0000\u0000\u0000,\u02a7\u0001\u0000\u0000\u0000."+
		"\u02b3\u0001\u0000\u0000\u00000\u02b5\u0001\u0000\u0000\u00002\u02b7\u0001"+
		"\u0000\u0000\u00004\u02b9\u0001\u0000\u0000\u00006\u02dc\u0001\u0000\u0000"+
		"\u00008\u02e4\u0001\u0000\u0000\u0000:\u02f4\u0001\u0000\u0000\u0000<"+
		"\u02f6\u0001\u0000\u0000\u0000>\u033b\u0001\u0000\u0000\u0000@\u0368\u0001"+
		"\u0000\u0000\u0000B\u0371\u0001\u0000\u0000\u0000D\u037a\u0001\u0000\u0000"+
		"\u0000F\u0391\u0001\u0000\u0000\u0000H\u039a\u0001\u0000\u0000\u0000J"+
		"\u039e\u0001\u0000\u0000\u0000L\u03a9\u0001\u0000\u0000\u0000N\u03ab\u0001"+
		"\u0000\u0000\u0000P\u03ad\u0001\u0000\u0000\u0000R\u03b2\u0001\u0000\u0000"+
		"\u0000T\u03b4\u0001\u0000\u0000\u0000V\u03e2\u0001\u0000\u0000\u0000X"+
		"\u03e4\u0001\u0000\u0000\u0000Z\u03e9\u0001\u0000\u0000\u0000\\\u03f8"+
		"\u0001\u0000\u0000\u0000^\u043a\u0001\u0000\u0000\u0000`\u043c\u0001\u0000"+
		"\u0000\u0000b\u0440\u0001\u0000\u0000\u0000d\u0444\u0001\u0000\u0000\u0000"+
		"f\u0449\u0001\u0000\u0000\u0000h\u044b\u0001\u0000\u0000\u0000j\u045b"+
		"\u0001\u0000\u0000\u0000ln\u0005\u0004\u0000\u0000ml\u0001\u0000\u0000"+
		"\u0000nq\u0001\u0000\u0000\u0000om\u0001\u0000\u0000\u0000op\u0001\u0000"+
		"\u0000\u0000p~\u0001\u0000\u0000\u0000qo\u0001\u0000\u0000\u0000r{\u0003"+
		"\u000e\u0007\u0000su\u0005\u0004\u0000\u0000ts\u0001\u0000\u0000\u0000"+
		"uv\u0001\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000"+
		"\u0000wx\u0001\u0000\u0000\u0000xz\u0003\u000e\u0007\u0000yt\u0001\u0000"+
		"\u0000\u0000z}\u0001\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000{|\u0001"+
		"\u0000\u0000\u0000|\u007f\u0001\u0000\u0000\u0000}{\u0001\u0000\u0000"+
		"\u0000~r\u0001\u0000\u0000\u0000~\u007f\u0001\u0000\u0000\u0000\u007f"+
		"\u0083\u0001\u0000\u0000\u0000\u0080\u0082\u0005\u0004\u0000\u0000\u0081"+
		"\u0080\u0001\u0000\u0000\u0000\u0082\u0085\u0001\u0000\u0000\u0000\u0083"+
		"\u0081\u0001\u0000\u0000\u0000\u0083\u0084\u0001\u0000\u0000\u0000\u0084"+
		"\u0086\u0001\u0000\u0000\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0086"+
		"\u0087\u0005\u0000\u0000\u0001\u0087\u0001\u0001\u0000\u0000\u0000\u0088"+
		"\u008c\u0003\u0006\u0003\u0000\u0089\u008c\u0003\u0004\u0002\u0000\u008a"+
		"\u008c\u0003\n\u0005\u0000\u008b\u0088\u0001\u0000\u0000\u0000\u008b\u0089"+
		"\u0001\u0000\u0000\u0000\u008b\u008a\u0001\u0000\u0000\u0000\u008c\u0003"+
		"\u0001\u0000\u0000\u0000\u008d\u008e\u0005-\u0000\u0000\u008e\u0005\u0001"+
		"\u0000\u0000\u0000\u008f\u0093\u0005\u001c\u0000\u0000\u0090\u0092\u0005"+
		"\u0004\u0000\u0000\u0091\u0090\u0001\u0000\u0000\u0000\u0092\u0095\u0001"+
		"\u0000\u0000\u0000\u0093\u0091\u0001\u0000\u0000\u0000\u0093\u0094\u0001"+
		"\u0000\u0000\u0000\u0094\u0096\u0001\u0000\u0000\u0000\u0095\u0093\u0001"+
		"\u0000\u0000\u0000\u0096\u009a\u0005\n\u0000\u0000\u0097\u0099\u0005\u0004"+
		"\u0000\u0000\u0098\u0097\u0001\u0000\u0000\u0000\u0099\u009c\u0001\u0000"+
		"\u0000\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009a\u009b\u0001\u0000"+
		"\u0000\u0000\u009b\u00ba\u0001\u0000\u0000\u0000\u009c\u009a\u0001\u0000"+
		"\u0000\u0000\u009d\u00a1\u0003\f\u0006\u0000\u009e\u00a0\u0005\u0004\u0000"+
		"\u0000\u009f\u009e\u0001\u0000\u0000\u0000\u00a0\u00a3\u0001\u0000\u0000"+
		"\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a1\u00a2\u0001\u0000\u0000"+
		"\u0000\u00a2\u00b4\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000"+
		"\u0000\u00a4\u00a8\u0005\u0005\u0000\u0000\u00a5\u00a7\u0005\u0004\u0000"+
		"\u0000\u00a6\u00a5\u0001\u0000\u0000\u0000\u00a7\u00aa\u0001\u0000\u0000"+
		"\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000"+
		"\u0000\u00a9\u00ab\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000\u0000"+
		"\u0000\u00ab\u00af\u0003\f\u0006\u0000\u00ac\u00ae\u0005\u0004\u0000\u0000"+
		"\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ae\u00b1\u0001\u0000\u0000\u0000"+
		"\u00af\u00ad\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000"+
		"\u00b0\u00b3\u0001\u0000\u0000\u0000\u00b1\u00af\u0001\u0000\u0000\u0000"+
		"\u00b2\u00a4\u0001\u0000\u0000\u0000\u00b3\u00b6\u0001\u0000\u0000\u0000"+
		"\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000"+
		"\u00b5\u00b8\u0001\u0000\u0000\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000"+
		"\u00b7\u00b9\u0005\u0005\u0000\u0000\u00b8\u00b7\u0001\u0000\u0000\u0000"+
		"\u00b8\u00b9\u0001\u0000\u0000\u0000\u00b9\u00bb\u0001\u0000\u0000\u0000"+
		"\u00ba\u009d\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000"+
		"\u00bb\u00bf\u0001\u0000\u0000\u0000\u00bc\u00be\u0005\u0004\u0000\u0000"+
		"\u00bd\u00bc\u0001\u0000\u0000\u0000\u00be\u00c1\u0001\u0000\u0000\u0000"+
		"\u00bf\u00bd\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000"+
		"\u00c0\u00c2\u0001\u0000\u0000\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000"+
		"\u00c2\u00c6\u0005\u000b\u0000\u0000\u00c3\u00c5\u0005\u0004\u0000\u0000"+
		"\u00c4\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c8\u0001\u0000\u0000\u0000"+
		"\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000\u0000"+
		"\u00c7\u00c9\u0001\u0000\u0000\u0000\u00c8\u00c6\u0001\u0000\u0000\u0000"+
		"\u00c9\u00ca\u0003\b\u0004\u0000\u00ca\u0007\u0001\u0000\u0000\u0000\u00cb"+
		"\u00cf\u0005&\u0000\u0000\u00cc\u00ce\u0005\u0004\u0000\u0000\u00cd\u00cc"+
		"\u0001\u0000\u0000\u0000\u00ce\u00d1\u0001\u0000\u0000\u0000\u00cf\u00cd"+
		"\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0\u00d2"+
		"\u0001\u0000\u0000\u0000\u00d1\u00cf\u0001\u0000\u0000\u0000\u00d2\u00d3"+
		"\u0003\u0002\u0001\u0000\u00d3\t\u0001\u0000\u0000\u0000\u00d4\u00d8\u0005"+
		"$\u0000\u0000\u00d5\u00d7\u0005\u0004\u0000\u0000\u00d6\u00d5\u0001\u0000"+
		"\u0000\u0000\u00d7\u00da\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000"+
		"\u0000\u0000\u00d8\u00d9\u0001\u0000\u0000\u0000\u00d9\u00db\u0001\u0000"+
		"\u0000\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00db\u00df\u0005\n\u0000"+
		"\u0000\u00dc\u00de\u0005\u0004\u0000\u0000\u00dd\u00dc\u0001\u0000\u0000"+
		"\u0000\u00de\u00e1\u0001\u0000\u0000\u0000\u00df\u00dd\u0001\u0000\u0000"+
		"\u0000\u00df\u00e0\u0001\u0000\u0000\u0000\u00e0\u00ff\u0001\u0000\u0000"+
		"\u0000\u00e1\u00df\u0001\u0000\u0000\u0000\u00e2\u00e6\u0003\u0002\u0001"+
		"\u0000\u00e3\u00e5\u0005\u0004\u0000\u0000\u00e4\u00e3\u0001\u0000\u0000"+
		"\u0000\u00e5\u00e8\u0001\u0000\u0000\u0000\u00e6\u00e4\u0001\u0000\u0000"+
		"\u0000\u00e6\u00e7\u0001\u0000\u0000\u0000\u00e7\u00f9\u0001\u0000\u0000"+
		"\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e9\u00ed\u0005\u0005\u0000"+
		"\u0000\u00ea\u00ec\u0005\u0004\u0000\u0000\u00eb\u00ea\u0001\u0000\u0000"+
		"\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001\u0000\u0000"+
		"\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00f0\u0001\u0000\u0000"+
		"\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f4\u0003\u0002\u0001"+
		"\u0000\u00f1\u00f3\u0005\u0004\u0000\u0000\u00f2\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f3\u00f6\u0001\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000\u0000"+
		"\u0000\u00f4\u00f5\u0001\u0000\u0000\u0000\u00f5\u00f8\u0001\u0000\u0000"+
		"\u0000\u00f6\u00f4\u0001\u0000\u0000\u0000\u00f7\u00e9\u0001\u0000\u0000"+
		"\u0000\u00f8\u00fb\u0001\u0000\u0000\u0000\u00f9\u00f7\u0001\u0000\u0000"+
		"\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa\u00fd\u0001\u0000\u0000"+
		"\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fc\u00fe\u0005\u0005\u0000"+
		"\u0000\u00fd\u00fc\u0001\u0000\u0000\u0000\u00fd\u00fe\u0001\u0000\u0000"+
		"\u0000\u00fe\u0100\u0001\u0000\u0000\u0000\u00ff\u00e2\u0001\u0000\u0000"+
		"\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100\u0104\u0001\u0000\u0000"+
		"\u0000\u0101\u0103\u0005\u0004\u0000\u0000\u0102\u0101\u0001\u0000\u0000"+
		"\u0000\u0103\u0106\u0001\u0000\u0000\u0000\u0104\u0102\u0001\u0000\u0000"+
		"\u0000\u0104\u0105\u0001\u0000\u0000\u0000\u0105\u0107\u0001\u0000\u0000"+
		"\u0000\u0106\u0104\u0001\u0000\u0000\u0000\u0107\u0108\u0005\u000b\u0000"+
		"\u0000\u0108\u000b\u0001\u0000\u0000\u0000\u0109\u010d\u0005-\u0000\u0000"+
		"\u010a\u010c\u0005\u0004\u0000\u0000\u010b\u010a\u0001\u0000\u0000\u0000"+
		"\u010c\u010f\u0001\u0000\u0000\u0000\u010d\u010b\u0001\u0000\u0000\u0000"+
		"\u010d\u010e\u0001\u0000\u0000\u0000\u010e\u0118\u0001\u0000\u0000\u0000"+
		"\u010f\u010d\u0001\u0000\u0000\u0000\u0110\u0114\u0005\u0006\u0000\u0000"+
		"\u0111\u0113\u0005\u0004\u0000\u0000\u0112\u0111\u0001\u0000\u0000\u0000"+
		"\u0113\u0116\u0001\u0000\u0000\u0000\u0114\u0112\u0001\u0000\u0000\u0000"+
		"\u0114\u0115\u0001\u0000\u0000\u0000\u0115\u0117\u0001\u0000\u0000\u0000"+
		"\u0116\u0114\u0001\u0000\u0000\u0000\u0117\u0119\u0003\u0002\u0001\u0000"+
		"\u0118\u0110\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000\u0000\u0000"+
		"\u0119\r\u0001\u0000\u0000\u0000\u011a\u0121\u0003\u0010\b\u0000\u011b"+
		"\u0121\u0003\u0016\u000b\u0000\u011c\u0121\u0003\u001a\r\u0000\u011d\u0121"+
		"\u0003\u001c\u000e\u0000\u011e\u0121\u0003\u001e\u000f\u0000\u011f\u0121"+
		"\u0003 \u0010\u0000\u0120\u011a\u0001\u0000\u0000\u0000\u0120\u011b\u0001"+
		"\u0000\u0000\u0000\u0120\u011c\u0001\u0000\u0000\u0000\u0120\u011d\u0001"+
		"\u0000\u0000\u0000\u0120\u011e\u0001\u0000\u0000\u0000\u0120\u011f\u0001"+
		"\u0000\u0000\u0000\u0121\u000f\u0001\u0000\u0000\u0000\u0122\u0126\u0005"+
		"\u001f\u0000\u0000\u0123\u0125\u0005\u0004\u0000\u0000\u0124\u0123\u0001"+
		"\u0000\u0000\u0000\u0125\u0128\u0001\u0000\u0000\u0000\u0126\u0124\u0001"+
		"\u0000\u0000\u0000\u0126\u0127\u0001\u0000\u0000\u0000\u0127\u0129\u0001"+
		"\u0000\u0000\u0000\u0128\u0126\u0001\u0000\u0000\u0000\u0129\u012d\u0005"+
		",\u0000\u0000\u012a\u012c\u0005\u0004\u0000\u0000\u012b\u012a\u0001\u0000"+
		"\u0000\u0000\u012c\u012f\u0001\u0000\u0000\u0000\u012d\u012b\u0001\u0000"+
		"\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e\u0130\u0001\u0000"+
		"\u0000\u0000\u012f\u012d\u0001\u0000\u0000\u0000\u0130\u0134\u0005\n\u0000"+
		"\u0000\u0131\u0133\u0005\u0004\u0000\u0000\u0132\u0131\u0001\u0000\u0000"+
		"\u0000\u0133\u0136\u0001\u0000\u0000\u0000\u0134\u0132\u0001\u0000\u0000"+
		"\u0000\u0134\u0135\u0001\u0000\u0000\u0000\u0135\u0137\u0001\u0000\u0000"+
		"\u0000\u0136\u0134\u0001\u0000\u0000\u0000\u0137\u013b\u0003\u0012\t\u0000"+
		"\u0138\u013a\u0005\u0004\u0000\u0000\u0139\u0138\u0001\u0000\u0000\u0000"+
		"\u013a\u013d\u0001\u0000\u0000\u0000\u013b\u0139\u0001\u0000\u0000\u0000"+
		"\u013b\u013c\u0001\u0000\u0000\u0000\u013c\u013e\u0001\u0000\u0000\u0000"+
		"\u013d\u013b\u0001\u0000\u0000\u0000\u013e\u013f\u0005\u000b\u0000\u0000"+
		"\u013f\u0011\u0001\u0000\u0000\u0000\u0140\u0144\u0003\u0014\n\u0000\u0141"+
		"\u0143\u0005\u0004\u0000\u0000\u0142\u0141\u0001\u0000\u0000\u0000\u0143"+
		"\u0146\u0001\u0000\u0000\u0000\u0144\u0142\u0001\u0000\u0000\u0000\u0144"+
		"\u0145\u0001\u0000\u0000\u0000\u0145\u0157\u0001\u0000\u0000\u0000\u0146"+
		"\u0144\u0001\u0000\u0000\u0000\u0147\u014b\u0005\u0005\u0000\u0000\u0148"+
		"\u014a\u0005\u0004\u0000\u0000\u0149\u0148\u0001\u0000\u0000\u0000\u014a"+
		"\u014d\u0001\u0000\u0000\u0000\u014b\u0149\u0001\u0000\u0000\u0000\u014b"+
		"\u014c\u0001\u0000\u0000\u0000\u014c\u014e\u0001\u0000\u0000\u0000\u014d"+
		"\u014b\u0001\u0000\u0000\u0000\u014e\u0152\u0003\u0014\n\u0000\u014f\u0151"+
		"\u0005\u0004\u0000\u0000\u0150\u014f\u0001\u0000\u0000\u0000\u0151\u0154"+
		"\u0001\u0000\u0000\u0000\u0152\u0150\u0001\u0000\u0000\u0000\u0152\u0153"+
		"\u0001\u0000\u0000\u0000\u0153\u0156\u0001\u0000\u0000\u0000\u0154\u0152"+
		"\u0001\u0000\u0000\u0000\u0155\u0147\u0001\u0000\u0000\u0000\u0156\u0159"+
		"\u0001\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000\u0000\u0157\u0158"+
		"\u0001\u0000\u0000\u0000\u0158\u015b\u0001\u0000\u0000\u0000\u0159\u0157"+
		"\u0001\u0000\u0000\u0000\u015a\u015c\u0005\u0005\u0000\u0000\u015b\u015a"+
		"\u0001\u0000\u0000\u0000\u015b\u015c\u0001\u0000\u0000\u0000\u015c\u0013"+
		"\u0001\u0000\u0000\u0000\u015d\u016c\u0005-\u0000\u0000\u015e\u0160\u0005"+
		"\u0004\u0000\u0000\u015f\u015e\u0001\u0000\u0000\u0000\u0160\u0163\u0001"+
		"\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000\u0000\u0161\u0162\u0001"+
		"\u0000\u0000\u0000\u0162\u0164\u0001\u0000\u0000\u0000\u0163\u0161\u0001"+
		"\u0000\u0000\u0000\u0164\u0168\u0005\u0013\u0000\u0000\u0165\u0167\u0005"+
		"\u0004\u0000\u0000\u0166\u0165\u0001\u0000\u0000\u0000\u0167\u016a\u0001"+
		"\u0000\u0000\u0000\u0168\u0166\u0001\u0000\u0000\u0000\u0168\u0169\u0001"+
		"\u0000\u0000\u0000\u0169\u016b\u0001\u0000\u0000\u0000\u016a\u0168\u0001"+
		"\u0000\u0000\u0000\u016b\u016d\u0005-\u0000\u0000\u016c\u0161\u0001\u0000"+
		"\u0000\u0000\u016c\u016d\u0001\u0000\u0000\u0000\u016d\u0015\u0001\u0000"+
		"\u0000\u0000\u016e\u0172\u0005\u001b\u0000\u0000\u016f\u0171\u0005\u0004"+
		"\u0000\u0000\u0170\u016f\u0001\u0000\u0000\u0000\u0171\u0174\u0001\u0000"+
		"\u0000\u0000\u0172\u0170\u0001\u0000\u0000\u0000\u0172\u0173\u0001\u0000"+
		"\u0000\u0000\u0173\u0175\u0001\u0000\u0000\u0000\u0174\u0172\u0001\u0000"+
		"\u0000\u0000\u0175\u0179\u0005-\u0000\u0000\u0176\u0178\u0005\u0004\u0000"+
		"\u0000\u0177\u0176\u0001\u0000\u0000\u0000\u0178\u017b\u0001\u0000\u0000"+
		"\u0000\u0179\u0177\u0001\u0000\u0000\u0000\u0179\u017a\u0001\u0000\u0000"+
		"\u0000\u017a\u017c\u0001\u0000\u0000\u0000\u017b\u0179\u0001\u0000\u0000"+
		"\u0000\u017c\u0180\u0005\n\u0000\u0000\u017d\u017f\u0005\u0004\u0000\u0000"+
		"\u017e\u017d\u0001\u0000\u0000\u0000\u017f\u0182\u0001\u0000\u0000\u0000"+
		"\u0180\u017e\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000\u0000\u0000"+
		"\u0181\u01a0\u0001\u0000\u0000\u0000\u0182\u0180\u0001\u0000\u0000\u0000"+
		"\u0183\u0187\u0003\f\u0006\u0000\u0184\u0186\u0005\u0004\u0000\u0000\u0185"+
		"\u0184\u0001\u0000\u0000\u0000\u0186\u0189\u0001\u0000\u0000\u0000\u0187"+
		"\u0185\u0001\u0000\u0000\u0000\u0187\u0188\u0001\u0000\u0000\u0000\u0188"+
		"\u019a\u0001\u0000\u0000\u0000\u0189\u0187\u0001\u0000\u0000\u0000\u018a"+
		"\u018e\u0005\u0005\u0000\u0000\u018b\u018d\u0005\u0004\u0000\u0000\u018c"+
		"\u018b\u0001\u0000\u0000\u0000\u018d\u0190\u0001\u0000\u0000\u0000\u018e"+
		"\u018c\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000\u0000\u018f"+
		"\u0191\u0001\u0000\u0000\u0000\u0190\u018e\u0001\u0000\u0000\u0000\u0191"+
		"\u0195\u0003\f\u0006\u0000\u0192\u0194\u0005\u0004\u0000\u0000\u0193\u0192"+
		"\u0001\u0000\u0000\u0000\u0194\u0197\u0001\u0000\u0000\u0000\u0195\u0193"+
		"\u0001\u0000\u0000\u0000\u0195\u0196\u0001\u0000\u0000\u0000\u0196\u0199"+
		"\u0001\u0000\u0000\u0000\u0197\u0195\u0001\u0000\u0000\u0000\u0198\u018a"+
		"\u0001\u0000\u0000\u0000\u0199\u019c\u0001\u0000\u0000\u0000\u019a\u0198"+
		"\u0001\u0000\u0000\u0000\u019a\u019b\u0001\u0000\u0000\u0000\u019b\u019e"+
		"\u0001\u0000\u0000\u0000\u019c\u019a\u0001\u0000\u0000\u0000\u019d\u019f"+
		"\u0005\u0005\u0000\u0000\u019e\u019d\u0001\u0000\u0000\u0000\u019e\u019f"+
		"\u0001\u0000\u0000\u0000\u019f\u01a1\u0001\u0000\u0000\u0000\u01a0\u0183"+
		"\u0001\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1\u01a5"+
		"\u0001\u0000\u0000\u0000\u01a2\u01a4\u0005\u0004\u0000\u0000\u01a3\u01a2"+
		"\u0001\u0000\u0000\u0000\u01a4\u01a7\u0001\u0000\u0000\u0000\u01a5\u01a3"+
		"\u0001\u0000\u0000\u0000\u01a5\u01a6\u0001\u0000\u0000\u0000\u01a6\u01a8"+
		"\u0001\u0000\u0000\u0000\u01a7\u01a5\u0001\u0000\u0000\u0000\u01a8\u01ac"+
		"\u0005\u000b\u0000\u0000\u01a9\u01ab\u0005\u0004\u0000\u0000\u01aa\u01a9"+
		"\u0001\u0000\u0000\u0000\u01ab\u01ae\u0001\u0000\u0000\u0000\u01ac\u01aa"+
		"\u0001\u0000\u0000\u0000\u01ac\u01ad\u0001\u0000\u0000\u0000\u01ad\u01b0"+
		"\u0001\u0000\u0000\u0000\u01ae\u01ac\u0001\u0000\u0000\u0000\u01af\u01b1"+
		"\u0003\u0018\f\u0000\u01b0\u01af\u0001\u0000\u0000\u0000\u01b0\u01b1\u0001"+
		"\u0000\u0000\u0000\u01b1\u01b5\u0001\u0000\u0000\u0000\u01b2\u01b4\u0005"+
		"\u0004\u0000\u0000\u01b3\u01b2\u0001\u0000\u0000\u0000\u01b4\u01b7\u0001"+
		"\u0000\u0000\u0000\u01b5\u01b3\u0001\u0000\u0000\u0000\u01b5\u01b6\u0001"+
		"\u0000\u0000\u0000\u01b6\u01b8\u0001\u0000\u0000\u0000\u01b7\u01b5\u0001"+
		"\u0000\u0000\u0000\u01b8\u01b9\u0003&\u0013\u0000\u01b9\u0017\u0001\u0000"+
		"\u0000\u0000\u01ba\u01be\u0005&\u0000\u0000\u01bb\u01bd\u0005\u0004\u0000"+
		"\u0000\u01bc\u01bb\u0001\u0000\u0000\u0000\u01bd\u01c0\u0001\u0000\u0000"+
		"\u0000\u01be\u01bc\u0001\u0000\u0000\u0000\u01be\u01bf\u0001\u0000\u0000"+
		"\u0000\u01bf\u01c1\u0001\u0000\u0000\u0000\u01c0\u01be\u0001\u0000\u0000"+
		"\u0000\u01c1\u01c2\u0003\u0002\u0001\u0000\u01c2\u0019\u0001\u0000\u0000"+
		"\u0000\u01c3\u01c7\u0005 \u0000\u0000\u01c4\u01c6\u0005\u0004\u0000\u0000"+
		"\u01c5\u01c4\u0001\u0000\u0000\u0000\u01c6\u01c9\u0001\u0000\u0000\u0000"+
		"\u01c7\u01c5\u0001\u0000\u0000\u0000\u01c7\u01c8\u0001\u0000\u0000\u0000"+
		"\u01c8\u01cb\u0001\u0000\u0000\u0000\u01c9\u01c7\u0001\u0000\u0000\u0000"+
		"\u01ca\u01cc\u0005*\u0000\u0000\u01cb\u01ca\u0001\u0000\u0000\u0000\u01cb"+
		"\u01cc\u0001\u0000\u0000\u0000\u01cc\u01d0\u0001\u0000\u0000\u0000\u01cd"+
		"\u01cf\u0005\u0004\u0000\u0000\u01ce\u01cd\u0001\u0000\u0000\u0000\u01cf"+
		"\u01d2\u0001\u0000\u0000\u0000\u01d0\u01ce\u0001\u0000\u0000\u0000\u01d0"+
		"\u01d1\u0001\u0000\u0000\u0000\u01d1\u01d3\u0001\u0000\u0000\u0000\u01d2"+
		"\u01d0\u0001\u0000\u0000\u0000\u01d3\u01d7\u0005-\u0000\u0000\u01d4\u01d6"+
		"\u0005\u0004\u0000\u0000\u01d5\u01d4\u0001\u0000\u0000\u0000\u01d6\u01d9"+
		"\u0001\u0000\u0000\u0000\u01d7\u01d5\u0001\u0000\u0000\u0000\u01d7\u01d8"+
		"\u0001\u0000\u0000\u0000\u01d8\u01e8\u0001\u0000\u0000\u0000\u01d9\u01d7"+
		"\u0001\u0000\u0000\u0000\u01da\u01de\u0005\u0006\u0000\u0000\u01db\u01dd"+
		"\u0005\u0004\u0000\u0000\u01dc\u01db\u0001\u0000\u0000\u0000\u01dd\u01e0"+
		"\u0001\u0000\u0000\u0000\u01de\u01dc\u0001\u0000\u0000\u0000\u01de\u01df"+
		"\u0001\u0000\u0000\u0000\u01df\u01e1\u0001\u0000\u0000\u0000\u01e0\u01de"+
		"\u0001\u0000\u0000\u0000\u01e1\u01e5\u0003\u0002\u0001\u0000\u01e2\u01e4"+
		"\u0005\u0004\u0000\u0000\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e4\u01e7"+
		"\u0001\u0000\u0000\u0000\u01e5\u01e3\u0001\u0000\u0000\u0000\u01e5\u01e6"+
		"\u0001\u0000\u0000\u0000\u01e6\u01e9\u0001\u0000\u0000\u0000\u01e7\u01e5"+
		"\u0001\u0000\u0000\u0000\u01e8\u01da\u0001\u0000\u0000\u0000\u01e8\u01e9"+
		"\u0001\u0000\u0000\u0000\u01e9\u01ea\u0001\u0000\u0000\u0000\u01ea\u01ee"+
		"\u0005\b\u0000\u0000\u01eb\u01ed\u0005\u0004\u0000\u0000\u01ec\u01eb\u0001"+
		"\u0000\u0000\u0000\u01ed\u01f0\u0001\u0000\u0000\u0000\u01ee\u01ec\u0001"+
		"\u0000\u0000\u0000\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef\u01f1\u0001"+
		"\u0000\u0000\u0000\u01f0\u01ee\u0001\u0000\u0000\u0000\u01f1\u01f2\u0003"+
		"6\u001b\u0000\u01f2\u001b\u0001\u0000\u0000\u0000\u01f3\u01f7\u0005)\u0000"+
		"\u0000\u01f4\u01f6\u0005\u0004\u0000\u0000\u01f5\u01f4\u0001\u0000\u0000"+
		"\u0000\u01f6\u01f9\u0001\u0000\u0000\u0000\u01f7\u01f5\u0001\u0000\u0000"+
		"\u0000\u01f7\u01f8\u0001\u0000\u0000\u0000\u01f8\u01fa\u0001\u0000\u0000"+
		"\u0000\u01f9\u01f7\u0001\u0000\u0000\u0000\u01fa\u01fe\u0005-\u0000\u0000"+
		"\u01fb\u01fd\u0005\u0004\u0000\u0000\u01fc\u01fb\u0001\u0000\u0000\u0000"+
		"\u01fd\u0200\u0001\u0000\u0000\u0000\u01fe\u01fc\u0001\u0000\u0000\u0000"+
		"\u01fe\u01ff\u0001\u0000\u0000\u0000\u01ff\u0201\u0001\u0000\u0000\u0000"+
		"\u0200\u01fe\u0001\u0000\u0000\u0000\u0201\u0205\u0005\b\u0000\u0000\u0202"+
		"\u0204\u0005\u0004\u0000\u0000\u0203\u0202\u0001\u0000\u0000\u0000\u0204"+
		"\u0207\u0001\u0000\u0000\u0000\u0205\u0203\u0001\u0000\u0000\u0000\u0205"+
		"\u0206\u0001\u0000\u0000\u0000\u0206\u0208\u0001\u0000\u0000\u0000\u0207"+
		"\u0205\u0001\u0000\u0000\u0000\u0208\u0209\u0003\u0002\u0001\u0000\u0209"+
		"\u001d\u0001\u0000\u0000\u0000\u020a\u020e\u0005\"\u0000\u0000\u020b\u020d"+
		"\u0005\u0004\u0000\u0000\u020c\u020b\u0001\u0000\u0000\u0000\u020d\u0210"+
		"\u0001\u0000\u0000\u0000\u020e\u020c\u0001\u0000\u0000\u0000\u020e\u020f"+
		"\u0001\u0000\u0000\u0000\u020f\u0211\u0001\u0000\u0000\u0000\u0210\u020e"+
		"\u0001\u0000\u0000\u0000\u0211\u0215\u0005-\u0000\u0000\u0212\u0214\u0005"+
		"\u0004\u0000\u0000\u0213\u0212\u0001\u0000\u0000\u0000\u0214\u0217\u0001"+
		"\u0000\u0000\u0000\u0215\u0213\u0001\u0000\u0000\u0000\u0215\u0216\u0001"+
		"\u0000\u0000\u0000\u0216\u0219\u0001\u0000\u0000\u0000\u0217\u0215\u0001"+
		"\u0000\u0000\u0000\u0218\u021a\u0003\"\u0011\u0000\u0219\u0218\u0001\u0000"+
		"\u0000\u0000\u0219\u021a\u0001\u0000\u0000\u0000\u021a\u001f\u0001\u0000"+
		"\u0000\u0000\u021b\u021f\u0005(\u0000\u0000\u021c\u021e\u0005\u0004\u0000"+
		"\u0000\u021d\u021c\u0001\u0000\u0000\u0000\u021e\u0221\u0001\u0000\u0000"+
		"\u0000\u021f\u021d\u0001\u0000\u0000\u0000\u021f\u0220\u0001\u0000\u0000"+
		"\u0000\u0220\u0222\u0001\u0000\u0000\u0000\u0221\u021f\u0001\u0000\u0000"+
		"\u0000\u0222\u0226\u0005-\u0000\u0000\u0223\u0225\u0005\u0004\u0000\u0000"+
		"\u0224\u0223\u0001\u0000\u0000\u0000\u0225\u0228\u0001\u0000\u0000\u0000"+
		"\u0226\u0224\u0001\u0000\u0000\u0000\u0226\u0227\u0001\u0000\u0000\u0000"+
		"\u0227\u022a\u0001\u0000\u0000\u0000\u0228\u0226\u0001\u0000\u0000\u0000"+
		"\u0229\u022b\u0003\"\u0011\u0000\u022a\u0229\u0001\u0000\u0000\u0000\u022a"+
		"\u022b\u0001\u0000\u0000\u0000\u022b\u022f\u0001\u0000\u0000\u0000\u022c"+
		"\u022e\u0005\u0004\u0000\u0000\u022d\u022c\u0001\u0000\u0000\u0000\u022e"+
		"\u0231\u0001\u0000\u0000\u0000\u022f\u022d\u0001\u0000\u0000\u0000\u022f"+
		"\u0230\u0001\u0000\u0000\u0000\u0230\u023a\u0001\u0000\u0000\u0000\u0231"+
		"\u022f\u0001\u0000\u0000\u0000\u0232\u0236\u0005\u0006\u0000\u0000\u0233"+
		"\u0235\u0005\u0004\u0000\u0000\u0234\u0233\u0001\u0000\u0000\u0000\u0235"+
		"\u0238\u0001\u0000\u0000\u0000\u0236\u0234\u0001\u0000\u0000\u0000\u0236"+
		"\u0237\u0001\u0000\u0000\u0000\u0237\u0239\u0001\u0000\u0000\u0000\u0238"+
		"\u0236\u0001\u0000\u0000\u0000\u0239\u023b\u0003\u0002\u0001\u0000\u023a"+
		"\u0232\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000\u023b"+
		"!\u0001\u0000\u0000\u0000\u023c\u0240\u0005\n\u0000\u0000\u023d\u023f"+
		"\u0005\u0004\u0000\u0000\u023e\u023d\u0001\u0000\u0000\u0000\u023f\u0242"+
		"\u0001\u0000\u0000\u0000\u0240\u023e\u0001\u0000\u0000\u0000\u0240\u0241"+
		"\u0001\u0000\u0000\u0000\u0241\u0260\u0001\u0000\u0000\u0000\u0242\u0240"+
		"\u0001\u0000\u0000\u0000\u0243\u0247\u0003$\u0012\u0000\u0244\u0246\u0005"+
		"\u0004\u0000\u0000\u0245\u0244\u0001\u0000\u0000\u0000\u0246\u0249\u0001"+
		"\u0000\u0000\u0000\u0247\u0245\u0001\u0000\u0000\u0000\u0247\u0248\u0001"+
		"\u0000\u0000\u0000\u0248\u025a\u0001\u0000\u0000\u0000\u0249\u0247\u0001"+
		"\u0000\u0000\u0000\u024a\u024e\u0005\u0005\u0000\u0000\u024b\u024d\u0005"+
		"\u0004\u0000\u0000\u024c\u024b\u0001\u0000\u0000\u0000\u024d\u0250\u0001"+
		"\u0000\u0000\u0000\u024e\u024c\u0001\u0000\u0000\u0000\u024e\u024f\u0001"+
		"\u0000\u0000\u0000\u024f\u0251\u0001\u0000\u0000\u0000\u0250\u024e\u0001"+
		"\u0000\u0000\u0000\u0251\u0255\u0003$\u0012\u0000\u0252\u0254\u0005\u0004"+
		"\u0000\u0000\u0253\u0252\u0001\u0000\u0000\u0000\u0254\u0257\u0001\u0000"+
		"\u0000\u0000\u0255\u0253\u0001\u0000\u0000\u0000\u0255\u0256\u0001\u0000"+
		"\u0000\u0000\u0256\u0259\u0001\u0000\u0000\u0000\u0257\u0255\u0001\u0000"+
		"\u0000\u0000\u0258\u024a\u0001\u0000\u0000\u0000\u0259\u025c\u0001\u0000"+
		"\u0000\u0000\u025a\u0258\u0001\u0000\u0000\u0000\u025a\u025b\u0001\u0000"+
		"\u0000\u0000\u025b\u025e\u0001\u0000\u0000\u0000\u025c\u025a\u0001\u0000"+
		"\u0000\u0000\u025d\u025f\u0005\u0005\u0000\u0000\u025e\u025d\u0001\u0000"+
		"\u0000\u0000\u025e\u025f\u0001\u0000\u0000\u0000\u025f\u0261\u0001\u0000"+
		"\u0000\u0000\u0260\u0243\u0001\u0000\u0000\u0000\u0260\u0261\u0001\u0000"+
		"\u0000\u0000\u0261\u0265\u0001\u0000\u0000\u0000\u0262\u0264\u0005\u0004"+
		"\u0000\u0000\u0263\u0262\u0001\u0000\u0000\u0000\u0264\u0267\u0001\u0000"+
		"\u0000\u0000\u0265\u0263\u0001\u0000\u0000\u0000\u0265\u0266\u0001\u0000"+
		"\u0000\u0000\u0266\u0268\u0001\u0000\u0000\u0000\u0267\u0265\u0001\u0000"+
		"\u0000\u0000\u0268\u0269\u0005\u000b\u0000\u0000\u0269#\u0001\u0000\u0000"+
		"\u0000\u026a\u026e\u0005-\u0000\u0000\u026b\u026d\u0005\u0004\u0000\u0000"+
		"\u026c\u026b\u0001\u0000\u0000\u0000\u026d\u0270\u0001\u0000\u0000\u0000"+
		"\u026e\u026c\u0001\u0000\u0000\u0000\u026e\u026f\u0001\u0000\u0000\u0000"+
		"\u026f\u0271\u0001\u0000\u0000\u0000\u0270\u026e\u0001\u0000\u0000\u0000"+
		"\u0271\u0275\u0005\u0006\u0000\u0000\u0272\u0274\u0005\u0004\u0000\u0000"+
		"\u0273\u0272\u0001\u0000\u0000\u0000\u0274\u0277\u0001\u0000\u0000\u0000"+
		"\u0275\u0273\u0001\u0000\u0000\u0000\u0275\u0276\u0001\u0000\u0000\u0000"+
		"\u0276\u0278\u0001\u0000\u0000\u0000\u0277\u0275\u0001\u0000\u0000\u0000"+
		"\u0278\u0279\u0003\u0002\u0001\u0000\u0279%\u0001\u0000\u0000\u0000\u027a"+
		"\u027d\u0003(\u0014\u0000\u027b\u027d\u0003,\u0016\u0000\u027c\u027a\u0001"+
		"\u0000\u0000\u0000\u027c\u027b\u0001\u0000\u0000\u0000\u027d\'\u0001\u0000"+
		"\u0000\u0000\u027e\u0282\u0005\f\u0000\u0000\u027f\u0281\u0005\u0004\u0000"+
		"\u0000\u0280\u027f\u0001\u0000\u0000\u0000\u0281\u0284\u0001\u0000\u0000"+
		"\u0000\u0282\u0280\u0001\u0000\u0000\u0000\u0282\u0283\u0001\u0000\u0000"+
		"\u0000\u0283\u0291\u0001\u0000\u0000\u0000\u0284\u0282\u0001\u0000\u0000"+
		"\u0000\u0285\u028e\u0003.\u0017\u0000\u0286\u0288\u0005\u0004\u0000\u0000"+
		"\u0287\u0286\u0001\u0000\u0000\u0000\u0288\u0289\u0001\u0000\u0000\u0000"+
		"\u0289\u0287\u0001\u0000\u0000\u0000\u0289\u028a\u0001\u0000\u0000\u0000"+
		"\u028a\u028b\u0001\u0000\u0000\u0000\u028b\u028d\u0003.\u0017\u0000\u028c"+
		"\u0287\u0001\u0000\u0000\u0000\u028d\u0290\u0001\u0000\u0000\u0000\u028e"+
		"\u028c\u0001\u0000\u0000\u0000\u028e\u028f\u0001\u0000\u0000\u0000\u028f"+
		"\u0292\u0001\u0000\u0000\u0000\u0290\u028e\u0001\u0000\u0000\u0000\u0291"+
		"\u0285\u0001\u0000\u0000\u0000\u0291\u0292\u0001\u0000\u0000\u0000\u0292"+
		"\u0296\u0001\u0000\u0000\u0000\u0293\u0295\u0005\u0004\u0000\u0000\u0294"+
		"\u0293\u0001\u0000\u0000\u0000\u0295\u0298\u0001\u0000\u0000\u0000\u0296"+
		"\u0294\u0001\u0000\u0000\u0000\u0296\u0297\u0001\u0000\u0000\u0000\u0297"+
		"\u029a\u0001\u0000\u0000\u0000\u0298\u0296\u0001\u0000\u0000\u0000\u0299"+
		"\u029b\u0003*\u0015\u0000\u029a\u0299\u0001\u0000\u0000\u0000\u029a\u029b"+
		"\u0001\u0000\u0000\u0000\u029b\u029f\u0001\u0000\u0000\u0000\u029c\u029e"+
		"\u0005\u0004\u0000\u0000\u029d\u029c\u0001\u0000\u0000\u0000\u029e\u02a1"+
		"\u0001\u0000\u0000\u0000\u029f\u029d\u0001\u0000\u0000\u0000\u029f\u02a0"+
		"\u0001\u0000\u0000\u0000\u02a0\u02a2\u0001\u0000\u0000\u0000\u02a1\u029f"+
		"\u0001\u0000\u0000\u0000\u02a2\u02a3\u0005\r\u0000\u0000\u02a3)\u0001"+
		"\u0000\u0000\u0000\u02a4\u02a5\u0005\u0010\u0000\u0000\u02a5\u02a6\u0003"+
		"6\u001b\u0000\u02a6+\u0001\u0000\u0000\u0000\u02a7\u02ab\u0005\u0007\u0000"+
		"\u0000\u02a8\u02aa\u0005\u0004\u0000\u0000\u02a9\u02a8\u0001\u0000\u0000"+
		"\u0000\u02aa\u02ad\u0001\u0000\u0000\u0000\u02ab\u02a9\u0001\u0000\u0000"+
		"\u0000\u02ab\u02ac\u0001\u0000\u0000\u0000\u02ac\u02ae\u0001\u0000\u0000"+
		"\u0000\u02ad\u02ab\u0001\u0000\u0000\u0000\u02ae\u02af\u00036\u001b\u0000"+
		"\u02af-\u0001\u0000\u0000\u0000\u02b0\u02b4\u00034\u001a\u0000\u02b1\u02b4"+
		"\u00032\u0019\u0000\u02b2\u02b4\u00030\u0018\u0000\u02b3\u02b0\u0001\u0000"+
		"\u0000\u0000\u02b3\u02b1\u0001\u0000\u0000\u0000\u02b3\u02b2\u0001\u0000"+
		"\u0000\u0000\u02b4/\u0001\u0000\u0000\u0000\u02b5\u02b6\u00036\u001b\u0000"+
		"\u02b61\u0001\u0000\u0000\u0000\u02b7\u02b8\u0003\u000e\u0007\u0000\u02b8"+
		"3\u0001\u0000\u0000\u0000\u02b9\u02bd\u0005\u0017\u0000\u0000\u02ba\u02bc"+
		"\u0005\u0004\u0000\u0000\u02bb\u02ba\u0001\u0000\u0000\u0000\u02bc\u02bf"+
		"\u0001\u0000\u0000\u0000\u02bd\u02bb\u0001\u0000\u0000\u0000\u02bd\u02be"+
		"\u0001\u0000\u0000\u0000\u02be\u02c0\u0001\u0000\u0000\u0000\u02bf\u02bd"+
		"\u0001\u0000\u0000\u0000\u02c0\u02c4\u0005\u001a\u0000\u0000\u02c1\u02c3"+
		"\u0005\u0004\u0000\u0000\u02c2\u02c1\u0001\u0000\u0000\u0000\u02c3\u02c6"+
		"\u0001\u0000\u0000\u0000\u02c4\u02c2\u0001\u0000\u0000\u0000\u02c4\u02c5"+
		"\u0001\u0000\u0000\u0000\u02c5\u02c7\u0001\u0000\u0000\u0000\u02c6\u02c4"+
		"\u0001\u0000\u0000\u0000\u02c7\u02cb\u0003f3\u0000\u02c8\u02ca\u0005\u0004"+
		"\u0000\u0000\u02c9\u02c8\u0001\u0000\u0000\u0000\u02ca\u02cd\u0001\u0000"+
		"\u0000\u0000\u02cb\u02c9\u0001\u0000\u0000\u0000\u02cb\u02cc\u0001\u0000"+
		"\u0000\u0000\u02cc\u02ce\u0001\u0000\u0000\u0000\u02cd\u02cb\u0001\u0000"+
		"\u0000\u0000\u02ce\u02cf\u0003&\u0013\u0000\u02cf5\u0001\u0000\u0000\u0000"+
		"\u02d0\u02dd\u00038\u001c\u0000\u02d1\u02dd\u0003:\u001d\u0000\u02d2\u02dd"+
		"\u0003<\u001e\u0000\u02d3\u02dd\u0003>\u001f\u0000\u02d4\u02dd\u0003B"+
		"!\u0000\u02d5\u02dd\u0003D\"\u0000\u02d6\u02dd\u0003F#\u0000\u02d7\u02dd"+
		"\u0003H$\u0000\u02d8\u02dd\u0003J%\u0000\u02d9\u02dd\u0003L&\u0000\u02da"+
		"\u02dd\u0003N\'\u0000\u02db\u02dd\u0003P(\u0000\u02dc\u02d0\u0001\u0000"+
		"\u0000\u0000\u02dc\u02d1\u0001\u0000\u0000\u0000\u02dc\u02d2\u0001\u0000"+
		"\u0000\u0000\u02dc\u02d3\u0001\u0000\u0000\u0000\u02dc\u02d4\u0001\u0000"+
		"\u0000\u0000\u02dc\u02d5\u0001\u0000\u0000\u0000\u02dc\u02d6\u0001\u0000"+
		"\u0000\u0000\u02dc\u02d7\u0001\u0000\u0000\u0000\u02dc\u02d8\u0001\u0000"+
		"\u0000\u0000\u02dc\u02d9\u0001\u0000\u0000\u0000\u02dc\u02da\u0001\u0000"+
		"\u0000\u0000\u02dc\u02db\u0001\u0000\u0000\u0000\u02dd\u02e1\u0001\u0000"+
		"\u0000\u0000\u02de\u02e0\u0003R)\u0000\u02df\u02de\u0001\u0000\u0000\u0000"+
		"\u02e0\u02e3\u0001\u0000\u0000\u0000\u02e1\u02df\u0001\u0000\u0000\u0000"+
		"\u02e1\u02e2\u0001\u0000\u0000\u0000\u02e27\u0001\u0000\u0000\u0000\u02e3"+
		"\u02e1\u0001\u0000\u0000\u0000\u02e4\u02e8\u0005\n\u0000\u0000\u02e5\u02e7"+
		"\u0005\u0004\u0000\u0000\u02e6\u02e5\u0001\u0000\u0000\u0000\u02e7\u02ea"+
		"\u0001\u0000\u0000\u0000\u02e8\u02e6\u0001\u0000\u0000\u0000\u02e8\u02e9"+
		"\u0001\u0000\u0000\u0000\u02e9\u02eb\u0001\u0000\u0000\u0000\u02ea\u02e8"+
		"\u0001\u0000\u0000\u0000\u02eb\u02ef\u00036\u001b\u0000\u02ec\u02ee\u0005"+
		"\u0004\u0000\u0000\u02ed\u02ec\u0001\u0000\u0000\u0000\u02ee\u02f1\u0001"+
		"\u0000\u0000\u0000\u02ef\u02ed\u0001\u0000\u0000\u0000\u02ef\u02f0\u0001"+
		"\u0000\u0000\u0000\u02f0\u02f2\u0001\u0000\u0000\u0000\u02f1\u02ef\u0001"+
		"\u0000\u0000\u0000\u02f2\u02f3\u0005\u000b\u0000\u0000\u02f39\u0001\u0000"+
		"\u0000\u0000\u02f4\u02f5\u0003(\u0014\u0000\u02f5;\u0001\u0000\u0000\u0000"+
		"\u02f6\u02fa\u0005\u0019\u0000\u0000\u02f7\u02f9\u0005\u0004\u0000\u0000"+
		"\u02f8\u02f7\u0001\u0000\u0000\u0000\u02f9\u02fc\u0001\u0000\u0000\u0000"+
		"\u02fa\u02f8\u0001\u0000\u0000\u0000\u02fa\u02fb\u0001\u0000\u0000\u0000"+
		"\u02fb\u02fd\u0001\u0000\u0000\u0000\u02fc\u02fa\u0001\u0000\u0000\u0000"+
		"\u02fd\u0301\u0005\n\u0000\u0000\u02fe\u0300\u0005\u0004\u0000\u0000\u02ff"+
		"\u02fe\u0001\u0000\u0000\u0000\u0300\u0303\u0001\u0000\u0000\u0000\u0301"+
		"\u02ff\u0001\u0000\u0000\u0000\u0301\u0302\u0001\u0000\u0000\u0000\u0302"+
		"\u0321\u0001\u0000\u0000\u0000\u0303\u0301\u0001\u0000\u0000\u0000\u0304"+
		"\u0308\u0003\f\u0006\u0000\u0305\u0307\u0005\u0004\u0000\u0000\u0306\u0305"+
		"\u0001\u0000\u0000\u0000\u0307\u030a\u0001\u0000\u0000\u0000\u0308\u0306"+
		"\u0001\u0000\u0000\u0000\u0308\u0309\u0001\u0000\u0000\u0000\u0309\u031b"+
		"\u0001\u0000\u0000\u0000\u030a\u0308\u0001\u0000\u0000\u0000\u030b\u030f"+
		"\u0005\u0005\u0000\u0000\u030c\u030e\u0005\u0004\u0000\u0000\u030d\u030c"+
		"\u0001\u0000\u0000\u0000\u030e\u0311\u0001\u0000\u0000\u0000\u030f\u030d"+
		"\u0001\u0000\u0000\u0000\u030f\u0310\u0001\u0000\u0000\u0000\u0310\u0312"+
		"\u0001\u0000\u0000\u0000\u0311\u030f\u0001\u0000\u0000\u0000\u0312\u0316"+
		"\u0003\f\u0006\u0000\u0313\u0315\u0005\u0004\u0000\u0000\u0314\u0313\u0001"+
		"\u0000\u0000\u0000\u0315\u0318\u0001\u0000\u0000\u0000\u0316\u0314\u0001"+
		"\u0000\u0000\u0000\u0316\u0317\u0001\u0000\u0000\u0000\u0317\u031a\u0001"+
		"\u0000\u0000\u0000\u0318\u0316\u0001\u0000\u0000\u0000\u0319\u030b\u0001"+
		"\u0000\u0000\u0000\u031a\u031d\u0001\u0000\u0000\u0000\u031b\u0319\u0001"+
		"\u0000\u0000\u0000\u031b\u031c\u0001\u0000\u0000\u0000\u031c\u031f\u0001"+
		"\u0000\u0000\u0000\u031d\u031b\u0001\u0000\u0000\u0000\u031e\u0320\u0005"+
		"\u0005\u0000\u0000\u031f\u031e\u0001\u0000\u0000\u0000\u031f\u0320\u0001"+
		"\u0000\u0000\u0000\u0320\u0322\u0001\u0000\u0000\u0000\u0321\u0304\u0001"+
		"\u0000\u0000\u0000\u0321\u0322\u0001\u0000\u0000\u0000\u0322\u0326\u0001"+
		"\u0000\u0000\u0000\u0323\u0325\u0005\u0004\u0000\u0000\u0324\u0323\u0001"+
		"\u0000\u0000\u0000\u0325\u0328\u0001\u0000\u0000\u0000\u0326\u0324\u0001"+
		"\u0000\u0000\u0000\u0326\u0327\u0001\u0000\u0000\u0000\u0327\u0329\u0001"+
		"\u0000\u0000\u0000\u0328\u0326\u0001\u0000\u0000\u0000\u0329\u032d\u0005"+
		"\u000b\u0000\u0000\u032a\u032c\u0005\u0004\u0000\u0000\u032b\u032a\u0001"+
		"\u0000\u0000\u0000\u032c\u032f\u0001\u0000\u0000\u0000\u032d\u032b\u0001"+
		"\u0000\u0000\u0000\u032d\u032e\u0001\u0000\u0000\u0000\u032e\u0331\u0001"+
		"\u0000\u0000\u0000\u032f\u032d\u0001\u0000\u0000\u0000\u0330\u0332\u0003"+
		"\u0018\f\u0000\u0331\u0330\u0001\u0000\u0000\u0000\u0331\u0332\u0001\u0000"+
		"\u0000\u0000\u0332\u0336\u0001\u0000\u0000\u0000\u0333\u0335\u0005\u0004"+
		"\u0000\u0000\u0334\u0333\u0001\u0000\u0000\u0000\u0335\u0338\u0001\u0000"+
		"\u0000\u0000\u0336\u0334\u0001\u0000\u0000\u0000\u0336\u0337\u0001\u0000"+
		"\u0000\u0000\u0337\u0339\u0001\u0000\u0000\u0000\u0338\u0336\u0001\u0000"+
		"\u0000\u0000\u0339\u033a\u00036\u001b\u0000\u033a=\u0001\u0000\u0000\u0000"+
		"\u033b\u033f\u0005\u0014\u0000\u0000\u033c\u033e\u0005\u0004\u0000\u0000"+
		"\u033d\u033c\u0001\u0000\u0000\u0000\u033e\u0341\u0001\u0000\u0000\u0000"+
		"\u033f\u033d\u0001\u0000\u0000\u0000\u033f\u0340\u0001\u0000\u0000\u0000"+
		"\u0340\u0343\u0001\u0000\u0000\u0000\u0341\u033f\u0001\u0000\u0000\u0000"+
		"\u0342\u0344\u0003@ \u0000\u0343\u0342\u0001\u0000\u0000\u0000\u0343\u0344"+
		"\u0001\u0000\u0000\u0000\u0344\u0348\u0001\u0000\u0000\u0000\u0345\u0347"+
		"\u0005\u0004\u0000\u0000\u0346\u0345\u0001\u0000\u0000\u0000\u0347\u034a"+
		"\u0001\u0000\u0000\u0000\u0348\u0346\u0001\u0000\u0000\u0000\u0348\u0349"+
		"\u0001\u0000\u0000\u0000\u0349\u034b\u0001\u0000\u0000\u0000\u034a\u0348"+
		"\u0001\u0000\u0000\u0000\u034b\u034f\u0005\f\u0000\u0000\u034c\u034e\u0005"+
		"\u0004\u0000\u0000\u034d\u034c\u0001\u0000\u0000\u0000\u034e\u0351\u0001"+
		"\u0000\u0000\u0000\u034f\u034d\u0001\u0000\u0000\u0000\u034f\u0350\u0001"+
		"\u0000\u0000\u0000\u0350\u035e\u0001\u0000\u0000\u0000\u0351\u034f\u0001"+
		"\u0000\u0000\u0000\u0352\u035b\u0003^/\u0000\u0353\u0355\u0005\u0004\u0000"+
		"\u0000\u0354\u0353\u0001\u0000\u0000\u0000\u0355\u0356\u0001\u0000\u0000"+
		"\u0000\u0356\u0354\u0001\u0000\u0000\u0000\u0356\u0357\u0001\u0000\u0000"+
		"\u0000\u0357\u0358\u0001\u0000\u0000\u0000\u0358\u035a\u0003^/\u0000\u0359"+
		"\u0354\u0001\u0000\u0000\u0000\u035a\u035d\u0001\u0000\u0000\u0000\u035b"+
		"\u0359\u0001\u0000\u0000\u0000\u035b\u035c\u0001\u0000\u0000\u0000\u035c"+
		"\u035f\u0001\u0000\u0000\u0000\u035d\u035b\u0001\u0000\u0000\u0000\u035e"+
		"\u0352\u0001\u0000\u0000\u0000\u035e\u035f\u0001\u0000\u0000\u0000\u035f"+
		"\u0363\u0001\u0000\u0000\u0000\u0360\u0362\u0005\u0004\u0000\u0000\u0361"+
		"\u0360\u0001\u0000\u0000\u0000\u0362\u0365\u0001\u0000\u0000\u0000\u0363"+
		"\u0361\u0001\u0000\u0000\u0000\u0363\u0364\u0001\u0000\u0000\u0000\u0364"+
		"\u0366\u0001\u0000\u0000\u0000\u0365\u0363\u0001\u0000\u0000\u0000\u0366"+
		"\u0367\u0005\r\u0000\u0000\u0367?\u0001\u0000\u0000\u0000\u0368\u036c"+
		"\u0005+\u0000\u0000\u0369\u036b\u0005\u0004\u0000\u0000\u036a\u0369\u0001"+
		"\u0000\u0000\u0000\u036b\u036e\u0001\u0000\u0000\u0000\u036c\u036a\u0001"+
		"\u0000\u0000\u0000\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u036f\u0001"+
		"\u0000\u0000\u0000\u036e\u036c\u0001\u0000\u0000\u0000\u036f\u0370\u0003"+
		"6\u001b\u0000\u0370A\u0001\u0000\u0000\u0000\u0371\u0375\u0005!\u0000"+
		"\u0000\u0372\u0374\u0005\u0004\u0000\u0000\u0373\u0372\u0001\u0000\u0000"+
		"\u0000\u0374\u0377\u0001\u0000\u0000\u0000\u0375\u0373\u0001\u0000\u0000"+
		"\u0000\u0375\u0376\u0001\u0000\u0000\u0000\u0376\u0378\u0001\u0000\u0000"+
		"\u0000\u0377\u0375\u0001\u0000\u0000\u0000\u0378\u0379\u0003&\u0013\u0000"+
		"\u0379C\u0001\u0000\u0000\u0000\u037a\u037e\u0005\'\u0000\u0000\u037b"+
		"\u037d\u0005\u0004\u0000\u0000\u037c\u037b\u0001\u0000\u0000\u0000\u037d"+
		"\u0380\u0001\u0000\u0000\u0000\u037e\u037c\u0001\u0000\u0000\u0000\u037e"+
		"\u037f\u0001\u0000\u0000\u0000\u037f\u0381\u0001\u0000\u0000\u0000\u0380"+
		"\u037e\u0001\u0000\u0000\u0000\u0381\u0385\u0005-\u0000\u0000\u0382\u0384"+
		"\u0005\u0004\u0000\u0000\u0383\u0382\u0001\u0000\u0000\u0000\u0384\u0387"+
		"\u0001\u0000\u0000\u0000\u0385\u0383\u0001\u0000\u0000\u0000\u0385\u0386"+
		"\u0001\u0000\u0000\u0000\u0386\u0388\u0001\u0000\u0000\u0000\u0387\u0385"+
		"\u0001\u0000\u0000\u0000\u0388\u038c\u0005\b\u0000\u0000\u0389\u038b\u0005"+
		"\u0004\u0000\u0000\u038a\u0389\u0001\u0000\u0000\u0000\u038b\u038e\u0001"+
		"\u0000\u0000\u0000\u038c\u038a\u0001\u0000\u0000\u0000\u038c\u038d\u0001"+
		"\u0000\u0000\u0000\u038d\u038f\u0001\u0000\u0000\u0000\u038e\u038c\u0001"+
		"\u0000\u0000\u0000\u038f\u0390\u00036\u001b\u0000\u0390E\u0001\u0000\u0000"+
		"\u0000\u0391\u0395\u0005\u0016\u0000\u0000\u0392\u0394\u0005\u0004\u0000"+
		"\u0000\u0393\u0392\u0001\u0000\u0000\u0000\u0394\u0397\u0001\u0000\u0000"+
		"\u0000\u0395\u0393\u0001\u0000\u0000\u0000\u0395\u0396\u0001\u0000\u0000"+
		"\u0000\u0396\u0398\u0001\u0000\u0000\u0000\u0397\u0395\u0001\u0000\u0000"+
		"\u0000\u0398\u0399\u00036\u001b\u0000\u0399G\u0001\u0000\u0000\u0000\u039a"+
		"\u039c\u0005%\u0000\u0000\u039b\u039d\u00036\u001b\u0000\u039c\u039b\u0001"+
		"\u0000\u0000\u0000\u039c\u039d\u0001\u0000\u0000\u0000\u039dI\u0001\u0000"+
		"\u0000\u0000\u039e\u03a7\u0005\u0015\u0000\u0000\u039f\u03a3\u0005+\u0000"+
		"\u0000\u03a0\u03a2\u0005\u0004\u0000\u0000\u03a1\u03a0\u0001\u0000\u0000"+
		"\u0000\u03a2\u03a5\u0001\u0000\u0000\u0000\u03a3\u03a1\u0001\u0000\u0000"+
		"\u0000\u03a3\u03a4\u0001\u0000\u0000\u0000\u03a4\u03a6\u0001\u0000\u0000"+
		"\u0000\u03a5\u03a3\u0001\u0000\u0000\u0000\u03a6\u03a8\u00036\u001b\u0000"+
		"\u03a7\u039f\u0001\u0000\u0000\u0000\u03a7\u03a8\u0001\u0000\u0000\u0000"+
		"\u03a8K\u0001\u0000\u0000\u0000\u03a9\u03aa\u0005\u0011\u0000\u0000\u03aa"+
		"M\u0001\u0000\u0000\u0000\u03ab\u03ac\u0005\u0012\u0000\u0000\u03acO\u0001"+
		"\u0000\u0000\u0000\u03ad\u03ae\u0005-\u0000\u0000\u03aeQ\u0001\u0000\u0000"+
		"\u0000\u03af\u03b3\u0003T*\u0000\u03b0\u03b3\u0003Z-\u0000\u03b1\u03b3"+
		"\u0003\\.\u0000\u03b2\u03af\u0001\u0000\u0000\u0000\u03b2\u03b0\u0001"+
		"\u0000\u0000\u0000\u03b2\u03b1\u0001\u0000\u0000\u0000\u03b3S\u0001\u0000"+
		"\u0000\u0000\u03b4\u03b8\u0005\n\u0000\u0000\u03b5\u03b7\u0005\u0004\u0000"+
		"\u0000\u03b6\u03b5\u0001\u0000\u0000\u0000\u03b7\u03ba\u0001\u0000\u0000"+
		"\u0000\u03b8\u03b6\u0001\u0000\u0000\u0000\u03b8\u03b9\u0001\u0000\u0000"+
		"\u0000\u03b9\u03d8\u0001\u0000\u0000\u0000\u03ba\u03b8\u0001\u0000\u0000"+
		"\u0000\u03bb\u03bf\u0003V+\u0000\u03bc\u03be\u0005\u0004\u0000\u0000\u03bd"+
		"\u03bc\u0001\u0000\u0000\u0000\u03be\u03c1\u0001\u0000\u0000\u0000\u03bf"+
		"\u03bd\u0001\u0000\u0000\u0000\u03bf\u03c0\u0001\u0000\u0000\u0000\u03c0"+
		"\u03d2\u0001\u0000\u0000\u0000\u03c1\u03bf\u0001\u0000\u0000\u0000\u03c2"+
		"\u03c6\u0005\u0005\u0000\u0000\u03c3\u03c5\u0005\u0004\u0000\u0000\u03c4"+
		"\u03c3\u0001\u0000\u0000\u0000\u03c5\u03c8\u0001\u0000\u0000\u0000\u03c6"+
		"\u03c4\u0001\u0000\u0000\u0000\u03c6\u03c7\u0001\u0000\u0000\u0000\u03c7"+
		"\u03c9\u0001\u0000\u0000\u0000\u03c8\u03c6\u0001\u0000\u0000\u0000\u03c9"+
		"\u03cd\u0003V+\u0000\u03ca\u03cc\u0005\u0004\u0000\u0000\u03cb\u03ca\u0001"+
		"\u0000\u0000\u0000\u03cc\u03cf\u0001\u0000\u0000\u0000\u03cd\u03cb\u0001"+
		"\u0000\u0000\u0000\u03cd\u03ce\u0001\u0000\u0000\u0000\u03ce\u03d1\u0001"+
		"\u0000\u0000\u0000\u03cf\u03cd\u0001\u0000\u0000\u0000\u03d0\u03c2\u0001"+
		"\u0000\u0000\u0000\u03d1\u03d4\u0001\u0000\u0000\u0000\u03d2\u03d0\u0001"+
		"\u0000\u0000\u0000\u03d2\u03d3\u0001\u0000\u0000\u0000\u03d3\u03d6\u0001"+
		"\u0000\u0000\u0000\u03d4\u03d2\u0001\u0000\u0000\u0000\u03d5\u03d7\u0005"+
		"\u0005\u0000\u0000\u03d6\u03d5\u0001\u0000\u0000\u0000\u03d6\u03d7\u0001"+
		"\u0000\u0000\u0000\u03d7\u03d9\u0001\u0000\u0000\u0000\u03d8\u03bb\u0001"+
		"\u0000\u0000\u0000\u03d8\u03d9\u0001\u0000\u0000\u0000\u03d9\u03dd\u0001"+
		"\u0000\u0000\u0000\u03da\u03dc\u0005\u0004\u0000\u0000\u03db\u03da\u0001"+
		"\u0000\u0000\u0000\u03dc\u03df\u0001\u0000\u0000\u0000\u03dd\u03db\u0001"+
		"\u0000\u0000\u0000\u03dd\u03de\u0001\u0000\u0000\u0000\u03de\u03e0\u0001"+
		"\u0000\u0000\u0000\u03df\u03dd\u0001\u0000\u0000\u0000\u03e0\u03e1\u0005"+
		"\u000b\u0000\u0000\u03e1U\u0001\u0000\u0000\u0000\u03e2\u03e3\u0003X,"+
		"\u0000\u03e3W\u0001\u0000\u0000\u0000\u03e4\u03e5\u00036\u001b\u0000\u03e5"+
		"Y\u0001\u0000\u0000\u0000\u03e6\u03e8\u0005\u0004\u0000\u0000\u03e7\u03e6"+
		"\u0001\u0000\u0000\u0000\u03e8\u03eb\u0001\u0000\u0000\u0000\u03e9\u03e7"+
		"\u0001\u0000\u0000\u0000\u03e9\u03ea\u0001\u0000\u0000\u0000\u03ea\u03ec"+
		"\u0001\u0000\u0000\u0000\u03eb\u03e9\u0001\u0000\u0000\u0000\u03ec\u03f0"+
		"\u0005\u000f\u0000\u0000\u03ed\u03ef\u0005\u0004\u0000\u0000\u03ee\u03ed"+
		"\u0001\u0000\u0000\u0000\u03ef\u03f2\u0001\u0000\u0000\u0000\u03f0\u03ee"+
		"\u0001\u0000\u0000\u0000\u03f0\u03f1\u0001\u0000\u0000\u0000\u03f1\u03f3"+
		"\u0001\u0000\u0000\u0000\u03f2\u03f0\u0001\u0000\u0000\u0000\u03f3\u03f4"+
		"\u0005-\u0000\u0000\u03f4[\u0001\u0000\u0000\u0000\u03f5\u03f7\u0005\u0004"+
		"\u0000\u0000\u03f6\u03f5\u0001\u0000\u0000\u0000\u03f7\u03fa\u0001\u0000"+
		"\u0000\u0000\u03f8\u03f6\u0001\u0000\u0000\u0000\u03f8\u03f9\u0001\u0000"+
		"\u0000\u0000\u03f9\u03fb\u0001\u0000\u0000\u0000\u03fa\u03f8\u0001\u0000"+
		"\u0000\u0000\u03fb\u03ff\u0005\t\u0000\u0000\u03fc\u03fe\u0005\u0004\u0000"+
		"\u0000\u03fd\u03fc\u0001\u0000\u0000\u0000\u03fe\u0401\u0001\u0000\u0000"+
		"\u0000\u03ff\u03fd\u0001\u0000\u0000\u0000\u03ff\u0400\u0001\u0000\u0000"+
		"\u0000\u0400\u0402\u0001\u0000\u0000\u0000\u0401\u03ff\u0001\u0000\u0000"+
		"\u0000\u0402\u0406\u00036\u001b\u0000\u0403\u0405\u0005\u0004\u0000\u0000"+
		"\u0404\u0403\u0001\u0000\u0000\u0000\u0405\u0408\u0001\u0000\u0000\u0000"+
		"\u0406\u0404\u0001\u0000\u0000\u0000\u0406\u0407\u0001\u0000\u0000\u0000"+
		"\u0407\u0409\u0001\u0000\u0000\u0000\u0408\u0406\u0001\u0000\u0000\u0000"+
		"\u0409\u040d\u0005\n\u0000\u0000\u040a\u040c\u0005\u0004\u0000\u0000\u040b"+
		"\u040a\u0001\u0000\u0000\u0000\u040c\u040f\u0001\u0000\u0000\u0000\u040d"+
		"\u040b\u0001\u0000\u0000\u0000\u040d\u040e\u0001\u0000\u0000\u0000\u040e"+
		"\u042d\u0001\u0000\u0000\u0000\u040f\u040d\u0001\u0000\u0000\u0000\u0410"+
		"\u0414\u0003V+\u0000\u0411\u0413\u0005\u0004\u0000\u0000\u0412\u0411\u0001"+
		"\u0000\u0000\u0000\u0413\u0416\u0001\u0000\u0000\u0000\u0414\u0412\u0001"+
		"\u0000\u0000\u0000\u0414\u0415\u0001\u0000\u0000\u0000\u0415\u0427\u0001"+
		"\u0000\u0000\u0000\u0416\u0414\u0001\u0000\u0000\u0000\u0417\u041b\u0005"+
		"\u0005\u0000\u0000\u0418\u041a\u0005\u0004\u0000\u0000\u0419\u0418\u0001"+
		"\u0000\u0000\u0000\u041a\u041d\u0001\u0000\u0000\u0000\u041b\u0419\u0001"+
		"\u0000\u0000\u0000\u041b\u041c\u0001\u0000\u0000\u0000\u041c\u041e\u0001"+
		"\u0000\u0000\u0000\u041d\u041b\u0001\u0000\u0000\u0000\u041e\u0422\u0003"+
		"V+\u0000\u041f\u0421\u0005\u0004\u0000\u0000\u0420\u041f\u0001\u0000\u0000"+
		"\u0000\u0421\u0424\u0001\u0000\u0000\u0000\u0422\u0420\u0001\u0000\u0000"+
		"\u0000\u0422\u0423\u0001\u0000\u0000\u0000\u0423\u0426\u0001\u0000\u0000"+
		"\u0000\u0424\u0422\u0001\u0000\u0000\u0000\u0425\u0417\u0001\u0000\u0000"+
		"\u0000\u0426\u0429\u0001\u0000\u0000\u0000\u0427\u0425\u0001\u0000\u0000"+
		"\u0000\u0427\u0428\u0001\u0000\u0000\u0000\u0428\u042b\u0001\u0000\u0000"+
		"\u0000\u0429\u0427\u0001\u0000\u0000\u0000\u042a\u042c\u0005\u0005\u0000"+
		"\u0000\u042b\u042a\u0001\u0000\u0000\u0000\u042b\u042c\u0001\u0000\u0000"+
		"\u0000\u042c\u042e\u0001\u0000\u0000\u0000\u042d\u0410\u0001\u0000\u0000"+
		"\u0000\u042d\u042e\u0001\u0000\u0000\u0000\u042e\u0432\u0001\u0000\u0000"+
		"\u0000\u042f\u0431\u0005\u0004\u0000\u0000\u0430\u042f\u0001\u0000\u0000"+
		"\u0000\u0431\u0434\u0001\u0000\u0000\u0000\u0432\u0430\u0001\u0000\u0000"+
		"\u0000\u0432\u0433\u0001\u0000\u0000\u0000\u0433\u0435\u0001\u0000\u0000"+
		"\u0000\u0434\u0432\u0001\u0000\u0000\u0000\u0435\u0436\u0005\u000b\u0000"+
		"\u0000\u0436]\u0001\u0000\u0000\u0000\u0437\u043b\u0003`0\u0000\u0438"+
		"\u043b\u0003b1\u0000\u0439\u043b\u0003d2\u0000\u043a\u0437\u0001\u0000"+
		"\u0000\u0000\u043a\u0438\u0001\u0000\u0000\u0000\u043a\u0439\u0001\u0000"+
		"\u0000\u0000\u043b_\u0001\u0000\u0000\u0000\u043c\u043d\u0005\u001d\u0000"+
		"\u0000\u043d\u043e\u00036\u001b\u0000\u043e\u043f\u0003&\u0013\u0000\u043f"+
		"a\u0001\u0000\u0000\u0000\u0440\u0441\u0005\u001e\u0000\u0000\u0441\u0442"+
		"\u0003f3\u0000\u0442\u0443\u0003&\u0013\u0000\u0443c\u0001\u0000\u0000"+
		"\u0000\u0444\u0445\u0005\u0018\u0000\u0000\u0445\u0446\u0003&\u0013\u0000"+
		"\u0446e\u0001\u0000\u0000\u0000\u0447\u044a\u0003h4\u0000\u0448\u044a"+
		"\u0003j5\u0000\u0449\u0447\u0001\u0000\u0000\u0000\u0449\u0448\u0001\u0000"+
		"\u0000\u0000\u044ag\u0001\u0000\u0000\u0000\u044b\u044f\u0003\u0002\u0001"+
		"\u0000\u044c\u044e\u0005\u0004\u0000\u0000\u044d\u044c\u0001\u0000\u0000"+
		"\u0000\u044e\u0451\u0001\u0000\u0000\u0000\u044f\u044d\u0001\u0000\u0000"+
		"\u0000\u044f\u0450\u0001\u0000\u0000\u0000\u0450\u0452\u0001\u0000\u0000"+
		"\u0000\u0451\u044f\u0001\u0000\u0000\u0000\u0452\u0456\u0005\u0013\u0000"+
		"\u0000\u0453\u0455\u0005\u0004\u0000\u0000\u0454\u0453\u0001\u0000\u0000"+
		"\u0000\u0455\u0458\u0001\u0000\u0000\u0000\u0456\u0454\u0001\u0000\u0000"+
		"\u0000\u0456\u0457\u0001\u0000\u0000\u0000\u0457\u0459\u0001\u0000\u0000"+
		"\u0000\u0458\u0456\u0001\u0000\u0000\u0000\u0459\u045a\u0005-\u0000\u0000"+
		"\u045ai\u0001\u0000\u0000\u0000\u045b\u045c\u0003\u0002\u0001\u0000\u045c"+
		"k\u0001\u0000\u0000\u0000\u009eov{~\u0083\u008b\u0093\u009a\u00a1\u00a8"+
		"\u00af\u00b4\u00b8\u00ba\u00bf\u00c6\u00cf\u00d8\u00df\u00e6\u00ed\u00f4"+
		"\u00f9\u00fd\u00ff\u0104\u010d\u0114\u0118\u0120\u0126\u012d\u0134\u013b"+
		"\u0144\u014b\u0152\u0157\u015b\u0161\u0168\u016c\u0172\u0179\u0180\u0187"+
		"\u018e\u0195\u019a\u019e\u01a0\u01a5\u01ac\u01b0\u01b5\u01be\u01c7\u01cb"+
		"\u01d0\u01d7\u01de\u01e5\u01e8\u01ee\u01f7\u01fe\u0205\u020e\u0215\u0219"+
		"\u021f\u0226\u022a\u022f\u0236\u023a\u0240\u0247\u024e\u0255\u025a\u025e"+
		"\u0260\u0265\u026e\u0275\u027c\u0282\u0289\u028e\u0291\u0296\u029a\u029f"+
		"\u02ab\u02b3\u02bd\u02c4\u02cb\u02dc\u02e1\u02e8\u02ef\u02fa\u0301\u0308"+
		"\u030f\u0316\u031b\u031f\u0321\u0326\u032d\u0331\u0336\u033f\u0343\u0348"+
		"\u034f\u0356\u035b\u035e\u0363\u036c\u0375\u037e\u0385\u038c\u0395\u039c"+
		"\u03a3\u03a7\u03b2\u03b8\u03bf\u03c6\u03cd\u03d2\u03d6\u03d8\u03dd\u03e9"+
		"\u03f0\u03f8\u03ff\u0406\u040d\u0414\u041b\u0422\u0427\u042b\u042d\u0432"+
		"\u043a\u0449\u044f\u0456";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}