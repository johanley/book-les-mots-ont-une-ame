/** 
 Use the JavaCC tool to parse text files.
 Ref: https://javacc.github.io/javacc/

 The setup of the Javacc tool is a bit wacky:
 <ol>
  <li> download the source tree
  <li> download the single binary jar file
  <li> put the binary jar in the 'target' directory in the source tree
  <li> put the 'scripts' directory of the source tree onto your path
 </oL>

 WARNING: the source tree has a 'bootstrap' dir that contains an old version of Javacc. Don't use it!
 
 <pre>
 javacc my-parser.jj
 javac *.java
 java MyParser
 </pre>

 There are many possible input formats.
 The idea here is to use JavaCC to translate from whatever the input format is, into
 a data structure that is directly consumable by PostScript (a dictionary).
 That data structure contains all the text and formatting instructions that you are interested in.
 
 Then, where needed, you write PostScript procs to do the actual rendering.
 For the core cases, you will have already written those procs before.
 
 If faced with different input formats, you just write a new parser for that format. 
*/
package book.parser;