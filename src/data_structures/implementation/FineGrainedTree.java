package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

class FTreeNode<T>{
	public T value;
	public FTreeNode<T> parent;
	public FTreeNode<T> right;
	public FTreeNode<T> left;
	public ReentrantLock lock;

	public FTreeNode(T val){
		this.value = val;
		this.parent = null;
		this.right = null;
		this.left = null;
		this.lock = new ReentrantLock();
	}

}
public class FineGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	FTreeNode<T> root;
	/**
	 * Since root can be null, a head lock is required
	 */
	private Lock head_lock = new ReentrantLock();

	public void addToTree(FTreeNode<T> node, T value){
		FTreeNode<T> parent = null;
		FTreeNode<T> current = node;
		current.lock.lock();

		if(current == root){
			head_lock.unlock();
		}

		while(true){
			parent = current;
			if(current.value.compareTo(value) < 0){
				current = current.right;
			} else {
				current = current.left;
			}

			if(current == null){
				break;
			} else {
				// Hand over to next node
				current.lock.lock();
				parent.lock.unlock();
			}
		}

		// Adding new Node
		if(parent.value.compareTo(value) < 0){
			parent.right = new FTreeNode<T>(value);
		} else {
			parent.left = new FTreeNode<T>(value);
		}
		parent.lock.unlock();
	}

	public void add(T t) {

		head_lock.lock();
		if(root == null){
			root = new FTreeNode<T>(t);
			head_lock.unlock();
			return;
		}
		addToTree(root,t);
	}

	/**
	 * Search and delete the node with given value
	 * @param node - node to check
	 * @param value - value of node to be deleted
	 * @return - true if a node was deleted
	 */
	private boolean deleteNode(FTreeNode<T> node, T value){


		boolean found = false;
		FTreeNode<T> parent = null;
		FTreeNode<T> current = node;

		current.lock.lock();
		head_lock.unlock();

		while(true){

			// Do this before setting parent = current
			if(current.value.compareTo(value) == 0){
					break;
			}
			parent = current;

			if(current.value.compareTo(value) > 0){
				current = current.left;

			} else if(current.value.compareTo(value) < 0) {
				current = current.right;

			}

			if(current == null){
				System.out.println("Exit!");
				return false;
			} else {
				current.lock.lock();
				// unlock only current is not our value
				// otherwise we need both locked to delete
				// current
				if(current.value.compareTo(value) != 0)
					parent.lock.unlock();
			}
		}

		if(current.value.compareTo(value) == 0) {
			found = true;
			if(current.left != null && current.right != null){
				FTreeNode<T> succ = getMin(current);
				current.value = succ.value;
			} else if(current.left != null){
				if(parent != null){
					current.left.lock.lock();
					if(parent.left == current)
						parent.left = current.left;
					else
						parent.right = current.left;
					current.left.lock.unlock();
				}
				else{
					FTreeNode<T> old = current.left;
					old.lock.lock();
					current.value = old.value;
					current.right = old.right;
					current.left = old.left;
					old.lock.unlock();
				}
			} else if(current.right != null){
				if(parent != null){
					current.right.lock.lock();

					if(parent.left == current)
						parent.left = current.right;
					else
						parent.right = current.right;

					current.right.lock.unlock();
				}
				else {
					FTreeNode<T> old = current.right;
					old.lock.lock();
					current.value = old.value;
					current.right = old.right;
					current.left = old.left;
					old.lock.unlock();
				}
			} else {
				if(parent != null){
					if(parent.left == current){
						parent.left = null;
					} else {
						parent.right = null;
					}
				} else {
					head_lock.lock();
					root = null;
					head_lock.unlock();
				}
			}
		}
		if(parent != null && parent.lock.isLocked()){
			parent.lock.unlock();
		}
		if(current != null)
			current.lock.unlock();
		return found;
	}

	/**
	 * Get the minimum from the right subtree and delete it
	 * @param node - minimum is searched in node.right
	 * @return	   - node with min value
	 */
	private FTreeNode<T> getMin(FTreeNode<T> node) {
		FTreeNode<T>parentNode = node;
		FTreeNode<T> current = node.right;
		current.lock.lock();
		while(current.left != null) {
			if(parentNode != node)
				parentNode.lock.unlock();
			parentNode = current;
			current = current.left;
			current.lock.lock();
		}

		// checking if smallest node do not have any right child
		if(current.right != null){
			current.right.lock.lock();
		}

		if(parentNode == node){
			parentNode.right = current.right;
		} else {
			parentNode.left = current.right;
			parentNode.lock.unlock();
		}

		if(current.right != null){
			current.right.lock.unlock();
		}
		current.lock.unlock();

		return current;
	}

	public void remove(T t) {
		head_lock.lock();
		if(root != null) {
			deleteNode(root,t);
		} else {
			System.out.println("Root is null");
			head_lock.unlock();
		}
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

	public String toString() {
		String rez = inOrder(root,0);
		if(!rez.isEmpty())
			rez = rez.substring(0, rez.length()-2);
		return "[" + rez + "]";
	}
}
