grammar BotOperations;

@parser::members 
{
  public java.util.HashMap<String, Double> memory = new java.util.HashMap<String, Double>();

  @Override
  public void notifyErrorListeners(Token offendingToken, String msg, RecognitionException ex)
  {
    throw new RuntimeException(msg); 
  }
}

@lexer::members 
{
  @Override
  public void recover(RecognitionException ex) 
  {
    throw new RuntimeException(ex.getMessage()); 
  }
}

botCommand: USERNAME WHITESPACE REFACTORING EOF;

REFACTORING: (ADD | RENAME | REORDER | REMOVE);

ADD: 'ADD' WHITESPACE ADDKIND;

RENAME: 'RENAME' WHITESPACE RENAMEKIND WHITESPACE 'TO' WHITESPACE WORD;

REORDER: 'REORDER' WHITESPACE REORDERKIND;

REMOVE: 'REMOVE' WHITESPACE REMOVEKIND;

ADDKIND: ANNOTATION;

REORDERKIND: 'MODIFIER';

REMOVEKIND: PARAMETER;

RENAMEKIND: 'METHOD';

ANNOTATION: 'ANNOTATION' WHITESPACE SUPPORTEDANNOTATIONS;

SUPPORTEDANNOTATIONS: 'Override';

PARAMETER: 'PARAMETER' WHITESPACE WORD;

WORD: (LOWERCASE | UPPERCASE)+;

USERNAME: (UPPERCASE | LOWERCASE | NUMBER | SYMBOL)+;

fragment UPPERCASE: [A-Z];

fragment LOWERCASE: [a-z];

DIGIT: [0-9]+;

NUMBER: [0-9];

SYMBOL: ('-' | '_' | '%' | '&' | '/' | 'ß' | '@');

WHITESPACE: ' ';