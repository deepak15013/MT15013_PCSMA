package me.branded.deepaksood.basicclientserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MyActivity";

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Button start;
    private Button stop;
    private Button send;

    private float valueX = 0;
    private float valueY = 0;
    private float valueZ = 0;

    private float timeStamp = 0;
    boolean flag = true;

    FileWriter writer;
    File file=null;

    private Socket client;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isExternalStorageWritable()) {
            Toast.makeText(MainActivity.this, "External storage writable", Toast.LENGTH_SHORT).show();
        }

        start=(Button)findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Start Pressed", Toast.LENGTH_SHORT).show();
                StartAcceleromter();

                generateCsvFile("Values.csv");

            }
        });

        stop = (Button)findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Stop Pressed", Toast.LENGTH_SHORT).show();
                StopAcceleromter();
            }
        });

        send=(Button)findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file == null) {
                    Toast.makeText(MainActivity.this, "No file present to send. Please first press Start", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "File Sent", Toast.LENGTH_SHORT).show();
                    sendFile sendfile = new sendFile();
                    sendfile.execute();
                    closeFile();
                }
            }
        });


    }

    private class sendFile extends AsyncTask<Void, Void, Void > {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                client = new Socket("192.168.50.26", 4444);   //ip address will be the server's ip-address

                byte[] mybytearray = new byte[(int) file.length()]; //create a byte array to file

                fileInputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(fileInputStream);

                bufferedInputStream.read(mybytearray, 0, mybytearray.length); //read the file

                outputStream = client.getOutputStream();

                outputStream.write(mybytearray, 0, mybytearray.length); //write file to the output stream byte by byte
                outputStream.flush();
                bufferedInputStream.close();
                outputStream.close();
                client.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    public void generateCsvFile(String sFileName)
    {
        try
        {
            File root = Environment.getExternalStorageDirectory();
            Log.v(TAG,root.toString());
            file = new File(root, sFileName);
            Log.v(TAG, file.toString());
            writer = new FileWriter(file);

            writer.append("TimeStamp");
            writer.append(',');
            writer.append("X-Value");
            writer.append(',');
            writer.append("Y-Value");
            writer.append(',');
            writer.append("Z-Value");
            writer.append('\n');

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void StartAcceleromter() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

            if(flag) {
                //Toast.makeText(MainActivity.this, "Accelerometer present", Toast.LENGTH_SHORT).show();
                flag=false;
            }

        }
        else {

            if(flag) {
                Toast.makeText(MainActivity.this, "Sorry! Accelerometer not present", Toast.LENGTH_SHORT).show();
                flag=false;
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void StopAcceleromter() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        valueX = event.values[0];
        valueY = event.values[1];
        valueZ = event.values[2];

        timeStamp = event.timestamp;

        try {
            writer.append(String.valueOf(timeStamp));
            writer.append(',');
            writer.append(String.valueOf(valueX));
            writer.append(',');
            writer.append(String.valueOf(valueY));
            writer.append(',');
            writer.append(String.valueOf(valueZ));
            writer.append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSensorManager != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }
}
