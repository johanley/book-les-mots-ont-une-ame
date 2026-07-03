package book.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** 
 Read text files with the expected syntax, produce tokens, and translate the tokens into corresponding PostScript snippets.
 
 <P>This file can do the translation at different levels:
 <ul> 
  <li>1 file
  <li>1 'chapter-directory' with 2 files (title and body)
  <li>list of chapter-directories (for an entire book)
 </ul> 
 
 <P>The syntax of the input 'body' text is here assumed to be correct (and compatible with the windows-1252 encoding).
 The job here is not to check the syntax, but to transform the input into valid PostScript snippets. 
 That can be done just by reacting to each token in sequence, as found in the source text.
*/
public final class PostScriptAssembler {
  
  public static void main(String[] args) throws IOException, ParseException {
    PostScriptAssembler psAssembler = new PostScriptAssembler();
    //String bodyFile = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\transform\\book\\parser\\simple-text-16.1252";
    //String bodyFile = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\input\\01\\body-La-Rempailleuse.1252";
    //String chapterDir = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\input\\01\\";
    String bookDir = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\input\\";
    //log("Assembling PostScript for 1 body file: " + bodyFile);
    String ps = psAssembler.transformBookIntoPostScript(bookDir, false);
    log(ps);
  }
  
  String transformBookIntoPostScript(String dir, boolean logTokens)  throws ParseException, IOException {
    StringBuilder res = new StringBuilder();
    File startingDir= new File(dir);
    File[] filesAndDirs = startingDir.listFiles();
    for (File item : filesAndDirs) {
      if (item.isDirectory()) {
        res.append(transformChapterIntoPostScript(item.getCanonicalPath(), logTokens) + NL + NL);
      }
    }
    return res.toString();
  }

  String transformChapterIntoPostScript(String dir, boolean logTokens)  throws ParseException, IOException {
    File titleFile = null;
    File bodyFile = null;
    File startingDir= new File(dir);
    File[] filesAndDirs = startingDir.listFiles();
    for(File file : filesAndDirs){
      if (file.getName().startsWith("body")) {
        bodyFile = file;
      }
      else if (file.getName().startsWith("title")) {
        titleFile = file;
      }
      else {
        log("Unexpected file name: " + file.getName());
      }
    }
    String res = transformTitleIntoPostScript(titleFile.getCanonicalPath());
    res = res + transformBodyIntoPostScript(bodyFile.getCanonicalPath(), logTokens);
    return chapter(res);
  }
  
  /**
    Read the source file (with windows-1252 encoding) and translate it into PostScript code.
    @param sourceFile has windows-1252 encoding, and contains body text using the expected control characters.
    @param logTokens logs each token's content, iff set to true. 
  */ 
  String transformBodyIntoPostScript(String sourceFile, boolean logTokens) throws ParseException, IOException {
    InputStream is = new FileInputStream(sourceFile);
    Reader r = new InputStreamReader(is, ENCODING);
    JavaCharStream jcs = new JavaCharStream(r);
    MyParser16TokenManager mgr = new MyParser16TokenManager(jcs);
    for(Token t = mgr.getNextToken(); t.kind != MyParser15TokenManager.EOF; t = mgr.getNextToken()) {
      if (logTokens) log("Token " + t.kind + ": " + MyParser16Constants.tokenImage[t.kind] + " '" + t.image + "'" );
      translate(t);
    }
    return sb.toString();
  }
  
  /**
   Read a small source file (with windows-1252 encoding) and translate it into PostScript code.
   @param sourceFile has windows-1252 encoding, and contains a single line of text having no control characters.
   The file is not tokenized; it's simply read as a simple, short string.
  */ 
  String transformTitleIntoPostScript(String sourceFile) throws IOException {
    Path path = Paths.get(sourceFile);
    List<String> lines = Files.readAllLines(path, ENCODING);
    return titleFor(lines.get(0).trim());
  }

  /** The final PostScript code created by this class. */ 
  @Override public String toString() {
    return sb.toString();
  }
  
  private static final Charset ENCODING = Charset.forName("windows-1252");
  private static final String NL = System.lineSeparator();
  
  private static void log(Object thing){
    System.out.println(thing.toString());
  }
  
  private String titleFor(String text) {
    return proc(string(text) + " NEW-CHAPTER");
  }
  
  private void translate(Token token) {
    //perhaps my tokens could be improved, such that only 1 'kind' is referenced here, instead of 3
    if (token.kind == MyParser16Constants.TEXT | token.kind == MyParser16Constants.TEXT_LINE | token.kind == MyParser16Constants.CHARS) {
      sb.append(proc("R " + string(nlToSpace(token.image))  +" PROSE"));
    }
    else if (token.kind == MyParser16Constants.ITALIC_TEXT) {
      sb.append(proc("I " + string(nlToSpace(stripFirstAndLast(token.image))) + " PROSE")); 
    }
    else if (token.kind == MyParser16Constants.NL | token.kind == MyParser16Constants.BLANK_LINE) {
      sb.append(proc("NEW-PARA"));
    }
    else if (token.kind == MyParser16Constants.POEM) {
      sb.append(proc("R " + poem(token.image) + " true STANZA"));
    }
    else if (token.kind == MyParser16Constants.CENTER) {
      sb.append(proc(string(stripFirstAndLast(nlToSpace(token.image))) + " CENTER"));
    }
  }

  /** A stanza of a poem, with no blanks between the lines of the stanza. */
  private String poem(String text) {
    String res = "";
    String[] lines = text.trim().split(NL);
    for(String line : lines) {
      res = res + string(line);
    }
    return array(res);
  }
  
  /** An entire chapter as a PostScript data structure.*/
  private String chapter(String procs) {
    return "[" + NL + procs + "] TYPESET";
  }
  
  private StringBuilder sb = new StringBuilder();
  private String string(String text) {
    return "(" + text + ")";
  }
  private String proc(String text) { 
    return " { " + text + " } " + NL;
  }
  private String array(String text) { 
    return "[" + text + "]";
  }
  private String stripFirstAndLast(String text) {
    return text.substring(1, text.length()-1);
  }
  private String nlToSpace(String text) {
    return text.replace(NL, " ");
  }
}
