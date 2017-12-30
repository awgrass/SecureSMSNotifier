package com.securesms.acn.securesmsclient;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

class ListViewAdapter extends ArrayAdapter<Server> {

    private HashMap<Server, Integer> map = new HashMap<>();
    private Activity activity;

    public ListViewAdapter(Activity activity, int resourceId, List<Server> serverList) {
        super(activity, resourceId, serverList);
        for (int i = 0; i < serverList.size(); ++i) {
            map.put(serverList.get(i), i);
        }
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater == null)
            return new View(getContext());
        View rowView;
        if(convertView != null)
            rowView = convertView;
        else
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        final Server server = getItem(position);
        if(server == null)
            return rowView;
        final ItemHolder holder = ItemHolder.byView(rowView);
        holder.imageViewDelete.getDrawable().setColorFilter(
                activity.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        holder.title.setText(server.getName());
        holder.description.setText(server.getIp());
        if (server.getType() == Server.ServerType.NOTEBOOK) {
            holder.imageViewNotebook.setVisibility(View.VISIBLE);
            holder.imageViewPc.setVisibility(View.INVISIBLE);
        } else {
            holder.imageViewNotebook.setVisibility(View.INVISIBLE);
            holder.imageViewPc.setVisibility(View.VISIBLE);
        }
        if (AppData.editServer) {
            holder.imageViewEdit.setVisibility(View.VISIBLE);
            holder.imageViewDelete.setVisibility(View.VISIBLE);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.setServerEnabled(activity, server, !server.isEnabled());
                    updateEnabledIcons(server, holder);
                }
            };
            holder.imageViewEnable.setOnClickListener(listener);
            holder.imageViewDisable.setOnClickListener(listener);
            holder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.renameServer(activity, server, ((MainActivity) activity).listViewAdapter);
                }
            });
            holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.deleteServer(activity, server, ((MainActivity) activity).listViewAdapter);
                }
            });

            updateEnabledIcons(server, holder);
        } else {
            updateEnabledIcons(server, holder);

            holder.imageViewEdit.setVisibility(View.INVISIBLE);
            holder.imageViewDelete.setVisibility(View.INVISIBLE);
            holder.imageViewEnable.setVisibility(View.INVISIBLE);
            holder.imageViewDisable.setVisibility(View.INVISIBLE);
        }
        return rowView;
    }

    @Override
    public long getItemId(int position) {
        Server item = getItem(position);
        try {
            return map.get(item);
        } catch (NullPointerException e) {
            return -1;
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void updateEnabledIcons(Server server, ItemHolder holder) {
        float alpha = 1;
        if (server.isEnabled()) {
            holder.imageViewEnable.setVisibility(View.VISIBLE);
            holder.imageViewDisable.setVisibility(View.INVISIBLE);
            holder.title.setTypeface(null, Typeface.BOLD);
        } else {
            holder.imageViewEnable.setVisibility(View.INVISIBLE);
            holder.imageViewDisable.setVisibility(View.VISIBLE);
            holder.title.setTypeface(null, Typeface.ITALIC);
            alpha = (float) 0.5;
        }
        holder.imageViewNotebook.setAlpha(alpha);
        holder.imageViewPc.setAlpha(alpha);
        holder.title.setAlpha(alpha);
        holder.description.setAlpha(alpha);
    }

    static class ItemHolder {
        TextView title;
        TextView description;
        ImageView imageViewNotebook;
        ImageView imageViewPc;
        ImageView imageViewEnable;
        ImageView imageViewDisable;
        ImageView imageViewEdit;
        ImageView imageViewDelete;

        static ItemHolder byView(View view) {
            ItemHolder holder = new ItemHolder();
            holder.title = view.findViewById(R.id.listViewItemFirstLine);
            holder.description = view.findViewById(R.id.listViewItemSecondLine);
            holder.imageViewNotebook = view.findViewById(R.id.listViewItemIconNotebook);
            holder.imageViewPc = view.findViewById(R.id.listViewItemIconPc);
            holder.imageViewEnable = view.findViewById(R.id.listViewItemIconEnable);
            holder.imageViewDisable = view.findViewById(R.id.listViewItemIconDisable);
            holder.imageViewEdit = view.findViewById(R.id.listViewItemIconEdit);
            holder.imageViewDelete = view.findViewById(R.id.listViewItemIconDelete);
            return holder;
        }
    }
}