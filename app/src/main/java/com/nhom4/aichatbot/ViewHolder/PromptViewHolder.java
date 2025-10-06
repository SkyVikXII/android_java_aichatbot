package com.nhom4.aichatbot.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Prompt;
import com.nhom4.aichatbot.R;

public class PromptViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName, textViewContent;
    public ImageButton buttonEdit, buttonDelete;
    public androidx.appcompat.widget.SwitchCompat switchPrompt;

    public PromptViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.TextView_prompt_name);
        textViewContent = itemView.findViewById(R.id.TextView_prompt_content);
        buttonEdit = itemView.findViewById(R.id.buttonPrompt_edit);
        buttonDelete = itemView.findViewById(R.id.buttonPrompt_delete);
        switchPrompt = itemView.findViewById(R.id.switchPrompt);
    }

    public void bind(Prompt prompt) {
        textViewName.setText(prompt.getName());
        String content = prompt.getContent();
        if (content != null && content.length() > 20) {
            content = content.substring(0, 20) + "...";
        }
        textViewContent.setText(content);
    }
}
