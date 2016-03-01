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

import y.io.IOHandler;
import y.view.Graph2D;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reads GEDCOM files into graphs. GEDCOM is a widely used format to store family trees, see
 * http://www.phpgedview.net/ged551-5.pdf for the most recent specifications.
 * <p/>
 * The reader uses a {@link GedcomInputParser} to fill the data from the GEDCOM file into a graph structure.
 */
public class GedcomHandler extends IOHandler {

  /**
   * Returns <code>false</code>, since writing of GEDCOM files is not supported.
   */
  public boolean canWrite() {
    return false;
  }

  /**
   * Throws an <code>UnsupportedOperationException</code>, since writing of GEDCOM files is not supported.
   */
  public void write(Graph2D graph, OutputStream out) throws IOException {
    throw new UnsupportedOperationException("No Gedcom export");
  }

  /**
   * Reads a GEDCOM file into the given graph graph.
   * <p/>
   * This implementation calls a <code>GedcomInputParser</code> to read the GEDCOM file into a graph.
   *
   * @param graph the graph to be built
   * @param in    the stream from the file
   * @throws IOException
   */
  public void read(final Graph2D graph, InputStream in) throws IOException {
    new GedcomInputParser().parse(in, createGedcomHandler(graph));
  }

  /**
   * Returns a descriptive string of the GEDCOM file format.
   */
  public String getFileFormatString() {
    return "GEDCOM (Genealogical Data)";
  }

  /**
   * Returns <code>ged</code>.
   */
  public String getFileNameExtension() {
    return "ged";
  }

  /**
   * Creates a handler for the <code>GedcomInputParser</code> that handles the tags from the GEDCOM file.
   *
   * @param graph the graph to be built
   * @return the handler that handles the GEDCOM tags
   */
  public GedcomInputHandler createGedcomHandler(final Graph2D graph) {
    return new GedcomInputHandlerImpl(graph);
  }
}
