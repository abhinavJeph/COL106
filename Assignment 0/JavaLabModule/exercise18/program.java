
import java.util.HashMap;
public class program
{
	public int test(String number)
	{
		/*
		Exercise 18: Most frequent digit- Given a number, the objective is to find out
		the most frequently occuring digit in the number. If more than 2 digits have
		the same frequency, return the smallest digit. The number is input as a string
		and the output should be the digit as an integer. For e.g. if the number is
		12345121, the most frequently occuring digit is 1. If the number is 9988776655
		the output should be 5 as it is the smallest of the digits with the highest frequency.
		*/
		
		//We will use HashMap;
		
		HashMap<Character,Integer> hm = new HashMap<>();
		
		char maxFrequencyNumber = number.charAt(0);
		
		for(int i=0;i<number.length();i++) {
			char key = number.charAt(i);
			if(hm.containsKey(key)) hm.put(key, hm.get(key)+1);
			else hm.put(key, 1);
			
			//Assigning max frequency value
			if(hm.get(maxFrequencyNumber) < hm.get(key)) maxFrequencyNumber = key;
		}
		
		//to return integer value. we could also use Character.getNumericValue
		return maxFrequencyNumber - '0' ;
	}
}
