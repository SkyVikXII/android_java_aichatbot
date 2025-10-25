package com.nhom4.aichatbot.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Chat;
import com.nhom4.aichatbot.R;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName, textViewDescription, textViewDateCreate, textViewDateUpdate;
    public ImageButton buttonEdit, buttonDelete;


    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.textViewChatName);
        textViewDescription = itemView.findViewById(R.id.textViewChatDescription);
        textViewDateCreate = itemView.findViewById(R.id.textViewChatDatecreate);
        textViewDateUpdate = itemView.findViewById(R.id.textViewChatDateupdate);
        buttonEdit = itemView.findViewById(R.id.buttonChatEdit);
        buttonDelete = itemView.findViewById(R.id.buttonChatDelete);
    }

    public void bind(Chat chat) {
        textViewName.setText(chat.getName());
        textViewDescription.setText(chat.getDescription());
        textViewDateCreate.setText("Ngày tạo :"+chat.getDateCreate());
        textViewDateUpdate.setText("Ngày sửa :"+chat.getDateUpdate());
    }
}
