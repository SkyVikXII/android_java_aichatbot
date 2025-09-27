package com.nhom4.aichatbot.ViewHolder;


import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Endpoint;
import com.nhom4.aichatbot.R;

public class EndpointViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName, textViewUrl;
    public ImageButton buttonEdit, buttonDelete;
    public androidx.appcompat.widget.SwitchCompat switchEndpoint;

    public EndpointViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.TextView_endpoint_title);
        textViewUrl = itemView.findViewById(R.id.TextView_endpoint_url);
        buttonEdit = itemView.findViewById(R.id.buttonEndpoint_edit);
        buttonDelete = itemView.findViewById(R.id.buttonEndpoint_delete);
        switchEndpoint = itemView.findViewById(R.id.switchEndpoint);
    }

    public void bind(Endpoint endpoint) {
        textViewName.setText(endpoint.getName());
        textViewUrl.setText(endpoint.getEndpoint_url());
    }
}
