// Generated from botGrammer\BotOperations.g4 by ANTLR 4.7.1
package de.BA.refactoringBot.grammar.botGrammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BotOperationsLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, REFACTORING=2, ADD=3, RENAME=4, REORDER=5, ADDKIND=6, REORDERKIND=7, 
		RENAMEKIND=8, METHOD=9, CLASS=10, VARIABLE=11, ANNOTATION=12, MODIFIER=13, 
		LINE=14, WORD=15, DIGIT=16, WHITESPACE=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "REFACTORING", "ADD", "RENAME", "REORDER", "ADDKIND", "REORDERKIND", 
		"RENAMEKIND", "METHOD", "CLASS", "VARIABLE", "ANNOTATION", "MODIFIER", 
		"LINE", "WORD", "UPPERCASE", "LOWERCASE", "DIGIT", "WHITESPACE"
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
	  public void recover(RecognitionException ex) 
	  {
	    throw new RuntimeException(ex.getMessage()); 
	  }


	public BotOperationsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BotOperations.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\23\u00af\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\2\3\2\3\3\3\3\3\3\5\3\61\n\3\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\5\td\n\t\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20\6\20\u00a1\n\20\r\20\16\20\u00a2"+
		"\3\21\3\21\3\22\3\22\3\23\6\23\u00aa\n\23\r\23\16\23\u00ab\3\24\3\24\2"+
		"\2\25\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\2#\2%\22\'\23\3\2\5\3\2C\\\3\2c|\3\2\62;\2\u00b3\2\3\3\2\2"+
		"\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3"+
		"\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2"+
		"\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\3)\3"+
		"\2\2\2\5\60\3\2\2\2\7\62\3\2\2\2\t=\3\2\2\2\13Q\3\2\2\2\r\\\3\2\2\2\17"+
		"^\3\2\2\2\21c\3\2\2\2\23e\3\2\2\2\25l\3\2\2\2\27r\3\2\2\2\31{\3\2\2\2"+
		"\33\u0090\3\2\2\2\35\u0099\3\2\2\2\37\u00a0\3\2\2\2!\u00a4\3\2\2\2#\u00a6"+
		"\3\2\2\2%\u00a9\3\2\2\2\'\u00ad\3\2\2\2)*\7D\2\2*+\7Q\2\2+,\7V\2\2,\4"+
		"\3\2\2\2-\61\5\7\4\2.\61\5\t\5\2/\61\5\13\6\2\60-\3\2\2\2\60.\3\2\2\2"+
		"\60/\3\2\2\2\61\6\3\2\2\2\62\63\7C\2\2\63\64\7F\2\2\64\65\7F\2\2\65\66"+
		"\3\2\2\2\66\67\5\'\24\2\678\5\r\7\289\5\'\24\29:\5\35\17\2:;\5\'\24\2"+
		";<\5%\23\2<\b\3\2\2\2=>\7T\2\2>?\7G\2\2?@\7P\2\2@A\7C\2\2AB\7O\2\2BC\7"+
		"G\2\2CD\3\2\2\2DE\5\'\24\2EF\5\21\t\2FG\5\'\24\2GH\5\35\17\2HI\5\'\24"+
		"\2IJ\5%\23\2JK\5\'\24\2KL\7V\2\2LM\7Q\2\2MN\3\2\2\2NO\5\'\24\2OP\5\37"+
		"\20\2P\n\3\2\2\2QR\7T\2\2RS\7G\2\2ST\7Q\2\2TU\7T\2\2UV\7F\2\2VW\7G\2\2"+
		"WX\7T\2\2XY\3\2\2\2YZ\5\'\24\2Z[\5\17\b\2[\f\3\2\2\2\\]\5\31\r\2]\16\3"+
		"\2\2\2^_\5\33\16\2_\20\3\2\2\2`d\5\23\n\2ad\5\25\13\2bd\5\27\f\2c`\3\2"+
		"\2\2ca\3\2\2\2cb\3\2\2\2d\22\3\2\2\2ef\7O\2\2fg\7G\2\2gh\7V\2\2hi\7J\2"+
		"\2ij\7Q\2\2jk\7F\2\2k\24\3\2\2\2lm\7E\2\2mn\7N\2\2no\7C\2\2op\7U\2\2p"+
		"q\7U\2\2q\26\3\2\2\2rs\7X\2\2st\7C\2\2tu\7T\2\2uv\7K\2\2vw\7C\2\2wx\7"+
		"D\2\2xy\7N\2\2yz\7G\2\2z\30\3\2\2\2{|\7C\2\2|}\7P\2\2}~\7P\2\2~\177\7"+
		"Q\2\2\177\u0080\7V\2\2\u0080\u0081\7C\2\2\u0081\u0082\7V\2\2\u0082\u0083"+
		"\7K\2\2\u0083\u0084\7Q\2\2\u0084\u0085\7P\2\2\u0085\u0086\3\2\2\2\u0086"+
		"\u0087\5\'\24\2\u0087\u0088\7Q\2\2\u0088\u0089\7x\2\2\u0089\u008a\7g\2"+
		"\2\u008a\u008b\7t\2\2\u008b\u008c\7t\2\2\u008c\u008d\7k\2\2\u008d\u008e"+
		"\7f\2\2\u008e\u008f\7g\2\2\u008f\32\3\2\2\2\u0090\u0091\7O\2\2\u0091\u0092"+
		"\7Q\2\2\u0092\u0093\7F\2\2\u0093\u0094\7K\2\2\u0094\u0095\7H\2\2\u0095"+
		"\u0096\7K\2\2\u0096\u0097\7G\2\2\u0097\u0098\7T\2\2\u0098\34\3\2\2\2\u0099"+
		"\u009a\7N\2\2\u009a\u009b\7K\2\2\u009b\u009c\7P\2\2\u009c\u009d\7G\2\2"+
		"\u009d\36\3\2\2\2\u009e\u00a1\5#\22\2\u009f\u00a1\5!\21\2\u00a0\u009e"+
		"\3\2\2\2\u00a0\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2"+
		"\u00a3\3\2\2\2\u00a3 \3\2\2\2\u00a4\u00a5\t\2\2\2\u00a5\"\3\2\2\2\u00a6"+
		"\u00a7\t\3\2\2\u00a7$\3\2\2\2\u00a8\u00aa\t\4\2\2\u00a9\u00a8\3\2\2\2"+
		"\u00aa\u00ab\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac&\3"+
		"\2\2\2\u00ad\u00ae\7\"\2\2\u00ae(\3\2\2\2\b\2\60c\u00a0\u00a2\u00ab\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}