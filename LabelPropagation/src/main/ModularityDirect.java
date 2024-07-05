/* 
 */
package org.fapra.LabelPropagation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;

/**
 * Bestimmung der Modularität für gerichtete in Communities partitionierte Graphen.
 * Anwendung der Formel nach Leicht und Newman zur Bestimmung der Modularität.
 * Formel lautet: Q = 1/m sum_ij [A_ij - (k_i^in * k_j^out)/m] * Kronecker-Delta_C_i,C_j
 * 
 *
 * @author Manuela Koller
 */
public class ModularityDirect {
    
    /*
    Adjazenzmatrix A für gerichteten Graph erstellen. 
    Anschließend von den Einträgen der Adjazenzmatrix jeweils 
    (inDegreeSource*outDegreeTarget)/Kantenanzahl subtrahieren, um die Werte 
    A_ij - (k_i^in * k_j^out)/m   zu erhalten.
    */
    
    public void initializeMatrixModDirect(DirectedGraph graphDir, Node[] nodes,
            double[][] matrixMod, HashMap<String, Integer> nodeIdComBegin){     
        
        //Eintrag hat den Wert 1 wenn eine gerichtete Kante vorhanden, ansonsten 0.          
        for(Node p: nodes){                            
                EdgeIterable edges0 = graphDir.getEdges(p);               
                for(Edge ed: edges0){
                    if(ed.getSource() == p){             
                        int xCoord = nodeIdComBegin.get(p.getId());
                        Node target = ed.getTarget();        
                        int yCoord = nodeIdComBegin.get(target.getId());
                        double insert = 1.0;                        
                        matrixMod[xCoord][yCoord] = insert;                                          
                    }
                }
        }        
        
        
        
        //von den Einträgen der Adjazenzmatrix jeweils (k_i^in * k_j^out)/m 
        //subtrahieren, um die Werte A_ij - (k_i^in * k_j^out)/m   zu erhalten.
        double numberEdges = graphDir.getEdgeCount(); //Anzahl der Kanten des Graphen             
        for(Node n: nodes){
            for(Node m: nodes){
                double outDeg = graphDir.getOutDegree(n);
                double inDeg = graphDir.getInDegree(m);              
                int xCoord = nodeIdComBegin.get(n.getId());
                int yCoord = nodeIdComBegin.get(m.getId());
                
                matrixMod[xCoord][yCoord] = matrixMod[xCoord][yCoord] -
                       ((outDeg*inDeg)/numberEdges);
            }
        }        
    }
    
   /*
    Modularität des gerichteten, in Communities partitionierten Graphen berechnen.
    Dazu werden nur die Einträge der Matrix aufsummiert, deren zugehörige Knoten 
    sich in derselben Community befinden (ist somit äquivalent zu der Multiplikation der
    Einträge mit dem Kronecker-Delta). Die Summe wird anschließend noch
    durch die Kantenanzahl des Graphen dividiert.
    */     
    public double modulCalcDirect(DirectedGraph graphDir, 
            TreeMap<Integer, TreeMap<String, String>> giveComOut, 
            HashMap<String, Integer> nodeIdComBegin, double[][] matrixMod){
        
        double modularityDir = 0.0;       
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
            
            //Einträge der Adjazenzmatrix aufsummieren und anschließend durch die Anzahl
            //der Kanten dividieren
            for(int i=0; i < comList.size(); i++){
                for(int j = 0; j < comList.size(); j++){
                    Integer a = comList.get(i);
                    Integer b = comList.get(j);         
                                          
                        double mod = matrixMod[a][b];                       
                        modularityDir += mod;
                    
                }
            }           
        }      
        modularityDir = modularityDir/graphDir.getEdgeCount();
        modularityDir = Math.round(modularityDir*1000)/1000.0;       
        return modularityDir;
    }   
}
