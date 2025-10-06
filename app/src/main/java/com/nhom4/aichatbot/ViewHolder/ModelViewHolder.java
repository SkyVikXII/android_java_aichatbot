package com.nhom4.aichatbot.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Model;
import com.nhom4.aichatbot.R;

public class ModelViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName, textViewDescription;
    public ImageButton buttonEdit, buttonDelete;
    public androidx.appcompat.widget.SwitchCompat switchModel;

    public ModelViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.TextView_model_name);
        textViewDescription = itemView.findViewById(R.id.TextView_model_description);
        buttonEdit = itemView.findViewById(R.id.buttonModel_edit);
        buttonDelete = itemView.findViewById(R.id.buttonModel_delete);
        switchModel = itemView.findViewById(R.id.switchModel);
    }

    public void bind(Model model) {
        textViewName.setText(model.getName());
        String description = model.getDescription();
        /*if (description != null && description.length() > 30) {
            description = description.substring(0, 30) + "...";
        }*/
        textViewDescription.setText(description);
    }
}
