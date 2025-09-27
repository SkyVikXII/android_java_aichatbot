package com.nhom4.aichatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Model;
import com.nhom4.aichatbot.R;
import com.nhom4.aichatbot.ViewHolder.ModelViewHolder;
import java.util.List;

public class ModelAdapter extends RecyclerView.Adapter<ModelViewHolder> {

    private List<Model> modelList;
    private OnModelClickListener listener;
    private Context context;

    public ModelAdapter(Context context, List<Model> modelList, OnModelClickListener listener) {
        this.context = context;
        this.modelList = modelList;
        this.listener = listener;
    }

    public void updateData(List<Model> newModelList) {
        this.modelList = newModelList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_item, parent, false);
        return new ModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {
        Model model = modelList.get(position);
        holder.bind(model);

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(model);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public interface OnModelClickListener {
        void onEditClick(Model model);
        void onDeleteClick(Model model);
    }
}
