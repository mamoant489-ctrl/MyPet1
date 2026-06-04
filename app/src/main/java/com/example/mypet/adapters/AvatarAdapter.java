package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.R;
import com.example.mypet.models.Avatar;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    public interface OnAvatarClickListener {
        void onAvatarClick(Avatar avatar);
    }

    private final List<Avatar> avatars;
    private final OnAvatarClickListener listener;

    public AvatarAdapter(List<Avatar> avatars,
                         OnAvatarClickListener listener) {

        this.avatars = avatars;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_avatar,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Avatar avatar = avatars.get(position);

        holder.ivAvatar.setImageResource(
                avatar.getImageRes()
        );

        holder.itemView.setOnClickListener(v ->
                listener.onAvatarClick(avatar));
    }

    @Override
    public int getItemCount() {
        return avatars.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAvatar =
                    itemView.findViewById(R.id.ivAvatar);
        }
    }
}