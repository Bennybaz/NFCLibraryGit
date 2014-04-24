package libalg;

import java.util.ArrayList;

public class BranchAndBound {
    
    int sourceCity;
    String result = new String();
    double[][] adjacency_matrix;
    ArrayList<Integer> sectors;
    
    
    ArrayList<Integer> initialRoute, optimumRoute, toPrint;
    int nodes = 0;
    int routeCost = 0;
    int optimumCost = Integer.MAX_VALUE;    
    
    /** Creates a new instance of BranchAndBound */
    public BranchAndBound(double[][] matrix, int sourceCity, ArrayList<Integer> order) {
    	
    	adjacency_matrix = new double[matrix.length][matrix.length];
    	sectors = (ArrayList<Integer>)order.clone();
        
    	for(int i=0; i< matrix.length; i++ ) 
            for(int j=0; j< matrix.length; j++ ) 
                    adjacency_matrix[i][j] = matrix[i][j];
    	
        this.sourceCity = sourceCity;
    }
    
    /**
     * executes the algorithm
     */
    public String execute () {
        
        initialRoute = new ArrayList<Integer>();
        initialRoute.add(sourceCity);
        optimumRoute = new ArrayList<Integer>();
        toPrint = new ArrayList<Integer>();
        nodes++;        
        
        result = "-------------------------------------\n";
        result +=  "BRANCH AND BOUND:\n";
        result += "-------------------------------------\n";        
        
        long startTime = System.currentTimeMillis();
        search(sourceCity, initialRoute);
        long endTime = System.currentTimeMillis();     
        
        System.out.println(optimumRoute.size());
        for(int k=0;k<optimumRoute.size();k++){ 
        	toPrint.add(sectors.get((int) optimumRoute.get(k)));
        	System.out.println(k);
        }
        result += "BEST SOLUTION: \t"+toPrint.toString() + "\nCOST: \t\t"+optimumCost+"\n";
        result += "NODES VISITED: \t"+nodes+"\n";
        result += "TIME PASSED: \t"+(endTime-startTime)+" ms\n";
        result += "-------------------------------------\n";
        
        return result;         
    }

    public ArrayList<Integer> execute2 () {

        initialRoute = new ArrayList<Integer>();
        initialRoute.add(sourceCity);
        optimumRoute = new ArrayList<Integer>();
        toPrint = new ArrayList<Integer>();
        nodes++;

        long startTime = System.currentTimeMillis();
        search(sourceCity, initialRoute);
        long endTime = System.currentTimeMillis();

        System.out.println(optimumRoute.size());
        for(int k=0;k<optimumRoute.size();k++){
            toPrint.add(sectors.get((int) optimumRoute.get(k)));
            //System.out.println(k);
        }
        //result += "BEST SOLUTION: \t"+toPrint.toString() + "\nCOST: \t\t"+optimumCost+"\n";
        //result += "NODES VISITED: \t"+nodes+"\n";
        //result += "TIME PASSED: \t"+(endTime-startTime)+" ms\n";
        //result += "-------------------------------------\n";

        //return result;
        return toPrint;
    }
    
    
    /**
     * @param from node where we start the search.
     * @param route followed route for arriving to node "from".
     */
    public void search (int from, ArrayList<Integer> followedRoute) {
        
        // we've found a new solution
        if (followedRoute.size() == adjacency_matrix.length-1) {
            
            followedRoute.add(sourceCity);
            nodes++;
            
            // update the route's cost
            routeCost += adjacency_matrix[from][sourceCity];
            
            if (routeCost < optimumCost) {
                optimumCost = routeCost;
                optimumRoute = (ArrayList<Integer>)followedRoute.clone();
            }
            
            // update the route's cost (back to the previous value)
            routeCost -= adjacency_matrix[from][sourceCity];
        }
        else {
            for (int to=0; to<adjacency_matrix.length-1; to++){
                if (!followedRoute.contains(to)) {
                    
                    // update the route's cost
                    routeCost += adjacency_matrix[from][to];
                    
                    if (routeCost < optimumCost) { 
                        ArrayList<Integer> increasedRoute = (ArrayList<Integer>)followedRoute.clone();
                        increasedRoute.add(to);
                        nodes++;
                        search(to, increasedRoute);    
                    }
                    
                    // update the route's cost (back to the previous value)
                    routeCost -= adjacency_matrix[from][to];
                }
            }
        }        
    }
    
}