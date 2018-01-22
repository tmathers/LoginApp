package com.example.tara.loginapp;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tara.loginapp.helper.GoogleSignInHelper;
import com.example.tara.loginapp.helper.ProfileDataHelper;
import com.example.tara.loginapp.helper.SharedPreferencesHelper;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * The profile page activity
 *
 * @author tmath
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient = null;

    private static final int PICK_IMAGE = 1;

    // Storage keys
    private static final String IMAGE_LOCATION_KEY = "imageLocation";

    private final String LOG_TAG = this.getClass().getSimpleName();

    private TextView cityTextView = null;

    private TextView weatherTextView = null;
    private TextView tempTextView = null;

    // Weather data
    private String temperature = "";
    private String weather = "";
    private String cityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_profile);

        findViewById(R.id.pick_image_button).setOnClickListener(this);

        mGoogleSignInClient = GoogleSignInHelper.getClient(this);

        try {
            // Set the profile image saved on internal storage
            String imagePath = SharedPreferencesHelper.getString(this, IMAGE_LOCATION_KEY);
            Log.i(LOG_TAG, "Getting image from " + imagePath);

            setProfileImage(BitmapFactory.decodeFile(imagePath));

        } catch (Exception e) {
            // Error while creating file
            e.printStackTrace();
        }

        setProfileInfo();

        cityTextView = ((TextView) findViewById(R.id.city_field));
        weatherTextView = ((TextView) findViewById(R.id.weather_field));
        tempTextView = ((TextView) findViewById(R.id.temp_field));


        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            cityTextView.setText("Please enable location permission");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3600, 0, new MyLocationListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_image_button:
                pickImage();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update info in case it was changed
        setProfileInfo();

    }

    private void setProfileInfo() {

        String firstName = ProfileDataHelper.getFirstName(this);
        String lastName= ProfileDataHelper.getLastName(this);

        String nameStr = firstName + " " + lastName;

        ((TextView) findViewById(R.id.headerText)).setText(nameStr);
        ((TextView) findViewById(R.id.emailText)).setText(ProfileDataHelper.getEmail(this));
    }

    /**
     * Signs out and returns to the sign in page.
     */
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                        ProfileActivity.this.finish();
                    }
                });
    }

    /**
     * Opens the Image chooser.
     */
    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Log.i(LOG_TAG, "onActivityResult called. Result code: " + resultCode);
        Log.i(LOG_TAG, "Request code: " + requestCode);

        if (requestCode == PICK_IMAGE && data != null) {

            new MyCopyTask().execute(data.getData());

        }
    }

    private void setProfileImage(Bitmap bitmap) {

        if (bitmap != null) {
            ImageView image = (ImageView) findViewById(R.id.profile_image);
            image.setImageBitmap(bitmap);
        }
    }

    private void setWeatherData() {
        weatherTextView.setText(weather);
        tempTextView.setText(temperature);
        cityTextView.setText(cityName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, ProfileSettingsActivity.class));
                return true;

            case R.id.action_sign_out:
                signOut();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private class MyCopyTask extends AsyncTask<Uri, Integer, Bitmap> {
        ProgressDialog progressDialog;
        @Override
        protected Bitmap doInBackground(Uri... uris) {


            Bitmap bitmap = null;

            try {

                bitmap = MediaStore.Images.Media.getBitmap(ProfileActivity.this.getContentResolver(), uris[0]);

                Bitmap rotatedBitmap = null;
                InputStream in = null;

                // Rotate the bitmap to its Exif oreintation
                try {
                    in = getContentResolver().openInputStream(uris[0]);
                    ExifInterface exifInterface = new ExifInterface(in);

                    int rotation = 0;
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotation = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotation = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotation = 270;
                            break;
                    }

                    Log.i(LOG_TAG, "***** Rotation: " + rotation);

                    Matrix matrix = new Matrix();

                    matrix.postRotate(rotation);

                    rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                } catch (IOException e) {
                    // Handle any errors
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ignored) {}
                    }
                }

                FileOutputStream out = null;
                try {

                    String imagePath = getBaseContext().getFilesDir().getPath().toString() + "/profilePic.png";
                    Log.i(LOG_TAG, "Storing image at " + imagePath);
                    SharedPreferencesHelper.storeString(ProfileActivity.this, IMAGE_LOCATION_KEY,
                            imagePath);
                    out = new FileOutputStream(imagePath);
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    setProfileImage(rotatedBitmap);

                    return rotatedBitmap;

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Hide ProgressDialog here
            progressDialog.dismiss();
        }


        @Override
        protected void onPreExecute() {
            // Show ProgressDialog here

            progressDialog = new ProgressDialog(ProfileActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Saving image...");
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

    }


    /**
     * Private class to get weather data from location
     */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            ((TextView) findViewById(R.id.city_field)).setText("");

            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + loc.getLatitude() +
                    "&lon=" + loc.getLongitude() + "&appid=cd886cb77fae414a345dbd3e0ab142f5";

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                Log.e(LOG_TAG, "Got Weather JSON: " + response.toString());
                                weather = response.getJSONArray("weather").getJSONObject(0).getString("main");
                                Double temp = response.getJSONObject("main").getDouble("temp");
                                temperature = "" + Math.round(temp - 273.15) + " ° C"; // Convert K to C

                                setWeatherData();

                            } catch(JSONException e) {
                                Log.e(LOG_TAG, "Error parsing JSON!");
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Log.e(LOG_TAG, "Error sending request:");
                            error.printStackTrace();

                        }
                    });

            RequestQueue queue = Volley.newRequestQueue(ProfileActivity.this);
            queue.add(jsObjRequest);


        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

}
