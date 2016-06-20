package com.example.shashankkalra.popularmovies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.shashankkalra.popularmovies.R;
import com.example.shashankkalra.popularmovies.entities.ReviewVO;

import java.util.ArrayList;

/**
 * MovieVO Adapter class
 *
 * @author shashankkalra
 */
public class ReviewAdapter extends ArrayAdapter<ReviewVO>{
    LayoutInflater inflater;
    Context context;
    public ReviewAdapter(Context context, int id, ArrayList<ReviewVO> reviewVOs){
        super(context, id, reviewVOs);
        this.context=context;
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ReviewVO reviewVO =getItem(position);
        if(convertView==null){
            convertView=inflater.inflate(R.layout.review_item,parent,false);
        }

        TextView review=(TextView)convertView.findViewById(R.id.review_item);
        review.setText(reviewVO.getAuthor() + " -> \n\n" + reviewVO.getContent());
        return convertView;
    }

}
