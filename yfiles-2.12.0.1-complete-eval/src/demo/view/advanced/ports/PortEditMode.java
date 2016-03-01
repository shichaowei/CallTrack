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
package demo.view.advanced.ports;

import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.MouseInputMode;
import y.view.MovePortMode;
import y.view.SelectionBoxMode;
import y.view.ViewMode;

/**
 * Customized {@link y.view.EditMode} that takes {@link y.view.NodePort}s
 * into account.
 *
 */
class PortEditMode extends EditMode {
  PortEditMode() {
    setCyclicSelectionEnabled(true);
  }

  /**
   * Overwritten to create a {@link demo.view.advanced.ports.NodePortPopupMode}
   * instance.
   * @return a {@link demo.view.advanced.ports.NodePortPopupMode}
   * instance.
   */
  protected ViewMode createPopupMode() {
    return new NodePortPopupMode();
  }

  /**
   * Overwritten to create a {@link y.view.MovePortMode} instance that allows
   * reassigning edges to new nodes.
   * @return a {@link y.view.MovePortMode} instance.
   */
  protected ViewMode createMovePortMode() {
    final MovePortMode mpm = new MovePortMode();
    mpm.setChangeEdgeEnabled(true);
    return mpm;
  }

  /**
   * Overwritten to create a {@link y.view.CreateEdgeMode} instance that
   * visually indicates target nodes and ports for newly created edges.
   * @return a {@link y.view.CreateEdgeMode} instance.
   */
  protected ViewMode createCreateEdgeMode() {
    final CreateEdgeMode cem = new PortCreateEdgeMode();
    cem.setIndicatingTargetNode(true);
    return cem;
  }

  /**
   * Overwritten to create a {@link y.view.MouseInputMode} instance
   * that supports interactive selecting/deselecting and moving of
   * {@link y.view.NodePort} instances.
   * @return a {@link y.view.MouseInputMode} instance.
   */
  protected MouseInputMode createMouseInputMode() {
    final MouseInputMode mim = new MouseInputMode();
    mim.setNodeSearchingEnabled(true);
    return mim;
  }

  /**
   * Overwritten to create a {@link y.view.SelectionBoxMode} instance
   * that supports selecting {@link y.view.NodePort} instances and
   * {@link y.view.YLabel} instances as well as {@link y.base.Node},
   * {@link y.base.Edge}, and {@link y.view.Bend} instances.
   * @return a {@link y.view.SelectionBoxMode} instance.
   */
  protected ViewMode createSelectionBoxMode() {
    final SelectionBoxMode sbm = new SelectionBoxMode();
    sbm.setExtendedTypeSelectionEnabled(true);
    return sbm;
  }
}
