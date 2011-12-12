/*************************************************************************
 *  Compilation:  javac TokenReader.java
 *  Execution:    java TokenReader
 *
 *  Reads in data of various types from standard input.
 *
 *  Portions of this class were derived from code supplied with
 *        Introduction to Programming in Java:  An Interdisciplinary Approach,
 *        Robert Sedgewick and Kevin Wayne, Addison-Wesley, 2007. 
 *        ISBN 0-321-49805-4
 *        http://www.cs.princeton.edu/IntroProgramming
 *
 *   Specifically, this class was derived from StdIn, which was based on the java 1.5's 
 *   Tokenizer. This system is required to work with earlier versions of java, so a new
 *   token-based system has been implemented here.
 *
 * Todo:
 *    - ability to push tokens back onto the reader and read them later.
 *
 *************************************************************************/

/***************************************************
 * Copyright 2007 by Simran Gleason,               *
 *                   Robert Sedgewick, Kevin Wayne *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class TokenReader {

    private BufferedReader reader = null;
    private boolean atEol = false;
    private boolean canClose = false;
    private ArrayList tokenStack = null;

    private String lastTokenRead = null;
    private boolean ignoreCommentLines = true;

    public TokenReader() {
        lastTokenRead = null;
    }
    public TokenReader(String filename) {
        lastTokenRead = null;
        if (filename.startsWith("http://") || filename.startsWith("file://")) {
            openHttp(filename);
        } else {
            open(filename);
        }
    }

    public TokenReader(InputStream in) {
        setInputStream(in);
    }

    public TokenReader(Reader in) {
        setReader(in);
    }

    public static TokenReader makeStringTokenReader(String input) {
        TokenReader tr = new TokenReader(new StringReader(input));
        tr.canClose = true;
        return tr;
    }
    
    public void setIgnoreCommentLines(boolean val) {
        ignoreCommentLines = val;
    }
    
    public void setInputStream(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
        lastTokenRead = null;
        clearTokenStack();
    }

    public void setReader(Reader reader) {
        this.reader = new BufferedReader(reader);
        lastTokenRead = null;
        clearTokenStack();
    }


    public void open(String filename) {
        if (filename.startsWith("http://") || filename.startsWith("file://")) {
            openHttp(filename);
            lastTokenRead = null;
            clearTokenStack();
            return;
        } 
        try {
            reader = new BufferedReader(new FileReader(filename));
            lastTokenRead = null;
            clearTokenStack();
            canClose = true;
        } catch (Exception ex) {
            System.out.println("TokenReader Caught exception opening file[" + filename + "]. " + ex);
            ex.printStackTrace();
        }
    }

    public void openHttp(String urlstr) {
        try {
            URL url = new URL(urlstr);
            InputStream urlstream = url.openStream();
            setInputStream(urlstream);
            canClose = true;
        } catch (Exception ex) {
            System.out.println("TokenReader Caught exception opening url[" + urlstr + "]. " + ex);
            ex.printStackTrace();
        }
    }

    public void close() {
        if (canClose) {
            try {
                reader.close();
            } catch (Exception ex) {
                System.out.println("TokenReader Caught exception trying to close().  " + ex);
                ex.printStackTrace();
            }
        }
    }

    public boolean atEol() {
        return atEol;
    }

    // whitespace-separated string. 
    public String  readToken()   throws IOException  {
        if (!tokenStackEmpty()) {
            lastTokenRead = tokenStackPop();
            return lastTokenRead;
        }
        StringBuffer buf = new StringBuffer();
        int ch = reader.read();
        boolean tokenfound = false;
        while(ch == ' ' || ch== '\n' || ch == '\t') {
            //System.out.println(" ch: [" + ch + "][" + (int)ch + "]");
            ch = reader.read();
        }
        if (ch == 0 || ch == '\n') {
            atEol = true;
        }
        while (ch > 0 && ch != ' ' && ch != '\n' && ch != '\t') {
            //System.out.println(" ch: [" + ch + "][" + (int)ch + "]");
            buf.append((char)ch);
            tokenfound = true;
            atEol = false;
            ch = reader.read();
        }
        if (ch == 0 || ch == '\n') {
            atEol = true;
        }

        if (tokenfound) {
            String token = buf.toString();
            if (ignoreCommentLines) {
                if (token.startsWith("//")) {
                    String comment = readLine();
                    //System.out.println(" Ignoring comment: [//" + comment + "]");
                    return readToken();
                }
            }
            lastTokenRead = token;
            return lastTokenRead;
        } else {
            return null;
        }
    }

    /**
     * Read until the target token is found (ignoring case),
     * then return the actual token read, or null if none is found.
     */
    public String readUntilToken(String targetToken) throws IOException {
        String token = readToken();
        while (token != null && !token.equalsIgnoreCase(targetToken)) {
            token = readToken();
        }
        return token;
    }

    /**
     * Read until the target token is found (ignoring case),
     * gathering the tokens along the way
     * then return the concatenation of the gathered tokens (turning all whitespace into
     * single spaces).
     * If (inclusive), concatenate the actual token on to the result. 
     */
    public String gatherUntilToken(String targetToken, boolean inclusive) throws IOException {
        StringBuffer buf = new StringBuffer();
        String token = readToken();
        boolean first = true;
        while (token != null && !token.equalsIgnoreCase(targetToken)) {
            if (!first) {
                buf.append(' '); // TODO: use tha actual whitespace found.
            }
            first = false;
            buf.append(token);
            token = readToken();
        }
        if (inclusive && token != null) {
            if (!first) {
                buf.append(' '); // TODO: use the actual whitespace found.
            }
            buf.append(token);
        }
        return buf.toString();
    }

    /**
     * Read until the target token is found (ignoring case),
     * gathering the tokens along the way
     * then return a list of the gathered tokens 
     * If (inclusive), add the actual token to the result. 
     */
    public List gatherTokensUntilToken(String targetToken, boolean inclusive) throws IOException {
        List list = new ArrayList();
        String token = readToken();
        boolean first = true;
        while (token != null && !token.equalsIgnoreCase(targetToken)) {
            list.add(token);
            token = readToken();
        }
        if (inclusive && token != null) {
            list.add(token);
        }
        return list;
    }

    public void pushToken() {
        if (lastTokenRead != null) {
            pushToken(lastTokenRead);
            lastTokenRead = null;
        }
    }
    
    public void pushToken(String token) {
        if (tokenStack == null) {
            makeTokenStack();
        }
        tokenStackPush(token);
    }

    public String getLastTokenRead() {
        return lastTokenRead;
    }
    
    
    public String  readTokenBeforeEol()   throws IOException  {
        if (atEol()) {
            return null;
        }
        return readToken();
    }

    
    public int     readInt()      throws IOException, NumberFormatException  {
        String t = readToken();
        return Integer.parseInt(t);
    }

    public float   readFloat()   throws IOException, NumberFormatException  { 
        String t = readToken();
        return Float.parseFloat(t);
    }

    public double  readDouble()   throws IOException, NumberFormatException  { 
        String t = readToken();
        return Double.parseDouble(t);
    }

    public boolean readBoolean()   throws IOException, NumberFormatException  {
        String t = readToken();
        return t.equalsIgnoreCase("true");
    }
    /*
      public float   readFloat()    throws IOException  { return reader.readFloat();     }
      public long    readLong()     throws IOException  { return reader.readLong();      }
      public byte    readByte()     throws IOException  { return reader.readByte();      }
      public boolean readBoolean()  throws IOException  { return reader.readBoolean();   }
    */
    // read until end of line
    // note: readline doesn't take into account the pushed token stack!!
    public String readLine()      throws IOException  {
        String rl = reader.readLine();
        atEol = true;
        return rl;
    }

    // return rest of input as string
    /*
      public String readAll() {
          if (!scanner.hasNextLine()) return null;

          // reference: http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
          return scanner.useDelimiter("\\A").next();
      }
    */

    private void clearTokenStack() {
        tokenStack = null;
    }
    private void makeTokenStack() {
        tokenStack = new ArrayList(10);
    }

    private String tokenStackTop() {
        if (tokenStack == null) {
            return null;
        }
        int sz = tokenStack.size();
        if (sz > 0) {
            return (String)tokenStack.get(sz - 1);
        }
        return null;
    }

    private String tokenStackPop() {
        int sz = tokenStack.size();
        if (sz > 0) {
            String top = (String)tokenStack.remove(sz - 1);
            return top;
        }
        return null;
    }

    private void tokenStackPush(String plate) {
        if (tokenStack == null) {
            makeTokenStack();
        }
        tokenStack.add(plate);
    }

    private boolean tokenStackEmpty() {
        return tokenStack == null || tokenStack.size() <= 0;
    }
  
    public String printTokenStack() {
        StringBuffer buf = new StringBuffer();
        if (tokenStackEmpty()) {
            buf.append("EMPTY");
        } else {
            for(int i=tokenStack.size()-1; i >= 0; i--) {
                buf.append(tokenStack.get(i));
                buf.append(":");
            }
        }
        return buf.toString();
    }

    // This method is just here to test the class
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                String file = args[0];
                TokenReader in = new TokenReader(file);
                boolean done = false;
                while (!done) {
                    String t = in.readToken();
                    if (t == null) {
                        done = true;
                    } else {
                        System.out.println("token: [" + t + "]");
                    }
                }
            } else {

                System.out.println("Type a token: ");
                TokenReader in = new TokenReader(System.in);
                String s = in.readToken();
                System.out.println("Your token was: " + s);
                System.out.println();

                System.out.println("Type an int: ");
                int a = in.readInt();
                System.out.println("Your int was: " + a);
                System.out.println();

                System.out.println("Type a boolean: ");
                boolean b = in.readBoolean();
                System.out.println("Your boolean was: " + b);
                System.out.println();
            }
        } catch (Exception ex) {
        }
    }

}
