package data_structures.implementation;

import data_structures.Sorted;

class Node<T>{
	public T value;
	public Node<T> next;

	Node(T value){
		this.value = value;
		this.next = null;
	}
}

public class CoarseGrainedList<T extends Comparable<T>> implements Sorted<T> {

	Node<T> root;

	public synchronized void  add(T t) {
//		System.out.println("Adding " + t);
		if(root == null){
			root = new Node<T>(t);
//			System.out.println("Got out 1");
//			System.out.println("Now list contains" + this.toString());
			return;
		}

		if(root.value.compareTo(t) > 0){
			Node<T> temp = root;
			root = new Node<T>(t);
			root.next = temp;
//			System.out.println("Now list contains" + this.toString());
			return;
		}

		Node<T> cursor = root;

		while(cursor.next != null && cursor.next.value.compareTo(t) < 0)
			cursor = cursor.next;

		if(cursor.next == null){
			cursor.next = new Node<T>(t);
		} else {
			Node<T> tmp = cursor.next;
			cursor.next = new Node<T>(t);
			cursor.next.next = tmp;
		}
//		System.out.println("Got out!");
//		System.out.println("Now list contains" + this.toString());
	}

	public synchronized void remove(T t) {

		// check if root is not null
		if(root == null)
			return;

		// check if root have the same value as t
		if(root.value.compareTo(t) == 0){
			if(root.next != null){
				root = root.next;
			} else {
				root = null;
			}
			return;
		}

		// Iterate
		Node<T> cursor = root;

		while(cursor.next != null && cursor.next.value.compareTo(t) != 0){
			cursor = cursor.next;
		}

		if(cursor.next == null){
			return;
		}

		// Found item
		if(cursor.next.value.compareTo(t) == 0){
			if(cursor.next.next != null){
				cursor.next = cursor.next.next;
			} else {
				cursor.next = null;
			}
		}
	}

	// Recommended format
	// [] if empty
	// [1] for one element
	// [1, 2, 3] if more than one element

	public String toString() {
		String result = new String();
		Node<T> cursor = root;
		if(cursor == null){
			return new String("[]");
		}
		result = result + "[" + cursor.value;

		while(cursor.next != null){
			result = result + ", " +cursor.next.value;
			cursor = cursor.next;
		}

		return result;
	}
}
