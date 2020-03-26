package br.com.jsonthegroup.trepidasp.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import br.com.jsonthegroup.trepidasp.R;

public class HomeFragment extends Fragment implements SensorEventListener, LocationListener {


    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;


    private HomeViewModel homeViewModel;

    private LocationManager locationManager;
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

    boolean isGPS;
    boolean isNetwork;

    private Location location;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }


        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnBom = root.findViewById(R.id.btn_bom);
        btnRuim = root.findViewById(R.id.btn_ruim);
        btnParar = root.findViewById(R.id.btn_parar);

        textAcelerometro2 = root.findViewById(R.id.text_acelerometro2);
        textGiroscopio2 = root.findViewById(R.id.text_giroscopio2);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

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

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        getLastLocation();

        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPS || isNetwork) {
            getLocation();
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
            File folderMyApp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "TrepidaSp/");
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

        double latitude = -1;
        double longitude = -1;

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

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
    }


    private void getLocation() {
        try {

            String provider = LocationManager.NETWORK_PROVIDER;

            if (isGPS) {
                provider = LocationManager.GPS_PROVIDER;
            }

            locationManager.requestLocationUpdates(provider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            this.location = locationManager.getLastKnownLocation(provider);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {


        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            isGPS = true;
        } else {
            isNetwork = true;
        }

        getLocation();


    }

    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            isGPS = false;
        } else {
            isNetwork = false;
        }
    }
}
