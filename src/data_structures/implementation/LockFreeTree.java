package data_structures.implementation;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import data_structures.Sorted;

enum UpdateEnum {
	CLEAN, DFLAG, IFLAG, MARKED
}

class LTNode<T>{
	public int key;
	public T value;
	public boolean isInternal;

	public LTNode(int key, T value){
		this.key = key;
		this.value = value;
	}
}

class InternalNode<T> extends LTNode<T>{

	AtomicReference<LTNode<T>> left;
	AtomicReference<LTNode<T>> right;
	Update<T> update;
	public InternalNode(int key, T value) {
		super(key,value);
		isInternal = true;
	}
}


class LeafNode<T> extends LTNode<T>{

	public LeafNode(int key, T value) {
		super(key, value);
		isInternal = false;
	}
}

class Update<T>{
	AtomicStampedReference<Info<T>> ref;

	public Update(UpdateEnum en, Info<T> info){
//		update = en;
//		this.info = info;
		ref = new AtomicStampedReference<Info<T>>(info, en.ordinal());
	}

	public Update<T> clone(){
		int[] stamp = {0};
		Info<T> info = this.ref.get(stamp);
		return new Update<T>(UpdateEnum.values()[stamp[0]], info);
	}
}

class Info<T>{
}

class IInfo<T> extends Info<T>{
	public InternalNode<T> p, newInternal;
	public LeafNode<T> l;
}

class DInfo<T> extends Info<T> {
	public InternalNode<T> gp, p;
	public LeafNode<T> l;
	public Update<T> pUpdate;
}

class SearchObject<T> extends DInfo<T> {
	public Update<T> gpUpdate;

	SearchObject(InternalNode<T> gp, InternalNode<T> p,
				LeafNode<T> l, Update<T> pUpdate, Update<T> gpUpdate)
				{
					super.gp = gp;
					super.p = p;
					super.l = l;
					super.pUpdate = pUpdate;
					this.gpUpdate = gpUpdate;
				}
}

public class LockFreeTree<T extends Comparable<T>> implements Sorted<T> {
	InternalNode<T> root;

	public LockFreeTree(){
		root = new InternalNode<T>(2, null);
		root.update = new Update<T>(UpdateEnum.CLEAN, null);
		root.left = new AtomicReference<LTNode<T>>(new LeafNode<T>(1, null));
		root.right = new AtomicReference<LTNode<T>>(new LeafNode<T>(2, null));
	}

	private int compareNode(LTNode<T> node, T t){
		if(node.key != 0){
			return 1;
		} else {
			return node.value.compareTo(t);
		}
	}

	private int compareNodes(LTNode<T> first, LTNode<T> second){
		if(first.key == 0 && second.key == 0){
			return first.value.compareTo(second.value);
		} else {
			return first.key - second.key;
		}
	}

	SearchObject<T> search(T value){
		InternalNode<T> p = null, gp = null;
		LTNode<T> l = root;
		Update<T> gpUpdate = null, pUpdate = null;

		while(l.isInternal){
			gp = p;
			p = (InternalNode<T>)l;
			gpUpdate = pUpdate;
			pUpdate = p.update.clone();
			if(compareNode(l,value) > 0){
				l = p.left.get();
			} else {
				l = p.right.get();
			}
		}

		return new SearchObject<T>(gp,p,(LeafNode<T>)l,pUpdate, gpUpdate);
	}

	public void add(T t) {
		InternalNode<T> p = null, newInternal = null;
		LeafNode<T> l, newSibling;
		LeafNode<T> newEl = new LeafNode<T>(0,t);
		Update<T> pUpdate = null;
		IInfo<T> op = null;

		while(true){
			SearchObject<T> sObject = search(t);
			p = sObject.p;
			l = sObject.l;
			pUpdate = sObject.pUpdate;
			if(compareNode(l,t) == 0)
				return;
			if(pUpdate.ref.getStamp() != UpdateEnum.CLEAN.ordinal()){
				 help(pUpdate);
			} else {
				newSibling = new LeafNode<T>(l.key, l.value);
				if(compareNode(l, t) > 0){
					newInternal = new InternalNode<T>(l.key,l.value);
					newInternal.update = new Update<T>(UpdateEnum.CLEAN, null);
					newInternal.left = new AtomicReference<LTNode<T>>(newEl);
					newInternal.right = new AtomicReference<LTNode<T>>(newSibling);
				} else {
					newInternal = new InternalNode<T>(0,t);
					newInternal.update = new Update<T>(UpdateEnum.CLEAN, null);
					newInternal.left = new AtomicReference<LTNode<T>>(newSibling);
					newInternal.right = new AtomicReference<LTNode<T>>(newEl);
				}
			}
			op = new IInfo<T>();
			op.p = p;
			op.newInternal = newInternal;
			op.l = l;

			boolean rez = p.update.ref.compareAndSet(pUpdate.ref.getReference(),
													op,
													pUpdate.ref.getStamp(),
													UpdateEnum.IFLAG.ordinal());
			if(rez){
				helpInsert(op);
				return;
			} else {
				help(p.update);
			}
		}
	}

	// verified
	private void help(Update<T> update) {
		if(update.ref.getStamp() == UpdateEnum.IFLAG.ordinal()){
			IInfo<T> info = (IInfo<T>)update.ref.getReference();
			helpInsert(info);
		} else if(update.ref.getStamp() == UpdateEnum.MARKED.ordinal()){
			DInfo<T> info = (DInfo<T>)update.ref.getReference();
			helpMarked(info);
		} else if(update.ref.getStamp() == UpdateEnum.DFLAG.ordinal()){
			DInfo<T> info = (DInfo<T>)update.ref.getReference();
			helpDelete(info);
		}

	}

	// verified
	private void helpInsert(IInfo<T> op) {
		casChild(op.p,op.l,op.newInternal);
		op.p.update.ref.compareAndSet(op,
									  op,
									  UpdateEnum.IFLAG.ordinal(),
									  UpdateEnum.CLEAN.ordinal());

	}

	// verified
	private boolean helpDelete(DInfo<T> op) {
		op.p.update.ref.compareAndSet(op.pUpdate.ref.getReference(),
									 op,
									 op.pUpdate.ref.getStamp(),
									 UpdateEnum.MARKED.ordinal());

		if(op.p.update.ref.getStamp() == UpdateEnum.MARKED.ordinal()){
			helpMarked(op);
			return true;
		} else {
			help(op.p.update);
			op.gp.update.ref.compareAndSet(op,
										   op,
										   UpdateEnum.DFLAG.ordinal(),
										   UpdateEnum.CLEAN.ordinal());
			return false;
		}

	}
	//verified
	private void helpMarked(DInfo<T> op) {
		LTNode<T> other = null;

		if(op.p.right.get() == op.l){
			other = op.p.left.get();
		} else {
			other = op.p.right.get();
		}

		casChild(op.gp, op.p, other);
		op.gp.update.ref.compareAndSet(op,
									   op,
									   UpdateEnum.DFLAG.ordinal(),
									   UpdateEnum.CLEAN.ordinal());
	}




	// verified
	private void casChild(InternalNode<T> parent, LTNode<T> oldNode,LTNode<T> newNode) {
		if(compareNodes(newNode, parent) < 0){
			parent.left.compareAndSet(oldNode, newNode);
		} else {
			parent.right.compareAndSet(oldNode, newNode);
		}

	}

	public void remove(T t) {
		InternalNode<T> gp = null, p = null;
		LeafNode<T> l;
		Update<T> pUpdate, gpUpdate;
		DInfo<T> op;
		while(true){
			SearchObject<T> sObject = search(t);
			if(compareNode(sObject.l, t) != 0){
				System.out.println("No element found");
				return;
			}

			gp = sObject.gp;
			p = sObject.p;
			l = sObject.l;
			pUpdate = sObject.pUpdate;
			gpUpdate = sObject.gpUpdate;

			if(gpUpdate.ref.getStamp() != UpdateEnum.CLEAN.ordinal()){
				help(gpUpdate);
			} else if(pUpdate.ref.getStamp() != UpdateEnum.CLEAN.ordinal()){
				help(pUpdate);
			} else {
				op = new DInfo<T>();
				op.gp = gp;
				op.p = p;
				op.l = l;
				op.pUpdate = pUpdate;

				boolean rez = gp.update.ref.compareAndSet(gpUpdate.ref.getReference(),
														  op,
														  gpUpdate.ref.getStamp(),
														  UpdateEnum.DFLAG.ordinal());
				if(rez){
					if(helpDelete(op)){
						return;
					}
				} else {
					help(gp.update);
				}
			}
		}

	}

	/**
	 * Used by toString to get the sorted elements in order
	 * @param node
	 * @return
	 */
	private String inOrder(LTNode<T> node, int depth){
		if(node == null)
			return new String();
		String left = "", right = "",current = "";
		if(node.isInternal){
			left = inOrder(((InternalNode<T>)node).left.get(), depth+1);
		}
		if(!node.isInternal && node.value != null)
			current = node.value + ", ";
		if(node.isInternal){
			right = inOrder(((InternalNode<T>)node).right.get(), depth+1);
		}

		return 	left + current + right;
	}

	public String toString() {
		String rez = inOrder(root,0);
		if(!rez.isEmpty())
			rez = rez.substring(0, rez.length()-2);
		return "[" + rez + "]";
	}
}
