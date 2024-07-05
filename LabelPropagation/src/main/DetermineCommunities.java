/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fapra.LabelPropagation;

import java.util.TreeMap;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Bestimmt die aktuell entdeckten Communities incl. deren Mitglieder.
 *
 * @author Manuela Koller
 */
public class DetermineCommunities {
    
    public TreeMap<Integer, TreeMap<String,String>> giveCommunities(Node[] nodes, TreeMap<Integer, TreeMap<String, String>> giveComOut,
            Column col){
        
        for(Node b: nodes){
            Integer comNode = (Integer) b.getAttribute(col);//Community des Knotens bestimmen
            String idNode = (String) b.getId(); //Id des Knotens bestimmen
            String labelNode = b.getLabel(); //Label des Knotens bestimmen             
            
            if(giveComOut.containsKey(comNode)){               
                TreeMap<String, String> helpTree4 = new TreeMap<>();
                helpTree4 = giveComOut.get(comNode);              
                helpTree4.put(idNode, labelNode);
                giveComOut.put(comNode, helpTree4);                
            }else{
                TreeMap<String, String> helpTree2 = new TreeMap<>();
                helpTree2.put(idNode, labelNode);
                giveComOut.put(comNode, helpTree2);                
            }
        }       
        return giveComOut;
    }  
        
    
    /*
    Wenn Kanten innerhalb der Communities kein höheres Gewicht zugewiesen werden soll,
    dann erhalten alle Kanten das Gewicht 1.0
    */
    public void resetEdgeWeight(Graph graph){
        EdgeIterable resetEdges = graph.getEdges();
        for(Edge reEd: resetEdges){
            reEd.setWeight(1.0);
        }
    }
    
    
    /*
    Benutzer kann wählen, ob er die Kanten innerhalb der Communities, mit höheren
    Gewichten versehen möchte. Dies ist für spätere Benutzung von Layout-Algorithmen
    von Vorteil, welche das Kantengewicht berücksichtigen.
    */    
    public void setEdgeWeightForInnerEdges (Graph graph, TreeMap<Integer, TreeMap<String, String>> giveComOut, Column col){
       
        //alle Kanten mit Gewicht 0.01 belegen      
        EdgeIterable edgesGraph = graph.getEdges();
        for(Edge edGr: edgesGraph){
            edGr.setWeight(0.01);
        }        
        for(Integer elem: giveComOut.keySet()){
            //alle Kanten innerhalb einer Community bestimmen
            TreeMap<String, String> help = new TreeMap<>();
            help = giveComOut.get(elem); //Knoten-Ids der Community-Mitglieder bestimmen
            for(String elem2: help.keySet()){
                for(String elem3: help.keySet()){                    
                    Node nn = graph.getNode(elem2);
                    Node mm = graph.getNode(elem3);
                    EdgeIterable edges = graph.getEdges(nn, mm);
                    //allen inneren Kanten wird ein höheres Kantengewicht zugewiesen
                    for(Edge ed: edges){
                        ed.setWeight(0.1);
                    }                    
                }               
            }            
        }
    }
}
