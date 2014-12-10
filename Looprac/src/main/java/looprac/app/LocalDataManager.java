package looprac.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by xiaoyong on 14-7-11.
 */
public class LocalDataManager {

    private static SharedPreferences loginDataPre;
    private static SharedPreferences recentSearchPre;

    private static SharedPreferences.Editor loginDataEditor;
    private static SharedPreferences.Editor recentSearchEditor;

    private static ArrayList<MyLocation> recentSearchList;

    /* Must be called within the LAUNCHER_ACTIVITY */
    public static void init(Activity initialActivity) {
        /* Initialize SharedPreferences */
        loginDataPre = initialActivity.getSharedPreferences("userlogin", Context.MODE_PRIVATE);
        recentSearchPre = initialActivity.getSharedPreferences("recentsearch", Context.MODE_PRIVATE);
        loginDataEditor = loginDataPre.edit();
        recentSearchEditor = recentSearchPre.edit();
        recentSearchList = new ArrayList<MyLocation>(); // At most 3 recent searched location
        /* Demo test code */
        for (int i = 1; i <= 3; i++) {
            recentSearchEditor.remove("addr" + i);
            recentSearchEditor.remove("city" + i);
            recentSearchEditor.remove("lati" + i);
            recentSearchEditor.remove("longi" + i);
        }
        recentSearchEditor.commit();
    }

    public static boolean isFirstTimeLogin() {
        return loginDataPre.getBoolean("firsttimelogin", true);
    }

    public static void restoreLoginStatus() {
        loginDataEditor.putBoolean("firsttimelogin", true);
        loginDataEditor.commit();
    }

    public static String getStoredUsrName() {
        return loginDataPre.getString("username", null);
    }

    public static String getStoredPassword() {
        return loginDataPre.getString("password", null);
    }

    public static void storeLoginData(String usrName, String password, boolean isFirstTimeLogin) {
        loginDataEditor.putString("username", usrName);
        loginDataEditor.putString("password", password);
        loginDataEditor.putBoolean("firsttimelogin", isFirstTimeLogin);
        loginDataEditor.commit();
    }

    public static ArrayList<MyLocation> getRecentSearch() {
        recentSearchList.clear();
        String addrKey = "addr";
        String cityKey = "city";
        String latiKey = "lati";
        String longiKey = "longi";
        for (int i = 1; i <= 3; i++) {
            addrKey = "addr";
            cityKey = "city";
            latiKey = "lati";
            longiKey = "longi";
            addrKey = addrKey + i;
            cityKey = cityKey + i;
            latiKey = latiKey + i;
            longiKey = longiKey + i;
            String addr = recentSearchPre.getString(addrKey, null);
            if (addr != null) {
                MyLocation myLocation = new MyLocation(addr, recentSearchPre.getString(cityKey, null), recentSearchPre.getFloat(latiKey, -200), recentSearchPre.getFloat(longiKey, -200));
                recentSearchList.add(myLocation);
            }
        }
        return recentSearchList;
    }

    public static void addRecentHistory(MyLocation newLocation) {
        if (recentSearchList.contains(newLocation)) {
            recentSearchList.remove(newLocation);
        }
        recentSearchList.add(0, newLocation);
        if (recentSearchList.size() >= 3) {
            for (int k = 3; k < recentSearchList.size(); k++) {
                recentSearchList.remove(k);
            }
        }
        int i, j;
        for (i = 1; i <= recentSearchList.size(); i++) {
            recentSearchEditor.putString("addr" + i, recentSearchList.get(i-1).getAddr());
            recentSearchEditor.putString("city" + i, recentSearchList.get(i-1).getCity());
            recentSearchEditor.putFloat("lati" + i, (float) recentSearchList.get(i-1).getLatitude());
            recentSearchEditor.putFloat("longi" + i, (float) recentSearchList.get(i-1).getLongitude());
        }
        for (j = i; j <= 3; j++) {
            recentSearchEditor.remove("addr" + j);
            recentSearchEditor.remove("city" + j);
            recentSearchEditor.remove("lati" + j);
            recentSearchEditor.remove("longi" + j);
        }
        recentSearchEditor.commit();
    }

    public static void deleteRecentHistory(int index) {
        recentSearchList.remove(index);
        index = index + 1;
        recentSearchEditor.remove("addr" + index);
        recentSearchEditor.remove("city" + index);
        recentSearchEditor.remove("lati" + index);
        recentSearchEditor.remove("longi" + index);
        recentSearchEditor.commit();
    }
}
