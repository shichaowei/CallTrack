/****************************************************************************
 * This demo file is part of yFiles for Java 2.12.0.1.
 * Copyright (c) 2000-2016 by yWorks GmbH, Vor dem Kreuzberg 28,
 * 72070 Tuebingen, Germany. All rights reserved.
 * 
 * yFiles demo files exhibit yFiles for Java functionalities. Any redistribution
 * of demo files in source code or binary form, with or without
 * modification, is not permitted.
 * 
 * Owners of a valid software license for a yFiles for Java version that this
 * demo is shipped with are allowed to use the demo source code as basis
 * for their own yFiles for Java powered applications. Use of such programs is
 * governed by the rights and conditions as set out in the yFiles for Java
 * license agreement.
 * 
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL yWorks BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***************************************************************************/
package demo.layout.genealogy.iohandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser that reads GEDCOM files into a graph.
 * <p/>
 * This implementation extracts the hierarchical structure and the data values of each line from the GEDCOM file. Then,
 * it passes the information to a handler which builds the graph from the information in the preprocessed lines.
 * <p/>
 * There are several encodings supported that are specified in the GEDCOM standard: UTF8, UTF16, ANSEL, ASCII and ANSI.
 */
public class GedcomInputParser {
  public static final String ENCODING_ANSEL = "ANSEL";
  public static final String ENCODING_UTF_8 = "UTF-8";
  public static final String ENCODING_UTF_16LE = "UTF-16LE";
  public static final String ENCODING_UTF_16BE = "UTF-16BE";
  public static final String ENCODING_ASCII = "ASCII";
  private static final String ENCODING_ANSI = "ANSI";

  static final String GEDCOM_ENCODING_ERROR_MESSAGE =
      "GEDCOM - invalid encoding: The encoding of the file should be UTF-8, UTF-16BE, UTF-16LE, ANSEL, ASCII, or ANSI.";

  /**
   * Extracts the information of each line of the GEDCOM file and passes it to the given handler.
   * <p/>
   * Before parsing the encoding of the file is detected.
   *
   * @param inStream the <code>InputStream</code> from the GEDCOM file
   * @param handler  the handler which uses the extracted information to build a graph
   * @throws IOException if the stream is not readable
   */
  public void parse(InputStream inStream, GedcomInputHandler handler) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String encoding = readEncoding(inStream, baos);
    ResettingInputStream in = new ResettingInputStream(inStream, baos.toByteArray());

    if (encoding.equals(ENCODING_ANSEL)) {
      parseLines(new AnselInputStreamReader(in), handler);
    } else if (encoding.equals(ENCODING_UTF_8)) {
      parseLines(new InputStreamReader(in, "UTF-8"), handler);
    } else if (encoding.equals(ENCODING_UTF_16LE)) {
      parseLines(new InputStreamReader(in, "UTF-16LE"), handler);
    } else if (encoding.equals(ENCODING_UTF_16BE)) {
      parseLines(new InputStreamReader(in, "UTF-16BE"), handler);
    } else if (encoding.equals(ENCODING_ASCII)) {
      parseLines(new InputStreamReader(in, "ASCII"), handler);
    } else if (encoding.equals(ENCODING_ANSI)) {
      parseLines(new InputStreamReader(in, "windows-1252"), handler);
    } else {
      throw new IOException(GEDCOM_ENCODING_ERROR_MESSAGE);
    }
  }

  /**
   * Detects the encoding of the GEDCOM file.
   * <p/>
   * While searching for the encoding references, the bytes that are already read from the stream are stored in an
   * <code>ByteArrayOutputStream</code>. So these bytes are still available for reading.
   *
   * @param inStream the stream from the GEDCOM file
   * @param baos     the storage for bytes that are already read
   * @return an identifier for the files encoding
   * @throws IOException if the file does not fit the GEDCOM standard
   */
  static String readEncoding(InputStream inStream, ByteArrayOutputStream baos) throws IOException {
    String encoding = null;
    int firstByte = inStream.read();
    if(firstByte < 0) {
      throw new IOException(GEDCOM_ENCODING_ERROR_MESSAGE);
    }
    int secondByte = inStream.read();
    if(secondByte < 0) {
      throw new IOException(GEDCOM_ENCODING_ERROR_MESSAGE);
    }

    if (firstByte == 0xef && secondByte == 0xbb) {
      inStream.read();
      encoding = ENCODING_UTF_8;
    } else if (firstByte == 0xfe && secondByte == 0xff) {
      encoding = ENCODING_UTF_16BE;
    } else if (firstByte == 0xff && secondByte == 0xfe) {
      encoding = ENCODING_UTF_16LE;
    } else if (firstByte == 0x00 && secondByte == 0x30) {
      baos.write(firstByte);
      baos.write(secondByte);
      encoding = ENCODING_UTF_16BE;
    } else if (firstByte == 0x30 && secondByte == 0x00) {
      baos.write(firstByte);
      baos.write(secondByte);
      encoding = ENCODING_UTF_16LE;
    } else {
      // there is no byte order mark
      // according to the specification, the first line has to be "0 HEAD"
      // this means firstByte corresponds to "0" and secondByte to " " 
      String line;
      baos.write(firstByte);
      baos.write(secondByte);
      while ((line = readLine(inStream, baos)) != null) {
        if (line.startsWith("1 CHAR")) {
          encoding = line.trim().substring(line.lastIndexOf(" ") + 1);
        } else if (line.startsWith("0 ")) {
          // since firstByte and secondByte stripped "0 " away from the first
          // line, the next occurrence of the "0 " prefix signals the start
          // of the first non-header top-level section and thus the end of
          // the header section
          // according to the specification, the CHAR directive has to be
          // in the header section, so it is ok to stop looking for the CHAR
          // directive now
          break;
        }
      }
    }
    if (encoding == null) {
      throw new IOException(GEDCOM_ENCODING_ERROR_MESSAGE);
    }
    return encoding;
  }

  /**
   * Reads a line from a file byte-wise and stores the read bytes.
   *
   * @param inStream the stream from the GEDCOM file
   * @param baos     the storage for bytes that are already read
   * @return the read line
   * @throws IOException if the file does not fit the GEDCOM standard
   */
  static String readLine(InputStream inStream, ByteArrayOutputStream baos) throws IOException {
    final int lastByte = baos.toByteArray()[baos.size() - 1];
    int nextByte = readNextByte(inStream, baos);
    if (nextByte == -1) {
      return null;
    }
    ByteArrayOutputStream line = new ByteArrayOutputStream();
    if (nextByte == '\n' || nextByte == '\r') {
      if (nextByte != lastByte) {
        nextByte = readNextByte(inStream, baos);
      } else {
        throw new IOException(
            "GEDCOM - invalid line terminator: All lines in the file should end with \\n, \\r, \\n\\r or \\r\\n.");
      }
    }
    while (nextByte != '\n' && nextByte != '\r' && nextByte != -1) {
      line.write(nextByte);
      nextByte = readNextByte(inStream, baos);
    }
    return line.toString();
  }

  /**
   * Reads a byte from a file and stores it in an <code>ByteArrayOutputStream</code>.
   *
   * @param inStream the stream from the GEDCOM file
   * @param baos     the storage for bytes that are already read
   * @return the read byte
   * @throws IOException if the file does not fit the GEDCOM standard
   */
  static int readNextByte(InputStream inStream, ByteArrayOutputStream baos) throws IOException {
    int nextByte = inStream.read();
    if (nextByte > -1) {
      baos.write(nextByte);
    }
    return nextByte;
  }

  /**
   * Extracts the fields level, id, tag and value from each line of the GEDCOM file and passes these fields to a handler
   * that fills them into a graph structure.
   *
   * @param reader  the stream from the GEDCOM file
   * @param handler a handler that builds the graph
   * @throws IOException if the file does not fit the GEDCOM standard
   */
  static void parseLines(InputStreamReader reader, GedcomInputHandler handler) throws IOException {
    final ParseContext parseContext = new ParseContext(handler);
    BufferedReader buffer = new BufferedReader(reader);
    int level;
    String id, tag, value;

    // gedcom file must start with "0 HEAD"
    String line = buffer.readLine();
    if (!"0 HEAD".equals(line)) {
      throw new IOException("GEDCOM - missing header: The file must start with \"0 HEAD\"");
    }

    parseContext.handleStartDocument();

    // gedcom line: LevelNumber [ID ] Tag[ LineValue]
    Pattern gedcomLinePattern = Pattern.compile("([0-9]{1,3}) ((@\\w[ \\p{Graph}&&[^%@]]*@) )?(_?[A-Z0-9]{2,4}) ?(.*)?");
    Matcher matcher;

    while (line != null) {
      ++parseContext.line;

      //if line is a gedcom line
      matcher = gedcomLinePattern.matcher(line);
      if (matcher.matches()) {
        level = Integer.parseInt(matcher.group(1));
        id = matcher.group(3);
        tag = matcher.group(4);
        value = matcher.group(5);

        line = buffer.readLine();

        if ("TRLR".equals(tag)) {
          line = null;
        }

        handleLine(parseContext, level, id, tag, value, line);
      } else {
        throw new IOException("GEDCOM - Invalid format at line " + parseContext.line);
      }
    }

    if (parseContext.size() > 0) {
      throw new IOException("GEDCOM - Missing end tag: The file must end with \"0 TRLR\"");
    }
    parseContext.handleEndDocument();
  }

  /**
   * Handles the hierarchic structure of the GEDCOM file and passes the lines to the handler that builds the graph.
   * <p/>
   * If the value of the GEDCOM line ranges over several lines, these lines are collected and passed as one line.
   *
   * @param context the parse context where the hierarchy of the tags and some intermediary result are stored
   * @param id      the id field of the GEDCOM line (might be <code>null</code>)
   * @param tag     the tag field of the GEDCOM line
   * @param value   the value field of the GEDCOM line (might be <code>null</code>)
   * @param peek    the next line in the file
   * @throws IOException if the file does not fit the GEDCOM standard
   */
  static void handleLine(ParseContext context, int level, String id, String tag, String value, String peek)
      throws IOException {
    if (!"CONT".equals(tag) && !"CONC".equals(tag)) {
      context.push(tag);
      context.level = level;
      context.id = id;
      context.value = new StringBuffer(50);
    }

    if ("CONT".equals(tag)) {
      context.value.append('\n');
    }
    if (value != null) {
      context.value.append(value);
    }

    if (peek != null) {
      final int currentLevel = context.size() - 1;
      if (!peek.startsWith((currentLevel + 1) + " CONT") && !peek.startsWith((currentLevel + 1) + " CONC")
          && peek.indexOf(" ") >= 0) {
        try {
          int nextLevel = Integer.parseInt(peek.substring(0, peek.indexOf(" ")));
          if (nextLevel > currentLevel) {
            if (nextLevel == currentLevel + 1) {
              context.handleStartTag(context.level, context.id, context.peek(), context.value.toString());
            } else {
              throw new IOException("GEDCOM - Invalid nesting at line " + context.line);
            }
          } else {
            context.handleStartTag(context.level, context.id, context.peek(), context.value.toString());
            context.handleEndTag(context.size() - 1, context.pop());
            for (int i = nextLevel; i < currentLevel; i++) {
              context.handleEndTag(context.size() - 1, context.pop());
            }
          }
        } catch (NumberFormatException e) {
          throw new IOException("GEDCOM - Invalid format at line " + (context.line + 1));
        }
      }
    } else {
      // stream end -> handle last line (0 TRLR)
      context.handleStartTag(context.level, context.id, context.peek(), context.value.toString());
      context.handleEndTag(context.size() - 1, context.pop());
    }
  }

  /**
   * Delegates all handler calls to a given handler and additionally provides a storage for the hierarchic information
   * of the GEDCOM file.
   */
  static final class ParseContext implements GedcomInputHandler {
    final GedcomInputHandler handler;
    final List stack;
    int line;
    int level;
    String id;
    StringBuffer value;

    ParseContext(GedcomInputHandler handler) {
      if (handler == null) {
        this.handler = new GedcomInputHandler() {
          public void handleStartDocument() {
          }

          public void handleEndDocument() {
          }

          public void handleStartTag(int level, String id, String tag, String value) {
          }

          public void handleEndTag(int level, String tag) {
          }
        };
      } else {
        this.handler = handler;
      }
      stack = new ArrayList();
    }

    public void handleEndDocument() {
      handler.handleEndDocument();
    }

    public void handleEndTag(int level, String tag) {
      handler.handleEndTag(level, tag);
    }

    public void handleStartDocument() {
      handler.handleStartDocument();
    }

    public void handleStartTag(int level, String id, String tag, String value) {
      handler.handleStartTag(level, id, tag, value);
    }

    public void push(String tag) {
      stack.add(tag);
    }

    public String pop() {
      return (String) stack.remove(stack.size() - 1);
    }

    public String peek() {
      return (String) stack.get(stack.size() - 1);
    }

    public int size() {
      return stack.size();
    }
  }

  /**
   * Reads bytes from an array and then continues with the byte from a context <code>InputStream</code>.
   * <p/>
   * This class can be used to find a specific piece of information in a file (e.g. the encoding) and then return to the
   * beginning to read the whole file.
   */
  static class ResettingInputStream extends InputStream {
    private final InputStream inStream;
    private final byte[] buffer;
    private int pos;

    ResettingInputStream(InputStream in, byte[] firstBytes) {
      inStream = in;
      buffer = firstBytes;
      pos = 0;
    }

    public int read() throws IOException {
      if (pos < buffer.length) {
        return buffer[pos++] & 0xFF;
      } else {
        return inStream.read();
      }
    }

    public int read(byte[] b, int off, int len) throws IOException {
      if (pos < buffer.length) {
        final int amount = Math.min(len, buffer.length - pos);
        System.arraycopy(buffer, pos, b, off, amount);
        pos += amount;
        return amount;
      } else {
        return inStream.read(b, off, len);
      }
    }
  }
}
