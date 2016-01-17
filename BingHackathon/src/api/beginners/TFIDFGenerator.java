/**
 * 
 */
package api.beginners;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hackathon.bing.NGram;

/**
 * @author Amit_Deshmane
 *
 */
public class TFIDFGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("input/BingHackathonTrainingData.txt"));
		FileWriter fwTrain = new FileWriter("input/customFormatTFIDFTrain_60.txt");
		FileWriter fwDev = new FileWriter("input/customFormatTFIDFDev_20.txt");
		FileWriter fwTest = new FileWriter("input/customFormatTFIDFTest_20.txt");
		FileWriter fwSVM = new FileWriter("input/libSVMFormat_Ngram.txt");
		String line = null;
		StringBuffer sb = new StringBuffer();
		int lines = 0;
		Set<String> tokenSet = new LinkedHashSet<String>();
		Set<String> bigramSet = new LinkedHashSet<String>();
		Set<String> trigramSet = new LinkedHashSet<String>();
		/*Set<String> topicSet = new LinkedHashSet<String>();*/
		List<String> docTopics = new ArrayList<String>();
		List<List<String>> docTokens = new ArrayList<List<String>>();
		List<List<String>> docSentences = new ArrayList<List<String>>();
		//read data to process
		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			/*topicSet.add(row[1].trim());*/
			docTopics.add(row[1].trim());
			sb.append(row[1]+",");
			String author = row[3].replaceAll("\\ ", "_").replaceAll(";", ",");
			String title = row[4].replaceAll("\\ |\\.", ",");
			String text = row[5].replaceAll("\\ |\\.", ",");
			text = text.replaceAll(",,", ",");
			if(text.charAt(text.length()-1) == ','){
				text = text.substring(0, text.length() - 1);
			}
			String[] tokens = text.split(",");
			List<String> tokensList = Arrays.asList(tokens);
			docTokens.add(tokensList);
			tokenSet.addAll(tokensList);
			sb.append(author+",");
			sb.append(title+",");
			sb.append(text+"\n");
			
			String[] sentences = row[5].split("\\.");
			List<String> sentencesList = new ArrayList<String>();
			for(String sentence : sentences){
				if(sentence.trim().equals(""))continue;
				sentencesList.add(sentence);
				NGram bigram = new NGram(sentence, 2);
				bigramSet.addAll(bigram.list());
				NGram trigram = new NGram(sentence, 3);
				trigramSet.addAll(trigram.list());
			}
			docSentences.add(sentencesList);
			
			lines++;
		}
		String [] rows = sb.toString().split("\n");
		/*double[][] topicTokenfreq = new double[topicSet.size()][tokenSet.size()];*/
		double[][] docTokenfreq = new double[rows.length][tokenSet.size()];
		double[] tokenDocFreq = new double[tokenSet.size()];
		double[] idf = new double[tokenSet.size()];
		List<String> tokenList = new ArrayList<String>(tokenSet);
		
		double[][] docBigramfreq = new double[rows.length][bigramSet.size()];
		double[] bigramDocFreq = new double[bigramSet.size()];
		double[] idfBigram = new double[bigramSet.size()];
		List<String> bigramList = new ArrayList<String>(bigramSet);
		
		double[][] docTrigramfreq = new double[rows.length][trigramSet.size()];
		double[] trigramDocFreq = new double[trigramSet.size()];
		double[] idfTrigram = new double[trigramSet.size()];
		List<String> trigramList = new ArrayList<String>(trigramSet);
		/*List<String> topicList = new ArrayList<String>(topicSet);*/
		//compute freq/docFreq/topicFreq
		for(int index = 0; index < rows.length; index++){
			List<String> tokens = docTokens.get(index);
			Set<String> tokensAsSet = new HashSet<String>(tokens);
			for(int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++){
				docTokenfreq[index][tokenList.indexOf(tokens.get(tokenIndex))] = docTokenfreq[index][tokenList.indexOf(tokens.get(tokenIndex))] + 1;
				/*topicTokenfreq[topicList.indexOf(docTopics.get(index))][tokenList.indexOf(tokens.get(tokenIndex))] = topicTokenfreq[topicList.indexOf(docTopics.get(index))][tokenList.indexOf(tokens.get(tokenIndex))] + 1;*/
			}
			Set<String> bigramAsSet = new HashSet<String>();
			Set<String> trigramAsSet = new HashSet<String>();
			for(String sentence : docSentences.get(index)){
				NGram bigram = new NGram(sentence, 2);
				bigramAsSet.addAll(bigram.list());
				for(String bigramString : bigram.list()){
					docBigramfreq[index][bigramList.indexOf(bigramString)] = docBigramfreq[index][bigramList.indexOf(bigramString)] + 1;
				}
				NGram trigram = new NGram(sentence, 3);
				trigramAsSet.addAll(trigram.list());
				for(String trigramString : trigram.list()){
					docTrigramfreq[index][trigramList.indexOf(trigramString)] = docTrigramfreq[index][trigramList.indexOf(trigramString)] + 1;
				}
			}
			Iterator<String> iterator = tokensAsSet.iterator();
			while(iterator.hasNext()){
				String token = iterator.next();
				tokenDocFreq[tokenList.indexOf(token)] = tokenDocFreq[tokenList.indexOf(token)] + 1;
			}
			iterator = bigramAsSet.iterator();
			while(iterator.hasNext()){
				String bigram = iterator.next();
				bigramDocFreq[bigramList.indexOf(bigram)] = bigramDocFreq[bigramList.indexOf(bigram)] + 1;
			}
			iterator = trigramAsSet.iterator();
			while(iterator.hasNext()){
				String trigram = iterator.next();
				trigramDocFreq[trigramList.indexOf(trigram)] = trigramDocFreq[trigramList.indexOf(trigram)] + 1;
			}
		}
		//get idf
		for(int index = 0; index < tokenList.size(); index++){
			idf[index] = Math.log(lines/tokenDocFreq[index]);
		}
		for(int index = 0; index < tokenList.size(); index++){
			idfBigram[index] = Math.log(lines/bigramDocFreq[index]);
		}
		for(int index = 0; index < tokenList.size(); index++){
			idfTrigram[index] = Math.log(lines/trigramDocFreq[index]);
		}
		//get tf-idf vectors for each doc
		StringBuffer svmFormat = new StringBuffer();
		for(int index = 0; index < lines; index++){
			
			svmFormat.append(docTopics.get(index) + " ");
			double [] vector = new double[tokenList.size() + bigramList.size() + trigramList.size()];
			List<Double> vectorDup = new ArrayList<Double>();
			for(int tokenIndex = 0; tokenIndex < tokenList.size(); tokenIndex++){
				double tfidf = docTokenfreq[index][tokenIndex] * idf[tokenIndex];
				vector[tokenIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((tokenIndex+1) + ":"+tfidf+" ");
				}
			}
			for(int bigramIndex = 0; bigramIndex < bigramList.size(); bigramIndex++){
				double tfidf = docBigramfreq[index][bigramIndex] * idfBigram[bigramIndex];
				vector[tokenList.size() + bigramIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((tokenList.size() + bigramIndex + 1) + ":"+tfidf+" ");
				}
			}
			for(int trigramIndex = 0; trigramIndex < trigramList.size(); trigramIndex++){
				double tfidf = docTrigramfreq[index][trigramIndex] * idfTrigram[trigramIndex];
				vector[tokenList.size() + bigramList.size() + trigramIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((tokenList.size() + bigramList.size() + trigramIndex+1) + ":"+tfidf+" ");
				}
			}
			svmFormat.append("\n");
			String vectorString = docTopics.get(index) + "," + vectorDup.toString().replaceAll("\\[|\\]|\\ ", "");
			if(index < 0.6 * lines){
				fwTrain.write(vectorString + "\n");
			}
			else if(index < 0.8 * lines){
				fwDev.write(vectorString + "\n");
			}
			else{
				fwTest.write(vectorString + "\n");
			}
		}
		fwSVM.write(svmFormat.toString());
		fwTrain.close();
		fwDev.close();
		fwTest.close();
		fwSVM.close();
		br.close();
	}

}
