package com.solusi247.reportmanagement.adapter.rest;

import com.solusi247.reportmanagement.model.ReportData;
import com.solusi247.reportmanagement.model.StatusData;
import com.solusi247.reportmanagement.model.UserData;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.EncodedPath;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/**
 * Created by usernames on 22/06/16.
 */
public interface RestApi {
    //View Report
    @GET("/report/getAllReportByIdPaginated")
    void getReport(@Query("user_id") int user_id, @Query("page") int page, @Query("perPage") int perPage, Callback<List<ReportData>> reportCallback);

    @GET("/report/getAllReportById")
    void getAllReportById(@Query("user_id") int user_id, Callback<List<ReportData>> reportCallback);

    @GET("/report/getReportsByDate")
    void getReportsByDate(@Query("firstDate") String firstDate, @Query("lastDate") String lastDate, @Query("user_id") int user_id, Callback<List<ReportData>> repListCallback);

    @GET("/report/insertReport/{date}/{project}/{activity}/{status}/{desc}/{attachment}")
    void insertReport(@Path("date") String date,
                      @Path("project") String project,
                      @Path("activity") String activity,
                      @Path("status") int status,
                      @Path("desc") String desc,
                      @Path("attachment") String attachment,
                      Callback<StatusData> statusDataCallback);

    @Multipart
    @POST("/report/insertReport")
    void postReport(@Query("user_id") int user_id,
                    @Query("date") String date,
                    @Query("project") String project,
                    @Query("activity") String activity,
                    @Query("status") int status,
                    @Query("desc") String desc,
                    @Part("attachment") TypedFile attachment,
                    Callback<Response> responseCallback);

    @POST("/report/insertReportWithoutImage")
    void postReportWithoutImage(@Query("user_id") int user_id,
                    @Query("date") String date,
                    @Query("project") String project,
                    @Query("activity") String activity,
                    @Query("status") int status,
                    @Query("desc") String desc,
                    Callback<Response> responseCallback);

    @GET("/user/getUser")
    void getUser(@Query("email") String email,
                 @Query("password") String password,
                 Callback<UserData> userDataCallback);

    @POST("/user/createUser")
    void createUser(@Query("name") String name,
                    @Query("email") String email,
                    @Query("password") String password,
                    Callback<Response> responseCallback);

    @GET("/user/findUser")
    void findUser(@Query("email") String email,
                  Callback<Response> responseCallback);

    @Multipart
    @PUT("/report/updateReport")
    void updateReport(@Query("user_id") int user_id,
                      @Query("date") String date,
                      @Query("project") String project,
                      @Query("activity") String activity,
                      @Query("status") int status,
                      @Query("desc") String desc,
                      @Query("old_attachment") String old_attachment,
                      @Part("attachment") TypedFile attachment,
                      @Query("report_id") int report_id,
                      Callback<Response> responseCallback);

    @PUT("/report/updateReportWithoutImage")
    void updateReportWithoutImage(@Query("date") String date,
                           @Query("project") String project,
                           @Query("activity") String activity,
                           @Query("status") int status,
                           @Query("desc") String desc,
                           @Query("old_attachment") String old_attachment,
                           @Query("attachment") String attachment,
                           @Query("report_id") int report_id,
                           Callback<Response> responseCallback);

    @DELETE("/report/deleteReport")
    void deleteReport(@Query("report_id") int report_id, @Query("attachment") String attachment, Callback<Response> responseCallback);

    @DELETE("/report/deleteImage")
    void deleteImage(@Query("attachment") String attachment, Callback<Response> responseCallback);

}
