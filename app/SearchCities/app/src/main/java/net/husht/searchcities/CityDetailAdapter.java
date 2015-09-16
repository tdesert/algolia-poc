package net.husht.searchcities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by tom on 9/16/15.
 */
public class CityDetailAdapter extends RecyclerView.Adapter<CityDetailAdapter.ViewHolder> {

    public static final int VIEW_TYPE_TEXT = 0;

    public static final int ROW_NAME =          0;
    public static final int ROW_COUNTRY =       1;
    public static final int ROW_POPULATION =    2;
    public static final int ROW_TIMEZONE =      3;
    public static final int ROW_DISTANCE =      4;
    public static final int ROW_LOCATION =      5;
    public static final int ROW_COUNT =         6;

    private City mCity;

    public CityDetailAdapter() {
        super();
    }

    public void setCity(City city) {
        mCity = city;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEXT) {
            View v = (View)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.city_detail_cell, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TextView label = (TextView)viewHolder.getView().findViewById(R.id.row_label);
        TextView value = (TextView)viewHolder.getView().findViewById(R.id.row_value);
        switch (position) {
            case ROW_NAME:
                label.setText("City: ");
                value.setText(mCity.getName());
                break;
            case ROW_COUNTRY:
                label.setText("Country: ");
                value.setText(mCity.getCountry());
                break;
            case ROW_POPULATION:
                label.setText("Population: ");
                value.setText(mCity.getPopulation() + "");
                break;
            case ROW_TIMEZONE:
                label.setText("Timezone: ");
                value.setText(mCity.getTimezone());
                break;
            case ROW_LOCATION:
                label.setText("Coordinates: ");
                break;
            case ROW_DISTANCE:
                label.setText("Distance: ");
                break;

        }
    }

    @Override
    public int getItemCount() {
        if (mCity != null) {
            return ROW_COUNT;
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_TEXT;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }

        public View getView() {
            return mView;
        }
    }

//    public static class TextViewHolder extends ViewHolder {
//        private TextView mTextView;
//        public TextViewHolder(TextView textView) {
//            super(textView);
//            mTextView = textView;
//        }
//
//        public TextView getTextView() {
//            return mTextView;
//        }
//    }


}
