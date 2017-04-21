package com.solusi247.reportmanagement.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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

import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.adapter.rest.Rest;
import com.solusi247.reportmanagement.util.SessionManager;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class InsertReportActivity extends AppCompatActivity {

    Rest rest;

    private String date;
    private String project;
    private String activity;
    private int status;
    private String desc;
    private String attachment;

    private TextView tvDate;
    private ImageView ivDate;
    private EditText etProject;
    private EditText etActivity;
    private Spinner sStatus;
    private EditText etDesc;
    private ImageView ivAttach;
    private TextView tvSave;
    private TextView tvDeleteImage;

    private DatePicker datePicker;
    private Calendar calendar;
    private int year, month, day;

    private CameraPhoto cameraPhoto;
    private GalleryPhoto galleryPhoto;

    private TypedFile imagePath = null;
    private ProgressDialog dialog;

    private com.solusi247.reportmanagement.util.SessionManager sessionManager;

    private int user_id;
    private String email;
    private String name;

    final int CAMERA_REQUEST= 13323;
    final int GALLERY_REQUEST=22222;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_report);
        setTitle("Insert Report");

        tvDate = (TextView) findViewById(R.id.tvDate);
        ivDate = (ImageView) findViewById((R.id.ivDate));
        etProject = (EditText) findViewById(R.id.etProject);
        etActivity = (EditText) findViewById(R.id.etActivity);
        sStatus = (Spinner) findViewById(R.id.sStatus);
        etDesc = (EditText) findViewById(R.id.etDesc);
        ivAttach = (ImageView) findViewById(R.id.ivAttach);
        tvSave = (TextView) findViewById(R.id.tvSave);
        tvDeleteImage = (TextView) findViewById(R.id.tvDeleteImage);
        tvDeleteImage.setVisibility(View.GONE);

        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());

        sessionManager = new SessionManager(getApplicationContext());

        //get user data from session
        HashMap<String, String> user = sessionManager.getUserDetails();

        user_id = Integer.parseInt(user.get(SessionManager.KEY_USER_ID));
        email = user.get(SessionManager.KEY_EMAIL);
        name = user.get(SessionManager.KEY_NAME);

        String[] items = new String[]{"In Progress", "Done"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        sStatus.setAdapter(adapter);

        final ViewGroup.LayoutParams params = ivAttach.getLayoutParams();

        tvDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InsertReportActivity.this);
                builder.setTitle("Are you sure want to delete the image?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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

        ivDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });
        calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        Log.i("Data tgl::", "Tanggal = " + year + month + day);

        showDate(year, month+1, day);

        ivAttach.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
//                AlertDialog.Builder builder = new AlertDialog.Builder(InsertReportActivity.this);
//
//                builder.setTitle("Choose image From");
//
//                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int which) {
//                    try{
//                        startActivityForResult(cameraPhoto.takePhotoIntent(),CAMERA_REQUEST);
//                    } catch(IOException e)
//                        {
//                            Toast.makeText(getApplicationContext(),"Something Wrong while taking photos", Toast.LENGTH_SHORT).show();
//                        }
//                    dialog.dismiss();
//                    }
//                });
//
//                builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        startActivityForResult(galleryPhoto.openGalleryIntent(),GALLERY_REQUEST);
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(InsertReportActivity.this);
                    builder.setTitle("Are you sure?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            insertData();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

    }

    private void showDate(int year, int month, int day) {
        tvDate.setText(new StringBuilder().append(day).append("-")
                .append(month).append("-").append(year));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(InsertReportActivity.this, "Error getting the image", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String selectedImagePath = null;
                Uri selectedImage = data.getData();
                tvDeleteImage.setVisibility(View.VISIBLE);

                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage,  projection, null, null, null);
                if (cursor == null) {
                    selectedImagePath = selectedImage.getPath();
                } else {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    File file = new File(path);
                    //Membatasi file upload sebesar 1MB
//                    if (file.length() > 1000000) {
//                        Toast.makeText(InsertReportActivity.this, "Image is too large, Max: 1MB", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    Log.d("IMAGE SELECTED", selectedImage.getPath());

//                    Bitmap compressedBitmap = Compressor.getDefault(this).compressToBitmap(compressedImageFile);
//                    Log.e("INSERT UPDATE", "compressed imageBitmap size: " + compressedBitmap.getByteCount());
//
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                    Bitmap decodedBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
//                    Log.e("INSERT UPDATE", "compressed decodedBitmap size: " + decodedBitmap.getByteCount());

                    String type = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
                    if (extension != null) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        
                    }
                    imagePath =new TypedFile(type, file);
                    Log.d("Choosen Image","Path - " + file.getAbsolutePath());
                    Log.d("Choosen Image", "Path - " + file.getAbsoluteFile());


                    ViewGroup.LayoutParams params = ivAttach.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ivAttach.setLayoutParams(params);
                    ivAttach.setImageURI(selectedImage);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                tvDeleteImage.setVisibility(View.GONE);
                Toast.makeText(InsertReportActivity.this, "Error getting image, not recognized mime type", Toast.LENGTH_SHORT);
                return;
            }
        }

//        if(resultCode == RESULT_OK)
//        {
//            if(requestCode == CAMERA_REQUEST)
//            {
//                String  photoPath = cameraPhoto.getPhotoPath();
//                try
//                {
//                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
//                    ivAttach.setImageBitmap(bitmap);
//                } catch (FileNotFoundException e) {
//                    Toast.makeText(getApplicationContext(),
//                            "Something Wrong while loading photos", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//            else if (requestCode == GALLERY_REQUEST)
//            {
//                Uri uri = data.getData();
//                galleryPhoto.setPhotoUri(uri);
//
//                String photoPath = galleryPhoto.getPath();
//                try
//                {
//                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
//
//                    ViewGroup.LayoutParams params = ivAttach.getLayoutParams();
//                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//                    ivAttach.setLayoutParams(params);
//                    ivAttach.setImageBitmap(bitmap);
//                } catch (FileNotFoundException e) {
//                    Toast.makeText(getApplicationContext(),
//                            "Something Wrong while choosing photos", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

    private void insertData() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage("Adding Report");
        }
        dialog.show();

        if (imagePath==null) {
            rest.getRestApi().postReportWithoutImage(user_id, date, activity, project, status, desc, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Toast.makeText(InsertReportActivity.this, "Report successfully created", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Intent intent = new Intent(InsertReportActivity.this, ViewReportActivity.class);
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i("ERROR POST REPORT :", "error post - " + error);
                    dialog.dismiss();
                    Toast.makeText(InsertReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            rest.getRestApi().postReport(user_id, date, project, activity, status, desc, imagePath, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Toast.makeText(InsertReportActivity.this, "Report successfully created", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Intent intent = new Intent(InsertReportActivity.this, ViewReportActivity.class);
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i("ERROR POST REPORT :", "error post - " + error);
                    dialog.dismiss();
                    Toast.makeText(InsertReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
                }
            });
        }

//        rest.getRestApi().postReport(date, project, activity, status, desc, imagePath, new Callback<StatusData>() {
//            @Override
//            public void success(StatusData statusData, Response response) {
//                Toast.makeText(InsertReportActivity.this, "Report successfully created", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//                Intent intent = new Intent(InsertReportActivity.this, ViewReportActivity.class);
//                startActivity(intent);
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.i("ERROR POST REPORT :", "error post - " + error);
//                dialog.dismiss();
//                Toast.makeText(InsertReportActivity.this, "Failed to create report, please check your connection..", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, myDateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            showDate(arg1, arg2+1, arg3);
        }
    };

}