package com.solusi247.reportmanagement.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.adapter.rest.Rest;
import com.solusi247.reportmanagement.util.SessionManager;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class UpdateReportActivity extends AppCompatActivity {

    private Rest rest;

    private int user_id;
    private int report_id;
    private String date;
    private String project;
    private String activity;
    private int status;
    private String desc;
    private String old_attachment;

    private boolean isImageUpdated = false;

    private TextView tvDate;
    private ImageView ivDate;
    private EditText etProject;
    private EditText etActivity;
    private EditText etDesc;
    private Spinner sStatus;
    private ImageView ivAttach;
    private TextView tvUpdate;
    private TextView tvDeleteImage;

    private Calendar calendar;
    private int year, month, day;

    private TypedFile imagePath = null;
    private ProgressDialog progressDialog;

    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_report);
        setTitle("Edit Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvDate = (TextView) findViewById(R.id.tvDate);
        ivDate = (ImageView) findViewById(R.id.ivDate);
        etProject = (EditText) findViewById(R.id.etProject);
        etActivity = (EditText)findViewById(R.id.etActivity);
        etDesc = (EditText) findViewById(R.id.etDesc);
        sStatus = (Spinner) findViewById(R.id.sStatus);
        ivAttach = (ImageView) findViewById(R.id.ivAttach);
        tvUpdate = (TextView) findViewById(R.id.tvUpdate);
        tvDeleteImage = (TextView) findViewById(R.id.tvDeleteImage);

        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();

        user_id = Integer.parseInt(user.get(SessionManager.KEY_USER_ID));
        String[] items = new String[]{"In Progress", "Done"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        sStatus.setAdapter(adapter);


        calendar = Calendar.getInstance();

        Bundle extras = getIntent().getExtras();
        report_id = extras.getInt("report_id");
        date = extras.getString("date");
        Log.e("geting date", "date extra:"+date);
        setDate(date);
        project = extras.getString("project");
        activity = extras.getString("activity");
        desc = extras.getString("desc");
        status = extras.getInt("status");
        old_attachment = extras.getString("attachment");

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });
        ivDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });


        tvDate.setText(date);
        etProject.setText(project);
        etActivity.setText(activity);
        etDesc.setText(desc);
        if(status==0){
            sStatus.setSelection(0);
        } else {
            sStatus.setSelection(1);
        }

        final ViewGroup.LayoutParams params = ivAttach.getLayoutParams();

        tvDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateReportActivity.this);
                builder.setTitle("Are you sure want to delete the image?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isImageUpdated = true;
                        ivAttach.setImageDrawable(getResources().getDrawable(R.drawable.bt_attach));
                        int int65dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
                        params.height = int65dp;
                        params.width = int65dp;
                        ivAttach.setLayoutParams(params);
                        tvDeleteImage.setVisibility(View.GONE);
                        imagePath = null;
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        if (old_attachment!=null){
//            String imageDir = "http://192.168.1.44:8081/projectManagementApi/uploads/"; //Laptop Edityo
            String imageDir = "http://192.168.1.228:8080/images/"; //Server Solusi
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            ivAttach.setLayoutParams(params);
            Glide.with(this).load(imageDir+old_attachment).into(ivAttach);
            Log.e("Image update" ,imageDir+old_attachment);
            tvDeleteImage.setVisibility(View.VISIBLE);
        } else {
            ivAttach.setImageDrawable(getResources().getDrawable(R.drawable.bt_attach));
            tvDeleteImage.setVisibility(View.GONE);
        }

        ivAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
            }
        });

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rest = new Rest();
                Boolean isFieldComplete = false;

                date = String.valueOf(tvDate.getText());
                project = String.valueOf(etProject.getText());
                activity = String.valueOf(etActivity.getText());
                if(sStatus.getSelectedItem().equals("In Progress")){
                    status = 0;
                }else
                {
                    status = 1;
                }
                desc = String.valueOf(etDesc.getText());

                if(project.equals("")){
                    etProject.setError("Please fill out this field");
                } else
                if(activity.equals("")){
                    etProject.setError(null);
                    etActivity.setError("Please fill out this field");
                } else
                if(desc.equals("")){
                    etActivity.setError(null);
                    etDesc.setError("Please fill out this field");
                } else {
                    isFieldComplete = true;
                }

                if (isFieldComplete) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateReportActivity.this);
                    builder.setTitle("Are you sure?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            insertData();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    private void setDate(String date){
        day = Integer.parseInt(date.substring(0, date.indexOf("-")));
        month = Integer.parseInt(date.substring(date.indexOf("-")+1, date.lastIndexOf("-")));
        year = Integer.parseInt(date.substring(date.lastIndexOf("-")+1));
    }

    private void showDate(int year, int month, int day) {
        tvDate.setText(new StringBuilder().append(day).append("-")
                .append(month).append("-").append(year));
    }

    private void insertData() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating Report");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (!isImageUpdated) {
            //1. update tidak merubah status foto
            rest.getRestApi().updateReportWithoutImage(date, project, activity, status, desc, null, old_attachment, report_id, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Toast.makeText(UpdateReportActivity.this, "Report updated", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    Intent intent = new Intent(UpdateReportActivity.this, ViewReportActivity.class);
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i("ERROR POST REPORT :", "error post - " + error);
                    progressDialog.dismiss();
                    Toast.makeText(UpdateReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if(old_attachment!=null&&imagePath==null) {
                //2. Menghapus foto
                rest.getRestApi().updateReportWithoutImage(date, project, activity, status, desc, old_attachment, null, report_id, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(UpdateReportActivity.this, "Report updated", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(UpdateReportActivity.this, ViewReportActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i("ERROR POST REPORT :", "error post - " + error);
                        progressDialog.dismiss();
                        Toast.makeText(UpdateReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if(old_attachment==null&&imagePath!=null){
                //3. update menambah foto
                rest.getRestApi().updateReport(user_id, date, project, activity, status, desc, null, imagePath, report_id, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(UpdateReportActivity.this, "Report updated", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(UpdateReportActivity.this, ViewReportActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i("ERROR POST REPORT :", "error post - " + error);
                        progressDialog.dismiss();
                        Toast.makeText(UpdateReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (old_attachment!=null&&imagePath!=null){
                //3. update mengganti foto
                rest.getRestApi().updateReport(user_id, date, project, activity, status, desc, old_attachment, imagePath, report_id, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(UpdateReportActivity.this, "Report updated", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(UpdateReportActivity.this, ViewReportActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i("ERROR POST REPORT :", "error post - " + error);
                        progressDialog.dismiss();
                        Toast.makeText(UpdateReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (old_attachment==null&&imagePath==null){
                //4. menghapus foto
                rest.getRestApi().updateReportWithoutImage(date, project, activity, status, desc, null, old_attachment, report_id, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(UpdateReportActivity.this, "Report updated", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(UpdateReportActivity.this, ViewReportActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i("ERROR POST REPORT :", "error post - " + error);
                        progressDialog.dismiss();
                        Toast.makeText(UpdateReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == 2 && resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Toast.makeText(UpdateReportActivity.this, "Error getting the image", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvDeleteImage.setVisibility(View.VISIBLE);
                isImageUpdated = true;

                String selectedImagePath = null;
                Uri selectedImage = data.getData();

                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage, projection, null, null, null);
                if (cursor == null) {
                    selectedImagePath = selectedImage.getPath();
                } else {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    File file = new File(path);
                    //Membatasi file upload sebesar 1MB
//                    if (file.length() > 1000000) {
//                        Toast.makeText(UpdateReportActivity.this, "Image is too large, Max: 1MB", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    String type = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
                    if (extension != null) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    }
                    imagePath = new TypedFile(type, file);
                    Log.d("Choosen Image", "Path - " + file.getAbsolutePath());
                    Log.d("Choosen Image", "Path - " + file.getAbsoluteFile());

                    ViewGroup.LayoutParams params = ivAttach.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ivAttach.setLayoutParams(params);
                    Log.e("Coosen Image","Set Image to imageview");
                    ivAttach.setImageURI(selectedImage);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            tvDeleteImage.setVisibility(View.GONE);
            Toast.makeText(UpdateReportActivity.this, "Error getting image, not recognized mime type", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            showDate(i, i1+1, i2);
        }
    };



    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            Log.e("DatePickerDialog", "Tanggal:"+day+"-"+month+"-"+year);
//            DatePickerDialog dialog = new DatePickerDialog(this, myDateListener, year, month, day);
//            DatePickerDialog dialog = new DatePickerDialog(this, myDateListener, 2016, 8, 17);
            DatePickerDialog dialog = new DatePickerDialog(this, myDateListener, 2016, 8, 17);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateReportActivity.this);
                builder.setTitle("Cancel editing?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateReportActivity.this);
        builder.setTitle("Cancel editing?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
