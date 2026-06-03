package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mypet.R;
import com.example.mypet.models.PetModel;

import java.util.List;

public class PetSwitcherAdapter
        extends RecyclerView.Adapter<PetSwitcherAdapter.Holder> {

    public interface Listener{
        void onPetClick(PetModel pet);
    }

    private final List<PetModel> list;
    private final Listener listener;

    public PetSwitcherAdapter(
            List<PetModel> list,
            Listener listener) {

        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_pet_switch,
                                parent,
                                false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull Holder holder,
            int position) {

        PetModel pet = list.get(position);

        holder.tvName.setText(
                pet.getName());

        holder.tvAge.setText(
                pet.getAge());

        Glide.with(holder.itemView.getContext())
                .load(pet.getPhotoUrl())
                .placeholder(R.drawable.usericon)
                .into(holder.ivPet);

        holder.itemView.setOnClickListener(v ->
                listener.onPetClick(pet));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        ImageView ivPet;
        TextView tvName;
        TextView tvAge;

        public Holder(@NonNull View itemView) {
            super(itemView);

            ivPet = itemView.findViewById(R.id.ivPet);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
        }
    }
}