package com.solusi247.reportmanagement.adapter.rvAdapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.activity.FullScreenViewActivity;
import com.solusi247.reportmanagement.interfaces.EndListListener;
import com.solusi247.reportmanagement.interfaces.OnLoadMoreListener;
import com.solusi247.reportmanagement.model.ReportData;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by chy47 on 21/06/16.
 */
public class RVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Context context;
    List<ReportData> reportDataList;

    //variables for pagination
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_END = 2;

    private OnLoadMoreListener onLoadMoreListener;
    private EndListListener endListListener;
    private boolean isLoading;
    private boolean isEndList;

    private int visibleThreshold;
    private int lastVisibleItem, totalItemCount;

    private int position;
    private int report_id;
    private String date;
    private String activity;
    private String project;
    private String desc;
    private String attachment;
    private int status;

    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;


    public RVAdapter(Context context, List<ReportData> reportDataList, RecyclerView recyclerView) {
        this.context = context;
        this.reportDataList = reportDataList;
        this.recyclerView = recyclerView;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        progressDialog = new ProgressDialog(context);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                visibleThreshold = recyclerView.getChildCount();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        Log.d("Fetching report adapter", "memuat tambahan data");
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setEndListListener(EndListListener endListListener) {
        this.endListListener = endListListener;
    }

    @Override
    public int getItemViewType(int position) {
//        Toast.makeText(context, "itemViewType = " + position, Toast.LENGTH_SHORT).show();
        return reportDataList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        CardView cvReport;
        TextView tvDate;
        TextView tvProject;
        TextView tvActivity;
        TextView tvStatus;
        TextView tvDesc;
        ImageView ivImage;

        boolean isImageFitToScreen;

        public ReportViewHolder(View itemView) {
            super(itemView);
            cvReport = (CardView)itemView.findViewById(R.id.cvReport);
            tvDate = (TextView)itemView.findViewById(R.id.tvDate);
            tvProject = (TextView)itemView.findViewById(R.id.tvProject);
            tvActivity = (TextView)itemView.findViewById(R.id.tvActivity);
            tvStatus = (TextView)itemView.findViewById(R.id.tvStatus);
            tvDesc = (TextView)itemView.findViewById(R.id.tvDesc);
            ivImage = (ImageView)itemView.findViewById(R.id.ivImage);

            cvReport.setOnCreateContextMenuListener(this);

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(Menu.NONE, 0, Menu.NONE, "Edit");
            contextMenu.add(Menu.NONE, 1, Menu.NONE, "Remove");
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbarBottom);
        }
    }

    static class EndListViewHolder extends  RecyclerView.ViewHolder{
        public LinearLayout linearLayout;

        public EndListViewHolder(View itemView) {
            super(itemView);
            linearLayout =(LinearLayout) itemView.findViewById(R.id.layout_endList);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_main,parent,false);
            return new ReportViewHolder(v);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading, parent, false);
            return new LoadingViewHolder(v);
        } else if (viewType == VIEW_TYPE_END) { //NOT BEING USED
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.end_list, parent, false);
            return new EndListViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        final Context context = holder.itemView.getContext();
        if (holder instanceof ReportViewHolder) {
            final ReportViewHolder reportViewHolder = (ReportViewHolder) holder;
            final String unformattedDate = reportDataList.get(i).getDate();
            SimpleDateFormat curFormater = new SimpleDateFormat("dd-MM-yyyy");
            Date dateObj = null;
            try
            {
                dateObj = curFormater.parse(unformattedDate);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            SimpleDateFormat postFormater = new SimpleDateFormat("EEE, dd MMM yyyy");

            String formattedDate = postFormater.format(dateObj);

            final int report_id = reportDataList.get(i).getReport_id();
            final String date = formattedDate;
            final String activity = reportDataList.get(i).getActivity();
            final String project = reportDataList.get(i).getProject();
            final String desc = reportDataList.get(i).getDesc();
            final String attachment = reportDataList.get(i).getAttachment();
            final int status = reportDataList.get(i).getStatus();
            final String statusString;
            if (status == 0) {
                statusString = "In Progress";
            } else {
                statusString = "Done";
            }

            reportViewHolder.tvDate.setText(formattedDate);
            reportViewHolder.tvProject.setText(project);
            reportViewHolder.tvActivity.setText(", " + activity);
            int projectTextLength = project.length();
            int activityTextLength = activity.length();
            if(projectTextLength + activityTextLength > 25 ){
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, R.id.tvProject);
                reportViewHolder.tvActivity.setLayoutParams(params);
            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.RIGHT_OF, R.id.tvProject);
                reportViewHolder.tvActivity.setLayoutParams(params);
            }

            reportViewHolder.tvStatus.setText(statusString);
            reportViewHolder.tvDesc.setText(desc);

            if(attachment==null||attachment.equals("null")){
                reportViewHolder.ivImage.setVisibility(View.GONE);
            } else {
                reportViewHolder.ivImage.setVisibility(View.VISIBLE);
            }

            if (attachment!=null) {
//                String imageDir = "http://192.168.1.44:8081/projectManagementApi/uploads/"; //laptop Edityo
                String imageDir = "http://192.168.1.228:8080/images/"; //Server solusi

                Glide.with(context).load(imageDir+attachment).into(reportViewHolder.ivImage);
                reportViewHolder.ivImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.show();
                        progressDialog.setMessage("Opening image");
                        Intent intent = new Intent(context, FullScreenViewActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("attachment", attachment);
                        intent.putExtras(extras);
                        context.startActivity(intent);
                        progressDialog.dismiss();
                    }
                });
            }

            reportViewHolder.cvReport.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    setPosition(reportViewHolder.getAdapterPosition());
                    setReport_id(report_id);
                    setDate(unformattedDate);
                    setProject(project);
                    setActivity(activity);
                    setDesc(desc);
                    setAttachment(attachment);
                    setStatus(status);
                    return false;
                }
            });

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }


    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void setEnd() {
        isEndList = false;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getReport_id() {
        return report_id;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getItemCount() {
        return reportDataList == null ? 0 : reportDataList.size();
    }


//    @Override
//    public void onViewRecycled(RecyclerView.ViewHolder holder) {
//        holder.itemView.setOnLongClickListener(null);
//        super.onViewRecycled(holder);
//    }
}
