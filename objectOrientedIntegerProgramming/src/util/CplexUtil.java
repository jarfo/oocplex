package util;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import java.util.Set;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;


public class CplexUtil {
	
	public static double epsilon = .000001;
	
	public static boolean doubleToBoolean(double value){
		if(Math.abs(1-value) < epsilon ){
			return true;
		}
		else if(Math.abs(value) < epsilon){
			return false;
		}
		else throw new RuntimeException("Failed to convert to boolean, not near zero or one: " + value);
	}
	
	
	/**
	 * 
	 * @param cplex
	 * @param set must contain no duplicates according to T.equals(), a duplicate causes an illegal argument exception
	 * @return
	 * @throws IloException
	 */
	public static <T> ImmutableBiMap<T,IloIntVar> makeBinaryVariables(IloCplex cplex, Iterable<T> set) throws IloException{
		Builder<T,IloIntVar> ans = ImmutableBiMap.builder();
		for(T t: set){
			ans.put(t, cplex.boolVar());
		}
		return ans.build();
	}
	
	/**
	 * 
	 * @param cplex
	 * @param set must contain no duplicates according to T.equals(), a duplicate causes an illegal argument exception
	 * @return
	 * @throws IloException
	 */
	public static <T> ImmutableBiMap<T,IloLinearIntExpr> makeLinearIntExpr(IloCplex cplex, Iterable<T> set) throws IloException{
		Builder<T,IloLinearIntExpr> ans = ImmutableBiMap.builder();
		for(T t: set){
			ans.put(t, cplex.linearIntExpr());
		}
		return ans.build();
	}

}
