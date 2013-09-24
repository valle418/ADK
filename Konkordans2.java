/**
* A program creating a concordance-database from a text to be used with the 
* command java Konkordans word-you-wish-to-look-up
*
* @author Therese Askling, taskling@kth.se
* @author Matilda Valleryd, valleryd@kth.se
*/

import java.util.*;
import java.io.*;
public class Konkordans2{
	
	// The files used:
	public static final String TOKENIZER_OUTPUT = "ut.txt";
	public static final String WORDLIST = "WordListFile.txt";
	public static final String INDEXLIST = "indexListFile.txt";
	public static final String AINDEX = "aIndex.txt";
	public static final String KORPUS = "korpus.txt";
	
	//The A-Index represented as datastructure:
	Hashtable<String, Long> aIndexMap = new Hashtable<String, Long>();
	
	public static void main(String[] args) {
		
		// Initiating konkordans object
		Konkordans2 app = new Konkordans2();
		
		// Check if the files already exist
		if(new File(AINDEX).isFile()){
			app.aIndexMap = app.readIndexFromFile();
		}
			
		// if not, create the index and save it to the file wordAndIndexFile
		else{
			//Create the aIndex, wordList and indexList from tokenizerOutput
			app.createTheData();
			
		}
		if(args.length>0){
			args[0] = args[0].toLowerCase();
			app.getRange(app.aIndexMap, args[0]);
		}

	
	}
	
	public void createTheData(){
		
		
		
		try {
			
			RandomAccessFile reader = new RandomAccessFile(TOKENIZER_OUTPUT, "r");
			
			String currentWord="";
			String oldWord = "";
			String word = "";
			String currentLine="";
			String currentLetters="";
			String oldLetters="";
			String letters="";
			String[] currentLineSplit;
			long pointerToIndexList;
			long pointerToWordList;
			long numberOfIndexes = 0;
			
			LinkedList<Long> currentIndexList = null;
			
			Iterator indexListIterator;
			
			RandomAccessFile writerIndexList = new RandomAccessFile(INDEXLIST, "rw");
			RandomAccessFile writerWordList = new RandomAccessFile(WORDLIST, "rw");
			RandomAccessFile writerAIndex = new RandomAccessFile(AINDEX, "rw");
			
			while ((currentLine = reader.readLine())!= null) {
			  
			  oldWord = word;
			  
			  oldLetters = letters;
  			
			  currentLineSplit = currentLine.split(" ");
			  word = currentLineSplit[0];
			  
			  if (word.length() < 3){
					letters = word;
      		
      		}else{
      			// set sub to the first three letters ín the word
      			letters = word.substring(0, 3);
     			}
			  
			  //If this is the current word, just add the index to the list 
			  
			  if(word.equals(currentWord)){
					currentIndexList.addLast(Long.parseLong(currentLineSplit[1]));
					numberOfIndexes++;
			  } 
			  
			  //If this is a new word:
				
			  else{
					//Write the old word and indexes to all the lists
					if (!oldWord.isEmpty()){ //Wont run if this is the first word
						
						currentIndexList.addFirst(numberOfIndexes);
						
						
						//Get pointerA to indexList file:
						pointerToIndexList = writerIndexList.getFilePointer();

						writerIndexList.writeBytes("\n");
						
						//Write numberOfIndexes to indexListFile:
						
						//Write currentIndexList to indexListFile:
						indexListIterator = currentIndexList.iterator();
						while(indexListIterator.hasNext()){
							String next = indexListIterator.next().toString();
							writerIndexList.writeBytes(next + " ");
						}
						
						
						
						
						//Check if this is new aIndex:
						if (!oldLetters.equals(currentLetters) ){
							//Get pointerB to WordListFile
							pointerToWordList = writerWordList.getFilePointer();
							//Add 3 first letters and pointer to aIndex
							writerAIndex.writeBytes(oldLetters + " " + pointerToWordList + "\n");
							
							//Add to the TreeMap for easy access:
							aIndexMap.put(oldLetters, pointerToWordList);
							
							//Change currentLetters:
							currentLetters = oldLetters;
						}
							
						
						//Write oldWord and pointerA to Wordlistfile:
						writerWordList.writeBytes(oldWord + " " + pointerToIndexList + " " + numberOfIndexes + "\n" );
						
					}
					
					//Begin the new word and indexList				
					currentIndexList = new LinkedList<Long>();
					currentWord = word;
					
					
					numberOfIndexes=1;
					currentIndexList.addLast(Long.parseLong(currentLineSplit[1]));
					
				}
			  
			}
			reader.close();
			writerWordList.close();
			writerIndexList.close();
			writerAIndex.close();
			
			
		} catch (IOException x) {
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
	public Hashtable<String, Long> readIndexFromFile(){
		Hashtable<String, Long> index = new Hashtable<String, Long>();
		String line = null;
		String[] parts = {" ", " ", " "};
		long pointer;
		String word = "";
		try{
			RandomAccessFile reader = new RandomAccessFile(AINDEX, "r");
			while ((line = reader.readLine()) != null) {
        		parts = line.split(" ");
        		if(parts.length == 2){
       				word = parts[0];
       				pointer = Long.parseLong(parts[1]);
       				index.put(word, pointer);
       			}	
			}
		reader.close();
		}catch(IOException x){
			System.err.println(x);
		}
		return index;
	};

	public void getRange(Hashtable<String, Long> aIndex, String word){
		long[] in = {0,0};
		String subWord = word;
		
		//Get the first 3 letters of the word:
		if(word.length()> 3){
			subWord = subWord.substring(0,3);
		}
		// Get the index corresponding to the correct 3-letter combination
		if(aIndex.containsKey(subWord)){
			in[0] = aIndex.get(subWord);
			//Use incremented to find the next available 3-letter combination
			subWord = incremented(subWord);
			while(!(aIndex.containsKey(subWord))){
				subWord = incremented(subWord);
			}

			in[1] = aIndex.get(subWord);
		}
		findWordPositions(word, in);
	};

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


	/** Find all the positions of the word that you are looking for from a indexfile
	* Parameters: word you are looking for, indexfile-name, array with two long 
	* numbers as the range in which look for the word
	* Returns: a LinkedList of all the positions where the word exists stored as long numbers
	*/
	public LinkedList<Long> findWordPositions(String word, long[] in){
		// Create local varibles
		int i = (int)in[0];
		int j = (int)in[1];
		int m = 0;
		long numberOfIndexes = 0;
		String s;
		LinkedList<Long> x = new LinkedList<Long>();
		char c;
		String parts[];
		try {
			// Open tokenizerOutput as randomaccessfile with read access
			RandomAccessFile readerA = new RandomAccessFile(WORDLIST, "r");
			RandomAccessFile readerB = new RandomAccessFile(INDEXLIST, "r");
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
				if (!(parts[0].equals(word))){
					s = readerA.readLine();
					parts = s.split(" ");
				}
				// if the word is the one you are looking for
				if (parts[0].equals(word)){
					
					
					System.out.println("Det finns "+ parts[2] + " förekomster av ordet.");
					if(Long.parseLong(parts[2])<=25){
						// set pointer as second element in list
						long pointer = Long.parseLong(parts[1]);
						// set readerB to the position given by pointer
						readerB.seek(pointer);
						// set string t as the value at line in file
						readerB.readLine();
						String t = readerB.readLine();
						// split t at space
						String[] wordPositions = t.split(" ");
						numberOfIndexes = Long.parseLong(wordPositions[0]);
						writeSentences(Arrays.copyOfRange(wordPositions, 1, wordPositions.length), word.length());
					}
					else{
						
						if(askUser("Vill du skriva ut alla förekomster? \nJ(Ja) or N(Nej)")){
							// set pointer as second element in list
							long pointer = Long.parseLong(parts[1]);
							// set readerB to the position given by pointer
							readerB.seek(pointer);
							// set string t as the value at line in file
							readerB.readLine();
							String t = readerB.readLine();
							// split t at space
							String[] wordPositions = t.split(" ");
							numberOfIndexes = Long.parseLong(wordPositions[0]);
							writeSentences(Arrays.copyOfRange(wordPositions, 1, wordPositions.length), word.length());
						}
					}
					
					// close readers
					readerA.close();
					readerB.close();
					return x;
				}if(s.compareTo(word)>0){
					// close readers
					readerA.close();
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
	* For each index in the linked list this method prints out the 30 chars
	* before the word begining at that index, the word and then the 30 chars after
	*
	* @param list
	* the linked list containing the position of each instance of the current word
	* @param wordLenght
	* lenght to be added to the trail of characters behind the word so that it really
	* prints the word + 30 chars
	*/
	public void writeSentences(String[] wordPositions, int wordLength){
		System.out.println("writing Sentences");
		byte[] b = new byte[(60 + wordLength)];
		long l;
		try{
			RandomAccessFile reader = new RandomAccessFile(KORPUS, "r");
			for(int j = 0; j<wordPositions.length; j++){
				l = Long.parseLong(wordPositions[j]);
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

}
