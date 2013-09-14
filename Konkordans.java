import java.util.*;
import java.io.*;
public class Konkordans{
	
	public static void main(String[] args) {
		Konkordans app = new Konkordans();
		String file = "ut.txt";
		String aIndex = "aIndex.txt";
		String txtFile = "korpus.txt";

		Hashtable<String, Long> index = new Hashtable<String, Long>();
		if(new File(aIndex).isFile()){
			index = app.readIndexFromFile(aIndex);
		}else{
			index = app.createIndex(file, index);
			app.indexToFile(index, aIndex);
		}
		long[] in = {0,0};
		if(args.length>0){
			in = app.runSeek(file, index, args[0]);	
			LinkedList<Long> x = app.findWords(args[0],file, in);
			System.out.println("Det finns "+ x.size() + " förekomster av ordet.");
			if(x.size()>25){
				System.out.println("Vill du skriva ut alla förekomster?");
			}else if (x.size()> 0){
				app.writeSentences(x, txtFile);
			}
		};
		
	};

	public LinkedList<Long> findWords(String word, String file, long[] in){
		int i = (int)in[0];
		int j = (int)in[1];
		int m = 0;
		String s;
		LinkedList<Long> x = new LinkedList<Long>();
		char c;
		String parts[];
		try {
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			while ((j-i)>1000){
				m = ((i+j)/2);
				reader.seek(m);
				s = reader.readLine();
				s = reader.readLine();
				parts = s.split(" ");
				if(word.compareTo(parts[0])>0){
					i = m;
				}else{
					j= m;
				}
			}
			reader.seek(i);
			while (true){
				s = reader.readLine();
				s = reader.readLine();
				parts = s.split(" ");
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

	public long[] runSeek(String file, Hashtable<String, Long> index, String word){
		long[] in = {0,0};
		if(word.length()> 3){
			word = word.substring(0,3);
		}
		if(index.containsKey(word)){
			in[0] = index.get(incremented(word));
			in[1] = index.get(incremented(incremented(word)));
			return in;
		}
		System.out.println(file);
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
			PrintWriter writer = new PrintWriter(file, "ISO-8859-1");
		while (keys.hasMoreElements()){
			String key = keys.nextElement();
			writer.println(key + " " + index.get(key));
		}

		}catch(IOException x){
			System.err.println(x);
		}
	}

	public Hashtable<String, Long> readIndexFromFile(String file){
		Hashtable<String, Long> index = new Hashtable<String, Long>();
		String line = null;
		String[] parts = {" ", " "};
		long l = 0;
		String s = "";
		try{
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			while ((line = reader.readLine()) != null) {
        			parts = line.split(" ");
        			s = parts[0];
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
