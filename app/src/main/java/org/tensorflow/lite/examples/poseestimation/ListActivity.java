package org.tensorflow.lite.examples.poseestimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;

import org.tensorflow.lite.examples.poseestimation.tracker.Action;
import org.tensorflow.lite.examples.poseestimation.tracker.ViewAdapter;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ImageButton item1;
    ImageButton item2;
    ImageButton item3;
    ImageButton item4;
    ViewAdapter mAdapter;
    RecyclerView mRecyclerView;
    ArrayList<Action> mDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
//
//        item1 = findViewById(R.id.item1);
//        item1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ListActivity.this, MainActivity.class);
//                intent.putExtra("pose","up");
//                startActivity(intent);
//            }
//        });
//
//        item2 = findViewById(R.id.item2);
//        item2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ListActivity.this, MainActivity.class);
//                intent.putExtra("pose","down");
//                startActivity(intent);
//            }
//        });
//
//        item3 = findViewById(R.id.item3);
//        item3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ListActivity.this, MainActivity.class);
//                intent.putExtra("pose","left");
//                startActivity(intent);
//            }
//        });
//
//        item4 = findViewById(R.id.item4);
//        item4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ListActivity.this, MainActivity.class);
//                intent.putExtra("pose","right");
//                startActivity(intent);
//            }
//        });

        mRecyclerView = findViewById(R.id.recyclerView);

        mDataset = new ArrayList<Action>();
        Action action = new Action();
        action.setmName(R.drawable.action1);
        action.setmThubnail(R.drawable.acitem1);
        mDataset.add(action);
        Action action1 = new Action();
        action1.setmName(R.drawable.action2);
        action1.setmThubnail(R.drawable.acitem2);
        mDataset.add(action1);
        Action action2 = new Action();
        action2.setmName(R.drawable.action3);
        action2.setmThubnail(R.drawable.acitem3);
        mDataset.add(action2);
        Action action3 = new Action();
        action3.setmName(R.drawable.action4);
        action3.setmThubnail(R.drawable.acitem4);
        mDataset.add(action3);

        // 設置RecyclerView為列表型態
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 將資料交給adapter
        mAdapter = new ViewAdapter(mDataset);
        // 設置adapter給recycler_view
        mRecyclerView.setAdapter(mAdapter);
    }
}