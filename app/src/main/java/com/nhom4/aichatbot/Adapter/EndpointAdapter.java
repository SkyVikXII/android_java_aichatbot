package com.nhom4.aichatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom4.aichatbot.Models.Endpoint;
import com.nhom4.aichatbot.R;
import com.nhom4.aichatbot.ViewHolder.EndpointViewHolder;
import java.util.List;

public class EndpointAdapter extends RecyclerView.Adapter<EndpointViewHolder> {

    private List<Endpoint> endpointList;
    private OnEndpointClickListener listener;
    private Context context;

    public EndpointAdapter(Context context, List<Endpoint> endpointList, OnEndpointClickListener listener) {
        this.context = context;
        this.endpointList = endpointList;
        this.listener = listener;
    }

    public void updateData(List<Endpoint> newEndpointsList) {
        this.endpointList = newEndpointsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EndpointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.endpoint_item, parent, false);
        return new EndpointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EndpointViewHolder holder, int position) {
        Endpoint endpoint = endpointList.get(position);
        holder.bind(endpoint);

        boolean isSystem = endpoint.getId() != null && endpoint.getId().startsWith("system_");

        holder.switchEndpoint.setOnCheckedChangeListener(null);
        holder.switchEndpoint.setChecked(endpoint.isActive());

        if (isSystem) {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.GONE);
        } else {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(endpoint);
                }
            });
            holder.buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(endpoint);
                }
            });
        }
        
        holder.switchEndpoint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onActivateClick(endpoint, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return endpointList.size();
    }

    public interface OnEndpointClickListener {
        void onEditClick(Endpoint endpoint);
        void onDeleteClick(Endpoint endpoint);
        void onActivateClick(Endpoint endpoint, boolean isActive);
    }
}