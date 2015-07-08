package org.cytoscape.intern;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;


/**
 * An instance of manager, which is constructed in 
 * DotWriteTask class. Calls methods which return a 
 * property String based on the passed in View.  
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DataManager {
  
  //instance of NodePropertyMapper class
  NodePropertyMapper nodeMapper;

  //instance of EdgePropertyMapper class
  EdgePropertyMapper edgeMapper;

  
  /**
   * checks out which method,either getEdgeString or getNodeString, 
   * will be called based on the parameter 
   *
   * @param pass in the View (Edge's or Node's), 
   * which determines Edge property or Node property
   */
  public String getElementString (View<Object> elementView) {
    //TODO
  }
 
  /**
   * Calls method mapVisToDot in EdgePropertyManager class 
   *
   * @param pass in the Edge View
   */
  public String getEdgeString(View<CyEdge> edgeView) {
    //TODO
  }

  /**
   * Calls method mapVisToDot in NodePropertyManager class 
   *
   * @param pass in the Node View
   */
   public String getNodeString(View<CyNode> nodeView) {
     //TODO
   }

}
