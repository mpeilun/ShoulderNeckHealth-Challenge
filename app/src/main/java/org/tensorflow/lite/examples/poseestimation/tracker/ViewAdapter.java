package org.tensorflow.lite.examples.poseestimation.tracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.poseestimation.MainActivity;
import org.tensorflow.lite.examples.poseestimation.R;

import java.util.ArrayList;

public class ViewAdapter  extends RecyclerView.Adapter<ViewAdapter.ViewHolder>
{

    private ArrayList<Action> mData = new ArrayList<>();

    public ViewAdapter(ArrayList<Action> data) {
        mData = data;
    }


    // 建立ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder{
        // 宣告元件
        private ImageView imageViewName, imageViewPic;

        ViewHolder(View itemView) {
            super(itemView);
            imageViewName = itemView.findViewById(R.id.imageView8);
            imageViewPic = itemView.findViewById(R.id.imageView9);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 連結項目布局檔list_item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // 設置txtItem要顯示的內容
        holder.itemView.setTag("" + position);
        Action action = mData.get(position);
        holder.imageViewName.setImageResource(action.getmName());
        holder.imageViewPic.setImageResource(action.getmThumbnail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                if (position == 0)
                    intent.putExtra("pose","up");
                else if (position == 1)
                    intent.putExtra("pose","down");
                else if (position == 2)
                    intent.putExtra("pose","left");
                else if (position == 3)
                    intent.putExtra("pose","right");
                v.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }
}
