
public class program
{
	public String test(String hex)
	{
		
		int num = Integer.parseInt(hex,16);

		String binary = Integer.toBinaryString(num);

		return binary;
	}
}
