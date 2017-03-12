package touhid.bustracker;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main2Activity extends AppCompatActivity {
    private Connection connection = null;
    private TelephonyManager telephonyManager = null;
    private WifiManager wifiManager = null;
    private LocationManager locationManager = null;
    private AlertDialog.Builder alartBuilder = null;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ConnectivityManager connectionManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        System.out.println("Internet connection is: " + isConnected);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (isConnected != false) {
            databaseConnection();
        } else {
            alartBuilder = new AlertDialog.Builder(this);
            alartBuilder.setMessage("Please Active Your Internet Connection.").setCancelable(false);
            AlertDialog alertDialog = alartBuilder.create();
            alertDialog.setTitle("Alert !!!");
            alertDialog.show();
        }
    }
    private void databaseConnection() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("PostgreSQL Connect Example.");
                    String url = "jdbc:postgresql://192.168.0.100:5432/";
                    String dbName = "busTracking";
                    String driver = "org.postgresql.Driver";
                    String userName = "postgres";
                    String password = "touhid45687";
                    try {
                        Class.forName(driver).newInstance();
                        connection = DriverManager.getConnection(url + dbName, userName, password);
                        postConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void postConnection() {
        if (connection != null) {

            System.out.println("Database not null..");
            String imeiNumber = telephonyManager.getDeviceId();
            System.out.println("Imei is: "+imeiNumber);
            try{
                DatabaseMetaData dbm = connection.getMetaData();
                ResultSet tables = dbm.getTables(null,null,imeiNumber,null);
                if(tables.next()){
                    getGPSInformation();
                    String insertInDatabase = "INSERT INTO public.\""+imeiNumber+"\" (\"IMEI\", \"BUILDNUMBER\", \"MACADDRESS\",\"LATITUDE\", \"LONGITUDE\" )VALUES ('"+imeiNumber+"', '"+ Build.SERIAL+"', '"+wifiManager.getConnectionInfo().getMacAddress()+"', "+latitude+", "+longitude+")";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertInDatabase);
                    int result = preparedStatement.executeUpdate();
                    if(result>0){System.out.println("Data successfully added to database...");}
                    else{System.out.println("Data not inserted...");}
                }
                else{
                    System.out.println("Table not exist");
                    alartBuilder = new AlertDialog.Builder(this);
                    alartBuilder.setMessage("Your device is not registered for bus tracking please your IMEI number to register your IMEI number is: ").setCancelable(true);
                    AlertDialog alertDialog = alartBuilder.create();
                    alertDialog.setTitle("Alert !!!");
                    alertDialog.show();
                }
            }catch(Exception e){e.printStackTrace(); System.out.println("Something went wrong..");}

        } else {
            alartBuilder = new AlertDialog.Builder(this);
            alartBuilder.setMessage("It seems that the application is not able to connect to server.").setCancelable(true);
            AlertDialog alertDialog = alartBuilder.create();
            alertDialog.setTitle("Alert !!!");
            alertDialog.show();
        }
    }

    private void getGPSInformation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        System.out.println("Gps info: "+latitude+" "+longitude);


        /*while(true){
            locationListener.
        }*/

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("Gps info2: "+latitude+" "+longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };

    }

}
