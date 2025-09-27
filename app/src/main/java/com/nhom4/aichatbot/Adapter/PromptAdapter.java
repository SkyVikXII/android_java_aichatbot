package com.nhom4.aichatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Prompt;
import com.nhom4.aichatbot.R;
import com.nhom4.aichatbot.ViewHolder.PromptViewHolder;
import java.util.List;

public class PromptAdapter extends RecyclerView.Adapter<PromptViewHolder> {

    private List<Prompt> promptList;
    private OnPromptClickListener listener;
    private Context context;

    public PromptAdapter(Context context, List<Prompt> promptList, OnPromptClickListener listener) {
        this.context = context;
        this.promptList = promptList;
        this.listener = listener;
    }

    public void updateData(List<Prompt> newPromptList) {
        this.promptList = newPromptList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.prompt_item, parent, false);
        return new PromptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromptViewHolder holder, int position) {
        Prompt prompt = promptList.get(position);
        holder.bind(prompt);

        holder.switchPrompt.setOnCheckedChangeListener(null);
        holder.switchPrompt.setChecked(prompt.isActive());

        if (prompt.isDefault()) {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.GONE);
        } else {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(prompt);
                }
            });
            holder.buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(prompt);
                }
            });
        }

        holder.switchPrompt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onActivateClick(prompt, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return promptList.size();
    }

    public interface OnPromptClickListener {
        void onEditClick(Prompt prompt);
        void onDeleteClick(Prompt prompt);
        void onActivateClick(Prompt prompt, boolean isActive);
    }
}
