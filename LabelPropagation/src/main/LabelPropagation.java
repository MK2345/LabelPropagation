package org.fapra.LabelPropagation;

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Column;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.statistics.plugin.ChartUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;






/**
 * Label Propagation Algorithmus
 * Bestimmen von Communities in Netzwerken.
 *  
 * @author Manuela Koller
 */
public class LabelPropagation implements Statistics {     
    
    //Knotentabelle
    public Table nodeTable;    
    
    /*
    Neue Spalte für Knotentabelle. Enthält die jeweilige Community der Knoten
    */    
    public Column col;      
    
    /*
    Name der neuen Spalte, welche zur Knotentabelle hinzugefügt wird
    */   
    public String labProp = "Community-ID (LPA)";    
    
    /*
    Klasse Neighbors enthält Methoden zur Bestimmung der Nachbarknoten
    */
    Neighbors neighbors;
    
    /*
    Klasse MostCommonLabel wird benötigt, um für jeden Knoten das häufigste 
    Community-Label in seiner Nachbarschaft zu bestimmen
    */        
    MostCommonLabel mostCommon;
    
    /*
    Klasse zur Bestimmung der entdeckten Communities incl. deren Mitglieder.
    Bietet zusätzlich Methoden, um die Kantengewichte innerhalb der Communities zu verändern.
    */
    DetermineCommunities detCommunities;
    
    
    /*
    keys dieser TreeMap enthalten die gefundenen Communities, value-TreeMap enthält 
    die Mitglieder (Id und Label) der jeweiligen Community.
    Wird zur Ausgabe der Communities im HTML-Report und zur Berechnung der Modularität
    verwendet.
    */   
    public TreeMap<Integer, TreeMap<String, String>> giveComOut;
    
    //speichert die Anzahl der Communities nach jeder Iteration
    public Vector<Integer> communityCountList;
    //speichert die Modularität nach jeder Iteration
    public Vector<Double> modularityList;      
    
    
    /*
    String welcher die gefundenen Communities und deren Mitglieder auflistet.
    Wird für HTML-Report benötigt.
    */
    public String communities = ""; 
    public String communitiesHelp = ""; //Hilfsvariable
    public int comNumb; //Anzahl gefundener Communities          
            
    //Matrix zur Bestimmung der Modularität nach Girvan-Newman
    public double[][] matrixModGN; 
    //Matrix zur Bestimmung der Modularität nach Leicht-Newman
    public double[][] matrixModLN;
    
     
    //Darstellung des Verlaufs der Modularität und der Anzahl der entdeckten Communities
    //nach jeder Iteration
    public String imageFile;
    public String imageFile2;
    public String imageFile3;
    public String imageFile4;
    public String minModularity;
    public String maxModularity ;
    
    /*
    für gerichtete Graphen stehen zusätzliche Methoden zur Verfügung, welche
    zur Berechnung der Modularität von gerichteten Graphen nützlich sind
    */
    DirectedGraph graphDir;  
    
    
    /*
    Klasse ModularityDirect verfügt über zwei Methoden, welche der Berechnung
    der Modularität von gerichteten, in Communities partitionierten Graphen, dienen 
    */
    ModularityDirect modulDir;    
    
    /*
    Klasse ModularityUndirect enthält zwei Methoden zur Berechnung der Modularität
    von ungerichteten Graphen, welche in Communities paritioniert wurden   
    */   
    ModularityUndirect modulUnDir;    
    
    //Modularität 
    public double modularity; 
      
    //wird für HTML-Report benötigt    
    public String message ="";   
    

    @Override
    public void execute(GraphModel graphModel) {        
        
        /*
        Hinzufügen einer Spalte zur Knotentabelle. In dieser werden die Communities
        zu den jeweiligen Knoten eingetragen. Communities werden hierbei durch
        eine natürliche Zahl dargestellt.
        */        
        nodeTable = graphModel.getNodeTable();        
        col = nodeTable.getColumn(labProp);
        if (col!=null && !col.getTypeClass().equals(Integer.class)) {
            nodeTable.removeColumn(col);
            col=null;
        }
        if (col == null){
            col = nodeTable.addColumn(labProp, Integer.class);            
        }
          
        Graph graph = graphModel.getGraph();
        
        /*
        enthält alle Knoten des Graphen
        */
        Node[] nodes = graph.getNodes().toArray();
        
        mostCommon = new MostCommonLabel();      
        detCommunities = new DetermineCommunities();
                
                
        /*
        Durch Methode assignUniqueLabelsToNodes wird jeden Knoten ein eindeutiges Community-Label 
        zugewiesen in Form einer fortlaufenden Nummer zugewiesen.
        Zusätzlich wird eine HashMap mit den Knoten-Ids als keys und den Community-Labels
        als values angelegt. Dadurch soll der Anfangszustand der Label-Belegung festgehalten werden
        (wird für die spätere Bestimmung der Modularität benötigt).
        */
        HashMap<String, Integer> nodeIdComBegin = new HashMap<>();
        nodeIdComBegin = assignUniqueLabelsToNodes(nodeIdComBegin, nodes);
                
        
        /*
        solange nicht jedem Knoten das Community-Label zugewiesen wurde, welches die meisten
        Nachbarknoten besitzen, enthält die Variable "change" den Wert true.       
        */        
        boolean change = true;   
        
         /*
        Zähler t, zählt die Iterationen
        */
        int t = 1;  
        
        
        /*
        Zu jedem Knoten werden dessen Nachbarknoten bestimmt und in der HashMap
        nodesNeighbors gespeichert. Bei der Bestimmung der Nachbarknoten wird
        zwischen gerichteten und ungerichteten Graphen unterschieden.        
        */       
                
        neighbors = new Neighbors();
        HashMap<Node, List<Node>> nodesNeighbors = new HashMap<>();//speichert zu jedem Knoten eine Liste seiner Nachbarn
        if(graph.isDirected()){           
            graphDir = graphModel.getDirectedGraph();   
            //da Graph gerichtet ist, werden nur Nachfolgerknoten als Nachbarknoten gespeichert            
            nodesNeighbors = neighbors.getSuccessorNodes(graphDir, nodes);             
            
        }else{
            //da Graph ungerichtet ist, werden alle Nachbarknoten berücksichtigt, ohne Unterscheidung
            //zwischen Ursprungs- und Zielknoten            
            nodesNeighbors = neighbors.getNeighborNodes(graph, nodes);            
        }      
          
               
        //speichert die nach jeder Iteration Anzahl entdeckter Communities. Wird für späteren
        //HTML-Report benötigt
        communityCountList = new Vector<Integer>();
        
        //speichert die nach jeder Iteration berechnete Modularität. Wird für späteren
        //HTML-Report benötigt
        modularityList = new Vector<Double>();      
      
                    
        //Matrix, welche zur Berechnung der Modularität benötigt wird, erstellen
        createMatrixForModularity(graph, nodes, nodeIdComBegin);
              
       
        /*
        while(change)-Schleife wird solange durchlaufen, bis jeder Knoten mindestens so viele
        Nachbarn innerhalb seiner Community aufweist wie er Nachbarn mit jeder anderen Community
        hat.
        */
        while(change) {    
            change = false; 
            
            /*                        
            Knoten werden in LinkedList nodesList gespeichert und anschließend 
            in eine zufällige Reihenfolge gebracht
            */
            LinkedList<Node> nodesList = new LinkedList<Node>(Arrays.asList(nodes));                   
            Collections.shuffle(nodesList);
                         
            //Knoten, welcher aus LinkedList nodesList entnommen wird
            Node nodeElement;
            
            while (nodesList.size() > 0){                    
                //ersten Knoten aus nodesList entnehmen und gleichzeitig in nodesList entfernen                              
                nodeElement = nodesList.removeFirst();
                
               /*
                Nachbarknoten von dem aus nodesList entnommenen Knoten
                nodeElement bestimmen und in der Liste neighborNodes speichern
                */                  
                List<Node> neighborNodes = new LinkedList<>();     
                neighborNodes = nodesNeighbors.get(nodeElement);                
                                
                /*
                das/die in den Nachbarknoten am häufigsten vorkommenden Community-Label 
                bestimmen und speichern
                */                
                ArrayList<Integer> mostCommonLabels = new ArrayList<>();
                mostCommonLabels = mostCommon.findMostCommonLabel(neighborNodes, col);
                                 
                /*
                Dem Knoten das Community-Label zuweisen, welches die meisten seiner Nachbarn
                besitzen. Falls mehrere Label mit gleicher maximaler Häufigkeit vorkommen, wird
                eines dieser Label zufällig gewählt
                */                
                if(mostCommonLabels.size() > 1){
                    int index1 = (int) (Math.random() * mostCommonLabels.size());
                    Integer rando = mostCommonLabels.get(index1);                                        
                    nodeElement.setAttribute(col, rando);                       
                                 
                }else if(mostCommonLabels.size() == 1){                                               
                    nodeElement.setAttribute(col, mostCommonLabels.get(0));     
                }                  
            } //Ende innere while-Schleife   
                       
            System.out.println("Es handelte sich um die "+ t +"-te Iteration");
            System.out.println("--------------------------------------------------------");                  
            
            /*
            Prüfen, ob jedem Knoten das in seiner Nachbarschaft am häufigsten vorkommende 
            Community-Label zugewiesen wurde (bzw. eines derjenigen Label, welche mit 
            derselben maximalen Häufigkeit vorkommen)
            */  
            for(Node n: nodesNeighbors.keySet()){  
               
                //Nachbarknoten von Knoten n mit Hilfe von HashMap nodesNeighbors bestimmen
                //und in Liste neighborNodesForTest speichern               
                List<Node> neighborNodesForTest = new LinkedList<>();
                neighborNodesForTest = nodesNeighbors.get(n);//Nachbarknoten bestimmen                                               
                
                /*                
                das/die in den Nachbarknoten mit maximaler Häufigkeit vorkommende 
                Community-Label mit Hilfe der Methode findMostCommonLabel bestimmen 
                und in LinkedList mostCommonLabelForTest speichern
                */   
                ArrayList<Integer> mostCommonLabelsForTest = new ArrayList<>();
                mostCommonLabelsForTest = mostCommon.findMostCommonLabel(neighborNodesForTest, col);                          
                
                /*
                Prüfen, ob dem Knoten n, eines der häufigsten Community-Label seiner Nachbarknoten,
                noch nicht zugewiesen wurde. Dies erfolgt, indem getestet wird, ob das Community-Label
                des Knotens, sich nicht in der LinkedList mostCommonLabelForTest befindet. 
                Ist dies der Fall, dann wird change auf true gesetzt und die while(change)-Schleife 
                wird erneut durchlaufen und somit der iterative Prozess fortgeführt.
                */                 
                Integer nLab = (Integer) n.getAttribute(col);             
                if(!mostCommonLabelsForTest.contains(nLab) & !mostCommonLabelsForTest.isEmpty()){                  
                    change = true; 
                    t += 1;  //Iterationszähler um 1 erhöhen
                    break;                   
                }else{                   
                }
            }              
                                  
            //Communities des aktuell paritionierten Graphen bestimmen und deren Anzahl
            //für den HTML-Report speichern
            determineDiscoveredCommunities(nodes);
            
            //Modularität des aktuell paritionierten Graphen bestimmen und für den
            //HTML-Report speichern
            calculateModularity(nodes, graph, graphDir, nodeIdComBegin);            
            
            if(change == false){                
                break;
            }                        
        } //while(change);     
        
        //Ausgabe der Modularität im HTML-Report
        if(!graph.isDirected()){
            message = "Modularität nach Girvan-Newman: "+String.valueOf(modularity);
        }else{
             message = "Modularität nach Leicht-Newman: "+String.valueOf(modularity);
        }        
        
        //Ausgabe der Communities und deren Mitglieder für HTML-Report vorbereiten
        prepareOutputCommunities(graph);
        
        //nach Anpassung der Kantengewichte fragen
        askAdaptEdgeWeight(graph);          
        
        //Chart für HTML-Report erstellen
        createCharts(graph);        
    }   
       
    
    /*
    Damit zu Beginn jeder Knoten ein eindeutiges Community-Label besitzt, wird den Knoten 
    eine fortlaufende Nummer als Attribut hinzugefügt. 
    Zusätzlich wird eine HashMap mit den Knoten-Ids als keys und den Community-Labels als
    values angelegt. Dadurch soll der Anfangszutand der Community-Label-Belegung festgehalten
    werden. Dies wird für die Berechnung der Modularität benötigt, um die entsprechenden Einträge 
    in der Adjazenzmatrix bestimmen zu können.
    */   
    private HashMap<String, Integer> assignUniqueLabelsToNodes(HashMap<String, Integer> nodeIdComBegin, Node[] nodes){
        Integer uniqueLabel = 0;
        for(Node n: nodes){
            n.setAttribute(col, uniqueLabel); 
            String nodeId = (String) n.getId();
            nodeIdComBegin.put(nodeId, uniqueLabel);          
            uniqueLabel += 1;
        }      
        return nodeIdComBegin;
    }

    
    /*
    Jeweilige Matrix erstellen, welche zur Berechnung der jeweiligen Modularitäten dienen.   
    */
    private void createMatrixForModularity(Graph graph, Node[] nodes, HashMap<String, Integer> nodeIdComBegin){
        if(!graph.isDirected()){  
            modulUnDir = new ModularityUndirect();
            matrixModGN = new double[graph.getNodeCount()][graph.getNodeCount()];
            modulUnDir.initializeMatrixMod(graph, nodes, matrixModGN, nodeIdComBegin);          
        }else{          
            modulDir = new ModularityDirect();
            matrixModLN = new double[graph.getNodeCount()][graph.getNodeCount()];        
            modulDir.initializeMatrixModDirect(graphDir, nodes, matrixModLN, nodeIdComBegin);         
        }
    }  
       
    
    
    /*
    Bestimmung der aktuell entdeckten Communities und deren Anzahl in Vector
    für HTML-Report speichern
    */
    public void determineDiscoveredCommunities(Node[] nodes){           
            giveComOut = new TreeMap<>(); 
            giveComOut = detCommunities.giveCommunities(nodes, giveComOut, col);
            int comCount1 = giveComOut.size(); 
            communityCountList.add(comCount1);           
    }
    

    /*
    aktuelle Modularität berechnen und in Vector für HTML-Report speichern   
    */
    private void calculateModularity(Node[] nodes, Graph graph, DirectedGraph graphDir, HashMap<String, Integer> nodeIdComBegin){
            if(!graph.isDirected()){               
                modularity = modulUnDir.modulCalcUndirect(graph, giveComOut, nodeIdComBegin, matrixModGN);                             
                modularityList.add(modularity);               
            }       
            else{                              
                modularity = modulDir.modulCalcDirect(graphDir, giveComOut, nodeIdComBegin, matrixModLN);
                modularityList.add(modularity);              
            }           
    }   
  
    
    /*
    Vorbereitung der Ausgabe der Communities und deren Mitglieder für HTML-Report
    */    
    private void prepareOutputCommunities(Graph graph){   
        comNumb = 0;
        comNumb = giveComOut.size();
        int comCount = 1;
        for (Integer elem : giveComOut.keySet()){
            TreeMap<String, String> help = new TreeMap<>();
            help = giveComOut.get(elem);
            int countMembers = help.size();
            for(String elem2: help.keySet()){
                String help2 = help.get(elem2);
                communitiesHelp = communitiesHelp+help2+", ";
                Node node = graph.getNode(elem2);
                node.setAttribute(col, comCount);                
            }  
            communitiesHelp = communitiesHelp.substring(0,communitiesHelp.length()-2); //letzte Komma entfernen
            communities = communities+"<br/><br/>"+comCount+". Community hat folgende " + countMembers+ "  Mitglieder: "+ "<br/>"+communitiesHelp;           
            communitiesHelp = "";
            comCount += 1;    
        }
    }
    
    
    
    /*
    Gibt den Benutzern die Möglichkeit, den Kanten innerhalb der Communities ein
    höheres Gewicht zuzuweisen. Dies hat den Vorteil, dass ein Layout-Algorithmus, welcher
    die Kantengewichte berücksichtigt, die Knoten innerhalb eines Community benachbart
    darstellen kann.
    */
    private void askAdaptEdgeWeight(Graph graph){        
        detCommunities.resetEdgeWeight(graph);//allen Kanten das Gewicht 1.0 zuweisen      
        JFrame jf= new JFrame();
        jf.setAlwaysOnTop(true);//Parent von JOptionPane, damit diese im Vordergrund erscheint
        int dialogButton = JOptionPane.showOptionDialog(jf, "Drücken Sie Ja, um Kanten innerhalb einer Komponente \n mehr Gewicht zu geben"
                , "Kantengewichte anpassen?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null, null, null);
        if(dialogButton == JOptionPane.YES_OPTION){
             //Innere Kanten der Communities mit einem höheren Gewicht versehen           
            detCommunities.setEdgeWeightForInnerEdges(graph, giveComOut, col);           
        }       
    }
         
    /*
    Erstellt Charts für HTML-Report
    */
    private void createCharts(Graph graph){
        
        boolean autoSort = false; 
        XYSeries series1 = new XYSeries("Iteration x", autoSort);;
        XYSeries series2 = new XYSeries("Iteration x");;
        XYSeries series3 = new XYSeries("Iteration x");//Anzahl Communities
        
        double minModul = modularityList.get(0);
        double maxModul = modularityList.get(0);                   
        for(int i=0; i < communityCountList.size();i++){
            int iteration = i+1;
            series1.add(communityCountList.get(i), modularityList.get(i));
            series3.add(iteration, communityCountList.get(i));//für Chart Iteration-Anzahl Communities
            series2.add(iteration, modularityList.get(i));//für Chart Iteration-GN
            if(modularityList.get(i) > maxModul){
                maxModul = modularityList.get(i); 
            }else if(modularityList.get(i) < minModul){
                minModul = modularityList.get(i);
            }
        }  
        //für die Ausgabe der minimalen und der maximalen Modularität 
        minModularity = "(minimale Modularität: "+minModul+")";
        maxModularity = "(maximale Modularität: "+maxModul+")";
                
        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(series1); 
                
        XYSeriesCollection dataset3 = new XYSeriesCollection();
        dataset3.addSeries(series3);
        
        XYSeriesCollection dataset5 = new XYSeriesCollection();
        dataset5.addSeries(series2);         
           
        //erstellen des Charts "Modularität bei fortschreitender Iteration"
        XYLineAndShapeRenderer dot2 = new XYLineAndShapeRenderer();        
        NumberAxis xIteration = new NumberAxis("Iteration");  
        xIteration.setLabelFont(new Font("Arial", Font.PLAIN, 14));
        xIteration.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis yModularity = new NumberAxis("Modularität");
        yModularity.setLabelFont(new Font("Arial", Font.PLAIN, 14));
        yModularity.setUpperBound(1.0);
        XYPlot plot2 = new XYPlot(dataset5, xIteration, yModularity, dot2);
        JFreeChart chart3 = new JFreeChart(plot2);
        chart3.removeLegend();
        chart3.setTitle("Modularität bei fortschreitender Iteration");
        imageFile2 = ChartUtils.renderChart(chart3, "iteration-modularity.png");
        
        //erstellen des Charts "Anzahl entdeckter Communities bei fortschreitender Iteration" 
        XYLineAndShapeRenderer dot3 = new XYLineAndShapeRenderer();
        NumberAxis xIteration2 = new NumberAxis("Iteration");
        xIteration2.setLabelFont(new Font("Arial", Font.PLAIN, 14));
        xIteration2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis yNumberCom = new NumberAxis("Anzahl Communities");
        yNumberCom.setLabelFont(new Font("Arial", Font.PLAIN, 14));
        yNumberCom.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYPlot plot3 = new XYPlot(dataset3, xIteration2, yNumberCom, dot3);
        JFreeChart chart4 = new JFreeChart(plot3);
        chart4.removeLegend();
        chart4.setTitle("Anzahl entdeckter Communities bei fortschreitender Iteration");
        imageFile3 = ChartUtils.renderChart(chart4, "iteration-NumbComs.png");
        
        //erstellen des Charts "Mitgliederanzahl der Communities"
        XYSeries series6 = new XYSeries("");
        XYSeriesCollection dataset4 = new XYSeriesCollection();
        Integer com = 1;
        for(Integer elem: giveComOut.keySet()){
            TreeMap<String, String> help = new TreeMap<String, String>();
            help = giveComOut.get(elem);
            Integer z = help.size();
            series6.add(com, z);
            com += 1;
        }
        dataset4.addSeries(series6);
        XYLineAndShapeRenderer dot4 = new XYLineAndShapeRenderer();
        NumberAxis xCommunity = new NumberAxis("Community");
        xCommunity.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis xMembers = new NumberAxis("Mitgliederanzahl");
        xMembers.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYPlot plot4 = new XYPlot(dataset4, xCommunity, xMembers, dot4);
        JFreeChart chart5 = new JFreeChart(plot4);
        chart5.removeLegend();
        chart5.setTitle("Mitgliederanzahl der Communities");
        imageFile4 = ChartUtils.renderChart(chart5, "MemberComs.png");
    }
        

    @Override
    public String getReport() {
        String report = "<HTML> <BODY> <h1>Label Propagation Report </h1> "
                + "<hr>"  
                + "<br> " + message 
                + "<br />"
                + "<br> Anzahl der gefundenen Communities: " + comNumb
                + "<br />"                 
                +"<br>" + imageFile2
                +"<br />"
                +"<br>" + minModularity
                +"<br />"
                +"<br>" + maxModularity
                +"<br />"
                 +"<br>" + imageFile3
                +"<br />"
                +"<br>" + imageFile4
                +"<br />"
                + "<br>Gefundene Communities und deren Mitglieder: \n" + communities             
                + "<br />"
                + "</BODY></HTML>";
        return report;
    }
    /**
     * Ergebnis als String
     * @return  modularity
     */
    String getValue() {
        NumberFormat nfm = DecimalFormat.getNumberInstance();
        nfm.setMaximumFractionDigits(3);
        return nfm.format(modularity);
    }

    
}
