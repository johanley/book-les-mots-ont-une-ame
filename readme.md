**Generate a pleasing PDF for a book from files containing the raw source text. The text files can include simple formatting information (italics and so on).**

The book can have multiple chapters (or short stories, or similar).

Tools used: 
- JavaCC parser generator (I actually build only a lexer)
- PostScript (Ghostscript)

I'm using the results of this project for the source text:

https://github.com/johanley/text-variations


## What I Learned In This project

I learned how to experiment with line-breaking algorithms using PostScript.

**Line-breaking seems to be a case of pick-your-poison.**
It seems you have to choose what defects you can live with.
The ideal of an algorithm being invisible doesn't seem to be achievable.
In my opinion, the assertion that *TeX* does perfect typesetting isn't true.

Line breaking algorithms make use of these techniques to change the horizontal length of a line:
- distorting the width of the space character
- distorting the horizontal space between glyphs (*glyph width* in PostScript lingo; *letter spacing*, or *tracking* elsewhere)
- distorting the glyph horizontally (*glyph scaling* in the lingo of *InDesign*; this only affects the width of the drawing)
- hyphenating words
- (changing the font size? I haven't seen that anywhere. It might be worth a go. The change in height isn't additive, so to speak.)

**The first three all introduce some level of distortion.**
To some extent, they undermine the intent of the font designer.
They can be implemented in PostScript using the `widthshow`, `awidthshow` and `makefont` operators, respectively.
I believe *InDesign* applies the distorting operations in the order used above.
That is, *InDesign* will use glyph-scaling only as a last resort in order to get smooth-right text.

Personally, I'm a bit tired of smooth-right, distorted text. 
I'm inclined nowadays to use jagged-right, undistorted text. 
When I read such text on the web, I enjoy the calm feeling it gives me.

**A brain interacts with a page of text in two different ways**:
- the *page-as-a-whole brain* sees the overall form
- the *reading-brain* does a laser-like sequential scan over the word stream

For a jagged-right text:
- the *page-as-a-whole* brain sees a displeasing, irregular form
- the *reading-brain* isn't annoyed by the jaggedness 

For smooth-right text, the converse is true: 
- the *page-as-a-whole* brain sees a pleasant rectangular form
- the *reading-brain* is annoyed by small distortions in the text

Again: pick your poison.

Paragraph indentation: the smaller it is, the more it helps with fitting words on the line.

In old English text, it's common to see a *double* space between a period and the start of the next sentence.

In the range 0..127 (0..7F, the range of ASCII), **most (but not all) encodings** use exactly 
the same mapping of glyphs to bytes.
In that case, a file containing such bytes can have multiple valid encodings associated with it.

**At least two encodings are *not* the same as ASCII in the range 0..7F**:
- PostScript's built-in *StandardEncoding* 
- *ISOLatin-1*

**The difference is just a single glyph/byte**: octal 140 (hex 60) is a *grave* in ASCII, but a *quoteleft* in 
both *ISOLatin-1* and PostScript's *StandardEncoding*.
(See the *PostScript Language Reference Manual*, 3rd edition, page 783.)
Sometimes you see text which mixes a *grave* with a quote character, like this: 

`blah blah' 

This might be caused by using *ISOLatin-1*, and incorrectly expecting it to be ASCII-compatible.

PostScript handles single-byte encodings easily, using its encoding vector (whose length is 256).
For multiple-byte encodings (Unicode encodings), PostScript can handle them, but with 
significantly more work (so I hear; I've never tried that myself).

The *windows-1252* encoding has more to offer than 8859-1. 
Its set of glyphs is larger; rounded quotations and em-dashes are very nice to have, but are absent from 8859-1.

Sometimes all you need is a lexer (tokens), not a full parser. 
If you can assume that the source text is syntactically correct, then using only a lexer can be good enough.

I had trouble trying to build a JavaCC parser for the source text. 
I eventually gave up, and just used a lexer.
I had problems removing ambiguity.
The newline was particularly painful, because it's used in three different ways:
- an *accidental* new line in source text prose (which get replaced by single spaces)
- a paragraph separator
- a line separator (in the context of poetry).

**The line between lexing and parsing is not always clear-cut**.

In my case, a simple lexer combined with some simple Java code was able to generate 
the desired PostScript data structures with only a small amount of code. 
This was a pleasing result.



## Notepad++ Encoding Logic Is Problematic For Me
Notepad++ has a lot of internal logic for encodings under the hood. 
Perhaps it's best not to rely on it for either detection of encoding or conversion of encoding.
In Notepad++, 'ANSI' means different things. 

Its 'Convert to...' operations *preserve glyphs*, while changing bytes that represent those glyphs.
Above the 'Convert to...' part, operations *preserve bytes*, but change displayed glyphs.
These are two completely different operations! 
I find this all rather confusing and unclear for casual use.

## General Notes
The general flow:
- find or write the source text; use the *CP1252* encoding (also known as *windows-1252*)
- `transform.book.util.WeirdBytValues.java`: examine the bytes in all files, looking for weird characters
- `transform.book.lexer.PostScriptAssembler`: a JavaCC lexer reads the text and emits PostScript code containing the *runs* of text having the same style 
- `output.BOOK.PS`: the generated PostScript code is pasted in here (if needed, small tweaks can be made here)
- a Ghostscript command changes `BOOK.PS` into the final PDF
- you print the book using lulu.com or a similar service


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


Example Ghostscript command to generate a PDF from a PostScript file:

`C:\ghostscript\gs10.04.0\bin\gswin64c.exe -dNOSAFER -sDEVICE=pdfwrite -o BOOK.PDF BOOK.PS`

The location of your font can be added as another command line switch:

`-sFONTPATH=C:\Windows\Fonts`

or it can be set in your environment: 

`GS_FONTPATH=C:\Windows\Fonts;C:\Users\Owner\AppData\Local\Microsoft\Fonts`
 
 
## Encoding Details 
 
I'm not sure how to use Unicode encodings in PostScript. 
I think it's possible, but more difficult.
For this project, I'm going to stick with the *CP-1252* encoding:
- it's expressive enough for my needs (and more expressive than 8859-1)
- it's a simple single-byte encoding
- it includes attractive curly double quotes
- it's easily used with PostScript
- it's easy to detect 'weird' bytes in source text

Reference for using *CP-1252* in PostScript: 
  https://stackoverflow.com/questions/32443178/how-do-i-encode-a-font-to-use-cp1252-windows-1252-encoding-in-postscript
See the response from KenS, to see how to re-encode a font to use *windows-1252*.
I made several corrections to his suggested encoding vector.

### Comparison of encodings

8859-1 has 191 visible chars in these ranges, in octal (hex):
- 040..176 (20..7E)
- 240..377 (A0..FF)

*ISOLatin-1* (PostScript) 191 + 14 (almost always unused) = 205 visible chars in these ranges, in octal (hex)
- 040..176 (20..7E)
- 220..230 (90..98)
- 232..233 (9A..9B)
- 235..377 (9D..FF)

*ISOLatin-1* is nearly a superset of 8859-1, except for one char (!): octal 140 (hex 60): 
- *quoteleft* in *ISOLatin-1*
- *grave* in 8859-1.
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

## Punctuation oddities:
- the 8859-1 open-quote (octal 047, hex 27) character is actually a back-quote on my PC's keyboard.
- the 8859-1 encoding has a 'minus' character (octal 055, hex 2D) and a 'hyphen' (octal 255, hex AD) character that are very similar.
        The hyphen is shorter, and usually is preferred.
        The hypen character has no dedicated key on my keyboard; Alt+0173 is needed.
- in 8859-1, the apostrophe is a single right quote (octal 047, hex 27)
- the apostrophe in *windows-1252* is (octal 221, hex 92), not used in 8859-1 or *ISOLatin-1*.


Selected *CP1252* characters (hex):
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
- extra line: ~~~~
- | at the start of Mademoiselle Perle? Some kind of section separator, I think.
- italic: underscores, as in  _Jean-Guiton_ in L'Epave.
- correspondance: an example in L'Enfant. '^'. Perhaps it means indentation?
  It has both '^' and '^120'
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

In simple cases, it's possible to use only the lexer, in order to get at the sequence of tokens.
That's reasonable iff you trust that the source data has the correct syntax.

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

Tokens are chained together, and know their neighbour.    

Be careful with where the 'main' method goes: either the parser, or the TokenManager.
For listing the detected tokens, it seems to make sense to put that code in the TokenManager class, using TOKEN_MGR_DECLS.

Token names need to be unique, even if they belong to different lexical states(?).

