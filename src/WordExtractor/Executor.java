package WordExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import suffixarray.sais;

public class Executor {
	
	/**
	 * 结果集写到指定文件
	 * @param results
	 * @param outputFile
	 */
	public static void write2File(Map<String, Word> results, String outputFile){
		File output = new File(outputFile);
		if(output.exists()){
			output.delete();
			try {
				output.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(output));
			Iterator<String> iter = results.keySet().iterator();
			bw.write("Word_Fragment, Frequency, Cohesion, Entropy"+"\n");
			while (iter.hasNext()) {
				String cword = iter.next();
				Word word = results.get(cword);
				String result = cword + ", " + word.getFrequency() + ", "
						+ word.getCohesiveDegree() + ", " + word.getEntropy();
					bw.write(result + "\n");
			}
				bw.flush();
				bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 结果打印到控制台
	 * @param results
	 */
	public static void printOut(Map<String, Word> results){
		Iterator<String> iter = results.keySet().iterator();
		while (iter.hasNext()) {
			String cword = iter.next();
			Word word = results.get(cword);
			String result = cword + ", " + word.getFrequency() + ", "
					+ word.getCohesiveDegree() + ", " + word.getEntropy();
			System.out.println(result);
		}
	}
	
	/**
	 * 主要处理逻辑
	 * @param txtStr
	 * @param max_word_length
	 */
	public static Map<String, Word> processCore(String txtStr, int max_word_length){
		int[] SA, PA;
		int n;
		long start, finish;
		n = txtStr.length();
		System.out.println(n + " characters ... ");
		SA = new int[n];
		PA = new int[n];
		String txtStrReverse = new StringBuffer(txtStr).reverse().toString();// 数组反转
		start = new Date().getTime();
		new sais().suffixsort(txtStr, SA, n);// 右邻
		new sais().suffixsort(txtStrReverse, PA, n);// 左邻
		finish = new Date().getTime();
		System.out.println("suffix array algorithm: " + ((finish - start) / 1000.0) + " sec");
		System.out.println("Start to extract words....");
		// step 1
		start = new Date().getTime();
		WordExtractor.computeWordFrequency(txtStr, SA, max_word_length, WordExtractor.RIGHT);
		finish = new Date().getTime();
		System.out.println("Compute frequency and find right neighbors: " + ((finish - start) / 1000.0) + " sec");
		// step 2
		start = new Date().getTime();
		WordExtractor.computeWordFrequency(txtStrReverse, PA, max_word_length, WordExtractor.LEFT);
		finish = new Date().getTime();
		System.out.println("Compute frequency and find left neighbors: " + ((finish - start) / 1000.0) + " sec");
		// step 3
		start = new Date().getTime();
		WordExtractor.computeWordsEntropy();
		finish = new Date().getTime();
		System.out.println("Compute entropy for each candidate words : " + ((finish - start) / 1000.0) + " sec");
		// step 4
		start = new Date().getTime();
		WordExtractor.computeCohesiveDegree(txtStr);
		finish = new Date().getTime();
		System.out.println("Compute cohesion degree : " + ((finish - start) / 1000.0) + " sec");
		
		return WordExtractor.getCandidate_Word_Set();
	}
	
	/**
	 * 获取候选词结果集，候选词的凝固度、自由度已都被计算
	 * @param corpus_file
	 * @param max_word_length
	 * @return
	 */
	public static Map<String, Word> getCandidateWordsSet(String corpus_file, int max_word_length){
		Map<String, Word> map = new HashMap<String,Word>();
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(Paths.get(corpus_file));
			String txtStr = Charset.forName("utf-8").decode(ByteBuffer.wrap(bytes)).toString()
					.replaceAll("[\\s，\"!！、:：。,.{}《》<>（）()?？‘’“”；'`【】;]", "");
			map = processCore(txtStr, max_word_length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 *  从抽好的词库中按照给定条件，即词的最小长度、最小频率、最小凝固度、最小自由度，进行过滤
	 * @param words_repo
	 * @param filterd_repo
	 * @param min_length
	 * @param min_frequency
	 * @param min_cohesionDegree
	 * @param min_entropy
	 * @return
	 */
	public static int getResultsFromWordsRepo(String words_repo, String filterd_repo, int min_length, int min_frequency,
			double min_cohesionDegree, double min_entropy) {
		int length = 0;
		File outfile = new File(filterd_repo);
		if (outfile.exists()) {
			outfile.delete();
			try {
				outfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(words_repo));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
			bw.write(br.readLine() + "\n");// 第一行是column name
			String line = br.readLine();
			while (line != null) {
				String[] records = line.split(",");
				String word = records[0].trim();
				int freq = Integer.parseInt(records[1].trim());
				double cohesion = Double.parseDouble(records[2].trim());
				double entropy = Double.parseDouble(records[3].trim());
				if (word.length() >= min_length && freq >= min_frequency && cohesion >= min_cohesionDegree
						&& entropy >= min_entropy) {
					bw.write(line + "\n");
					length++;
				}
				line = br.readLine();
			}
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return length;
	}
	
	public static void main(String[] args) {

		int max_wordFragment_length = 3;//抽词时，将文本片断当做词的最大长度
		int min_word_length = 2;//最后结果中出现的词的最小长度
		int min_frequency = 2;//某文本片断在整个文本中出现的频数
		double min_cohesionDegree = 0.0; //最小凝固度
		double min_entropy = 0.001;//自由度，即左、右邻信息熵的最小值
		String corpus = "test2";// 资本论
		String corpus_file = "data/" + corpus + ".txt";
		String wordsRepo_file = "./result/wordsRepo_" + max_wordFragment_length + "_" + corpus + ".txt";
		if(min_word_length>max_wordFragment_length){
			System.err.println("The minimum word length can not be greater than the maximum length of the word fragment.");
			System.exit(-1);
		}
		String filteredWords_Repo_file = "./result/filteredWords_L" + max_wordFragment_length + "_L" + min_word_length
				+ "_F" + min_frequency + "_C" + min_cohesionDegree + "_E" + min_entropy + "_" + corpus + ".txt";
		
//		String  txtStr = "四是四十是十十四是十四四十是四十";//测试用
		// 四是四十是十十四是十四四十是四十，皮萄葡吐倒萄葡吃不皮萄葡吐不萄葡吃, 吃葡萄不吐葡萄皮不吃葡萄倒吐葡萄皮
		
		//先初步把满足一定长度的候选词进行计算，并输出到词库文件中
		Map<String, Word> candidateWordsSet = getCandidateWordsSet(corpus_file,max_wordFragment_length);
		if(candidateWordsSet.isEmpty()){
			System.out.println("No results will be written to "+ wordsRepo_file);
			return;
		}
		write2File(candidateWordsSet, wordsRepo_file);
		System.out.println("Number of words in word repo: " + candidateWordsSet.size());
		
		//从词库文件中获取,若后续需要尝试多组不同的过滤条件，可以将之前生成原始库的方法注释掉，以免多次执行
		int size = getResultsFromWordsRepo(wordsRepo_file, filteredWords_Repo_file, min_word_length, min_frequency,
				min_cohesionDegree, min_entropy);
		System.out.println("Number of words in filtered repo: " + size);
			
	}

}
