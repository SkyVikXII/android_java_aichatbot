package com.nhom4.aichatbot.ViewHolder;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Chat;
import com.nhom4.aichatbot.R;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName, textViewDescription;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.textViewChatName);
        textViewDescription = itemView.findViewById(R.id.textViewChatDescription);
    }

    public void bind(Chat chat) {
        textViewName.setText(chat.getName());
        textViewDescription.setText(chat.getDescription());
    }
}
