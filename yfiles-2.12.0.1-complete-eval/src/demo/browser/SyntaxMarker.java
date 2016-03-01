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
package demo.browser;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * TODO: add documentation
 *
 */
public class SyntaxMarker
{
  private static final String ELEMENT_COMMENT = "comment";
  private static final String ELEMENT_STRING = "string";
  private static final String ELEMENT_CODE = "code";

  private static final String COMMENT_PLACEHOLDER = "\n__COMMENT__\n";
  private static final String STRING_PLACEHOLDER = "\n__STRING__\n";

  private static final Pattern COMMENT_PATTERN =
          Pattern.compile("(?://[^\\n]*(?=\n))|(?s:(?:/\\*.*?\\*/))");

  private static final Pattern STRING_PATTERN =
          Pattern.compile("\".*?\"(?<!\\\\)");

  private static final Map KEYWORDS;
  static
  {
    KEYWORDS = new HashMap();
    final String[] keywords = {
            "abstract",
            "assert",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "boolean",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while"
    };
    for (int i = 0; i < keywords.length; ++i)
    {
      KEYWORDS.put(keywords[i], "<keyword>" + keywords[i] + "</keyword>");
    }
  }

  private SyntaxMarker()
  {
  }

  public static String toHtml( final String code )
  {
    String work = toXml(code);
    final StringWriter sw = new StringWriter();
    try {
      XmlTransformerFactory.code()
                           .transform(new StreamSource(new StringReader(work)),
                                      new StreamResult(sw));
      work = normalize(sw);
    } catch (TransformerException te) {
      work = "<html><head></head><body><pre>" + code + "</pre></body></html>";
    }
    return work;
  }

  private static String normalize( final StringWriter sw )
  {
    return sw.toString().replaceAll("(\\s*)xmlns(\\S*)\"", "")
                        .replaceAll("<(?i)META[^>]*>", "");
  }

  private static String toXml( final String code )
  {
    StringBuffer sb;
    String work = code;
    StringTokenizer st;

    sb = new StringBuffer(work.length());
    for (int i = 0, n = work.length(); i < n; ++i) {
      final char c = work.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    work = sb.toString();

    final List comments = new LinkedList();
    final List strings = new LinkedList();

    work = replaceAllAndCache(work, COMMENT_PATTERN, COMMENT_PLACEHOLDER, comments);
    work = replaceAllAndCache(work, STRING_PATTERN, STRING_PLACEHOLDER, strings);

    sb.setLength(0);
    st = new StringTokenizer(work, " \t\n\r\f{}[]().:;", true);
    while (st.hasMoreTokens())
    {
      final String token = st.nextToken();
      final Object replacement = KEYWORDS.get(token);
      if (replacement == null)
      {
        sb.append(token);
      }
      else
      {
        sb.append(replacement);
      }
    }

    work = sb.toString();
    final Pattern invCommentPattern = Pattern.compile(COMMENT_PLACEHOLDER);
    String prefix = "<" + ELEMENT_COMMENT + ">";
    String suffix = "</" + ELEMENT_COMMENT + ">";
    work = insertCache(work, invCommentPattern, comments, prefix, suffix);
    final Pattern invStringPattern = Pattern.compile(STRING_PLACEHOLDER);
    prefix = "<" + ELEMENT_STRING + ">";
    suffix = "</" + ELEMENT_STRING + ">";
    work = insertCache(work, invStringPattern, strings, prefix, suffix);

    work = work.trim();

    sb = new StringBuffer(work.length());
    sb.append('<').append(ELEMENT_CODE).append(">");
    for (int i = 0, n = work.length(); i < n; ++i) {
      final char c = work.charAt(i);
      switch (c) {
//        case ' ':
//          sb.append("&nbsp;");
//          break;
        case '\t':
//          sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
          sb.append("    ");
          break;
//        case '\n':
//          sb.append("<br>\n");
//          break;
        default:
          sb.append(c);
          break;
      }
    }
    sb.append("</").append(ELEMENT_CODE).append(">");

    return sb.toString();
  }

  private static String insertCache(final String s,
                                    final Pattern pattern,
                                    final List cache,
                                    final String prefix,
                                    final String suffix)
  {
    final Matcher matcher = pattern.matcher(s);
    matcher.reset();
    boolean result = matcher.find();
    if (result) {
      StringBuffer sb = new StringBuffer();
      do {
        String replacementString = (( String ) cache.remove( 0 )).replaceAll( "\\\\", "\\\\\\\\");
//        replacementString.;
        matcher.appendReplacement(sb, prefix + replacementString + suffix);
        result = matcher.find();
      } while (result);
      matcher.appendTail(sb);
      return sb.toString();
    }
    return s;
  }

  private static String replaceAllAndCache(final String s,
                                           final Pattern pattern,
                                           final String replacement,
                                           final List cache)
  {
    final Matcher matcher = pattern.matcher(s);
    matcher.reset();
    boolean result = matcher.find();
    if (result) {
      StringBuffer sb = new StringBuffer();
      do {
        cache.add(s.substring( matcher.start(), matcher.end() ) );

        matcher.appendReplacement(sb, replacement);
        result = matcher.find();
      } while (result);
      matcher.appendTail(sb);

      return sb.toString();
    }
    return s;
  }
}
