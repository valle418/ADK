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
	String tokenizerOutput = "ut.txt";
	String wordList = "WordListFile.txt";
	String indexList = "indexListFile.txt";
	String aIndex = "aIndex.txt";
	String korpus = "korpus";
	
	//The A-Index represented as datastructure:
	TreeMap<String, Long> aIndexMap = new TreeMap<String, Long>();
	
	public static void main(String[] args) {
		
		// Initiating konkordans object
		Konkordans2 app = new Konkordans2();
		
		// Check if the files already exist
		/*if(new File(aIndex).isFile()){
			// Read in the files using a function
		}
			
		// if not, create the index and save it to the file wordAndIndexFile
		else{*/
			//Create the aIndex, wordList and indexList from tokenizerOutput
			app.createTheData();
			
		//}
	
	}
	
	public void createTheData(){
		
		
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(tokenizerOutput));
			
			String currentWord="";
			String oldWord = "";
			String word = "";
			String currentLine="";
			String currentLetters="";
			String oldLetters="";
			String letters="";
			String[] currentLineSplit;
			long numberOfIndexes = 0;
			long pointerToIndexList;
			long pointerToWordList;
			
			LinkedList<Long> currentIndexList = null;
			
			Iterator indexListIterator;
			
			RandomAccessFile writerIndexList = new RandomAccessFile(indexList, "rw");
			RandomAccessFile writerWordList = new RandomAccessFile(wordList, "rw");
			RandomAccessFile writerAIndex = new RandomAccessFile(aIndex, "rw");
			
			while (reader.ready()) {
			  currentLine = reader.readLine();
			  
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
						
						//Write numberOfIndexes to indexListFile:
						writerIndexList.writeBytes("\n" + numberOfIndexes + " ");
						//Write currentIndexList to indexListFile:
						indexListIterator = currentIndexList.iterator();
						while(indexListIterator.hasNext()){
							String next = indexListIterator.next().toString();
							writerIndexList.writeBytes( next + " ");
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
						writerWordList.writeBytes(oldWord + " " + pointerToIndexList + "\n" );
						
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
}
