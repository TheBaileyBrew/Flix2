package com.thebaileybrew.flix2.interfaces.adapters;

import com.thebaileybrew.flix2.models.Credit;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class CreditsDiffCallback extends DiffUtil.Callback {

    private final List<Credit> mOldList;
    private final List<Credit> mNewList;

    public CreditsDiffCallback(List<Credit> mOldList, List<Credit> mNewList) {
        this.mOldList = mOldList;
        this.mNewList = mNewList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).getCreditID() == mNewList.get(newItemPosition).getCreditID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Credit oldCredit = mOldList.get(oldItemPosition);
        final Credit newCredit = mNewList.get(newItemPosition);

        return oldCredit.getCreditID() == newCredit.getCreditID();
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
