package data_structures.implementation;

import data_structures.Sorted;

class TreeNode<T>{
	T value;
	TreeNode<T> parent;
	TreeNode<T> right;
	TreeNode<T> left;

	TreeNode(T val){
		this.value = val;
		this.parent = null;
		this.right = null;
		this.left = null;
	}

}
public class CoarseGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	TreeNode<T> root;

	public synchronized void add(T t) {
//		throw new UnsupportedOperationException();
		if(root == null){
			root = new TreeNode<T>(t);
			return;
		}

		addToTree(root,t);
	}

	public void addToTree(TreeNode<T> node, T value){
		if(node.right == null && node.value.compareTo(value) < 0){
			node.right = new TreeNode<T>(value);
			node.right.parent = node;
			return;
		}

		if(node.left == null && node.value.compareTo(value) >= 0){
			node.left = new TreeNode<T>(value);
			node.left.parent = node;
			return;
		}

		if(node.value.compareTo(value) < 0){
			addToTree(node.right, value);
		} else {
			addToTree(node.left, value);
		}
	}

	public synchronized void remove(T t) {
		// each time only one node is deleted
		// run deleteNode until all nodes with
		// value t are deleted
		deleteNode(root,t);
	}

	/**
	 * Search and delete the node with given value
	 * @param node - node to check
	 * @param value - value of node to be deleted
	 * @return - true if a node was deleted
	 */
	private boolean deleteNode(TreeNode<T> node, T value){
		if(node == null)
			return false;
		boolean found = false;
		if(node.value.compareTo(value) > 0){
			found = deleteNode(node.left,value);

		} else if(node.value.compareTo(value) < 0) {
			found = deleteNode(node.right, value);

		} else if(node.value.compareTo(value) == 0) {
			found = true;
			if(node.left != null && node.right != null){
				TreeNode<T> succ = getMin(node.right);
				node.value = succ.value;
				deleteNode(succ, succ.value);
			} else if(node.left != null){
				replace_parent(node, node.left);
			} else if(node.right != null){
				replace_parent(node, node.right);
			} else {
				replace_parent(node, null);
			}
		}
		return found;
	}

	/**
	 * Executes the delete operation for node
	 * @param node - node to be deleted
	 * @param newNode - new node to be set instead of old one
	 */
	private void replace_parent(TreeNode<T> node, TreeNode<T> newNode){
		if(node.parent != null){
			if(node == node.parent.left){
				node.parent.left = newNode;
			} else {
				node.parent.right = newNode;
			}
		} else { // root node
			root = newNode;
		}

		if(newNode != null){
			newNode.parent = node.parent;
		}
	}
	private TreeNode<T> getMin(TreeNode<T> node) {
		TreeNode<T> current = node;

		while(current.left != null){
			current = current.left;
		}
		return current;
	}

	/**
	 * Used by toString to get the sorted elements in order
	 * @param node
	 * @return
	 */
	private String inOrder(TreeNode<T> node, int depth){
		if(node == null)
			return new String();

		return 	inOrder(node.left, depth+1)
				+ node.value + ", "
				+ inOrder(node.right, depth+1);
	}

	public String toString() {
		String rez = inOrder(root, 0);
		if(!rez.isEmpty())
			rez = rez.substring(0, rez.length()-2);
		return "[" + inOrder(root, 0) + "]";
	}
}
