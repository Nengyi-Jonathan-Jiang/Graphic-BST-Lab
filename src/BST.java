import java.util.*;

// Hehe this is literally Java TreeSet but with BSTs and exposed TreeNodes
public class BST<T extends Comparable<T>> implements Set<T> {
    BSTNode<T> root = null;

    /**
     * @param value The value to search for
     * @return whether the value exists in the tree
     * @throws ClassCastException when value is not comparable
     */
    public boolean contains(Object value) {
        if(!(value instanceof Comparable<?>)) throw new ClassCastException();
        return find((T) value) != null;
    }

    /**
     * @param values The values to search for
     * @return whether all the values exists in the tree
     * @throws ClassCastException when any of the values is not comparable
     */
    public boolean containsAll(Collection<?> values){
        return values.stream().allMatch(this::contains);
    }

    /**
     * @param value The value to insert into the tree
     * @return whether the tree changed as a result of this call
     */
    public boolean add(T value) {
        if (root == null) {
            root = new BSTNode<>(value);
            return true;
        } else return add(root, value);
    }

    /**
     * @param values the values to be inserted into the tree.
     * @return whether the collection was changed as a result of this operation
     */
    public boolean addAll(Collection<? extends T> values) {
        return values.stream().map(this::add).toList().contains(true);
    }

    /**
     * @param parent The root node to insert under
     * @param value  The value to insert into the tree
     * @return whether the tree changed as a result of this call
     */
    protected boolean add(BSTNode<T> parent, T value) {
        int compare = value.compareTo(parent.value);

        if (compare <= 0) {
            if (parent.hasLeft())
                add(parent.getLeft(), value);
            else parent.setLeft(value);
        } else {
            if (parent.hasRight())
                add(parent.getRight(), value);
            else parent.setRight(value);
        }
        //else return false; // else: Node already in tree, do nothing :)

        return true;
    }

    /**
     * @param value the value to erase from the tree
     * @return whether a value was removed as a result of this call
     * @throws ClassCastException when value is not comparable
     */
    @Override
    public boolean remove(Object value) {
        // <rant>
        // I hate how Java generics work
        // Why can't T be treated like a real class name?
        // Look at C#, or even C++! That is the right way to do it, to build it into the language as a core feature
        // Instead, Java compiles it all down to Object and adds casts only on parameters and return types
        // In the name of "Backwards Compatability" so old JVMs can still run the generated bytecode
        // EXCEPT THEY CAN'T!
        // ERROR: "THIS FILE WAS COMPILED WITH A NEWER VERSION OF JAVA!"
        // BACKWARDS COMPATABILITY MY ASS! (If you can curse in class, I can curse here. Also, please don't kill me)
        // I can't even check that value is of type T! (I have to take it as Object because Collection dictates so)
        // I can only ensure it is a Comparable (which is just sufficient to prevent breaking the code)
        // *Breaths heavily*
        // </rant>

        if(!(value instanceof Comparable<?>)) throw new ClassCastException(); // See what I have to resort to? Disgusting.

        BSTNode<T> target = find((T)value);
        if (target == null) return false;

        if (target.getDegree() == 2) {  // Find inorder successor n and swap, then erase n
            BSTNode<T> node = target.getRight();
            while (node.hasLeft()) node = node.getLeft();

            T temp = node.value;
            deleteDegNotTwo(node);
            target.value = temp;
        } else {  // Deg = 0 or 1, shunt up the child node (if exists) into the place that the node previously occupied
            deleteDegNotTwo(target);
        }
        return true;
    }

    protected void deleteDegNotTwo(BSTNode<T> target){
        var node = target.hasLeft() ? target.getLeft() : target.hasRight() ? target.getRight() : null;
        switch (target.getChildType()) {
            case LEFT -> target.getParent().setLeft(node);
            case RIGHT -> target.getParent().setRight(node);
            case ROOT -> root = target.getLeft().makeRoot();
        }
    }

    /**
     * @param values The values to remove from the tree
     * @return whether the collection was changed as a result of this operation
     */
    public boolean removeAll(Collection<?> values) {
        return values.stream().map(this::remove).toList().contains(true);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // Ok im going to be lazy and not figure out a fast way to do this because you never check this I think
        // I put it here because Set requires it to be implemented
        // It should be fast anyway, around O(N (log M + log N)) where N is the size of c and M is the size of the tree.
        BST<T> temp = new BST<>();
        temp.addAll(this);
        for(var val : c) if(contains(val)) temp.add((T) val);
        boolean res = !stream().toList().equals(temp.stream().toList()); // This operation should be O(N) I think
        root = temp.getRoot();  // Hehe just copy the temp tree to this tree
        return res;
    }

    public void clear(){
        // Well, that was a fast implementation...
        root = null;
    }

    /**
     * @param value the value to search for
     * @return the {@link BSTNode} with that value, or null if the value is not in the tree
     */
    protected BSTNode<T> find(T value) {
        return find(root, value);
    }

    /**
     * @param node  The root node to search from
     * @param value The value to search for
     * @return the {@link BSTNode} with that value, or null if the value is not in the tree
     */
    protected BSTNode<T> find(BSTNode<T> node, T value) {
        if (node == null) return null;

        int compare = value.compareTo(node.value);

        if (compare < 0)
            return find(node.getLeft(), value);
        if (compare > 0)
            return find(node.getRight(), value);
        return node;
    }

    @Override
    public int size() {
        return countNodes(root);
    }

    protected int countNodes(BSTNode<T> node){
        return node == null ? 0 : 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }

    /**
     * @return whether the tree is empty
     */
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public Iterator<T> iterator() {
        return root == null ? Collections.emptyIterator() : new Iterator<>() {
            protected final Stack<BSTNode<T>> nodes = new Stack<>();
            protected BSTNode<T> curr = root;

            @Override
            public boolean hasNext() {
                while (curr != null) {
                    nodes.push(curr);
                    curr = curr.getLeft();
                }
                return !nodes.isEmpty();
            }

            public T next() {
                var res = nodes.pop();
                curr = res.getRight();
                return res.value;
            }
        };
    }

    public Object[] toArray() {
        // IntelliJ suggests that I replace stream().toArray() with this.toArray()
        // Wow, that would definitely work!
        // Yeah, I wanna call toArray() in the toArray() method!
        // No.
        // Just no.

        //noinspection SimplifyStreamApiCallChains
        return stream().toArray();
    }

    public <U> U[] toArray(U[] a) {
        // IDK how hacky this is, I never understood the purpose of this method overload
        return (U[]) toArray();
    }

    @Override
    public String toString() {
        return root == null ? "BST{}" : "BST" + root;
    }

    /**
     * @return The height of the tree. This is the number of edges from the root to the deepest leaf
     */
    public int getHeight() {
        return root.getHeight();
    }

    /**
     * @return The root node of the tree. If the tree is empty, this is null
     */
    public BSTNode<T> getRoot() {
        return root;
    }

    /**
     * @return the number of leaves in the tree
     */
    public int countLeaves() {
        return countLeaves(root);
    }

    /**
     * @param node the root of the tree to search
     * @return the number of leaves in the tree
     */
    protected int countLeaves(BSTNode<T> node) {
        return node == null ? 0 : node.isLeaf() ? 1 : countLeaves(node.getLeft()) + countLeaves(node.getRight());
    }

    /**
     * @return The number of levels in the tree.
     */
    public int countLevels() {
        return getHeight() + 1;
    }

    public int getWidth() {
        return Arrays.stream(getLevelWidths()).reduce(0, Math::max);
    }

    public int getDiameter() {
        return root == null ? 0 : 3 + (root.hasLeft() ? root.getLeft().getHeight() : 0) + (root.hasRight() ? root.getRight().getHeight() : 0);
    }

    public boolean isFullTree() {
        return root == null || isFull(root);
    }

    protected boolean isFull(BSTNode<T> node){
        return node.getDegree() == 0 || node.getDegree() == 2 && isFull(node.getLeft()) && isFull(node.getRight());
    }

    public T getLargest() {
        return root == null ? null : getLargest(root);
    }

    protected T getLargest(BSTNode<T> node) {
        return node.hasRight() ? getLargest(node.getRight()) : node.value;
    }

    public T getSmallest() {
        return root == null ? null : getSmallest(root);
    }

    protected T getSmallest(BSTNode<T> node) {
        return node.hasLeft() ? getSmallest(node.getLeft()) : node.value;
    }

    public T[][] getLevels(){
        if(root == null) return (T[][]) new Comparable[][]{};

        final int levels = countLevels();
        final T[][] res = (T[][]) new Comparable[levels][];
        final BSTNode<T>[][] nodes = new BSTNode[levels][];
        nodes[0] = new BSTNode[]{root};
        res[0] = (T[]) new Comparable[]{root.value};

        for(int h = 1; h < levels; h++){
            int size = 1 << h;
            nodes[h] = new BSTNode[size];
            res[h] = (T[]) new Comparable[size];
            for(int i = 0; i * 2 < size; i++){
                var n = nodes[h - 1][i];
                if(n != null){
                    var l = nodes[h][i * 2] = n.getLeft();
                    var r = nodes[h][i * 2 + 1] = n.getRight();
                    if(l != null) res[h][i * 2] = l.value;
                    if(r != null) res[h][i * 2 + 1] = r.value;
                }
            }
        }
        return res;
    }

    public int[] getLevelWidths(){
        // Java FP is nice, but verbose
        // Whatever, I will use it because I am lazy
        return Arrays.stream(getLevels()).mapToInt(i -> (int) Arrays.stream(i).filter(Objects::nonNull).count()).toArray();
    }
}