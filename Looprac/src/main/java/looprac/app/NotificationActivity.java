package looprac.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class NotificationActivity extends Activity {

    private static class NotificationVH {
        TextView notificationContentTV;
        Button detailInfoBT;
    }

    BaseAdapter notificationListAdapter;

    private int currentNotificationIndex;
    private ArrayList<NotificationInfo> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getNotificationList();

        notificationListAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (notificationList == null) {
                    return 0;
                }
                else if (notificationList.size() == 0) {
                    return 1; // No notification yet
                }
                else {
                    return notificationList.size();
                }
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup viewGroup) {
                NotificationVH notificationVH;
                if (convertView == null) {
                    convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notification_item, null);
                    notificationVH = new NotificationVH();
                    notificationVH.notificationContentTV = (TextView) convertView.findViewById(R.id.notificationcontent);
                    notificationVH.detailInfoBT = (Button) convertView.findViewById(R.id.relateddetail);
                    convertView.setTag(notificationVH);
                }
                else {
                    notificationVH = (NotificationVH) convertView.getTag();
                }

                if (notificationList.size() != 0) {
                    final NotificationInfo currentNotification = notificationList.get(position);
                    final String notificationContent;
                    if (currentNotification.getNotificationType() == 0) {
                        notificationContent = currentNotification.getPassUsrName() + " request to cancel the " +
                                "carpool reservation with you";
                    } else {
                        notificationContent = currentNotification.getPassUsrName() + " request to reserve a " +
                                "carpool with you";
                    }
                    notificationVH.notificationContentTV.setText(notificationContent);
                    if (notificationVH.detailInfoBT.getVisibility() == View.GONE) {
                        notificationVH.detailInfoBT.setVisibility(View.VISIBLE);
                    }
                    notificationVH.detailInfoBT.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            currentNotificationIndex = position; // Mark item index clicked
                            Bundle carpoolData = new Bundle();
                            carpoolData.putSerializable("notificationdata", currentNotification);
                            carpoolData.putInt("prevactivity", ConstantVal.NOTIFICATION_CARPOOLDETAIL);
                            Intent carpoolDetailIntent = new Intent(NotificationActivity.this, CarpoolDetailActivity.class);
                            carpoolDetailIntent.putExtras(carpoolData);
                            startActivityForResult(carpoolDetailIntent, ConstantVal.NOTIFICATION_CARPOOLDETAIL);
                        }
                    });
                }
                else {
                    notificationVH.detailInfoBT.setVisibility(View.GONE);
                    notificationVH.notificationContentTV.setText("No notification yet");
                }
                return convertView;
            }
        };
        ListView notificationLV = (ListView)findViewById(R.id.notificationlist);
        notificationLV.setAdapter(notificationListAdapter);
    }

    public void getNotificationList() {
        GetNotificationTask getNotificationTask = new GetNotificationTask();
        getNotificationTask.execute();
    }

    public class GetNotificationTask extends AsyncTask<String, Integer, Boolean> {
        ProgressBar loadingNotificationPB = (ProgressBar) findViewById(R.id.loadingnotifaction);
        CarpoolClient carpoolClient;
        @Override
        protected void onPreExecute() {
            loadingNotificationPB = (ProgressBar) findViewById(R.id.loadingnotifaction);
            carpoolClient = CarpoolClient.create();
            loadingNotificationPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            notificationList = carpoolClient.getNotificationList();
            if (notificationList != null) {
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            if (result) {
                notificationListAdapter.notifyDataSetChanged();
            }
            loadingNotificationPB.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        System.out.println("Activity result");
        /* When a notification has already been handled */
        if (requestCode == ConstantVal.NOTIFICATION_CARPOOLDETAIL && resultCode == ConstantVal.NOTIFICATION_CARPOOLDETAIL) {
            System.out.println("Delete");
            notificationList.remove(currentNotificationIndex);
            notificationListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        else if (id == R.id.action_refresh_notification) {
            getNotificationList();
        }
        return super.onOptionsItemSelected(item);
    }
}
