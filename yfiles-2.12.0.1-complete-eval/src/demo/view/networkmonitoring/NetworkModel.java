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
package demo.view.networkmonitoring;

import y.view.Graph2D;

/**
 * Interface for a network model
 */
public interface NetworkModel {

  /**
   * Key to register a {@link y.base.DataProvider} that contains an unique id object for every network node and connection.
   */
  public static final Object ELEMENT_ID_DPKEY = "demo.view.networkmonitoring.NetworkModelImpl.ELEMENT_ID_DPKEY";

  /**
   * Key to register a {@link y.base.DataProvider} that contains a type for each network node. The type must be one of the
   * following:
   * {@link #PC}, {@link #LAPTOP}, {@link #SMARTPHONE}, {@link #SWITCH}, {@link #WLAN}, {@link #SERVER},
   * {@link #DATABASE}.
   */
  public static final Object NODE_TYPE_DPKEY = "demo.view.networkmonitoring.NetworkModelImpl.NODE_TYPE_DPKEY";

  /** Specifier for the network element PC.*/
  public static final int PC = 1;
  /** Specifier for the network element laptop.*/
  public static final int LAPTOP = 2;
  /** Specifier for the network element smartphone.*/
  public static final int SMARTPHONE = 3;
  /** Specifier for the network element switch.*/
  public static final int SWITCH = 4;
  /** Specifier for the network element wlan router.*/
  public static final int WLAN = 5;
  /** Specifier for the network element server.*/
  public static final int SERVER = 6;
  /** Specifier for the network element database.*/
  public static final int DATABASE = 7;

  /**
   * Key to register a {@link y.base.DataProvider} that contains a capacity for each connection. The capacity must be an
   * integer value.
   */
  public static final Object ELEMENT_CAPACITY_DPKEY = "demo.view.networkmonitoring.NetworkModelImpl.ELEMENT_CAPACITY_DPKEY";
  /**
   * Key to register a {@link y.base.DataProvider} that contains some additional information
   * for a Node {@link NetworkNodeInfo}
   */
  public static final Object NODE_INFO_DPKEY = "demo.view.networkmonitoring.NetworkModelImpl.NODE_INFO_DPKEY";

  /**
   * Add the given {@link NetworkModelObserver observer} to the observer list.
   * The observer get informed when a status changed, e.g. when a new network
   * status where calculated or a element where deactivated.
   * @param observer observer
   */
  public void addObserver(NetworkModelObserver observer);

  /**
   * Remove the given {@link NetworkModelObserver observer} from the observer list.
   * @param observer observer
   */
  public void removeObserver(NetworkModelObserver observer);

  /**
   * Return the graph representing the network.
   * @return network graph
   */
  public Graph2D getNetworkModel();

  /**
   * Enable the given network node.
   * @param id network node
   */
  public void enableNetworkNode(Object id);

  /**
   * Disable the given network node.
   * @param id network node
   */
  public void disableNetworkNode(Object id);

  /**
   * Enable the given connection.
   * @param id connection
   */
  public void enableConnection(Object id);

  /**
   * Disable the given connection.
   * @param id connection
   */
  public void disableConnection(Object id);

  /**
   * Repair the given network node.
   * @param id network node
   */
  public void repairNetworkNode(Object id);

  /**
   * Repair the given connection.
   * @param id connection
   */
  public void repairEdge(Object id);

  /**
   * Set if network errors may happen
   * @param errorsEnabled true if network errors may happen
   */
  public void setNetworkErrorsEnabled(boolean errorsEnabled);

  /**
   * Return if network errors may happen
   * @return true if errors may happen
   */
  public boolean isNetworkErrorsEnabled();

  /**
   * Set duration until a new status should be calculated
   * @param duration time in milliseconds
   */
  public void setUpdateCycle(final int duration);

  /**
   * Return duration of a network cycle
   * @return duration of a network cycle in milliseconds
   */
  public int getUpdateCycle();

}
