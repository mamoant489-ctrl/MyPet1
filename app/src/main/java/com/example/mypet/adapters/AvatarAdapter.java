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

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.Holder> {

    public interface OnAvatarClick {
        void onClick(Avatar avatar);
    }

    private final List<Avatar> list;
    private final OnAvatarClick listener;

    public AvatarAdapter(List<Avatar> list,
                         OnAvatarClick listener) {

        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_avatar,
                        parent,
                        false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull Holder holder,
            int position) {

        Avatar avatar = list.get(position);

        holder.image.setImageResource(
                avatar.getImageRes());

        holder.itemView.setOnClickListener(v ->
                listener.onClick(avatar));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        ImageView image;

        public Holder(@NonNull View itemView) {
            super(itemView);

            image =
                    itemView.findViewById(
                            R.id.ivAvatar);
        }
    }
}