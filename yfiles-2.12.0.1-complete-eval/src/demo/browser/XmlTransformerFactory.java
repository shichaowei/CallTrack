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

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.io.IOException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * TODO: add documentation
 *
 */
class XmlTransformerFactory
{
  private static final String CODE_2_HTML = "resource/code2html.xsl";
  private static final String DEMO_2_HTML = "resource/demo2html.xsl";
  private static final String DESC_2_TOOLTIP = "resource/desc2tooltip.xsl";
  private static final String PACKAGE_2_HTML = "resource/package2html.xsl";
  private static final String PLAIN = "demo.browser.XmlTransformerFactory.PLAIN";
  private static final Map transformers = new HashMap();

  static synchronized Transformer tooltip() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(DESC_2_TOOLTIP);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(DESC_2_TOOLTIP));
      transformers.put(DESC_2_TOOLTIP, t);
    }
    return t;
  }

  static synchronized Transformer code() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(CODE_2_HTML);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(CODE_2_HTML));
      transformers.put(CODE_2_HTML, t);
    }
    return t;
  }

  static synchronized Transformer demo() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(DEMO_2_HTML);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(DEMO_2_HTML));
      transformers.put(DEMO_2_HTML, t);
    }
    return t;
  }

  static synchronized Transformer pkg() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(PACKAGE_2_HTML);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(PACKAGE_2_HTML));
      transformers.put(PACKAGE_2_HTML, t);
    }
    return t;
  }

  static synchronized Transformer plain() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(PLAIN);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer();
      transformers.put(PLAIN, t);
    }
    return t;
  }

  private static Source source( final String key ) throws TransformerConfigurationException
  {
    URL resource = XmlTransformerFactory.class.getResource(key);
    if (resource == null) {
      String message = "Cannot locate resource in classpath: " + XmlTransformerFactory.class.getPackage().getName().replace( '.', '/' ) + '/' + key;
      throw new TransformerConfigurationException(message );
    }
    try {
      return new StreamSource( resource.openStream() );
    } catch (IOException ioe) {
      throw new TransformerConfigurationException(ioe);
    }
  }

  private XmlTransformerFactory()
  {
  }
}