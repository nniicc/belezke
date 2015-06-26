package com.belzeke.notepad.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Models.ListViewHolder;
import com.belzeke.notepad.Models.Note;
import com.belzeke.notepad.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by marko on 8.6.2015.
 */
public class ListViewAdapter extends BaseAdapter implements Filterable {

    private final Integer gridType;
    private Context mContext;
    private List<Note> items;
    private List<Note> Iitems;

    public ListViewAdapter(Context mContext, List<Note> items, Integer gridType) {
        this.mContext = mContext;
        if(items == null) {
            items = new ArrayList<>();
        }
        this.items = items;
        this.Iitems = items;
        this.gridType = gridType;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Note getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    public void setItems(List<Note> items){
        if(items != null) {
            this.items = items;
            this.Iitems = items;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ListViewHolder holder;


        if(convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(R.layout.list_view_item, parent, false);
            holder = new ListViewHolder();
            holder.text = (TextView) row.findViewById(R.id.file_name_text);
            holder.audioLenght = (TextView) row.findViewById(R.id.audio_length);
            holder.uploaded = (ImageView) row.findViewById(R.id.list_view_uploaded_img);
            row.setTag(holder);
        }else{
            holder = (ListViewHolder) convertView.getTag();
        }

        if(gridType == AppConfig.NOTE_AUDIO_TYPE){
            holder.audioLenght.setVisibility(View.VISIBLE);
            File file = new File(items.get(position).getNoteName());
            if(file.exists()){
                holder.audioLenght.setText(items.get(position).getDuration());
                holder.text.setText(file.getName());
            }
        }else if(gridType == AppConfig.NOTE_TEXT_TYPE){
            holder.audioLenght.setVisibility(View.GONE);
        }

        if(items.get(position).isUploaded()){
            holder.uploaded.setImageResource(R.drawable.check_mark);
        }else{
            holder.uploaded.setImageResource(R.drawable.cross_icon);
        }
        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase(new Locale("Si"));

                final FilterResults results = new FilterResults();

                final List<Note> orig = Iitems;

                final List<Note> list = new ArrayList<>();

                String searchString;
                for (int i=0;i<orig.size();i++){
                    searchString = orig.get(i).getNoteName();
                    if(searchString.toLowerCase(new Locale("Si")).contains(filterString)){
                        list.add(orig.get(i));
                    }
                }
                results.count = orig.size();
                results.values = list;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (List<Note>)results.values;
                notifyDataSetChanged();
            }
        };
    }
}
