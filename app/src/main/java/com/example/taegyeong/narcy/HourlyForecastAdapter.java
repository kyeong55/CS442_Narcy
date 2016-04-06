package com.example.taegyeong.narcy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taegyeong on 16. 3. 25..
 */
public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>{
    private Context context;
//
    private List<Weather> items;

    public HourlyForecastAdapter(Context context){
        this.context = context;
        items = new ArrayList<>();
    }

    public Drawable getIconDrawable(String icon){
        if (icon.contentEquals("clear")||icon.contentEquals("sunny"))
            return context.getResources().getDrawable(R.drawable.sunny);
        else if (icon.contentEquals("cloudy"))
            return context.getResources().getDrawable(R.drawable.cloud);
        else if (icon.indexOf("partly")>=0||icon.indexOf("mostly")>=0)
            return context.getResources().getDrawable(R.drawable.sunnycloud);
        else if (icon.contentEquals("fog")||icon.contentEquals("hazy"))
            return context.getResources().getDrawable(R.drawable.mist);
        else if (icon.contentEquals("rain"))
            return context.getResources().getDrawable(R.drawable.rain);
        else if (icon.contentEquals("tstorms"))
            return context.getResources().getDrawable(R.drawable.thunder);
        else if (icon.contentEquals("snow")||icon.contentEquals("sleet")||icon.contentEquals("flurries"))
            return context.getResources().getDrawable(R.drawable.snow);
        else
            return null;
    }

    @Override
    public HourlyForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.forecast_elem,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HourlyForecastAdapter.ViewHolder holder, int position) {
        Weather w = items.get(position);
        holder.time.setText(w.getTime());
        holder.temp.setText(w.getTemp());
        holder.pop.setText(w.getPrecipProbability());
        holder.icon.setImageDrawable(getIconDrawable(w.getIcon()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Weather> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView time;
        TextView temp;
        TextView pop;
        ImageView icon;

        View layout;

        public ViewHolder(View itemView) {
            super(itemView);
            time = (TextView)itemView.findViewById(R.id.forecast_elem_time);
            temp = (TextView)itemView.findViewById(R.id.forecast_elem_temp);
            pop = (TextView)itemView.findViewById(R.id.forecast_elem_precip);
            icon = (ImageView)itemView.findViewById(R.id.forecast_elem_icon);
            layout = itemView.findViewById(R.id.forecast_elem_layout);
        }
    }
}
