/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fapra.LabelPropagation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

/**
 * Klasse Neighbors stellt zwei Methoden zur Verfügung, mit deren Hilfe die
 * Nachbarknoten der Knoten des Graphen bestimmt werden.  *  
 * 
 * @author Manuela Koller
 * 
 */
public class Neighbors {
    /*
    Methode getSuccessorNodes wird bei gerichteten Graphen angewandt.
    Bei gerichteten Kanten stellen nur die Nachfolgerknoten (Zielknoten) 
    die jeweiligen Nachbarknoten dar.
    */
    public HashMap<Node, List<Node>> getSuccessorNodes(DirectedGraph graphDir, Node[] nodes){
        
        HashMap<Node, List<Node>> nodesWithNeighbors = new HashMap<>();        
        
        for(Node n: nodes){
            List<Node> neighbors = new LinkedList<>();
            EdgeIterable edges = graphDir.getOutEdges(n);
            for(Edge e: edges){
                if(!e.isSelfLoop()){
                    Node targetNode = e.getTarget();
                    neighbors.add(targetNode);
                }                
            }
            nodesWithNeighbors.put(n, neighbors);            
        }            
        return nodesWithNeighbors;        
    }
    
    /*
    Methode getNeighborNodes wird bei ungerichteten Graphen angewandt.
    Da es sich hierbei um ungerichtete Kanten handelt, stellen sowohl Vorgänger-
    als auch Nachfolgerknoten jeweils Nachbarknoten dar.
    */
    public HashMap<Node, List<Node>> getNeighborNodes(Graph graph, Node[] nodes){
        
        HashMap<Node, List<Node>> nodesWithNeighbors = new HashMap<>();
        for(Node n: nodes){
            List<Node> neighbors = new LinkedList<>();
            NodeIterable help = graph.getNeighbors(n);
            for(Node o: help){
                Edge edge = graph.getEdge(n, o);
                if(!edge.isSelfLoop()){
                    neighbors.add(o);
                }               
            }
            nodesWithNeighbors.put(n, neighbors);           
        }       
        return nodesWithNeighbors;        
    }    
}
