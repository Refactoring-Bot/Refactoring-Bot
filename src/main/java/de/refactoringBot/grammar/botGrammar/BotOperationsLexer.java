// Generated from botGrammer\BotOperations.g4 by ANTLR 4.7.1
package de.refactoringBot.grammar.botGrammar;
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\26\u00d8\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\2\3"+
		"\2\3\3\3\3\3\3\3\3\5\38\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\5\13\u0083\n\13\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\6\23\u00ca\n\23\r\23\16\23"+
		"\u00cb\3\24\3\24\3\25\3\25\3\26\6\26\u00d3\n\26\r\26\16\26\u00d4\3\27"+
		"\3\27\2\2\30\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\35\20\37\21!\22#\23%\24\'\2)\2+\25-\26\3\2\5\3\2C\\\3\2c|\3\2\62"+
		";\2\u00dd\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\3/\3\2\2\2\5\67\3\2\2\2"+
		"\79\3\2\2\2\tD\3\2\2\2\13X\3\2\2\2\rc\3\2\2\2\17y\3\2\2\2\21{\3\2\2\2"+
		"\23}\3\2\2\2\25\u0082\3\2\2\2\27\u0084\3\2\2\2\31\u008b\3\2\2\2\33\u0091"+
		"\3\2\2\2\35\u009a\3\2\2\2\37\u00af\3\2\2\2!\u00b8\3\2\2\2#\u00c2\3\2\2"+
		"\2%\u00c9\3\2\2\2\'\u00cd\3\2\2\2)\u00cf\3\2\2\2+\u00d2\3\2\2\2-\u00d6"+
		"\3\2\2\2/\60\7D\2\2\60\61\7Q\2\2\61\62\7V\2\2\62\4\3\2\2\2\638\5\7\4\2"+
		"\648\5\t\5\2\658\5\13\6\2\668\5\r\7\2\67\63\3\2\2\2\67\64\3\2\2\2\67\65"+
		"\3\2\2\2\67\66\3\2\2\28\6\3\2\2\29:\7C\2\2:;\7F\2\2;<\7F\2\2<=\3\2\2\2"+
		"=>\5-\27\2>?\5\17\b\2?@\5-\27\2@A\5#\22\2AB\5-\27\2BC\5+\26\2C\b\3\2\2"+
		"\2DE\7T\2\2EF\7G\2\2FG\7P\2\2GH\7C\2\2HI\7O\2\2IJ\7G\2\2JK\3\2\2\2KL\5"+
		"-\27\2LM\5\25\13\2MN\5-\27\2NO\5#\22\2OP\5-\27\2PQ\5+\26\2QR\5-\27\2R"+
		"S\7V\2\2ST\7Q\2\2TU\3\2\2\2UV\5-\27\2VW\5%\23\2W\n\3\2\2\2XY\7T\2\2YZ"+
		"\7G\2\2Z[\7Q\2\2[\\\7T\2\2\\]\7F\2\2]^\7G\2\2^_\7T\2\2_`\3\2\2\2`a\5-"+
		"\27\2ab\5\21\t\2b\f\3\2\2\2cd\7T\2\2de\7G\2\2ef\7O\2\2fg\7Q\2\2gh\7X\2"+
		"\2hi\7G\2\2ij\3\2\2\2jk\5-\27\2kl\5\23\n\2lm\5-\27\2mn\5#\22\2no\5-\27"+
		"\2op\5+\26\2pq\5-\27\2qr\7P\2\2rs\7C\2\2st\7O\2\2tu\7G\2\2uv\3\2\2\2v"+
		"w\5-\27\2wx\5%\23\2x\16\3\2\2\2yz\5\35\17\2z\20\3\2\2\2{|\5\37\20\2|\22"+
		"\3\2\2\2}~\5!\21\2~\24\3\2\2\2\177\u0083\5\27\f\2\u0080\u0083\5\31\r\2"+
		"\u0081\u0083\5\33\16\2\u0082\177\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0081"+
		"\3\2\2\2\u0083\26\3\2\2\2\u0084\u0085\7O\2\2\u0085\u0086\7G\2\2\u0086"+
		"\u0087\7V\2\2\u0087\u0088\7J\2\2\u0088\u0089\7Q\2\2\u0089\u008a\7F\2\2"+
		"\u008a\30\3\2\2\2\u008b\u008c\7E\2\2\u008c\u008d\7N\2\2\u008d\u008e\7"+
		"C\2\2\u008e\u008f\7U\2\2\u008f\u0090\7U\2\2\u0090\32\3\2\2\2\u0091\u0092"+
		"\7X\2\2\u0092\u0093\7C\2\2\u0093\u0094\7T\2\2\u0094\u0095\7K\2\2\u0095"+
		"\u0096\7C\2\2\u0096\u0097\7D\2\2\u0097\u0098\7N\2\2\u0098\u0099\7G\2\2"+
		"\u0099\34\3\2\2\2\u009a\u009b\7C\2\2\u009b\u009c\7P\2\2\u009c\u009d\7"+
		"P\2\2\u009d\u009e\7Q\2\2\u009e\u009f\7V\2\2\u009f\u00a0\7C\2\2\u00a0\u00a1"+
		"\7V\2\2\u00a1\u00a2\7K\2\2\u00a2\u00a3\7Q\2\2\u00a3\u00a4\7P\2\2\u00a4"+
		"\u00a5\3\2\2\2\u00a5\u00a6\5-\27\2\u00a6\u00a7\7Q\2\2\u00a7\u00a8\7x\2"+
		"\2\u00a8\u00a9\7g\2\2\u00a9\u00aa\7t\2\2\u00aa\u00ab\7t\2\2\u00ab\u00ac"+
		"\7k\2\2\u00ac\u00ad\7f\2\2\u00ad\u00ae\7g\2\2\u00ae\36\3\2\2\2\u00af\u00b0"+
		"\7O\2\2\u00b0\u00b1\7Q\2\2\u00b1\u00b2\7F\2\2\u00b2\u00b3\7K\2\2\u00b3"+
		"\u00b4\7H\2\2\u00b4\u00b5\7K\2\2\u00b5\u00b6\7G\2\2\u00b6\u00b7\7T\2\2"+
		"\u00b7 \3\2\2\2\u00b8\u00b9\7R\2\2\u00b9\u00ba\7C\2\2\u00ba\u00bb\7T\2"+
		"\2\u00bb\u00bc\7C\2\2\u00bc\u00bd\7O\2\2\u00bd\u00be\7G\2\2\u00be\u00bf"+
		"\7V\2\2\u00bf\u00c0\7G\2\2\u00c0\u00c1\7T\2\2\u00c1\"\3\2\2\2\u00c2\u00c3"+
		"\7N\2\2\u00c3\u00c4\7K\2\2\u00c4\u00c5\7P\2\2\u00c5\u00c6\7G\2\2\u00c6"+
		"$\3\2\2\2\u00c7\u00ca\5)\25\2\u00c8\u00ca\5\'\24\2\u00c9\u00c7\3\2\2\2"+
		"\u00c9\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00cc"+
		"\3\2\2\2\u00cc&\3\2\2\2\u00cd\u00ce\t\2\2\2\u00ce(\3\2\2\2\u00cf\u00d0"+
		"\t\3\2\2\u00d0*\3\2\2\2\u00d1\u00d3\t\4\2\2\u00d2\u00d1\3\2\2\2\u00d3"+
		"\u00d4\3\2\2\2\u00d4\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5,\3\2\2\2"+
		"\u00d6\u00d7\7\"\2\2\u00d7.\3\2\2\2\b\2\67\u0082\u00c9\u00cb\u00d4\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}