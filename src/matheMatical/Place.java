package matheMatical;

import java.util.HashMap;
import java.util.Map;


public class Place {
    private static int MAX_YEAR = 3000;
    // 地点编号，景区或者城市
    private int placeNum;
    private String placeName;
    
    public Place(int placeNum, String placeName, double stayDate, boolean hasHalfDay,
            int assessmentYear, int type) {
        super();
        this.placeNum = placeNum;
        this.placeName = placeName;
        this.hasVisited = false;
        this.stayDate = stayDate;
        this.hasHalfDay = hasHalfDay;
        this.assessmentYear = assessmentYear;
        this.type = type;
    }

    private boolean hasVisited = false;
    // 最少需停留时间，仅仅是城市景区的游览时间为0|0.5|1|2 
    private double stayDate = 0.0;
    
    // true|false 代表有半天的景区|无半天的景区 
    private boolean hasHalfDay;
    
    public boolean isHasHalfDay() {
        return hasHalfDay;
    }

    // 如果是5A景区，则代表注册时间，如果不是，则为2015
    private int assessmentYear = MAX_YEAR;
    
    // 是否是5A级景区[0|1|2|3] [省会|景区|地级市|县城],如果为1，是省会的话，天数加一，时间不变
    private int type;

    public int getType() {
        return type;
    }

    public Place(int placeNum, String placeName, double stayDate, int type) {
        super();
        this.placeNum = placeNum;
        this.placeName = placeName;
        this.stayDate = stayDate;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Place [placeNum=" + placeNum + ", placeName=" + placeName + ", hasVisited=" + hasVisited
                + ", stayDate=" + stayDate + ", assessmentYear=" + assessmentYear
                + ", startDistance=" + ", type=" + type + "]";
    }
    
    public double getStayDate() {
        return stayDate;
    }
    
    public String getPlaceName() {
        return placeName;
    }


}
