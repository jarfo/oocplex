package minCut;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

public class PriorityQueueBounded<T> implements Iterable<T>{
	
	private int maxSize;
	private PriorityQueue<T> priorityQueue;
	private Set<T> data;
	private Comparator<? super T> comparator;
	

	
	public PriorityQueueBounded(int maxSize, Comparator<? super T> comparator){
		if(maxSize <=0){
			throw new RuntimeException();
		}
		this.comparator = comparator;
		this.maxSize = maxSize;
		this.data = new HashSet<T>();
		this.priorityQueue = new PriorityQueue<T>(maxSize,comparator);
	}
	
	private void addHelp(T element){
		this.data.add(element);
		this.priorityQueue.add(element);
	}
	
	public int size(){
		return this.priorityQueue.size();
	}
	
	private T pollHelp(){
		T toRemove = this.priorityQueue.poll();
		if(toRemove!=null){
			this.data.remove(toRemove);
		}
		return toRemove;
	}
	
	public boolean add(T element){		
		if(!this.data.contains(element)){
			if(this.priorityQueue.size() == maxSize){
				//System.out.println(priorityQueue.peek());
				if(this.comparator.compare(element, priorityQueue.peek()) > 0){
					pollHelp();
					addHelp(element);
					return true;
				}
				return false;
			}
			else{
				addHelp(element);
				return true;
			}			
		}
		else{
			return false;
		}		
	}
	
	public T poll(){
		return pollHelp();
	}
	
	

	@Override
	public Iterator<T> iterator() {
		return this.priorityQueue.iterator();
	}
	
	

}
