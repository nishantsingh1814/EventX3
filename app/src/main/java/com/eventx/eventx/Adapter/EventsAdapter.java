package com.eventx.eventx.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventx.eventx.Model.EventModel;
import com.eventx.eventx.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import at.markushi.ui.CircleButton;

/**
 * Created by Nishant on 5/10/2017.
 */

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsHolder> {

    ArrayList<EventModel> eventModels;
    Context mContext;
    LayoutInflater inflater;

    public EventsAdapter(ArrayList<EventModel> mCast, Context mContext) {
        this.eventModels = mCast;
        this.inflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
    }

    @Override
    public EventsHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = inflater.inflate(R.layout.event_row, parent, false);

        return new EventsHolder(v);
    }

    @Override
    public void onBindViewHolder(EventsHolder holder, int position) {
        Log.i("tagh", "onResponse: "+position);

        EventModel model=eventModels.get(position);
        holder.post_category.setText(model.getCats()[0]);
        holder.post_name.setText(model.getTitle());
        holder.post_location.setText(model.getVenue().getName()+","+model.getCity());
        Picasso.with(mContext).load(model.getImg_url()).into(holder.post_image);
    }

    @Override
    public int getItemCount() {
        return eventModels.size();
    }

    class EventsHolder extends RecyclerView.ViewHolder {

        private TextView post_name;
        private CircleButton mLikeBtn;
        private ImageView post_image;
        private TextView post_location;
        private TextView post_category;
        private TextView post_start_date_time;
        private CircleButton mShareBtn;


        public EventsHolder(View itemView) {
            super(itemView);

            post_name = (TextView) itemView.findViewById(R.id.post_name);
            mLikeBtn = (CircleButton) itemView.findViewById(R.id.like_btn);
            mShareBtn = (CircleButton) itemView.findViewById(R.id.share_btn);
            post_start_date_time = (TextView) itemView.findViewById(R.id.post_start_date_time);
            post_location = (TextView) itemView.findViewById(R.id.post_location);
            post_category = (TextView) itemView.findViewById(R.id.post_category);
            post_image = (ImageView) itemView.findViewById(R.id.post_image);
        }

    }
}
