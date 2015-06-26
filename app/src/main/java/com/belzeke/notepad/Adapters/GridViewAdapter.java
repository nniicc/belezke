package com.belzeke.notepad.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.belzeke.notepad.Models.GridViewHolder;
import com.belzeke.notepad.Models.Note;
import com.belzeke.notepad.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marko on 23.5.2015.
 */
public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private List<Note> items;

    public GridViewAdapter(Context mContext, List<Note> items) {
        this.mContext = mContext;
        if(items == null) items = new ArrayList<>();
        this.items = items;
    }

    public List<Note> getItems(){
        return items;
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
        return 0;
    }

    public void setItems(List<Note> items){
        if(items != null) {
            this.items = items;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        GridViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(R.layout.grid_view_item, parent, false);
            holder = new GridViewHolder();
            holder.mainImage = (ImageView) row.findViewById(R.id.gridViewImage);
            holder.uploadedImage = (ImageView) row.findViewById(R.id.gridViewUploaded);
            holder.durationText = (TextView)row.findViewById(R.id.gridViewVideoDuration);
            row.setTag(holder);
        }else{
            holder = (GridViewHolder) convertView.getTag();
        }


        ImageView imageView = new ImageView(mContext);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);

        imageView.setImageBitmap(items.get(position).getBitmap());

        if(!items.get(position).getDuration().equals("")){
            holder.durationText.setVisibility(View.VISIBLE);
            holder.durationText.setText(items.get(position).getDuration());
        }

        holder.mainImage.setImageDrawable(imageView.getDrawable());
        if(items.get(position).isUploaded()){
            holder.uploadedImage.setImageResource(R.drawable.check_mark);
        }else{
            holder.uploadedImage.setImageResource(R.drawable.cross_icon);
        }
        return row;
    }
}
