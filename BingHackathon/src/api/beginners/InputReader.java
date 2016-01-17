/**
 * 
 */
package api.beginners;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hackathon.bing.NGram;

/**
 * @author Amit_Deshmane
 *
 */
public class InputReader {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("input/BingHackathonTrainingData.txt"));
		
		double trainThreshold = 1;
		double devThreshold = 0;
		
		FileWriter fwTrain = new FileWriter("input/customFormatAuthorTrain_100.txt");
		FileWriter fwDev = new FileWriter("input/customFormatAuthorDev_0.txt");
		FileWriter fwTest = new FileWriter("input/customFormatAuthorTest_0.txt");
		
		String line = null;
		StringBuffer sb = new StringBuffer();
		int lines = 0;
		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			
			sb.append(row[1]+",");
			String author = row[3].replaceAll("\\ ", "_").replaceAll(";", ",");
			String title = row[4].replaceAll("\\ |\\.", ",");
			String text = row[5].replaceAll("\\ |\\.", ",");
			text = text.replaceAll(",,", ",");
			if(text.charAt(text.length()-1) == ','){
				text = text.substring(0, text.length() - 1);
			}
			
			String[] sentences = row[5].split("\\.");
			List<String> sentencesList = new ArrayList<String>();
			StringBuffer bigramBuffer = new StringBuffer();
			StringBuffer trigramBuffer = new StringBuffer();
			for(String sentence : sentences){
				if(sentence.trim().equals(""))continue;
				sentencesList.add(sentence);
				NGram bigram = new NGram(sentence, 2);
				for(String bigramString : bigram.list()){
					bigramBuffer.append(bigramString.replaceAll("\\ ", "_") + ",");
				}
				NGram trigram = new NGram(sentence, 3);
				for(String trigramString : trigram.list()){
					trigramBuffer.append(trigramString.replaceAll("\\ ", "_") + ",");
				}
			}
			
			
			/*sb.append(title+",");*/
			/*sb.append(bigramBuffer.toString());
			sb.append(trigramBuffer.toString());*/
			sb.append(author+",");
			sb.append(text);
			sb.append("\n");
			lines++;
		}
		String [] rows = sb.toString().split("\n");
		for(int index = 0; index < rows.length; index++){
			if(index < trainThreshold * rows.length){
				fwTrain.write(rows[index] + "\n");
			}
			else if(index < (trainThreshold + devThreshold) * rows.length){
				fwDev.write(rows[index] + "\n");
			}
			else{
				fwTest.write(rows[index] + "\n");
			}
		}
		fwTrain.close();
		fwDev.close();
		fwTest.close();
		br.close();
	}

}
