package matheMatical;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class buildMap {
    public static void main(String[] args) {
        Place xiAnPlace = build();
        System.out.println(xiAnPlace);
    }

    public static Place build() {
        Place startPlace = new Place(26, "西安", 2, 0);
        String fileName = "D://数学建模//place.csv";
        Map<Integer, Place> placeMap = insertNodeFromFile(fileName);
        for (Integer place : placeMap.keySet()) {
            System.out.println(placeMap.get(place));
        }
        return startPlace;
    }

    public static Map<Integer, Place> insertNodeFromFile(String fileName) {
        Map<Integer, Place> placeMap = new HashMap<Integer, Place>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null; // 用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        try {
            String str = "";
            String str1 = "";
            fis = new FileInputStream(fileName);// FileInputStream
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new
                                         // InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                String strs[] = str.split(",");
                if (strs[0].equals("p")) {
                    Place thisPlace = new Place(Integer.valueOf(strs[1]), strs[2], Double.valueOf(strs[3]),
                            Integer.valueOf(strs[4]) == 1, Integer.valueOf(strs[5]), Integer.valueOf(strs[6]));
                    System.out.println(thisPlace);
                    placeMap.put(Integer.valueOf(strs[1]), thisPlace);

                } else {
                    Route thisRoute = new Route(Integer.valueOf(strs[1]), Integer.valueOf(strs[2]),
                            Integer.valueOf(strs[3]), Integer.valueOf(strs[4]));
                }

            }
            // 当读取的一行不为空时,把读到的str的值赋给str1
            System.out.println(str1);// 打印出str1
        } catch (FileNotFoundException e) {
            System.out.println("找不到指定文件");
        } catch (IOException e) {
            System.out.println("读取文件失败");
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
                // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return placeMap;

    }

    // 通过给定的节点路径(place列表)，根据题目规则访问这个路径，得到最终需要的时间
    private int visitNode(int[] bestTour, Map<Integer, Place> placeMap){
        for (int i = 0; i < bestTour.length; i++) {
            Place place = placeMap.get(bestTour[i]);
            String placeName = place.getPlaceName();
            System.out.println();
        }
        return 0;
        
    }
    
    private int visitNodeFromNodeList(LinkedList<Integer> nodeList, int needDate, int currentTime, int driveTime) {
        return 0;
    }
}
