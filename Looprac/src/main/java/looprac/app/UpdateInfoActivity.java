package looprac.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

@SuppressWarnings("all")
public class UpdateInfoActivity extends Activity {

    private EditText userNameET;
    private EditText firstNameET;
    private EditText lastNameET;
    private EditText phoneNumET;
    private EditText carModelET;
    private RadioButton passengerRB;
    private RadioButton driverRB;

    private String usrName;
    private String firstName;
    private String lastName;
    private String phoneNum;
    private int usrType;
    private String carModel;
    private float rating;

    private boolean isTypeChange;

    PersonInfo newPersonInfo;
    PersonInfo oldPersonInfo;

    private CarpoolClient carpoolClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_update_info);

        userNameET = (EditText) findViewById(R.id.usernameEU);
        firstNameET = (EditText) findViewById(R.id.firstnameE);
        lastNameET = (EditText) findViewById(R.id.lastnameE);
        phoneNumET = (EditText) findViewById(R.id.phoneE);
        carModelET = (EditText) findViewById(R.id.carE);
        passengerRB = (RadioButton) findViewById(R.id.passengerE);
        driverRB = (RadioButton) findViewById(R.id.driverE);

        fillOutInfo();

        RadioGroup chooseType = (RadioGroup) findViewById(R.id.choosetypeE);
        chooseType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.driverE) {
                    carModelET.setVisibility(View.VISIBLE);
                    carModelET.setText(carModel);
                    usrType = ConstantVal.DRIVER;
                    isTypeChange = !isTypeChange;
                }
                else {
                    carModelET.setVisibility(View.GONE);
                    usrType = ConstantVal.PASSENGER;
                    isTypeChange = !isTypeChange;
                }
            }
        });

        Button confirmBT = (Button) findViewById(R.id.confirmupdate);
        confirmBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInfo()) {
                    update(firstName + " " + lastName, phoneNum, Integer.toString(usrType), carModel);
                }
            }
        });
    }

    protected boolean checkInfo() {
        usrName = userNameET.getText().toString();
        firstName = firstNameET.getText().toString();
        lastName = lastNameET.getText().toString();
        phoneNum = phoneNumET.getText().toString();

        if (usrType == ConstantVal.DRIVER) {
            carModel = carModelET.getText().toString();
        }

        if (usrName.length() == 0 || phoneNum.length() == 0 || firstName.length() == 0 || lastName.length() == 0 || (usrType == ConstantVal.DRIVER && carModel.length() == 0)) {
            NotificationManager.showToast(getApplicationContext(), "Some fields left empty");
            return false;
        }
        else {
            return true;
        }
    }

    protected void fillOutInfo() {
        oldPersonInfo = (PersonInfo) getIntent().getSerializableExtra("userdata");

        userNameET.setText(oldPersonInfo.getUsrName());
        String name[] = oldPersonInfo.getName().split(" ");
        firstNameET.setText(name[0]);
        lastNameET.setText(name[1]);
        phoneNumET.setText(oldPersonInfo.getPhoneNumber());

        switch (oldPersonInfo.getUsrType()) {
            case ConstantVal.PASSENGER: {
                passengerRB.setChecked(true);
                usrType = ConstantVal.PASSENGER;
                break;
            }
            case ConstantVal.DRIVER: {
                driverRB.setChecked(true);
                usrType = ConstantVal.DRIVER;
                carModel = ((DriverInfo) oldPersonInfo).getCarModel();
                carModelET.setVisibility(View.VISIBLE);
                carModelET.setText(carModel);
                break;
            }
        }
    }

    private void update(String name, String phoneNum, String usrType, String carModel) {
        UpdateTask updateTask = new UpdateTask();
        updateTask.execute(name, phoneNum, usrType, carModel);
    }

    class UpdateTask extends AsyncTask<String, Integer, Boolean> {
        CarpoolClient carpoolClient;
        @Override
        protected void onPreExecute() {
            carpoolClient = CarpoolClient.create();
            setProgressBarIndeterminateVisibility(true);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            boolean updateResult = carpoolClient.update(params[0], params[1], params[2], params[3]);
            if (updateResult) {
                if (usrType == ConstantVal.DRIVER) {
                    if (isTypeChange) {
                        rating = carpoolClient.getDriRating(usrName);
                    } else {
                        rating = ((DriverInfo) oldPersonInfo).getRating();
                    }
                }
            }
            return updateResult;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            if (result) {
                NotificationManager.showToast(getApplicationContext(), "Update succeeds");
                if (usrType == ConstantVal.DRIVER) {
                    newPersonInfo = new DriverInfo(usrName, firstName + " " + lastName, phoneNum, usrType, carModel, rating);
                }
                else {
                    newPersonInfo = new PassengerInfo(usrName, firstName + " " + lastName, phoneNum, usrType);
                }
                Bundle updateData = new Bundle();
                updateData.putSerializable("userdata", newPersonInfo);
                Intent backToPersonInfoIntent = new Intent();
                backToPersonInfoIntent.putExtras(updateData);
                UpdateInfoActivity.this.setResult(ConstantVal.PERSONINFO_UPDATE, backToPersonInfoIntent);
                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.update_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.cancel_update) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
