package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

class FNode <T extends Comparable<T>> {
	public T data;
	public FNode next;
	public int key;

	public FNode(T data)
	{
		this.data = data;
		this.key = data.hashCode();
		this.next = null;
	}

	public String toString()
	{
		return this.data+"";
	}

}

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
	private FNode head;
	private Lock lock = new ReentrantLock();
	public FineGrainedList()
	{
		this.head = new FNode(Integer.MIN_VALUE);
		this.head.next = new FNode(Integer.MAX_VALUE);
	}

	public void add(T t) {

		FNode pred, curr;
		int key = t.hashCode();


		try {
			pred = head;
			curr = pred.next;
			while (curr.key < key) {
				pred = curr;
				curr = curr.next;
			}

			lock.lock();
			// current position can be corrupted by multiple insert between
			// current and pred position. So a new check should be done from
			// the last sure postion (pred one).
			curr = pred;
			while (curr.key < key) {
				pred = curr;
				curr = curr.next;
			}
			FNode FNode = new FNode(t);
			FNode.next = curr;
			pred.next = FNode;
			return;

		} finally {
			lock.unlock();
		}
	}

	public void remove(T t) {
		FNode pp, pred, curr;
		int key = t.hashCode();
		try {
			pp = head;
			pred = head.next;
			curr = pred.next;
			//empty list
			if (curr == null)
				return;

			while (curr.key < key) {
				pp = pred;
				pred = curr;
				curr = curr.next;
			}

			// parent of current position can be corrupt so we need grand-parent
			// to be sure of non-coruption
			lock.lock();
			curr = pp;
			while (curr.key < key) {
				pp = pred;
				pred = curr;
				curr = curr.next;
			}
			if (key == curr.key) {
				pred.next = curr.next;
				return;
			} else {
				return;
			}
		} finally {
			lock.unlock();
		}
	}

	public String toString() {
		FNode curr;
		String result = "";
		int key = head.hashCode();

		curr = head.next;
		int x=0;
		while (curr.key < key) {
			result += curr.toString()+" ";
			x++;
			curr = curr.next;
		}


		return x+">>"+result;
	}
}
