package libalg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class TSP
{
    private int numberOfNodes;
    private Stack<Integer> stack;
    public static double[][] adjacency_matrix;
    public static ArrayList<Integer> sectors;
    
    public static int[] path;
 
    public TSP()
    {
        stack = new Stack<Integer>();
    }
 
    public void tsp(double adjacencyMatrix[][])
    {
        numberOfNodes = adjacencyMatrix[1].length - 1;
        int[] visited = new int[numberOfNodes + 1];
        visited[0] = 1;
        stack.push(0);
        int element, dst = 0, i;
        double min = Double.MAX_VALUE;
        boolean minFlag = false;
        System.out.print(1 + "\t");
 
        while (!stack.isEmpty())
        {
            element = stack.peek();
            i = 1;
            min = Double.MAX_VALUE;
            while (i <= numberOfNodes)
            {
                if (adjacencyMatrix[element][i] > 1 && visited[i] == 0)
                {
                    if (min > adjacencyMatrix[element][i])
                    {
                        min = adjacencyMatrix[element][i];
                        dst = i;
                        minFlag = true;
                    }
                }
                i++;
            }
            if (minFlag)
            {
                visited[dst] = 1;
                stack.push(dst);
                System.out.print(sectors.get(dst) + "\t");
                
                minFlag = false;
                continue;
            }
            stack.pop();
        }
    }
 
    
    public void tsp(final double[][] distanceMatrix, final int startCity) {
        
        path = new int[distanceMatrix[0].length];
        
        path[0] = startCity;
        int currentCity = startCity;
        
        /**
         * until there are cities that are not yet been visited
         */
        int i = 1;
        while (i < path.length) {
                // find next city
        	System.out.print(distanceMatrix[currentCity-1][0]);
                int nextCity = findMin(distanceMatrix[currentCity-1]);
                //System.out.println(nextCity);
                // if the city is not -1 (meaning if there is a city to be visited
                if(nextCity != -1) {
                        // add the city to the path
                        path[i] = nextCity;
                        // update currentCity and i
                        currentCity = nextCity;
                        i++;
                }
        }
        
        //for(int k=0;k<path.length;k++) System.out.print(path[k]+" ");
}
    public static void main(String... arg) throws IOException
    {
        int row;
        sectors = new ArrayList<Integer>();
        Scanner scanner = null;
        try
        {
        	double[][] data2 = getDoubleTwoDimArray("dump.txt");

            System.out.println("Enter the nodes");
            scanner = new Scanner(System.in);
            row = scanner.nextInt();
            while(row!=-1){
            	if(!sectors.contains(row)){
            	sectors.add(row);
            	}
            	row = scanner.nextInt();
            }
            if(sectors.get(0)!=1) sectors.add(1);
            Collections.sort(sectors);
            
            adjacency_matrix = new double[sectors.size() + 1][sectors.size() + 1];
            
            createAdj(data2, sectors);
            
            /*for (int i = 1; i <= number_of_nodes; i++)
            {
                for (int j = 1; j <= number_of_nodes; j++)
                {
                    adjacency_matrix[i][j] = scanner.nextDouble();
                }
            }*/
            for(int i=0;i<sectors.size();i++)
        	{
        		System.out.println();
        		for(int j=0;j<sectors.size();j++)
        		{
        			System.out.print(adjacency_matrix[i][j]+" ");
        		}
        	}
            
            /*for (int i = 1; i <= sectors.size(); i++)
            {
                for (int j = 1; j <= sectors.size(); j++)
                {
                    if (adjacency_matrix[i][j] == 1 && adjacency_matrix[j][i] == 0)
                    {
                        adjacency_matrix[j][i] = 1;
                    }
                }
            }*/
            System.out.println("the citys are visited as follows");
            TSP tspNearestNeighbour = new TSP();
            //tspNearestNeighbour.tsp(adjacency_matrix, 1);
            BranchAndBound bnb = new BranchAndBound(adjacency_matrix,0, sectors);
            String result = bnb.execute();
            System.out.println(result);
            
            
        } catch (InputMismatchException inputMismatch)
         {
             System.out.println("Wrong Input format");
         }
        scanner.close();
    }
    
    // create a distance matrix according to the input
    public static void createAdj(double[][] input, ArrayList<Integer> sectors){
    	
    	double[] row = new double[input.length];
    	int i=0;
    	
    	while(i<sectors.size()){
    		row = input[sectors.get(i)-1];
    		int j=0;
    		while(j<sectors.size()){
    			adjacency_matrix[i][j]=row[sectors.get(j)-1];
    			j++;
    		}
    		i++;
    	}
    	
    	
    }
    
    public static double[][] getDoubleTwoDimArray(Reader baseReader) throws IOException {
		BufferedReader reader = new BufferedReader(baseReader);
		int rows = Integer.parseInt(reader.readLine());
		int cols = Integer.parseInt(reader.readLine());
		double [][] data = new double[rows][cols];
		for(int row=0; row<rows; row++) {
			for(int col=0; col<cols; col++) {
				data[row][col] = Double.parseDouble(reader.readLine());
			}
		}
		return data;
	}

	public static double[][] getDoubleTwoDimArray(String fileName) throws IOException {
		Reader reader = new FileReader(fileName);
		double[][] data = getDoubleTwoDimArray(reader);
		reader.close();
		return data;
	}
	
	//////////
	private int findMin(double[] row) {
        
        int nextCity = -1;
        int i = 0;
        double min = Double.MAX_VALUE;
        
        while(i < row.length)  {
                if(isCityInPath(path, i) == false && row[i] < min && row[i]>0) {
                        min = row[i];
                        nextCity = i;
                }
                i++;
        }
        return nextCity;
}
	
	
	
	public int computeCost(int[] path, int[][] distanceMatrix) {
        int cost = 0;
        for(int i = 1; i < path.length; i++) {
                cost += distanceMatrix[path[i-1]][path[i]];
        }
        cost += distanceMatrix[path[path.length - 1]] [path[0]];
        return cost;
}

/**
 * @param path
 * @param srcIndex
 * @return destination city of an edge given the source index
 */
public int getDestination(int[] path, int srcIndex) {
        if(srcIndex + 1 == path.length) {
                return path[0];
        }
        return path[srcIndex + 1];
}

/**
 * @param path
 * @param srcIndex
 * @return destination index of an edge given the source index
 */
public int getIndexOfDestination(int[] path, int srcIndex) {
        if(srcIndex + 1 == path.length) {
                return 0;
        }
        return srcIndex + 1;
}


/**
 * Check if the city is in the path
 * @param city
 * @return true: if the city is already in the path, false otherwise
 */
public boolean isCityInPath(int[] path, int city) {
        for(int i = 0; i < path.length; i++) {
                if(path[i] == city) {
                        return true;
                }
        }
        return false;
}
	///////////
}