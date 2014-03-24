package libalg;

public class ConstructMatrix {

	private int [][]neighbor;
	private int [][]adj;
	
    ConstructMatrix()
    {
    	neighbor = new int[97][97];
    	adj = new int[96][96];
    	for(int i=0;i<=96;i++)
    		for(int j=0;j<=96;j++)
    		neighbor[i][j]=0;
    	
    	for(int i=5;i<=85;i+=8)
    	{
    		if(i<=37)
    		{
    			neighbor[i][i-4]=1;
    			neighbor[i-4][i]=1;
    		}
    		else
    		{
    			neighbor[i+3][i-1]=1;
    			neighbor[i-1][i+3]=1;
    		}
    		
    		
    		neighbor[i][i+1]=1;
    		neighbor[i][i+4]=1;
    		neighbor[i][i+5]=1;	
    		neighbor[i][i+6]=1;
    		neighbor[i][i+7]=1;  		
    		neighbor[i+1][i]=1;
    		neighbor[i+4][i]=1;
    		neighbor[i+5][i]=1;
    		neighbor[i+6][i]=1;
    		neighbor[i+7][i]=1;
    		
    		neighbor[i+1][i+2]=1;
    		neighbor[i+1][i+4]=1;
    		neighbor[i+1][i+5]=1;
    		neighbor[i+1][i+6]=1;
    		neighbor[i+1][i+7]=1;  		
    		neighbor[i+2][i+1]=1;
    		neighbor[i+4][i+1]=1;
    		neighbor[i+5][i+1]=1;
    		neighbor[i+6][i+1]=1;
    		neighbor[i+7][i+1]=1;
    		
    		neighbor[i+2][i+3]=1;
    		neighbor[i+2][i+4]=1;
    		neighbor[i+2][i+5]=1;
    		neighbor[i+2][i+6]=1;
    		neighbor[i+2][i+7]=1; 		
    		neighbor[i+3][i+2]=1;
    		neighbor[i+4][i+2]=1;
    		neighbor[i+5][i+2]=1;
    		neighbor[i+6][i+2]=1;
    		neighbor[i+7][i+2]=1;
    		
    		neighbor[i+3][i+4]=1;
    		neighbor[i+3][i+5]=1;
    		neighbor[i+3][i+6]=1;
    		neighbor[i+3][i+7]=1;		
    		neighbor[i+4][i+3]=1;
    		neighbor[i+5][i+3]=1;
    		neighbor[i+6][i+3]=1;
    		neighbor[i+7][i+3]=1;
    		
    		neighbor[i+4][i+5]=1;
    		neighbor[i+5][i+4]=1;
    		neighbor[i+5][i+6]=1;
    		neighbor[i+6][i+5]=1;
    		neighbor[i+6][i+7]=1;
    		neighbor[i+7][i+6]=1;
    		
    		if(i==37)
            	i=45;
    		
    	}
    	
    	neighbor[1][2]=1;
    	neighbor[2][1]=1;
    	neighbor[2][3]=1;
    	neighbor[3][2]=1;
    	neighbor[3][4]=1;
    	neighbor[4][3]=1;
    	neighbor[45][96]=1;
    	neighbor[96][45]=1;
    	neighbor[45][46]=1;
    	neighbor[46][45]=1;
    	//
    	neighbor[45][41]=1;
    	neighbor[41][45]=1;
    	
    	neighbor[96][92]=1;
    	neighbor[92][96]=1;
    	//
    	neighbor[46][47]=1;
    	neighbor[47][46]=1;
    	neighbor[47][48]=1;
    	neighbor[48][47]=1;
    	
    	neighbor[1][52]=1;
    	neighbor[52][1]=1;
    	
    	neighbor[49][50]=1;
    	neighbor[50][49]=1;
    	neighbor[50][51]=1;
    	neighbor[51][50]=1;
    	neighbor[51][52]=1;
    	neighbor[52][51]=1;
    	
    	neighbor[93][94]=1;
    	neighbor[94][93]=1;
    	neighbor[94][95]=1;
    	neighbor[95][94]=1;
    	neighbor[95][96]=1;
    	neighbor[96][95]=1;
    	   	
    	for(int i=0;i<96;i++)
    	{
    		for(int j=0;j<96;j++)
    		{
    			adj[i][j]=neighbor[i+1][j+1];
    		}
    	}
    }
    
    public void printADJ()
    {
    	for(int i=0;i<96;i++)
    	{
    		System.out.println();
    		for(int j=0;j<96;j++)
    		{
    			System.out.print(adj[i][j]+" ");
    		}
    	}
    }
    
    public static void main(String[]args)
    {
    	ConstructMatrix c=new ConstructMatrix();
    	c.printADJ();
    }
    
}
