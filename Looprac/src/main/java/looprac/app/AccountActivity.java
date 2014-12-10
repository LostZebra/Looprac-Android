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
import android.widget.Toast;

@SuppressWarnings("all")
public class AccountActivity extends Activity{

    private String usrName;
    private String password;

    private boolean firstTimeLogin;

    private EditText usrNameET;
    private EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_account);

        LocalDataManager.init(this); // Initialize local data manager, called only this once

        NotificationManager.init(getApplicationContext(), getIntent()); // Initialize notification service, called only this once

        /* Load UI components */
        usrNameET = (EditText) findViewById(R.id.username);
        passwordET = (EditText) findViewById(R.id.password);
        Button loginBT = (Button) findViewById(R.id.login);
        Button registerBT = (Button) findViewById(R.id.register);

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usrName = usrNameET.getText().toString();
                password = passwordET.getText().toString();
                if (usrName.length() == 0 || password.length() == 0) {
                    Toast emptyInfoToast = Toast.makeText(AccountActivity.this, "Empty username or password!", Toast.LENGTH_SHORT);
                    emptyInfoToast.show();
                }
                else {
                    login(usrName, password);
                }
            }
        });

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usrName = usrNameET.getText().toString();
                password = passwordET.getText().toString();
                Bundle preRegisterInfo = new Bundle();
                if (usrName.length() != 0) {
                    preRegisterInfo.putString("username", usrName);
                }
                if (password.length() != 0) {
                    preRegisterInfo.putString("password", password);
                }
                Intent registerIntent = new Intent(AccountActivity.this, RegisterActivity.class);
                registerIntent.putExtras(preRegisterInfo);
                startActivityForResult(registerIntent, ConstantVal.LOGIN_REGISTER);
            }
        });

        fillOutInfo();

        /* Automatic login if user data already exists */
        firstTimeLogin = LocalDataManager.isFirstTimeLogin();
        if (!firstTimeLogin) {
            login(usrName, password);
        }
    }

    /* Fill out UI content */
    private void fillOutInfo() {
        usrName = LocalDataManager.getStoredUsrName();
        password = LocalDataManager.getStoredPassword();
        if (usrName != null && password != null) {
            usrNameET.setText(usrName);
            passwordET.setText(password);
        }
    }

    /* Call asynctask to login*/
    private void login(String usrName, String password) {
        LoginTask loginTask = new LoginTask();
        loginTask.execute(usrName, password);
    }

    /*　AsyncTask */
    public class LoginTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() { setProgressBarIndeterminateVisibility(true); }
        @Override
        protected Boolean doInBackground(String... params) {
            CarpoolClient carpoolClient = CarpoolClient.create();
            return carpoolClient.loginToService(params[0], params[1]);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            setProgressBarIndeterminateVisibility(false);
            if (result) {
                if (firstTimeLogin) {
                    NotificationManager.showToast(getApplicationContext(), "Login succeeds!");
                    firstTimeLogin = false;
                    LocalDataManager.storeLoginData(usrName, password, firstTimeLogin); // Store login info locally
                    NotificationManager.createNewSegment(usrName); // Create a new segment on Parse
                }
                finish();
                /* Put username in bundle */
                Intent mainMapIntent = new Intent(AccountActivity.this, MainMapActivity.class);
                startActivity(mainMapIntent);
            }
            else {
                NotificationManager.showToast(getApplicationContext(), "Login failed, wrong username or password!");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ConstantVal.LOGIN_REGISTER && resultCode == ConstantVal.LOGIN_REGISTER) {
            Bundle registeredData = intent.getExtras();
            usrName = registeredData.getString("username");
            password = registeredData.getString("password");
            usrNameET.setText(usrName);
            passwordET.setText(password);
            login(usrName, password);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
