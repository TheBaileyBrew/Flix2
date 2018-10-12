package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Review;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<Review> reviewList;
    private ReviewClickHandler clickHandler;

    public interface ReviewClickHandler {
        void onClick(View view, Review review);
    }

    public ReviewAdapter(Context context, List<Review> reviewList, ReviewClickHandler clickHandler) {
        this.layoutInflater = LayoutInflater.from(context);
        this.reviewList = reviewList;
        this.clickHandler = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.review_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Review currentReview = reviewList.get(position);
        holder.reviewDetails.setText(currentReview.getReviewContent());
        holder.reviewAuthor.setText(currentReview.getReviewAuthor());
    }

    @Override
    public int getItemCount() {
        if (reviewList == null) {
            return 0;
        } else {
            return reviewList.size();
        }
    }

    public void setReviewCollection(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView reviewDetails;
        final TextView reviewAuthor;

        private ViewHolder(View reviewView) {
            super(reviewView);
            reviewDetails = reviewView.findViewById(R.id.review_details);
            reviewAuthor = reviewView.findViewById(R.id.review_author);
            reviewView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Review currentReview = reviewList.get(getAdapterPosition());
            clickHandler.onClick(v, currentReview);
        }
    }
}
