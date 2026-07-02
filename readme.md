**Generate a pleasing PDF for a book from files containing the raw source text. The text files can include formatting information (italics and so on).**

The book can have multiple chapters (or short stories, or similar).

Tools used: 
- JavaCC parser generator
- PostScript (Ghostscript)

The general flow:
- find or write the source text (usually with the CP1252 encoding (also known as 'windows-1252')
- a JavaCC parser reads the text and emits PostScript dictionaries (saved to files) containing the 'runs' of text having the same style
- a 'MAIN.PS' PostScript file reads the dictionary files and, if needed, it allows for custom, manual changes
- a Ghostscript command changes the PostScript into the final PDF
- you print the book using lulu.com or similar 


There are many, many ways to typeset text : 
- regular text flow
- letters
- poems
- dealing with orphaned text and custom line breaks
- centered snippets
- footnotes
- and many other variations

It's difficult to foresee all possibilities.
The idea here is to abandon the idea of trying to automate all possible variations in the text, and just concentrate on what you need the most in a given case.
In most cases, what you need the most is simply the body of the chapter text. 
Secondary items like a title page or a table of contents can be hand-written directly in PostScript.

Outline of the steps to make an output (a single PDF file): 
- 1. acquire the source text from somewhere. 
     Save under the input directory. 
     Use CP-1252 encoding; convert if needed. In Java, use Charset and CharsetEncoder (I think).
     Verify that all bytes are consistent with the CP-1252 encoding (see WeirdByteValues.java).
     Separate into chapters, if appropriate.
     Formatting codes are used in the source text (for italic text, for example).
- 2. 'preprocess' to generate an initial version. 
     Run Java code to generate PostScript dictionaries (saved in files) that contain the source text and formatting instructions expressed in a way that's consumable by PostScript.
     The main idea is to chunk the text into N 'runs' of text, with each run sharing the same style/format.
     The PostScript dictionaries (files) are then read directly by other PostScript files representing the whole document/book.
- 3. run GhostScript to render a PDF. 
     Review manually to find errors. Fix where needed. 
     This usually means writing code to handle a new variation on typesetting text.
     Such changes may or may not be absorbed back into the 'core' preprocessor.


Example Ghostscript command to generate a PDF from a PostScript file:
`C:\ghostscript\gs10.04.0\bin\gswin64c.exe -dNOSAFER -sDEVICE=pdfwrite -o COVER.PDF COVER.PS`

The location of your font can be added as another command line switch:
`-sFONTPATH=C:\Windows\Fonts`

or it can be set in your environment: 
`GS_FONTPATH=C:\Windows\Fonts;C:\Users\Owner\AppData\Local\Microsoft\Fonts`
 
 
## Encoding Details 
 
I'm not sure how to use Unicode encodings in PostScript. 
I think it's possible, but more difficult.
For this project, I'm going to stick with the CP-1252 encoding:
- it's expressive enough for my needs (and more expressive than 8859-1)
- it's a simple single-byte encoding
- it includes attractive curly double quotes
- it's easily used with PostScript
- it's easy to detect 'weird' bytes in source text

Reference for using CP-1252 in PostScript: 
  https://stackoverflow.com/questions/32443178/how-do-i-encode-a-font-to-use-cp1252-windows-1252-encoding-in-postscript
See the response from KenS, to see how to re-encode a font to use Windows-1252.

### Comparison of encodings

8859-1 has 191 visible chars in these ranges, in octal (hex):
- 040..176 (20..7E)
- 240..377 (A0..FF)

ISOLatin-1 (PostScript) 191 + 14 (almost always unused) = 205 visible chars in these ranges, in octal (hex)
- 040..176 (20..7E)
- 220..230 (90..98)
- 232..233 (9A..9B)
- 235..377 (9D..FF)

ISOLatin-1 is nearly a superset of 8859-1, except for one char (!): octal 140 (hex 60): 
- quoteleft in ISOLatin-1
- grave in 8859-1.
- this is the source of the weird occurence of grave where an open-single-quote is expected by PostScript

Windows 1252 has 191 + 27 = 218 visible chars 
- it is a superset of 8859-1 (close enough; the control codes don't matter in practice), so 8859-1 data can be used without change
- the 27 additional characters include double quotation marks that have a more pleasing appearance
- 040..176 (20..7E)
- 200 (80)
- 202..214 (82..8C)
- 216 (8E)
- 221..234 (91..9C)
- 236..377 (9E..FF)

In the range 0..127 (0..7F), the encoding of windows-1252 is the same as UTF-8.
In that case, a file can have more than one valid encoding associated with it!
    

## Notepad++ Encoding Logic Is Problematic For Me
Notepad++ has a lot of internal logic for encodings under the hood. 
Perhaps it's best not to rely on it for either detection of encoding or conversion of encoding.
In Notepad++, 'ANSI' means different things. 

Its 'Convert to...' operations preserve glyphs, while changing bytes that represent those glyphs.
Above the 'Convert to...' part, operations preserve bytes, but change displayed glyphs.
** WARNING ** : These are two completely different operations! 
I find this all rather confusing and unclear for casual use.
      

## Punctuation oddities:
- the 8859-1 open-quote (octal 047, hex 27) character is actually a back-quote on my PC's keyboard.
- the 8859-1 encoding has a 'minus' character (octal 055, hex 2D) and a 'hyphen' (octal 255, hex AD) character that are very similar.
        The hyphen is shorter, and usually is preferred.
        The hypen character has no dedicated key on my keyboard; Alt+0173 is needed.
- in 8859-1, the apostrophe is a single right quote (octal 047, hex 27)
- the apostrophe in Windows-1252 is (octal 221, hex 92), not used in 8859-1 or ISOLatin-1.


Selected CP1252 characters (hex):
- open, close curly double quote: 93, 94
- open, close curly single quote: 91, 92
- open, close guillemet: AB, BB
- minus sign: 2D
- short (en) and long (em) dash: 96, 97 
- ellipsis: 85
- straight apostrophe: 27
- curved apostrophe: 92, re-use the close curly single quote


Selected 8859-1 characters, octal (hex)
- emdash: none!
- endash: none!
- left-right curvy double quotes: none! only the straight version is available
- single-quote (straight): none! only the curvy left-right version is available
- open guillemet: 253 (AB)
- close guillemet: 273 (BB)
- hyphen: 255 (AD) is shorter than minus, and is usually preferred
- minus: 055 (2D)
- double-quote (straight, open and close): 042 (27)
- grave/left-single-quote: 140 (60) (be careful! grave-or-quote issue!)
- right-single-quote: 047 (27)
- eacute: 351 (E9)
- egrave: 350 (E8)    
- ccedilla: 347 (E7)


## Source Text Styling Formats 

- paragraph: blank line
- extra line: '~~~~'
- | at the start of Mademoiselle Perle? Some kind of section separator, I think.
- italic: underscores, as in  _Jean-Guiton_ in L'Epave.
- correspondance: an example in L'Enfant, Decouverte. '^'. Perhaps it means indentation?
- poetry: an example in Le Lit. '@'


## JavaCC 

There's often no clear division between the pieces (tokens) and how they are put together (syntax).
Since you can define new tokens from existing ones, that is indeed a syntactic operation.

You could have fewer tokens, and more syntactic productions, or vice versa.
The tokens could be little independent pieces, that are never 'put together' to make more tokens. 
Or, you can indeed make tokens from smaller tokens. 
It seems to be a matter of choice where you 'choose to stop' defining tokens, so to speak.

AI recommends: 'If something is naturally a single "word" in the language, make it a token. 
If it represents a relationship between words, make it a grammar production.'

AI recommends: 'Keep the lexer dumb, and the parser smart.'

Parser construction is simplified if you separate characters into two bags: 
- control / typesetting
- regular text flow

If those two bags have no overlap, then you avoid a lot of difficulties from the start.

The order of appearance of tokens can be very important.
If you change the order of appearance of the token definitions, that will often cause large changes in behaviour.

Unexpected: if you only define a simple 'skip tabs' token defined, every single character in the stream is apparently a token by default.

Unexpected: the built-in <EOF> can't be placed inside a token definition. 
It can only appear in a 'syntactic production'. 

The snippet of code you see in docs for viewing all of the tokens in the stream:
 
    `for(Token t = mgr.getNextToken(); t.kind != EOF; t = mgr.getNextToken()) {
      log("Token: '" + t.image + "'");
      ++numTokensFound;
    }`
    
This code snippet fails when the input is from a file. 
It barfs on the final EOF.
You have a choice:
- do nothing about it, but just be aware of it
- read the file content into a String, and pass a StringReader into the tokenizer    


Be careful with where the 'main' method goes: either the parser, or the TokenManager.
For listing the detected tokens, it seems to make sense to put that code in the TokenManager class, using TOKEN_MGR_DECLS.

Token names need to be unique, even if they belong to different lexical states(?).

