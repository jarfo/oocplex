package minCut;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.Ordering;

public class PriorityQueueBoundedTest {

	@Test
	public void test() {
		PriorityQueueBounded<Integer> pq = new PriorityQueueBounded<Integer>(2, Ordering.<Integer>natural());
		assertTrue(pq.add(Integer.valueOf(3)));
		assertEquals(1,pq.size());
		assertTrue(pq.add(Integer.valueOf(4)));
		assertEquals(2,pq.size());
		assertTrue(pq.add(Integer.valueOf(5)));
		assertEquals(2,pq.size());
		assertEquals(Integer.valueOf(4),pq.poll());
		assertEquals(1,pq.size());
		assertEquals(Integer.valueOf(5),pq.poll());
		assertEquals(0,pq.size());
		assertTrue(pq.add(Integer.valueOf(3)));
		assertEquals(1,pq.size());
		assertTrue(pq.add(Integer.valueOf(4)));
		assertEquals(2,pq.size());
		assertTrue(!pq.add(Integer.valueOf(2)));
		assertEquals(2,pq.size());
		assertEquals(Integer.valueOf(3),pq.poll());
		assertEquals(1,pq.size());
		assertEquals(Integer.valueOf(4),pq.poll());
		assertEquals(0,pq.size());
	}

}
