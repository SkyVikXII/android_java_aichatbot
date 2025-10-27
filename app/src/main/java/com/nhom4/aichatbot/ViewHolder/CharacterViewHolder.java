package com.nhom4.aichatbot.ViewHolder;


import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.R;

public class CharacterViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName, textViewDescription, textViewDateCreate, textViewDateUpdate;
    public ImageButton buttonEdit, buttonDelete;

    public CharacterViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.TextView_character_name);
        textViewDateCreate = itemView.findViewById(R.id.TextView_date_create);
        textViewDateUpdate = itemView.findViewById(R.id.TextView_date_update);
        buttonEdit = itemView.findViewById(R.id.buttonEdit);
        buttonDelete = itemView.findViewById(R.id.buttonDelete);
    }

    public void bind(Character character) {
        textViewName.setText(character.getName());
        textViewDateCreate.setText("Ngày tạo :"+character.getDatecreate());
        textViewDateUpdate.setText("Ngày sửa :"+character.getDateupdate());
    }
}
