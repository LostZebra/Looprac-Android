package looprac.app;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;

public class CarpoolClient {

    private static CarpoolClient carpoolClient;

    /* User tokens */
    public static String usrNameToken;
    public static int usrTypeToken;

    public static CarpoolClient create() {
        if (carpoolClient == null) {
            carpoolClient = new CarpoolClient();
            usrNameToken = null;
            usrTypeToken = ConstantVal.INVALID_USER;
        }
        return carpoolClient;
    }

    public boolean loginToService(String usrName, String password) {
        boolean ifSuccess = false;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/login";
            HttpPost loginRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> loginParams = new ArrayList<NameValuePair>();
            loginParams.add(new BasicNameValuePair("username", usrName));
            loginParams.add(new BasicNameValuePair("password", password));
            loginRequest.setEntity(new UrlEncodedFormEntity(loginParams));

            HttpResponse loginResponse = new DefaultHttpClient().execute(loginRequest);
            JSONObject loginResultJSON = new JSONObject(EntityUtils.toString(loginResponse.getEntity()));

            if (loginResultJSON.getString("success").compareTo("1") == 0) {
                usrNameToken = loginResultJSON.getString("username");
                usrTypeToken = Integer.valueOf(loginResultJSON.getString("type"));
                ifSuccess = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ifSuccess;
    }

    public int register(String usrName, String password, String name, String phone, String usrType, String carModel) {
        int registerResult = -1;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/register";
            HttpPost registerRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> registerParams = new ArrayList<NameValuePair>();
            registerParams.add(new BasicNameValuePair("username", usrName));
            registerParams.add(new BasicNameValuePair("password", password));
            registerParams.add(new BasicNameValuePair("name", name));
            registerParams.add(new BasicNameValuePair("phone", phone));
            registerParams.add(new BasicNameValuePair("type", usrType));
            if (carModel != null) {
                registerParams.add(new BasicNameValuePair("car", carModel));
            }

            registerRequest.setEntity(new UrlEncodedFormEntity(registerParams));

            HttpResponse loginResponse = new DefaultHttpClient().execute(registerRequest);
            JSONObject registerResultJSON = new JSONObject(EntityUtils.toString(loginResponse.getEntity()));
            registerResult = Integer.valueOf(registerResultJSON.getString("success"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return registerResult;
    }

    public boolean update(String name, String phoneNum, String usrType, String carModel) {
        boolean isSuccess = false;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/update/info/" + usrNameToken;
            HttpPost updateRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> updateParams = new ArrayList<NameValuePair>();
            updateParams.add(new BasicNameValuePair("name", name));
            updateParams.add(new BasicNameValuePair("phone", phoneNum));
            updateParams.add(new BasicNameValuePair("type", usrType));
            if (carModel != null) {
                updateParams.add(new BasicNameValuePair("car", carModel));
            }

            updateRequest.setEntity(new UrlEncodedFormEntity(updateParams));

            HttpResponse updateResponse = new DefaultHttpClient().execute(updateRequest);
            JSONObject updateResultJSON = new JSONObject(EntityUtils.toString(updateResponse.getEntity()));
            if (updateResultJSON.getString("success").compareTo("1") == 0) {
                usrTypeToken = Integer.valueOf(usrType) ; // Reset user type token
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    public PersonInfo getUsrInfo(String usrName) {
        PersonInfo personInfo = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/user/" + usrName;
            HttpGet usrInfoRequest = new HttpGet(apiUrl);

            HttpResponse usrInfoResponse = new DefaultHttpClient().execute(usrInfoRequest);
            JSONObject usrInfoResultJSON = new JSONObject(EntityUtils.toString(usrInfoResponse.getEntity()));
            if (usrInfoResultJSON.getString("success").compareTo("1") == 0) {
                int usrType = Integer.valueOf(usrInfoResultJSON.getString("type"));
                if (usrType == ConstantVal.PASSENGER) {
                    personInfo = new PassengerInfo(usrName, usrInfoResultJSON.getString("name"), usrInfoResultJSON.getString("phone"), ConstantVal.PASSENGER);
                }
                else {
                    personInfo = new DriverInfo(usrName, usrInfoResultJSON.getString("name"), usrInfoResultJSON.getString("phone"), ConstantVal.DRIVER, usrInfoResultJSON.getString("car"), getDriRating(usrName));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return personInfo;
    }

    public CarpoolInfo getCarpoolInfo(String carpoolId) {
        CarpoolInfo carpoolInfo = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/carpool";
            HttpGet getCarpoolListRequest = new HttpGet(apiUrl);

            HttpResponse getCarpoolListResponse = new DefaultHttpClient().execute(getCarpoolListRequest);
            JSONObject getCarpoolListResult = new JSONObject(EntityUtils.toString(getCarpoolListResponse.getEntity()));
            if (getCarpoolListResult.getString("success").compareTo("1") == 0) {
                for (int i = 0; i < getCarpoolListResult.length() - 1; i++) {
                    JSONObject carpoolJSON = getCarpoolListResult.getJSONObject(Integer.toString(i));
                        if (carpoolJSON.getString("nbr").compareTo(carpoolId) == 0) {
                            carpoolInfo = new CarpoolInfo(carpoolJSON.getString("nbr"), carpoolJSON.getString("driver"), Float.valueOf(carpoolJSON.getString("rating")), Long.valueOf(carpoolJSON.getString("date")),
                                    new MyLocation(carpoolJSON.getString("pickup"), carpoolJSON.getString("dep_city"), Double.valueOf(carpoolJSON.getString("pick_la")), Double.valueOf(carpoolJSON.getString("pick_lo"))),
                                    new MyLocation(carpoolJSON.getString("dropoff"), carpoolJSON.getString("des_city"), Double.valueOf(carpoolJSON.getString("drop_la")), Double.valueOf(carpoolJSON.getString("drop_lo"))),
                                    Integer.valueOf(carpoolJSON.getString("max_seat")),
                                    Integer.valueOf(carpoolJSON.getString("seat")),
                                    Integer.valueOf(carpoolJSON.getString("price")),
                                    Double.valueOf(carpoolJSON.getString("pickup_radius")));
                        }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return carpoolInfo;
    }

    public boolean rateDriver(String driUsrName, String passUsrName, float rating) {
        boolean isSuccess = false;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/rating";
            HttpPost rateDriverRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> rateDriverParams = new ArrayList<NameValuePair>();
            rateDriverParams.add(new BasicNameValuePair("driver", driUsrName));
            rateDriverParams.add(new BasicNameValuePair("passenger", passUsrName));
            rateDriverParams.add(new BasicNameValuePair("rating", Float.toString(rating)));

            rateDriverRequest.setEntity(new UrlEncodedFormEntity(rateDriverParams));
            HttpResponse rateDriverResponse = new DefaultHttpClient().execute(rateDriverRequest);
            JSONObject rateDriverResultJSON = new JSONObject(EntityUtils.toString(rateDriverResponse.getEntity()));
            if (rateDriverResultJSON.getString("success").compareTo("1") == 0) {
                isSuccess = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    public float getDriRating(String usrName) {
        float rating = (float)0.0;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/rating/" + usrName;
            HttpGet driRatingRequest = new HttpGet(apiUrl);

            HttpResponse driRatingResponse = new DefaultHttpClient().execute(driRatingRequest);
            JSONObject driRatingResultJSON = new JSONObject(EntityUtils.toString(driRatingResponse.getEntity()));
            if (driRatingResultJSON.getString("success").compareTo("1") == 0) {
                rating = Float.valueOf(driRatingResultJSON.getString("rating"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rating;
    }

    public boolean publishCarpool(CarpoolInfo carpoolInfo) {
        boolean isSuccess = false;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/carpool";
            HttpPost publishCarpoolRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> carpoolPostParams = new ArrayList<NameValuePair>();
            carpoolPostParams.add(new BasicNameValuePair("dep_city", carpoolInfo.getDeparLoc().getCity()));
            carpoolPostParams.add(new BasicNameValuePair("des_city", carpoolInfo.getArrLoc().getCity()));

            carpoolPostParams.add(new BasicNameValuePair("pickup", carpoolInfo.getDeparLoc().getAddr()));
            carpoolPostParams.add(new BasicNameValuePair("pick_la", Double.toString(carpoolInfo.getDeparLoc().getLatitude())));
            carpoolPostParams.add(new BasicNameValuePair("pick_lo", Double.toString(carpoolInfo.getDeparLoc().getLongitude())));
            carpoolPostParams.add(new BasicNameValuePair("pickup_radius", Double.toString(carpoolInfo.getRadius())));

            carpoolPostParams.add(new BasicNameValuePair("dropoff", carpoolInfo.getArrLoc().getAddr()));
            carpoolPostParams.add(new BasicNameValuePair("drop_la", Double.toString(carpoolInfo.getArrLoc().getLatitude())));
            carpoolPostParams.add(new BasicNameValuePair("drop_lo", Double.toString(carpoolInfo.getArrLoc().getLongitude())));

            carpoolPostParams.add(new BasicNameValuePair("price", Integer.toString(carpoolInfo.getPrice())));
            carpoolPostParams.add(new BasicNameValuePair("time", carpoolInfo.getUnixTime()));
            carpoolPostParams.add(new BasicNameValuePair("max_seat", Integer.toString(carpoolInfo.getCapacity())));
            carpoolPostParams.add(new BasicNameValuePair("username", carpoolInfo.getUsrName()));

            publishCarpoolRequest.setEntity(new UrlEncodedFormEntity(carpoolPostParams));
            HttpResponse carpoolPostResponse = new DefaultHttpClient().execute(publishCarpoolRequest);
            JSONObject carpoolPostResultJSON = new JSONObject(EntityUtils.toString(carpoolPostResponse.getEntity()));
            if (carpoolPostResultJSON.getString("success").compareTo("1") == 0) {
                isSuccess = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    public ArrayList<CarpoolInfo> getDriCarpoolList(String usrName) {
        ArrayList<CarpoolInfo> carpoolList = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/carpool_history/" + usrName;
            HttpGet getDriCarpoolListRequest = new HttpGet(apiUrl);

            HttpResponse getDriCarpoolListResponse = new DefaultHttpClient().execute(getDriCarpoolListRequest);
            JSONObject getDriCarpoolListResult = new JSONObject(EntityUtils.toString(getDriCarpoolListResponse.getEntity()));
            if (getDriCarpoolListResult.getString("success").compareTo("1") == 0) {
                carpoolList = new ArrayList<CarpoolInfo>();
                for (int i = 0; i < getDriCarpoolListResult.length() - 1; i++) {
                    JSONObject carpoolJSON = getDriCarpoolListResult.getJSONObject(Integer.toString(i));
                    carpoolList.add(new CarpoolInfo(carpoolJSON.getString("nbr"), carpoolJSON.getString("driver"), Float.valueOf(carpoolJSON.getString("rating")), Long.valueOf(carpoolJSON.getString("date")),
                            new MyLocation(carpoolJSON.getString("pickup"), carpoolJSON.getString("dep_city"), Double.valueOf(carpoolJSON.getString("pick_la")), Double.valueOf(carpoolJSON.getString("pick_lo"))),
                            new MyLocation(carpoolJSON.getString("dropoff"), carpoolJSON.getString("des_city"), Double.valueOf(carpoolJSON.getString("drop_la")), Double.valueOf(carpoolJSON.getString("drop_lo"))),
                            Integer.valueOf(carpoolJSON.getString("max_seat")),
                            Integer.valueOf(carpoolJSON.getString("seat")),
                            Integer.valueOf(carpoolJSON.getString("price")),
                            Double.valueOf(carpoolJSON.getString("pickup_radius"))));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return carpoolList;
    }

    public ArrayList<CarpoolInfo> searchByLocation(double latitude, double longitude, int radius) {
        ArrayList<CarpoolInfo> carpoolList = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/search/location/" + latitude + "/" + longitude;
            HttpGet searchByLocationRequest = new HttpGet(apiUrl);

            HttpResponse searchByLocationResponse = new DefaultHttpClient().execute(searchByLocationRequest);
            JSONObject searchByLocationResult = new JSONObject(EntityUtils.toString(searchByLocationResponse.getEntity()));
            if (searchByLocationResult.getString("success").compareTo("1") == 0) {
                System.out.println("Search by location success!");
                carpoolList = new ArrayList<CarpoolInfo>();
                for (int i = 0; i < searchByLocationResult.length() - 1; i++) {
                    JSONObject carpoolJSON = searchByLocationResult.getJSONObject(Integer.toString(i));
                    carpoolList.add(new CarpoolInfo(carpoolJSON.getString("nbr"), carpoolJSON.getString("driver"), Float.valueOf(carpoolJSON.getString("rating")), Long.valueOf(carpoolJSON.getString("date")),
                            new MyLocation(carpoolJSON.getString("pickup"), carpoolJSON.getString("dep_city"), Double.valueOf(carpoolJSON.getString("pick_la")), Double.valueOf(carpoolJSON.getString("pick_lo"))),
                            new MyLocation(carpoolJSON.getString("dropoff"), carpoolJSON.getString("des_city"), Double.valueOf(carpoolJSON.getString("drop_la")), Double.valueOf(carpoolJSON.getString("drop_lo"))),
                            Integer.valueOf(carpoolJSON.getString("max_seat")),
                            Integer.valueOf(carpoolJSON.getString("seat")),
                            Integer.valueOf(carpoolJSON.getString("price")),
                            Double.valueOf(carpoolJSON.getString("pickup_radius"))));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return carpoolList;
    }

    public ArrayList<CarpoolInfo> searchByCity(String deparCity, String arrCity) {
        System.out.println("Departure city is: " + deparCity + " Destination city is: " + arrCity);
        ArrayList<CarpoolInfo> carpoolList = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/search/cities/" + deparCity + "/" + arrCity;
            HttpGet searchByCityRequest = new HttpGet(apiUrl);

            HttpResponse searchByCityResponse = new DefaultHttpClient().execute(searchByCityRequest);
            JSONObject searchByCityResult = new JSONObject(EntityUtils.toString(searchByCityResponse.getEntity()));
            if (searchByCityResult.getString("success").compareTo("1") == 0) {
                carpoolList = new ArrayList<CarpoolInfo>();
                for (int i = 0; i < searchByCityResult.length() - 1; i++) {
                    JSONObject carpoolJSON = searchByCityResult.getJSONObject(Integer.toString(i));
                    carpoolList.add(new CarpoolInfo(carpoolJSON.getString("nbr"), carpoolJSON.getString("driver"), Float.valueOf(carpoolJSON.getString("rating")), Long.valueOf(carpoolJSON.getString("date")),
                            new MyLocation(carpoolJSON.getString("pickup"), carpoolJSON.getString("dep_city"), Double.valueOf(carpoolJSON.getString("pick_la")), Double.valueOf(carpoolJSON.getString("pick_lo"))),
                            new MyLocation(carpoolJSON.getString("dropoff"), carpoolJSON.getString("des_city"), Double.valueOf(carpoolJSON.getString("drop_la")), Double.valueOf(carpoolJSON.getString("drop_lo"))),
                            Integer.valueOf(carpoolJSON.getString("max_seat")),
                            Integer.valueOf(carpoolJSON.getString("seat")),
                            Integer.valueOf(carpoolJSON.getString("price")),
                            Double.valueOf(carpoolJSON.getString("pickup_radius"))));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return carpoolList;
    }

    public ArrayList<CarpoolInfo> searchByCityDates(String departCity, String arrCity, String startTime, String endTime) {
        ArrayList<CarpoolInfo> carpoolList = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/search/date/" + startTime + "/" + endTime + "/" + departCity + "/" + arrCity;
            HttpGet searchByCityDatesRequest = new HttpGet(apiUrl);

            HttpResponse searchByCityDatesResponse = new DefaultHttpClient().execute(searchByCityDatesRequest);
            JSONObject searchByCityDatesResult = new JSONObject(EntityUtils.toString(searchByCityDatesResponse.getEntity()));
            if (searchByCityDatesResult.getString("success").compareTo("1") == 0) {
                carpoolList = new ArrayList<CarpoolInfo>();
                for (int i = 0; i < searchByCityDatesResult.length() - 1; i++) {
                    JSONObject carpoolJSON = searchByCityDatesResult.getJSONObject(Integer.toString(i));
                    carpoolList.add(new CarpoolInfo(carpoolJSON.getString("nbr"), carpoolJSON.getString("driver"), Float.valueOf(carpoolJSON.getString("rating")), Long.valueOf(carpoolJSON.getString("date")),
                            new MyLocation(carpoolJSON.getString("pickup"), carpoolJSON.getString("dep_city"), Double.valueOf(carpoolJSON.getString("pick_la")), Double.valueOf(carpoolJSON.getString("pick_lo"))),
                            new MyLocation(carpoolJSON.getString("dropoff"), carpoolJSON.getString("des_city"), Double.valueOf(carpoolJSON.getString("drop_la")), Double.valueOf(carpoolJSON.getString("drop_lo"))),
                            Integer.valueOf(carpoolJSON.getString("max_seat")),
                            Integer.valueOf(carpoolJSON.getString("seat")),
                            Integer.valueOf(carpoolJSON.getString("price")),
                            Double.valueOf(carpoolJSON.getString("pickup_radius"))));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return carpoolList;
    }

    public ArrayList<NotificationInfo> getNotificationList() {
        ArrayList<NotificationInfo> notificationList = null;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/request/" + CarpoolClient.usrNameToken;
            HttpGet getNotificationListRequest = new HttpGet(apiUrl);

            HttpResponse getNoticationListResponse = new DefaultHttpClient().execute(getNotificationListRequest);
            JSONObject getNotificationListResult = new JSONObject(EntityUtils.toString(getNoticationListResponse.getEntity()));
            if (getNotificationListResult.getString("success").compareTo("1") == 0) {
                notificationList = new ArrayList<NotificationInfo>();
                for (int i = 0; i < getNotificationListResult.length() - 1; i++) {
                    JSONObject notificationJSON = getNotificationListResult.getJSONObject(Integer.toString(i));
                    if (notificationJSON.getString("dealt_state").compareTo("0") == 0) {
                        NotificationInfo newNotificationInfo = new NotificationInfo(notificationJSON.getString("request_nbr"),
                                notificationJSON.getString("carpool_nbr"), notificationJSON.getString("driver"),
                                notificationJSON.getString("passenger"), Integer.valueOf(notificationJSON.getString("change_type")),
                                notificationJSON.getString("change_date"));
                        notificationList.add(newNotificationInfo);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return notificationList;
    }

    public int handleCarpoolRequest(String requestType, String carpoolId, String passUsrName) {
        int requestResult = -1;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/request/" + usrNameToken;
            HttpPost handleRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> handleRequestParams = new ArrayList<NameValuePair>();
            handleRequestParams.add(new BasicNameValuePair("type", requestType));
            handleRequestParams.add(new BasicNameValuePair("carpool_nbr", carpoolId));
            handleRequestParams.add(new BasicNameValuePair("passenger", passUsrName));

            handleRequest.setEntity(new UrlEncodedFormEntity(handleRequestParams));
            HttpResponse handleRequestResponse = new DefaultHttpClient().execute(handleRequest);
            JSONObject handleRequestResult = new JSONObject(EntityUtils.toString(handleRequestResponse.getEntity()));
            requestResult = Integer.valueOf(handleRequestResult.getString("success"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return requestResult;
    }

    public int passengerRequest(String carpoolId) {
        int requestResult = -1;
        try {
            String apiUrl = "http://cs646.twomini.com/server.php/seat/register/" + usrNameToken;
            HttpPost passengerRequest = new HttpPost(apiUrl);

            ArrayList<NameValuePair> passengerRequestParams = new ArrayList<NameValuePair>();
            passengerRequestParams.add(new BasicNameValuePair("carpool_nbr", carpoolId));

            passengerRequest.setEntity(new UrlEncodedFormEntity(passengerRequestParams));
            HttpResponse passengerRequestReponse = new DefaultHttpClient().execute(passengerRequest);
            JSONObject passengerRequestResult = new JSONObject(EntityUtils.toString(passengerRequestReponse.getEntity()));
            requestResult = Integer.valueOf(passengerRequestResult.getString("success"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return requestResult;
    }
}
