import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class FabricBreakup {	
	public static void main(String args[]) throws IOException{

		//Scanner to scan the input file
		Scanner sc = new Scanner(new File(args[0]));
		// Stack that stores Fav. shirt at top
		StackInterface shirtRank = new Stack();
		// Stack that maps the total count that shirt will topple
		StackInterface toppleCount = new Stack();

		int operations = sc.nextInt();
		int i = 1;
		int id = 1;
		int prev_toppleCount = -1;

		while(sc.hasNextLine() && i<operations){
			i = sc.nextInt();
			id = sc.nextInt();
			
			// Move shirt from Big Heap to Pile
			if(id==1){										
				int curr_shirtRank = sc.nextInt();
				try {
					//Current shirt is not fav. shirt
					if(curr_shirtRank<(Integer)shirtRank.top()){
						//it will only get toppled in future
						prev_toppleCount++;
					}
					// Current Shirt is the new favorite shirt
					else{
						//It will be added in shirtRank stack that stores the fav. shirt
						toppleCount.push(prev_toppleCount);
						shirtRank.push(curr_shirtRank);
						prev_toppleCount =0;
					}
				} catch (EmptyStackException e) {	
					shirtRank.push(curr_shirtRank);
					prev_toppleCount=0;
				}
			// Party with Friends
			}else if(id==2){							
				System.out.println(i+" "+prev_toppleCount);
				try {
					//Popping out the fav. shirt
					shirtRank.pop();
					prev_toppleCount = (Integer)toppleCount.pop();
				} catch (EmptyStackException e) {
					prev_toppleCount = -1;
				}
			}
		}
	}
}
