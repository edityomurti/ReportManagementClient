package com.solusi247.reportmanagement.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.adapter.rest.Rest;
import com.solusi247.reportmanagement.model.ReportData;
import com.solusi247.reportmanagement.util.SessionManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ExportCsvActivity extends AppCompatActivity {

    private int user_id;

    private LinearLayout llFromDate, llUntilDate;
    private TextView tvFromDate, tvUntilDate, tvExport;
    private ProgressDialog progressDialog;

    private Calendar calendarFrom, calendarUntil;
    private int fromYear, fromMonth, fromDay, untilYear, untilMonth, untilDay;

    private SessionManager sessionManager;

    private Rest rest;

    //PERMISSION FOR API23+
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<ReportData> reportDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_csv);
        setTitle("Export To CSV");

        llFromDate = (LinearLayout) findViewById(R.id.llFromDate);
        llUntilDate = (LinearLayout) findViewById(R.id.llUntilDate);
        tvFromDate = (TextView) findViewById(R.id.tvFromDate);
        tvUntilDate = (TextView) findViewById(R.id.tvUntilDate);
        tvExport = (TextView) findViewById(R.id.tvExport);
        progressDialog = new ProgressDialog(this);

        sessionManager = new SessionManager(this);
        //get user data from session
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = Integer.parseInt(user.get(SessionManager.KEY_USER_ID));

        rest = new Rest();

        calendarFrom = Calendar.getInstance();
        calendarFrom.setTime(calendarFrom.getTime());
        calendarFrom.add(Calendar.DAY_OF_YEAR, -7);
        fromYear = calendarFrom.get(Calendar.YEAR);
        fromMonth = calendarFrom.get(Calendar.MONTH);
        fromDay= calendarFrom.get(Calendar.DAY_OF_MONTH);
        Log.i("Data tgl::", "Dari Tanggal = " + fromYear + fromMonth + fromDay);

        calendarUntil = Calendar.getInstance();
        untilYear = calendarUntil.get(Calendar.YEAR);
        untilMonth = calendarUntil.get(Calendar.MONTH);
        untilDay = calendarUntil.get(Calendar.DAY_OF_MONTH);
        Log.i("Data tgl::", "Hingga Tanggal = " + untilYear + untilMonth + untilDay);

        showFromDate(fromYear, fromMonth+1, fromDay);
        showUntilDate(untilYear, untilMonth+1, untilDay);

        llFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(998);
            }
        });

        llUntilDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });

        tvExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String fromDate, untilDate;

                fromDate = tvFromDate.getText().toString();
                untilDate = tvUntilDate.getText().toString();

                progressDialog.setMessage("Exporting to CSV..");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                reportDataList.clear();
                rest.getRestApi().getReportsByDate(fromDate, untilDate, user_id, new Callback<List<ReportData>>() {
                    @Override
                    public void success(List<ReportData> reportDatas, Response response) {
                        reportDataList.addAll(reportDatas);
                        if (reportDataList.size()==0){
                            progressDialog.dismiss();
                            Toast.makeText(ExportCsvActivity.this, "No report found on that period, CSV not created.", Toast.LENGTH_SHORT).show();
                        } else {
                            writeCSV();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(ExportCsvActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id==998){
            DatePickerDialog dialog = new DatePickerDialog(this, fromDateListener, fromYear, fromMonth, fromDay);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        } else
        if (id==999){
            DatePickerDialog dialog = new DatePickerDialog(this, untilDateListener, untilYear, untilMonth, untilDay);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener fromDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = Year
            // arg2 = month
            // arg3 = day
            showFromDate(arg1, arg2+1, arg3);
        }
    };

    private DatePickerDialog.OnDateSetListener untilDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = Year
            // arg2 = month
            // arg3 = day
            showUntilDate(arg1, arg2+1, arg3);
        }
    };

    private void showFromDate(int year, int month, int day){
        tvFromDate.setText(new StringBuilder().append(day).append("-")
                .append(month).append("-").append(year));
    }

    private void showUntilDate(int year, int month, int day) {
        tvUntilDate.setText(new StringBuilder().append(day).append("-")
                .append(month).append("-").append(year));
    }

    public void writeCSV(){
        verifyStoragePermissions(this);
        String extStore = "/storage/emulated/legacy/Report" + user_id + System.currentTimeMillis() + ".csv";
        final File dir = new File(Environment.getExternalStorageDirectory() + "/Report CSV/");

        dir.mkdirs(); //create folders where write files
        String fileName = "Report " + user_id + System.currentTimeMillis() + ".csv";
        final File file = new File(dir, fileName);
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(file));

            List<String[]> data = new ArrayList<String[]>();

            data.add(new String[] {"report_id", "user_id", "date", "project", "activity", "status", "desc", "attachment", "created_at"});
            for (int i=0; i<reportDataList.size(); i++) {
                String report_id, user_id, date, project, activity, status, desc, attachment, created_at;
                ReportData reportData = reportDataList.get(i);
                report_id = String.valueOf(reportData.getReport_id());
                user_id = String.valueOf(reportData.getUser_id());
                date = reportData.getDate();
                project = reportData.getProject();
                activity = reportData.getActivity();
                if (reportData.getStatus()==0){
                    status = "in progress";
                } else {
                    status = "done";
                }

                desc = reportData.getDesc();
                attachment = reportData.getAttachment();
                created_at = reportData.getCreated_at();

                data.add(new String[] {report_id, user_id, date, project, activity, status, desc, attachment, created_at});
            }
            writer.writeAll(data);
            writer.close();
            Toast.makeText(ExportCsvActivity.this, "CSV created in " + dir+fileName, Toast.LENGTH_LONG).show();
            Log.e("EXPORT DB", "path csv :"+dir);
            progressDialog.dismiss();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("export DB", "error export: " + e);
            progressDialog.dismiss();
            Toast.makeText(ExportCsvActivity.this, "Failed to Export Report", Toast.LENGTH_SHORT).show();
        }
    }

    //PERMISSION FOR CREATING CSV FILE
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
