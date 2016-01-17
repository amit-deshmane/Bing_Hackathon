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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hackathon.bing.NGram;

/**
 * @author Amit_Deshmane
 *
 */
public class TFIDFGeneratorDenoise {
	public static int supportSize = 7000;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
		FileWriter fwTrain = new FileWriter("input/customFormatTFIDFTrain_60.txt");
		FileWriter fwDev = new FileWriter("input/customFormatTFIDFDev_20.txt");
		FileWriter fwTest = new FileWriter("input/customFormatTFIDFTest_20.txt");
		FileWriter fwSVM = new FileWriter("input/libSVMFormat_NgramRegressionTest.txt");
		String authorFeatureFilePath = "resources/authorFeatures.txt";
		String tokenFeatureFilePath = "resources/tokenFeatures.txt";
		String bigramFeatureFilePath = "resources/bigramFeatures.txt";
		String trigramFeatureFilePath = "resources/trigramFeatures.txt";
		boolean loadExisting = true;
		
		String line = null;
		int lines = 0;

		Set<String> authorSet = null;
		Set<String> tokenSet = null;
		Set<String> bigramSet = null;
		Set<String> trigramSet = null;
		if(loadExisting){
			authorSet = loadFeatures(authorFeatureFilePath);
			tokenSet = loadFeatures(tokenFeatureFilePath);
			bigramSet = loadFeatures(bigramFeatureFilePath);
			trigramSet = loadFeatures(trigramFeatureFilePath);
		}
		else{
			authorSet = getRelevantAuthors(br);
			saveFeatures(authorSet, authorFeatureFilePath);
			br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
			tokenSet = getRelevantTokens(br);
			saveFeatures(tokenSet, tokenFeatureFilePath);
			br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
			bigramSet = getRelevantBigrams(br);
			saveFeatures(bigramSet, bigramFeatureFilePath);
			br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
			trigramSet = getRelevantTrigrams(br);
			saveFeatures(trigramSet, trigramFeatureFilePath);
		}
		br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
		lines = getRows(br);
		
		double[][] docTokenfreq = new double[lines][tokenSet.size()];
		double[] tokenDocFreq = new double[tokenSet.size()];
		double[] idf = new double[tokenSet.size()];
		
		List<String> authorList = new ArrayList<String>(authorSet);
		List<String> tokenList = new ArrayList<String>(tokenSet);
		
		double[][] docBigramfreq = new double[lines][bigramSet.size()];
		double[] bigramDocFreq = new double[bigramSet.size()];
		double[] idfBigram = new double[bigramSet.size()];
		List<String> bigramList = new ArrayList<String>(bigramSet);

		double[][] docTrigramfreq = new double[lines][trigramSet.size()];
		double[] trigramDocFreq = new double[trigramSet.size()];
		double[] idfTrigram = new double[trigramSet.size()];
		List<String> trigramList = new ArrayList<String>(trigramSet);
		//compute freq/docFreq/topicFreq
		List<String> docTopics = new ArrayList<String>();
		List<String> docYears = new ArrayList<String>();
		List<List<String>> authorsDocList = new ArrayList<List<String>>();
		int index = 0;
		br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			docTopics.add(row[1].trim());
			docYears.add(row[2].trim());
			/*topicSet.add(row[1].trim());*/
			String[] authors = row[3].split(";");
			authorsDocList.add(Arrays.asList(authors));
			String title = row[4].replaceAll("\\ ", ",");
			String text = row[5].replaceAll("\\ |\\.", ",");
			text = text.replaceAll(",,", ",");
			/*text = title + "," + text;*/
			if(text.charAt(text.length()-1) == ','){
				text = text.substring(0, text.length() - 1);
			}
			String[] tokens = text.split(",");

			Set<String> tokensAsSet = new HashSet<String>(Arrays.asList(tokens));
			for(int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++){
				if(tokenList.contains(tokens[tokenIndex])){
					docTokenfreq[index][tokenList.indexOf(tokens[tokenIndex])] = docTokenfreq[index][tokenList.indexOf(tokens[tokenIndex])] + 1;
				}
				/*topicTokenfreq[topicList.indexOf(docTopics.get(index))][tokenList.indexOf(tokens.get(tokenIndex))] = topicTokenfreq[topicList.indexOf(docTopics.get(index))][tokenList.indexOf(tokens.get(tokenIndex))] + 1;*/
			}
			String totalText = /*row[4] + "." + */row[5];
			String[] sentences = totalText.split("\\.");
			Set<String> bigramAsSet = new HashSet<String>();
			Set<String> trigramAsSet = new HashSet<String>();
			for(String sentence : sentences){
				NGram bigram = new NGram(sentence, 2);
				List<String> sentenceBigram = bigram.list();
				bigramAsSet.addAll(sentenceBigram);
				for(String bigramString : sentenceBigram){
					if(bigramList.contains(bigramString)){
						docBigramfreq[index][bigramList.indexOf(bigramString)] = docBigramfreq[index][bigramList.indexOf(bigramString)] + 1;
					}
				}
				NGram trigram = new NGram(sentence, 3);
				List<String> sentenceTrigram = trigram.list();
				trigramAsSet.addAll(sentenceTrigram);
				for(String trigramString : sentenceTrigram){
					if(trigramList.contains(trigramString)){
						docTrigramfreq[index][trigramList.indexOf(trigramString)] = docTrigramfreq[index][trigramList.indexOf(trigramString)] + 1;
					}
				}
			}
			Iterator<String> iterator = tokensAsSet.iterator();
			while(iterator.hasNext()){
				String token = iterator.next();
				if(tokenList.contains(token)){
					tokenDocFreq[tokenList.indexOf(token)] = tokenDocFreq[tokenList.indexOf(token)] + 1;
				}
			}
			iterator = bigramAsSet.iterator();
			while(iterator.hasNext()){
				String bigram = iterator.next();
				if(bigramList.contains(bigram)){
					bigramDocFreq[bigramList.indexOf(bigram)] = bigramDocFreq[bigramList.indexOf(bigram)] + 1;
				}
			}
			iterator = trigramAsSet.iterator();
			while(iterator.hasNext()){
				String trigram = iterator.next();
				if(trigramList.contains(trigram)){
					trigramDocFreq[trigramList.indexOf(trigram)] = trigramDocFreq[trigramList.indexOf(trigram)] + 1;
				}
			}
			index++;
		}
		//get idf
		for(index = 0; index < tokenList.size(); index++){
			idf[index] = Math.log(lines/tokenDocFreq[index]);
		}
		for(index = 0; index < bigramList.size(); index++){
			idfBigram[index] = Math.log(lines/bigramDocFreq[index]);
		}
		for(index = 0; index < trigramList.size(); index++){
			idfTrigram[index] = Math.log(lines/trigramDocFreq[index]);
		}
		//get tf-idf vectors for each doc
		StringBuffer svmFormat = new StringBuffer();
		for(index = 0; index < lines; index++){
			
			svmFormat.append(docYears.get(index) + " ");
			double [] vector = new double[tokenList.size() + bigramList.size() + trigramList.size()];
			List<Double> vectorDup = new ArrayList<Double>();
			
			for(int tokenIndex = 0; tokenIndex < authorList.size(); tokenIndex++){
				double tfidf = (authorsDocList.get(index).contains(authorList.get(tokenIndex)))?0.5:0;
				vector[tokenIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((tokenIndex+1) + ":"+tfidf+" ");
				}
			}
			for(int tokenIndex = 0; tokenIndex < tokenList.size(); tokenIndex++){
				double tfidf = docTokenfreq[index][tokenIndex] * idf[tokenIndex];
				vector[tokenIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((authorList.size() + tokenIndex + 1) + ":"+tfidf+" ");
				}
			}
			for(int bigramIndex = 0; bigramIndex < bigramList.size(); bigramIndex++){
				double tfidf = docBigramfreq[index][bigramIndex] * idfBigram[bigramIndex];
				vector[tokenList.size() + bigramIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((authorList.size() + tokenList.size() + bigramIndex + 1) + ":"+tfidf+" ");
				}
			}
			for(int trigramIndex = 0; trigramIndex < trigramList.size(); trigramIndex++){
				double tfidf = docTrigramfreq[index][trigramIndex] * idfTrigram[trigramIndex];
				vector[tokenList.size() + bigramList.size() + trigramIndex] = tfidf;
				vectorDup.add(tfidf);
				if(tfidf > 0){
					svmFormat.append((authorList.size() + tokenList.size() + bigramList.size() + trigramIndex+1) + ":"+tfidf+" ");
				}
			}
			svmFormat.append("\n");
			String vectorString = docYears.get(index) + "," + vectorDup.toString().replaceAll("\\[|\\]|\\ ", "");
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

	private static Set<String> getRelevantAuthors(BufferedReader br) throws IOException {
		String line = null;
		Set<String> tokensList = new LinkedHashSet<String>();

		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			String[] authors = row[3].split(";");
			for(String token: authors){
				tokensList.add(token);
			}
		}
		br.close();
		return tokensList;
	}

	private static Set<String> getRelevantTrigrams(BufferedReader br) throws IOException {
		String line = null;
		Map<String, Integer> tokenWithIndex = new HashMap<String, Integer>();
		List<String> tokensList = new ArrayList<String>();

		int[] tokenFreq = new int[supportSize];
		Set<Integer> noiseIndices = new HashSet<Integer>();
		int availableIndex = 0;

		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			String text = /*row[4]+"."+*/row[5];
			String[] sentences = text.split("\\.");
			for(String sentence : sentences){
				NGram trigram = new NGram(sentence, 3);
				for(String token : trigram.list()){
					if(tokenWithIndex.keySet().contains(token)){
						tokenFreq[tokenWithIndex.get(token)] = tokenFreq[tokenWithIndex.get(token)] + 1;
					}
					else{
						int denoiseFreq = 1;
						while(availableIndex >= supportSize && noiseIndices.size() == 0){
							denoise(denoiseFreq, noiseIndices, tokenFreq, tokenWithIndex, tokensList);
							denoiseFreq = denoiseFreq + 1;
						}
						if(availableIndex < supportSize){
							tokenFreq[availableIndex] = 1;
							tokenWithIndex.put(token, availableIndex);
							availableIndex++;
							tokensList.add(token);
						}
						else{
							Integer newIndex = noiseIndices.iterator().next();
							tokenFreq[newIndex] = 1;
							noiseIndices.remove(newIndex);
							tokensList.set(newIndex, token);
							tokenWithIndex.put(token, newIndex);
						}
					}
				}
			}
			
		}
		/*denoise(1, noiseIndices, tokenFreq, tokenWithIndex, tokensList);*/
		br.close();
		return tokenWithIndex.keySet();
	}

	private static Set<String> getRelevantBigrams(BufferedReader br) throws IOException {
		String line = null;
		Map<String, Integer> tokenWithIndex = new HashMap<String, Integer>();
		List<String> tokensList = new ArrayList<String>();

		int[] tokenFreq = new int[supportSize];
		Set<Integer> noiseIndices = new HashSet<Integer>();
		int availableIndex = 0;

		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			String text = /*row[4]+"."+*/row[5];
			String[] sentences = text.split("\\.");
			for(String sentence : sentences){
				NGram bigram = new NGram(sentence, 2);
				for(String token : bigram.list()){
					if(tokenWithIndex.keySet().contains(token)){
						tokenFreq[tokenWithIndex.get(token)] = tokenFreq[tokenWithIndex.get(token)] + 1;
					}
					else{
						int denoiseFreq = 1;
						while(availableIndex >= supportSize && noiseIndices.size() == 0){
							denoise(denoiseFreq, noiseIndices, tokenFreq, tokenWithIndex, tokensList);
							denoiseFreq = denoiseFreq + 1;
						}
						if(availableIndex < supportSize){
							tokenFreq[availableIndex] = 1;
							tokenWithIndex.put(token, availableIndex);
							availableIndex++;
							tokensList.add(token);
						}
						else{
							Integer newIndex = noiseIndices.iterator().next();
							tokenFreq[newIndex] = 1;
							noiseIndices.remove(newIndex);
							tokensList.set(newIndex, token);
							tokenWithIndex.put(token, newIndex);
						}
					}
				}
			}
			
		}
		/*denoise(1, noiseIndices, tokenFreq, tokenWithIndex, tokensList);*/
		br.close();
		return tokenWithIndex.keySet();
	}

	/**
	 * @param br
	 * @param lines
	 * @return
	 * @throws IOException
	 */
	public static int getRows(BufferedReader br) throws IOException {
		int lines = 0;
		String line;
		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			lines++;
		}
		br.close();
		return lines;
	}

	private static Set<String> loadFeatures(String featureFilePath) {
		Set<String> tokenSet = new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(featureFilePath));
			String line = null;
			while((line = br.readLine())!=null){
				tokenSet.add(line.trim());
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return tokenSet;
	}

	private static void saveFeatures(Set<String> tokenSet,
			String featureFilePath) {
		try{
			FileWriter fw = new FileWriter(featureFilePath);
			Iterator<String> iterator = tokenSet.iterator();
			while(iterator.hasNext()){
				fw.write(iterator.next() + "\n");
			}
			fw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * @param br
	 * @return 
	 * @throws IOException
	 */
	public static Set<String> getRelevantTokens(BufferedReader br) throws IOException {
		String line = null;
		Map<String, Integer> tokenWithIndex = new HashMap<String, Integer>();
		List<String> tokensList = new ArrayList<String>();

		int[] tokenFreq = new int[supportSize];
		Set<Integer> noiseIndices = new HashSet<Integer>();
		int availableIndex = 0;

		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			String title = row[4].replaceAll("\\ ", ",");
			String text = row[5].replaceAll("\\ |\\.", ",");
			text = text.replaceAll(",,", ",");
			if(text.charAt(text.length()-1) == ','){
				text = text.substring(0, text.length() - 1);
			}
			/*text = title + "," + text;*/
			String[] tokens = text.split(",");
			for(String token: tokens){
				if(tokenWithIndex.keySet().contains(token)){
					tokenFreq[tokenWithIndex.get(token)] = tokenFreq[tokenWithIndex.get(token)] + 1;
				}
				else{
					int denoiseFreq = 1;
					while(availableIndex >= supportSize && noiseIndices.size() == 0){
						denoise(denoiseFreq, noiseIndices, tokenFreq, tokenWithIndex, tokensList);
						denoiseFreq = denoiseFreq + 1;
					}
					if(availableIndex < supportSize){
						tokenFreq[availableIndex] = 1;
						tokenWithIndex.put(token, availableIndex);
						availableIndex++;
						tokensList.add(token);
					}
					else{
						Integer newIndex = noiseIndices.iterator().next();
						tokenFreq[newIndex] = 1;
						noiseIndices.remove(newIndex);
						tokensList.set(newIndex, token);
						tokenWithIndex.put(token, newIndex);
					}
				}
			}
		}
		/*denoise(1, noiseIndices, tokenFreq, tokenWithIndex, tokensList);*/
		br.close();
		return tokenWithIndex.keySet();
	}

	private static void denoise(int denoiseFreq, Set<Integer> noiseIndices,
			int[] tokenFreq, Map<String, Integer> tokenWithIndex, List<String> tokensList) {
		System.out.println("Denoising with freq : "+ denoiseFreq);
		for(int index = 0; index < supportSize; index++){
			if(tokenFreq[index] <= denoiseFreq){
				noiseIndices.add(index);
				tokenWithIndex.remove(tokensList.get(index));
			}
		}
	}
}
