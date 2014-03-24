package libalg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

public class FloydWarshall {
public double[][] distTo;  // distTo[v][w] = length of shortest v->w path
public Edge[][] edgeTo;  // edgeTo[v][w] = last edge on shortest v->w path

/**
* Computes a shortest paths tree from each vertex to to every other vertex in
* the edge-weighted digraph <tt>G</tt>. If no such shortest path exists for
* some pair of vertices, it computes a negative cycle.
* @param G the edge-weighted digraph
*/
public FloydWarshall(Graph G) {
int V = G.getVertexes().size();
distTo = new double[V][V];
edgeTo = new Edge[V][V];

// initialize distances to infinity
for (int v = 0; v < V; v++) {
for (int w = 0; w < V; w++) {
distTo[v][w] = Double.POSITIVE_INFINITY;
}
}

// initialize distances using edge-weighted digraph's
for (int v = 0; v <  V ; v++) {
for (Edge e : G.getEdges()) {
distTo[Integer.parseInt(e.getSource().getId())-1][Integer.parseInt(e.getDestination().getId())-1] = e.getWeight();
edgeTo [Integer.parseInt(e.getSource().getId())-1][Integer.parseInt(e.getDestination().getId())-1] = e;
}
// in case of self-loops
if (distTo[v][v] >= 0.0) {
distTo[v][v] = 0.0;
edgeTo[v][v] = null;
}
}

// Floyd-Warshall updates
for (int i = 0; i < G.getVertexes().size(); i++) {
// compute shortest paths using only 0, 1, ..., i as intermediate vertices
for (int v = 0; v < V; v++) {
if (edgeTo[v][i] == null) continue;  // optimization
for (int w = 0; w < V; w++) {
if (distTo[v][w] > distTo[v][i] + distTo[i][w]) {
distTo[v][w] = distTo[v][i] + distTo[i][w];
edgeTo[v][w] = edgeTo[i][w];
}
}
}
}
}

/**
* Is there a negative cycle?
* @return <tt>true</tt> if there is a negative cycle, and <tt>false</tt> otherwise
*/

/**
* Returns a negative cycle, or <tt>null</tt> if there is no such cycle.
* @return a negative cycle as an iterable of edges,
* or <tt>null</tt> if there is no such cycle
*/

/**
* Is there a path from the vertex <tt>s</tt> to vertex <tt>t</tt>?
* @param s the source vertex
* @param t the destination vertex
* @return <tt>true</tt> if there is a path from vertex <tt>s</tt>
* to vertex <tt>t</tt>, and <tt>false</tt> otherwise
*/
public boolean hasPath(int s, int t) {
return distTo[s][t] < Double.POSITIVE_INFINITY;
}

/**
* Returns the length of a shortest path from vertex <tt>s</tt> to vertex <tt>t</tt>.
* @param s the source vertex
* @param t the destination vertex
* @return the length of a shortest path from vertex <tt>s</tt> to vertex <tt>t</tt>;
* <tt>Double.POSITIVE_INFINITY</tt> if no such path
* @throws UnsupportedOperationException if there is a negative cost cycle
*/
public double dist(int s, int t) {
return distTo[s][t];
}

/**
* Returns a shortest path from vertex <tt>s</tt> to vertex <tt>t</tt>.
* @param s the source vertex
* @param t the destination vertex
* @return a shortest path from vertex <tt>s</tt> to vertex <tt>t</tt>
* as an iterable of edges, and <tt>null</tt> if no such path
* @throws UnsupportedOperationException if there is a negative cost cycle
*/
public Iterable<Edge> path(int s, int t) {
if (!hasPath(s, t)) return null;
Stack<Edge> path = new Stack<Edge>();
for (Edge e = edgeTo[s][t]; e != null; e = edgeTo[s][Integer.parseInt(e.getSource().getId())]) {
path.push(e);
}
return path;
}
/*
// check optimality conditions
private boolean check(EdgeWeightedDigraph G, int s) {

// no negative cycle
if (!hasNegativeCycle()) {
for (int v = 0; v < G.V(); v++) {
for (Edge e : G.adj(v)) {
int w = e.to();
for (int i = 0; i < G.V(); i++) {
if (distTo[i][w] > distTo[i][v] + e.weight()) {
System.err.println("edge " + e + " is eligible");
return false;
}
}
}
}
}
return true;
}
*/

/**
* Unit tests the <tt>FloydWarshall</tt> data type.
*/
public static void main(String[] args) {

ConstructGraph CG = new ConstructGraph();
Graph G = CG.graph;

// run Floyd-Warshall algorithm
FloydWarshall spt = new FloydWarshall(G);

// print all-pairs shortest path distances
System.out.print("  ");
//System.out.print("*");
// System.out.print("\t");
// for (int v = 0; v < G.getVertexes().size(); v++) {
// System.out.print(" "+(v+1));
// System.out.print("\t");
// }
System.out.println();
/*for (int v = 0; v < G.getVertexes().size(); v++) {
       //System.out.print(" "+(v+1));
       //System.out.print("\t");
for (int w = 0; w < G.getVertexes().size(); w++) {
if (spt.hasPath(v, w)) { System.out.print(" "+spt.dist(v, w)); System.out.print("\t");}
else { System.out.print(" Inf "); System.out.print("\t");}
}
System.out.println();
}*/

for (int v = 0; v < 10; v++) {
       //System.out.print(" "+(v+1));
       //System.out.print("\t");
for (int w = 0; w < 10; w++) {
if (spt.hasPath(v, w)) { System.out.print(" "+spt.dist(v, w)); System.out.print("\t");}
else { System.out.print(" Inf "); System.out.print("\t");}
}
System.out.println();
}

// write the matrix to a file
   try {
       write(spt.distTo, "dump.txt");
   } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
   }
}

public static void write(double[][] data, Writer baseWriter) throws IOException {
   int rows = data.length;
   if (rows==0) { return; }
   int cols = data[0].length;
   BufferedWriter writer = new BufferedWriter(baseWriter);
   writer.write("" + rows); writer.newLine();
   writer.write("" + cols); writer.newLine();
   for(int row=0; row<rows; row++) {
       for(int col=0; col<cols; col++) {
           writer.write(data[row][col]+" ");
           //writer.write("\t");
           writer.newLine();
       }

   }
   writer.flush();
}

public static void write(double[][] data, String fileName) throws IOException {
   Writer writer = new FileWriter(fileName);
   write(data, writer);
   writer.close();
}

}
