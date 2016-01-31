package WordExtractor;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import suffixarray.sais;

public class WordExtractor {
	
//	private static int MAX_WORD_LENGTH = 5;
	public static int RIGHT = 1;
	public static int LEFT = 2;
	public static Map<String,Word> candidate_Word_Set = new HashMap<String, Word>(); //<String,String>=<word, "frequency+entropy">
//	private static Map<String,Word> candidate_Word_Set = new HashMap<String, Word>();

	/**
	 * 根据用户给定的最小凝固度和最小自由度对候选词列表进行过滤处理
	 * @param min_cohesionDegree，最小凝固度
	 * @param min_entropy，最小自由度（信息熵）
	 * @return
	 */
	public static Map<String,Word> getResult_Word_Set(double min_cohesionDegree, double min_entropy){
		Map<String,Word> result_Word_Set = new HashMap<String, Word>();
		Iterator<String> iter = candidate_Word_Set.keySet().iterator();
		while(iter.hasNext()){
			String cword = iter.next();
			if(cword.length()>1){
				Word word = candidate_Word_Set.get(cword);
				if(word.getCohesiveDegree()>=min_cohesionDegree && word.getEntropy()>=min_entropy){
					result_Word_Set.put(cword, word);
				}
			}
		}
		return result_Word_Set;
	}
	
	/**
	 * 对每个候选集中的文本片断进行自由度的计算，该方法要在将右邻和左邻找出后调用
	 * 即先调用方法computeWordFrequency，把词频和左右邻都找出后再调用
	 */
	public static void computeWordsEntropy(){
		Iterator<String> iter = candidate_Word_Set.keySet().iterator();
		while(iter.hasNext()){
			String cword = iter.next();
			Word word = candidate_Word_Set.get(cword);
			word.computeEntropy();
		}
	}

	/**
	 * 输出给定子串在原字符串中的个数
	 * @param orig
	 * @param sub
	 * @return
	 */
	public static int subStringCounter(String orig, String sub){
		int counter = 0;
		Pattern p = Pattern.compile(sub, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(orig);
		while(m.find()){
			counter++;
		}
		return counter;
	}
	
	public static Map<String, Word> getCandidate_Word_Set() {
		return candidate_Word_Set;
	}

	/**
	 * 获取每个文本片断的词频和该片断某一侧的信息熵（即自由度），结果存储在candidate_Word_Set中
	 * @param txtStr
	 * @param suffixArray
	 * @param max_word_length
	 * @param side
	 */
	public static void computeWordFrequency(String txtStr, int[] suffixArray, int max_word_length, int side){
		String str = "";
		for(int i=0; i<suffixArray.length;i++){
			str = txtStr.substring(suffixArray[i]);
			segmentWords(str,max_word_length, side);
		}
	}
	
	/**
	 * 计算candidate_Word_Set中每个文本片断的凝固度
	 * @param txtStr
	 */
	public static void computeCohesiveDegree(String txtStr){
		int size = txtStr.length();
		Iterator<String> iter = candidate_Word_Set.keySet().iterator();
		while(iter.hasNext()){
			String fragment = iter.next();
			Word w = candidate_Word_Set.get(fragment);
			String word = w.getWord();
			double p0 = (double)w.getFrequency()/size;
			if(word.length()>1){
				double min=Double.POSITIVE_INFINITY;
				for(int i=0; i<word.length();i++){
					String s1 = word.substring(0,i+1);
					String s2 = word.substring(i+1, word.length());
					if(s2.isEmpty()){
						break;
					}
					double p1 = (double)candidate_Word_Set.get(s1).getFrequency()/size;
					double p2 = (double)candidate_Word_Set.get(s2).getFrequency()/size;
					double degree = p0/(p1*p2);
					if(degree<min){
						min = degree;
					}
				}
				w.setCohesiveDegree(min);
			}
		}
	}
	
	/**
	 * 对每个后缀字符串进行子串提取，把每个子串看作一个词，并计算词的词频
	 * @param str
	 * @param max_word_length
	 * @param side
	 */
	private static void segmentWords(String str, int max_word_length, int side){
		if(str.length()>max_word_length){
			str = str.substring(0, max_word_length);
		}
		String word="";
		for(int j=0;j<str.length();j++){
			word = str.substring(0, j+1);
			String rightNeighbor = "";
			if(j+2<=str.length()){
				rightNeighbor = str.substring(j+1,j+2);
			}
			computeWordFrequency(word,rightNeighbor, side);
		}
	}

	/**
	 * 计算候选词的词频
	 * @param word：候选词
	 * @param side：右邻or左邻
	 */
	private static void computeWordFrequency(String word, String neighbor, int side) {
		int freq=1;
		Word w = null;
		if(side == WordExtractor.RIGHT){
			if(candidate_Word_Set.containsKey(word)){
				w = candidate_Word_Set.get(word);
				freq = w.getFrequency()+1;
				w.setFrequency(freq);
				w.addRightNeighbor(neighbor);
			}else{
				w = new Word(word,freq);
				w.addRightNeighbor(neighbor);
				candidate_Word_Set.put(word, w);
			}
		}else if(side == WordExtractor.LEFT){
			//word=皮萄，neighbor=葡,把word反转，变成“萄皮”，那么“萄”便是“萄皮”的左邻字了
			word = new StringBuffer(word).reverse().toString();
			if(candidate_Word_Set.containsKey(word)){
				w = candidate_Word_Set.get(word);
				w.addLeftNeighbor(neighbor);
			}else{
				try {
					throw new Exception("The word "+ word+" does not exist in candidate word set.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	public static void main(String[] args) {

		int max_word_length = 2;
//		double combineDegree_Threshold = 0.1;
//		double entropy_Threshold = ;
		int[] SA,PA;
		int n;
		long start, finish;

		try {
			byte[] bytes = Files.readAllBytes(Paths.get("data/资本论.txt"));
			String txtStr = Charset.forName("utf-8").decode(ByteBuffer.wrap(bytes))
					.toString()
					.replaceAll("[\\s，\"!！、:：。,.{}《》<>（）()?？‘’“”；'`【】;]", "");
			txtStr = "四是四十是十十四是十四四十是四十";//测试用
			n = txtStr.length();
			System.out.println(n + " characters ... ");

			/* Allocate 5n bytes of memory. */
			SA = new int[n];
			PA = new int[n];
			
			String txtStrReverse = new StringBuffer(txtStr).reverse().toString();//数组反转
//			System.out.println(txtStrReverse);
			/* Construct the suffix array. */
			start = new Date().getTime();
			new sais().suffixsort(txtStr, SA, n);//右邻
			new sais().suffixsort(txtStrReverse, PA, n);//左邻
			finish = new Date().getTime();
			System.out.println("suffix array algorithm: "+((finish - start) / 1000.0) + " sec");
			System.out.println("Start to extract words....");
//			for(int i=0;i<n;i++){
//				System.out.println(txtStr.substring(SA[i]));
//			}
			WordExtractor.computeWordFrequency(txtStr, SA, max_word_length, RIGHT);
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
	}

}
