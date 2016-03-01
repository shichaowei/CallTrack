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

/**
 * TODO: add documentation
 *
 */
public final class XmlTags
{
  public static final String DEFAULT_NS_URI = "http://www.w3.org/1999/xhtml";
  public static final String YWORKS_NS_URI = "http://www.yworks.com/demo";
  public static final String YWORKS_NS_PREFIX = "y";

  public static final String ATTRIBUTE_NAME = "javaname";
  public static final String ATTRIBUTE_SOURCE = "source";
  public static final String ATTRIBUTE_BROWSER = "browser";
  public static final String ATTRIBUTE_EXECUTABLE = "executable";
  public static final String ATTRIBUTE_DEFAULT_NS = "xmlns";
  public static final String ATTRIBUTE_YWORKS_NS = "xmlns:" + YWORKS_NS_PREFIX;

  public static final String ELEMENT_NAME = "displayname";
  public static final String ELEMENT_PACKAGE = "package";
  public static final String ELEMENT_DESCRIPTION = "description";
  public static final String ELEMENT_SUMMARY = "summary";
  public static final String ELEMENT_DEMO = "demo";
  public static final String ELEMENT_HTML = "html";
  public static final String ELEMENT_PRIORITY = "displaypriority";
  public static final String ELEMENT_KEYWORDS = "keywords";
  public static final String ELEMENT_KEYWORD = "keyword";

  private XmlTags()
  {
  }
}
