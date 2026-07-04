package book.lexer;

import java.io.IOException;

public class CustomCharStream implements CharStream {
  
    private String input;
    private int position = 0;
    private int markedPosition = 0;

    public CustomCharStream(String input) {
        this.input = input;
    }

    /** Returns the next character from the stream.     */
    @Override  public char readChar() throws IOException {
        if (position >= input.length()) {
            throw new IOException("End of stream reached");
        }
        return input.charAt(position++);
    }

    /**
     Backs up the stream pointer by the specified amount.
     This is critical for JavaCC's token manager!
    */
    @Override  public void backup(int amount) {
        position -= amount;
        if (position < 0) {
            position = 0;
        }
    }

    /*** Signals the beginning of a token. Marks the current pointer location.   */
    @Override  public char BeginToken() throws IOException {
        markedPosition = position;
        return readChar();
    }

    /** Returns the exact string representing the matched token.    */
    @Override  public String GetImage() {
        return input.substring(markedPosition, position);
    }

    /** Returns an array of characters representing the matched token.  */
    @Override  public char[] GetSuffix(int len) {
        char[] suffix = new char[len];
        input.getChars(position - len, position, suffix, 0);
        return suffix;
    }

    /** Closes the stream resource.  */
    @Override  public void Done() {
        // Clean up resources if necessary
    }

    // --- Line and Column Tracking Methods ---
    // Note: If you do not need line tracking, you can safely return 0.

    @Override public int getBeginLine() { return 0; }

    @Override  public int getBeginColumn() { return 0; }

    @Override public int getEndLine() { return 0; }

    @Override public int getEndColumn() { return 0; }

    @Override  public int getLine() { return 0; }

    @Override public int getColumn() { return 0; }

    @Override public void setTabSize(int i) {
      // TODO Auto-generated method stub
    }

    @Override public int getTabSize() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override public boolean getTrackLineColumn() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override public void setTrackLineColumn(boolean trackLineColumn) {
      // TODO Auto-generated method stub
    }
}
