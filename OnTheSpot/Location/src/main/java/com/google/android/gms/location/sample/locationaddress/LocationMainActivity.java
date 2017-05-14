/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationaddress;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.email.sendgrid.Util;
import com.github.sendgrid.SendGrid;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.NaturalLanguageClassifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;


/**
 * Getting the Location Address.
 *
 * Demonstrates how to use the {@link android.location.Geocoder} API and reverse geocoding to
 * display a device's location as an address. Uses an IntentService to fetch the location address,
 * and a ResultReceiver to process results sent by the IntentService.
 *
 * Android has two location request settings:
 * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
 * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
 * the AndroidManifest.xml.
 *
 * For a starter example that displays the last known location of a device using a longitude and latitude,
 * see https://github.com/googlesamples/android-play-location/tree/master/BasicLocation.
 *
 * For an example that shows location updates using the Fused Location Provider API, see
 * https://github.com/googlesamples/android-play-location/tree/master/LocationUpdates.
 *
 * This sample uses Google Play services (GoogleApiClient) but does not need to authenticate a user.
 * For an example that uses authentication, see
 * https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart.
 */
public class LocationMainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = "main-activity";

    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     * The user requests an address by pressing the Fetch Address button. This may happen
     * before GoogleApiClient connects. This activity uses this boolean to keep track of the
     * user's intent. If the value is true, the activity tries to fetch the address as soon as
     * GoogleApiClient connects.
     */
    protected boolean mAddressRequested;

    /**
     * The formatted location address.
     */
    protected String mAddressOutput;

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    /**
     * Displays the location address.
     */
    protected TextView mLocationAddressTextView;
    protected TextView mDescriptionTextView;
    protected TextView mObjectTextView;
    protected TextView mUserTextView;
    private String userName;
    private String objectName;
    private String description;
    private String authority;
    private String encodedImage;

    /**
     * Visible while the address is being fetched.
     */
    ProgressBar mProgressBar;

    /**
     * Kicks off the request to fetch an address when pressed.
     */
    //Button mFetchAddressButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.l_main_activity);
        buildGoogleApiClient();
        mResultReceiver = new AddressResultReceiver(new Handler());

        mLocationAddressTextView = (TextView) findViewById(R.id.location_address_view);
        mDescriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        mObjectTextView = (TextView) findViewById(R.id.objectTextView);
        mUserTextView = (TextView) findViewById(R.id.userTextView);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        //mFetchAddressButton = (Button) findViewById(R.id.fetch_address_button);

        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = true;
        mAddressOutput = "";
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        // If GoogleApiClient isn't connected, we process the user's request by setting
        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address
        updateUIWidgets();

        Intent in = getIntent();
        Bundle b = in.getExtras();

        if (b != null) {
            if (b.containsKey("identifiedObject")) {
                objectName = b.get("identifiedObject").toString();
                description = b.get("description").toString();
                // mObjectTextView.setText(objectName);
                // mDescriptionTextView.setText(description);
                userName = b.get("userName").toString();
            //    encodedImage = b.get("encodedImage").toString();

                //mUserTextView.setText(userName);
            }

        }


        updateValuesFromBundle(savedInstanceState);
        //fetchUserIdentity();
        //updateUIWidgets();

    }


    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);

                displayAddressOutput();
            }
        }
    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when user clicks the Fetch Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.

     public void fetchAddressButtonHandler(View view) {
     // We only start the service to fetch the address if GoogleApiClient is connected.
     if (mGoogleApiClient.isConnected() && mLastLocation != null) {
     startIntentService();
     }
     // If GoogleApiClient isn't connected, we process the user's request by setting
     // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
     // fetch the address. As far as the user is concerned, pressing the Fetch Address button
     // immediately kicks off the process of getting the address.
     mAddressRequested = true;
     updateUIWidgets();
     }*/

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                new AsyncTask<String, Void, Classification>() {

                    private Exception exception;

                    protected Classification doInBackground(String... urls) {
                        Classification classification=new Classification();
                        try {
                            NaturalLanguageClassifier service = new NaturalLanguageClassifier();
                            service.setUsernameAndPassword("d1bed693-c368-4316-a7fb-ec3cc5c1deb7", "gq7WidZllcMq");
                            classification = service.classify("90e7b7x198-nlc-52253", objectName+" "+description).execute();
                            System.out.println("aaaa"+classification);

                        } catch (Exception e) {
                            this.exception = e;


                        }
                        return classification;
                    }

                    protected void onPostExecute(Classification classification) {
                        authority=classification.getTopClass();
                        System.out.print("check"+authority);
                        startIntentService();
                    }
                }.execute();



            }
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {

        Intent intent = new Intent(getApplicationContext(), FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        intent.putExtra("username", userName);
        intent.putExtra("object", objectName);
        intent.putExtra("authority",authority);
        intent.putExtra("description", description);
    //    intent.putExtra("encodedImage", encodedImage);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);


    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        //mLocationAddressTextView.setText(mAddressOutput);
        Intent intent=new Intent();
        intent.putExtra("MESSAGE","DONE");
        setResult(RESULT_OK,intent);
        finish();
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private void updateUIWidgets() {
        if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            //    mFetchAddressButton.setEnabled(false);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
            //  mFetchAddressButton.setEnabled(true);
        }
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);

        // Save the address string.
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in LocationMainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Bundle sub=resultData.getBundle("emailDetails");

            sendMessage(sub.getString("userFullName"),sub.getString("userEmail"),
                    sub.getString("object"),sub.getString("description"),sub.getString("authority"),sub.getString("authorityEmail"),
                    sub.getString("image"),sub.getString("address"));

        }

        void getImage(){

            SharedPreferences sharedpreferences = getSharedPreferences("pref",
                    Context.MODE_PRIVATE);


            encodedImage = sharedpreferences.getString("image", "");

        }

        private class SendEmailWithSendGrid extends AsyncTask<Hashtable<String,String>, Void, String> {

            @Override
            protected String doInBackground(Hashtable<String,String>... params) {
                Hashtable<String,String> h = params[0];
                Util creds = new Util();
                SendGrid sendgrid = new SendGrid(creds.getUsername(),creds.getPassword());
                sendgrid.addTo(h.get("to"));
                sendgrid.setFrom(h.get("from"));
                sendgrid.setSubject(h.get("subject"));
                sendgrid.setText(h.get("text"));
                getImage();
                byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

                //create a file to write bitmap data
                File f = new File(getApplicationContext().getCacheDir(), objectName+".jpeg");
                try {
                    f.createNewFile();

//Convert bitmap to byte array

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                    sendgrid.addFile(f);
                }
                catch (Exception e)
                {

                }

                String response = sendgrid.send();
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                mAddressRequested = false;
                displayAddressOutput();

            }
        }


        public void sendMessage(String userFullName,String userEmail,String object,String description,String authority,String authorityEmail,String image,String address) {

            Hashtable<String,String> params = new Hashtable<String,String>();
            String result = null;

            // Get the values from the form
            String to =authorityEmail;
            params.put("to", to);

            String from = userEmail;
            params.put("from", from);

            String subject = "Report a complaint regarding "+object;
            params.put("subject", subject);

            String text = "Dear "+authority+",\n\n"+
                    "I would like to report a complaint regarding "+object+".\n Please find below all the details \n\n"
                    +" DESCRIPTION: "+description+"\n"
                    +" ADDRESS: "+address
                    +" \n\nPlease find attached the site image for your reference.\n"
                    +"Thanks in advance for your help and support!\n\n"
                    +"Best Regards\n\n"+userFullName;
            params.put("text", text);

            /*String imageURI = encodedImage;
            params.put("imageURI", imageURI)*/;


            // Send the Email
            SendEmailWithSendGrid email = new SendEmailWithSendGrid();

            try {
                result = email.execute(params).get();
            } catch (InterruptedException e) {
                // TODO Implement exception handling
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Implement exception handling
                e.printStackTrace();
            }


        }

    }


}

