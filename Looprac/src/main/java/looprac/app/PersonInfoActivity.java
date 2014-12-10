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
import android.widget.RatingBar;
import android.widget.TextView;

@SuppressWarnings("all")
public class PersonInfoActivity extends Activity {

    public static class PersonInfoVH {
        TextView infoLabel;
        TextView infoText;
        RatingBar ratingBar;
    }

    private String usrName;
    private PersonInfo personInfo;

    private BaseAdapter personInfoAdapter;

    private boolean isTypeUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.general_info_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        usrName = getIntent().getStringExtra("username");

        getPersonInfo();

        personInfoAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (personInfo == null) {
                    return 0;
                }
                else if (personInfo.getUsrType() == ConstantVal.PASSENGER) {
                    return 4;
                }
                else {
                    return 6;
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
            public View getView(int i, View convertView, ViewGroup viewGroup) {
                PersonInfoVH personInfoVH;
                if (convertView == null) {
                    convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.general_info_item, null);
                    personInfoVH = new PersonInfoVH();
                    personInfoVH.infoLabel = (TextView) convertView.findViewById(R.id.infolabel);
                    personInfoVH.infoText = (TextView) convertView.findViewById(R.id.infotext);
                    personInfoVH.ratingBar = (RatingBar) convertView.findViewById(R.id.driverratings);
                    convertView.setTag(personInfoVH);
                }
                else {
                    personInfoVH = (PersonInfoVH) convertView.getTag();
                }
                personInfoVH.ratingBar.setVisibility(View.GONE);
                switch (i) {
                    case 0: {
                        personInfoVH.infoLabel.setText("User Name");
                        personInfoVH.infoText.setText(personInfo.getUsrName());
                        break;
                    }
                    case 1: {
                        personInfoVH.infoLabel.setText("Name");
                        personInfoVH.infoText.setText(personInfo.getName());
                        break;
                    }
                    case 2: {
                        personInfoVH.infoLabel.setText("Phone");
                        personInfoVH.infoText.setText(personInfo.getPhoneNumber());
                        break;
                    }
                    case 3: {
                        personInfoVH.infoLabel.setText("User Type");
                        if (personInfo.getUsrType() == ConstantVal.PASSENGER)
                            personInfoVH.infoText.setText("Passenger");
                        else if (personInfo.getUsrType() == ConstantVal.DRIVER)
                            personInfoVH.infoText.setText("Driver");
                        break;
                    }
                    case 4: {
                        personInfoVH.infoLabel.setText("Car Model");
                        personInfoVH.infoText.setText(((DriverInfo) personInfo).getCarModel());
                        break;
                    }
                    case 5: {
                        personInfoVH.infoLabel.setText("Rating");
                        float driRating = ((DriverInfo)personInfo).getRating();
                        System.out.println("The rating of the driver is: " + driRating); // Test code
                        if (driRating >= 0) {
                            personInfoVH.ratingBar.setVisibility(View.VISIBLE);
                            personInfoVH.ratingBar.setRating(driRating);
                        }
                        else {
                            personInfoVH.infoText.setText("No rating yet");
                        }
                        break;
                    }
                }
                return convertView;
            }
        };
        ListView personInfoList = (ListView) findViewById(R.id.infolist);
        personInfoList.setAdapter(personInfoAdapter);
    }

    protected void getPersonInfo() {
        GetUsrInfoTask getUsrInfoTask = new GetUsrInfoTask();
        getUsrInfoTask.execute(usrName);
    }

    public class GetUsrInfoTask extends AsyncTask<String, Integer, Boolean> {
        CarpoolClient carpoolClient;
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            carpoolClient = CarpoolClient.create();
            personInfo = carpoolClient.getUsrInfo(params[0]);
            if (personInfo != null) {
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
                personInfoAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ConstantVal.PERSONINFO_UPDATE && resultCode == ConstantVal.PERSONINFO_UPDATE) {
            int oldUsrType = personInfo.getUsrType();
            personInfo =  (PersonInfo) intent.getSerializableExtra("userdata");
            if (oldUsrType != personInfo.getUsrType()) {
                isTypeUpdated = true;
            }
            personInfoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getIntExtra("prevactivity", ConstantVal.MAP_PERSONINFO) == ConstantVal.MAP_PERSONINFO) {
            getMenuInflater().inflate(R.menu.person_info, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.edit) {
            if (personInfo != null) {
                Bundle preUpdateData = new Bundle();
                preUpdateData.putSerializable("userdata", personInfo);
                Intent updateIntent = new Intent(PersonInfoActivity.this, UpdateInfoActivity.class);
                updateIntent.putExtras(preUpdateData);
                startActivityForResult(updateIntent, ConstantVal.PERSONINFO_UPDATE);
            }
            else {
                NotificationManager.showAlertDialog(PersonInfoActivity.this, "Error", "No person infor" +
                        "mation available for editing", "Got it");
            }
            return true;
        }
        /* Back to parent UI */
        else if (id == android.R.id.home) {
            if (isTypeUpdated) {
                setResult(ConstantVal.MAP_PERSONINFO, getIntent());
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
