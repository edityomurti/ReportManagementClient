package com.solusi247.reportmanagement.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.adapter.rest.Rest;
import com.solusi247.reportmanagement.adapter.rvAdapter.RVAdapter;
import com.solusi247.reportmanagement.interfaces.OnLoadMoreListener;
import com.solusi247.reportmanagement.model.ReportData;
import com.solusi247.reportmanagement.util.SessionManager;
import com.solusi247.reportmanagement.util.SharedPreference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

 public class ViewReportActivity extends AppCompatActivity {

    private int user_id;
    private String email;
    private String name;
    private Rest rest;
    private RecyclerView rv;

    private TextView tvUsername;
    private TextView errorLoadData;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar progressBar;
    private RVAdapter recyclerViewAdapter;
    public ProgressDialog progressDialog;

    private LinearLayoutManager llm;

    private SessionManager sessionManager;

    private int i=0;

    private boolean isEndList = false;

     private List<ReportData> reportDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Home");

        sessionManager = new SessionManager(getApplicationContext());

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        tvUsername = (TextView) findViewById(R.id.tvUserName);
//        text = sharedPreference.getValue(context);
//        tvUsername.setText(text);

        errorLoadData = (TextView) findViewById(R.id.tvErrorLoadData);
        errorLoadData.setVisibility(View.GONE);
        errorLoadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dibutuhkan onclick kosong untuk swiperefreshlistener
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        rv = (RecyclerView)findViewById(R.id.rv);
        registerForContextMenu(rv);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());

        recyclerViewAdapter = new RVAdapter(ViewReportActivity.this, reportDataList, rv);


        rv.setHasFixedSize(true);


        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        //get user data from session
        HashMap<String, String> user = sessionManager.getUserDetails();

        user_id = Integer.parseInt(user.get(SessionManager.KEY_USER_ID));
        email = user.get(SessionManager.KEY_EMAIL);
        name = user.get(SessionManager.KEY_NAME);

        tvUsername.setText(name);

        rest = new Rest();

//        initializeData();
        fetchRefreshWithoutPagination();

//        loadMoreTrigger();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reportDataList.clear();
                fetchRefreshWithoutPagination();
                recyclerViewAdapter.notifyDataSetChanged();
//                loadMoreTrigger();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewReportActivity.this, InsertReportActivity.class);
                startActivity(intent);
            }
        });


    }

     private void fetchRefreshWithoutPagination(){
         reportDataList.clear();
         rest.getRestApi().getAllReportById(user_id, new Callback<List<ReportData>>() {
             @Override
             public void success(List<ReportData> reportDatas, Response response) {
                 if (response.getStatus()==200) {
                     progressBar.setVisibility(View.GONE);
                     errorLoadData.setVisibility(View.GONE);
                     rv.setAdapter(recyclerViewAdapter);
                     reportDataList.addAll(reportDatas);
                     mSwipeRefreshLayout.setRefreshing(false);
                     if (reportDatas.size()==0){
                         Toast.makeText(ViewReportActivity.this, "Report kosong", Toast.LENGTH_SHORT).show();
                         errorLoadData.setText("No Data");
                         errorLoadData.setVisibility(View.VISIBLE);
                     }
                 } else if (response.getStatus()==204){
                     progressBar.setVisibility(View.GONE);
                     errorLoadData.setText("No report yet");
                     errorLoadData.setVisibility(View.VISIBLE);
                     mSwipeRefreshLayout.setRefreshing(false);
                 }
             }

             @Override
             public void failure(RetrofitError error) {
                 Toast.makeText(ViewReportActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                 Log.i("ERROR:", "Error parsing : " + error);
                 progressBar.setVisibility(View.GONE);
                 errorLoadData.setText("No data");
                 errorLoadData.setVisibility(View.VISIBLE);
                 mSwipeRefreshLayout.setRefreshing(false);
             }
         });
     }

    private void fetchRefresh(){
        Log.d("Fetching Report", "Memuat data baru");
        reportDataList.clear();
        i=0;

        Log.d("REST", "Checking Report Length");
        rest.getRestApi().getAllReportById(user_id, new Callback<List<ReportData>>() {
            @Override
            public void success(List<ReportData> reportDatas, Response response) {
                int reportLength = reportDatas.size();
                if (reportLength>5){
                    rest.getRestApi().getReport(user_id, 0, 5, new Callback<List<ReportData>>() {
                        @Override
                        public void success(List<ReportData> reportDatas, Response response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.getStatus()==200) {
                                errorLoadData.setVisibility(View.GONE);
                                rv.setAdapter(recyclerViewAdapter);
                                reportDataList.addAll(reportDatas);
                                mSwipeRefreshLayout.setRefreshing(false);
                            } else if (response.getStatus()==204){
                                errorLoadData.setText("No report yet");
                                errorLoadData.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(ViewReportActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                            Log.i("ERROR:", "Error parsing : " + error);
                            progressBar.setVisibility(View.GONE);
                            errorLoadData.setText("No data");
                            errorLoadData.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                } else {
                    rest.getRestApi().getReport(user_id, 0, reportLength, new Callback<List<ReportData>>() {
                        @Override
                        public void success(List<ReportData> reportDatas, Response response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.getStatus()==200) {
                                errorLoadData.setVisibility(View.GONE);
                                rv.setAdapter(recyclerViewAdapter);
                                reportDataList.addAll(reportDatas);
                                mSwipeRefreshLayout.setRefreshing(false);
                            } else if (response.getStatus()==204){
                                errorLoadData.setText("No report yet");
                                errorLoadData.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setRefreshing(false);
                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(ViewReportActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                            Log.i("ERROR:", "Error parsing : " + error);
                            progressBar.setVisibility(View.GONE);
                            errorLoadData.setText("No data");
                            errorLoadData.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Error Parsing:","error:" + error);
            }
        });


    }

     private void loadMoreTrigger(){
         recyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
             @Override
             public void onLoadMore() {
                 Log.e("haint", "Load more");
                 reportDataList.add(null);
                 recyclerViewAdapter.notifyItemInserted(reportDataList.size() - 1);

                 new Handler().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         Toast.makeText(ViewReportActivity.this, "mengahapus data terakhir", Toast.LENGTH_SHORT).show();
                         reportDataList.remove(reportDataList.size() - 1);
                         recyclerViewAdapter.notifyItemRemoved(reportDataList.size());
                         i+=5;
                         fetchMore(i);
                     }
                 }, 5000);
             }
         });
     }


    private void fetchMore(int page){
        Log.d("Fetching Report", "Memuat tambahan data");
        if (isEndList) {
            rest.getRestApi().getReport(user_id,page, 5, new Callback<List<ReportData>>() {
                @Override
                public void success(List<ReportData> reportDatas, Response response) {
                    if (response.getStatus()==200){
                        reportDataList.addAll(reportDatas);
                        recyclerViewAdapter.notifyDataSetChanged();
                        recyclerViewAdapter.setLoaded();
                        isEndList=false;
                    } else if (response.getStatus()==204) {
                        i-=5;
                        recyclerViewAdapter.setLoaded();
                        Toast.makeText(ViewReportActivity.this, "data habis", Toast.LENGTH_SHORT).show();
                        isEndList=true;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(ViewReportActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    Log.i("ERROR:", "Error parsing : " + error);
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            i-=5;
            recyclerViewAdapter.setLoaded();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }


    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(ViewReportActivity.this, "onResume()", Toast.LENGTH_SHORT).show();
//        loadMoreTrigger();
    }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater =  getMenuInflater();
         inflater.inflate(R.menu.menu_action, menu);
         return super.onCreateOptionsMenu(menu);
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()){
             case R.id.action_export_csv:
//                 progressDialog.setMessage("Exporting Reports to CSV ..");
//                 writeCSV();
//                 progressDialog.dismiss();
                 Intent intent = new Intent(ViewReportActivity.this, ExportCsvActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.action_sign_out:
                 AlertDialog.Builder builder = new AlertDialog.Builder(ViewReportActivity.this);
                 builder.setTitle("Are you sure want to Log out?");
                 builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         sessionManager.logoutUser();
                         finish();
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
                 return true;

             default:
                 return super.onOptionsItemSelected(item);
         }
     }

     @Override
     public boolean onContextItemSelected(MenuItem item) {
         int position;
         final int report_id;
         final String date;
         final String activity;
         final String project;
         final String desc;
         final String attachment;
         final int status;
         try{
             report_id = (recyclerViewAdapter.getReport_id());
             date = (recyclerViewAdapter.getDate());
             activity = (recyclerViewAdapter.getActivity());
             project = (recyclerViewAdapter.getProject());
             desc =(recyclerViewAdapter.getDesc());
             attachment = (recyclerViewAdapter.getAttachment());
             status = (recyclerViewAdapter.getStatus());
         } catch (Exception e){
             e.printStackTrace();
             return super.onContextItemSelected(item);
         }
         switch (item.getItemId()) {
             case 0:
                 Intent intent = new Intent(ViewReportActivity.this, UpdateReportActivity.class);
                 Bundle extras = new Bundle();
                 extras.putInt("report_id", report_id);
                 extras.putString("date", date);
                 extras.putString("activity", activity);
                 extras.putString("project", project);
                 extras.putString("desc", desc);
                 extras.putString("attachment", attachment);
                 extras.putInt("status", status);
                 intent.putExtras(extras);
                 startActivity(intent);
                 break;
             case 1:
                 AlertDialog.Builder builder = new AlertDialog.Builder(ViewReportActivity.this);
                 builder.setTitle("Remove '" +activity+ "' report?");
                 builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         progressDialog.setMessage("Deleting Report");
                         rest.getRestApi().deleteReport(report_id, attachment, new ResponseCallback() {
                             @Override
                             public void success(Response response) {
                                 recyclerViewAdapter.notifyDataSetChanged();
                                 progressDialog.dismiss();
                                 Toast.makeText(ViewReportActivity.this, "Report deleted", Toast.LENGTH_SHORT).show();
                                 progressBar.setVisibility(View.VISIBLE);
                                 fetchRefreshWithoutPagination();
                             }

                             @Override
                             public void failure(RetrofitError error) {
                                 Toast.makeText(ViewReportActivity.this, "Report not deleted, please check your connection", Toast.LENGTH_SHORT).show();
                             }
                         });
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
         return super.onContextItemSelected(item);
     }
 }