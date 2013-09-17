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
		String tokenizerOutput = "ut.txt";
		String wordAndIndexFile = "aIndex.txt";
		String onlyIndexFile = "bIndex.txt";
		String firstThreeIndexFile = "massaIndex.txt";
		String korpus = "korpus.txt";

		// create a hashtable for the a-index
		TreeMap<String, LinkedList<Long>> indexList = new TreeMap<String, LinkedList<Long>>();
		Hashtable<String, Long> firstThreeIndex = new Hashtable<String, Long>();
		// if the index has previously been written to a file, create a hashtable from it
		if(new File(wordAndIndexFile).isFile()){	
			firstThreeIndex = app.readIndexFromFile(firstThreeIndexFile);
		// if not, create the index and save it to the file wordAndIndexFile
		}else{
			indexList = app.createIndex(tokenizerOutput);
			firstThreeIndex = app.indexToFile(indexList, wordAndIndexFile, onlyIndexFile);
			app.firstThreeIndexToFile(firstThreeIndex, firstThreeIndexFile);
		}
		// initilize a list to contain where to start and stop the to be search
		long[] in = {0,0};
		// If the user has provided a word to be searched, get it
		if(args.length>0){
			args[0] = args[0].toLowerCase();
			// get the start- and stop-indexes to be searched
			in = app.getRange(firstThreeIndex, args[0]);	
			// create LinkedList for the word positions
			LinkedList<Long> x = app.findWordPositions(args[0], wordAndIndexFile, onlyIndexFile, in);
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
	public LinkedList<Long> findWordPositions(String word, String wordAndIndexFile, String onlyIndexFile, long[] in){
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
			RandomAccessFile readerA = new RandomAccessFile(wordAndIndexFile, "r");
			RandomAccessFile readerB = new RandomAccessFile(onlyIndexFile, "r");
			// While the range to look in is longer than 1000
			while ((j-i)>1000){
				// give m a value in the middle of j and i
				m = ((i+j)/2);
				// positions read in file at m-position
				readerA.seek(m);
				// read one line first, in case m is in the middle of a line
				s = readerA.readLine();
				// read next line, guaranteed a full line
				s = readerA.readLine();
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
			readerA.seek(i);
			// read one line first, in case i-pos is in the middle of a line
			// Keep going until you find word, or you find a word which is
			// greater than the one you are looking for
			while (true){
				// read line from A-file
				s = readerA.readLine();
				// splitLine at space
				parts = s.split(" ");
				// if the word is the one you are looking for
				if (parts[0].equals(word)){
					// set pointer as second element in list
					long pointer = Long.parseLong(parts[1]);
					// set readerB to the position given by pointer
					readerB.seek(pointer);
					// set string t as the value at line in file
					String t = readerB.readLine();
					// split t at space
					String[] party = t.split(" ");
					Long lon;

					for(int y = 0; y<party.length; y++){
						lon = Long.parseLong(party[y]);
						x.addFirst(lon);
					}
					// close readers
					readerA.close();
					readerB.close();
					return x;
				}if(s.compareTo(word)>0){
					// close readers
					readerB.close();
					readerB.close();
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
	public long[] getRange(Hashtable<String, Long> firstThreeIndex, String word){
		long[] in = {0,0};
		
		//Get the first 3 letters of the word:
		if(word.length()> 3){
			word = word.substring(0,3);
		}
		// Get the index corresponding to the correct 3-letter combination
		if(firstThreeIndex.containsKey(word)){
			in[0] = firstThreeIndex.get(word);
			//Use incremented to find the next available 3-letter combination
			word = incremented(word);
			while(!(firstThreeIndex.containsKey(word))){
				word = incremented(word);
			}
			in[1] = firstThreeIndex.get(word);
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
	public TreeMap<String, LinkedList<Long>> createIndex(String tokenizerOutput){
		TreeMap<String, LinkedList<Long>> indexList = new TreeMap<String, LinkedList<Long>>();
		try {
			RandomAccessFile reader = new RandomAccessFile(tokenizerOutput, "r");
			String line = null;
			String[] parts;
			long pointer = reader.getFilePointer();
    			while ((line = reader.readLine()) != null) {
        			parts = line.split(" ");
        			String word = parts[0];
     				if(indexList.containsKey(word)){
     					indexList.get(word).addLast(Long.parseLong(parts[1]));
     				}
     				else{
     					LinkedList<Long> list = new LinkedList<Long>();
     					list.addFirst(Long.parseLong(parts[1]));
      					indexList.put(word, list);
     				}
    			};
    		reader.close();
		} catch (IOException x) {
    	System.err.println(x);
		}
		return indexList;
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
	public Hashtable<String, Long> indexToFile(TreeMap<String, LinkedList<Long>> index, String wordAndIndexFile, String onlyIndexfile){
		// An iterator over all the keys, all the words, in the treemap.
		Iterator<String> keys = index.descendingKeySet().descendingIterator();
		// A hashtable for the word and the position of that word's indexes in an other file.  
		Hashtable<String, Long> firstThreeIndex = new Hashtable<String, Long>();
		
		Iterator<Long> indexList;;
		String key;
		String sub;
		Long pointerToB;
		Long pointerToA;
		try{
			// Two writers for two files
			RandomAccessFile writerOne = new RandomAccessFile(wordAndIndexFile, "rw");
			RandomAccessFile writerTwo = new RandomAccessFile(onlyIndexfile, "rw");
			// While there are more words in the treemap
			while (keys.hasNext()){
				// set key variable to next element in the iterator
				key = keys.next();
				// // Iterator over all indexes for the word in key
				indexList = index.get(key).descendingIterator();
				// get fileposition in the onlyindexes file
				pointerToB = writerTwo.getFilePointer();
				// while there are indexes left in the list
				while(indexList.hasNext()){
					// write the index to the onlyindexfile with a space
					writerTwo.writeBytes(indexList.next() + " ");
				}
				// end writing with a newline
				writerTwo.writeBytes("\n");
				// set pointerToA as the position in the A-file
				pointerToA = writerOne.getFilePointer();
				// Write the word, space and pointerToB to wordAndIndexFile
				writerOne.writeBytes(key + " " + pointerToB +  "\n");
				// if the length of the word is less than 3
				if (key.length() < 3){
					// set the string sub to the word
        			sub = key;
        		// if the length of the word is 3 or more
        		}else{
        			// set sub to the first three letters ín the word
  	     			sub = key.substring(0, 3);
       			}
       			// if the firstThreeIndex already contains sub, continue
       			if (!(firstThreeIndex.containsKey(sub))){
       				firstThreeIndex.put(sub, pointerToA);
				}
			}
		writerOne.close();
		writerTwo.close();
		return firstThreeIndex;
		}catch(IOException x){ 
			System.err.println(x);
		}
		return firstThreeIndex;
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
        		if(parts.length == 2){
       				s = parts[0];
       				l = Long.parseLong(parts[1]);
       				index.put(s, l);
       			}	
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
		long l;
		try{
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			for(int j = 0; j<size; j++){
				l= list.removeLast();
				reader.seek((l-30));
				for(int i = 0; i < (wordLength+60); i++){
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

	public void firstThreeIndexToFile(Hashtable<String, Long>index, String file){
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
	
}
