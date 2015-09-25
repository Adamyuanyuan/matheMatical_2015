package matheMatical;

public class Route {

    private int routeId;
    private String routeName;
    private int startPlaceNum;
    private int endPlaceNum;
    private int distance;
    private int driveTime;

    public Route(int routeId, String routeName, int startPlaceNum, int endPlaceNum, int distance, int driveTime) {
        super();
        this.routeId = routeId;
        this.routeName = routeName;
        this.startPlaceNum = startPlaceNum;
        this.endPlaceNum = endPlaceNum;
        this.distance = distance;
        this.driveTime = driveTime;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getStartPlaceNum() {
        return startPlaceNum;
    }

    public void setStartPlaceNum(int startPlaceNum) {
        this.startPlaceNum = startPlaceNum;
    }

    public int getEndPlaceNum() {
        return endPlaceNum;
    }

    public void setEndPlaceNum(int endPlaceNum) {
        this.endPlaceNum = endPlaceNum;
    }

    public Route(int startPlaceNum, int endPlaceNum, int distance, int driveTime) {
        super();
        this.startPlaceNum = startPlaceNum;
        this.endPlaceNum = endPlaceNum;
        this.distance = distance;
        this.driveTime = driveTime;
    }

    public Route(int distance, int driveTime) {
        this.distance = distance;
        this.driveTime = driveTime;
    }

    @Override
    public String toString() {
        return "Route [routeId=" + routeId + ", routeName=" + routeName + ", startPlaceNum=" + startPlaceNum
                + ", endPlaceNum=" + endPlaceNum + ", distance=" + distance + ", driveTime=" + driveTime + "]";
    }

    /**
     * @return the driveTime
     */
    public int getDriveTime() {
        return driveTime;
    }

    /**
     * @param driveTime the driveTime to set
     */
    public void setDriveTime(int driveTime) {
        this.driveTime = driveTime;
    }

    /**
     * @return the distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

}
