public class ArrayDeque implements DequeInterface {
  private Object[] arr; 
  private int head;
  private int size;
  private int capacity = 1;;

  //Constructor
  ArrayDeque(){
    arr = new Object[capacity];
    head=0;
    size=0;
  }

  //insert Element at head of ArrayDeque
  public void insertFirst(Object o){
    if(isFull()) arr = reSize(arr); // double the array when it becomes full

    head = (head-1+capacity)%capacity; // increment head in circular way
    arr[head] = o;
    size++;
  }
  
  //insert element at tail of arrayDeque
  public void insertLast(Object o){
    if(isFull()) arr = reSize(arr);   // double the array when it becomes full

    /*tail = front+size-1
    After incrementing, tail = front+size*/
    int tail = (head+size)%capacity;
    arr[tail] = o;
    size++;
  }

  //Copy the elements in larger array of double capacity
  private Object[] reSize(Object[] arr){
    int it = head;      // pointer in old array
    int tail = (head+size-1)%capacity;

    int newCapacity = capacity<<1; // new capacity = 2*capacity 
    Object[] newArr = new Object[newCapacity];
    int i=0;    //Pointer in new array

    //Copying Elements
    while(it!=tail){
      newArr[i] = arr[it];
      i++;
      it = (it+1)%capacity;
    }
    newArr[i] = arr[it];

    // reseting head and capacity
    head =0;
    capacity = newCapacity;
    return newArr;
  }
  
  //remove and returns the element at head of ArrayDeque
  public Object removeFirst() throws EmptyDequeException{
    if(isEmpty()){
      throw new EmptyDequeException("Deque is Empty !");
    }
    Object temp = first();  // storing front value, i.e, to be returned
    head = (head+1)%capacity;
    size--;
    return temp;
  }
  
  //remove and returns the element at tail of ArrayDeque
  public Object removeLast() throws EmptyDequeException{
    if(isEmpty()){
      throw new EmptyDequeException("Deque is Empty !");
    }
    Object temp = last();   // storing Last value, i.e, to be returned
    size--;
    return temp;    
  }

  //returns the element at head of ArrayDeque
  public Object first() throws EmptyDequeException{
    if(isEmpty()){
      throw new EmptyDequeException("Deque is Empty !");
    }
    return arr[head%capacity];
  }
  
  //returns the element at tail of ArrayDeque
  public Object last() throws EmptyDequeException{
    if(isEmpty()){
      throw new EmptyDequeException("Deque is Empty !");
    }
    // tail = head+size-1
    return arr[(head+size-1)%capacity];
  }
  
  //returns the no. of elements in array
  public int size(){
   return size;
  }
  
  //returns true if Empty
  public boolean isEmpty(){
    return (size==0);
  }

  // returns true if full
  private boolean isFull(){
    return size==capacity;
  }

  //toString method to print the ArrayDeque
  public String toString(){
    if(size==0) return "[]";

    StringBuffer s = new StringBuffer();
    s.append('[');

    int it = head;  // iteraor to traverse from head to tail
    int tail = (head+size-1)%capacity;
    while(true){
      s.append(arr[it]);
      if(it==tail){
        s.append(']');
        break;
      }
      s.append(',');
      it = (it+1)%capacity;
    }
    return s.toString();
  }  
}