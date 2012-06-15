package minCut;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;

public interface MinCutSolver<V,E> {
	
	/**
	 * 
	 * @param graph
	 * @param edgeWeights
	 * @param value
	 * @return at least one cut less than value if such a cut exists, otherwise an empty list.
	 */
	public Iterable<Cut<E>> findCutsLessThan(UndirectedGraph<V,E> graph, Transformer<E,Number> edgeWeights, double value);

}
