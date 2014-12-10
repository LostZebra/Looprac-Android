package looprac.app;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by xiaoyong on 14-7-10.
 */
public class NotificationInfo implements Serializable{
    private String notificationId;
    private String carpoolId;
    private String driUsrName;
    private String passUsrName;
    private int notificationType;
    private String lastModificationDate;

    public NotificationInfo(String notificationId, String carpoolId, String driUsrName, String passUsrName,
                            int notificationType, String lastModificationDate) {
        this.notificationId = notificationId;
        this.carpoolId = carpoolId;
        this.driUsrName = driUsrName;
        this.passUsrName = passUsrName;
        this.notificationType = notificationType;
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(Long.valueOf(lastModificationDate) * 1000);
        this.lastModificationDate = date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-" + date.get(Calendar.DAY_OF_MONTH)
                + " " + date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE);
    }

    public String getNotificationId() {
        return this.notificationId;
    }

    public String getCarpoolId() {
        return this.carpoolId;
    }

    public String getDriUsrName() {
        return this.driUsrName;
    }

    public String getPassUsrName() {
        return this.passUsrName;
    }

    public int getNotificationType() {
        return this.notificationType;
    }

    public String getLastModificationDate() {
        return this.lastModificationDate;
    }

}
