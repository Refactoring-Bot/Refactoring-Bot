// Generated from botGrammer\BotOperations.g4 by ANTLR 4.7.1
package de.BA.refactoringBot.grammar.botGrammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BotOperationsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, REFACTORING=2, ADD=3, RENAME=4, REORDER=5, ADDKIND=6, REORDERKIND=7, 
		RENAMEKIND=8, METHOD=9, CLASS=10, VARIABLE=11, ANNOTATION=12, MODIFIER=13, 
		LINE=14, WORD=15, DIGIT=16, WHITESPACE=17;
	public static final int
		RULE_botCommand = 0;
	public static final String[] ruleNames = {
		"botCommand"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'BOT'", null, null, null, null, null, null, null, "'METHOD'", "'CLASS'", 
		"'VARIABLE'", null, "'MODIFIER'", "'LINE'", null, null, "' '"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "REFACTORING", "ADD", "RENAME", "REORDER", "ADDKIND", "REORDERKIND", 
		"RENAMEKIND", "METHOD", "CLASS", "VARIABLE", "ANNOTATION", "MODIFIER", 
		"LINE", "WORD", "DIGIT", "WHITESPACE"
	};
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
	public String getGrammarFileName() { return "BotOperations.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	  public java.util.HashMap<String, Double> memory = new java.util.HashMap<String, Double>();

	  @Override
	  public void notifyErrorListeners(Token offendingToken, String msg, RecognitionException ex)
	  {
	    throw new RuntimeException(msg); 
	  }

	public BotOperationsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class BotCommandContext extends ParserRuleContext {
		public TerminalNode WHITESPACE() { return getToken(BotOperationsParser.WHITESPACE, 0); }
		public TerminalNode REFACTORING() { return getToken(BotOperationsParser.REFACTORING, 0); }
		public BotCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_botCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BotOperationsListener ) ((BotOperationsListener)listener).enterBotCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BotOperationsListener ) ((BotOperationsListener)listener).exitBotCommand(this);
		}
	}

	public final BotCommandContext botCommand() throws RecognitionException {
		BotCommandContext _localctx = new BotCommandContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_botCommand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2);
			match(T__0);
			setState(3);
			match(WHITESPACE);
			setState(4);
			match(REFACTORING);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\23\t\4\2\t\2\3\2"+
		"\3\2\3\2\3\2\3\2\2\2\3\2\2\2\2\7\2\4\3\2\2\2\4\5\7\3\2\2\5\6\7\23\2\2"+
		"\6\7\7\4\2\2\7\3\3\2\2\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}