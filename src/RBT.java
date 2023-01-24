public class RBT<T extends Comparable<T>> extends BST<T> {
	/**
	 * Performs a left-left rotation
	 *
	 * @param p p, the parent of x
	 * @param g g, the grandparent of x
	 */
	private void LL_Rotation (RBTNode<T> p, RBTNode<T> g) {
		/*-Subtree looks like:----
		 *    ?
		 *    g
		 *  p   u
		 * x s
		 -----------------------*/
		g.setLeftChild(p.getRightChild());
		/*-Subtree looks like:----
		 *  ?
		 *  g
		 * s u
		 *
		 * floating:
		 *  p
		 * x
		 -----------------------*/
		switch (g.getChildType()) {
			case LEFT -> g.getParent().setLeftChild(p);
			case RIGHT -> g.getParent().setRightChild(p);
			case ROOT -> root = p.makeRoot();
		}
		/*-Subtree looks like:----
		 *  ?
		 *  p
		 * x
		 *
		 * floating:
		 *  g
		 * s u
		 -----------------------*/
		p.setRightChild(g);
		/*-Subtree looks like:----
		 *   ?
		 *   p
		 * x   g
		 *    s u
		 -----------------------*/

		// Finally, recolor the nodes
		g.makeRed();
		p.makeBlack();
	}

	/**
	 * Performs a right-right rotation
	 *
	 * @param p p, the parent of x
	 * @param g g, the grandparent of x
	 */
	private void RR_Rotation (RBTNode<T> p, RBTNode<T> g) {
		/*-Subtree looks like:----
		 *    ?
		 *    g
		 *  u   p
		 *     s x
		 -----------------------*/
		g.setRightChild(p.getLeftChild());
		/*-Subtree looks like:----
		 *  ?
		 *  g
		 * u s
		 *
		 * floating:
		 *  p
		 *   x
		 -----------------------*/
		switch (g.getChildType()) {
			case LEFT -> g.getParent().setLeftChild(p);
			case RIGHT -> g.getParent().setRightChild(p);
			case ROOT -> root = p.makeRoot();
		}
		/*-Subtree looks like:----
		 *  ?
		 *  p
		 *   x
		 *
		 * floating:
		 *  g
		 * u s
		 -----------------------*/
		p.setLeftChild(g);
		/*-Subtree looks like:----
		 *    ?
		 *    p
		 *  g   x
		 * u s
		 -----------------------*/

		// Finally, recolor the nodes
		g.makeRed();
		p.makeBlack();
	}

	/**
	 * Performs a left-left rotation
	 *
	 * @param p p, the parent of x
	 * @return The new grandparent
	 */
	private RBTNode<T> LL_Rotation (RBTNode<T> p) {
		System.out.println("Performing Left-Left Rotation");

		LL_Rotation(p, p.getParent());

		return p;
	}

	/**
	 * Performs a right-right rotation
	 *
	 * @param p p, the parent of x
	 * @return The new grandparent
	 */
	private RBTNode<T> RR_Rotation (RBTNode<T> p) {
		System.out.println("Performing Right-Right Rotation");

		RR_Rotation(p, p.getParent());

		return p;
	}

	/**
	 * Performs a left-right rotation
	 *
	 * @param p p, the parent of x
	 * @return The new grandparent
	 */
	private RBTNode<T> LR_Rotation (RBTNode<T> p) {
		var x = p.getRightChild();
		var g = p.getParent();

		System.out.println("Performing Left-Right Rotation");

		RR_Rotation(x, p);
		LL_Rotation(x, g);

		return x;
	}


	/**
	 * Performs a right-left rotation
	 *
	 * @param p p, the parent of x
	 * @return The new grandparent
	 */
	private RBTNode<T> RL_Rotation (RBTNode<T> p) {
		var x = p.getLeftChild();
		var g = p.getParent();

		System.out.println("Performing Right-Left rotation");

		LL_Rotation(x, p);
		RR_Rotation(x, g);

		return x;
	}

	/**
	 * Automatically performs the correct rotation based on x's role in the tree
	 *
	 * @return The new grandparent
	 */
	private RBTNode<T> rotate (RBTNode<T> x) {
		var p = x.getParent();

		if (p.isLeftChild() && x.isLeftChild())
			return LL_Rotation(p);
		else if (p.isLeftChild() && x.isRightChild())
			return LR_Rotation(p);
		else if (p.isRightChild() && x.isLeftChild())
			return RL_Rotation(p);
		else if (p.isRightChild() && x.isRightChild())
			return RR_Rotation(p);
		else
			throw new Error("This should never happen");
	}

	/**
	 * @param value The value to insert into the tree
	 * @return whether the tree changed as a result of this call
	 */
	@Override
	public boolean add (T value) {
		if (root == null) {
			root = new RBTNode<>(value).makeRoot();
			return true;
		}
		return add(root, value);
	}

	/**
	 * @param _parent The root node to insert under
	 * @param value  The value to insert into the tree
	 * @return whether the tree changed as a result of this call
	 */
	@Override
	protected boolean add (BSTNode<T> _parent, T value) {
		var parent = (RBTNode<T>) _parent;

		// Color swap if necessary
		if (RBTNode.isRed(parent.getLeftChild()) && RBTNode.isRed(parent.getRightChild())) {
			if (parent == root) {
				RBTNode.swapColor(parent.getLeftChild());
				RBTNode.swapColor(parent.getRightChild());
			} else {
				RBTNode.swapColor(parent);
				RBTNode.swapColor(parent.getLeftChild());
				RBTNode.swapColor(parent.getRightChild());
				fix(parent);
			}
		}

		int compare = value.compareTo(parent.value);

		if (compare < 0)
			if (parent.hasLeftChild())
				return add(parent.getLeftChild(), value);
			else
				fix(parent.insertLeft(value));
		else if (parent.hasRightChild())
			return add(parent.getRightChild(), value);
		else
			fix(parent.insertRight(value));
		//else return false; // else: Node already in tree, do nothing :)

		return true;
	}

	/**
	 * Fixes red-red violation and root violation, which occur when inserting nodes
	 *
	 * @param x The node which may be violating the red rule
	 */
	protected void fix (RBTNode<T> x) {
		// Check for Red violation
		var p = x.getParent();
		if (RBTNode.isRed(p))   // Do the corresponding rotation
			rotate(x);

		// Make root black
		((RBTNode<?>) root).makeBlack();
	}

	/**
	 * @param value the value to search for
	 * @return the node with that value, or null if the value is not in the tree
	 */
	public RBTNode<T> find (T value) {
		return (RBTNode<T>) super.find(value);
	}

	/**
	 * @param sib The sibling of the double-black node (We use sibling because node itself may be null).
	 */
	private void fixDoubleBlack (RBTNode<T> sib) {
		var parent = sib.getParent();

		if (sib.getSibling() == null)
			System.out.println("Fix double black null");
		else System.out.println("Fix double black " + sib.getSibling().value);

		if (RBTNode.isRed(sib)) { // Red sibling
			if (sib.getChildType() == BSTNode.ChildType.RIGHT) {
				RR_Rotation(sib);
				fixDoubleBlack(parent.getRightChild());
			} else {
				LL_Rotation(sib);
				fixDoubleBlack(parent.getLeftChild());
			}
		}
		// Black sibling, has red child
		else if (RBTNode.isRed(sib.getLeftChild()) || RBTNode.isRed(sib.getRightChild())) {
			Main.printTree(this);

			RBTNode.Color origColor = RBTNode.getColor(sib.getParent());
			RBTNode<T> p = rotate(RBTNode.isRed(sib.getLeftChild()) ? sib.getLeftChild() : sib.getRightChild());

			RBTNode.setColor(p, origColor);

			Main.printTree(this);

			RBTNode.makeBlack(p.getLeftChild());
			RBTNode.makeBlack(p.getRightChild());
		} else { // Black sibling, no red child
			Main.printTree(this);

			sib.makeRed();
			if (parent.isNotRoot() && RBTNode.isBlack(parent)) {
				fixDoubleBlack(sib.getParent().getSibling());
			} else RBTNode.makeBlack(parent);
		}
	}

	@Override
	protected void deleteSimple (BSTNode<T> _target) {
		var target = (RBTNode<T>) _target;
		var node = target.hasLeftChild() ? target.getLeftChild() : target.getRightChild();

		if (RBTNode.isRed(target) || RBTNode.isRed(node)) {
			super.deleteSimple(target);
			RBTNode.makeBlack(node);
		} else { // Double black
			var sib = target.hasSibling() ? target.getSibling() : target.getParent().getSibling();
			super.deleteSimple(target);
			fixDoubleBlack(sib);
		}
	}
}

