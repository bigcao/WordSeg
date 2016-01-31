import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import suffixarray.sais;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		byte[] bytes = null;
//		try {
//			bytes = Files.readAllBytes(Paths.get("resource/test2.txt"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String txtStr = Charset.forName("utf-8").decode(ByteBuffer.wrap(bytes))
//				.toString()
//				.replaceAll("[\\s，\"!！、:：。,.{}《》<>（）()?？‘’“”；'`【】;]", " ");
//		String[] txtArray = txtStr.split(" {1,}");
//		System.out.println("the length of the text array: "+txtArray.length);
//		for(String s:txtArray){
//			System.out.println(s);
//		}
		String txtStr = "十j";//测试用
		System.out.println(txtStr.substring(0,2));
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(null, 1);
		System.out.println(map.get(null));
//		Map<String, Integer> map1 = new HashMap<String, Integer>();
//		map1.put("b", 2);
//		Map<String, Integer> mapAll = new HashMap<String, Integer>();
//		mapAll.putAll(map);
//		mapAll.putAll(map1);
//		
//		
//		System.out.println(mapAll.get("b"));

	}

}
