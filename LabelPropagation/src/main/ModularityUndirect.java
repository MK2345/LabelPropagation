/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fapra.LabelPropagation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Bestimmung der Modularität von ungerichteten in Communities partitionierten Graphen.
 * Anwendung der Formel von Girvan und Newman zur Bestimmung der Modularität.
 * Die Formel lautet: Q = 1/2*m sum_ij (A_ij - k_i*k_j/2*m)  
 * @author Manuela Koller
 */
public class ModularityUndirect {
    
     //Adjazenzmatrix erstellen
     public void initializeMatrixMod(Graph graph, Node[] nodes,
            double[][] matrixMod3, HashMap<String, Integer> nodeIdComBegin){    
        
        //Eintrag hat den Wert 1 wenn eine ungerichtete Kante vorhanden, ansonsten 0.          
        for(Node p: nodes){                            
                EdgeIterable edges0 = graph.getEdges(p);               
                for(Edge ed: edges0){
                    Node target = graph.getOpposite(p, ed);
                    int xCoord = nodeIdComBegin.get(p.getId());
                    int yCoord = nodeIdComBegin.get(target.getId());
                    double insert = 1.0;
                    matrixMod3[xCoord][yCoord] = insert;                    
                }
        }  
                
        
        //Modularity-Matrix erstellen:
        //von den Einträgen der Adjazenzmatrix jeweils (k_i * k_j)/2*m 
        //subtrahieren, um die Werte A_ij - (k_i * k_j)/2*m  zu erhalten 
        double numberEdges = graph.getEdgeCount(); //Anzahl der Kanten des Graphen             
        for(Node n: nodes){
            for(Node m: nodes){
                double nDeg = graph.getDegree(n);
                double mDeg = graph.getDegree(m);                         
                int xCoord = nodeIdComBegin.get(n.getId());
                int yCoord = nodeIdComBegin.get(m.getId());
                
                matrixMod3[xCoord][yCoord] = matrixMod3[xCoord][yCoord] -
                       ((nDeg*mDeg)/(2*numberEdges));
            }
        }  
    }
    
   /*
    Modularität des ungerichteten, in Communities partitionierten Graphen berechnen.
    Dazu werden nur die Einträge der Matrix aufsummiert, deren zugehörige Knoten 
    sich in derselben Community befinden (ist somit äquivalent zu der Multiplikation der
    Einträge mit dem Kronecker-Delta). Die Summe wird anschließend noch
    durch 2*Kantenanzahl des Graphen dividiert.
    */     
    public double modulCalcUndirect(Graph graph, 
            TreeMap<Integer, TreeMap<String, String>> giveComOut, 
            HashMap<String, Integer> nodeIdComBegin, double[][] matrixMod){
        
        double modularityUndir = 0.0;       
        //zu den einzelnen Communities werden die zugehörigen Mitglieder mit Hilfe von giveComOut 
        //abgefragt
        for(Integer elem: giveComOut.keySet()){            
            TreeMap<String,String> helpTree = new TreeMap<String,String>();
            helpTree = giveComOut.get(elem);//Id und Label der Mitglieder
            Set<String> keyList = helpTree.keySet();//Knoten Id's welche sich in gleicher Community befinden
                        
                    
            //Communities der Knoten, welche sie zu Beginn hatten in comList speichern,
            //damit der entsprechende Eintrag in der Adjazenzmatrix ausfindig gemacht werden kann
            LinkedList<Integer> comList = new LinkedList<>();
            Iterator<String> it = keyList.iterator();
            while(it.hasNext()){
                String comKey = it.next();
                Integer comStart = nodeIdComBegin.get(comKey);//Community des Knotens zu Beginn
                comList.add(comStart); //Anfangscommunity hinzufügen                 
            }           
            
            //Einträge der Adjazenzmatrix aufsummieren und anschließend durch die 
            //zweifache Anzahl der Kanten dividieren
            for(int i=0; i < comList.size(); i++){
                for(int j = 0; j < comList.size(); j++){
                    Integer a = comList.get(i);
                    Integer b = comList.get(j);         
                                          
                        double mod = matrixMod[a][b];                       
                        modularityUndir += mod;
                    
                }
            }           
        }      
        modularityUndir = modularityUndir/(2*graph.getEdgeCount());
        modularityUndir = Math.round(modularityUndir*1000)/1000.0;       
        return modularityUndir;
    }    
}
