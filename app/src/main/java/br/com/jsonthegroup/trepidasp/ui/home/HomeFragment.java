package br.com.jsonthegroup.trepidasp.ui.home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import br.com.jsonthegroup.trepidasp.R;

public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel homeViewModel;

    private SensorManager sensorManager;
    private Sensor sensor;

    private Button btnBom;
    private Button btnRuim;
    private Button btnParar;

    private Boolean parado = true;
    private Boolean bom = false;
    private Boolean ruim = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        btnBom = root.findViewById(R.id.btn_bom);
        btnRuim = root.findViewById(R.id.btn_ruim);
        btnParar = root.findViewById(R.id.btn_parar);


        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


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

    public void doClickBom(View view) {
        System.out.println("click bom = " + view);
    }

    public void doClickRuim(View view) {
        System.out.println("click ruim = " + view);
    }

    public void doClickParar(View view) {
        System.out.println("click parar = " + view);
    }

    public void onSensorChanged(SensorEvent event) {

        if (parado) {
            return;
        }

        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.
        float alpha = 0.8f;
        float[] gravity = new float[3];
        float[] linear_acceleration = new float[3];

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        System.out.println("Sensor movido");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
