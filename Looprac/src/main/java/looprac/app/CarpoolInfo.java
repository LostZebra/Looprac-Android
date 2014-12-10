package looprac.app;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/* Sort the list ascendently according to driver ratings */
final class CompRating implements Comparator<CarpoolInfo> {
    public int compare(CarpoolInfo carpoolInfoA, CarpoolInfo carpoolInfoB) {
        if (carpoolInfoA.getDriRating() > carpoolInfoB.getDriRating()) {
            return -1;
        }
        else if (carpoolInfoA.getDriRating() == carpoolInfoB.getDriRating()) {
            return 0;
        }
        else {
            return 1;
        }
    }
}

@SuppressWarnings("unused")
public class CarpoolInfo implements Serializable{

    private String carpoolId;

    private String usrName;
    private float driRating;

    private String deparTime;
    private String unixTime;

    private MyLocation deparLoc;
    private MyLocation destLoc;

    private int capacity;
    private int remaining;

    private int price;
    private double radius;

    public CarpoolInfo (String carpoolId, String usrName, float driRating, String deparTime, MyLocation deparLoc,
                        MyLocation destLoc,  int capacity, int remaining, int price, double radius) {
        this.carpoolId = carpoolId;
        this.usrName = usrName;
        this.driRating = driRating;
        this.deparTime = deparTime;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
            Date date = dateFormat.parse(deparTime);
            this.unixTime = Long.toString(date.getTime() / 1000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.deparLoc = deparLoc;
        this.destLoc = destLoc;
        this.capacity = capacity;
        this.remaining = remaining;
        this.price = price;
        this.radius = radius;
    }

    public CarpoolInfo (String carpoolId, String usrName, float driRating, Long deparTime, MyLocation deparLoc,
                        MyLocation destLoc,  int capacity, int remaining, int price, double radius) {
        this.carpoolId = carpoolId;
        this.usrName = usrName;
        this.driRating = driRating;
        this.unixTime = Long.toString(deparTime * 1000);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(Long.valueOf(this.unixTime));
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH))).append(" ");
        sb.append(String.format("%02d:%02d", date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE)));
        this.deparTime = sb.toString();
        this.deparLoc = deparLoc;
        this.destLoc = destLoc;
        this.capacity = capacity;
        this.remaining = remaining;
        this.price = price;
        this.radius = radius;
    }

    public CarpoolInfo (String usrName, String deparTime, MyLocation deparLoc,
                        MyLocation destLoc) {
        this.carpoolId = "NOT_AVAILABLE";
        this.usrName = usrName;
        this.driRating = -1;
        this.deparTime = deparTime;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
            Date date = dateFormat.parse(deparTime);
            this.unixTime = Long.toString(date.getTime() / 1000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.deparLoc = deparLoc;
        this.destLoc = destLoc;
        this.capacity = this.remaining = capacity;
    }

    /* Get */
    public String getCarpoolId() { return this.carpoolId; }

    public String getUsrName() { return this.usrName; }

    public float getDriRating() { return this.driRating; }

    public String getDeparTime() {
        return this.deparTime;
    }

    public String getUnixTime() { return this.unixTime; }

    public MyLocation getDeparLoc() {
        return this.deparLoc;
    }

    public MyLocation getArrLoc() {
        return this.destLoc;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getRemaining() { return this.remaining; }

    public int getPrice() {
        return this.price;
    }

    public double getRadius() {
        return this.radius;
    }

    /* Set */
    public void setCapacity(int capacity) {
        this.remaining = this.capacity = capacity;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
