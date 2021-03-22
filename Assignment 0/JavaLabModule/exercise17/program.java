import java.util.Vector;
public class program
{
	public String[] test(String fileNames[])
	{
		
		
		Vector<String> v = new Vector<>();

		//If java file then add file into vector
		for(String item :fileNames){
			if(isJavaFile(item))
				v.add(item);
		}

		//copying vector elements in String array
		String javaFiles[] = v.toArray(new String[v.size()]);
		return javaFiles;
	}

	public static boolean isJavaFile(String str){
		if(str.length<5) return false;
		return str.substring(str.length()-5).equals(".java");
	}
}
