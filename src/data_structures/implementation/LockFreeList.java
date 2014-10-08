package data_structures.implementation;

import java.util.concurrent.atomic.AtomicMarkableReference;

import data_structures.Sorted;

class LNode<T>{
	public T value;
	AtomicMarkableReference<LNode<T>> next;

	LNode(LNode<T> next,T value){
		this.next = new AtomicMarkableReference<LNode<T>>(next, false);
		this.value = value;
	}
}

class Window<T>{
	LNode<T> pred, current;

	Window(LNode<T> myPred, LNode<T> myCurrent){
		this.pred = myPred;
		this.current = myCurrent;
	}
}
public class LockFreeList<T extends Comparable<T>> implements Sorted<T> {

	LNode<T> root;
	LNode<T> tail;
	public LockFreeList(){
		tail = new LNode<T>(null,null);
		root = new LNode<T>(tail,null);
	}

	public void add(T t) {

		while(true){
			Window<T> window = find(root, t);
			LNode<T> pred = window.pred, current = window.current;
			LNode<T> node = new LNode<T>(current,t);
			if(pred.next.compareAndSet(current, node, false, false))
				return;
			}
		}


	public void remove(T t) {
		boolean snip = false;
		while(true){
			Window<T> window = find(root, t);
			LNode<T> pred = window.pred, current = window.current;

//			if(current.value.compareTo(t) != 0){
//				return;
//			} else {
				LNode<T> succ = current.next.getReference();
				snip = current.next.attemptMark(succ, true);
				if(!snip)
					continue;
				pred.next.compareAndSet(current, succ, false, false);
				return;
			}
		}

	public String toString() {
		String result = new String();
		LNode<T> cursor = root.next.getReference();
		if(cursor == tail){
			return new String("[]");
		}
		if(!root.next.isMarked())
			result = result + "[" + cursor.value;

		while(cursor.next.getReference() != tail){
			if(!cursor.next.isMarked())
				result = result + ", " +cursor.next.getReference().value;
			cursor = cursor.next.getReference();
		}

		return result + "]";
	}

	public Window<T> find(LNode<T> head, T key){
		LNode<T> pred = null, curr = null, succ = null;
		boolean[] marked = {false};
		boolean snip;

		retry: while(true){
			pred = head;
			curr = pred.next.getReference();
			while(true){
				if(curr == tail)
					return new Window<T>(pred,curr);
				succ = curr.next.get(marked);
				while(marked[0]){
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if(!snip)
						continue retry;
					curr = succ;
					succ = curr.next.get(marked);
				}

				if(curr.value.compareTo(key) >= 0)
					return new Window<T>(pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
}
