package ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import matheMatical.Place;
import matheMatical.buildMap;

/**
 * 
 * @author Adam Wang
 * 
 *
 */
public class ACO {
    private static final int XI_AN_NUM = 0;

    private Ant[] ants; // 蚂蚁
    private int antNum; // 蚂蚁数量
    private int cityNum; // 城市数量
    private int MAX_GEN; // 运行代数
    private double[][] pheromone; // 信息素矩阵
    private int[][] distance; // 距离矩阵
    private int bestLength; // 最佳长度
    private int bestHour; // 最短用时
    private int[] bestTour; // 最佳路径
    private int[] bestHours; // 时间最短路径
    private Map<Integer, Place> placeMap; // 城市节点信息

    // 三个参数
    private double alpha;
    private double beta;
    private double rho;
    
    private String bestTourFileName;

    public ACO() {

    }

    /**
     * constructor of ACO
     * 
     * @param n
     *            城市数量
     * @param m
     *            蚂蚁数量
     * @param g
     *            运行代数
     * @param a
     *            alpha
     * @param b
     *            beta
     * @param r
     *            rho
     * 
     **/
    public ACO(int n, int m, int g, double a, double b, double r) {
        cityNum = n;
        antNum = m;
        ants = new Ant[antNum];
        MAX_GEN = g;
        alpha = a;
        beta = b;
        rho = r;

    }

    /**
     * 初始化ACO算法类
     * 
     * @param filename
     *            数据文件名，该文件存储所有城市节点坐标数据
     * @throws IOException
     */
    @SuppressWarnings("resource")
    private void init(String filename) throws IOException {
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        distance = new int[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            strbuff = data.readLine();
            String[] eachNum = strbuff.split(",");
            // System.out.println(cityNum + " " + eachNum.length);
            for (int j = 0; j < cityNum; j++) {
                int thisNum = Integer.valueOf(eachNum[j]);
                if (thisNum <= 0) {
                    distance[i][j] = Integer.MAX_VALUE;
                }
                distance[i][j] = thisNum;
            }
        }

        // int[] x;
        // int[] y;
        // x = new int[cityNum];
        // y = new int[cityNum];
        // for (int i = 0; i < cityNum; i++) {
        // strbuff = data.readLine();
        // String[] strcol = strbuff.split(" ");
        // x[i] = Integer.valueOf(strcol[1]);
        // y[i] = Integer.valueOf(strcol[2]);
        // }
        // // 计算距离矩阵
        // // ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
        // for (int i = 0; i < cityNum - 1; i++) {
        // distance[i][i] = 0; // 对角线为0
        // for (int j = i + 1; j < cityNum; j++) {
        // double rij = Math.sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
        // * (y[i] - y[j])) / 10.0);
        // int tij = (int) Math.round(rij);
        // if (tij < rij) {
        // distance[i][j] = tij + 1;
        // distance[j][i] = distance[i][j];
        // } else {
        // distance[i][j] = tij;
        // distance[j][i] = distance[i][j];
        // }
        // }
        // }
        // distance[cityNum - 1][cityNum - 1] = 0;

        // 初始化信息素矩阵
        pheromone = new double[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = 0.1f; // 初始化为0.1
            }
        }
        bestLength = Integer.MAX_VALUE;
        bestHour = Integer.MAX_VALUE;
        bestTour = new int[300 + 1];
        bestHours = new int[300 + 1];


        String fileName = "D://数学建模//ant//Cities_info2.csv";
        placeMap = buildMap.insertNodeFromFile(fileName);

        // 随机放置蚂蚁(西安)
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(distance, alpha, beta, placeMap);
        }
    }

    public void solve() throws IOException {
        int deadedAnts = 0;
        int servivedAnts = 0;

        for (int g = 0; g < MAX_GEN; g++) {
            for (int i = 0; i < antNum; i++) {
                ants[i].setAntId(g*MAX_GEN + i);
                String fileName = "ant_" + g + "_" + i + ".txt";
                ants[i].setFileName(fileName);
                File file = new File(fileName);
                file.delete();
                File fileTable = new File(fileName + "_table.txt");
                fileTable.delete();
                // FileWriter fileWriter = new FileWriter(fileName, true);
                // ants[i].setFileWriter(fileWriter);
                while (ants[i].getAllowedCities().size() > 0) {
                    if (ants[i].isDead()) {
                        FileWriter fileWriter = new FileWriter(ants[i].getFileName(), true);
                        fileWriter.append("蚂蚁死亡！\r\n");
                        fileWriter.close();
                        // System.out.println("ants " + i + "dead");
                        break;
                    }
                    ants[i].selectNextCity(pheromone);
                }
                if (ants[i].isDead()) {
                    deadedAnts++;
                    System.out.println("ants " + i + "dead");
                    continue;
                } 
                
            
                servivedAnts++;

                // 最后在路径上加上初始出发的城市
                ants[i].getTabu().add(ants[i].getFirstCity());
                FileWriter fileWriter = new FileWriter(ants[i].getFileName(),true);
                
                System.out.println(distance.length + " " + distance[0].length);
                fileWriter.append("这是最后一个景区，花了" + (distance[ants[i].getCurrentCity()][XI_AN_NUM] / 8) + "天回到西安！\r\n");
                fileWriter.append("本次出行概括:\r\n");
                fileWriter.append(ants[i].toResultString());
                fileWriter.close();

                if (ants[i].getTourLength() < bestLength) {
                    bestLength = ants[i].getTourLength();
                    for (int k = 0; k < ants[i].getTabu().size(); k++) {
                        bestTour[k] = ants[i].getTabu().get(k).intValue();
                    }
                }
                System.out.println(ants[i].calculateTourTime());
                if (ants[i].calculateTourTime() < bestHour) {
                    bestHour = ants[i].calculateTourTime();
                    bestTourFileName = ants[i].getFileName();
                    
                    for (int k = 0; k < ants[i].getTabu().size(); k++) {
                        bestHours[k] = ants[i].getTabu().get(k).intValue();
                    }
                }
                for (int j = 0; j < ants[i].getTabu().size() - 1; j++) {
                    ants[i].getDelta()[ants[i].getTabu().get(j).intValue()][ants[i].getTabu().get(j + 1).intValue()] = (double) (1. / ants[i]
                            .getTourLength());
                    ants[i].getDelta()[ants[i].getTabu().get(j + 1).intValue()][ants[i].getTabu().get(j).intValue()] = (double) (1. / ants[i]
                            .getTourLength());
                }
            }

            // 更新信息素
            updatePheromone();

            // 重新初始化蚂蚁
            for (int i = 0; i < antNum; i++) {
                ants[i].init(distance, alpha, beta, placeMap);
            }
        }

        // 打印最佳结果
        printOptimal();
        System.out.println(deadedAnts + " / " + servivedAnts);
    }

    // 更新信息素
    private void updatePheromone() {
        // 信息素挥发
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = pheromone[i][j] * (1 - rho);
            }
        }
        // 信息素更新
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                for (int k = 0; k < antNum; k++) {
                    pheromone[i][j] += ants[k].getDelta()[i][j];
                }
            }
        }
                
        // 西安的信息素全为1
        for (int i = 0; i < cityNum; i++) {
            pheromone[i][XI_AN_NUM] = 0.1;
            pheromone[XI_AN_NUM][i] = 0.1;
        }
    }

    private void printOptimal() throws IOException {
        FileWriter fileWriter = new FileWriter("result.txt", true);
        
        System.out.println("路上花费最短的里程为：" + bestLength * 90 + "km");
        System.out.println("最短路程线路：");
        fileWriter.append("路上花费最短的里程为：" + bestLength * 90 + "km" + "\r\n");
        fileWriter.append("最短路程线路：" + "\r\n");
        int length = bestTour.length;
        for (int i = 0; i < length; i++) {
            System.out.print(placeMap.get(bestTour[i]).getPlaceName() + " --> ");
            fileWriter.append(placeMap.get(bestTour[i]).getPlaceName() + " --> ");
        }
        
        System.out.println("\n路上花费的最短时间为： " + (bestHour/1000) + "年零" + (bestHour % 1000)/10 + "天");
        System.out.println("最短路径城市列表为：");
        fileWriter.append("\r\n路上花费的最短时间为： " + (bestHour/1000) + "年零" + (bestHour % 1000)/10 + "天" + "\r\n");
        fileWriter.append("最短路径城市列表为：" + "\r\n");
        for (int i = 0; i < bestHours.length; i++) {
            System.out.print(placeMap.get(bestHours[i]).getPlaceName() + " --> ");
            fileWriter.append(placeMap.get(bestHours[i]).getPlaceName() + " --> ");
        }
        fileWriter.append("\r\n");
        
        // 输出城市编号
        for (int i = 0; i < bestHours.length; i++) {
            System.out.print(placeMap.get(bestHours[i]).getPlaceName() + " --> ");
            fileWriter.append(bestHours[i] + " ");
        }
        
        System.out.println("\n上述行程详细日志文件存储在:" + bestTourFileName);
        fileWriter.append("\r\n上述行程详细日志文件存储在:" + bestTourFileName + "\r\n");
        fileWriter.close();
    }

    public Ant[] getAnts() {
        return ants;
    }

    public void setAnts(Ant[] ants) {
        this.ants = ants;
    }

    public int getAntNum() {
        return antNum;
    }

    public void setAntNum(int m) {
        this.antNum = m;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public int getMAX_GEN() {
        return MAX_GEN;
    }

    public void setMAX_GEN(int mAX_GEN) {
        MAX_GEN = mAX_GEN;
    }

    public double[][] getPheromone() {
        return pheromone;
    }

    public void setPheromone(double[][] pheromone) {
        this.pheromone = pheromone;
    }

    public int[][] getDistance() {
        return distance;
    }

    public void setDistance(int[][] distance) {
        this.distance = distance;
    }

    public int getBestLength() {
        return bestLength;
    }

    public void setBestLength(int bestLength) {
        this.bestLength = bestLength;
    }

    public int[] getBestTour() {
        return bestTour;
    }

    public void setBestTour(int[] bestTour) {
        this.bestTour = bestTour;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ACO aco = new ACO(198, 50, 10, 1, 5, 0.5);
        // aco.init("D://data.txt");
        // 初始化 距离矩阵，信息素矩阵，第一个蚂蚁的位置
        aco.init("D://数学建模//ant//timeArrayAllInt2.csv");
        aco.solve();
    }

}
