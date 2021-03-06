package ant2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import matheMatical.Place;

/**
 * 
 * @author Adam Wang
 *
 */
public class Ant implements Cloneable {
    private static final double P_STARTCITY = 0.2;

    private static final double COST_EACH_KM_OF_CAR = 0.96;
    
    private static final double COST_EACH_NIGHT_OF_CAR = 139;
    
    private static final int XI_AN_NUM = 0;

    private static final int WINDOW_SIZE = 5;

    private int antId;
    private Vector<Integer> tabu; // 禁忌表
    private Vector<Integer> allowedCities; // 允许搜索的城市
    private double[][] delta; // 信息数变化矩阵
    private int[][] distance; // 距离矩阵
    private Map<Integer, Place> placeMap; // 城市节点信息

    // 当前时间，0-24
    private int currentTime = 7;

    // 一次已经访问的天数
    private double visitDate = 1;

    // 一年已经访问的天数
    private double visitDateThisYear = 1;

    // 今年已经访问的次数
    private int times = 1;

    // 已经访问的年数
    private int years = 1;

    private volatile boolean isDead = false;

    public boolean isDead() {
        return isDead;
    }

    private double alpha;
    private double beta;

    private int tourLength; // 路径长度
    private int cityNum; // 城市数量

    private int firstCity; // 起始城市
    private int currentCity; // 当前城市
    
    /////////////////////////////////////下面是ant2新加的参数////////////////////////////////////////////
    // 判断出行方式[0|1|2] [汽车|火车|飞机]
    private int tripMode;
    
    private int cost;
    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    // 飞机花费矩阵，飞机只有省会
    private double[][] planeCost;
    
    // 飞机用时矩阵，飞机只有省会
    private double[][] planeTime;
    
    // 动车花费矩阵，只有省会之间才有
    private double[][] trainCost;
    
    private double[][] trainTime;
    
    private int[] avgDistance; // 出发地到其它任何地方的平均距离

    
    
    public int getCurrentCity() {
        return currentCity;
    }

    private FileWriter fileWriter; // 记录路程信息的文件名称

    public FileWriter getFileWriter() {
        return fileWriter;
    }

    public void setFileWriter(FileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName; // 记录路程信息的文件名称

    private int closestCapitalCity; // 离上一个当前点最近的省会

    public Ant() {
        cityNum = 288;
        tourLength = 0;
    }

    /**
     * Constructor of Ant
     * 
     * @param num
     *            蚂蚁数量
     */
    public Ant(int num) {
        cityNum = num;
        tourLength = 0;

    }

    public String toResultString() {
        return "蚂蚁概况： [ 当前时间=" + currentTime + ", 本次旅游天数=" + (visitDate + distance[currentCity][XI_AN_NUM] / 8)
                + ", 今年旅游天数=" + (visitDateThisYear + + distance[currentCity][XI_AN_NUM] / 8) + ", 今年旅游次数=" + times + ", 旅游年数=" + years
                + "旅游开销=" + cost + ", 旅游里程=" + tourLength + ", 旅游的城市数量=" + cityNum + ", 本人所在城市=" + placeMap.get(firstCity).getPlaceName() + ", 具体行程文件="
                + fileName + "]";
    }

    /**
     * 初始化蚂蚁，随机选择起始位置(固定为西安)
     * 
     * @param distance
     *            距离矩阵
     * @param a
     *            alpha
     * @param b
     *            beta
     * @param placeMap2
     */
    public void init(int[][] distance, double a, double b, Map<Integer, Place> placeMap, 
            double[][] planeCost, double[][] planeTime, double[][] trainCost, double[][] trainTime) {
        isDead = false;
        alpha = a;
        beta = b;
        allowedCities = new Vector<Integer>();
        tabu = new Vector<Integer>();
        currentTime = 7;
        visitDate = 1;
        visitDateThisYear = 1;
        times = 1;
        years = 1;
        this.placeMap = placeMap;
        this.distance = distance;
        delta = new double[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            allowedCities.add(i);
            for (int j = 0; j < cityNum; j++) {
                delta[i][j] = 0;
            }
        }
        this.planeCost = planeCost;
        this.planeTime = planeTime;
        this.trainCost = trainCost;
        this.trainTime = trainTime;
        
        this.avgDistance = new int[distance.length];
        
        // 算出平均距离
        for (int i = 0; i < distance.length; i++) {
            int sum = 0;
            for (int j = 0; j < distance.length; j++) {
                sum += distance[i][j];
            }
            System.out.println(sum);
            System.out.println(avgDistance[i]);
            avgDistance[i] = sum / distance.length;
        }

        // Random random = new Random(System.currentTimeMillis());
        // firstCity = random.nextInt(cityNum);
        firstCity = XI_AN_NUM;
        allowedCities.remove(firstCity);

        tabu.add(Integer.valueOf(firstCity));
        currentCity = firstCity;
    }

    /**
     * 选择下一个城市
     * 
     * @param pheromone
     *            信息素矩阵
     * @throws IOException
     */
    public void selectNextCity(double[][] pheromone) throws IOException {
        
        if (tripMode == 0) {
            // Vector<Integer> neighborCities;
            double[] p = new double[cityNum];
            // 计算分母部分
//            p = calculateP1(pheromone);
            p = calculateP2(pheromone);

            // 轮盘赌选择下一个城市
            Random random = new Random(System.currentTimeMillis());
            double selectP = random.nextDouble();
            int selectCity = XI_AN_NUM;
            double sum1 = 0;
            for (int i = 0; i < cityNum; i++) {
                sum1 += p[i];
                if (sum1 >= selectP) {
                    selectCity = i;
                    break;
                }
            }
            
            // 如果是出发城市，则下一个城市随机进行选择
            if (currentCity == XI_AN_NUM) {
                tripMode = selectTripeMode(currentCity, selectCity);
                int randomNum = random.nextInt(allowedCities.size());
                selectCity = allowedCities.get(randomNum);
            }
            
            updateStatusSelfDrive(selectCity);
            
            // 从允许选择的城市中去除select city
            for (Integer i : allowedCities) {
                if (i.intValue() == selectCity) {
                    allowedCities.remove(i);
                    break;
                }
            }
            // 在禁忌表中添加select city
            tabu.add(Integer.valueOf(selectCity));
            // 将当前城市改为选择的城市
            currentCity = selectCity;
        } else {
            // 如果当前城市不是省会，则此时肯定在租车，要考虑到很多因素
            if (!currentCityIsBig()) {
                
                double[] p = new double[cityNum];
                // 计算分母部分
//                p = calculateP1(pheromone);
                p = calculateP3(pheromone);

                // 轮盘赌选择下一个城市
                Random random = new Random(System.currentTimeMillis());
                double selectP = random.nextDouble();
                int selectCity = XI_AN_NUM;
                double sum1 = 0;
                for (int i = 0; i < cityNum; i++) {
                    sum1 += p[i];
                    if (sum1 >= selectP) {
                        selectCity = i;
                        break;
                    }
                }
                
                updateStatusOfNotBigCity(selectCity);
                
                // 从允许选择的城市中去除select city
                for (Integer i : allowedCities) {
                    if (i.intValue() == selectCity) {
                        allowedCities.remove(i);
                        break;
                    }
                }
                // 在禁忌表中添加select city
                tabu.add(Integer.valueOf(selectCity));
                // 将当前城市改为选择的城市
                currentCity = selectCity;
                                
            } else {
                // 如果是省会城市的话
                
                double[] p = new double[cityNum];
                // 计算分母部分

                p = calculateP4(pheromone);

                // 轮盘赌选择下一个城市
                Random random = new Random(System.currentTimeMillis());
                double selectP = random.nextDouble();
                int selectCity = XI_AN_NUM;
                double sum1 = 0;
                for (int i = 0; i < cityNum; i++) {
                    sum1 += p[i];
                    if (sum1 >= selectP) {
                        selectCity = i;
                        break;
                    }
                }
                
                updateStatusOfBigCity(selectCity);
                
                // 从允许选择的城市中去除select city
                for (Integer i : allowedCities) {
                    if (i.intValue() == selectCity) {
                        allowedCities.remove(i);
                        break;
                    }
                }
                // 在禁忌表中添加select city
                tabu.add(Integer.valueOf(selectCity));
                // 将当前城市改为选择的城市
                currentCity = selectCity;
            }
        }
                
    }

    private void updateStatusOfBigCity(int selectCity) throws IOException {
        if (selectCity >= 31 || currentCity >= 31) {
            updateStatusOfNotBigCity(selectCity);
        } else {
            calculateNeedDateAndChangePlaneOrTrain(currentCity, selectCity, tripMode);
        }
        
    }

    private double[] calculateP4(double[][] pheromone) {
        double[] p = new double[cityNum];
        double sum = 0;
        
        // 下面这段算法的思路是找到距离最小的五个地点的下标，使用了一个辅助数组
        int[] cities = distance[currentCity].clone();
        
        int[] indexs = new int[cities.length];
        for (int i = 0; i < cities.length; i++) {
            int min = i;
            for (int j = 0; j < cities.length; j++) {
                if (cities[j] < cities[min]) {
                    min = j;
                }                
            }
            cities[min] = Integer.MAX_VALUE;
            indexs[i] = min;            
        }

        
        // 算法思想，判断离得最近的5个节点，然后将这5个节点与西安节点同时作为计算下一次访问概率的参数
        HashSet<Integer> nextNodes = new HashSet<Integer>();
        for (int i = 0, j = 0; i< indexs.length && j < 5; i++) {
            if (allowedCities.contains(indexs[i])) {
                if (calculateNotDeadCapitalCity(indexs[i])) {
                    j++;
                    nextNodes.add(indexs[i]);
                }
            }
        }
        if (nextNodes.size() == 0) {
            nextNodes.add(XI_AN_NUM);
        }
        for (Integer i : nextNodes) {
            sum += Math.pow(pheromone[currentCity][i.intValue()], alpha)
                    * Math.pow(1.0 / distance[currentCity][i.intValue()], beta);
            // distance[currentCity][i.intValue()] 改成 T
        }

        // 计算概率矩阵，给回西安一个概率,与其它城市的概率加起来为1
        for (int i = 0; i < cityNum; i++) {
            if (nextNodes.contains(i)) {
                p[i] = (double)(Math.pow(pheromone[currentCity][i], alpha) * Math.pow(1.0 / distance[currentCity][i], beta))
                        / sum;
            } else {
                p[i] = 0;
            }

        }
        return p;
    }

    private boolean calculateNotDeadCapitalCity(int selectP) {
        if (selectP >= 31) {
            double routeDate1 = calculateNeedDateSelfDrive(currentTime, currentCity, selectP); //过去的时间
            double routeDate2 = calculateNeedDateSelfDrive(currentTime, selectP, currentCity); //过去的时间
            double stayDate = placeMap.get(selectP).getStayDate(); //游玩时间
            double backDate = quickestBackTime(currentCity,XI_AN_NUM);    // 返回西安的时间
            
            if (((routeDate1 + routeDate2+ stayDate + (int)(backDate/8) + visitDate) < 15) 
                    && ((routeDate1 + stayDate + (int)(backDate/8) + visitDateThisYear) < 30)) {
                return true;
            } else {
                return false;   
            }
        } else {
            double routeDate1 = quickestBackTime(currentCity,selectP); 
            double routeDate2 = quickestBackTime(selectP,XI_AN_NUM); 
            double stayDate = placeMap.get(selectP).getStayDate(); //游玩时间
            
            if (((routeDate1 + routeDate2+ stayDate+ visitDate) < 15) 
                    && ((routeDate1 + stayDate + visitDateThisYear) < 30)) {
                return true;
            } else {
                return false;   
            }
        }        
    }

    private void updateStatusOfNotBigCity(int selectP) throws IOException {
        
        fileWriter = new FileWriter(fileName, true);
        FileWriter fileWriterTable = new FileWriter(fileName, true);
        
        Place selectPlace = placeMap.get(selectP);
        String selectCityName = placeMap.get(selectP).getPlaceName();
        String currentCityName = placeMap.get(currentCity).getPlaceName();
        int routeDistance = distance[currentCity][selectP];
        
        int needDate = 0;
        if (selectP < 31) {
            needDate += 1;
            cost += 600;
        }
        
        int rawDistance = 90 * distance[currentCity][selectP];
        int needDate2 = distance[currentCity][selectP] / 8;
        // 计算在路上的花费
        int roadCost = calCostOnRoadByCar(needDate2, rawDistance);
        needDate += needDate2;

        cost = cost + roadCost;
        
        fileWriter.append("租车从" + currentCityName + "出发到" + selectCityName + "花费了" + roadCost + "\r\n");
        if (currentTime + distance[currentCity][selectP] < 19) {
            currentTime += distance[currentCity][selectP];
        } else {
            currentTime = currentTime + distance[currentCity][selectP] - 19 + 7;
        }

        int currentTime2 = currentTime;
        // 停留时间是整数时间
        if ((int) selectPlace.getStayDate() == selectPlace.getStayDate()) {
            if (currentTime2 <= 10) {
                needDate += selectPlace.getStayDate() - 1;
                currentTime2 = currentTime2 + 8;
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达后，在" + selectCityName + "游玩总共"
                        + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                        + currentTime2 + "点\r\n");

            } else if (currentTime2 <= 14) {
                if (selectPlace.isHasHalfDay()) {// 判断是否有半天的项目,you
                    if (routeDistance <= 5) {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : 下午到达，开车时间少于5小时，且有半天的项目，故可先游玩半天。" + currentTime2 + "点到达后，在" + selectCityName + "游玩总共"
                                + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                                + 12 + "点\r\n");

                        needDate += selectPlace.getStayDate() - 1;
                        currentTime2 = 12;
                        
                    } else {
                        needDate += selectPlace.getStayDate() + 1;
                        currentTime2 = 7;
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达" + selectCityName + "后，开车时间大于5小时，休息到明天早上8点开始游玩，游玩总共"
                                + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                                + currentTime2 + "点\r\n");

                    }
                } else {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : 无半天的项目，" + currentTime2 + "点到达后，休息到第二天早上8点，在" + selectCityName + "游玩总共"
                            + (selectPlace.getStayDate()+1) + "天,玩完这边的景点要到" + (selectPlace.getStayDate() + 1) + "天后的当天"
                            + 7 + "点\r\n");

                    needDate += selectPlace.getStayDate() + 1;
                    currentTime2 = 7;

                }
            } else {
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达后，休息到第二天早上8点，在" + selectCityName + "游玩总共"
                        + (selectPlace.getStayDate() + 1) + "天,玩完这边的景点要到" + (selectPlace.getStayDate() + 1) + "天后的当天"
                        + 7 + "点\r\n");

                needDate += selectPlace.getStayDate() + 1;
                currentTime2 = 7;
            }
        } else {
            // 代表停留时间中有半天的情况
            if (currentTime2 < 10) {
                if (selectPlace.getStayDate() <= 1) {

                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达" + selectCityName + ",游玩半天到当天的"+ (currentTime2 + 4) + "点\r\n");
                    currentTime2 = currentTime2 + 4;
                } else {

                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达" + selectCityName + ",游玩到" + selectPlace.getStayDate() + "天后的"+ 12 + "点\r\n");
                    needDate += selectPlace.getStayDate();
                    currentTime2 = 12;
                }
            } else if (currentTime2 <= 14) {
                if (routeDistance <= 5) {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达" + selectCityName + ",先玩半天后休息，然后游玩这个地方到" + (selectPlace.getStayDate() + 0.5) + "天后的上午7点\r\n");

                    needDate += selectPlace.getStayDate() + 0.5;
                    currentTime2 = 7;
                } else {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达" + selectCityName + ",开车时间超过5小时，所以休息，第二天起来后玩到" + (selectPlace.getStayDate() + 0.5) + "天后的中午12点\r\n");

                    needDate += selectPlace.getStayDate() + 0.5;
                    currentTime2 = 12;
                }
            } else {
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + currentTime2 + "点到达" + selectCityName + ",到达时间超过14点，所以休息，第二天起来后玩到第" + (selectPlace.getStayDate() + 0.5) + "天的中午12点\r\n");

                needDate += selectPlace.getStayDate() - 0.5;
                currentTime2 = 12;
            }
        }
        
        int cityCost = calCostOnCity(needDate, selectP);
        fileWriter.append("在这个地方呆的这几天共花费了 " + cityCost + " 块钱\r\n");
        cost += cityCost;
        visitDate += needDate;
        visitDateThisYear += needDate;
        this.currentTime = currentTime2;
        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + selectCityName + "已经完毕,共用去" + needDate + "天，现在是" + currentTime2 + "点\r\n");
        fileWriterTable.append((years + 2015) + "年第 " + times + " 次,第 " + visitDate + " 天： " + currentCityName + "\t" + needDate + "d\t" + routeDistance + "h\t" + (routeDistance * 90) + "km\t" + selectCityName + "\r\t");
    
                
        fileWriter.flush();
        fileWriter.close();
        fileWriterTable.flush();
        fileWriterTable.close();

        if (visitDate > 15) {
            System.out.print("饿死  " + visitDate + " year " + visitDateThisYear);
            isDead = true;
        } else if (visitDateThisYear > 30) { 
            // 如果今年访问天数超过30，则认为这次旅行是下一年的旅行
            years += 1;
            times = 1;
            visitDateThisYear = visitDate;
            return;
        }
        
    }
    


    private double[] calculateP3(double[][] pheromone) {
        double[] p = new double[cityNum];
        double sum = 0;
        
        // 下面这段算法的思路是找到距离最小的五个地点的下标，使用了一个辅助数组
        int[] cities = distance[currentCity].clone();
        
        int[] indexs = new int[cities.length];
        for (int i = 0; i < cities.length; i++) {
            int min = i;
            for (int j = 0; j < cities.length; j++) {
                if (cities[j] < cities[min]) {
                    min = j;
                }                
            }
            cities[min] = Integer.MAX_VALUE;
            indexs[i] = min;            
        }

        
        // 算法思想，判断离得最近的5个节点，然后将这5个节点与西安节点同时作为计算下一次访问概率的参数
        HashSet<Integer> nextNodes = new HashSet<Integer>();
        for (int i = 0, j = 0; i< indexs.length && j < 5; i++) {
            if (allowedCities.contains(indexs[i])) {
                if (calculateNotDeadRent(indexs[i])) {
                    j++;
                    nextNodes.add(indexs[i]);
                }
            }
        }
        if (nextNodes.size() == 0) {
            nextNodes.add(closestCapitalCity);
        }
        for (Integer i : nextNodes) {
            sum += Math.pow(pheromone[currentCity][i.intValue()], alpha)
                    * Math.pow(1.0 / distance[currentCity][i.intValue()], beta);
            // distance[currentCity][i.intValue()] 改成 T
        }

        // 计算概率矩阵，给回西安一个概率,与其它城市的概率加起来为1
        for (int i = 0; i < cityNum; i++) {
            if (nextNodes.contains(i)) {
                p[i] = (double)(Math.pow(pheromone[currentCity][i], alpha) * Math.pow(1.0 / distance[currentCity][i], beta))
                        / sum;
            } else {
                p[i] = 0;
            }

        }
        return p;
    }

    // 判断是否会活
    private boolean calculateNotDeadRent(int selectP) {
        double routeDate1 = calculateNeedDateSelfDrive(currentTime, currentCity, selectP); //过去的时间
        double routeDate2 = calculateNeedDateSelfDrive(currentTime, selectP, closestCapitalCity); //过去的时间
        double stayDate = placeMap.get(selectP).getStayDate(); //游玩时间
        double backDate = quickestBackTime(closestCapitalCity,XI_AN_NUM);    // 返回西安的时间
        if (((routeDate1 + routeDate2+ stayDate + (int)(backDate/8) + visitDate) < 15) 
                && ((routeDate1 + stayDate + (int)(backDate/8) + visitDateThisYear) < 30)) {
            return true;
        } else {
            return false;   
        }
        
    }

    private double quickestBackTime(int closestCapitalCity2, int xiAnNum) {
        if (planeTime[closestCapitalCity2][xiAnNum] > 0) {
            return planeTime[closestCapitalCity2][xiAnNum];
        } else {
            return trainTime[closestCapitalCity2][xiAnNum];
        }
    }

    private boolean currentCityIsBig() {
        return placeMap.get(currentCity).getType() == 0;

    }

    // 计算下一个转移数组的概率，输入信息素，对返回西安概率设定一个固定的概率
    private double[] calculateP1(double[][] pheromone) {
        double[] p = new double[cityNum];
        double sum = 0;
        for (Integer i : allowedCities) {
            sum += Math.pow(pheromone[currentCity][i.intValue()], alpha)
                    * Math.pow(1.0 / distance[currentCity][i.intValue()], beta);
            // distance[currentCity][i.intValue()] 改成 T
        }

        // 计算概率矩阵，给回西安一个概率,与其它城市的概率加起来为1
        for (int i = 0; i < cityNum; i++) {
            if (allowedCities.contains(i)) {
                p[i] = (double) (1 - P_STARTCITY)
                        * (Math.pow(pheromone[currentCity][i], alpha) * Math.pow(1.0 / distance[currentCity][i], beta))
                        / sum;
            } else {
                p[i] = 0;
            }
            p[XI_AN_NUM] = P_STARTCITY;

        }
        return p;
    }
    
    // 计算下一个转移数组的概率，输入信息素，对返回西安概率设定一个固定的概率
    private double[] calculateP2(double[][] pheromone) {
        double[] p = new double[cityNum];
        double sum = 0;
        
        // 下面这段算法的思路是找到距离最小的五个地点的下标，使用了一个辅助数组
        int[] cities = distance[currentCity].clone();
        
        int[] indexs = new int[cities.length];
        for (int i = 0; i < cities.length; i++) {
            int min = i;
            for (int j = 0; j < cities.length; j++) {
                if (cities[j] < cities[min]) {
                    min = j;
                }                
            }
            cities[min] = Integer.MAX_VALUE;
            indexs[i] = min;            
        }

        
        // 算法思想，判断离得最近的5个节点，然后将这5个节点与西安节点同时作为计算下一次访问概率的参数
        HashSet<Integer> nextNodes = new HashSet<Integer>();
        for (int i = 0, j = 0; i< indexs.length && j < 5; i++) {
            if (allowedCities.contains(indexs[i])) {
                if (calculateNotDeadSelfDrive(indexs[i])) {
                    j++;
                    nextNodes.add(indexs[i]);
                }
            }
        }
        if (nextNodes.size() ==0) {
            nextNodes.add(XI_AN_NUM);
        }
        for (Integer i : nextNodes) {
            sum += Math.pow(pheromone[currentCity][i.intValue()], alpha)
                    * Math.pow(1.0 / distance[currentCity][i.intValue()], beta);
            // distance[currentCity][i.intValue()] 改成 T
        }

        // 计算概率矩阵，给回西安一个概率,与其它城市的概率加起来为1
        for (int i = 0; i < cityNum; i++) {
            if (nextNodes.contains(i)) {
                p[i] = (double)(Math.pow(pheromone[currentCity][i], alpha) * Math.pow(1.0 / distance[currentCity][i], beta))
                        / sum;
            } else {
                p[i] = 0;
            }

        }
        return p;
    }

    // 计算当前节点到selectP节点后能否安全返回西安
    private boolean calculateNotDeadSelfDrive(int selectP) {
        double routeDate = calculateNeedDateSelfDrive(currentTime, selectP); //过去的时间
        double stayDate = placeMap.get(selectP).getStayDate(); //游玩时间
        int backDate = distance[selectP][XI_AN_NUM ];    // 返回西安的时间
        if (((routeDate + stayDate + (int)(backDate/8) + visitDate) < 15) 
                && ((routeDate + stayDate + (int)(backDate/8) + visitDateThisYear) < 30)) {
            return true;
        } else {
            return false;   
        }
    }

    // 更新蚂蚁的状态,记录日志能功能
    private void updateStatusSelfDrive(int selectP) throws IOException {

        if (tripMode == 0) {
         // 计算修改后的时间，日期，等信息
            calculateNeedDateAndChangeSelfDrive(currentTime, selectP);
        } else {
            calculateNeedDateAndChangePlaneOrTrain(currentTime, selectP, tripMode);
        }
        

        if (visitDate > 15) {
            System.out.print("饿死  " + visitDate + " year " + visitDateThisYear);
            isDead = true;
        } else if (visitDateThisYear > 30) { 
            // 如果今年访问天数超过30，则认为这次旅行是下一年的旅行
            years += 1;
            times = 1;
            visitDateThisYear = visitDate;
            return;
        }
    }

    private void calculateNeedDateAndChangePlaneOrTrain(int curruntTime, int selectP, int tripMode) throws IOException {
        double needDate = 0;
        
        
        
        Place selectPlace = placeMap.get(selectP);
//        System.out.println(currentCity + " " + selectP);
        double routeDistance = 0;
        if (calTrainTime(currentCity, selectP) > 0) {
            routeDistance = trainTime[currentCity][selectP];
        } else if (calPlaneTime(currentCity, selectP) > 0) {
            routeDistance = trainTime[currentCity][selectP];
        }
        
        String currentCityName = placeMap.get(currentCity).getPlaceName();
        String selectCityName = placeMap.get(selectP).getPlaceName();
        
        fileWriter = new FileWriter(fileName, true);
        FileWriter fileWriterTable = new FileWriter(fileName.split(",")[0] + "_table.txt", true);

        if (routeDistance >= (19 - curruntTime)) {
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + "现在是" + curruntTime + "点，时间不够不走了，在当地住一晚\r\n");
            needDate += 1;
            cost += 200 * 3;
            cost += 3 * ((tripMode == 1) ? trainCost[currentCity][selectP] : planeCost[currentCity][selectP]);
            curruntTime = 7 + (int)routeDistance;
        } else {
            if (tripMode == 1) {
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 :坐火车 " + (curruntTime) + "点从 " + currentCityName + " 出发，经过" + routeDistance + "小时,"
                        + (curruntTime + routeDistance) + "点到达 " + selectCityName + "\r\n");
                cost += 3 * distance[currentCity][selectP];
                curruntTime = curruntTime + (int)routeDistance;
            } else {
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 :坐飞机 " + (curruntTime) + "点从 " + currentCityName + " 出发，经过" + routeDistance + "小时,"
                        + (curruntTime + routeDistance) + "点到达 " + selectCityName + "\r\n");
                cost += 3 * calPlaneCost(currentCity, selectP);
                curruntTime = curruntTime + (int)routeDistance;
            }
            
        }
        
        if (isStartPlace(selectP)) {
//            System.out.println("--------回到了西安" + visitDateThisYear + " " + currentTime + " " + times + " " + visitDate + " " + years);
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点回到了自己的家乡西安，旅游次数增加，旅游天数清零\r\n");
            fileWriterTable.append((years + 2015) + "年第 " + times + " 次,第 " + visitDate + " 天： " + currentCityName + "\t" + needDate + "d\t" + routeDistance + "h\t" + (routeDistance * 90) + "km\t" + selectCityName + "\r\n");

            currentTime = 7;
            visitDate = 1;
            visitDateThisYear += needDate;
            times += 1;
            if (times > 4 || visitDateThisYear >= 30) {
                times = 1;
                years += 1;
                visitDateThisYear = 1;
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "旅游年数增加1\r\n");
            }
        } else {
            // 如果是省会，则日期自动加1
            if (selectPlace.getType() == 0) {
                needDate += 1;
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + "第一次来到了这个省会，玩一天，体验这里的风土人情~\r\n");
                
            }

            // 停留时间是整数时间
            if ((int) selectPlace.getStayDate() == selectPlace.getStayDate()) {
                if (curruntTime <= 10) {
                    needDate += selectPlace.getStayDate() - 1;
                    curruntTime = curruntTime + 8;
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达后，在" + selectCityName + "游玩总共"
                            + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                            + curruntTime + "点\r\n");

                } else if (curruntTime <= 14) {
                    if (selectPlace.isHasHalfDay()) {// 判断是否有半天的项目,you
                        if (routeDistance <= 5) {
                            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : 下午到达，开车时间少于5小时，且有半天的项目，故可先游玩半天。" + curruntTime + "点到达后，在" + selectCityName + "游玩总共"
                                    + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                                    + 12 + "点\r\n");

                            needDate += selectPlace.getStayDate() - 1;
                            curruntTime = 12;
                            
                        } else {
                            needDate += selectPlace.getStayDate() + 1;
                            curruntTime = 7;
                            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + "后，开车时间大于5小时，休息到明天早上8点开始游玩，游玩总共"
                                    + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                                    + curruntTime + "点\r\n");

                        }
                    } else {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : 无半天的项目，" + curruntTime + "点到达后，休息到第二天早上8点，在" + selectCityName + "游玩总共"
                                + (selectPlace.getStayDate()+1) + "天,玩完这边的景点要到" + (selectPlace.getStayDate() + 1) + "天后的当天"
                                + 7 + "点\r\n");

                        needDate += selectPlace.getStayDate() + 1;
                        curruntTime = 7;

                    }
                } else {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达后，休息到第二天早上8点，在" + selectCityName + "游玩总共"
                            + (selectPlace.getStayDate() + 1) + "天,玩完这边的景点要到" + (selectPlace.getStayDate() + 1) + "天后的当天"
                            + 7 + "点\r\n");

                    needDate += selectPlace.getStayDate() + 1;
                    curruntTime = 7;
                }
            } else {
                // 代表停留时间中有半天的情况
                if (curruntTime < 10) {
                    if (selectPlace.getStayDate() <= 1) {

                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",游玩半天到当天的"+ (curruntTime + 4) + "点\r\n");
                        curruntTime = curruntTime + 4;
                    } else {

                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",游玩到" + selectPlace.getStayDate() + "天后的"+ 12 + "点\r\n");
                        needDate += selectPlace.getStayDate();
                        curruntTime = 12;
                    }
                } else if (curruntTime <= 14) {
                    if (routeDistance <= 5) {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",先玩半天后休息，然后游玩这个地方到" + (selectPlace.getStayDate() + 0.5) + "天后的上午7点\r\n");

                        needDate += selectPlace.getStayDate() + 0.5;
                        curruntTime = 7;
                    } else {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",开车时间超过5小时，所以休息，第二天起来后玩到" + (selectPlace.getStayDate() + 0.5) + "天后的中午12点\r\n");

                        needDate += selectPlace.getStayDate() + 0.5;
                        curruntTime = 12;
                    }
                } else {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",到达时间超过14点，所以休息，第二天起来后玩到第" + (selectPlace.getStayDate() + 0.5) + "天的中午12点\r\n");

                    needDate += selectPlace.getStayDate() - 0.5;
                    curruntTime = 12;
                }
            }
            visitDate += needDate;
            visitDateThisYear += needDate;
            this.currentTime = curruntTime;
            cost += calCostOnCity((int) needDate,selectPlace.getType());
            fileWriter.append("本次旅游花费为" + calCostOnCity((int) needDate,selectPlace.getType()) + "元,目前已经用了" + cost + "元\r\n");
            fileWriterTable.append((years + 2015) + "年第 " + times + " 次,第 " + visitDate + " 天： " + currentCityName + "\t" + needDate + "d\t" + routeDistance + "h\t" + (routeDistance * 90) + "km\t" + selectCityName + "\r\n");

            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + selectCityName + "已经完毕,共用去" + needDate + "天，现在是" + curruntTime + "点\r\n");
            fileWriterTable.append((years + 2015) + "年第 " + times + " 次,第 " + visitDate + " 天： " + currentCityName + "\t" + needDate + "d\t" + routeDistance + "h\t" + (routeDistance * 90) + "km\t" + selectCityName + "\r\n");
            
        }
        
        fileWriter.flush();
        fileWriter.close();
        fileWriterTable.flush();
        fileWriterTable.close();
    }
        
    // 走这条路，并且更新蚂蚁的时间属性，即更新 今天的时间，本次天数，本年天数，本年次数，年数
    private void calculateNeedDateAndChangeSelfDrive(int curruntTime, int selectP) throws IOException {
        double needDate = 0;
        
        Place selectPlace = placeMap.get(selectP);
//        System.out.println(currentCity + " " + selectP);
        
        
        int routeDistance = distance[currentCity][selectP];
        String currentCityName = placeMap.get(currentCity).getPlaceName();
        String selectCityName = placeMap.get(selectP).getPlaceName();
        fileWriter = new FileWriter(fileName, true);
        FileWriter fileWriterTable = new FileWriter(fileName + "_table.txt",true);

        
        if (routeDistance >= 8) {
            // 如果路程大于8小时，则先把路程时间减去今天可以走的时间，最多不超过8小时，然后计算在路上的天数和最后一天早上距离终点的时间，即将路上时间转化为小于8点的情况
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + "现在是" + curruntTime + "点，从 "
                    + currentCityName + " 到 " + selectCityName + " 需要" + routeDistance + "个小时,路上需要经过"
                    + ((routeDistance / 8) + 1) + "天" + ",假设我们已经到了目标地区附近," + ((routeDistance / 8) + 1)
                    + "天后早上7点的时候，我们距目标地区还有" + routeDistance % 8 + "小时\r\n");
            routeDistance -= ((19 - curruntTime) > 8) ? 8 : (19 - curruntTime);
            // needDate += 1;
            needDate += (routeDistance / 8);
            routeDistance = routeDistance % 8;
            curruntTime = 7;
            
            cost += needDate * COST_EACH_NIGHT_OF_CAR;
        }
        int routeDate = (int)needDate;

        if (routeDistance >= (19 - curruntTime)) {
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + "现在是" + curruntTime + "点，从" + currentCityName + "到" + selectCityName + "需要"
                    + routeDistance + "个小时，晚于19点，在路上休息一晚，明天早上七点出发\r\n");
            needDate += 1;
            routeDistance -= (19 - curruntTime);
            curruntTime = 7;
        } else {
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + (curruntTime) + "点从 " + currentCityName + " 出发，经过" + routeDistance + "小时,"
                    + (curruntTime + routeDistance) + "点到达 " + selectCityName + "\r\n");
            curruntTime = curruntTime + routeDistance;
        }
        
        if (isStartPlace(selectP)) {
//            System.out.println("--------回到了西安" + visitDateThisYear + " " + currentTime + " " + times + " " + visitDate + " " + years);
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点回到了自己的家乡西安，旅游次数增加，旅游天数清零\r\n");
            fileWriterTable.append((years + 2015) + "年第 " + times + " 次,第 " + visitDate + " 天： " + currentCityName + "\t" + needDate + "d\t" + routeDistance + "h\t" + (routeDistance * 90) + "km\t" + selectCityName + "\r\n");

            currentTime = 7;
            visitDate = 1;
            visitDateThisYear += needDate;
            times += 1;
            if (times > 4 || visitDateThisYear >= 30) {
                times = 1;
                years += 1;
                visitDateThisYear = 1;
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "旅游年数增加1\r\n");
            }
        } else {
         // 如果是省会，则日期自动加1
            if (selectPlace.getType() == 0) {
                needDate += 1;
                fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + "第一次来到了这个省会，玩一天，体验这里的风土人情~\r\n");
                
            }

            // 停留时间是整数时间
            if ((int) selectPlace.getStayDate() == selectPlace.getStayDate()) {
                if (curruntTime <= 10) {
                    needDate += selectPlace.getStayDate() - 1;
                    curruntTime = curruntTime + 8;
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达后，在" + selectCityName + "游玩总共"
                            + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                            + curruntTime + "点\r\n");

                } else if (curruntTime <= 14) {
                    if (selectPlace.isHasHalfDay()) {// 判断是否有半天的项目,you
                        if (routeDistance <= 5) {
                            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : 下午到达，开车时间少于5小时，且有半天的项目，故可先游玩半天。" + curruntTime + "点到达后，在" + selectCityName + "游玩总共"
                                    + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                                    + 12 + "点\r\n");

                            needDate += selectPlace.getStayDate() - 1;
                            curruntTime = 12;
                            
                        } else {
                            needDate += selectPlace.getStayDate() + 1;
                            curruntTime = 7;
                            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + "后，开车时间大于5小时，休息到明天早上8点开始游玩，游玩总共"
                                    + selectPlace.getStayDate() + "天,玩完这边的景点要到" + (selectPlace.getStayDate() - 1) + "天后的当天"
                                    + curruntTime + "点\r\n");

                        }
                    } else {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : 无半天的项目，" + curruntTime + "点到达后，休息到第二天早上8点，在" + selectCityName + "游玩总共"
                                + (selectPlace.getStayDate()+1) + "天,玩完这边的景点要到" + (selectPlace.getStayDate() + 1) + "天后的当天"
                                + 7 + "点\r\n");

                        needDate += selectPlace.getStayDate() + 1;
                        curruntTime = 7;

                    }
                } else {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达后，休息到第二天早上8点，在" + selectCityName + "游玩总共"
                            + (selectPlace.getStayDate() + 1) + "天,玩完这边的景点要到" + (selectPlace.getStayDate() + 1) + "天后的当天"
                            + 7 + "点\r\n");

                    needDate += selectPlace.getStayDate() + 1;
                    curruntTime = 7;
                }
            } else {
                // 代表停留时间中有半天的情况
                if (curruntTime < 10) {
                    if (selectPlace.getStayDate() <= 1) {

                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",游玩半天到当天的"+ (curruntTime + 4) + "点\r\n");
                        curruntTime = curruntTime + 4;
                    } else {

                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",游玩到" + selectPlace.getStayDate() + "天后的"+ 12 + "点\r\n");
                        needDate += selectPlace.getStayDate();
                        curruntTime = 12;
                    }
                } else if (curruntTime <= 14) {
                    if (routeDistance <= 5) {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",先玩半天后休息，然后游玩这个地方到" + (selectPlace.getStayDate() + 0.5) + "天后的上午7点\r\n");

                        needDate += selectPlace.getStayDate() + 0.5;
                        curruntTime = 7;
                    } else {
                        fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",开车时间超过5小时，所以休息，第二天起来后玩到" + (selectPlace.getStayDate() + 0.5) + "天后的中午12点\r\n");

                        needDate += selectPlace.getStayDate() + 0.5;
                        curruntTime = 12;
                    }
                } else {
                    fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + curruntTime + "点到达" + selectCityName + ",到达时间超过14点，所以休息，第二天起来后玩到第" + (selectPlace.getStayDate() + 0.5) + "天的中午12点\r\n");

                    needDate += selectPlace.getStayDate() - 0.5;
                    curruntTime = 12;
                }
            }
            visitDate += needDate;
            visitDateThisYear += needDate;
            cost += (needDate - routeDate) * 200 * 3;
            this.currentTime = curruntTime;
            fileWriter.append((years + 2015) + "年第" + times + "次旅游的第" + visitDate + "天 : " + selectCityName + "已经完毕,共用去" + needDate + "天，现在是" + curruntTime + "点\r\n");
            fileWriterTable.append((years + 2015) + "年第 " + times + " 次,第 " + visitDate + " 天： " + currentCityName + "\t" + needDate + "d\t" + routeDistance + "h\t" + (routeDistance * 90) + "km\t" + selectCityName + "\r\n");
            
        }
        
        fileWriter.flush();
        fileWriter.close();
        fileWriterTable.flush();
        fileWriterTable.close();
    }

    private double calculateNeedDateSelfDrive(int curruntTime, int startP, int selectP) {
        double needDate = 0;
        Place selectPlace = placeMap.get(selectP);
        int routeDistance = distance[startP][selectP];

        if (routeDistance > 8) {
            // 如果路程大于8小时，则先把路程时间减去今天可以走的时间，最多不超过8小时，然后计算在路上的天数和最后一天早上距离终点的时间，即将路上时间转化为小于8点的情况
            
            routeDistance -= ((18 - curruntTime) > 8) ? 8 : (18 - curruntTime);
            needDate += 1;
            needDate += (routeDistance / 8);
            routeDistance = routeDistance % 8;
            curruntTime = 7;
        }

        if ((curruntTime + routeDistance) > 19) {
            curruntTime = 7;
            needDate += 1;

        } else {
            curruntTime = curruntTime + routeDistance;
        }

        // 停留时间是整数时间
        if ((int) selectPlace.getStayDate() == selectPlace.getStayDate()) {
            if (curruntTime <= 10) {
                needDate += selectPlace.getStayDate() - 1;
                curruntTime = curruntTime + 8;
            } else if (curruntTime <= 14) {
                if (selectPlace.isHasHalfDay()) {// 判断是否有半天的项目,you
                    if (routeDistance <= 5) {
                        needDate += selectPlace.getStayDate() - 1;
                        curruntTime = 12;
                        
                    } else {
                        needDate += selectPlace.getStayDate() + 1;
                        curruntTime = 7;
                    }
                } else {
                    needDate += selectPlace.getStayDate() + 1;
                    curruntTime = 7;

                }
            } else {
                needDate += selectPlace.getStayDate() + 1;
                curruntTime = 7;
            }
        } else {
            // 代表停留时间中有半天的情况
            if (curruntTime <= 10) {
                if (selectPlace.getStayDate() < 1) {
                    curruntTime = curruntTime + 4;
                } else {
                    needDate += selectPlace.getStayDate();
                    curruntTime = 12;
                }
            } else if (curruntTime <= 14) {
                if (routeDistance <= 5) {
                    needDate += selectPlace.getStayDate() + 0.5;
                    curruntTime = 7;
                } else {
                    needDate += selectPlace.getStayDate() + 0.5;
                    curruntTime = 12;
                }
            } else {
                needDate += selectPlace.getStayDate() + 0.5;
                curruntTime = 12;
            }
        }

        return needDate;
        
    }
    // 估计走这条路需要的时间
    private double calculateNeedDateSelfDrive(int curruntTime, int selectP) {
        double needDate = 0;
        Place selectPlace = placeMap.get(selectP);
        int routeDistance = distance[currentCity][selectP];

        if (routeDistance > 8) {
            // 如果路程大于8小时，则先把路程时间减去今天可以走的时间，最多不超过8小时，然后计算在路上的天数和最后一天早上距离终点的时间，即将路上时间转化为小于8点的情况
            
            routeDistance -= ((18 - curruntTime) > 8) ? 8 : (18 - curruntTime);
            needDate += 1;
            needDate += (routeDistance / 8);
            routeDistance = routeDistance % 8;
            curruntTime = 7;
        }

        if ((curruntTime + routeDistance) > 19) {
            curruntTime = 7;
            needDate += 1;

        } else {
            curruntTime = curruntTime + routeDistance;
        }

        // 停留时间是整数时间
        if ((int) selectPlace.getStayDate() == selectPlace.getStayDate()) {
            if (curruntTime <= 10) {
                needDate += selectPlace.getStayDate() - 1;
                curruntTime = curruntTime + 8;
            } else if (curruntTime <= 14) {
                if (selectPlace.isHasHalfDay()) {// 判断是否有半天的项目,you
                    if (routeDistance <= 5) {
                        needDate += selectPlace.getStayDate() - 1;
                        curruntTime = 12;
                        
                    } else {
                        needDate += selectPlace.getStayDate() + 1;
                        curruntTime = 7;
                    }
                } else {
                    needDate += selectPlace.getStayDate() + 1;
                    curruntTime = 7;

                }
            } else {
                needDate += selectPlace.getStayDate() + 1;
                curruntTime = 7;
            }
        } else {
            // 代表停留时间中有半天的情况
            if (curruntTime <= 10) {
                if (selectPlace.getStayDate() < 1) {
                    curruntTime = curruntTime + 4;
                } else {
                    needDate += selectPlace.getStayDate();
                    curruntTime = 12;
                }
            } else if (curruntTime <= 14) {
                if (routeDistance <= 5) {
                    needDate += selectPlace.getStayDate() + 0.5;
                    curruntTime = 7;
                } else {
                    needDate += selectPlace.getStayDate() + 0.5;
                    curruntTime = 12;
                }
            } else {
                needDate += selectPlace.getStayDate() + 0.5;
                curruntTime = 12;
            }
        }

        return needDate;
    }

    private boolean isStartPlace(int selectP) {
        if (selectP == XI_AN_NUM) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 计算路径长度
     * 
     * @return 路径长度
     */
    private int calculateTourLength() {
        int len = 0;
        for (int i = 0; i < cityNum; i++) {
            len += distance[this.tabu.get(i).intValue()][this.tabu.get(i + 1).intValue()];
        }
        return len;
    }

    public int calculateTourTime() {
        int time = 0;
        time = (int) (1000 * years + 10 * visitDateThisYear);
        return time;
    }

    public Vector<Integer> getAllowedCities() {
        return allowedCities;
    }

    public void setAllowedCities(Vector<Integer> allowedCities) {
        this.allowedCities = allowedCities;
    }

    public int getTourLength() {
        tourLength = calculateTourLength();
        return tourLength;
    }

    public void setTourLength(int tourLength) {
        this.tourLength = tourLength;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public Vector<Integer> getTabu() {
        return tabu;
    }

    public void setTabu(Vector<Integer> tabu) {
        this.tabu = tabu;
    }

    public double[][] getDelta() {
        return delta;
    }

    public void setDelta(double[][] delta) {
        this.delta = delta;
    }

    public int getFirstCity() {
        return firstCity;
    }

    public void setFirstCity(int firstCity) {
        this.firstCity = firstCity;
    }

    /**
     * @return the antId
     */
    public int getAntId() {
        return antId;
    }

    /**
     * @param antId
     *            the antId to set
     */
    public void setAntId(int antId) {
        this.antId = antId;
    }
    
    private int selectTripeMode(int currentCity2, int selectCity) {
        if (currentCity2 > 30 || selectCity > 30){
            return 0;
        } else if (currentCity2 != XI_AN_NUM) {
            if ((distance[currentCity2][selectCity] > avgDistance[currentCity2]) && calPlaneTime(currentCity2, selectCity) > 0) {
                return 1;
            } else {
                return 2;
            }
        }
        // 如果距离大于平局距离，则灰机
        if ((distance[currentCity2][selectCity] > avgDistance[currentCity2]) && calPlaneCost(currentCity2, selectCity) > 0) {
            return 2;
        } else {
            if (calTrainTime(currentCity2, selectCity) > 0) {
                int cTime = calculateCarTime(currentCity2, selectCity);
                double tTime = trainTime[currentCity2][selectCity];
                if (cTime > tTime && trainCost[currentCity2][selectCity] > 0) {
                    // 火车
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
            
        }
    }
    
    private double calPlaneCost(int currentCity2, int selectCity) {
        if (currentCity2 < 31 && selectCity < 31) {
            return planeCost[currentCity2][selectCity];
        } else {
            return 0;
        }
        
    }
    
    private double calPlaneTime(int currentCity2, int selectCity) {
        if (currentCity2 >= 31 || selectCity >= 31) {
            return 0;
        } else {
            return planeTime[currentCity2][selectCity];
        }
        
    }
    
    private double calTrainCost(int currentCity2, int selectCity) {
        if (currentCity2 > 31 && selectCity > 31) {
            return 0;
        } else {
            return trainCost[currentCity2][selectCity];
        }
        
    }
    
    private double calTrainTime(int currentCity2, int selectCity) {
        if (currentCity2 < 31 && selectCity < 31) {
            return trainTime[currentCity2][selectCity];
        } else {
            return 0;
        }
    }

    private int calculateCarTime(int currentCity2, int selectCity) {
        // 此distance数组即为路上花费时间的数组
        return distance[currentCity2][selectCity];
    }
    
    private int calCostOnRoadByCar(int needDate, int rawDistance) {
        return (int) (rawDistance * COST_EACH_KM_OF_CAR + 3 * needDate * COST_EACH_NIGHT_OF_CAR + 300 * needDate);
    }
    
    private int calCostOnCity(int needDate, int type) {
        if (type == 3){
            return (int) (100 * needDate * 3 + 300 * needDate);
        } else if (type == 2) {
            return (int) (150 * needDate * 3 + 300 * needDate);
        } else{
            return (int) (200 * needDate * 3 + 300 * needDate);
        }
    }
    

}

