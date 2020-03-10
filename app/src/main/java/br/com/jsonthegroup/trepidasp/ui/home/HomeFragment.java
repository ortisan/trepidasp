package br.com.jsonthegroup.trepidasp.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

import br.com.jsonthegroup.trepidasp.R;

public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel homeViewModel;

    private SensorManager sensorManager;
    private Sensor sensor;

    private Button btnBom;
    private Button btnRuim;
    private Button btnParar;

    private TextView textAcelerometro2;
    private TextView textGiroscopio2;

    private Boolean parado = true;
    private Boolean bom = false;
    private Boolean ruim = false;

    private OutputStreamWriter reportGiroscopio;
    private OutputStreamWriter reportAcelerometro;

    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView textLocalizacao2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnBom = root.findViewById(R.id.btn_bom);
        btnRuim = root.findViewById(R.id.btn_ruim);
        btnParar = root.findViewById(R.id.btn_parar);

        textAcelerometro2 = root.findViewById(R.id.text_acelerometro2);
        textGiroscopio2 = root.findViewById(R.id.text_giroscopio2);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        btnBom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruim = false;
                bom = true;
                parado = false;
                doClickBom(v);
            }
        });

        btnRuim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruim = true;
                bom = false;
                parado = false;
                doClickRuim(v);
            }
        });

        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruim = false;
                bom = false;
                parado = true;
                doClickParar(v);
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        textLocalizacao2 = root.findViewById(R.id.text_localizacao2);
        getLastLocation();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void doStartListener() {
        sensorManager.registerListener(HomeFragment.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(HomeFragment.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void startFiles(String tipo) {
        try {
            File folderMyApp = new File(Environment.getExternalStorageDirectory() + "/" + "TrepidaSp/");
            if (!folderMyApp.exists()) {
                folderMyApp.mkdirs();
            }
            long currentTime = System.currentTimeMillis();

            String acelerometroFileName = String.format("acelerometro_%s_%d.log", tipo, currentTime);
            File acelerometroFile = new File(folderMyApp, acelerometroFileName);
            acelerometroFile.createNewFile();
            reportAcelerometro = new OutputStreamWriter(new FileOutputStream(acelerometroFile));

            String giroscopioFileName = String.format("giroscopio_%s_%d.log", tipo, currentTime);
            File giroscopioFile = new File(folderMyApp, giroscopioFileName);
            giroscopioFile.createNewFile();
            reportGiroscopio = new OutputStreamWriter(new FileOutputStream(giroscopioFile));

        } catch (Exception exc) {
            throw new RuntimeException("Erro ao gravar o arquivo");
        }
    }

    public void doClickBom(View view) {
        doClickParar(view);
        startFiles("BOM");
        doStartListener();
    }

    public void doClickRuim(View view) {
        doClickParar(view);
        startFiles("RUIM");
        doStartListener();
    }

    public void doClickParar(View view) {
        try {
            if (reportAcelerometro != null) {
                reportAcelerometro.close();
                reportGiroscopio.close();
            }
        } catch (Exception exc) {
            throw new RuntimeException("Erro ao fechar os arquivos");
        }
        sensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {

//        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();

        double latitude = -1;
        double longitude = -1;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String valoresAcelerometro = String.format("X=%.4f;Y=%.4f;Z=%.4f;latitude=%.4f;longitude=%.4f;timestamp=%d;", event.values[0], event.values[1], event.values[2], latitude, longitude, System.currentTimeMillis());
            this.textAcelerometro2.setText(valoresAcelerometro);
            try {
                reportAcelerometro.write(String.format("%s\n", valoresAcelerometro));
            } catch (Exception exc) {
                throw new RuntimeException("Erro");
            }

        } else {
            String valoresGiroscopio = String.format("X=%.4f;Y=%.4f;Z=%.4f;latitude=%.4f;longitude=%.4f;timestamp=%d;", event.values[0], event.values[1], event.values[2], latitude, longitude, System.currentTimeMillis());
            this.textGiroscopio2.setText(valoresGiroscopio);

            try {
                reportGiroscopio.write(String.format("%s\n", valoresGiroscopio));
            } catch (Exception exc) {
                throw new RuntimeException("Erro");
            }
        }

        getLastLocation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    private void getLastLocation(){
        if(checkPermissions()){
            if(isLocationEnabled()){
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null){
                            requestNewLocationData();
                        } else {
                            getCurrentAddress(location);
                        }
                    }
                });
            } else {
                Toast.makeText(this.getActivity(), "Por favor ative a Localização!", Toast.LENGTH_LONG);
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                this.getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
        }
    }

    private boolean checkPermissions(){
        return ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            mLastLocation.getLatitude();
            mLastLocation.getLongitude();
            getCurrentAddress(mLastLocation);
        }
    };

    private void getCurrentAddress(Location location){
        Geocoder gcd = new Geocoder(this.getContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                String address = this.handleNullEmptyString(addresses.get(0).getAddressLine(0));
                String locality = this.handleNullEmptyString(addresses.get(0).getLocality());
                String subLocality = this.handleNullEmptyString(addresses.get(0).getSubLocality());
                String state = this.handleNullEmptyString(addresses.get(0).getAdminArea());
                String country = this.handleNullEmptyString(addresses.get(0).getCountryName());
                String postalCode = this.handleNullEmptyString(addresses.get(0).getPostalCode());
                String knownName = this.handleNullEmptyString(addresses.get(0).getFeatureName());

                String completeAddress = address + ", " + locality + ", " + subLocality + ", " + subLocality + ", " + state + ", " + country + ", " + postalCode + ", " + knownName;
                textLocalizacao2.setText(completeAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getActivity(), "Ocorreu um erro ao obter a localização", Toast.LENGTH_SHORT).show();
        }
    }

    private String handleNullEmptyString(String s){
        return s == null ? "" : s.trim().toUpperCase();
    }
}
