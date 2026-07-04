/** 
 Use the JavaCC tool to parse text files into tokens, and use Java to massage those tokens into PostScript.
 
 <p>The top-level class is PostScriptAssembler. 
 It uses the BookLexer class.
 
 <P>The source text needs minor changes for poems and letters (correspondance).
 Poems need to both start and end with '@', while  letters start and end with '^'.
 Since there aren't many of these, it shouldn't be too onerous.
 You'll need to grep the source tree for @ and ^ characters.
 
 <P>Example, run in the top directory: `grep -r "\^" .`
 
 <p>References:
 <ul>
  <li>https://javacc.github.io/javacc/
  <li>https://javacc.github.io/javacc/documentation/grammar.html#javacc-options
  <li>https://javacc.github.io/javacc/documentation/
 <ul>

 <p>WARNING: the basic setup of the Javacc tool is a bit wacky:
 <ol>
  <li> download the source tree
  <li> download the single binary jar file
  <li> put the binary jar in the 'target' directory in the source tree
  <li> put the 'scripts' directory of the source tree onto your path
 </oL>

 <P>WARNING: the source tree has a 'bootstrap' dir that contains an old version of Javacc. Don't use it!
 
 <P>To generate the JavaCC files:
 <pre>
 javacc book-lexer.jj
 javac *.java
 </pre>

<P>I HAVE FOUND THAT JUST DEFINING A LEXER IS SUFFICIENT FOR THIS PROJECT.
The lexer's raw output is quite close to the data I'm looking to pass to PostScript.
With the addition of some small Java 'helpers' methods, I can generate the PostScript in a satisfyingly compact way: 
the JavaCC grammar is compact, and so is the Java that transforms its tokens into PostScript.

 <P>There are many possible input formats.
 The idea here is to use JavaCC to translate from some input format into a data structure that is directly consumable by PostScript.
 That data structure contains all the text and formatting instructions that you are interested in.
 
 <P>If faced with different input formats, you just write a new parser for that format.
 
 <P>ENCODING: JavaCC seems to be designed with two main cases in mind: 
 <ul>
  <li>ASCII everywhere (the default) 
  <li>UTF-8 everywhere
 </ul>
  For other encodings (for example windows-1252), I found that more work in JavaCC was needed.
 
 <P>After some experiments, I decided to just use the simplest technique:
 <ul>
  <li>use the usual ASCII characters in the JavaCC grammar file (for ASCII characters, UTF-8 and windows-1252 share the same single-byte encoding) 
  <li>IF NEEDED, use Unicode escape sequences ('\u20D1') to refer to windows-1252 characters above the '~' character (hex 7E); add the 
  JAVA_UNICODE_ESCAPE:true flag on the JavaCC command line, to let JavaCC know you're using Unicode escape sequences 
  <li>when reading in the source text from files, just pass an encoding string explicitly to Java classes (in the usual way)
 </ul>
 
 <P>(In Windows 11, the default file encoding is UTF-8.)
  
*/
package book.lexer;