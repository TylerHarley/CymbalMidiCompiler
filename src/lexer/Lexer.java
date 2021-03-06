/* 
 * lexer.jlex
 * Edited by Terrence Tan, Harry Bartlett, Tyler Harley
 * Last Edit: 02/17/2014
 * Added %eofval cases
 * Added multiple regular expression rules and 3 states
 *
 */
/* Bantam Java Compiler and Language Toolset.
   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.
   The Bantam Java toolset is distributed under the following 
   conditions:
     You may make copies of the toolset for your own use and 
     modify those copies.
     All copies of the toolset must retain the author names and 
     copyright notice.
     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE. 
*/
/* code below is copied to the file containing the lexer */
package lexer;
import parser.TokenIds;
/* import Symbol class, which represents the symbols that are passed
   from the lexer to the parser.  Each symbol consists of an ID 
   and a token value, which is defined in Token.java */
import java_cup.runtime.Symbol;


public class Lexer implements java_cup.runtime.Scanner {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

    /* code below is copied to the class containing the lexer */
    /** maximum string size allowed */
    private final int MAX_STRING_SIZE = 5000;
    /** boolean indicating whether debugging is enabled */
    private boolean debug = false;
    /** boolean indicating whether we're lexing multiple files or a single file */
    private boolean multipleFiles = false;
    /** array that holds the names of each file we're lexing 
      * (used only when multipleFiles is true)
      * */
    private String[] filenames;
    /** array that holds the reader for each file we're lexing 
      * (used only when multipleFiles is true)
      * */
    private java.io.BufferedReader[] fileReaders;
    /** current file number used to index filenames and fileReaders
      * (used only when multipleFiles is true)
      * */
    private int fileCnt = 0;
    /** Lexer constructor - defined in JLex specification file
      * Needed to handle lexing multiple files
      * @param filenames list of filename strings
      * @param debug boolean indicating whether debugging is enabled
      * */
    public Lexer(String[] filenames, boolean debug) {
	// call private constructor, which does some initialization
	this();
	this.debug = debug;
	// set the multipleFiles flag to true (provides compatibility
	// with the single file constructors)
	multipleFiles = true;
	// initialize filenames field to parameter filenames
	// used later for finding the name of the current file
	this.filenames = filenames;
	// check that there is at least one specified filename
	if (filenames.length == 0)
	    throw new RuntimeException("Must specify at least one filename to scan");
	// must initialize readers for each file (BufferedReader)
	fileReaders = new java.io.BufferedReader[filenames.length];
	for (int i = 0; i < filenames.length; i++) {
	    // try...catch checks if file is found
	    try {
		// create the ith file reader
		fileReaders[i] = new java.io.BufferedReader(new java.io.FileReader(filenames[i]));
	    }
	    catch(java.io.FileNotFoundException e) {
		// if file not found then report an error and exit
		System.err.println("Error: file '" + filenames[i] + "' not found");
		System.exit(1);
	    }
	}
	// set yy_reader (a JLex variable) to the first file reader
	yy_reader = fileReaders[0];
	// set yyline to 1 (as opposed to 0)
	yyline = 1;
    }
    /** holds the current string constant
      * note: we use StringBuffer so that appending does not require constructing a new object 
      * */
    private StringBuffer currStringConst;
    /** getter method for accessing the current line number
      * @return current line number
      * */
    public int getCurrLineNum() {
	return yyline;
    }
    /** getter method for accessing the current file name
      * @return current filename string
      * */
    public String getCurrFilename() {
	return filenames[fileCnt];
    }
    /** print tokens - used primarily for debugging the lexer 
      * */
    public void printTokens() throws java.io.IOException {
	// prevFileCnt is used to determine when the filename has changed
	// every time an EOF is encountered fileCnt is incremented
	// by testing fileCnt with prevFileCnt, we can determine when the
	// filename has changed and print the filename along with the tokens
	int prevFileCnt = -1;
	// try...catch needed since next_token() can throw an IOException
	try {
	    // iterate through all tokens
	    while (true) {
		// get the next token
		Symbol symbol = next_token();
		// check if file has changed
		if (prevFileCnt != fileCnt) {
		    // if it has then print out the new filename
		    System.out.println("# " + filenames[fileCnt]);
		    // update prevFileCnt
		    prevFileCnt = fileCnt;
		}
		// print out the token
		System.out.println((Token)symbol.value);
		// if we've reached the EOF (EOF only returned for the last
		// file) then we break out of loop
		if (symbol.sym == TokenIds.EOF)
		    break;
	    }
	}
	catch (java.io.IOException e) {
	    // if an IOException occurs then print error and exit
	    System.err.println("Unexpected IO exception while scanning.");
	    throw e;
	}
    }
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private int yyline;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	public Lexer (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	public Lexer (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Lexer () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yyline = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;

    // set yyline to 1 (as opposed to 0)
    yyline = 1;
	}

	private boolean yy_eof_done = false;
	private final int YYSTR_ESCAPE = 2;
	private final int YYINITIAL = 0;
	private final int YYMLCOMMENT = 3;
	private final int YYINSTRING = 1;
	private final int yy_state_dtrans[] = {
		0,
		107,
		108,
		109
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr=true;
			} else yy_last_was_cr=false;
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NOT_ACCEPT,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NOT_ACCEPT,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NOT_ACCEPT,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NOT_ACCEPT,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NOT_ACCEPT,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NOT_ACCEPT,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NOT_ACCEPT,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NOT_ACCEPT,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NOT_ACCEPT,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NOT_ACCEPT,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NOT_ACCEPT,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NOT_ACCEPT,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NOT_ACCEPT,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NOT_ACCEPT,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NOT_ACCEPT,
		/* 68 */ YY_NO_ANCHOR,
		/* 69 */ YY_NOT_ACCEPT,
		/* 70 */ YY_NOT_ACCEPT,
		/* 71 */ YY_NOT_ACCEPT,
		/* 72 */ YY_NOT_ACCEPT,
		/* 73 */ YY_NOT_ACCEPT,
		/* 74 */ YY_NOT_ACCEPT,
		/* 75 */ YY_NOT_ACCEPT,
		/* 76 */ YY_NOT_ACCEPT,
		/* 77 */ YY_NOT_ACCEPT,
		/* 78 */ YY_NOT_ACCEPT,
		/* 79 */ YY_NOT_ACCEPT,
		/* 80 */ YY_NOT_ACCEPT,
		/* 81 */ YY_NOT_ACCEPT,
		/* 82 */ YY_NOT_ACCEPT,
		/* 83 */ YY_NOT_ACCEPT,
		/* 84 */ YY_NOT_ACCEPT,
		/* 85 */ YY_NOT_ACCEPT,
		/* 86 */ YY_NOT_ACCEPT,
		/* 87 */ YY_NOT_ACCEPT,
		/* 88 */ YY_NOT_ACCEPT,
		/* 89 */ YY_NOT_ACCEPT,
		/* 90 */ YY_NOT_ACCEPT,
		/* 91 */ YY_NOT_ACCEPT,
		/* 92 */ YY_NOT_ACCEPT,
		/* 93 */ YY_NOT_ACCEPT,
		/* 94 */ YY_NOT_ACCEPT,
		/* 95 */ YY_NOT_ACCEPT,
		/* 96 */ YY_NOT_ACCEPT,
		/* 97 */ YY_NOT_ACCEPT,
		/* 98 */ YY_NOT_ACCEPT,
		/* 99 */ YY_NOT_ACCEPT,
		/* 100 */ YY_NOT_ACCEPT,
		/* 101 */ YY_NOT_ACCEPT,
		/* 102 */ YY_NOT_ACCEPT,
		/* 103 */ YY_NOT_ACCEPT,
		/* 104 */ YY_NOT_ACCEPT,
		/* 105 */ YY_NOT_ACCEPT,
		/* 106 */ YY_NOT_ACCEPT,
		/* 107 */ YY_NOT_ACCEPT,
		/* 108 */ YY_NOT_ACCEPT,
		/* 109 */ YY_NOT_ACCEPT,
		/* 110 */ YY_NOT_ACCEPT,
		/* 111 */ YY_NOT_ACCEPT,
		/* 112 */ YY_NOT_ACCEPT,
		/* 113 */ YY_NOT_ACCEPT,
		/* 114 */ YY_NOT_ACCEPT,
		/* 115 */ YY_NOT_ACCEPT,
		/* 116 */ YY_NOT_ACCEPT,
		/* 117 */ YY_NOT_ACCEPT,
		/* 118 */ YY_NOT_ACCEPT,
		/* 119 */ YY_NOT_ACCEPT,
		/* 120 */ YY_NOT_ACCEPT,
		/* 121 */ YY_NOT_ACCEPT
	};
	private int yy_cmap[] = unpackFromString(1,130,
"3:9,4,6,3,4,1,3:18,4,3,48,38,3:4,40,41,5,39,3,39,3,2,36,35,36,34:3,37:2,33:" +
"2,3:7,32:4,47,32:2,45,28,3:5,17,11,44,21,7,27,3,23,46,3:4,49,3:4,14,38,18,3" +
"0,16,31,10,12,29,3:2,24,26,9,8,22,3,13,15,19,25,20,3:4,42,3,43,3:2,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,122,
"0,1,2,3,1,4,1:8,5,1:20,6,1,7,8,9,10,11,4,12,13,14,15,16,17,18,19,20,21,22,2" +
"3,24,25,4,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47" +
",48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,37,70,71" +
",72,73,74,75,76,77,78,79,80,81,70,82,83,84,85,86,87")[0];

	private int yy_nxt[][] = unpackFromString(88,50,
"1,2,3,36,2,36,2,41,36:3,44,36:5,46,36:3,48,50,52,36:2,54,56,58,36,60,62,4,5" +
",6,38,6,42,7,8,9,10,11,12,64,66,68,37,13,36,-1:51,2,-1:2,2,-1,2,-1:45,14,-1" +
":2,15,-1:77,16:5,-1:14,14:4,-1,14:43,-1:9,63,-1:69,61,-1:55,57:2,-1:15,34,-" +
"1:60,120,-1:44,35,-1:60,121,-1:42,40,-1:52,65,-1:6,67,-1:45,43,-1:45,69,-1:" +
"51,45,-1:57,70,-1:54,47,-1:35,114,-1:42,49,-1:67,71,-1:39,51,-1:64,73,-1:34" +
",53,-1:42,113,-1:48,76,-1:57,55,-1:43,77,-1:47,110,-1:51,17,-1:64,111,-1:43" +
",18,-1:44,112,-1:51,116,-1:45,59,-1:46,80,-1:65,81,-1:46,83,-1:46,117,-1:44" +
",84,-1:48,86,-1:67,19,-1:42,87,-1:37,119,-1:52,88,-1:54,89,-1:37,20,-1:67,9" +
"1,-1:45,92,-1:8,93,-1:26,21,-1:66,94,-1:40,20,-1:52,95,-1:46,19,-1:49,22,-1" +
":49,23,-1:52,24,-1:46,25,-1:62,97,-1:28,98,-1:65,100,-1:41,101,-1:45,19,-1:" +
"51,102,-1:48,103,-1:62,104,-1:42,20,-1:43,19,-1:55,85,-1:46,105,-1:42,106,-" +
"1:59,26,-1:30,1,-1,27:4,28,27:41,29,30,1,31:8,32,31:9,32,31:11,32,31:16,32:" +
"2,1,33:4,39,33:44,-1:13,115,-1:50,74,-1:59,75,-1:40,72,-1:42,82,-1:55,90,-1" +
":48,118,-1:61,99,-1:43,96,-1:44,78,-1:49,79,-1:35");

	public java_cup.runtime.Symbol next_token ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

    /* code below is executed when the end-of-file is reached */
    switch(yy_lexical_state) {
    case YYINITIAL:
	// if in YYINITIAL when EOF occurs then no error
	break;
    case YYMLCOMMENT:
        //if in YYMLCOMMENT when EOF occurs, then we have an unterminated
        //multi-line comment and we return an error
        yybegin(YYINITIAL);
        return new Symbol(TokenIds.LEX_ERROR, new Token("LEX_ERROR", "Unterminated multi-line comment", yyline));
    // if defining other states then might want to add other cases here...
    }
    // if we reach here then we should either start lexing the next
    // file (if there are more files to lex) or return EOF (if we're
    // at the file)
    if (multipleFiles && fileCnt < fileReaders.length - 1) {
	// more files to lex so update yy_reader and yyline and then continue
	yy_reader = fileReaders[++fileCnt];
	yyline = 1;
	continue;
    }
    // if we reach here, then we're at the last file so we return EOF
    // to parser
    return new Symbol(TokenIds.EOF, new Token("EOF", yyline));
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{}
					case -3:
						break;
					case 3:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -4:
						break;
					case 4:
						{ return new Symbol(TokenIds.NOTE, 
						    new Token("NOTE", yytext(), yyline)); }
					case -5:
						break;
					case 5:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -6:
						break;
					case 6:
						{ return new Symbol(TokenIds.OCTAVEINTCONST, 
						    new Token("OCTAVEINTCONST", yytext(), yyline)); }
					case -7:
						break;
					case 7:
						{ return new Symbol(TokenIds.ACCIDENTAL, 
						    new Token("ACCIDENTAL", yytext() ,yyline)); }
					case -8:
						break;
					case 8:
						{ return new Symbol(TokenIds.OCTAVEMODVAL, 
						    new Token("OCTAVEMODVAL", yytext(), yyline)); }
					case -9:
						break;
					case 9:
						{ return new Symbol(TokenIds.LPAREN, 
						    new Token("LPAREN", "(", yyline)); }
					case -10:
						break;
					case 10:
						{ return new Symbol(TokenIds.RPAREN,
                                                    new Token("RPAREN", ")", yyline)); }
					case -11:
						break;
					case 11:
						{ return new Symbol(TokenIds.LBRACE,
                                                    new Token("LBRACE", "{", yyline)); }
					case -12:
						break;
					case 12:
						{ return new Symbol(TokenIds.RBRACE,
                                                    new Token("RBRACE", "}", yyline)); }
					case -13:
						break;
					case 13:
						{ currStringConst = new StringBuffer();
                          yybegin(YYINSTRING); }
					case -14:
						break;
					case 14:
						{ }
					case -15:
						break;
					case 15:
						{yybegin(YYMLCOMMENT);}
					case -16:
						break;
					case 16:
						{ return new Symbol(TokenIds.TEMPOINTCONST, 
						    new Token("TEMPOINTCONST", yytext(), yyline)); }
					case -17:
						break;
					case 17:
						{ return new Symbol(TokenIds.SONG, 
						    new Token("SONG", yyline)); }
					case -18:
						break;
					case 18:
						{ return new Symbol(TokenIds.REST, 
						    new Token("REST", yytext(), yyline)); }
					case -19:
						break;
					case 19:
						{ return new Symbol(TokenIds.LENGTH, 
												    new Token("LENGTH", yytext(), yyline)); }
					case -20:
						break;
					case 20:
						{ return new Symbol(TokenIds.VOLUMEWORD, 
						    new Token("VOLUMEWORD", yytext(), yyline)); }
					case -21:
						break;
					case 21:
						{ return new Symbol(TokenIds.TEMPO,
							new Token("TEMPO", yyline)); }
					case -22:
						break;
					case 22:
						{ return new Symbol(TokenIds.PHRASE, 
						    new Token("PHRASE", yyline)); }
					case -23:
						break;
					case 23:
						{ return new Symbol(TokenIds.OCTAVE,
							new Token("OCTAVE", yyline)); }
					case -24:
						break;
					case 24:
						{ return new Symbol(TokenIds.REPEAT,
							new Token("REPEAT", yyline)); }
					case -25:
						break;
					case 25:
						{ return new Symbol(TokenIds.VOLUME,
							new Token("VOLUME", yyline)); }
					case -26:
						break;
					case 26:
						{ return new Symbol(TokenIds.INSTRUMENT,
							new Token("INSTRUMENT",yyline));}
					case -27:
						break;
					case 27:
						{currStringConst.append(yytext());}
					case -28:
						break;
					case 28:
						{ yybegin(YYINITIAL);
                            return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", "Unterminated or Multi-line string", yyline)); }
					case -29:
						break;
					case 29:
						{ yybegin(YYINITIAL);
                          if(currStringConst.length() > 5000){
                              return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", "String too long",yyline));
                          }
                          return new Symbol(TokenIds.STRING_CONST, 
						    new Token("STRING_CONST", currStringConst.toString() , yyline));}
					case -30:
						break;
					case 30:
						{ yybegin(YYSTR_ESCAPE);}
					case -31:
						break;
					case 31:
						{ yybegin(YYINSTRING);
                            return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", "Unrecognized escape character in string", yyline)); }
					case -32:
						break;
					case 32:
						{ currStringConst.append("\\"+yytext());
                           yybegin(YYINSTRING);}
					case -33:
						break;
					case 33:
						{}
					case -34:
						break;
					case 34:
						{yybegin(YYINITIAL);}
					case -35:
						break;
					case 36:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -36:
						break;
					case 37:
						{ return new Symbol(TokenIds.NOTE, 
						    new Token("NOTE", yytext(), yyline)); }
					case -37:
						break;
					case 38:
						{ return new Symbol(TokenIds.OCTAVEINTCONST, 
						    new Token("OCTAVEINTCONST", yytext(), yyline)); }
					case -38:
						break;
					case 39:
						{}
					case -39:
						break;
					case 41:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -40:
						break;
					case 42:
						{ return new Symbol(TokenIds.OCTAVEINTCONST, 
						    new Token("OCTAVEINTCONST", yytext(), yyline)); }
					case -41:
						break;
					case 44:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -42:
						break;
					case 46:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -43:
						break;
					case 48:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -44:
						break;
					case 50:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -45:
						break;
					case 52:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -46:
						break;
					case 54:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -47:
						break;
					case 56:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -48:
						break;
					case 58:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -49:
						break;
					case 60:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -50:
						break;
					case 62:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -51:
						break;
					case 64:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -52:
						break;
					case 66:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -53:
						break;
					case 68:
						{ return new Symbol(TokenIds.LEX_ERROR, 
						    new Token("LEX_ERROR", ("Unrecognized character: " + yytext()), yyline)); }
					case -54:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
