package looprac.app;

/**
 * Created by XiaoYong on 2014/6/18.
 */
public class DriverInfo extends PersonInfo{

    private String carModel;
    private float rating;

    public DriverInfo(String usrName, String name, String phoneNumber, int usrType, String carModel, float rating) {
        super(usrName, name, phoneNumber, usrType);
        this.carModel = carModel;
        this.rating = rating;
    }

    /* Get */
    public String getCarModel() { return this.carModel; }

    public float getRating() {
        return this.rating;
    }

    /* Set */
    public void setRating(float rating) { this.rating = rating; }
}
