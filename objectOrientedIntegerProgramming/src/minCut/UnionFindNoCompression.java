package minCut;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class UnionFindNoCompression<T> {
	
	private static class UnionPair<U>{
		private U parent;
		private U child;
		private boolean parentRankPromotion;
		
		public U getParent() {
			return parent;
		}
		public U getChild() {
			return child;
		}
		
		public boolean isParentRankPromotion() {
			return parentRankPromotion;
		}
		public UnionPair(U parent, U child, boolean parentRankPromotion) {
			super();
			this.parent = parent;
			this.child = child;
			this.parentRankPromotion = parentRankPromotion;
		}
		
		
	}
	
	int getUndoSize(){
		return stack.size();
	}
	
	private Map<T,T> parentMap;
	private Multimap<T,T> childMap;
	private Map<T,Integer> depthOfChildren;
	private int numComponents;
	private Deque<UnionPair<T>> stack;
	
	public UnionFindNoCompression(Set<T> set){
		parentMap = new HashMap<T,T>();
		childMap = HashMultimap.create();
		depthOfChildren = new HashMap<T,Integer>();
		numComponents = set.size();
		stack = new ArrayDeque<UnionPair<T>>();
		for(T val :set){
			parentMap.put(val, val);
			depthOfChildren.put(val, 0);			
		}
	}
	
	//including self
	public Set<T> allChildren(T val){
		Set<T> ans = new HashSet<T>();
		allChildren(ans,val);
		return ans;
	}
	
	private void allChildren(Set<T> toAddTo, T val ){
		toAddTo.add(val);
		for(T child: this.childMap.get(val)){
			allChildren(toAddTo,child);
		}
	}
	
	public T find(T val){
		T potential = parentMap.get(val);
		if(val == potential){
			return val;
		}
		else{
			return find(potential);
		}
	}
	
	public T union(T first, T second){
		T firstParent = find(first);
		T secondParent = find(second);
		if(firstParent == secondParent){
			return firstParent;
		}
		numComponents--;
		if(depthOfChildren.get(firstParent).intValue() > depthOfChildren.get(secondParent).intValue()){
			this.parentMap.put(secondParent, firstParent);			
			this.childMap.put(firstParent, secondParent);
			stack.addLast(new UnionPair<T>(firstParent,secondParent,false));
			return firstParent;
		}
		else if(depthOfChildren.get(firstParent).intValue() < depthOfChildren.get(secondParent).intValue()){
			this.parentMap.put(firstParent, secondParent);
			this.childMap.put(secondParent, firstParent);
			stack.addLast(new UnionPair<T>(secondParent,firstParent,false));
			return secondParent;
		}
		else{
			this.depthOfChildren.put(firstParent, depthOfChildren.get(firstParent)+1);
			this.parentMap.put(secondParent, firstParent);
			this.childMap.put(firstParent, secondParent);
			stack.addLast(new UnionPair<T>(firstParent,secondParent,true));
			return firstParent;
		}
	}
	
	public void undo(){
		UnionPair<T> pair = stack.removeLast();
		parentMap.put(pair.getChild(), pair.getChild());
		childMap.remove(pair.getParent(), pair.getChild());
		if(pair.isParentRankPromotion()){
			depthOfChildren.put(pair.getParent(), depthOfChildren.get(pair.getParent())-1);
		}
		this.numComponents++;
	}
	
	public int getNumComponents(){
		return this.numComponents;
	}

}
