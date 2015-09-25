package arrayDis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Dijkstra {

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
//        int[][] weight = { { 0, 3, 9999999, 7, 9999999 }, { 3, 0, 4, 2, 9999999 }, { 9999999, 4, 0, 5, 6 },
//                { 7, 2, 5, 0, 4 }, { 9999999, 9999999, 6, 4, 0 } };
//        String filename1 = "D://数学建模//ant//routeArray.csv";
        String filename2 = "D://数学建模//ant//result_route6.csv";
        int length = 288;
        int[][] routeArray = new int[length][length];
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename2)));

        for (int i = 0; i < length; i++) {
            strbuff = data.readLine();
            String[] eachNum = strbuff.split(",",-1);
            System.out.println("数组的长度" + eachNum.length);
            for (int j = 0; j < length; j++) {
                if (eachNum[j].equals("")) {
                    routeArray[i][j] = 0;
                } else {
                    int thisNum = Integer.valueOf(eachNum[j]);
                    routeArray[i][j] = thisNum;
                }
                
            }
        }

        // for (int i = 0; i < length; i++) {
        // strbuff = data.readLine();
        // String[] eachNum = strbuff.split(",");
        // for (int j = 0; j < length; j++) {
        // int thisNum = Integer.valueOf(eachNum[j]);
        // if (thisNum <= 0 && i != j) {
        // routeArray[i][j] = 9999999;
        // } else {
        // routeArray[i][j] = thisNum;
        // }
        // }
        // }
        //
        data.close();

        FileWriter fileWriter2 = new FileWriter("routeArrayDui6.csv", true);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (routeArray[i][j] > 0) {
                    routeArray[j][i] = routeArray[i][j];
                }
            }
        }
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                fileWriter2.append(routeArray[i][j] + ",");
            }
            fileWriter2.append("\n");
        }

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (routeArray[i][j] <= 0 && i != j) {
                    routeArray[i][j] = 9999999;
                }
            }
        }

        fileWriter2.close();

        FileWriter fileWriter1 = new FileWriter("routeArrayAll.csv", true);
        FileWriter fileWriter3 = new FileWriter("timeArrayAll.csv", true);

        for (int i = 0; i < routeArray.length; i++) {
            int[] path = Dijsktra(routeArray, i);
            for (int j = 0; j < path.length; j++) {
                fileWriter1.append(path[j] + ",");
                int result = path[j] / 90;
                if (result == 0) {
                    result = 1;
                }
                fileWriter3.append(result + ",");
            }
            fileWriter1.append("\r\n");
            fileWriter3.append("\r\n");
        }
        fileWriter1.close();
        fileWriter3.close();
        System.out.println("OK");

    }

    public static int[] Dijsktra(int[][] weight, int start) {
        // 接受一个有向图的权重矩阵，和一个起点编号start（从0编号，顶点存在数组中）
        // 返回一个int[] 数组，表示从start到它的最短路径长度
        int n = weight.length; // 顶点个数
        int[] shortPath = new int[n]; // 存放从start到其他各点的最短路径
        int[] visited = new int[n]; // 标记当前该顶点的最短路径是否已经求出,1表示已求出

        // 初始化，第一个顶点求出
        shortPath[start] = 0;
        visited[start] = 1;

        for (int count = 1; count <= n - 1; count++) // 要加入n-1个顶点
        {
            int k = -1; // 选出一个距离初始顶点start最近的未标记顶点
            int dmin = 10000000;
            for (int i = 0; i < n; i++) {
                if (visited[i] == 0 && weight[start][i] < dmin) {
                    dmin = weight[start][i];
                    k = i;
                }
            }

            // 将新选出的顶点标记为已求出最短路径，且到start的最短路径就是dmin
            shortPath[k] = dmin;
            visited[k] = 1;

            // 以k为中间点想，修正从start到未访问各点的距离
            for (int i = 0; i < n; i++) {
                if (visited[i] == 0 && weight[start][k] + weight[k][i] < weight[start][i])
                    weight[start][i] = weight[start][k] + weight[k][i];
            }

        }

        return shortPath;
    }
}