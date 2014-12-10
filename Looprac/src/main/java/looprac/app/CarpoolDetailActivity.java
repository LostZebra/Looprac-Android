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
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

@SuppressWarnings("all")
public class CarpoolDetailActivity extends Activity {

    private CarpoolClient carpoolClient;

    private BaseAdapter carpoolInfoAdapter;

    private CarpoolInfo carpoolInfo;
    private NotificationInfo notificationInfo;

    private int parentActivityIdenfier;
    private Intent parentActivityIntent;

    /* List item container class */
    final static class CarpoolInfoVH {
        TextView infoLabel;
        TextView infoText;
        TextView infoButton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.general_info_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        parentActivityIntent = getIntent();
        parentActivityIdenfier = parentActivityIntent.getIntExtra("prevactivity", ConstantVal.MAP_CARPOOLDETAIL);
        if (parentActivityIdenfier == ConstantVal.NOTIFICATION_CARPOOLDETAIL) {
            notificationInfo = (NotificationInfo) parentActivityIntent.getSerializableExtra("notificationdata");
        }
        else {
            carpoolInfo = (CarpoolInfo) parentActivityIntent.getSerializableExtra("carpooldata");
        }

        carpoolClient = CarpoolClient.create(); // Initialize carpool client

        ListView carpoolInfoList = (ListView) findViewById(R.id.infolist);

        fillOutInfo();

        carpoolInfoAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (carpoolInfo == null)
                    return 0;
                return 6;
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
            public View getView(int position, View convertView, ViewGroup viewGroup) {
                CarpoolInfoVH carpoolInfoVH;
                if (convertView == null) {
                    convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.general_info_item, null);
                    carpoolInfoVH = new CarpoolInfoVH();
                    carpoolInfoVH.infoLabel = (TextView) convertView.findViewById(R.id.infolabel);
                    carpoolInfoVH.infoText = (TextView) convertView.findViewById(R.id.infotext);
                    carpoolInfoVH.infoButton = (Button) convertView.findViewById(R.id.infobutton);
                    convertView.setTag(carpoolInfoVH);
                }
                else {
                    carpoolInfoVH = (CarpoolInfoVH) convertView.getTag();
                }
                carpoolInfoVH.infoButton.setVisibility(View.GONE);
                switch (position) {
                    case 0: {
                        carpoolInfoVH.infoLabel.setText("User Name");
                        /* Name */
                        final String usrName;
                        if (parentActivityIdenfier == ConstantVal.NOTIFICATION_CARPOOLDETAIL) {
                            usrName = notificationInfo.getPassUsrName();
                        }
                        else {
                            usrName = carpoolInfo.getUsrName();
                        }
                        carpoolInfoVH.infoText.setText(usrName);
                        carpoolInfoVH.infoButton.setVisibility(View.VISIBLE);
                        carpoolInfoVH.infoButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                /* Go to PersonInfoActivity */
                                Bundle usrData = new Bundle();
                                usrData.putString("username", usrName);
                                usrData.putInt("prevactivity", ConstantVal.CARPOOLDETAIL_PERSONINFO);
                                Intent personInfoIntent = new Intent(CarpoolDetailActivity.this, PersonInfoActivity.class);
                                personInfoIntent.putExtras(usrData);
                                startActivity(personInfoIntent);
                            }
                        });
                        break;
                    }
                    case 1: {
                        carpoolInfoVH.infoLabel.setText("Departure\nlocation");
                        /* Departure */
                        carpoolInfoVH.infoText.setText(carpoolInfo.getDeparLoc().getAddr());
                        break;
                    }
                    case 2: {
                        carpoolInfoVH.infoLabel.setText("Arrive\nlocation");
                        /* Arrival */
                        carpoolInfoVH.infoText.setText(carpoolInfo.getArrLoc().getAddr());
                        break;
                    }
                    case 3: {
                        carpoolInfoVH.infoLabel.setText("Departure\nTime");
                        /* Time */
                        carpoolInfoVH.infoText.setText(carpoolInfo.getDeparTime());
                        break;
                    }
                    case 4: {
                        carpoolInfoVH.infoLabel.setText("Price");
                        /* Price */
                        String priceStr = String.format("$%d", carpoolInfo.getPrice());
                        carpoolInfoVH.infoText.setText(priceStr);
                        break;
                    }
                    case 5: {
                        carpoolInfoVH.infoLabel.setText("Capacity");
                        /* Capacity */
                        String capacityStr = String.format("%d seats / %d remaining", carpoolInfo.getCapacity(), carpoolInfo.getRemaining());
                        carpoolInfoVH.infoText.setText(capacityStr);
                        break;
                    }
                }
                return convertView;
            }
        };
        carpoolInfoList.setAdapter(carpoolInfoAdapter);
    }

    private void fillOutInfo() {
        GetCarpoolInfoTask getCarpoolInfoTask = new GetCarpoolInfoTask();
        getCarpoolInfoTask.execute();
    }

    private void requestCarpool(String carpoolId) {
        if (carpoolInfo != null) {
            SendRequestTask sendRequestTask = new SendRequestTask();
            sendRequestTask.execute(carpoolId);
        }
        else {
            NotificationManager.showToast(getApplicationContext(), "No carpool information available");
        }
    }

    private void handleRequest(String requestType, String carpoolId, String passUsrName) {
        if (carpoolInfo != null) {
            RequestResponseTask requestResponseTask = new RequestResponseTask();
            requestResponseTask.execute(requestType, carpoolId, passUsrName);
        }
        else {
            NotificationManager.showToast(getApplicationContext(), "No carpool information available");
        }
    }

    /* AsyncTasks */
    public class GetCarpoolInfoTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            if (parentActivityIdenfier == ConstantVal.NOTIFICATION_CARPOOLDETAIL) {
                carpoolInfo = carpoolClient.getCarpoolInfo(notificationInfo.getCarpoolId());
            }
            if (carpoolInfo != null) {
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
                carpoolInfoAdapter.notifyDataSetChanged();
            }
        }
    }

    public class SendRequestTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Integer doInBackground(String... params) {
            int requestResult = -1;
            requestResult = carpoolClient.passengerRequest(params[0]);
            return requestResult;
        }
        @Override
        protected void onPostExecute(Integer result) {
            setProgressBarIndeterminateVisibility(false);
            switch (result) {
                case -1: {
                    NotificationManager.showToast(getApplicationContext(), "Error sending request");
                    break;
                }
                case 0: {
                    NotificationManager.showToast(getApplicationContext(), "Server error, please try later");
                    break;
                }
                case 1: {
                    NotificationManager.showToast(getApplicationContext(), "Request sent successfully");
                    NotificationManager.sendNotification("Looprac", String.format("%s requests to join your carpool", CarpoolClient.usrNameToken), carpoolInfo.getUsrName());
                    finish(); // Back to parent activity
                    break;
                }
                case 2: {
                    NotificationManager.showToast(getApplicationContext(), "You have already reserved");
                    break;
                }
            }
        }
    }

    public class RequestResponseTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Integer doInBackground(String... params) {
            int requestResult = -1;
            requestResult = carpoolClient.handleCarpoolRequest(params[0], params[1], params[2]);
            return requestResult;
        }
        @Override
        protected void onPostExecute(Integer result) {
            setProgressBarIndeterminate(false);
            switch (result) {
                case -1: {
                    NotificationManager.showToast(getApplicationContext(), "Error handling request");
                    break;
                }
                case 0: {
                    NotificationManager.showToast(getApplicationContext(), "Server error, please try later");
                    break;
                }
                case 1: {
                    NotificationManager.showToast(getApplicationContext(), "Request handled successfully");
                    finish(); // Back to parent activity
                    break;
                }
                case 2: {
                    NotificationManager.showToast(getApplicationContext(), "No remaining seat, confirmaion failed");
                    break;
                }
                case 3: {
                    NotificationManager.showToast(getApplicationContext(), "Invalid request, please check again");
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /* Judge on what parent view is and inflate corresponding layout */
        parentActivityIdenfier = parentActivityIntent.getIntExtra("prevactivity", ConstantVal.MAP_CARPOOLDETAIL);
        if (CarpoolClient.usrTypeToken == ConstantVal.PASSENGER && (parentActivityIdenfier == ConstantVal.MAP_CARPOOLDETAIL || parentActivityIdenfier == ConstantVal.CARPOOLLIST_CARPOOLDETAIL)) {
            getMenuInflater().inflate(R.menu.carpool_request, menu);
        } else if (CarpoolClient.usrTypeToken == ConstantVal.DRIVER && parentActivityIdenfier == ConstantVal.NOTIFICATION_CARPOOLDETAIL) {
            getMenuInflater().inflate(R.menu.carpool_confirm, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
            /* These three menu items only apply to the situation when you are a passenger */
            case R.id.sendrequest: {
                if (carpoolInfo != null) {
                    requestCarpool(carpoolInfo.getCarpoolId());
                }
                else {
                    NotificationManager.showToast(getApplicationContext(), "No carpool information available");
                }
                return true;
            }
            case R.id.call: {
                return true;
            }
            case R.id.text: {
                return true;
            }
            /* These two menu items apply only to the situation when you are handling the
             * notification. Besides, here we leaves the functionality that passenger can
             * cancel the reservation */
            case R.id.confirmrequest: {
                if (carpoolInfo != null) {
                    handleRequest(Integer.toString(2), notificationInfo.getCarpoolId(), notificationInfo.getPassUsrName()); // Type is 2
                    NotificationManager.sendNotification("Looprac", String.format("%s have confirmed your request", CarpoolClient.usrNameToken), notificationInfo.getPassUsrName());
                    CarpoolDetailActivity.this.setResult(parentActivityIdenfier, parentActivityIntent);
                    CarpoolDetailActivity.this.finish();
                }
                else {
                    NotificationManager.showToast(getApplicationContext(), "No carpool information available");
                }
                return true;
            }
            case R.id.cancelrequest: {
                if (carpoolInfo != null) {
                    handleRequest(Integer.toString(1), notificationInfo.getCarpoolId(), notificationInfo.getPassUsrName()); // Type is 1
                    CarpoolDetailActivity.this.setResult(parentActivityIdenfier, parentActivityIntent);
                    CarpoolDetailActivity.this.finish();
                }
                else {
                    NotificationManager.showToast(getApplicationContext(), "No carpool information available");
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
