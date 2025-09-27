package com.nhom4.aichatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.R;
import com.nhom4.aichatbot.ViewHolder.CharacterViewHolder;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterViewHolder> {

    private List<Character> characterList;
    private OnCharacterClickListener listener;
    private Context context;

    public CharacterAdapter(Context context, List<Character> characterList, OnCharacterClickListener listener) {
        this.context = context;
        this.characterList = characterList;
        this.listener = listener;
    }

    public void updateData(List<Character> newCharacterList) {
        this.characterList = newCharacterList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.character_item, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        Character character = characterList.get(position);
        holder.bind(character);

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(character);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(character);
            }
        });
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    public interface OnCharacterClickListener {
        void onEditClick(Character character);
        void onDeleteClick(Character character);
    }
}