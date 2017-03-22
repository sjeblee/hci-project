package edu.toronto.touchband.touchbandapp;

/**
 * Created by sjeblee on 2/21/17.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.MyViewHolder> {

    private List<String> itemList;
    private int mSelectedPos = 0;
    private int mPixelPos = 0;
    private int mItemHeight = 40;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            view.setClickable(true);
            title = (TextView) view.findViewById(R.id.title);
        }
    }

    public RecyclerListAdapter(List<String> list) {
        this.itemList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);
        //mItemHeight = itemView.getHeight();
        System.out.println("item height: " + mItemHeight);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String item = itemList.get(position);

        holder.title.setText(item);
        holder.itemView.setSelected(mSelectedPos == position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public String getItem(int position) {
        return itemList.get(position);
    }

    public int onScrolled(int dy) {
        mPixelPos += dy;

        if (mPixelPos == 0) {
            mSelectedPos = 0;
        } else {
            // Recalculate visible item
            int index = (mPixelPos / 80) + 1;
            //System.out.println("scroll position: " + mPixelPos + ", item: " + index);
            mSelectedPos = index;
        }
        return mSelectedPos;
    }

    public int getSelectedIndex() {
        return mSelectedPos;
    }

    public String getCurrentItem() {
        return getItem(mSelectedPos);
    }
}
