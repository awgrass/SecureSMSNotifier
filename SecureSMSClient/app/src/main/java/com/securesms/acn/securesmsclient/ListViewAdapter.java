package com.securesms.acn.securesmsclient;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        final Server server = getItem(position);
        if (server == null)
            return rowView;
        final TextView title = rowView.findViewById(R.id.listViewItemFirstLine);
        final TextView description = rowView.findViewById(R.id.listViewItemSecondLine);
        final ImageView imageViewNotebook = rowView.findViewById(R.id.listViewItemIconNotebook);
        final ImageView imageViewPc = rowView.findViewById(R.id.listViewItemIconPc);
        final ImageView imageViewEnable = rowView.findViewById(R.id.listViewItemIconEnable);
        final ImageView imageViewDisable = rowView.findViewById(R.id.listViewItemIconDisable);
        ImageView imageViewEdit = rowView.findViewById(R.id.listViewItemIconEdit);
        ImageView imageViewDelete = rowView.findViewById(R.id.listViewItemIconDelete);
        imageViewDelete.getDrawable().setColorFilter(
                activity.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        title.setText(server.getName());
        description.setText(server.getIp());
        if (server.getType() == Server.ServerType.NOTEBOOK) {
            imageViewNotebook.setVisibility(View.VISIBLE);
            imageViewPc.setVisibility(View.INVISIBLE);
        } else {
            imageViewNotebook.setVisibility(View.INVISIBLE);
            imageViewPc.setVisibility(View.VISIBLE);
        }
        if (AppData.editServer) {
            imageViewEdit.setVisibility(View.VISIBLE);
            imageViewDelete.setVisibility(View.VISIBLE);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.setServerEnabled(activity, server, !server.isEnabled());
                    updateEnabledIcons(server, imageViewEnable, imageViewDisable, imageViewNotebook, imageViewPc, title, description);
                }
            };
            imageViewEnable.setOnClickListener(listener);
            imageViewDisable.setOnClickListener(listener);
            imageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.renameServer(activity, server, ((MainActivity) activity).listViewAdapter);
                }
            });
            imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.deleteServer(activity, server, ((MainActivity) activity).listViewAdapter);
                }
            });

            updateEnabledIcons(server, imageViewEnable, imageViewDisable, imageViewNotebook, imageViewPc, title, description);
        } else {
            updateEnabledIcons(server, imageViewEnable, imageViewDisable, imageViewNotebook, imageViewPc, title, description);

            imageViewEdit.setVisibility(View.INVISIBLE);
            imageViewDelete.setVisibility(View.INVISIBLE);
            imageViewEnable.setVisibility(View.INVISIBLE);
            imageViewDisable.setVisibility(View.INVISIBLE);
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

    private void updateEnabledIcons(Server server, ImageView imageViewEnable, ImageView imageViewDisable,
                                    ImageView imageViewNotebook, ImageView imageViewPc, TextView title, TextView description) {
        float alpha = 1;
        if (server.isEnabled()) {
            imageViewEnable.setVisibility(View.VISIBLE);
            imageViewDisable.setVisibility(View.INVISIBLE);
            title.setTypeface(null, Typeface.BOLD);
        } else {
            imageViewEnable.setVisibility(View.INVISIBLE);
            imageViewDisable.setVisibility(View.VISIBLE);
            title.setTypeface(null, Typeface.ITALIC);
            alpha = (float) 0.5;
        }
        imageViewNotebook.setAlpha(alpha);
        imageViewPc.setAlpha(alpha);
        title.setAlpha(alpha);
        description.setAlpha(alpha);
    }
}