package com.google.android.gms.location.sample.locationaddress;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.dao.Authority;
import com.firebase.dao.Complaint;
import com.firebase.dao.ComplaintAddress;
import com.firebase.dao.Person;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
public class FetchAddressIntentService extends IntentService {
    private static final String TAG = "FetchAddressIS";

    private SharedPreferences sharedpreferences;
    private String userFullName;
    private String userEmail;
    private String userName;
    private String object;
    private String description;
    private String address;
    private String authority;
    private String authorityEmail;
    private String encodedImage;

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link android.os.ResultReceiver} in * LocationMainActivity to process content
     * sent from this service.
     *
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            return;
        }

         userName = intent.getStringExtra("username");
         object = intent.getStringExtra("object");
         authority = intent.getStringExtra("authority");
        description = intent.getStringExtra("description");
      //  encodedImage = intent.getStringExtra("encodedImage");

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            //registerComplaint();
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.address_found));
            saveAuthority();
            getImage();
            addComplaint(userName, description, object, address,authority, encodedImage);


            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        address=message;
        Bundle subBundle=new Bundle();
        subBundle.putString("userFullName",userFullName);
        subBundle.putString("userEmail",userEmail);
        subBundle.putString("object",object);
        subBundle.putString("description",description);
        subBundle.putString("authority",authority);
        subBundle.putString("authorityEmail",authorityEmail);
        subBundle.putString("image",encodedImage);
        subBundle.putString("address",address);

        bundle.putString(Constants.RESULT_DATA_KEY, message);
        bundle.putBundle("emailDetails",subBundle);
        mReceiver.send(resultCode, bundle);
    }

    private void addComplaint(String userName, String description, String object, Address address,String authority, String encodedImage){


        ComplaintAddress complaintAddress = new ComplaintAddress();

        complaintAddress.setCity(address.getLocality());
        complaintAddress.setCountry(address.getCountryName());
        complaintAddress.setState(address.getAdminArea());
        complaintAddress.setStreet(address.getSubThoroughfare()+" "+address.getThoroughfare());
        complaintAddress.setZip(address.getPostalCode());

        Complaint complaint = new Complaint();


        complaint.setComplaintDate(new Date());
        complaint.setComplaintLocation(complaintAddress);
        complaint.setComplaintImage(encodedImage);
        complaint.setDescription(description);
        complaint.setComplaintObject(object);

        sharedpreferences = getSharedPreferences("pref",
                Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String json1 = sharedpreferences.getString(userName, "");
        String json2 = sharedpreferences.getString("authorityList", "");


        Person obj = gson.fromJson(json1, Person.class);
        userFullName= obj.getFullName();
        userEmail=obj.getEmail();

        Type type = new TypeToken<List<Authority>>(){}.getType();


        List<Authority> authorityList = gson.fromJson(json2, type);

        for (Authority auth:authorityList){

            if (Arrays.asList(auth.getZipHandled()).contains(address.getPostalCode())
                    && auth.getName().equals(authority)) {
                complaint.setAuthorityName(auth.getName()+" "+address.getLocality());
                //authority=auth.getName();
                authorityEmail=auth.getEmail();
                System.out.println("authotrity name" + auth.getName());
            }
        }

        List<Complaint> complaintList = new ArrayList<Complaint>();

        complaintList = obj.getComplaints();

        complaintList.add(complaint);

        //person.setComplaints(complaintList);

        SharedPreferences.Editor editor = sharedpreferences.edit();

        String json = gson.toJson(obj); // myObject - instance of MyObject
        editor.putString(userName, json);
        editor.commit();




    }

    void getImage(){

        sharedpreferences = getSharedPreferences("pref",
                Context.MODE_PRIVATE);


        encodedImage = sharedpreferences.getString("image", "");

    }

    void saveAuthority(){

        List<Authority> authorityList = new ArrayList<Authority>();
        Authority authority1 = new Authority();
        String zipArray1[] = {"95112", "95113", "95114"};
        authority1.setEmail("kimtani90@gmail.com");
        authority1.setContact("+14088861711");
        authority1.setName("Department of Transportation");
        authority1.setZipHandled(zipArray1);

        Authority authority2 = new Authority();
        String zipArray2[] = {"95112", "95113", "95114"};
        authority2.setEmail("kimtani90@gmail.com");
        authority2.setContact("+14088861711");
        authority2.setName("Animal Care and Services");
        authority2.setZipHandled(zipArray2);

        Authority authority3 = new Authority();
        String zipArray3[] = {"95112", "95113", "95114"};
        authority3.setEmail("kimtani90@gmail.com");
        authority3.setContact("+14088861711");
        authority3.setName("Environmental Services");
        authority3.setZipHandled(zipArray3);

        Authority authority4 = new Authority();
        String zipArray4[] = {"95112", "95113", "95114"};
        authority4.setEmail("kimtani90@gmail.com");
        authority4.setContact("+14088861711");
        authority4.setName("Public Works Department");
        authority4.setZipHandled(zipArray4);

        Authority authority5 = new Authority();
        String zipArray5[] = {"95112", "95113", "95114"};
        authority5.setEmail("arsh291991@gmail.com");
        authority5.setContact("+14088861711");
        authority5.setName("Ambulance Service");
        authority5.setZipHandled(zipArray5);

        Authority authority6 = new Authority();
        String zipArray6[] = {"95112", "95113", "95114"};
        authority6.setEmail("kimtani90@gmail.com");
        authority6.setContact("+14088861711");
        authority6.setName("Code Enforcement Services Dept\n");
        authority6.setZipHandled(zipArray6);

        Authority authority7 = new Authority();
        String zipArray7[] = {"95112", "95113", "95114"};
        authority7.setEmail("kimtani90@gmail.com");
        authority7.setContact("+14088861711");
        authority7.setName("Fire Department");
        authority7.setZipHandled(zipArray7);

        Authority authority8 = new Authority();
        String zipArray8[] = {"95112", "95113", "95114"};
        authority8.setEmail("kimtani90@gmail.com");
        authority8.setContact("+14088861711");
        authority8.setName("Library");
        authority8.setZipHandled(zipArray8);

        Authority authority9 = new Authority();
        String zipArray9[] = {"95112", "95113", "95114"};
        authority9.setEmail("kimtani90@gmail.com");
        authority9.setContact("+14088861711");
        authority9.setName("Police Department");
        authority9.setZipHandled(zipArray9);

        Authority authority0 = new Authority();
        String zipArray0[] = {"95112", "95113", "95114"};
        authority0.setEmail("kimtani90@gmail.com");
        authority0.setContact("+14088861711");
        authority0.setName("Parks, Recreation & Neighborhood Services");
        authority0.setZipHandled(zipArray0);

        authorityList.add(authority0);
        authorityList.add(authority1);
        authorityList.add(authority2);
        authorityList.add(authority3);
        authorityList.add(authority4);
        authorityList.add(authority5);
        authorityList.add(authority6);
        authorityList.add(authority7);
        authorityList.add(authority8);
        authorityList.add(authority9);

        sharedpreferences = getSharedPreferences("pref",
                Context.MODE_PRIVATE);

        Gson gson = new Gson();

        SharedPreferences.Editor editor = sharedpreferences.edit();

        String json = gson.toJson(authorityList); // myObject - instance of MyObject
        editor.putString("authorityList", json);
        editor.commit();


    }

}
