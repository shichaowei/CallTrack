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

/**
 * Handles data from a GEDCOM file input.
 * <p/>
 * As the GEDCOM standard defines a format with a tree structure, this handler provides some callbacks to react to the
 * start and end of a level and the input as well. Because of these callbacks the handler does not have to take care of
 * the hierarchic structure.
 */
public interface GedcomInputHandler {

  /**
   * Callback to handle the begin of the GEDCOM file.
   */
  public void handleStartDocument();

  /**
   * Callback to handle the end of the GEDCOM file.
   */
  public void handleEndDocument();

  /**
   * Callback to handle the begin of a new level labeled by a given tag.
   *
   * @param id    the id from the GEDCOM line (might be <code>null</code>)
   * @param tag   the tag from the GEDCOM line
   * @param value the value from the GEDCOM line (might be <code>null</code>)
   */
  public void handleStartTag(int level, String id, String tag, String value);

  /**
   * Callback to handle the end of a level named by a given tag.
   *
   * @param tag the name of ending level
   */
  public void handleEndTag(int level, String tag);
}
