package br.com.jsonthegroup.trepidasp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.w3c.dom.Text;

import java.io.File;

import br.com.jsonthegroup.trepidasp.R;

public class HomeFragment extends Fragment implements SensorEventListener {

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

    private File reportGiroscopio;
    private File reportAcelerometro;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


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
//        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            throw new SecurityException("Faltou permissão de gps");
//        }
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

    public void doClickBom(View view) {
        long currentTime = System.currentTimeMillis();

        String acelerometroFileName = String.format("acelerometro_%s_%d.log", "BOM", currentTime);
        reportAcelerometro = new File(this.getContext().getFilesDir(), acelerometroFileName);

//        String giroscopioFileName = String.format("giroscopio_%s_%d.log", "BOM", currentTime);
//        reportGiroscopio = new File(this.getContext().getFilesDir(), giroscopioFileName);

        doStartListener();
    }

    public void doClickRuim(View view) {
        long currentTime = System.currentTimeMillis();

        String acelerometroFileName = String.format("acelerometro_%s_%d.log", "RUIM", currentTime);
        reportAcelerometro = new File(this.getContext().getFilesDir(), acelerometroFileName);

//        String giroscopioFileName = String.format("giroscopio_%s_%d.log", "RUIM", currentTime);
//        reportGiroscopio = new File(this.getContext().getFilesDir(), giroscopioFileName);

        doStartListener();
    }

    public void doClickParar(View view) {
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
        } else {
            String valoresGiroscopio = String.format("X=%.4f;Y=%.4f;Z=%.4f;latitude=%.4f;longitude=%.4f;timestamp=%d;", event.values[0], event.values[1], event.values[2], latitude, longitude, System.currentTimeMillis());
            this.textGiroscopio2.setText(valoresGiroscopio);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
