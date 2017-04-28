package com.example.wuzhi.intelligentmandarin.DataClass;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wuzhi.intelligentmandarin.DialogueActivity;
import com.example.wuzhi.intelligentmandarin.R;

import java.util.List;

/**
 * Created by wuzhi on 2017/4/15.
 */

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder> {
    private List<String> dialog;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView portrait;
        TextView content;

        public ViewHolder(View view) {
            super(view);
            portrait = (ImageView) view.findViewById(R.id.portrait);
            content = (TextView) view.findViewById(R.id.content);
        }
    }

    public DialogAdapter(List<String> strings) {
        dialog = strings;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialogue_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogueActivity.speak(dialog.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String s = dialog.get(position);
        if (position % 2 == 0) {
            holder.portrait.setImageResource(R.drawable.ic_system);
        } else {
            holder.portrait.setImageResource(R.drawable.ic_client);
        }
        holder.content.setText(s);
    }

    @Override
    public int getItemCount() {
        return dialog.size();
    }
}
