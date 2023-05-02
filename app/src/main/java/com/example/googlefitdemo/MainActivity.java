package com.example.googlefitdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.googlefitdemo.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
     private boolean weekOrNot;
    private String TAG = "MyTag";
    private ActivityMainBinding binding = null;
    private FitnessOptions fitnessOptions ;
    private FitnessDataResponseModel fitnessDataResponseModel;
    private TextView currentSteps,weeklySteps,monthSteps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        initialization();
        fitnessDataResponseModel.setSteps(4.5f);
        checkPermissions();

        binding.btnWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekOrNot = true;
                requestForWeek();
            }
        });
        binding.btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekOrNot = false;
                requestForMonth();
            }
        });
    }

    private void initialization(){
        fitnessDataResponseModel = new FitnessDataResponseModel();
        currentSteps = findViewById(R.id.currentSteps);
        weeklySteps = findViewById(R.id.weeklySteps);
        monthSteps = findViewById(R.id.monthly);
    }

    public void checkPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted, request permission
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//            checkGoogleFitPermission();
//        }
        checkGoogleFitPermission();
    }

    public void checkGoogleFitPermission(){
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA,FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA,FitnessOptions.ACCESS_READ)
                . build();
        GoogleSignInAccount account = getGoogleAccount();

        if (!GoogleSignIn.hasPermissions(account , fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    MainActivity.this,
                    1000, account, fitnessOptions);
        }else{
            startDataReading();
        }

        }

        private void startDataReading(){
            getTodayData();
        }

        private void getTodayData(){

        Fitness.getHistoryClient(this,getGoogleAccount())
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(this::onSuccessListner);
        }

        private void requestForWeek(){
            Calendar cal = Calendar.getInstance();

            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                    .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime,endTime,TimeUnit.MILLISECONDS)
                    .build();

            Fitness.getHistoryClient(this, getGoogleAccount())
                    .readData(readRequest)
                    .addOnSuccessListener(this::onSuccessListner);
        }
        private void requestForMonth(){
        Calendar cal = Calendar.getInstance();

        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime,endTime,TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, getGoogleAccount())
                .readData(readRequest)
                .addOnSuccessListener(this::onSuccessListner);
    }


    private GoogleSignInAccount getGoogleAccount(){
         if(fitnessOptions == null)checkGoogleFitPermission();
          return GoogleSignIn.getAccountForExtension(MainActivity.this,fitnessOptions);
        }

    private void onSuccessListner(Object o){

        if(o instanceof  DataSet){
            DataSet dataSet = (DataSet) o;
            getDataFromDataSet(dataSet);
        }else if (o instanceof DataReadResponse){
            fitnessDataResponseModel.steps = 0f;
            DataReadResponse dataReadResponse = (DataReadResponse) o;
            dataReadResponse.getBuckets();
            if (!dataReadResponse.getBuckets().isEmpty()){
                List<Bucket>bucketList = dataReadResponse.getBuckets();
                if (!bucketList.isEmpty())
                {
                    for(Bucket bucket : bucketList){
                        DataSet stepDataSet = bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        assert stepDataSet != null;
                        dumpDataSet(stepDataSet);
                    }
                }

            }
        }

        }
    private void dumpDataSet(DataSet dataSet) {

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                Toast.makeText(this, "weekly : " + dp.getValue(field), Toast.LENGTH_SHORT).show();

               if (weekOrNot) weeklySteps.setText("Weekly Steps " + dp.getValue(field));
               else monthSteps.setText("Monthly Steps " + dp.getValue(field));
            }
        }
    }
    private void getDataFromDataSet(DataSet dataSet) {

        List<DataPoint> dataPoints = dataSet.getDataPoints();
        for (DataPoint dataPoint : dataPoints){
            Log.d("MyTag", "getDataFromDataSet: " + dataPoint.getOriginalDataSource().getStreamName());

            for(Field field : dataPoint.getDataType().getFields()){
                float value = Float.parseFloat(dataPoint.getValue(field).toString());
                Log.e("MyTag" , "data : "+ value);
                currentSteps.setText("CurrentSteps : "+ value);
            }
        }

    }
}



