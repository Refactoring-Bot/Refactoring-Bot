// Generated from botGrammar\BotOperations.g4 by ANTLR 4.7.1
package de.refactoringbot.grammar.botgrammar;
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
		T__0=1, REFACTORING=2, ADD=3, RENAME=4, REORDER=5, REMOVE=6, ADDKIND=7, 
		REORDERKIND=8, REMOVEKIND=9, RENAMEKIND=10, METHOD=11, CLASS=12, VARIABLE=13, 
		ANNOTATION=14, MODIFIER=15, PARAMETER=16, LINE=17, WORD=18, DIGIT=19, 
		WHITESPACE=20;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "REFACTORING", "ADD", "RENAME", "REORDER", "REMOVE", "ADDKIND", 
		"REORDERKIND", "REMOVEKIND", "RENAMEKIND", "METHOD", "CLASS", "VARIABLE", 
		"ANNOTATION", "MODIFIER", "PARAMETER", "LINE", "WORD", "UPPERCASE", "LOWERCASE", 
		"DIGIT", "WHITESPACE"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'BOT'", null, null, null, null, null, null, null, null, null, "'METHOD'", 
		"'CLASS'", "'VARIABLE'", null, "'MODIFIER'", "'PARAMETER'", "'LINE'", 
		null, null, "' '"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "REFACTORING", "ADD", "RENAME", "REORDER", "REMOVE", "ADDKIND", 
		"REORDERKIND", "REMOVEKIND", "RENAMEKIND", "METHOD", "CLASS", "VARIABLE", 
		"ANNOTATION", "MODIFIER", "PARAMETER", "LINE", "WORD", "DIGIT", "WHITESPACE"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\26\u00cc\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\2\3"+
		"\2\3\3\3\3\3\3\3\3\5\38\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\5\13"+
		"w\n\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\6\23\u00be"+
		"\n\23\r\23\16\23\u00bf\3\24\3\24\3\25\3\25\3\26\6\26\u00c7\n\26\r\26\16"+
		"\26\u00c8\3\27\3\27\2\2\30\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\2)\2+\25-\26\3\2\5\3\2C"+
		"\\\3\2c|\3\2\62;\2\u00d1\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\3/\3\2\2\2"+
		"\5\67\3\2\2\2\79\3\2\2\2\t@\3\2\2\2\13P\3\2\2\2\r[\3\2\2\2\17m\3\2\2\2"+
		"\21o\3\2\2\2\23q\3\2\2\2\25v\3\2\2\2\27x\3\2\2\2\31\177\3\2\2\2\33\u0085"+
		"\3\2\2\2\35\u008e\3\2\2\2\37\u00a3\3\2\2\2!\u00ac\3\2\2\2#\u00b6\3\2\2"+
		"\2%\u00bd\3\2\2\2\'\u00c1\3\2\2\2)\u00c3\3\2\2\2+\u00c6\3\2\2\2-\u00ca"+
		"\3\2\2\2/\60\7D\2\2\60\61\7Q\2\2\61\62\7V\2\2\62\4\3\2\2\2\638\5\7\4\2"+
		"\648\5\t\5\2\658\5\13\6\2\668\5\r\7\2\67\63\3\2\2\2\67\64\3\2\2\2\67\65"+
		"\3\2\2\2\67\66\3\2\2\28\6\3\2\2\29:\7C\2\2:;\7F\2\2;<\7F\2\2<=\3\2\2\2"+
		"=>\5-\27\2>?\5\17\b\2?\b\3\2\2\2@A\7T\2\2AB\7G\2\2BC\7P\2\2CD\7C\2\2D"+
		"E\7O\2\2EF\7G\2\2FG\3\2\2\2GH\5-\27\2HI\5\25\13\2IJ\5-\27\2JK\7V\2\2K"+
		"L\7Q\2\2LM\3\2\2\2MN\5-\27\2NO\5%\23\2O\n\3\2\2\2PQ\7T\2\2QR\7G\2\2RS"+
		"\7Q\2\2ST\7T\2\2TU\7F\2\2UV\7G\2\2VW\7T\2\2WX\3\2\2\2XY\5-\27\2YZ\5\21"+
		"\t\2Z\f\3\2\2\2[\\\7T\2\2\\]\7G\2\2]^\7O\2\2^_\7Q\2\2_`\7X\2\2`a\7G\2"+
		"\2ab\3\2\2\2bc\5-\27\2cd\5\23\n\2de\5-\27\2ef\7P\2\2fg\7C\2\2gh\7O\2\2"+
		"hi\7G\2\2ij\3\2\2\2jk\5-\27\2kl\5%\23\2l\16\3\2\2\2mn\5\35\17\2n\20\3"+
		"\2\2\2op\5\37\20\2p\22\3\2\2\2qr\5!\21\2r\24\3\2\2\2sw\5\27\f\2tw\5\31"+
		"\r\2uw\5\33\16\2vs\3\2\2\2vt\3\2\2\2vu\3\2\2\2w\26\3\2\2\2xy\7O\2\2yz"+
		"\7G\2\2z{\7V\2\2{|\7J\2\2|}\7Q\2\2}~\7F\2\2~\30\3\2\2\2\177\u0080\7E\2"+
		"\2\u0080\u0081\7N\2\2\u0081\u0082\7C\2\2\u0082\u0083\7U\2\2\u0083\u0084"+
		"\7U\2\2\u0084\32\3\2\2\2\u0085\u0086\7X\2\2\u0086\u0087\7C\2\2\u0087\u0088"+
		"\7T\2\2\u0088\u0089\7K\2\2\u0089\u008a\7C\2\2\u008a\u008b\7D\2\2\u008b"+
		"\u008c\7N\2\2\u008c\u008d\7G\2\2\u008d\34\3\2\2\2\u008e\u008f\7C\2\2\u008f"+
		"\u0090\7P\2\2\u0090\u0091\7P\2\2\u0091\u0092\7Q\2\2\u0092\u0093\7V\2\2"+
		"\u0093\u0094\7C\2\2\u0094\u0095\7V\2\2\u0095\u0096\7K\2\2\u0096\u0097"+
		"\7Q\2\2\u0097\u0098\7P\2\2\u0098\u0099\3\2\2\2\u0099\u009a\5-\27\2\u009a"+
		"\u009b\7Q\2\2\u009b\u009c\7x\2\2\u009c\u009d\7g\2\2\u009d\u009e\7t\2\2"+
		"\u009e\u009f\7t\2\2\u009f\u00a0\7k\2\2\u00a0\u00a1\7f\2\2\u00a1\u00a2"+
		"\7g\2\2\u00a2\36\3\2\2\2\u00a3\u00a4\7O\2\2\u00a4\u00a5\7Q\2\2\u00a5\u00a6"+
		"\7F\2\2\u00a6\u00a7\7K\2\2\u00a7\u00a8\7H\2\2\u00a8\u00a9\7K\2\2\u00a9"+
		"\u00aa\7G\2\2\u00aa\u00ab\7T\2\2\u00ab \3\2\2\2\u00ac\u00ad\7R\2\2\u00ad"+
		"\u00ae\7C\2\2\u00ae\u00af\7T\2\2\u00af\u00b0\7C\2\2\u00b0\u00b1\7O\2\2"+
		"\u00b1\u00b2\7G\2\2\u00b2\u00b3\7V\2\2\u00b3\u00b4\7G\2\2\u00b4\u00b5"+
		"\7T\2\2\u00b5\"\3\2\2\2\u00b6\u00b7\7N\2\2\u00b7\u00b8\7K\2\2\u00b8\u00b9"+
		"\7P\2\2\u00b9\u00ba\7G\2\2\u00ba$\3\2\2\2\u00bb\u00be\5)\25\2\u00bc\u00be"+
		"\5\'\24\2\u00bd\u00bb\3\2\2\2\u00bd\u00bc\3\2\2\2\u00be\u00bf\3\2\2\2"+
		"\u00bf\u00bd\3\2\2\2\u00bf\u00c0\3\2\2\2\u00c0&\3\2\2\2\u00c1\u00c2\t"+
		"\2\2\2\u00c2(\3\2\2\2\u00c3\u00c4\t\3\2\2\u00c4*\3\2\2\2\u00c5\u00c7\t"+
		"\4\2\2\u00c6\u00c5\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8"+
		"\u00c9\3\2\2\2\u00c9,\3\2\2\2\u00ca\u00cb\7\"\2\2\u00cb.\3\2\2\2\b\2\67"+
		"v\u00bd\u00bf\u00c8\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}