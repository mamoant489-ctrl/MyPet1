package com.example.mypet;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ViewHolder extends RecyclerView.ViewHolder {
    ImageView ivIcon, ivEdit, ivDelete;
    TextView tvName, tvDate, tvStatus;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        ivIcon = itemView.findViewById(R.id.ivIcon);
        tvName = itemView.findViewById(R.id.tvName);
        tvDate = itemView.findViewById(R.id.tvDate);
        tvStatus = itemView.findViewById(R.id.tvStatus);
        ivEdit = itemView.findViewById(R.id.ivEdit);
        ivDelete = itemView.findViewById(R.id.ivDelete);
    }
}

