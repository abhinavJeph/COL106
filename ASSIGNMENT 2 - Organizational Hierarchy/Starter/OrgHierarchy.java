import java.util.Vector;

// Tree node
class Node {
	int id, level;			// to store id and level of the employee
	Node left, right;		// to store left and right Nodes in AVL tree
	int height;				// to store height in AVL tree (Used for balancing)
	boolean flag;			// to store boolean value to check if it is to be printed or not (Used in toString method)

	Node boss;				// to store the boss of an employee
	Vector<Node> child;		// to store a list of all junior employees

	Node(int id) {
		this.id = id;
		this.level = 1;
		this.height = 1;
		child = new Vector<>();
		this.left = null;
		this.right = null;
		this.flag = false;
		this.boss = null;
	}

}

public class OrgHierarchy implements OrgHierarchyInterface {

	Node owner; 			// root node for organization
	AVLTree tree; 			// balanced tree to store each employee Node in sorted order
	int max_level; 			// maximum level
	String[] out_Array; 	// output array for toString method
	int size;				// to store the number of employees

	public OrgHierarchy() {
		tree = new AVLTree();
		owner = null;
		max_level = 0;
		size = 0;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public int level(int id) throws IllegalIDException, EmptyTreeException {
		Node emp = tree.search(id);
		return emp.level;

	}

	public void hireOwner(int id) throws NotEmptyException {
		if (owner != null)
			throw new NotEmptyException("Owner is already present !");
		tree.root = tree.insert(null, new Node(id));
		owner = tree.root;
		size++;
	}

	public void hireEmployee(int id, int bossid) throws IllegalIDException, EmptyTreeException {
		Node boss = tree.search(bossid);
		Node emp = new Node(id);

		tree.root = tree.insert(tree.root, emp);
		boss.child.add(emp);
		emp.level = boss.level + 1;
		emp.boss = boss;
		size++;
	}

	public void fireEmployee(int id) throws IllegalIDException, EmptyTreeException {
		Node emp = tree.search(id);
		Node boss = emp.boss;
		if (emp.id == owner.id)
			throw new IllegalIDException("Can not delete Owner");
		if (!emp.child.isEmpty())
			throw new IllegalIDException("This employee has juniors !");

		boss.child.remove(emp);
		tree.root = tree.delete(tree.root, emp);
		size--;
	}

	public void fireEmployee(int id, int manageid) throws IllegalIDException, EmptyTreeException {
		Node emp = tree.search(id);
		if (emp.id == owner.id)
			throw new IllegalIDException("Can not delete Owner");

		Node manager = tree.search(manageid);
		if (emp.level != manager.level)
			throw new IllegalIDException("Both employee are not on Same level");

		for (Node emp_child : emp.child) {
			manager.child.add(emp_child);
			emp_child.boss = manager;
		}

		emp.boss.child.remove(emp);
		tree.root = tree.delete(tree.root, emp);
		size--;
	}

	public int boss(int id) throws IllegalIDException, EmptyTreeException {
		Node emp = tree.search(id);
		if (emp.id == owner.id)
			return -1;
		else
			return emp.boss.id;
	}

	public int lowestCommonBoss(int id1, int id2) throws IllegalIDException, EmptyTreeException {
		Node e1 = tree.search(id1);
		Node e2 = tree.search(id2);
		if (e1.id == owner.id || e2.id == owner.id)
			return -1;

		// making both employees level same
		if (e1.level > e2.level) {
			while (e1.level != e2.level) {
				e1 = e1.boss;
			}
		} else if (e1.level < e2.level) {
			while (e1.level != e2.level) {
				e2 = e2.boss;
			}
		}

		while (e1.boss.id != e2.boss.id) {
			e1 = e1.boss;
			e2 = e2.boss;
		}

		return e1.boss.id;
	}

	public String toString(int id) throws IllegalIDException, EmptyTreeException {
		Node emp = tree.search(id);
		String str = "";

		Queue q = new Queue();
		q.add(emp);

		// toggling flag to true for emp and its descendants(tree under emp)
		while (!q.isEmpty()) {
			Node temp = q.remove();
			temp.flag = true;

			for (Node child : temp.child) {
				q.add(child);
			}

			// last element of queue
			// setting max_level to the level of last element of the tree
			if (q.isEmpty()) {
				max_level = temp.level;
			}
		}

		out_Array = new String[max_level];

		// to fill out out_Array
		FillOutArray(tree.root);

		// adding out_Array strings to str
		for (int i = emp.level - 1; i < max_level; i++) {
			str += out_Array[i].substring(0, out_Array[i].length() - 1);
			if (i != max_level - 1)
				str += ',';
		}
		return str;
	}

	public void FillOutArray(Node root) {
		if (root != null) {
			FillOutArray(root.left);
			if (root.flag) { // add only those node will be add whose flag = true
				if (out_Array[root.level - 1] == null) {
					out_Array[root.level - 1] = root.id + " ";
				} else
					out_Array[root.level - 1] += root.id + " ";
				root.flag = false; // reset the flag
			}
			FillOutArray(root.right);
		}
	}
}

class AVLTree {
	Node root;

	AVLTree() {

		root = null;
	}

	public int max(int a, int b) {
		if (a > b)
			return a;
		else
			return b;
	}

	public Node insert(Node root, Node emp) {
		if (root == null) {

			return emp;
		} else if (root.id > emp.id)
			root.left = insert(root.left, emp);
		else if (root.id < emp.id)
			root.right = insert(root.right, emp);
		else
			return root;

		root.height = 1 + max(getHeight(root.left), getHeight(root.right));

		int balance_factor = getBalance(root);

		// Left Left
		if (balance_factor > 1 && root.left.id > emp.id) {
			return rightRotate(root);
		}
		// Left Right
		else if (balance_factor > 1 && root.left.id < emp.id) {
			root.left = leftRotate(root.left);
			return rightRotate(root);
		}
		// Right Right
		else if (balance_factor < -1 && root.right.id < emp.id) {
			return leftRotate(root);
		}
		// Right left
		else if (balance_factor < -1 && root.right.id > emp.id) {
			root.right = rightRotate(root.right);
			return leftRotate(root);
		}

		return root;

	}

	Node getSuccesor(Node node) {
		Node current = node;

		while (current.left != null)
			current = current.left;

		return current;
	}

	public Node delete(Node root, Node emp) {
		if (root == null)
			return root;

		if (emp.id < root.id)
			root.left = delete(root.left, emp);
		else if (emp.id > root.id)
			root.right = delete(root.right, emp);
		else {
			if ((root.left == null) || (root.right == null)) {
				Node temp = null;
				if (temp == root.left)
					temp = root.right;
				else
					temp = root.left;

				// No child case
				if (temp == null) {
					temp = root;
					root = null;
				} else // One child case
					root = temp;

			} else { // two child case
				Node temp = getSuccesor(root.right);
				root.id = temp.id;
				root.level = temp.level;
				root.child = temp.child;
				root.boss = temp.boss;

				if (temp.boss != null) {
					for (int i = 0; i < temp.boss.child.size(); i++) {
						if (temp.boss.child.get(i).id == temp.id) {
							temp.boss.child.set(i, root);
							break;
						}
					}
				}

				for (Node child : root.child) {
					child.boss = root;
				}

				root.right = delete(root.right, temp);
			}
		}

		if (root == null) {
			return root;
		}

		root.height = max(getHeight(root.left), getHeight(root.right)) + 1;

		int balance = getBalance(root);

		// Left Left
		if (balance > 1 && getBalance(root.left) >= 0)
			return rightRotate(root);
		// Left Right
		if (balance > 1 && getBalance(root.left) < 0) {
			root.left = leftRotate(root.left);
			return rightRotate(root);
		}
		// Right Right
		if (balance < -1 && getBalance(root.right) <= 0)
			return leftRotate(root);
		// Right Lefts
		if (balance < -1 && getBalance(root.right) > 0) {
			root.right = rightRotate(root.right);
			return leftRotate(root);
		}
		return root;
	}

	public Node leftRotate(Node y) {
		Node x = y.right;
		Node T2 = x.left;

		x.left = y;
		y.right = T2;

		y.height = 1 + max(getHeight(y.left), getHeight(y.right));
		x.height = 1 + max(getHeight(x.left), getHeight(x.right));

		return x;

	}

	public Node rightRotate(Node y) {
		Node x = y.left;
		Node T2 = x.right;

		x.right = y;
		y.left = T2;

		y.height = 1 + max(getHeight(y.left), getHeight(y.right));
		x.height = 1 + max(getHeight(x.left), getHeight(x.right));

		return x;
	}

	public int getHeight(Node x) {
		if (x == null)
			return 0;
		return x.height;
	}

	public int getBalance(Node root) {
		if (root == null)
			return 0;
		return getHeight(root.left) - getHeight(root.right);
	}

	public Node search(int id) throws EmptyTreeException, IllegalIDException {
		if (root == null)
			throw new EmptyTreeException("Empty tree");

		Node curr = root;
		while (curr != null && curr.id != id) {
			if (curr.id > id)
				curr = curr.left;
			else
				curr = curr.right;
		}
		if (curr != null)
			return curr;
		else {
			throw new IllegalIDException("ID not found");
		}

	}
}

// Node for Queue
class MyNode {
	Node emp;
	MyNode next;

	MyNode(Node x) {
		emp = x;
		next = null;
	}
}

// Queue implementation
class Queue {
	MyNode front;
	MyNode rear;
	int size;

	Queue() {
		front = null;
		rear = null;
		size = 0;
	}

	public void add(Node item) {
		MyNode temp = new MyNode(item);
		if (isEmpty()) {
			front = temp;
			rear = front;
		} else {
			rear.next = temp;
			rear = temp;
		}
		size++;
	}

	public Node remove() {
		MyNode temp = front;
		front = front.next;
		size--;
		return temp.emp;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Node peek() {
		return front.emp;
	}

	public int size() {
		return size;
	}

}
