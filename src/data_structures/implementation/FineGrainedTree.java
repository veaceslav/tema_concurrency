package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

class FTreeNode<T>{
	public T value;
	public FTreeNode<T> parent;
	public FTreeNode<T> right;
	public FTreeNode<T> left;
	public ReentrantLock lock_left, lock_right;

	public FTreeNode(T val){
		this.value = val;
		this.parent = null;
		this.right = null;
		this.left = null;
		this.lock_left = new ReentrantLock();
		this.lock_right = new ReentrantLock();
	}

}
public class FineGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	FTreeNode<T> root;
	private Lock root_lock = new ReentrantLock();

	public void addToTree(FTreeNode<T> node, T value){
		if(node.right == null && node.value.compareTo(value) < 0){
			node.lock_right.lock();
			if(node.right == null)
			{
				node.right = new FTreeNode<T>(value);
				node.right.parent = node;
			}
			else
				addToTree(node, value);
			node.lock_right.unlock();
			return;
		}

		if(node.left == null && node.value.compareTo(value) >= 0){
			node.lock_left.lock();
			if(node.left == null)
			{
				node.left = new FTreeNode<T>(value);
				node.left.parent = node;
			}
			else
				addToTree(node, value);
			node.lock_left.unlock();
			return;
		}

		if(node.value.compareTo(value) < 0){
			addToTree(node.right, value);
		} else {
			addToTree(node.left, value);
		}
	}

	public void add(T t) {

		root_lock.lock();
		if(root == null){
			root = new FTreeNode<T>(t);
			root_lock.unlock();
			return;
		}
		root_lock.unlock();

		addToTree(root,t);
	}

	/**
	 * Search and delete the node with given value
	 * @param node - node to check
	 * @param value - value of node to be deleted
	 * @return - true if a node was deleted
	 */
	private boolean deleteNode(FTreeNode<T> node, T value){
		if(node == null)
			return false;
		boolean found = false;
		if(node.value.compareTo(value) > 0){
			node.lock_left.lock();
			found = deleteNode(node.left, value);
			node.lock_left.unlock();

		} else if(node.value.compareTo(value) < 0) {
			node.lock_right.lock();
			found = deleteNode(node.right, value);
			node.lock_right.unlock();

		} else if(node.value.compareTo(value) == 0) {
			found = true;
			if(node.left != null && node.right != null){
				FTreeNode<T> succ = getMin(node.right);
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
	private void replace_parent(FTreeNode<T> node, FTreeNode<T> newNode){
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

	private FTreeNode<T> getMin(FTreeNode<T> node) {
		FTreeNode<T> current = node;

		while(current.left != null){
			current = current.left;
		}
		return current;
	}

	public void remove(T t) {
		deleteNode(root,t);
	}

	/**
	 * Used by toString to get the sorted elements in order
	 * @param node
	 * @return
	 */
	private String inOrder(FTreeNode<T> node, int depth){
		if(node == null)
			return new String();

		return 	inOrder(node.left, depth+1)
				 + node.value + ", "
				+ inOrder(node.right, depth+1);
	}

	private void Test( FTreeNode node )
	  {
	      if ( node == null )
	        return;

	     Test( node.left );
	     System.out.println( "Node data: " + node.value);
	     Test( node.right );

	  }

	public String toString() {
		String rez = inOrder(root,0);
		if(!rez.isEmpty())
			rez = rez.substring(0, rez.length()-2);
		return "[" + rez + "]";
	}
}
