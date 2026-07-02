package book.parser;

/** 
 React to each token by emitting corresponding PostScript snippets.
 
 <P>The syntax of the input text is here assumed to be correct.
 The job is not to check the syntax, but to transform the input into PostScript snippets. 
 That can be done just by reacting to each token in sequence, as found in the source text.
*/
final class PostScriptAssembler {

  void apply(Token token) {
    if (token.kind == MyParser15Constants.TEXT) {
      sb.append(" {R (" + token.image + ")} ");
    }
    else if (token.kind == MyParser15Constants.ITALIC_TEXT) {
      //STRIP THE UNDERSCORES
      sb.append(" {I (" + token.image + ")} "); 
    }
    else if (token.kind == MyParser15Constants.SECOND_NL) {
      sb.append(" {START_PARA} ");
    }
  }
  
  @Override public String toString() {
    return sb.toString();
  }
  
  private StringBuilder sb = new StringBuilder();
}
