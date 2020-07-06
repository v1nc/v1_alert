package de.reckendrees.systems.alert;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;


public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<DataObject> mDataset;
    private static MyClickListener myClickListener;
    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView label;
        TextView dateTime;
        MaterialCardView card;
        SharedPreferences preferenceManager;
        public DataObjectHolder(View itemView) {
            super(itemView);
            final MaterialCardView cardView = itemView.findViewById(R.id.card_view);
            final LinearLayout cardBackground = itemView.findViewById(R.id.card_background);
            final TextView textView = itemView.findViewById(R.id.textView);
            final TextView textView2 = itemView.findViewById(R.id.textView2);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            final SharedPreferences.Editor editor = prefs.edit();

            cardBackground.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.gradient_main));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    String prefString = "hook_"+textView.getText().toString();
                    editor.putBoolean(prefString,!cardView.isChecked());
                    editor.commit();
                    Context context = textView.getContext();
                    if(cardView.isChecked()){
                        textView2.setText(context.getString(R.string.alert_off));
                    }else{
                        textView2.setText(context.getString(R.string.alert_on));

                    }
                    cardView.toggle();


                    /*Intent i = new Intent();
                    i.setComponent(new ComponentName("de.reckendrees.systems.v1_alert", "de.reckendrees.systems.v1_alert.HUD"));
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ComponentName c = cardView.getContext().startForegroundService(i);
                    }else{
                        ComponentName c = cardView.getContext().startService(i);
                    }*/


                }
            });
            label = (TextView) itemView.findViewById(R.id.textView);
            dateTime = (TextView) itemView.findViewById(R.id.textView2);
            card = cardView;
            preferenceManager = prefs;
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }
    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }
    public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {
        mDataset = myDataset;
    }
    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }
    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        Context context = holder.label.getContext();
        String name= mDataset.get(position).getmText1();
        holder.label.setText(name);
        holder.dateTime.setText(mDataset.get(position).getmText2());
        holder.card.setChecked(holder.preferenceManager.getBoolean("hook_"+name,false));
        if(holder.card.isChecked()){
            holder.dateTime.setText(context.getString(R.string.alert_on));
        }else{
            holder.dateTime.setText(context.getString(R.string.alert_off));
        }
    }
    public void addItem(DataObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }
    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
}