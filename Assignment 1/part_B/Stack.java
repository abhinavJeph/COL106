public class Stack implements StackInterface {
	private DequeInterface ad;

	Stack(){
		ad =  new ArrayDeque();
	}

	public void push(Object o){
    	ad.insertFirst(o);
  	}

	public Object pop() throws EmptyStackException{
		try{
			return ad.removeFirst();
		}catch(EmptyDequeException e){
			throw new EmptyStackException("Stack is Empty !");
		}	
	}

	public Object top() throws EmptyStackException{
		try{
			return ad.first();
		}catch(EmptyDequeException e){
			throw new EmptyStackException("Stack is Empty !");
		}
	}

	public boolean isEmpty(){
    	return ad.isEmpty();
	}

    public int size(){
    	return ad.size();
    }
}