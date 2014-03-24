package libalg;

import java.util.ArrayList;
import java.util.List;

//import org.junit.Test;

//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;

public class ConstructGraph {

    private List<Vertex> nodes;
    private List<Edge> edges;
    Graph graph;

    public ConstructGraph() {
        nodes = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        
        int j=0;
        for (int i = 1; i < 97; i++) {
            Vertex location = new Vertex(""+i, "Node_" + i);
            nodes.add(location);
        }
        
        for(int i=5;i<=85;i+=8) //left , internal edges when 5<=i<=37; right, internal edges when 53<=i<=85
        {
        	if(i<=37)
    		{
    			addLane(""+i+","+(i-4), i, i-4, 140);
    			addLane(""+(i-4)+","+i, i-4, i, 140);
    		}
    		else
    		{
    			addLane(""+(i+3)+","+(i-1), i+3, i-1, 140);
    			addLane(""+(i-1)+","+(i+3), i-1, i+3, 140);
    		}
        	/*			the source is marked with ***i***
        	 * 			i+7 | i+6 | i+5 | i+4
        	 * 
        	 * 			i+3 | i+2 | i+1 | ***i***
        	 */     // out
        			addLane(""+i+","+(i+1), i, i+1, 90);
	                addLane(""+i+","+(i+4), i, i+4, 80);
	                addLane(""+i+","+(i+5), i, i+5, 130);
	                addLane(""+i+","+(i+6), i, i+6, 200);
	                addLane(""+i+","+(i+7), i, i+7, 285);
	                // in
	                addLane(""+(i+1)+","+i, i+1, i, 90);
	                addLane(""+(i+4)+","+i, i+4, i, 80);
	                addLane(""+(i+5)+","+i, i+5, i, 130);
	                addLane(""+(i+6)+","+i, i+6, i, 200);
	                addLane(""+(i+7)+","+i, i+7, i, 285);      
	              

                /*			the source is marked with ***i***
            	 * 			i+7 | i+6 | i+5 | i+4
            	 * 
            	 * 			i+3 | i+2 | ***i+1*** | i
            	 */ // out
	                addLane(""+(i+1)+","+(i+2), i+1, i+2, 90);
	                addLane(""+(i+1)+","+(i+4), i+1, i+4, 130);
	                addLane(""+(i+1)+","+(i+5), i+1, i+5, 80);
	                addLane(""+(i+1)+","+(i+6), i+1, i+6, 130);
	                addLane(""+(i+1)+","+(i+7), i+1, i+7, 200);
	               // in
	                addLane(""+(i+2)+","+(i+1), i+2, i+1, 90);
	                addLane(""+(i+4)+","+(i+1), i+4, i+1, 130);
	                addLane(""+(i+5)+","+(i+1), i+5, i+1, 80);
	                addLane(""+(i+6)+","+(i+1), i+6, i+1, 130);
	                addLane(""+(i+7)+","+(i+1), i+7, i+1, 200);  
                /*			the source is marked with ***i***
            	 * 			i+7 | i+6 | i+5 | i+4
            	 * 
            	 * 			i+3 | ***i+2*** | i+1 | i
            	 */ // out	                
	                addLane(""+(i+2)+","+(i+3), i+2, i+3, 90);
	                addLane(""+(i+2)+","+(i+4), i+2, i+4, 200);
	                addLane(""+(i+2)+","+(i+5), i+2, i+5, 130);
	                addLane(""+(i+2)+","+(i+6), i+2, i+6, 80);
	                addLane(""+(i+2)+","+(i+7), i+2, i+7, 130);
	                // in
	                addLane(""+(i+3)+","+(i+2), i+3, i+2, 90);
	                addLane(""+(i+4)+","+(i+2), i+4, i+2, 200);
	                addLane(""+(i+5)+","+(i+2), i+5, i+2, 130);
	                addLane(""+(i+6)+","+(i+2), i+6, i+2, 80);
	                addLane(""+(i+7)+","+(i+2), i+7, i+2, 130); 
                /*			the source is marked with ***i***
            	 * 			i+7 | i+6 | i+5 | i+4
            	 * 
            	 * 			***i+3*** | i+2 | i+1 | i
            	 */ // out 
	                addLane(""+(i+3)+","+(i+4), i+3, i+4, 285);
	                addLane(""+(i+3)+","+(i+5), i+3, i+5, 200);
	                addLane(""+(i+3)+","+(i+6), i+3, i+6, 130);
	                addLane(""+(i+3)+","+(i+7), i+3, i+7, 80);
	                // in
	                addLane(""+(i+4)+","+(i+3), i+4, i+3, 285);
	                addLane(""+(i+5)+","+(i+3), i+5, i+3, 200);
	                addLane(""+(i+6)+","+(i+3), i+6, i+3, 130);
	                addLane(""+(i+7)+","+(i+3), i+7, i+3, 80);
                /*			handling the connection between neighbor sectors in the upper book stand
            	 * 			i+7 | i+6 | i+5 | i+4
            	 * 
            	 * 			i+3 | i+2 | i+1 | i
            	 */   
	                addLane(""+(i+4)+","+(i+5), i+4, i+5, 90); //out
	                addLane(""+(i+5)+","+(i+4), i+5, i+4, 90); //in
	                addLane(""+(i+5)+","+(i+6), i+5, i+6, 90); //out
	                addLane(""+(i+6)+","+(i+5), i+6, i+5, 90); //in
	                addLane(""+(i+6)+","+(i+7), i+6, i+7, 90); //out
	                addLane(""+(i+7)+","+(i+6), i+7, i+6, 90); //in
	                
	                //move to the right
	                if(i==37)
	                	i=45;
        	}
        	//left, special edges
			addLane("1,2", 1, 2, 90);
			addLane("2,1", 2, 1, 90);
			addLane("2,3", 2, 3, 90);
			addLane("3,2", 3, 2, 90);
			addLane("3,4", 3, 4, 90);
			addLane("4,3", 4, 3, 90);
			//!!!!!!need to add edge between the left-down book stand to the right-down book stand!!!!!!!! and the reverse one
			addLane("45,96", 45, 96, 1320);
			addLane("96,45", 96, 45, 1320);
			//
			addLane("45,46", 45, 46, 90);
			addLane("46,45", 46, 45, 90);
			addLane("46,47", 46, 47, 90);
			addLane("47,46", 47, 46, 90);
			addLane("47,48", 47, 48, 90);
			addLane("48,47", 48, 47, 90);
			//!!!!!!need to add edge between the left-up book stand to the right-up book stand!!!!!!!! and the reverse one
			addLane("1,52", 1, 52, 1320);
			addLane("52,1", 52, 1, 1320);
			//
			//right, special edges
			addLane("49,50", 49, 50, 90);
			addLane("50,49", 50, 49, 90);
			addLane("50,51", 50, 51, 90);
			addLane("51,50", 51, 50, 90);
			addLane("51,52", 51, 52, 90);
			addLane("52,51", 52, 51, 90);
			
			addLane("93,94", 93, 94, 90);
			addLane("94,93", 94, 93, 90);
			addLane("94,95", 94, 95, 90);
			addLane("95,94", 95, 94, 90);
			addLane("95,96", 95, 96, 90);
			addLane("96,95", 96, 95, 90);

	    	addLane("45,41", 45, 41, 140);
			addLane("41,45", 41, 45, 140);

	    	addLane("96,92", 96, 92, 140);
			addLane("92,96", 92, 96, 140);
			
			graph = new Graph(nodes, edges);
			
	    	
        }
   	
        
        // Lets check from location Loc_1 to Loc_10

        /*DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        dijkstra.execute(nodes.get(0));
        LinkedList<Vertex> path = dijkstra.getPath(nodes.get(10));

        //assertNotNull(path);
        //assertTrue(path.size() > 0);

        for (Vertex vertex : path) {
            System.out.println(vertex);
        }
*/
    private void addLane(String laneId, int sourceLocNo, int destLocNo,
                         int duration) {
        Edge lane = new Edge(laneId,nodes.get(sourceLocNo-1), nodes.get(destLocNo-1), duration);
        edges.add(lane);
    }
} 