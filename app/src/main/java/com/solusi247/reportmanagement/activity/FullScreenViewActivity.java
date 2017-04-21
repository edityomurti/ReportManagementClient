package com.solusi247.reportmanagement.activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.solusi247.reportmanagement.R;

public class FullScreenViewActivity extends AppCompatActivity {

    String attachment;

    ImageView ivFullscreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_view);

        ivFullscreenImage= (ImageView) findViewById(R.id.IvFullscreenImage);

        Bundle extras = getIntent().getExtras();
        attachment= extras.getString("attachment");
//        String imageDir = "http://192.168.1.44:8081/projectManagementApi/uploads/"; //Laptop Edityo
        String imageDir = "http://192.168.1.228:8080/images/"; //Server Solusi

        Glide.with(this).load(imageDir+attachment).into(ivFullscreenImage);

    }

}
