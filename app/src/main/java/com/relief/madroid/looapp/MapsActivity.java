package com.relief.madroid.looapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ProgressDialog pDialog;
    public static String Package_name;

    // URL to get contacts JSON
   // private static String url = "http://54.152.88.70/cake_pharma/webservices/listView";
    private static String url = "http://52.20.83.178/looapp/webservices/listview";
    // JSON Node names
    private static final String TAG_CONTACTS = "PublicT";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "pharmacy_name";
    private static final String TAG_EMAIL = "owner_name";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_PHONE_MOBILE = "phone";
    private static final String TAG_PHONE_HOME = "home";
    private static final String TAG_PHONE_OFFICE = "office";
    private static final String TAG_latitude = "latitude";
    private static final String TAG_longitude = "longitude";

    public int intenttype;
    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList=new ArrayList<>();

  /*  ArrayList<String> contactList=new ArrayList<>();
    List<List<String>> ls2d = new ArrayList<List<String>>();
*/
    //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    Marker mymark=null,newmark=null;
    double lat,lng;
    String latlng;
    int cnt=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Package_name=getApplicationContext().getPackageName();
        CheckEnableGPS();
        pDialog = new ProgressDialog(MapsActivity.this);
        pDialog.setMessage("Fetching address...");
        pDialog.setCancelable(false);
        pDialog.show();


    }

    @Override
    protected void onResume() {
        super.onResume();
       // Log.d("inte", ">" + intenttype);
        if(intenttype == 1){
            CheckEnableGPS();
        }
         else if(intenttype == 2){
            new Getlist().execute();
        }

        //setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        try {
            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();
                mMap.setMyLocationEnabled(true);

                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    //setUpMap();


                    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                        @Override
                        public void onMyLocationChange(Location arg0) {
                            //Log.d("Not1: ", ">here" );

                            // TODO Auto-generated method stub
                            if (mymark != null) {
                                mymark.remove();
                            }

                            // intenttype=1;
                            mymark = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));

                            lat = arg0.getLatitude();
                            lng = arg0.getLongitude();

                            latlng = lat + "," + lng;


                            if (cnt == 0) {
                                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
                                CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);

                                mMap.moveCamera(center);
                                mMap.animateCamera(zoom);
                                //Toast.makeText(MapsActivity.this, latlng, Toast.LENGTH_SHORT).show();
                                pDialog.cancel();
                                //calling an async task to get json
                                new Getlist().execute();
                                cnt++;

                            }
                        }
                    });


                }
            }
        }catch (Exception e){
            Toast.makeText(MapsActivity.this, "Please Update Google location services", Toast
                    .LENGTH_LONG).show();

        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class Getlist extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            //pDialog.cancel();
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("lat",lat+""));
            nameValuePair.add(new BasicNameValuePair("lng", lng + ""));



            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.POST, nameValuePair);

           // Log.d("Response: ", "> " + jsonStr);
            try{
                byte[] data = Base64.decode(jsonStr, Base64.DEFAULT);
                jsonStr = new String(data, "UTF-8");
            }catch (Exception Err){
                jsonStr=null;
            }

          //  Log.d("Response_decoded: ", "> " + jsonStr);
            pDialog.cancel();

            if (jsonStr != null) {

                try {
                    JSONArray jObj = new JSONArray(jsonStr);
                   // Log.d("Response_ss: ", "> " + jObj);

                    for (int i = 0; i < jObj.length(); i++) {
                        JSONObject c = jObj.getJSONObject(i);
                       // Log.d("Response_ssc: ", "> " + c);
                        for (int j = 0; j < c.length(); j++) {
                            JSONObject d = c.getJSONObject(TAG_CONTACTS);
                            String id = d.getString(TAG_ID);
                            //String id = c.getString(TAG_ID);
                            String address = d.getString(TAG_ADDRESS);
                            //String email = d.getString(TAG_EMAIL);
                            //String mobile = d.getString(TAG_PHONE);
                            String latitude = d.getString(TAG_latitude);
                            String longitude = d.getString(TAG_longitude);

                            HashMap<String, String> contact = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            contact.put(TAG_ID, id);
                            contact.put(TAG_ADDRESS,address);
                            //contact.put(TAG_EMAIL, email);
                            //contact.put(TAG_PHONE, mobile);
                            contact.put(TAG_latitude, latitude);
                            contact.put(TAG_longitude, longitude);

                            // adding contact to contact list
                            contactList.add(contact);


                        }
                    }
                } catch (JSONException e) {
                   // Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
            } else {
               // Log.e("ServiceHandler", "Couldn't get any data from the url");
                return null;


                //Toast.makeText(getApplicationContext(), "Cannot connect!",
                // Toast.LENGTH_SHORT).show();
            }
           // Log.d("Response_ssd: ", "> " + contactList);
            return 1;
        }

        public void onPostExecute(Integer arg){

                if(arg != null){
                   // Log.d("Response_ssdpe: ", "> " + contactList);

                    for (int i = 0; i < contactList.size(); i++) {
                        //Log.d("clis: ", "> " + contactList.get(i));

                        newmark= mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker
                                                (136))
                                .position(new LatLng(Double.parseDouble(contactList.get(i).get("latitude")), Double.parseDouble(contactList.get(i).get("longitude"))))
                                .title(contactList.get(i).get("address"))
                        );


                       // Log.d("hashmps_lats: ", "> " + contactList.get(i).get("latitude"));

                        save("tois", contactList, getApplicationContext());

                    }

                } else{

                    try{

                        String DIRECTORY_ADDRESS = "/Android/data/"+Package_name+"/tois";
                        String filename = DIRECTORY_ADDRESS+"/tois.txt";

                        File toRead = new File(Environment.getExternalStorageDirectory(),filename);

                        if(toRead.exists()) {

                           // Log.d("file1: ", "> " + toRead);
                            // File toRead=new File("fileone");
                            FileInputStream fis = new FileInputStream(toRead);
                            ObjectInputStream ois = new ObjectInputStream(fis);

                            ArrayList<HashMap<String, String>> mapInFile = (ArrayList<HashMap<String, String>>) ois.readObject();

                            ois.close();
                            fis.close();
                            for (int i = 0; i < mapInFile.size(); i++) {

                               // Log.d("fileinfo: ", "> " + mapInFile.get(i));

                                newmark= mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                                        (136))
                                        .position(new LatLng(Double.parseDouble(mapInFile.get(i).get("latitude")), Double.parseDouble(mapInFile.get(i).get("longitude")))).title(mapInFile.get(i).get("address")));


                                //Log.d("filelat: ", "> " + mapInFile.get(i).get("latitude"));

                            }
                            //print All data in MAP
                       /* for (Map.Entry<String, String> m : mapInFile.entrySet()) {
                            Log.d("filekey: ", "> " + m.getKey());
                            Log.d("fileval: ", "> " + m.getValue());

                            //System.out.println(m.getKey()+" : "+m.getValue());
                        }*/
                        }else{

                            if(!isOnline()){
                                Toast.makeText(MapsActivity.this, "Please Enable Internet Connectivity!", Toast.LENGTH_LONG).show();
                                intenttype=2;
                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(intent);
                            }
                            //Log.d("net stat ", ">"+isOnline());
                        }
                    }catch(Exception e){

                       // Log.d("fileerr: ", ">"+e);

                    }
                }
            }


        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }




    }
    private void CheckEnableGPS(){
        String provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.equals("")){
            //GPS Enabled
            /*Toast.makeText(MapsActivity.this, "GPS Enabled: " + provider,
                    Toast.LENGTH_LONG).show();*/
            setUpMapIfNeeded();
        }else{
            intenttype=1;
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }

    public static void save(String filename,  ArrayList<HashMap<String, String>> theObjectAr, Context ctx) {

      String DIRECTORY_ADDRESS = "/Android/data/"+Package_name+"/tois";
        filename = DIRECTORY_ADDRESS+"/tois.txt";

        File cacheDir;
       // Log.d("File",">"+ DIRECTORY_ADDRESS);
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(Environment.getExternalStorageDirectory(),DIRECTORY_ADDRESS);

        else
            cacheDir=ctx.getCacheDir();

        if(!cacheDir.exists()){
            cacheDir.mkdirs();
          //  Log.d("Filed","> Directory created");

        }

        File tempFile = new File(Environment.getExternalStorageDirectory(),filename);



        try {
            if(!tempFile.exists()){

                tempFile.createNewFile();
              //  Log.d("Filed","> File created");
            }



            FileOutputStream fOut = new FileOutputStream(tempFile);
            ObjectOutputStream oos = new ObjectOutputStream(fOut);
            oos.writeObject(theObjectAr);
            oos.close();
            fOut.close();
        }catch (Exception e)
        {
           // Log.d("File exception",">"+ e.toString());
        }
    }



}
