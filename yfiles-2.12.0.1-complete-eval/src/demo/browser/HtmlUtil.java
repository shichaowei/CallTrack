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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Extracts the displayed text from HTML formatted strings.
 */
class HtmlUtil {
  static String getText( final String html ) {
    if (html == null) {
      return null;
    } else {
      try {
        final Parser parser = new Parser();
        final ParserDelegator driver = new ParserDelegator();
        driver.parse(new StringReader(html), parser, true);
        return parser.toString().trim();
      } catch (IOException e) {
        return "";
      }
    }
  }

  private static final class Parser extends HTMLEditorKit.ParserCallback {
    final StringBuffer sb;
    final HashSet startNewlines;
    boolean active;

    Parser() {
      sb = new StringBuffer();
      startNewlines = new HashSet();
      startNewlines.add(HTML.Tag.DL);
      startNewlines.add(HTML.Tag.OL);
      startNewlines.add(HTML.Tag.TABLE);
      startNewlines.add(HTML.Tag.UL);
    }

    public void handleStartTag( final HTML.Tag t, final MutableAttributeSet a, final int pos ) {
      if (HTML.Tag.BODY.equals(t)) {
        active = true;
      } else if (startNewlines.contains(t)) {
        sb.append("\n");
      }
    }

    public void handleEndTag( final HTML.Tag t, final int pos ) {
      if (t.breaksFlow() || t.isBlock()) {
        sb.append("\n");
      }
    }

    public void handleSimpleTag( final HTML.Tag t, final MutableAttributeSet a, final int pos ) {
      if (t.breaksFlow()) {
        sb.append("\n");
      }
    }

    public void handleText( final char[] data, final int pos ) {
      if (active) {
        sb.append(data);
      }
    }

    public String toString() {
      return sb.toString();
    }
  }
}
