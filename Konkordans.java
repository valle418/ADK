import java.util.*;
import java.io.*;
public class Konkordans{
	
	public static void main(String[] args) {
		Konkordans app = new Konkordans();
		String file = "ut.txt";
		Hashtable<String, Integer> index = new Hashtable<String, Integer>();
		app.createIndex(file);
		app.runSeek(file);	
	};

	public void runSeek(String file){
		System.out.println(file);
	};


	public void createIndex(String file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
    			while ((line = reader.readLine()) != null) {
        			System.out.println(line);
    			};
		} catch (IOException x) {
    	System.err.println(x);
		}
	}
}
