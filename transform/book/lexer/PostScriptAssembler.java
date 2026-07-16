package book.lexer;

import static book.lexer.BookLexerConstants.*;

import java.io.BufferedWriter;
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
 This can be done just by reacting to each token in sequence, as found in the source text.
*/
public final class PostScriptAssembler {
  
  public static void main(String[] args) throws IOException, ParseException {
    PostScriptAssembler psAssembler = new PostScriptAssembler();
    
    /*
    String bodyFile = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\transform\\book\\lexer\\simple-text-17.1252";
    String ps = psAssembler.transformBodyIntoPostScript(bodyFile, true);
    */
    
    /*
    String bodyFile = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\input\\04\\body-LEpave.1252";
    String ps = psAssembler.transformBodyIntoPostScript(bodyFile, true);
    log(ps);
    */

    String inputDir = "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\input\\";
    log("Transforming book content into PostScript. Input from: " + inputDir);
    String postScript = psAssembler.transformBookIntoPostScript(inputDir, false);
    psAssembler.saveTo1252File(postScript, "book-content-ps.1252", "C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\output\\");
    
    log("Done.");
  }
  
  String transformBookIntoPostScript(String dir, boolean logTokens)  throws ParseException, IOException {
    StringBuilder res = new StringBuilder();
    File startingDir= new File(dir);
    File[] filesAndDirs = startingDir.listFiles();
    int countChapters = 0;
    for (File item : filesAndDirs) {
      if (item.isDirectory()) {
        log(item.getCanonicalPath());
        res.append(transformChapterIntoPostScript(item.getCanonicalPath(), logTokens) + NEW_LINE + NEW_LINE);
        ++countChapters;
      }
    }
    log("Num chapters: " + countChapters);
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
        log("  " + bodyFile.getName());
      }
      else if (file.getName().startsWith("title")) {
        titleFile = file;
        log("  " + titleFile.getName());
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
    StringBuilder sb = new StringBuilder();
    InputStream is = new FileInputStream(sourceFile);
    Reader r = new InputStreamReader(is, ENCODING);
    JavaCharStream jcs = new JavaCharStream(r);
    BookLexerTokenManager mgr = new BookLexerTokenManager(jcs);
    for(Token t = mgr.getNextToken(); t.kind != BookLexerTokenManager.EOF; t = mgr.getNextToken()) {
      if (logTokens) log("Token " + t.kind + ": " + BookLexerConstants.tokenImage[t.kind] + " '" + t.image + "'" );
      translate(t, sb);
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
  
  /** Save the final PostScript to a file (windows-1252 encoding) in the given directory. */
  void saveTo1252File(String ps, String fileName, String dirName) throws IOException {
    Path path = Path.of(dirName, fileName);
    log("Saving output PostScript to " + path);
    try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)){
      writer.write(ps);
    }
  }

  private static final Charset ENCODING = Charset.forName("windows-1252");
  private static final String NEW_LINE = System.lineSeparator();
  
  private static void log(Object thing){
    System.out.println(thing.toString());
  }
  
  private String titleFor(String text) {
    return proc(string(text) + " NEW-CHAPTER");
  }
  
  private void translate(Token token, StringBuilder sb) {
    //perhaps my tokens could be improved, such that only 1 'kind' is referenced here, instead of 3
    if (token.kind == TEXT | token.kind == TEXT_LINE | token.kind == CHARS) {
      sb.append(proc("R " + string(nlToSpace(token.image))  +" PROSE"));
    }
    else if (token.kind == ITALIC_TEXT) {
      sb.append(proc("I " + string(nlToSpace(strip("_", token.image))) + " PROSE")); 
    }
    else if (token.kind == NL | token.kind == BLANK_LINE) {
      sb.append(proc("NEW-PARA"));
    }
    else if (token.kind == POEM) {
      sb.append(proc("R " + poemLines(token.image) + " true STANZA"));
    }
    else if (token.kind == CENTER) {
      sb.append(proc(string(stripFirstAndLast(nlToSpace(token.image))) + " SECTION"));
    }
    else if (token.kind == CORRESPONDANCE) {
      sb.append( proc(correspondanceParts(token.image) + " CORRESPONDANCE")  );
    }
  }

  /** 
   A stanza of a poem, with no blanks between the lines of the stanza.
   Returns an array of strings. 
  */
  private String poemLines(String text) {
    String res = "";
    String[] lines = stripFirstAndLast(text).trim().split(NEW_LINE);
    for(String line : lines) {
      res = res + string(line);
    }
    return array(res);
  }
  
  /** 
   There must be three parts: body (string), intro and outro (arrays of short strings).
   Limitation: no italics in the text. 
  */
  private String correspondanceParts(String text) {
    String[] parts = text.trim().split(NEW_LINE+NEW_LINE);
    String body = string(nlToSpace(parts[1]));
    String intro = letterLines(parts[0]);  
    String outro = letterLines(parts[2]); 
    return intro + " " + body + " " + outro;
  }
  
  private String letterLines(String text) {
    String res = "";
    String[] lines = text.replace("^", "").trim().split(NEW_LINE);
    for(String line : lines) {
      res = res + string(line);
    }
    return array(res);
  }
  
  /** An entire chapter as a PostScript data structure.*/
  private String chapter(String procs) {
    return "[" + NEW_LINE + procs + "] TYPESET";
  }
  
  private String string(String text) {
    return "(" + text + ")";
  }
  private String proc(String text) { 
    return " { " + text + " } " + NEW_LINE;
  }
  private String array(String text) { 
    return "[" + text + "]";
  }
  private String stripFirstAndLast(String text) {
    return text.substring(1, text.length()-1);
  }
  /** Replace target with empty space. */ 
  private String strip(String target, String text) {
    return text.replace(target, "");
  }
  private String nlToSpace(String text) {
    return text.replace(NEW_LINE, " ");
  }
}
