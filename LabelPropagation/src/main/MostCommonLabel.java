/*
Klasse MostCommonLabel. Bestimmt für einen Knoten das am Häufigsten in seiner
Nachbarschaft vorkommende Community-Label.
 */
package org.fapra.LabelPropagation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Node;

/**
 * Klasse MostCommonLabel dient zur Bestimmung der häufigsten Community-Labels
 * in der Nachbarschaft eines Knotens
 * 
 * @author Manuela Koller
 */
public class MostCommonLabel {    
    
    /*
    Bestimmt aus einer Liste mit den Nachbarknoten, die jeweilige Anzahl der Community-Label
    und speichert diese in einer HashMap. Anschließend wird anhand dieser HashMap das/die
    häufigste/n Community-Label/s bestimmt und in LinkedList "mostCommonLabel" zurückgegeben.
    */
    
    public ArrayList<Integer> findMostCommonLabel(List<Node> neighborNodes, Column col){ 
        ArrayList<Integer> mostCommonLabel = new ArrayList<>();
        HashMap<Integer,Integer> numbersOfLabels = new HashMap<>();
        for(Node n: neighborNodes){
            Integer labNei = (Integer) n.getAttribute(col);
            if(numbersOfLabels.containsKey(labNei)){
                //wenn Label bereits in HashMap enthalten, dann Anzahl um 1 erhöhen
                Integer number = numbersOfLabels.get(labNei); 
                number += 1;
                numbersOfLabels.put(labNei,number);                
            }else{  //ansonsten Label mit Anzahl 1 in HashMap aufnehmen
                numbersOfLabels.put(labNei, 1);
            }            
        }
        //Häufigste/n Nachbarlabel/s bestimmen
        int max = 0;
        for(HashMap.Entry<Integer,Integer> e: numbersOfLabels.entrySet()){
            int val = (int) e.getValue();
            Integer key = e.getKey();                   
            if(val > max){
                max = val;
                mostCommonLabel.clear();                
                mostCommonLabel.add(key);               
            }else if(val == max){
                mostCommonLabel.add(key);               
            }
        }        
        return mostCommonLabel;
    }   
}
