import java.util.*;
import java.io.*;
public class Konkordans{
	
	public static void main(String[] args) {
		// Initiating konkordans object
		Konkordans app = new Konkordans();
		// names of files used
		String file = "ut.txt";
		String aIndex = "aIndex.txt";
		String txtFile = "korpus.txt";

		// create hashtable for index
		Hashtable<String, Long> index = new Hashtable<String, Long>();
		
		// if index has been written to file, read it from file
		if(new File(aIndex).isFile()){	
			index = app.readIndexFromFile(aIndex);
		// otherwise, create index and write it to file
		}else{
			index = app.createIndex(file, index);
			app.indexToFile(index, aIndex);
		}
		// list for range of indexes in "ut.txt"
		long[] in = {0,0};
		// check to see that the user has provided arguments
		if(args.length>0){
			args[0] = args[0].toLowerCase();
			// get the range of the indexes
			in = app.getRange(file, index, args[0]);	
			// create Linkedlist for the word positions
			LinkedList<Long> x = app.findWordPositions(args[0],file, in);
			// Print number of words in text
			System.out.println("Det finns "+ x.size() + " förekomster av ordet.");
			// if the number of words in the text is more than 25, ask the user 
			// if the word in the sentence should be printed.
			if(x.size()>25){
				if(app.askUser("Vill du skriva ut alla förekomster?")){
					app.writeSentences(x, txtFile);
				}
			}
			// if the number of words in the text is less than 25, but more than 0
			// print the word with sentence
			else if (x.size()> 0){
				app.writeSentences(x, txtFile);
			}
		};
		
	};
	/* Find all the positions of the word that you are looking for from a indexfile
	* Parameters: word you are looking for, indexfile-name, array with two long 
	* numbers as the range in which look for the word
	* Returns: a LinkedList of all the positions where the word exists stored as long numbers*/
	public LinkedList<Long> findWordPositions(String word, String file, long[] in){
		// Create local varibles
		int i = (int)in[0];
		int j = (int)in[1];
		int m = 0;
		String s;
		LinkedList<Long> x = new LinkedList<Long>();
		char c;
		String parts[];
		try {
			// Open file as randomaccessfile with read access
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			// While the range to look in is longer than 1000
			while ((j-i)>1000){
				// give m a value in the middle of j and i
				m = ((i+j)/2);
				// positions read in file at m-position
				reader.seek(m);
				// read one line first, in case m is in the middle of a line
				s = reader.readLine();
				// read next line, guaranteed a full line
				s = reader.readLine();
				// Split line at space and store in an array
				// first part is a word, second is the position 
				// of the word in the textfile.
				parts = s.split(" ");
				// If the word you are looking for ís greater than
				// the word in the array
				if(word.compareTo(parts[0])>0){
					// put i as m (we want to look in the half which 
					// is between j and m)
					i = m;
				// Else if the word you are looking for ís lesser than
				// the word in the array
				}else{
					// put j as m (we want to look in the half which 
					// is between i and m)
					j= m;
				}
			}
			// when the distance between j and i (the range) is lesser than
			// 1000, place read in file at the i-position 
			reader.seek(i);
			// read one line first, in case i-pos is in the middle of a line
			s = reader.readLine();
			// Keep going until you find word, or you find a word which is
			// greater than the one you are looking for
			while (true){
				s = reader.readLine();
				parts = s.split(" ");
				parts[0] = parts[0].replace("/[,;.]$/","");
				if (parts[0].equals(word)){
					int z= 0;
					while (parts[0].equals(word)){
						x.add(Long.parseLong(parts[1]));
						s = reader.readLine();
						parts = s.split(" ");
						z++;
					}
					return x;
				}if(s.compareTo(word)>0){
					return x;
				}
			}
		} catch (IOException y) {
    		System.err.println(y);
		}
		return x;
	}

	public long[] getRange(String file, Hashtable<String, Long> index, String word){
		long[] in = {0,0};
		if(word.length()> 3){
			word = word.substring(0,3);
		}
		System.out.println("Checking if i is a key in index");
		System.out.println(index.get("i"));
		if(index.containsKey(word)){
			in[0] = index.get(word);
			word = incremented(word);
			while(!(index.containsKey(word))){
				word = incremented(word);
			}
			word = incremented(word);
			while(!(index.containsKey(word))){
				word = incremented(word);
			}
			in[1] = index.get(word);
			return in;
		}
		return in;

	};


	public Hashtable<String, Long> createIndex(String file, Hashtable<String, Long> index){
		try {
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			String line = null;
			String[] parts;
			long pointer = reader.getFilePointer();
    			while ((line = reader.readLine()) != null) {
        			parts = line.split(" ");
        			String sub;
        			if (parts[0].length() < 3){
        				sub = parts[0];
        			}else{
        				sub = parts[0].substring(0, 3);
        			}
        			if (index.containsKey(sub)){
        				continue;
        			}
        			index.put(sub, pointer);
        			if(sub.equals("i")){
        				System.out.println("i is found at " + index.get("i"));
        			}
        			pointer = reader.getFilePointer();
    			};
    		reader.close();
		} catch (IOException x) {
    	System.err.println(x);
		}
		return index;
	}

	public String incremented(String original) {
    	StringBuilder buf = new StringBuilder(original);
   		int index = buf.length() -1;
    	while(index >= 0) {
       		char c = buf.charAt(index);
       		c++;
       		buf.setCharAt(index, c);
       		if(c == 0) { 
        		index--;
        		continue;
    		}
       		return buf.toString();
    	}
    	// overflow at the first "digit", need to add one more digit
    	buf.insert(0, '\1');
    	return buf.toString();
	}

	public void indexToFile(Hashtable<String, Long>index, String file){
		Enumeration<String> keys = index.keys();
		try{
			RandomAccessFile writer = new RandomAccessFile(file, "rw");
		while (keys.hasMoreElements()){
			String key = keys.nextElement();
			if(key.equals("i")){
        				System.out.println("i is found at " + index.get(key));
        			}
			writer.writeBytes(key + " " + index.get(key)+ "\n");
		}

		}catch(IOException x){
			System.err.println(x);
		}
	}

	public Hashtable<String, Long> readIndexFromFile(String file){
		Hashtable<String, Long> index = new Hashtable<String, Long>();
		String line = null;
		String[] parts = {" ", " "};
		long l;
		String s = "";
		try{
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			while ((line = reader.readLine()) != null) {
        			parts = line.split(" ");
        			s = parts[0];
        			if(s.equals("i")){
        				System.out.println("found i at " + parts[1]);
        			}
        			l = Long.parseLong(parts[1]);
        			index.put(s, l);
			}
		}catch(IOException x){
			System.err.println(x);
		}
		return index;
	}

	public void writeSentences(LinkedList<Long> list, String file){
		byte[] b = new byte[60];
		int size = list.size();
		try{
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			for(int j = 0; j<size; j++){
				reader.seek((list.removeFirst()-30));
				for(int i = 0; i < 60; i++){
					b[i]= reader.readByte();
				}
				String s = new String(b, "ISO-8859-1");
				s = s.replaceAll("(\\n)", " ");
				System.out.println(s);
			}
		}catch(IOException x){
			System.err.println(x);
		}

	}
}
