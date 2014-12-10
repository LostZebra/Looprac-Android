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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressWarnings("all")
public class PostCarpoolActivity extends Activity {

    private static class CarpoolInfoVH {
        TextView infoLabel;
        TextView infoText;
        Spinner choosePriceSP;
        Spinner chooseCapacitySP;
        Spinner chooseRadiusSP;
    }

    private CarpoolInfo carpoolInfo;

    private BaseAdapter carpoolInfoAdapter;
    private ListView carpoolInfoLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.general_info_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        carpoolInfoLV = (ListView) findViewById(R.id.infolist);

        fillOutInfo();

        carpoolInfoAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (carpoolInfo == null) {
                    return 0;
                }
                return 7;
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
                    carpoolInfoVH.choosePriceSP = (Spinner) convertView.findViewById(R.id.chooseprice);
                    carpoolInfoVH.chooseCapacitySP = (Spinner) convertView.findViewById(R.id.choosecapacity);
                    carpoolInfoVH.chooseRadiusSP = (Spinner) convertView.findViewById(R.id.chooseradius);
                    convertView.setTag(carpoolInfoVH);
                } else {
                    carpoolInfoVH = (CarpoolInfoVH) convertView.getTag();
                }
                carpoolInfoVH.chooseCapacitySP.setVisibility(View.GONE);
                carpoolInfoVH.chooseCapacitySP.setVisibility(View.GONE);
                carpoolInfoVH.chooseRadiusSP.setVisibility(View.GONE);
                switch (position) {
                    case 0: {
                        carpoolInfoVH.infoLabel.setText("User Name");
                        /* Name */
                        carpoolInfoVH.infoText.setText(carpoolInfo.getUsrName());
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
                        carpoolInfoVH.choosePriceSP.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 5: {
                        carpoolInfoVH.infoLabel.setText("Capacity");
                        /* Capacity */
                        carpoolInfoVH.chooseCapacitySP.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 6: {
                        carpoolInfoVH.infoLabel.setText("Radius");
                        /* Capacity */
                        carpoolInfoVH.chooseRadiusSP.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                return convertView;
            }
        };
        carpoolInfoLV.setAdapter(carpoolInfoAdapter);
    }

    public void fillOutInfo() {
        GetCarpoolInfoTask getDriverInfoTask = new GetCarpoolInfoTask();
        getDriverInfoTask.execute();
    }

    /* AsyncTasks */
    public class GetCarpoolInfoTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            carpoolInfo = (CarpoolInfo) getIntent().getSerializableExtra("carpooldata");
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

    public void postCarpool(CarpoolInfo carpoolInfo) {
        PostCarpoolTask postCarpoolTask = new PostCarpoolTask();
        postCarpoolTask.execute(carpoolInfo);
    }

    public class PostCarpoolTask extends AsyncTask<CarpoolInfo, Integer, Boolean> {
        CarpoolClient carpoolClient;
        @Override
        protected void onPreExecute() {
            carpoolClient = CarpoolClient.create();
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Boolean doInBackground(CarpoolInfo... params) {
            return carpoolClient.publishCarpool(params[0]);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            if (result) {
                NotificationManager.showToast(getApplicationContext(), "New carpool post");
                Intent parentIntent = getIntent();
                PostCarpoolActivity.this.setResult(ConstantVal.MAP_POSTCARPOOL, parentIntent);
                PostCarpoolActivity.this.finish();
            }
            else {
                NotificationManager.showToast(getApplicationContext(), "Error posting new carpool");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_carpool, menu);
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
        else if (id == R.id.post) {
            if (carpoolInfo == null) {
                NotificationManager.showAlertDialog(PostCarpoolActivity.this, "Error", "No carpool available to " +
                        "be post", "Got it");
            }
            else {
                Spinner chooseCapacitySP = (Spinner) carpoolInfoLV.getChildAt(5).findViewById(R.id.choosecapacity);
                Spinner choosePriceSP = (Spinner) carpoolInfoLV.getChildAt(4).findViewById(R.id.chooseprice);
                Spinner chooseRadiusSP = (Spinner) carpoolInfoLV.getChildAt(6).findViewById(R.id.chooseradius);
                carpoolInfo.setCapacity(Integer.valueOf(chooseCapacitySP.getSelectedItem().toString()));
                carpoolInfo.setPrice(Integer.valueOf(choosePriceSP.getSelectedItem().toString()));
                carpoolInfo.setRadius(Integer.valueOf(chooseRadiusSP.getSelectedItem().toString()));
                postCarpool(carpoolInfo);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
