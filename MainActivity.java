package com.felhr.serialportexample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.ref.WeakReference;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;

    GraphView graph;
    SeekBar seek;
    VideoView video;
    TextView txt,txt2,txt3,txt4;


    int k;


    LineGraphSeries<DataPoint> ser;
    LineGraphSeries<DataPoint> ser1,ser2;
    double prg=2;

    float r,v,w=0,l,cp,po;
    int c= 20;
    int point =157;

    private MyHandler mHandler;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);


        graph= (GraphView) findViewById(R.id.graph);
        seek= (SeekBar) findViewById(R.id.seekBar);
        video =(VideoView) findViewById(R.id.videoView);
        txt = (TextView) findViewById(R.id.textext);
        txt2 = (TextView) findViewById(R.id.text2);
        txt3 = (TextView) findViewById(R.id.text3);
        txt4 = (TextView) findViewById(R.id.text4);

        seek.setMax(30);
        seek.setProgress(10);
        seek.setBackgroundColor(Color.TRANSPARENT);

        txt.setText("10");
        txt2.setText(String.valueOf(current(point,10)));
        txt3.setText("157");
        txt4.setText(String.valueOf(current(point,10)/point));

        ser= new LineGraphSeries<DataPoint>();
        ser1= new LineGraphSeries<DataPoint>();
        ser2= new LineGraphSeries<DataPoint>();



        inti(20,point);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prg=progress;
                inti((int)prg,point);
                ChooseVideo((int)prg);
                txt.setText(String.valueOf(prg));
                txt2.setText(String.valueOf(current(point,(int)prg)));
                txt4.setText(String.valueOf(current(point,(int)prg)/point));

                if (!txt.getText().toString().equals("")) {
                    String data = txt.getText().toString();
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        usbService.write(data.getBytes());
                    }
                }



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                inti((int) prg,point);
                ChooseVideo((int)prg);
                txt.setText(String.valueOf(prg));
                txt2.setText(String.valueOf(current(point,(int)prg)));

                if (!txt.getText().toString().equals("")) {
                    String data = txt.getText().toString();
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        usbService.write(data.getBytes());
                    }
                }


            }
        });




        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                ChooseVideo(seek.getProgress());
                video.requestFocus();
                video.start();
            }
        });


        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.setLooping(true);
            }
        });

        video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.b));
        video.requestFocus();
        video.start();







    }
    void inti(int a,int point){


        graph.removeAllSeries();
        int max=4000;
        DataPoint[] dataPoints =new DataPoint[400];
        for(int i=0;i<400;i++){
            dataPoints[i]=new DataPoint(i,(int)current(i,a));
            if(current(i,a)>max){
                max=(int) current(i,a)+10;
            }
        }

        DataPoint[] dataPoint=new DataPoint[1];
        for(int j=0;j<1;j++){
            dataPoint[j]=new DataPoint(point,(int)current(point,a));
        }

        DataPoint[] dataPoint1=new DataPoint[1];
        for(int k=0;k<1;k++){
            dataPoint1[k]=new DataPoint(point,(int)current(point,a));
        }





        ser=new LineGraphSeries<>(dataPoints);
        ser1=new LineGraphSeries<>(dataPoint);
        ser2=new LineGraphSeries<>(dataPoint1);


        ser.setThickness(2);
        ser.setDrawDataPoints(true);
        ser.setDataPointsRadius(2);




        ser1.setColor(Color.WHITE);
        ser1.setThickness(6);
        ser1.setDrawDataPoints(true);
        ser1.setDataPointsRadius(6);

        ser2.setColor(Color.RED);
        ser2.setThickness(4);
        ser2.setDrawDataPoints(true);
        ser2.setDataPointsRadius(4);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxX(400);
        graph.getViewport().setMaxY(1000+max);
        graph.addSeries(ser);
        graph.addSeries(ser1);
        graph.addSeries(ser2);



        graph.setBackgroundColor(Color.rgb(57,57,57));
        graph.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.WHITE);
        graph.getViewport().setBorderColor(Color.WHITE);
        graph.setTitleColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);


        ser.setTitle("P,RPM Curve");
        ser2.setTitle("Config point");

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setTextColor(Color.WHITE);

        graph.getLegendRenderer().setBackgroundColor(Color.rgb(30,30,30));
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    if(value!=0)return super.formatLabel(value, isValueX) + " RPM";
                    else return "0";
                } else {
                    // show currency for y values
                    if(value!=0)return super.formatLabel(value, isValueX) + " W";
                    else return "";
                }
            }
        });




    }
    public float current(float o,int k){


        r=(float) 1.2;
        v= (int) k;
        w=o;
        l=w*r/v;
        cp= (float) ((float) 0.22*(116/l-5)*Math.exp(-12.5/l));
        po= (float) (0.5*1.23*cp*Math.pow(v,3));
        return po;
    }

    @Override
    public void onResume() {
        super.onResume();
        video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.b));
        video.requestFocus();
        video.start();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }


    void ChooseVideo(int a){
        if(prg==0){
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.a));
        }else if(prg<5) {
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.b));
        }else if(prg<10){
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.c));
        }else if(prg<15){
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.d));
        }else if(prg<20){
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.e));
        }else if(prg<25){
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.f));
        }else {
            video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.i));
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().point= Integer.parseInt(data)*10;
                    mActivity.get().inti((int) mActivity.get().prg,Integer.parseInt(data)*10);


                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}