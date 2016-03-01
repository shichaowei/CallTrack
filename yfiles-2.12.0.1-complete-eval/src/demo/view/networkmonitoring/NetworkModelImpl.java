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

import org.w3c.dom.Element;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.base.YList;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseException;
import y.util.DataProviderAdapter;
import y.util.GraphCopier;
import y.util.Maps;
import y.view.Graph2D;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

/**
 * This is an elementary network model that creates consistent data load. Data is sent between nodes without
 * a specific target. Every element has a queue that stores sent data packets. Data load changes
 * are turn-based.
 * The nodes behave differently:
 * <ul><li>Switches and W-LANs route data to the least busy node.</li>
 * <li>Server and databases send (maybe more) data back to the node who send him data.</li>
 * <li>PCs, Laptops and Tablets generate data and are the only target for data</li></ul>
 */
public class NetworkModelImpl implements NetworkModel {

  /**
   * The maximum number of cycles a data packet can stay at one network element until it gets deleted.
   */
  private static final int MAX_WAITING_CYCLES = 10;
  /**
   * The maximum number of data packets a PC generates in one cycle.
   */
  private static final int MAX_DATA_AMOUNT = 15;
  /**
   * Probability of data packet generation on a PC.
   */
  private static final double DATA_GENERATION_PROBABILITY = 0.02;
  /**
   * Map that holds the current state relative network load for each network element.
   * 0-1 is data load, -1 is disabled, -2 is broken.
   */
  private final DataMap statusMap;
  /**
   * Map that holds the capacity for each network element.
   */
  private final DataMap volumeMap;
  /**
   * Map that holds a queue for each network element that contains all data packets currently located at this
   * element.
   */
  private final DataMap loadMap;
  private final NodeMap nodeKind;
  private final NodeMap nodeInfo;
  private boolean canFailure;
  private final Random random = new Random(666);

  private final YList observerList;

  private final Graph2D graph;

  private final Timer timer;


  public NetworkModelImpl(URL resource) {
    observerList = new YList();
    graph = new Graph2D();
    //map a node to itself
    graph.addDataProvider(ELEMENT_ID_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return dataHolder;
      }
    });
    statusMap = Maps.createHashedDataMap();
    loadMap = Maps.createHashedDataMap();
    nodeKind = Maps.createHashedNodeMap();
    volumeMap = Maps.createHashedDataMap();
    nodeInfo = Maps.createHashedNodeMap();

    graph.addDataProvider(NetworkModel.NODE_TYPE_DPKEY,nodeKind);
    graph.addDataProvider(NetworkModel.ELEMENT_CAPACITY_DPKEY,volumeMap);
    graph.addDataProvider(NetworkModel.NODE_INFO_DPKEY, nodeInfo);

    loadNetworkGraph(resource);
    //calculate some steps to make the initial graph more interesting
    for (int i = 0; i < 40; i++) {
      updateState();
    }
    //This creates a timer that performs an action every 1500 milliseconds.
    //The actions calculate the next network status and notifies all registered observer.
    //This acts like a main loop for this model.
    timer = new Timer(1500, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateState();
        notifyObserver(statusMap);
      }
    });
    timer.start();
  }

  /**
   * Load the network graph from a given file.
   * @param resource <code>GraphML</code> file
   */
  private void loadNetworkGraph(URL resource) {
    final GraphMLIOHandler ioh = new GraphMLIOHandler();
    //register data acceptor to load additional data
    ioh.getGraphMLHandler().addInputDataAcceptor("NetworkMonitoring.Volume",volumeMap,KeyScope.ALL,KeyType.INT);
    ioh.getGraphMLHandler().addInputDataAcceptor("NetworkMonitoring.NodeKind", nodeKind, KeyScope.NODE, KeyType.INT);
    ioh.getGraphMLHandler().addInputDataAcceptor("NetworkMonitoring.NodeInfo", nodeInfo, KeyScope.NODE,
        new DeserializationHandler() {
          public void onHandleDeserialization(DeserializationEvent event) throws GraphMLParseException {
            final Element element = (Element) event.getXmlNode();
            event.setResult(new NetworkNodeInfo(element.getAttribute("Name"), element.getAttribute("IP")));
          }
        });
    try {
      ioh.read(graph, resource);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    //initialize queues for all elements
    for (final NodeCursor nodeCursor = graph.nodes();nodeCursor.ok();nodeCursor.next()) {
      loadMap.set(nodeCursor.node(),new YList());
    }
    for (final EdgeCursor edges = graph.edges();edges.ok();edges.next()) {
      loadMap.set(edges.edge(),new YList());
    }
  }

  /**
   * Updates the next state of the network. Network nodes and connections pass their current date packet to the
   * next destination.
   */
  private void updateState() {
    //process data on edges and nodes
    boolean hasCrashed = false;
    for (final EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
      final Edge edge = edges.edge();
      if (!hasCrashed) {
        hasCrashed = tryCrash(edge);
      }
      if (isOK(edge)) {
        updateEdge(edge);
      }
    }
    for (final NodeCursor nodes = graph.nodes(); nodes.ok(); nodes.next()) {
      final Node node = nodes.node();
      if (!hasCrashed) {
        hasCrashed = tryCrash(node);
      }
      if (isOK(node)) {
        switch (nodeKind.getInt(node)) {
          case PC:
          case LAPTOP:
          case SMARTPHONE:
            updatePC(node);
            break;
          case SWITCH:
          case WLAN:
            updateSwitch(node);
            break;
          case SERVER:
          case DATABASE:
            updateServer(node);
            break;
        }
      }
    }
    //calculate status values for view
    for (final NodeCursor nodes = graph.nodes(); nodes.ok(); nodes.next()) {
      final Node node = nodes.node();
      if (isOK(node)) {
        final double value = (double) ((YList) loadMap.get(node)).size() / (double) volumeMap.getInt(node);
        statusMap.setDouble(node, value);
      }
    }
    for (final EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
      final Edge edge = edges.edge();
      if (isOK(edge)) {
        final double value = (double) ((YList) loadMap.get(edge)).size() / (double) volumeMap.getInt(edge);
        statusMap.setDouble(edge, value);
      }
    }
  }

  /**
   * Processes all data packets on a server.
   * Servers send packets back to the last source of a data packet,
   * randomly along with newly generated data packets.
   * @param node the server node
   */
  private void updateServer(final Node node) {
    final YList packetQueue = (YList) loadMap.get(node);
    dropOldPackets(packetQueue);
    final YList unsentPackets = new YList();
    final int serverVolume = volumeMap.getInt(node);
    for (final Iterator iterator = packetQueue.iterator(); iterator.hasNext(); ) {
      DataPacket packet = (DataPacket) iterator.next();
      //ignore packets that were send to the server in the same cycle
      Edge edge = node.getEdge(packet.source);
      if ((packet.getWaitingCycles() > 0) && isOK(edge)) {
        final YList edgeQueue = (YList) loadMap.get(edge);
        final int edgeVolume = volumeMap.getInt(edge);
        //send back to source of data packet, if possible
        if (edgeVolume > edgeQueue.size()) {
          packet.setSource(node);
          packet.resetWaitingCycles();
          edgeQueue.add(packet);
          iterator.remove();
          //server response may contains  more packets than request
          final int newPackages = random.nextInt(3);
          for (int i = 0; i < newPackages; i++) {
            if (edgeVolume > edgeQueue.size()) {
              edgeQueue.add(new DataPacket(node));
            } else if (serverVolume - unsentPackets.size() > packetQueue.size()) {
              packet = new DataPacket(edge.opposite(node));
              unsentPackets.add(packet);
            }
          }
        } else {
          packet.incrementWaitingCycles();
        }
      } else {
        packet.incrementWaitingCycles();
      }
    }
    //add unsent packets to queue
    packetQueue.addAll(unsentPackets);
  }

  /**
   * Processes all data packets on a switch.
   * Switches forward data packets to a connected node that was not the source node.
   * The node whose connection has the most space left is chosen.
   * @param node the switch node
   */
  private void updateSwitch(final Node node) {
    final YList switchQueue = (YList) loadMap.get(node);
    dropOldPackets(switchQueue);
    //forward data packets, if possible
    for (final Iterator iterator = switchQueue.iterator(); iterator.hasNext(); ) {
      final DataPacket packet = (DataPacket) iterator.next();
      //ignore packets that were send to the switch in the same cycle
      if (packet.getWaitingCycles() > 0) {
        final Edge edge = getBestEdge(node.edges(), packet.getSource());
        if (edge != null) {
          final YList edgeLoad = (YList) loadMap.get(edge);
          packet.setSource(node);
          packet.resetWaitingCycles();
          edgeLoad.add(packet);
          iterator.remove();
        } else {
          packet.incrementWaitingCycles();
        }
      } else {
        packet.incrementWaitingCycles();
      }
    }

  }

  /**
   * Determines the edge out of the given set of edges which has the most unused capacity.
   * The edge that connects back to the source will be ignored.
   * @param edges  available connections
   * @param source source of the data packet
   * @return edge with most available space, if there is any, otherwise null.
   */
  private Edge getBestEdge(final EdgeCursor edges, final Node source) {
    EdgeList bestEdges = new EdgeList();
    int bestSpace = 0;
    for (; edges.ok(); edges.next()) {
      Edge edge = edges.edge();
      if (edge.target() != source && edge.source() != source && isOK(edge)) {
        int edgeSpace = volumeMap.getInt(edge) - ((YList) loadMap.get(edge)).size();
        if (edgeSpace > bestSpace) {
          bestEdges = new EdgeList(edge);
          bestSpace = edgeSpace;
        } else if (edgeSpace > 0 && edgeSpace == bestSpace) {
          bestEdges.add(edge);
        }
      }
    }
    if (bestEdges.isEmpty()) {
      return null;
    } else {
      //if there are many best edges, choose one randomly
      return (Edge) bestEdges.get(random.nextInt(bestEdges.size()));
    }
  }

  /**
   * Processes data packets on a PC.
   * Randomly generates new data. Data send to PC will be deleted.
   * @param node the pc node
   */
  private void updatePC(final Node node) {
    final YList load = (YList) loadMap.get(node);
    dropOldPackets(load);
    final int volume = volumeMap.getInt(node);
    //as PC has one out edge
    final Edge edge = node.edges().edge();
    if (isOK(edge)) {
      final YList edgeLoad = (YList) loadMap.get(edge);
      int availableEdgeSpace = volumeMap.getInt(edge) - edgeLoad.size();
      //first, process existing data packets
      for (final Iterator iterator = load.iterator(); iterator.hasNext(); ) {
        final DataPacket packet = (DataPacket) iterator.next();
        if (packet.getWaitingCycles() > 0) {
          if (packet.getSource() != node) {
            iterator.remove();
          } else if (availableEdgeSpace > 0) {
            edgeLoad.add(packet);
            packet.resetWaitingCycles();
            availableEdgeSpace--;
            iterator.remove();
            //package stays at PC
          } else {
            packet.incrementWaitingCycles();
          }
        } else {
          packet.incrementWaitingCycles();
        }
      }
      //then, create new data packets
      if (random.nextDouble() <= DATA_GENERATION_PROBABILITY) {
        final int dataAmount = random.nextInt(MAX_DATA_AMOUNT);
        for (int i = 0; i < dataAmount; i++) {
          final DataPacket packet = new DataPacket(node);
          if (availableEdgeSpace > 0) {
            edgeLoad.add(packet);
            availableEdgeSpace--;
          } else if (volume > load.size()) {
            load.add(packet);
          } else {
            //when queue of edge and PC are full, all following data will be dropped
            break;
          }
        }
      }
    }
  }

  /**
   * Connections forward data packets to the node opposite of the packet source.
   * @param edge the connection edge
   */
  private void updateEdge(final Edge edge) {
    final YList edgeLoad = (YList) loadMap.get(edge);
    dropOldPackets(edgeLoad);
    final Node source = edge.source();
    final Node target = edge.target();
    final YList sourceLoad = (YList) loadMap.get(source);
    final YList targetLoad = (YList) loadMap.get(target);
    int availableSpaceSource = volumeMap.getInt(source) - sourceLoad.size();
    int availableSpaceTarget = volumeMap.getInt(target) - targetLoad.size();
    for (final Iterator iterator = edgeLoad.iterator(); iterator.hasNext(); ) {
      DataPacket packet = (DataPacket) iterator.next();
      if ((packet.getSource() == source) && (availableSpaceTarget > 0)) {
        //packet came from source, so we try to send to target
        targetLoad.add(packet);
        packet.resetWaitingCycles();
        availableSpaceTarget--;
        iterator.remove();
      } else if ((packet.getSource() == target) && (availableSpaceSource > 0)) {
        //packet came from target, so we try to send to source
        sourceLoad.add(packet);
        packet.resetWaitingCycles();
        availableSpaceSource--;
        iterator.remove();
      } else {
        //packet stays at edge
        packet.incrementWaitingCycles();
      }
    }
  }

  /**
   * Removes data from given list that stayed more than {@link #MAX_WAITING_CYCLES}.
   * @param load queue
   */
  private void dropOldPackets(YList load) {
    for (final Iterator iterator = load.iterator();iterator.hasNext();) {
      final DataPacket next = (DataPacket) iterator.next();
      if (next.getWaitingCycles() > MAX_WAITING_CYCLES) {
        iterator.remove();
      }
    }
  }

  /**
   * Check by random if this element crashed.
   * When the element is a node, crashing will deactivate all connected edges.
   * @param id the element
   * @return true if element crashed, false otherwise
   */
  private boolean tryCrash(Object id) {
    if (canFailure && random.nextInt(3000) == 0) {
      statusMap.setDouble(id, -2);
      loadMap.set(id,new YList());
      if (id instanceof Node) {
        Node node = (Node) id;
        for (final EdgeCursor edgeCursor = node.edges();edgeCursor.ok();edgeCursor.next()) {
          disableConnection(edgeCursor.edge());
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Notifies observer of new step
   */
  private void notifyObserver(DataMap sendMap) {
    final Iterator iterator = observerList.iterator();
    while (iterator.hasNext()) {
      final NetworkModelObserver observer = (NetworkModelObserver) iterator.next();
      observer.update(sendMap);
    }
  }

  public void addObserver(final NetworkModelObserver observer) {
    observerList.add(observer);
  }

  public void removeObserver(final NetworkModelObserver observer) {
    observerList.remove(observer);
  }

  public Graph2D getNetworkModel() {
    final GraphCopier graphCopier = new GraphCopier();
    graphCopier.setCopyFactory(graph.getGraphCopyFactory());
    graphCopier.setDataProviderContentCopying(true);
    return (Graph2D) graphCopier.copy(graph);
  }

  /**
   * Check if an element is not broken or deactivated
   * @param id the element
   * @return true if element is not broken and not deactivated
   */
  public boolean isOK(final Object id) {
    return statusMap.getDouble(id) >= 0;
  }

  public void enableNetworkNode(final Object id) {
    DataMap sendMap = Maps.createHashedDataMap();
    statusMap.setDouble(id, 0);
    final Node node = (Node) id;
    for (final EdgeCursor edges = node.edges(); edges.ok(); edges.next()) {
      final Edge edge = edges.edge();
      if (isOK(edge.opposite(node)) && statusMap.getDouble(edge) != -2) {
        enableConnection(edge);
        statusMap.set(edge, new Double(0));
        sendMap.set(edge,new Double(0));
      }
    }
    sendMap.set(id,new Double(0));
    notifyObserver(sendMap);
  }

  public void disableNetworkNode(final Object id) {
    DataMap sendMap = Maps.createHashedDataMap();
    statusMap.setDouble(id, -1);
    loadMap.set(id,new YList());
    for (final EdgeCursor edges = ((Node) id).edges(); edges.ok(); edges.next()) {
      final Edge edge = edges.edge();
      if (isOK(edge)) {
        sendMap.set(edge,new Double(-1));
        statusMap.setDouble(edge, -1);
        loadMap.set(edge, new YList());
      }
    }
    sendMap.set(id,new Double(0));
    notifyObserver(sendMap);
  }

  public void enableConnection(final Object id) {
    DataMap sendMap = Maps.createHashedDataMap();
    sendMap.setDouble(id,0);
    statusMap.set(id, new Double(0));
    notifyObserver(sendMap);
  }

  public void disableConnection(final Object id) {
    if (isOK(id)) {
      DataMap sendMap = Maps.createHashedDataMap();
      statusMap.setDouble(id, -1);
      sendMap.set(id, new Double(-1));
      loadMap.set(id, new YList());
      notifyObserver(sendMap);
    }
  }

  public void repairNetworkNode(final Object id) {
    enableNetworkNode(id);
  }

  public void repairEdge(final Object id) {
    statusMap.setDouble(id, 0);
    DataMap sendMap = Maps.createHashedDataMap();
    sendMap.set(id,new Double(0));
    notifyObserver(sendMap);
  }

  public void setUpdateCycle(final int duration) {
    timer.setDelay(duration);
  }

  public int getUpdateCycle() {
    return timer.getDelay();
  }

  public void setNetworkErrorsEnabled(boolean errorsEnabled) {
    this.canFailure = errorsEnabled;
    if (!errorsEnabled) {
      //if no failures should happen, repair all elements at once
      for (final NodeCursor nodes = graph.nodes(); nodes.ok(); nodes.next()) {
        final Node node = nodes.node();
        if (statusMap.getDouble(node) == -2) {
          repairNetworkNode(node);
        }
      }
      for (final EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
        final Edge edge = edges.edge();
        if (statusMap.getDouble(edge) == -2) {
          repairEdge(edge);
        }
      }
    }
  }

  public boolean isNetworkErrorsEnabled() {
    return canFailure;
  }

  /**
   * A DataPacket is the unit for data load.
   */
  private class DataPacket {
    private Node source;
    private int waitingCycles;

    private DataPacket(Node source) {
      this.source = source;
    }

    public Node getSource() {
      return source;
    }

    public void setSource(final Node source) {
      this.source = source;
    }

    public void incrementWaitingCycles() {
      waitingCycles++;
    }

    public void resetWaitingCycles() {
      waitingCycles = 0;
    }

    public int getWaitingCycles() {
      return waitingCycles;
    }
  }
}
