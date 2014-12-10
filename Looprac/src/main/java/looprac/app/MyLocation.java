package looprac.app;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by XiaoYong on 2014/6/9.
 */
public class MyLocation implements Serializable{
    private String addr;
    private String city;
    private double latitude;
    private double longitude;

    public MyLocation() {
        this.addr = null;
        this.city = null;
        this.latitude = 0;
        this.longitude = 0;
    }

    public MyLocation(String addr, String city, double latitude, double longitude) {
        this.addr = addr;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddr() {
        return this.addr;
    }

    public String getCity() { return this.city; }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public boolean equals(Object object) {
        if (object instanceof MyLocation) {
            MyLocation location = (MyLocation) object;
            return (this.addr.compareTo(location.getAddr()) == 0 && this.city.compareTo(location.getCity()) == 0 &&
                    this.latitude == location.getLatitude() && this.longitude == location.getLongitude());

        }
        return super.equals(object);
    }
}
