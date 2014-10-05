package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

class FNode <T extends Comparable<T>> {
	public T data;
	public FNode<T> next;
	public int key;
	public Lock lock;

	public FNode(T data)
	{
		this.data = data;
		if(data != null)
			this.key = data.hashCode();
		else
			this.key = Integer.MIN_VALUE;
		this.next = null;
		this.lock = new ReentrantLock();
	}

	public String toString()
	{
		return this.data+"";
	}

	public void lock()
	{
		this.lock.lock();
	}
	public void unlock()
	{
		this.lock.unlock();
	}
}

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
	private FNode<T> head;
	public FineGrainedList()
	{
		this.head = new FNode<T>(null);
	}

	public void add(T t) {

		FNode<T> pred, curr;
		int key = t.hashCode();
		head.lock();
		pred = head;

		try {
			curr = pred.next;
			if(curr != null)
			{
				curr.lock();
				try{
					while (curr.key < key && curr.next != null) {
						pred.unlock();
						pred = curr;
						curr = curr.next;
						curr.lock();
					}

				FNode<T> FNode = new FNode<T>(t);
				FNode.next = curr;
				pred.next = FNode;
				} finally {
					curr.unlock();
				}
			}
			else
			{
				FNode<T> FNode = new FNode<T>(t);
				FNode.next = null;
				pred.next = FNode;
			}
		} finally {
			pred.unlock();
		}
	}

	public void remove(T t) {
		FNode<T> pred = null, curr=null;
		int key = t.hashCode();
		head.lock();
		try {
			pred = head;
			curr = pred.next;
			curr.lock();
			try{
				while (curr.key < key) {
					pred.unlock();
					pred = curr;
					curr = curr.next;
					curr.lock();
				}
				if (curr.key == key) {
					pred.next = curr.next;
				}
			} finally {
				curr.unlock();
			}
		} finally {
			pred.unlock();
		}
	}

	public String toString() {
		FNode<T> curr;
		String result = "[";

		curr = head.next;
		if(curr == null)
			return "[]";

		while (curr != null) {
			result += curr.toString()+", ";
			curr = curr.next;
		}

		// delete last " ,"
		if(result.length() != 1)
			result= result.substring(0,result.length()-2);

		return result + "]";
	}
}
