package com.nhom4.aichatbot.ViewHolder;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewMessage;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewMessage = itemView.findViewById(R.id.textViewMessage);
    }
}
