package looprac.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.PushService;

import org.json.JSONObject;

/**
 * Created by xiaoyong on 14-7-11.
 */
public class NotificationManager {
    public static void init(Context appContext, Intent appIntent) {
        Parse.initialize(appContext, ConstantVal.PARSE_APPID, ConstantVal.PARSE_APPKEY);
        ParseAnalytics.trackAppOpened(appIntent);
        PushService.setDefaultPushCallback(appContext, NotificationActivity.class);
    }

    public static void createNewSegment(String usrName) {
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("user", usrName);
        parseInstallation.saveInBackground();
    }

    public static void sendNotification(String alert, String title, String targetUsr) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", targetUsr);
        try {
            JSONObject receiverIntentData = new JSONObject();
            receiverIntentData.put("alert", alert);
            receiverIntentData.put("title", title);

            ParsePush push = new ParsePush();
            push.setQuery(pushQuery);
            push.setData(receiverIntentData);
            push.sendInBackground();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(Context appContext, String content) {
        Toast infoToast = Toast.makeText(appContext, content, Toast.LENGTH_SHORT);
        infoToast.show();
    }

    public static void showAlertDialog(Context appContext, String Content, String title, String buttonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle(title);
        builder.setMessage(Content);
        builder.setNegativeButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
