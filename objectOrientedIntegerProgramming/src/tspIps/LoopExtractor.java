package tspIps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class LoopExtractor {
	
	/**
	 * 
	 * @param graph
	 * @param edgesSelected will be mutated
	 * @return
	 */
	public static <V,E> List<List<E>> subTours(UndirectedGraph<V,E> graph, Set<E> edgesSelected){
		List<List<E>> ans = new ArrayList<List<E>>();
		
		while(!edgesSelected.isEmpty()){
			List<E> loop = new ArrayList<E>();
			E first = edgesSelected.iterator().next();
			loop.add(first);
			edgesSelected.remove(first);
			Pair<V> endpoints = graph.getEndpoints(first);			
			V source = endpoints.getFirst();
			V next = endpoints.getSecond();
			while(next != source){
				boolean foundNext = false;
				E nextEdge = null;
				for(E e: graph.getIncidentEdges(next)){
					if(edgesSelected.contains(e)){
						if(foundNext){
							throw new RuntimeException();
						}
						foundNext = true;
						nextEdge = e;						
					}
				}
				if(foundNext){
					edgesSelected.remove(nextEdge);
					loop.add(nextEdge);
					next = graph.getOpposite(next, nextEdge);
				}
				else{
					throw new RuntimeException();
				}
			}
			ans.add(loop);
		}
		return ans;
	}

}
