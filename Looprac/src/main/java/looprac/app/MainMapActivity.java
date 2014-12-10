package looprac.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//@SuppressWarnings("all")
public class MainMapActivity extends Activity implements SearchView.OnQueryTextListener, GooglePlayServicesClient.ConnectionCallbacks {

    private Button startTimeBT;
    private Button endtimeBT;
    private Button carpoolListBT;

    private SearchView searchLocSV;
    private ListView recentSearchListLV;
    private ListView searchResultListLV;

    private MapFragment carMapMF;

    private BaseAdapter recentListAdapter;
    private BaseAdapter searchResultListAdapter;

    private String usrName;
    private ArrayList<MyLocation> recentSearchList;
    private ArrayList<String> searchResultList;
    private Map<Marker, CarpoolInfo> markerToCarpool;

    /* Google Map variables */
    private MapClient mapClient;
    private GoogleMap googleMap;
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private LocationListener locationListener;
    private Marker currLocMarker;
    private Marker destLocMarker;
    private Marker deparLocMarker;
    private MyLocation currLoc;
    private MyLocation destLoc;
    private MyLocation deparLoc;
    private boolean firstPin;

    private int searchMode;
    private int currentSearchOption;

    private GetSearchResultTask getSearchResultTask; // Compromised solution

    /* List item container class */
    public static class SearchResultVH {
        TextView searchResultAddressTV;
    }

    public static class RecentResultVH {
        TextView recentSearchTV;
        Button deleteRecentBT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        getActionBar().setDisplayHomeAsUpEnabled(false);

        configGoogleMap(); // Initialize google map configurations

        /* Load UI components */
        startTimeBT = (Button) findViewById(R.id.starttime);
        endtimeBT = (Button) findViewById(R.id.endtime);
        carpoolListBT = (Button) findViewById(R.id.carpoollistbutton);
        searchLocSV = (SearchView) findViewById(R.id.searchdestination);
        recentSearchListLV = (ListView) findViewById(R.id.recentsearchlist);
        searchResultListLV = (ListView) findViewById(R.id.searchresultlist);

        usrName = CarpoolClient.usrNameToken;

        recentSearchList = new ArrayList<MyLocation>(LocalDataManager.getRecentSearch());
        searchResultList = new ArrayList<String>();

        /* UI initialization */
        startTimeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(view);
            }
        });

        int usrMode = CarpoolClient.usrTypeToken;
        setUsrModeUI(usrMode);

        searchLocSV.setSubmitButtonEnabled(true);
        searchLocSV.setOnQueryTextListener(MainMapActivity.this);
        searchLocSV.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchMode();
            }
        });
        searchLocSV.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchResultListLV.setVisibility(View.GONE);
                recentSearchListLV.setVisibility(View.GONE);
                if (carMapMF.getView() != null) {
                    carMapMF.getView().setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        recentListAdapter = new BaseAdapter() {

            @Override
            public int getCount() {
                if (recentSearchList.size() == 0) {
                    return recentSearchList.size() + 1;
                }
                else {
                    return recentSearchList.size();
                }

            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                RecentResultVH recentResultVH;
                if (convertView == null) {
                    convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.recent_search_item, null);
                    recentResultVH = new RecentResultVH();
                    recentResultVH.recentSearchTV = (TextView) convertView.findViewById(R.id.recentsearchaddress);
                    recentResultVH.deleteRecentBT = (Button) convertView.findViewById(R.id.deleterecentsearchaddress);
                    convertView.setTag(recentResultVH);
                }
                else {
                    recentResultVH = (RecentResultVH) convertView.getTag();
                }
                if (recentSearchList.size() == 0) {
                    recentResultVH.recentSearchTV.setText("No recently searched location");
                    recentResultVH.deleteRecentBT.setVisibility(View.GONE);
                }
                else {
                    recentResultVH.recentSearchTV.setText(recentSearchList.get(position).getAddr());
                    if (recentResultVH.deleteRecentBT.getVisibility() == View.GONE)
                        recentResultVH.deleteRecentBT.setVisibility(View.VISIBLE);
                    recentResultVH.recentSearchTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mapMode();
                            addDesAndArrMarker(recentSearchList.get(position), searchMode);
                        }
                    });
                    recentResultVH.deleteRecentBT.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LocalDataManager.deleteRecentHistory(position);
                            recentSearchList = LocalDataManager.getRecentSearch();
                            notifyDataSetChanged();

                        }
                    });
                }
                return convertView;
            }
        };
        recentSearchListLV.setAdapter(recentListAdapter);

        searchResultListAdapter = new BaseAdapter() {

            @Override
            public int getCount() {
                return searchResultList.size();
            }

            @Override
            public Object getItem(int position) { return null; }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                SearchResultVH searchResultVH;
                if (convertView == null) {
                    convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.search_result_item, null);
                    searchResultVH = new SearchResultVH();
                    searchResultVH.searchResultAddressTV = (TextView) convertView.findViewById(R.id.searchresultaddress);
                    convertView.setTag(searchResultVH);
                } else {
                    searchResultVH = (SearchResultVH) convertView.getTag();
                }
                searchResultVH.searchResultAddressTV.setText(searchResultList.get(position));
                searchResultVH.searchResultAddressTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapMode();
                        getLocData(searchResultList.get(position));
                    }
                });
                return convertView;
            }
        };
        searchResultListLV.setAdapter(searchResultListAdapter);

        Button confirmScheduleBT = (Button) findViewById(R.id.confirmschedule);
        confirmScheduleBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CarpoolClient.usrTypeToken == ConstantVal.PASSENGER) {
                    showSearchOptions(); // For passenger to choose search options
                }
                else {
                    preConfirmNewCarpool(); // For driver to check before posting new carpool
                }
            }
        });
    }

    private void configGoogleMap() {
        firstPin = true;
        /* Location service */
        locationClient = new LocationClient(MainMapActivity.this, MainMapActivity.this, null);
        carMapMF = (MapFragment) getFragmentManager().findFragmentById(R.id.maps);
        googleMap = carMapMF.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(false); /* Disable zoom control */

        locationListener = new MyLocationListener();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(300000);
        locationRequest.setFastestInterval(100000);
        locationClient.connect();

        searchMode = ConstantVal.DESTINATION;

        mapClient = MapClient.create(); // Google map api client
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getId().compareTo(currLocMarker.getId()) == 0) {
                    TextView titleTV = new TextView(MainMapActivity.this);
                    titleTV.setText(currLoc.getAddr());
                    titleTV.setLines(2);
                    titleTV.setPadding(30, 20, 10, 20);
                    titleTV.setTextSize(18);
                    String options[] = {"Search based on this address", "Select a new address"};
                    AlertDialog.Builder optionsDialog = new AlertDialog.Builder(MainMapActivity.this)
                            .setCustomTitle(titleTV)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0: {
                                            dialogInterface.dismiss();
                                            deparLoc = currLoc;
                                            deparLocMarker = currLocMarker;
                                            break;
                                        }
                                        case 1: {
                                            dialogInterface.dismiss();
                                            searchMode = ConstantVal.DEPARTURE;
                                            searchMode();
                                            break;
                                        }
                                    }
                                }
                            });
                    optionsDialog.show();
                } else if (markerToCarpool != null){
                    for (Marker carpoolMarker : markerToCarpool.keySet()) {
                        if (carpoolMarker.getId().compareTo(marker.getId()) == 0) {
                            Bundle carpoolData = new Bundle();
                            carpoolData.putSerializable("carpooldata", markerToCarpool.get(carpoolMarker));
                            carpoolData.putInt("prevactivity", ConstantVal.MAP_CARPOOLDETAIL);
                            Intent carpoolRequestIntent = new Intent(MainMapActivity.this, CarpoolDetailActivity.class);
                            carpoolRequestIntent.putExtras(carpoolData);
                            startActivity(carpoolRequestIntent);
                        }
                    }
                }
                return false;
            }
        });
    }

    protected void updateLocation(double latitude, double longitude) {
        if (carMapMF.getView().getVisibility() == View.VISIBLE) {
            getLocDes(latitude, longitude);
            /* Clear previous marker */
            if (currLocMarker != null)
                currLocMarker.remove();
            LatLng currLat = new LatLng(latitude, longitude);
            //LatLng currLat = new LatLng(currLoc.getLatitude(), currLoc.getLongitude());
            currLocMarker = googleMap.addMarker(new MarkerOptions().position(currLat));
            if (firstPin) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLat, 11.5f), 3000, null);
                if (CarpoolClient.usrTypeToken == ConstantVal.DRIVER) {
                    GetCarpoolListTask getCarpoolListTask = new GetCarpoolListTask();
                    getCarpoolListTask.execute(3);
                }
                firstPin = false;
            }
        }
    }

    private void showDatePicker(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(MainMapActivity.this, R.layout.date_picker_dialog, null);
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datepicker);
        final TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timepicker);
        builder.setView(dialogView);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        datePicker.setCalendarViewShown(false);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(Calendar.MINUTE);

        builder.setTitle("Choose time");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d",
                        datePicker.getYear(),
                        datePicker.getMonth() + 1,
                        datePicker.getDayOfMonth())).append("\n");
                sb.append(String.format("%02d:%02d", timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute()));

                if (view.getId() == R.id.starttime) {
                    startTimeBT.setText(sb);
                }
                else {
                    endtimeBT.setText(sb);
                }

                dialog.cancel();
            }
        }).show();
    }

    private void showSearchOptions() {
        if (deparLoc == null || destLoc == null) {
            NotificationManager.showAlertDialog(MainMapActivity.this, "Incomplete info", "Departure location or destination location hasn't " +
                    "been specified, please check again", "Got it");
        }
        else {
            String searchOptions[] = {"Current location",
                    "Departure and arrival location",
                    "Locations and time range"};
            AlertDialog.Builder searchOptionsDialog = new AlertDialog.Builder(this)
                    .setTitle("Search based on")
                    .setItems(searchOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            GetCarpoolListTask getCarpoolListTask = new GetCarpoolListTask();
                            getCarpoolListTask.execute(i);
                        }
                    });
            searchOptionsDialog.show();
        }
    }

    private void preConfirmNewCarpool() {
        String startTimeStr = startTimeBT.getText().toString();
        if (deparLoc == null || destLoc == null ||startTimeStr.compareTo("Start Time") == 0) {
            NotificationManager.showAlertDialog(MainMapActivity.this, "Incomplete info", "Departure location, destination " +
                    "location or start time hasn't been specified, please check again", "Got it");
        }
        else {
            startTimeStr = startTimeStr.replace('\n', ' ');
            Bundle carpoolData = new Bundle();
            CarpoolInfo carpoolInfo = new CarpoolInfo(CarpoolClient.usrNameToken, startTimeStr, deparLoc, destLoc);
            carpoolData.putSerializable("carpooldata", carpoolInfo);
            Intent editCarpoolIntent = new Intent(MainMapActivity.this, PostCarpoolActivity.class);
            editCarpoolIntent.putExtras(carpoolData);
            startActivityForResult(editCarpoolIntent, ConstantVal.MAP_POSTCARPOOL);
        }
    }

    private void addDesAndArrMarker(MyLocation myLocation, int searchMode) {
        if (searchMode == ConstantVal.DESTINATION) {
            destLoc = myLocation;
            LatLng destLat = new LatLng(destLoc.getLatitude(), destLoc.getLongitude());
            if (destLocMarker != null)
                destLocMarker.remove();
            destLocMarker = googleMap.addMarker(new MarkerOptions().position(destLat).alpha((float) 0.8).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLat, 11.5f), 3000, null);
        }
        else if (searchMode == ConstantVal.DEPARTURE){
            deparLoc = myLocation;
            LatLng currLat = new LatLng(deparLoc.getLatitude(), deparLoc.getLongitude());
            if (deparLocMarker != null)
                deparLocMarker.remove();
            deparLocMarker = googleMap.addMarker(new MarkerOptions().position(currLat).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLat, 11.5f), 3000, null);
            this.searchMode = ConstantVal.DESTINATION;
        }

        /* Refresh recently searched history */
        LocalDataManager.addRecentHistory(myLocation);
        recentSearchList = LocalDataManager.getRecentSearch();
        recentListAdapter.notifyDataSetChanged();

    }

    private void addCarpoolMarker(ArrayList<CarpoolInfo> carpoolList) {
        /* Clear previous result */
        if (markerToCarpool != null) {
            for (Marker oldCarpoolmarker : markerToCarpool.keySet()) {
                if (oldCarpoolmarker != null) {
                    oldCarpoolmarker.remove();
                }
            }
            markerToCarpool.clear();
        }
        else {
            markerToCarpool = new HashMap<Marker, CarpoolInfo>();
        }

        for (CarpoolInfo carpoolInfo : carpoolList) {
            MyLocation carpoolLoc = carpoolInfo.getDeparLoc();
            LatLng carpoolLat = new LatLng(carpoolLoc.getLatitude(), carpoolLoc.getLongitude());
            Marker newCarpoolMarker = googleMap.addMarker(new MarkerOptions().position(carpoolLat).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            markerToCarpool.put(newCarpoolMarker, carpoolInfo);
        }
        if (deparLoc != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(deparLoc.getLatitude(), deparLoc.getLongitude()), 11.5f), 3000, null);
        }
        else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), 11.5f), 3000, null);
        }
    }

    private boolean compareTime(String departureTime, String arriveTime) {
        DateFormat standardTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
            Date departureDate = standardTimeFormat.parse(departureTime);
            Date arriveDate = standardTimeFormat.parse(arriveTime);
            if (departureDate.getTime() > arriveDate.getTime()) {
                NotificationManager.showAlertDialog(MainMapActivity.this, "Invalid time", "Departure time can't be later than arrival time!", "Got it");
                return false;
            }
        }
        catch (Exception e) {
            NotificationManager.showToast(getApplicationContext(), "Error getting time info");
            return false;
        }
        return true;
    }

    /* UI mode switch */
    public void searchMode() {
        carMapMF.getView().setVisibility(View.GONE);
        recentSearchList = LocalDataManager.getRecentSearch();
        recentListAdapter.notifyDataSetChanged();
        recentSearchListLV.setVisibility(View.VISIBLE);
    }

    public void mapMode() {
        searchLocSV.setIconified(true);
        searchResultListLV.setVisibility(View.GONE);
        recentSearchListLV.setVisibility(View.GONE);
        carMapMF.getView().setVisibility(View.VISIBLE);
    }

    public void setUsrModeUI(int usrMode) {
        if (destLocMarker != null)
            destLocMarker.remove();
        if (deparLocMarker != null && CarpoolClient.usrTypeToken == ConstantVal.DRIVER)
            deparLocMarker.remove();
        if (markerToCarpool != null && markerToCarpool.size() != 0) {
            for (Marker marker : markerToCarpool.keySet()) {
                if (marker != null)
                    marker.remove();
            }
        }

        if (usrMode == ConstantVal.DRIVER) {
            endtimeBT.setVisibility(View.GONE);
            carpoolListBT.setVisibility(View.GONE);
        }
        else {
            endtimeBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePicker(view);
                }
            });
            carpoolListBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (markerToCarpool == null) {
                        NotificationManager.showAlertDialog(MainMapActivity.this, "Error", "You haven't make any search or " +
                                "Looprac have encountered an error requesting carpool list", "Got it");
                    }
                    else if (markerToCarpool.keySet().size() == 0) {
                        NotificationManager.showToast(getApplicationContext(), "No available carpool");
                    }
                    else {
                        /* Go to carpool list */
                        ArrayList<CarpoolInfo> tempCarpoolList = new ArrayList<CarpoolInfo>();
                        for (Marker marker : markerToCarpool.keySet()) {
                            tempCarpoolList.add(markerToCarpool.get(marker));
                        }
                        SerializableList tempList = new SerializableList();
                        tempList.setList(tempCarpoolList);
                        Bundle carpoolListData = new Bundle();
                        carpoolListData.putSerializable("carpoollistdata", tempList);
                        Intent detailInfoIntent = new Intent(MainMapActivity.this, CarpoolListActivity.class);
                        detailInfoIntent.putExtras(carpoolListData);
                        startActivity(detailInfoIntent);
                    }
                }
            });
        }
    }

    private final class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged (Location location) {
            if (location != null)
                updateLocation(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            searchResultListLV.clearTextFilter();
            searchResultListLV.setVisibility(View.GONE);
            recentListAdapter.notifyDataSetChanged();
            recentSearchListLV.setVisibility(View.VISIBLE);
        }
        else {
            getSearchResult(newText);
            searchResultListAdapter.notifyDataSetChanged();
            searchResultListLV.setVisibility(View.VISIBLE);
            recentSearchListLV.setVisibility(View.GONE);

        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String queryText) {
        return true;
    }

    /* Google services callback */
    @Override
    public void onConnected(Bundle dataBundle) {
        locationClient.requestLocationUpdates(locationRequest, locationListener);
    }

    @Override
    public void onDisconnected() {
        NotificationManager.showToast(getApplicationContext(), "Google map services disconnected!");
    }

    /* Using Google Map apis to translate location information */
    protected void getLocDes(final double latitude, final double longitude) {
        GetLocDesTask getLocDesTask = new GetLocDesTask();
        getLocDesTask.execute(latitude, longitude);
    }

    protected void getSearchResult(String queryText) {
        if (getSearchResultTask != null) {
            getSearchResultTask.cancel(true);
            getSearchResultTask = null;
        }
        else {
            getSearchResultTask = new GetSearchResultTask();
            getSearchResultTask.execute(queryText);
        }
    }

    protected void getLocData(String locDes) {
        GetLocDataTask getLocLatTask = new GetLocDataTask();
        getLocLatTask.execute(locDes);
    }

    /*　AsyncTask */
    public class GetLocDesTask extends AsyncTask<Double, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Double... params) {
            currLoc = mapClient.getLocDes(params[0], params[1]);
            return true;
        }
    }

    /*　AsyncTask */
    public class GetSearchResultTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            searchResultList.clear();
            ArrayList<String> returnedResultsList = mapClient.getSearchResultsList(params[0]);
            if (returnedResultsList == null)
                return false;
            searchResultList.addAll(returnedResultsList);
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                searchResultListAdapter.notifyDataSetChanged();
            }
        }
    }

    /*　AsyncTask */
    public class GetLocDataTask extends AsyncTask<String, Integer, Boolean> {
        MyLocation myLocation = null;
        @Override
        protected Boolean doInBackground(String... params) {
            return !((myLocation = mapClient.getLocData(params[0])) == null);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                addDesAndArrMarker(myLocation, searchMode);
            }
        }
    }

    public class GetCarpoolListTask extends AsyncTask<Integer, Integer, ArrayList<CarpoolInfo>> {
        ArrayList<CarpoolInfo> carpoolList;
        @Override
        protected ArrayList<CarpoolInfo> doInBackground(Integer... params) {
            CarpoolClient carpoolClient = CarpoolClient.create();
            switch (params[0]) {
                case 0: {
                    carpoolList = carpoolClient.searchByLocation(deparLoc.getLatitude(), deparLoc.getLongitude(), 10);
                    currentSearchOption = 0;
                    break;
                }
                case 1: {
                    carpoolList = carpoolClient.searchByCity(deparLoc.getCity(), destLoc.getCity());
                    currentSearchOption = 1;
                    break;
                }
                case 2: {
                    String startTimeStr = startTimeBT.getText().toString();
                    String endTimeStr = endtimeBT.getText().toString();
                    if (endTimeStr.compareTo("End Time") != 0 && startTimeStr.compareTo("Start Time") != 0) {
                        startTimeStr = startTimeStr.replace('\n', ' ');
                        endTimeStr = endTimeStr.replace('\n', ' ');
                        if (compareTime(startTimeStr, endTimeStr)) {
                            carpoolList = carpoolClient.searchByCityDates(deparLoc.getCity(), deparLoc.getCity(), startTimeStr, endTimeStr);
                        }
                    }
                    currentSearchOption = 2;
                    break;
                }
                case 3: {
                    carpoolList = carpoolClient.getDriCarpoolList(CarpoolClient.usrNameToken);
                    break;
                }
            }
            return carpoolList;
        }
        @Override
        protected void onPostExecute(ArrayList<CarpoolInfo> carpoolList) {
            if (carpoolList != null) {
                if (carpoolList.size() == 0) {
                    NotificationManager.showToast(getApplicationContext(), "No available carpool yet");
                }
                else {
                    addCarpoolMarker(carpoolList);
                }
            }
            else {
                NotificationManager.showToast(getApplicationContext(), "Error requesting carpool list");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_map, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ConstantVal.MAP_PERSONINFO && resultCode == ConstantVal.MAP_PERSONINFO) {
            setUsrModeUI(CarpoolClient.usrTypeToken);
        }
        else if(requestCode == ConstantVal.MAP_POSTCARPOOL && resultCode == ConstantVal.MAP_POSTCARPOOL) {
            if (markerToCarpool != null && markerToCarpool.keySet().size() > 0) {
                for (Marker oldCarpoolmarker : markerToCarpool.keySet()) {
                    if (oldCarpoolmarker != null) {
                        oldCarpoolmarker.remove();
                    }
                }
                markerToCarpool.clear();
            }
            if (deparLocMarker != currLocMarker && deparLocMarker != null) {
                deparLocMarker.remove();
            }
            if (destLocMarker != null) {
                destLocMarker.remove();
            }
            GetCarpoolListTask getCarpoolListTask = new GetCarpoolListTask();
            getCarpoolListTask.execute(3);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_person_info: {
                Bundle usrData = new Bundle();
                usrData.putString("username", usrName);
                usrData.putInt("prevactivity", ConstantVal.MAP_PERSONINFO);
                Intent personInfoIntent = new Intent(MainMapActivity.this, PersonInfoActivity.class);
                personInfoIntent.putExtras(usrData);
                startActivityForResult(personInfoIntent, ConstantVal.MAP_PERSONINFO);
                return true;
            }
            case R.id.action_notification: {
                Intent notificationIntent = new Intent(MainMapActivity.this, NotificationActivity.class);
                startActivity(notificationIntent);
                return true;
            }
            case R.id.action_settings: {
                System.out.println("Settings");
                return true;
            }
            case R.id.logout: {
                /* Clear map memory */
                locationClient.disconnect();
                if (currLocMarker != null)
                    currLocMarker.remove();
                if (deparLocMarker != null)
                    deparLocMarker.remove();
                if (destLocMarker != null)
                    destLocMarker.remove();
                googleMap.clear();

                LocalDataManager.restoreLoginStatus(); // Restore login status

                finish();
                Intent backToLoginIntent = new Intent(MainMapActivity.this, AccountActivity.class);
                startActivity(backToLoginIntent);
                return true;
            }
            case R.id.action_refresh_carpool: {
                if (CarpoolClient.usrTypeToken == ConstantVal.DRIVER) {
                    GetCarpoolListTask getCarpoolListTask = new GetCarpoolListTask();
                    getCarpoolListTask.execute(3);
                }
                else {
                    GetCarpoolListTask getCarpoolListTask = new GetCarpoolListTask();
                    getCarpoolListTask.execute(currentSearchOption);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
