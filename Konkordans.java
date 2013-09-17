/**
* A program creating a concordance-database from a text to be used with the 
* command java Konkordans word-you-wish-to-look-up
*
* @author Therese Askling, taskling@kth.se
* @author Matilda Valleryd, valleryd@kth.se
*/

import java.util.*;
import java.io.*;
public class Konkordans{
	
	public static void main(String[] args) {
		// Initiating konkordans object
		Konkordans app = new Konkordans();
		// The files used
		String tokenizerOutput = "/var/tmp/ut.txt";
		String aIndexFile = "aIndex.txt";
		String korpus = "/info/adk13/labb1/korpus";

		// create a hashtable for the a-index
		Hashtable<String, Long> aIndex = new Hashtable<String, Long>();
		// if the index has previously been written to a file, create a hashtable from it
		if(new File(aIndexFile).isFile()){	
			aIndex = app.readIndexFromFile(aIndexFile);
		// if not, create the index and save it to the file aIndexFile
		}else{
			aIndex = app.createAIndex(tokenizerOutput, aIndex);
			app.indexToFile(aIndex, aIndexFile);
		}
		// initilize a list to contain where to start and stop the to be search
		long[] in = {0,0};
		// If the user has provided a word to be searched, get it
		if(args.length>0){
			args[0] = args[0].toLowerCase();
			// get the start- and stop-indexes to be searched
			in = app.getRange(tokenizerOutput, aIndex, args[0]);	
			// create Linkedlist for the word positions
			LinkedList<Long> x = app.findWordPositions(args[0],tokenizerOutput, in);
			// Print number of words in text
			System.out.println("Det finns "+ x.size() + " förekomster av ordet.");
			// if the number of words in the text is more than 25, ask the user 
			// if the word in the sentence should be printed.
			if(x.size()>25){
				if(app.askUser("Vill du skriva ut alla förekomster? \nJ(Ja) or N(Nej)")){
					app.writeSentences(x, korpus, args[0].length());
				}
			}
			// if the number of words in the text is less than 25, but more than 0
			// print the word with sentence
			else if (x.size()> 0){
				app.writeSentences(x, korpus, args[0].length());
			}
		};
		
	};
	/** Find all the positions of the word that you are looking for from a indexfile
	* Parameters: word you are looking for, indexfile-name, array with two long 
	* numbers as the range in which look for the word
	* Returns: a LinkedList of all the positions where the word exists stored as long numbers
	*/
	public LinkedList<Long> findWordPositions(String word, String tokenizerOutput, long[] in){
		// Create local varibles
		int i = (int)in[0];
		int j = (int)in[1];
		int m = 0;
		String s;
		LinkedList<Long> x = new LinkedList<Long>();
		char c;
		String parts[];
		try {
			// Open tokenizerOutput as randomaccessfile with read access
			RandomAccessFile reader = new RandomAccessFile(tokenizerOutput, "r");
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
					reader.close();
					return x;
				}if(s.compareTo(word)>0){
					reader.close();
					return x;
				}
			}
		} catch (IOException y) {
    		System.err.println(y);
		}
		return x;
	}

	/**
	* Uses the A-index to find the first and last index in the tokenizerOutput file 
	* that the program will search to find the given word.
	* Uses the method "incremented" to get the next available 3-letter combination and
	* uses that value to get the index where to stop the search.
	*
	* @param aIndex
	* the hashtable containing the A-index
	* @param word 
	* the word we are getting the range for
	* @return the range as a list of 2 longs containing start- and stop-index
	*/
	public long[] getRange(String file, Hashtable<String, Long> aIndex, String word){
		long[] in = {0,0};
		
		//Get the first 3 letters of the word:
		if(word.length()> 3){
			word = word.substring(0,3);
		}
		// Get the index corresponding to the correct 3-letter combination
		if(aIndex.containsKey(word)){
			in[0] = aIndex.get(word);
			//Use incremented to find the next available 3-letter combination
			word = incremented(word);
			while(!(aIndex.containsKey(word))){
				word = incremented(word);
			}

			in[1] = aIndex.get(word);
			return in;
		}
		return in;

	};


	/**
	* Method used to create an A-index which contains 3-letter combinations and an
	* index to the first word begining with each combination. 
	*
	* @param file
	* A file containing on each row, a keyword (and an index but we will not use this
	* index here). 
	* @param aIndex
	* A hashtable containing each 3-letter (or fewer) combination exsisting in the file 
	* and an index to where in the input-file the first word begining with it is. 
	*/
	public Hashtable<String, Long> createAIndex(String file, Hashtable<String, Long> aIndex){
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
        			if (aIndex.containsKey(sub)){
        				continue;
        			}
        			aIndex.put(sub, pointer);
        			pointer = reader.getFilePointer();
    			};
    		reader.close();
		} catch (IOException x) {
    	System.err.println(x);
		}
		return aIndex;
	}

	/**
	* Helpmethod for "getRange" used to find the next available 3-letter 
	* combination.
	*
	* @param original
	* original 3-letter combination
	* @return next available 3-letter combination
	*/
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

	/**
	* Writes a hastable to a file
	*
	* @param index
	* hashtable we wish to write to a file
	* @param file
	* file we wish to write to
	*/
	public void indexToFile(Hashtable<String, Long>index, String file){
		Enumeration<String> keys = index.keys();
		try{
			RandomAccessFile writer = new RandomAccessFile(file, "rw");
			while (keys.hasMoreElements()){
				String key = keys.nextElement();
				writer.writeBytes(key + " " + index.get(key)+ "\n");
			}
		writer.close();
		}catch(IOException x){
			System.err.println(x);
		}
	}

	/**
	* Gets an index from an existing file by reading each line, splitting it into keyword and
	* index and saves each set in a hashtable which is returned.
	*
	* @param file
	* The file that is storing the keywords and indexes
	* @return the input file as a hashtable with keywords and indexes
	*/
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
        			l = Long.parseLong(parts[1]);
        			index.put(s, l);
			}
		reader.close();
		}catch(IOException x){
			System.err.println(x);
		}
		return index;
	}

	/**
	* For each index in the linked list this method prints out the 30 chars
	* before the word begining at that index, the word and then the 30 chars after
	*
	* @param list
	* the linked list containing the position of each instance of the current word
	* @param wordLenght
	* lenght to be added to the trail of characters behind the word so that it really
	* prints the word + 30 chars
	*/
	public void writeSentences(LinkedList<Long> list, String file, int wordLength){
		byte[] b = new byte[(60 + wordLength)];
		int size = list.size();
		try{
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			for(int j = 0; j<size; j++){
				reader.seek((list.removeFirst()-30));
				for(int i = 0; i < (60+ wordLength); i++){
					b[i]= reader.readByte();
				}
				String s = new String(b, "ISO-8859-1");
				s = s.replaceAll("(\\n)", " ");
				System.out.println(s);
			}
		reader.close();
		}catch(IOException x){
			System.err.println(x);
		}

	}

	/**
	* Prints a question to the user and returns the answer given
	*
	* @param question
	* A string containing the question to be asked
	* @return the answer given by the user 
	*/
	public boolean askUser(String question){
		Scanner sc = new Scanner(System.in);
		System.out.println(question);
		String response = sc.nextLine();
		sc.close();
		if(response.toLowerCase().equals("j")){
			return true;
		}
		else if (response.toLowerCase().equals("n")){
			return false;
		}else{
			return askUser(question);
		}
	}
}
