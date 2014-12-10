package looprac.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

@SuppressWarnings("all")
public class RegisterActivity extends Activity {

    private EditText userNameET;
    private EditText passwordET;
    private EditText repasswordET;
    private EditText firstNameET;
    private EditText lastNameET;
    private EditText phoneNumET;
    private EditText carModelET;

    private String usrName;
    private String password;
    private String repassword;
    private int usrType;
    private String phoneNum;
    private String firstName;
    private String lastName;
    private String carModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_register);

        usrType = ConstantVal.PASSENGER;

        userNameET = (EditText) findViewById(R.id.usernameR);
        passwordET = (EditText) findViewById(R.id.passwordR);
        repasswordET = (EditText) findViewById(R.id.repassword);
        firstNameET = (EditText) findViewById(R.id.firstname);
        lastNameET = (EditText) findViewById(R.id.lastname);
        phoneNumET = (EditText) findViewById(R.id.phone);
        carModelET = (EditText) findViewById(R.id.car);

        RadioGroup chooseType = (RadioGroup) findViewById(R.id.choosetype);
        chooseType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.driver) {
                    carModelET.setVisibility(View.VISIBLE);
                    usrType = ConstantVal.DRIVER;
                }
                else {
                    carModelET.setVisibility(View.INVISIBLE);
                    usrType = ConstantVal.PASSENGER;
                }
            }
        });

        usrName = getIntent().getStringExtra("username");
        if (usrName != null)
            userNameET.setText(usrName);
        password = getIntent().getStringExtra("password");
        if (password != null)
            passwordET.setText(password);

        Button confirmBT = (Button) findViewById(R.id.confirmregister);
        confirmBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInfo()) {
                    register(usrName, password, firstName + " " + lastName, phoneNum,
                            Integer.toString(usrType), carModel);
                }
            }
        });
    }

    public boolean checkInfo() {
        usrName = userNameET.getText().toString();
        password = passwordET.getText().toString();
        repassword = repasswordET.getText().toString();
        phoneNum = phoneNumET.getText().toString();
        firstName = firstNameET.getText().toString();
        lastName = lastNameET.getText().toString();

        if (usrType == ConstantVal.DRIVER) {
            carModel = carModelET.getText().toString();
        }

        if (usrName.length() == 0 || password.length() == 0 || repassword.length() == 0 || phoneNum.length() == 0 || firstName.length() == 0 || lastName.length() == 0 || (usrType == ConstantVal.DRIVER && carModel.length() == 0)) {
            NotificationManager.showToast(getApplicationContext(), "Some fields left empty");
            return false;
        }
        else if (password.compareTo(repassword) != 0) {
            NotificationManager.showToast(getApplicationContext(), "Two passwords are not equal");
            return false;
        }
        return true;
    }

    public void register(String usrName, String password, String name, String phoneNum, String usrType, String carModel) {
        RegisterTask registerTask = new RegisterTask();
        registerTask.execute(usrName, password, name, phoneNum, usrType, carModel);
    }

    public class RegisterTask extends AsyncTask<String, Integer, Integer> {
        CarpoolClient carpoolClient;
        @Override
        protected void onPreExecute() {
            carpoolClient = CarpoolClient.create();
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            return carpoolClient.register(params[0], params[1], params[2], params[3], params[4], params[5]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case 0: {
                    NotificationManager.showAlertDialog(RegisterActivity.this, "Error", "Username already exists, please use another one", "Got it");
                    break;
                }
                case 2: {
                    NotificationManager.showAlertDialog(RegisterActivity.this, "Error", "Looprac server error, please try again later", "Got it");
                    break;
                }
                case 1: {
                    NotificationManager.showToast(getApplicationContext(), "Register succeeds");

                    /* Back to AccountActivity and precedes with login */
                    Bundle registeredData = new Bundle();
                    registeredData.putString("username", usrName);
                    registeredData.putString("password", password);
                    Intent backToLoginIntent = getIntent();
                    backToLoginIntent.putExtras(registeredData);
                    RegisterActivity.this.setResult(ConstantVal.LOGIN_REGISTER, backToLoginIntent);
                    RegisterActivity.this.finish();
                    break;
                }
                default: {
                    NotificationManager.showAlertDialog(RegisterActivity.this, "Error", "Unknown error, please try again later"
                            , "Got it");
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.cancel_register) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
