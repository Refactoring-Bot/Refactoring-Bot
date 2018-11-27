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

botCommand: 'BOT' WHITESPACE REFACTORING;

REFACTORING: (ADD | RENAME | REORDER | REMOVE);

ADD: 'ADD' WHITESPACE ADDKIND WHITESPACE LINE WHITESPACE DIGIT;

RENAME: 'RENAME' WHITESPACE RENAMEKIND WHITESPACE LINE WHITESPACE DIGIT WHITESPACE 'TO' WHITESPACE WORD;

REORDER: 'REORDER' WHITESPACE REORDERKIND;

REMOVE: 'REMOVE' WHITESPACE REMOVEKIND WHITESPACE LINE WHITESPACE DIGIT WHITESPACE 'NAME' WHITESPACE WORD;

ADDKIND: ANNOTATION;

REORDERKIND: MODIFIER;

REMOVEKIND: PARAMETER;

RENAMEKIND: (METHOD | CLASS | VARIABLE);

METHOD: 'METHOD';

CLASS: 'CLASS';

VARIABLE: 'VARIABLE';

ANNOTATION: 'ANNOTATION' WHITESPACE 'Override';

MODIFIER: 'MODIFIER';

PARAMETER: 'PARAMETER';

LINE: 'LINE';

WORD: (LOWERCASE | UPPERCASE)+;

fragment UPPERCASE: [A-Z];

fragment LOWERCASE: [a-z];

DIGIT: [0-9]+;

WHITESPACE: ' ';