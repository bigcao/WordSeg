package WordExtractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Word {
	
	private String word = "";
	private int frequency = 0;
	private int rightNeighbor_Sum = 0;
	private int leftNeighbor_Sum = 0;
	private double cohesiveDegree = Double.MAX_VALUE;
	private double entropy = -1l;
	private Map<String,Integer> rightNeighbors = new HashMap<String, Integer>();//<String, Integer>:<右邻字，频率>
	private Map<String,Integer> leftNeighbors = new HashMap<String,Integer>();//<String, Integer>:<左邻字，频率>
	
	
	public void addLeftNeighbor(String neighbor){
		int freq=1;
		if ((neighbor == "") || (neighbor == null)) {
			return;
		}
		if(leftNeighbors.containsKey(neighbor)){
			freq = leftNeighbors.get(neighbor);
			freq++;
		}
		leftNeighbors.put(neighbor, freq);
		leftNeighbor_Sum++;
	}
	
	public void addRightNeighbor(String neighbor){
		int freq=1;
		if ((neighbor == "") || (neighbor == null)) {
			return;
		}
		if(rightNeighbors.containsKey(neighbor)){
			freq = rightNeighbors.get(neighbor);
			freq++;
		}
		rightNeighbors.put(neighbor, freq);
		rightNeighbor_Sum++;
	}
	
//	public void addNeighbor(String neighbor, int side){
//		int freq=1;
//		if((neighbor == "")||(neighbor == null)){
//			return;
//		}
//		if(side == WordExtractor.RIGHT){
//			if(rightNeighbors.containsKey(neighbor)){
//				freq = rightNeighbors.get(neighbor);
//				freq++;
//			}
//			rightNeighbors.put(neighbor, freq);
//			rightNeighbor_Sum++;
//		}else if(side == WordExtractor.LEFT){
//			if(leftNeighbors.containsKey(neighbor)){
//				freq = leftNeighbors.get(neighbor);
//				freq++;
//			}
//			leftNeighbors.put(neighbor, freq);
//			leftNeighbor_Sum++;
//		}
//	}
	public double getEntropy(){
		return this.entropy;
	}

	/**
	 * 该单词的自由运用程度定义为其左邻字信息熵和右邻字信息熵中的较小值
	 * @return
	 */
	public void computeEntropy() {
		Iterator<String> rightIter = rightNeighbors.keySet().iterator();
		Iterator<String> leftIter = leftNeighbors.keySet().iterator();
		double rightEntropy_Sum=0l, leftEntropy_Sum=0l;//该文本片断的右邻字信息熵和左邻字信息熵中 
		while(rightIter.hasNext()){
			String rightNeighbor = rightIter.next();
			int freq = rightNeighbors.get(rightNeighbor);
			rightEntropy_Sum = rightEntropy_Sum-((double)freq/rightNeighbor_Sum)*Math.log10((double)freq/rightNeighbor_Sum);
		}
		while(leftIter.hasNext()){
			String leftNeighbor = leftIter.next();
			int freq = leftNeighbors.get(leftNeighbor);
			leftEntropy_Sum = leftEntropy_Sum-((double)freq/leftNeighbor_Sum)*Math.log10((double)freq/leftNeighbor_Sum);
		}
//		System.out.println(this.word+", right entropy: "+rightEntropy_Sum);
//		System.out.println(this.word+", left entropy: "+leftEntropy_Sum);
		this.entropy = (rightEntropy_Sum>leftEntropy_Sum)?leftEntropy_Sum:rightEntropy_Sum;
	}
	
	public double getCohesiveDegree() {
		return cohesiveDegree;
	}


	public void setCohesiveDegree(double cohesiveDegree) {
		this.cohesiveDegree = cohesiveDegree;
	}


	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getFrequency() {
		return frequency;
	}
	
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}



	public Word(String word, int freq) {
		super();
		this.word = word;
		this.frequency = freq;
	}

	

	@Override
	public String toString() {
		return  word;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
