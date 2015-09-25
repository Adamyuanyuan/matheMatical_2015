package ant2;

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
    private static final int BIG_CITY_LENGTH = 31;

    private Ant[] ants; // 蚂蚁
    private int antNum; // 蚂蚁数量
    private int cityNum; // 城市数量
    private int MAX_GEN; // 运行代数
    private double[][] pheromone; // 信息素矩阵
    private int[][] distance; // 距离矩阵
    private int bestLength; // 最佳长度
    private int bestHour; // 最短用时
    private int bestCost;
    private int[] bestTour; // 最佳路径
    private int[] bestHours; // 时间最短路径
    private int[] bestCosts;
    private Map<Integer, Place> placeMap; // 城市节点信息
    private int cost;
    
    // 飞机花费矩阵，飞机只有省会
    private double[][] planeCost;
    
    // 飞机用时矩阵，飞机只有省会
    private double[][] planeTime;
    
    // 动车花费矩阵，只有省会之间才有
    private double[][] trainCost;
    
    private double[][] trainTime;

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
        initDistanceArray(filename);
        // initTrainTimeArray();

        // 初始化信息素矩阵
        pheromone = new double[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = 0.1f; // 初始化为0.1
            }
        }
        bestLength = Integer.MAX_VALUE;
        bestHour = Integer.MAX_VALUE;
        bestCost = Integer.MAX_VALUE;
        bestTour = new int[300];
        bestHours = new int[300];
        bestCosts = new int[300];


        String fileName = "D://数学建模//ant2//Cities_info2.csv";
        placeMap = buildMap.insertNodeFromFile(fileName);

        // 随机放置蚂蚁(西安)
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(distance, alpha, beta, placeMap, planeCost, planeTime, trainCost, trainTime);
        }
    }
    
    private void initDistanceArray(String filename) throws IOException {
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
        data.close();
    }
    
    private void initPlaneCostArray(String filename) throws IOException {
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        planeCost = new double[BIG_CITY_LENGTH][BIG_CITY_LENGTH];
        for (int i = 0; i < BIG_CITY_LENGTH; i++) {
            strbuff = data.readLine();
            String[] eachNum = strbuff.split(",");
            // System.out.println(BIG_CITY_LENGTH + " " + eachNum.length);
            for (int j = 0; j < BIG_CITY_LENGTH; j++) {
                int thisNum = Integer.valueOf(eachNum[j]);
                if (thisNum <= 0) {
                    planeCost[i][j] = Integer.MAX_VALUE;
                }
                planeCost[i][j] = thisNum;
            }
        }
        data.close();
    }
    
    private void initPlaneTimeArray(String filename) throws IOException {
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        planeTime = new double[BIG_CITY_LENGTH][BIG_CITY_LENGTH];
        for (int i = 0; i < BIG_CITY_LENGTH; i++) {
            strbuff = data.readLine();
            String[] eachNum = strbuff.split(",");
            // System.out.println(BIG_CITY_LENGTH + " " + eachNum.length);
            for (int j = 0; j < BIG_CITY_LENGTH; j++) {
                double thisNum = Double.valueOf(eachNum[j]);
                if (thisNum <= 0) {
                    planeTime[i][j] = Integer.MAX_VALUE;
                }
                planeTime[i][j] = thisNum;
            }
        }
        data.close();
    }
    
    private void initTrainTimeArray(String filename) throws IOException {
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        trainTime = new double[BIG_CITY_LENGTH][BIG_CITY_LENGTH];
        for (int i = 0; i < BIG_CITY_LENGTH; i++) {
            strbuff = data.readLine();
            String[] eachNum = strbuff.split(",", -1);
            // System.out.println(BIG_CITY_LENGTH + " " + eachNum.length);
            for (int j = 0; j < BIG_CITY_LENGTH; j++) {
                double thisNum = Double.valueOf(eachNum[j]);
                trainTime[i][j] = thisNum;
            }
        }
        data.close();
    }
    
    private void initTrainCostArray(String filename) throws IOException {
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        trainCost = new double[BIG_CITY_LENGTH][BIG_CITY_LENGTH];
        for (int i = 0; i < BIG_CITY_LENGTH; i++) {
            strbuff = data.readLine();
            String[] eachNum = strbuff.split(",");
            // System.out.println(BIG_CITY_LENGTH + " " + eachNum.length);
            for (int j = 0; j < BIG_CITY_LENGTH; j++) {
                double thisNum = Double.valueOf(eachNum[j]);
                if (thisNum <= 0) {
                    trainCost[i][j] = Integer.MAX_VALUE;
                }
                trainCost[i][j] = thisNum;
            }
        }
        data.close();
    }

    public void solve() throws IOException {
        int deadedAnts = 0;
        int servivedAnts = 0;

        for (int g = 0; g < MAX_GEN; g++) {
            for (int i = 0; i < antNum; i++) {
                System.out.println("ant + " + i);
                ants[i].setAntId(g * MAX_GEN + i);
                String fileName = "ant2_" + g + "_" + i + ".txt";
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
                
                fileWriter.append("这是最后一个景区，花了" + (distance[ants[i].getCurrentCity()][XI_AN_NUM] / 8) + "天回到西安！\r\n");
                fileWriter.append("本次出行概括:\r\n");
                fileWriter.append(ants[i].toResultString());
                fileWriter.close();
                
                if (ants[i].getTourLength() < bestLength) {
                    bestLength = ants[i].getTourLength();
                    for (int k = 0; k < cityNum + 1; k++) {
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
                
                System.out.println(ants[i].getCost());
                if (ants[i].getCost() < bestCost) {
                    bestCost = ants[i].getCost();
                    bestTourFileName = ants[i].getFileName();
                    for (int k = 0; k < ants[i].getTabu().size(); k++) {
                        bestCosts[k] = ants[i].getTabu().get(k).intValue();
                    }
                }
                
                for (int j = 0; j < cityNum; j++) {
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
                ants[i].init(distance, alpha, beta, placeMap, pheromone, pheromone, pheromone, pheromone);
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
        
        System.out.println("路上花费最短的花费为：" + bestCost + "元");
        System.out.println("最短路程线路：");
        fileWriter.append("路上花费最短的花费为：" + bestCost +"元" + "\r\n");
        fileWriter.append("最少开销路程线路：" + "\r\n");
        for (int i = 0; i < bestCosts.length; i++) {
            System.out.print(placeMap.get(bestCosts[i]).getPlaceName() + " --> ");
            fileWriter.append(placeMap.get(bestCosts[i]).getPlaceName() + " --> ");
        }
        
        for (int i = 0; i < bestCosts.length; i++) {
            System.out.print(bestCosts[i] + " ");
            fileWriter.append(bestCosts[i] + " ");
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
        ACO aco = new ACO(198, 50, 5, 1, 5, 0.5);
        // aco.init("D://data.txt");
        // 初始化 距离矩阵，信息素矩阵，第一个蚂蚁的位置
        aco.initPlaneCostArray("D://数学建模//ant2//planeCostTable.csv");
        aco.initPlaneTimeArray("D://数学建模//ant2//planeTimeTable.csv");
        aco.initTrainCostArray("D://数学建模//ant2//trainCostTable.csv");
        aco.initTrainTimeArray("D://数学建模//ant2//trainTimeTable.csv");
        aco.init("D://数学建模//ant2//timeArrayAllInt2.csv");
        
        
        aco.solve();
    }
}
