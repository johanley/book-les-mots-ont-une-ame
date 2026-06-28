/** 
 Use the JavaCC tool to parse text files.
 Ref: https://javacc.github.io/javacc/

 The basic setup of the Javacc tool is a bit wacky:
 <ol>
  <li> download the source tree
  <li> download the single binary jar file
  <li> put the binary jar in the 'target' directory in the source tree
  <li> put the 'scripts' directory of the source tree onto your path
 </oL>

 <P>WARNING: the source tree has a 'bootstrap' dir that contains an old version of Javacc. Don't use it!
 
 <P>General idea of the sequence of operations:
 <pre>
 javacc -JAVA_UNICODE_ESCAPE:true my-parser.jj
 javac *.java
 java MyParser
 </pre>

 <P>There are many possible input formats.
 The idea here is to use JavaCC to translate from some input format into a data structure that is directly consumable by PostScript (a dictionary).
 That data structure contains all the text and formatting instructions that you are interested in.
 
 <P>Then, where needed, you write PostScript procs to do the actual rendering.
 In many cases, you'll be able to reuse 'old' procs for the rendering.
 
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
  <li>use Unicode escape sequences ('\u20D1') to refer to all printable windows-1252 characters above the '~' character (hex 7E)  
  <li>add the JAVA_UNICODE_ESCAPE:true flag on the JavaCC command line, to let JavaCC know you're using Unicode escape sequences 
  <li>when reading in the source text from files, just pass an encoding string explicitly to Java classes in the usual way
  <li>in this way, the source text can be either windows-1252 or UTF-8, according to taste 
 </ul>
 
 <P>(In Windows 11, the default file encoding is UTF-8.)
  
*/
package book.parser;