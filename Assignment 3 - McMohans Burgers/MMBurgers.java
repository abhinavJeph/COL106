import java.util.Vector;

public class MMBurgers implements MMBurgersInterface {
	int K; // Number of billing counters
	int M; // Maximum capacity of griddle of chef
	int globalTime; // to maintain present global time
	int totalCustomer; // to maintain number of customers arrived
	int OrderDone; // Number of customers those have recieved their orders
	int totalWaitTime; // Calculates the total waiting time of all the customers

	AVLTree tree; // tree to store all customers
	Heap<BillingCounter> counters; // Heap to store billing counters
	Chef chef; // chef which maintains all griddle methods
	Queue<Customer> delievering; // Queue which will finally deliever the order of cooked burger

	MMBurgers() {
		K = -1;
		M = -1;
		globalTime = 0;
		totalCustomer = 0;
		OrderDone = 0;
		totalWaitTime = 0;
		tree = new AVLTree();
		counters = new Heap<>();
		chef = null;
		delievering = new Queue<>();
	}

	public boolean isEmpty() {
		return (OrderDone == totalCustomer);
	}

	public void setK(int k) throws IllegalNumberException {
		if (K != -1)
			throw new IllegalNumberException("K is already set");

		K = k;
		for (int i = 1; i <= K; i++) {
			counters.add(new BillingCounter(i));
		}
	}

	public void setM(int m) throws IllegalNumberException {
		if (M != -1)
			throw new IllegalNumberException("M id already set");

		M = m;
		chef = new Chef(M);
	}

	public void advanceTime(int t) throws IllegalNumberException {
		simulation(t);
		deliever(t);
	}

	public void arriveCustomer(int id, int t, int numb) throws IllegalNumberException {
		simulation(t);
		Customer c = new Customer(id, t, numb);
		BillingCounter bcounter = counters.top();

		tree.root = tree.insert(tree.root, c);
		totalCustomer++;

//		Setting the time at which customer will leave the queue
		if (bcounter.q.isEmpty()) {
			c.oTime = c.aTime + bcounter.k;
		} else {
			Customer prevCustomer = bcounter.q.getLast();
			c.oTime = max(c.aTime, prevCustomer.oTime) + bcounter.k;
		}

//		Adding Customer in Queue
		bcounter.q.add(c);
		counters.heapify(0);
		c.bCounter = bcounter;
		c.state = bcounter.k;

	}

	public int customerState(int id, int t) throws IllegalNumberException {
		advanceTime(t);
		Customer cstmr = tree.search(id);
		if (cstmr == null)
			return 0;
		return cstmr.state;
	}

	public int griddleState(int t) throws IllegalNumberException {
		advanceTime(t);
		return chef.griddleBurgers;
	}

	public int griddleWait(int t) throws IllegalNumberException {
		advanceTime(t);
		return chef.waitingBurgers;
	}

	public int customerWaitTime(int id) throws IllegalNumberException {
		Customer cstmr = tree.search(id);

		if (cstmr == null)
			throw new IllegalNumberException("Not found");

		return cstmr.lTime - cstmr.aTime;
	}

	public float avgWaitTime() {
		return ((float) totalTime(tree.root)) / totalCustomer;
	}

	public int totalTime(Customer temp) {
		if (temp != null) {
			return totalTime(temp.left) + (temp.lTime - temp.aTime) + totalTime(temp.right);
		}
		return 0;
	}

//	Delievring
	public void deliever(int t) {
		while (!delievering.isEmpty() && delievering.getFront().lTime <= t) {
			Customer cstmr = delievering.remove();
			OrderDone++;
			cstmr.state = K + 2;
		}
	}

//	Customer : BillingQueue --> griddleWait --> griddleMake --> deliever Queue 
	public void simulation(int t) throws IllegalNumberException {
		if (t < globalTime)
			throw new IllegalNumberException("Time can not be Lower");

//			Adding customers to griddleWait
		for (BillingCounter counter : counters.v) { // for each billing counter
			QueueNode<Customer> it = counter.q.front;
			while (it != null && it.c.oTime <= t) { // for each customer in queue
				chef.gWait.add(counter.q.remove());
				it.c.state = K + 1;
				it.c.BWait = it.c.BTotal;
				chef.waitingBurgers += it.c.BWait;
				it = it.next;
			}
		}
//	Setting Billing counters again in priority
		Heap<BillingCounter> newCounters = new Heap<>();
		for (BillingCounter counter : counters.v) {
			BillingCounter newCounter = new BillingCounter(counter.k);
			newCounter.q = counter.q;
			newCounters.add(newCounter);
		}
		counters = newCounters;

		int CurrTime = globalTime;
		while (CurrTime <= t) {
//			removing the cooked burger
			while (!chef.gMake.isEmpty() && chef.gMake.getFront().cookedTime <= CurrTime) {
				Burger brgr = chef.gMake.remove();
				Customer cstmr = brgr.c;

				chef.griddleBurgers--;
				cstmr.BCooked++;

				if (cstmr.BTotal == cstmr.BCooked) {
					cstmr.lTime = CurrTime + 1;
					delievering.add(cstmr);
				}
			}

//			Adding burgers on griddle
			while ((chef.griddleBurgers < chef.capacity) && !chef.gWait.isEmpty()
					&& chef.gWait.top().oTime <= CurrTime) {
				int size = chef.capacity - chef.griddleBurgers;
				Customer top = chef.gWait.top();
				if (top.BWait <= size) {
					for (int i = 0; i < top.BWait; i++) {
						chef.gMake.add(new Burger(top, CurrTime));
					}
					chef.griddleBurgers += top.BWait;
					chef.waitingBurgers -= top.BWait;
					top.BWait = 0;
					chef.gWait.deleteTop();
				} else {
					for (int i = 0; i < size; i++) {
						chef.gMake.add(new Burger(top, CurrTime));
					}
					chef.griddleBurgers = chef.capacity;
					chef.waitingBurgers -= size;
					top.BWait -= size;
				}
			}

			CurrTime++;
		}
		globalTime = t;
	}

	public int max(int a, int b) {
		if (a > b)
			return a;
		return b;
	}
}

class BillingCounter implements Comparable<BillingCounter> {
	int k;
	Queue<Customer> q;

	BillingCounter(int k) {
		this.k = k;
		q = new Queue<>();
	}

	public int compareTo(BillingCounter b) {
		if (this.q.size() != b.q.size()) {
			return b.q.size() - this.q.size();
		}
		return b.k - this.k;
	}

}

class Burger {
	Customer c; // Customer whose burger it is
	int gTime; // time at which it is added on griddle
	int cookedTime; // time at which it will leave the griddle

	Burger(Customer c, int GTime) {
		this.c = c;
		this.gTime = GTime;
		this.cookedTime = GTime + 10;
	}
}

class Customer implements Comparable<Customer> {
	int id;
	int aTime; // Time of arrival

	int BTotal; // total number of burger customer wants
	int BWait; // Number of burgers waiting for griddle
	int BCooked; // Number of burgers cooked but Not recieved;

	// For AVL Tree
	Customer left, right;
	int height;

	BillingCounter bCounter; // Billing Counter at which customer will get added in queue
	int oTime; // Time at which order is printed and shifted to griddle wait

	int lTime; // Time at which order is recieved and customer is leaving

	int state; // State of customer : k if in billing Queue , K+1 if waiting for food, K+2 if
				// order delievered;

	Customer(int id, int aTime, int numb) throws IllegalNumberException {
		if (numb < 0)
			throw new IllegalNumberException("Number of burgers can not be negative");
		this.id = id;
		this.aTime = aTime;
		this.BTotal = numb;

		this.BWait = 0;
		this.BCooked = 0;

		this.left = null;
		this.right = null;
		this.height = 1;

		this.bCounter = null;

		this.oTime = -1;
		this.lTime = -1;
		this.state = 0;
	}

	public int compareTo(Customer cstmr) {
		if (this.oTime != cstmr.oTime) {
			return cstmr.oTime - this.oTime;
		}
		return this.bCounter.k - cstmr.bCounter.k;
	}
}

class Chef {
	int capacity; // maximum capacity of griddle
	int griddleBurgers; // burgers on griddle
	int waitingBurgers; // burgers that are ordered and are waiting for griddle
	Queue<Burger> gMake; // griddle queue on which burger will be made
	Heap<Customer> gWait; // Griddle waiting heap

	Chef(int c) {
		this.capacity = c;
		this.griddleBurgers = 0;
		this.waitingBurgers = 0;
		gMake = new Queue<>();
		gWait = new Heap<>();
	}
}

class QueueNode<T> {
	T c;
	QueueNode<T> next;

	QueueNode(T x) {
		c = x;
		next = null;
	}
}

class Queue<T> {
	public QueueNode<T> front;
	public QueueNode<T> last;
	public int size;

	Queue() {
		size = 0;
		front = null;
		last = null;
	}

	public void add(T c) {
		QueueNode<T> node = new QueueNode<>(c);

		if (size == 0) {
			front = node;
			last = node;
		} else {
			last.next = node;
			last = node;
		}
		size++;
	}

	public T remove() {
		if (size == 0) {
			System.out.println("Queue is Empty");
			return null;
		}

		QueueNode<T> Node = front;
		front = front.next;
		size--;

		return Node.c;
	}

	public T getFront() {
		if (size == 0) {
			System.out.println("Queue is Empty");
			return null;
		}
		return front.c;
	}

	public T getLast() {
		if (size == 0) {
			System.out.println("Queue is Empty");
			return null;
		}
		return last.c;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}
}

class Heap<T extends Comparable<T>> {
	Vector<T> v;

	Heap() {
		v = new Vector<>();
	}

	public boolean isEmpty() {
		return v.isEmpty();
	}

	public int left(int i) {
		return 2 * i + 1;
	}

	public int right(int i) {
		return 2 * i + 2;
	}

	public int parent(int i) {
		return (i - 1) / 2;
	}

	public void swap(int i, int j) {
		T temp = v.get(i);
		v.set(i, v.get(j));
		v.set(j, temp);
	}

	public void add(T customer) {
		v.add(customer);

		int i = v.size() - 1;
		while (i != 0 && v.get(i).compareTo(v.get(parent(i))) > 0) {
			swap(i, parent(i));
			i = parent(i);
		}
	}

	public void heapify(int i) {
		while (i < v.size()) {
			int smallest = i;
			int lt = left(i);
			int rt = right(i);

//			Finding the minimum of three
			if (lt < v.size() && v.get(lt).compareTo(v.get(smallest)) > 0) {
				smallest = lt;
			}
			if (rt < v.size() && v.get(rt).compareTo(v.get(smallest)) > 0) {
				smallest = rt;
			}
//			If i is not minimum then swap
			if (smallest != i) {
				swap(i, smallest);
				i = smallest;
			} else
				break;
		}
	}

	public void deleteTop() {
		if (isEmpty()) {
			System.out.println("Heap is Empty");
			return;
		}
		swap(0, v.size() - 1);
		v.remove(v.size() - 1);
		heapify(0);
	}

	public T top() {
		return v.get(0);
	}
}

class AVLTree {
	Customer root;

	AVLTree() {
		root = null;
	}

	public int max(int a, int b) {
		if (a > b)
			return a;
		else
			return b;
	}

	public Customer insert(Customer root, Customer cstmr) throws IllegalNumberException {
		if (root == null) {

			return cstmr;
		} else if (root.id > cstmr.id)
			root.left = insert(root.left, cstmr);
		else if (root.id < cstmr.id)
			root.right = insert(root.right, cstmr);
		else
			throw new IllegalNumberException("same ID is already present");

		root.height = 1 + max(getHeight(root.left), getHeight(root.right));

		int balance_factor = getBalance(root);

		// Left Left
		if (balance_factor > 1 && root.left.id > cstmr.id) {
			return rightRotate(root);
		}
		// Left Right
		else if (balance_factor > 1 && root.left.id < cstmr.id) {
			root.left = leftRotate(root.left);
			return rightRotate(root);
		}
		// Right Right
		else if (balance_factor < -1 && root.right.id < cstmr.id) {
			return leftRotate(root);
		}
		// Right left
		else if (balance_factor < -1 && root.right.id > cstmr.id) {
			root.right = rightRotate(root.right);
			return leftRotate(root);
		}

		return root;

	}

	Customer getSuccesor(Customer node) {
		Customer current = node;

		while (current.left != null)
			current = current.left;

		return current;
	}

	public Customer leftRotate(Customer y) {
		Customer x = y.right;
		Customer T2 = x.left;

		x.left = y;
		y.right = T2;

		y.height = 1 + max(getHeight(y.left), getHeight(y.right));
		x.height = 1 + max(getHeight(x.left), getHeight(x.right));

		return x;

	}

	public Customer rightRotate(Customer y) {
		Customer x = y.left;
		Customer T2 = x.right;

		x.right = y;
		y.left = T2;

		y.height = 1 + max(getHeight(y.left), getHeight(y.right));
		x.height = 1 + max(getHeight(x.left), getHeight(x.right));

		return x;
	}

	public int getHeight(Customer x) {
		if (x == null)
			return 0;
		return x.height;
	}

	public int getBalance(Customer root) {
		if (root == null)
			return 0;
		return getHeight(root.left) - getHeight(root.right);
	}

	public Customer search(int id) {
		if (root == null) {
			return null;
		}

		Customer curr = root;
		while (curr != null && curr.id != id) {
			if (curr.id > id)
				curr = curr.left;
			else
				curr = curr.right;
		}
		if (curr != null)
			return curr;
		else {
			return null;
		}
	}

	public void inorderPrint(Customer root) {
		if (root != null) {
			inorderPrint(root.left);
			System.out.print(root.id + "" + root.BTotal + " ");
			inorderPrint(root.right);
		}
	}
}
