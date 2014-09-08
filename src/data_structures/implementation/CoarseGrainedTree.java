package data_structures.implementation;

import data_structures.Sorted;

class TreeNode<T>{
	T value;
	TreeNode<T> right;
	TreeNode<T> left;

	TreeNode(T val){
		this.value = val;
		this.right = null;
		this.left = null;
	}
}
public class CoarseGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	public void add(T t) {
//		throw new UnsupportedOperationException();
	}

	public void remove(T t) {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		throw new UnsupportedOperationException();
	}
}
