package jacobi.core.util;

import java.util.ArrayDeque;

import org.junit.Assert;
import org.junit.Test;

public class EnqueTest {
	
	@Test
	public void shouldBeAbleToUseEnqueAsAStack() {
		Enque<String> stack = Enque.stackOf(new ArrayDeque<>());
		stack.push("A").push("B").push("C");
		
		Assert.assertEquals(3, stack.size());
		Assert.assertEquals("C", stack.pop());
		Assert.assertEquals("B", stack.pop());
		Assert.assertEquals("A", stack.pop());
		Assert.assertTrue(stack.isEmpty());
	}
	
	@Test
	public void shouldBeAbleToUseEnqueAsAQueue() {
		Enque<String> queue = Enque.queueOf(new ArrayDeque<>());
		queue.push("A").push("B").push("C");
		
		Assert.assertEquals(3, queue.size());
		Assert.assertEquals("A", queue.pop());
		Assert.assertEquals("B", queue.pop());
		Assert.assertEquals("C", queue.pop());
		Assert.assertTrue(queue.isEmpty());
	}
	
	@Test
	public void shouldAllItemsInTheSameOrdering() {
		Enque<String> stack = Enque.stackOf(new ArrayDeque<>());
		stack.push("A").push("B").push("C");
		
		Enque<String> queue = Enque.queueOf(new ArrayDeque<>());
		queue.push("A").push("B").push("C");
		
		Assert.assertArrayEquals(
			new String[] {"A", "B", "C"},
			stack.toArray(n -> new String[n])
		);
		
		Assert.assertArrayEquals(
			new String[] {"A", "B", "C"},
			queue.toArray(n -> new String[n])
		);
	}

}
