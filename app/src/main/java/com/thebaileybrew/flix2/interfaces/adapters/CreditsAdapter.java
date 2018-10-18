package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Credit;
import com.thebaileybrew.flix2.utils.UrlUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class CreditsAdapter extends RecyclerView.Adapter<CreditsAdapter.ViewHolder> {
    private final static String TAG = CreditsAdapter.class.getSimpleName();

    private static final int UNSELECTED = -1;

    private int selectedItem = UNSELECTED;


    private final LayoutInflater layoutInflater;
    private final RecyclerView recyclerView;
    private List<Credit> creditList;

    public CreditsAdapter(Context context, List<Credit> creditList, RecyclerView recyclerView) {
        this.layoutInflater = LayoutInflater.from(context);
        this.recyclerView = recyclerView;
        this.creditList = creditList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.credit_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Credit currentCredit = creditList.get(position);
        holder.updateItem(position);
        holder.setIsRecyclable(false);
        holder.expandableLayout.setExpanded(false);
        holder.expandableLayout.setVisibility(View.INVISIBLE);
        holder.characterName.setText(currentCredit.getCreditCharacterName());
        holder.characterActor.setText(currentCredit.getCreditActorName());
        String actorImagePath = UrlUtils.buildCreditImageUrl(currentCredit.getCreditPath());
        Picasso.get().load(actorImagePath)
                .placeholder(R.drawable.flix_logo)
                .into(holder.characterImage);

    }


    @Override
    public int getItemCount() {
        if (creditList == null) {
            return 0;
        } else {
            return creditList.size();
        }
    }

    public void setCreditCollection(List<Credit> creditReturn) {
        creditList = creditReturn;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ExpandableLayout.OnExpansionUpdateListener{
        final ExpandableLayout expandableLayout;
        final TextView characterName;
        final TextView characterActor;
        final ImageView characterImage;

        private ViewHolder(View creditView) {
            super(creditView);
            expandableLayout = creditView.findViewById(R.id.expandable_credit_view);
            expandableLayout.setExpanded(false);
            expandableLayout.setOnExpansionUpdateListener(this);
            characterName = creditView.findViewById(R.id.credit_character_name);
            characterActor = creditView.findViewById(R.id.credit_actor_name);
            characterImage = creditView.findViewById(R.id.credit_actor_image);
            characterImage.setOnClickListener(this);
        }

        private void updateItem(final int position) {
            boolean isSelected = position == selectedItem;
            characterImage.setSelected(isSelected);
            expandableLayout.setExpanded(isSelected, false);
        }

        @Override
        public void onClick(View v) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(selectedItem);
            if (holder != null) {
                characterImage.setSelected(false);
                expandableLayout.collapse();
            }

            int position = getAdapterPosition();
            if (position == selectedItem) {
                selectedItem = UNSELECTED;
            } else {
                characterImage.setSelected(true);
                expandableLayout.expand();
                selectedItem = position;
            }
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            if (state == ExpandableLayout.State.EXPANDING) {
                Log.e(TAG, "onExpansionUpdate: item is expanding" );
            }

        }
    }

}
