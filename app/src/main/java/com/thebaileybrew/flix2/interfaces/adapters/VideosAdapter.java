package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Videos;
import com.thebaileybrew.flix2.utils.UrlUtils;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<Videos> videoCollection;
    private RecyclerView videoRecycler;
    private VideoClickHandler clickHandler;

    public interface VideoClickHandler {
        void onClick(View view, Videos video);
    }

    public VideosAdapter(Context context, List<Videos> videoCollection, VideoClickHandler clickHandler) {
        this.layoutInflater = LayoutInflater.from(context);
        this.videoCollection = videoCollection;
        this.clickHandler = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.video_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Videos currentVideo = videoCollection.get(position);
        String videoImagePath = UrlUtils.buildYoutubeImageUrl(currentVideo.getVideoKey());
        holder.videoTitle.setText(currentVideo.getVideoName());
        holder.videoSize.setText(currentVideo.getVideoSize());
        holder.videoType.setText(currentVideo.getVideoType());
        Picasso.get()
                .load(videoImagePath)
                .placeholder(R.drawable.flix_logo)
                .into(holder.videoThumbnail);
    }

    @Override
    public int getItemCount() {
        if (videoCollection == null) {
            return 0;
        } else {
            return videoCollection.size();
        }
    }

    public void setVideoCollection(List<Videos> videoCollection) {
        this.videoCollection = videoCollection;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView videoThumbnail;
        final TextView videoTitle;
        final TextView videoType;
        final TextView videoSize;

        private ViewHolder(View videoView) {
            super(videoView);
            videoThumbnail = videoView.findViewById(R.id.video_trailer);
            videoTitle = videoView.findViewById(R.id.video_name);
            videoType = videoView.findViewById(R.id.video_type);
            videoSize = videoView.findViewById(R.id.video_size);
            videoView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Videos currentVideo = videoCollection.get(getAdapterPosition());
            clickHandler.onClick(v, currentVideo);
        }
    }
}
