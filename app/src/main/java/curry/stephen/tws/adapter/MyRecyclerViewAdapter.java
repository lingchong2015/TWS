package curry.stephen.tws.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import curry.stephen.tws.R;
import curry.stephen.tws.model.MyRecyclerViewModel;

/**
 * Created by lingchong on 16/6/22.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private Context mContext;
    private List<MyRecyclerViewModel> mMyRecyclerViewModelList;

    public MyRecyclerViewAdapter(Context context, List<MyRecyclerViewModel> myRecyclerViewModelList) {
        mContext = context;
        mMyRecyclerViewModelList = myRecyclerViewModelList;
    }

    public List<MyRecyclerViewModel> getMyRecyclerViewModelList() {
        return mMyRecyclerViewModelList;
    }

    public void setMyRecyclerViewModelList(List<MyRecyclerViewModel> myRecyclerViewModelList) {
        mMyRecyclerViewModelList = myRecyclerViewModelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_recycler_view, parent, false));

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (mOnItemClickListener != null) {
            holder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view, position);
                }
            });
        }

        holder.getImageViewStatusGreen().setImageDrawable(mMyRecyclerViewModelList.get(position).getDrawableStatusGreen());
        holder.getImageViewStatusRed().setImageDrawable(mMyRecyclerViewModelList.get(position).getDrawableStatusRed());
        holder.getTextViewTransmitterName().setText(mMyRecyclerViewModelList.get(position).getTransmitterName());
        holder.getTextViewInfo().setText(mMyRecyclerViewModelList.get(position).getInfo());
    }

    @Override
    public int getItemCount() {
        return mMyRecyclerViewModelList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageViewStatusGreen;
        private ImageView mImageViewStatusRed;
        private TextView mTextViewTransmitterName;
        private TextView mTextViewInfo;

        public View getRootView() {
            return mRootView;
        }

        public void setRootView(View rootView) {
            mRootView = rootView;
        }

        private View mRootView;

        public ImageView getImageViewStatusGreen() {
            return mImageViewStatusGreen;
        }

        public void setImageViewStatusGreen(ImageView imageViewStatusGreen) {
            mImageViewStatusGreen = imageViewStatusGreen;
        }

        public TextView getTextViewTransmitterName() {
            return mTextViewTransmitterName;
        }

        public void setTextViewTransmitterName(TextView textViewTransmitterName) {
            mTextViewTransmitterName = textViewTransmitterName;
        }

        public TextView getTextViewInfo() {
            return mTextViewInfo;
        }

        public void setTextViewInfo(TextView textViewInfo) {
            mTextViewInfo = textViewInfo;
        }

        public ImageView getImageViewStatusRed() {
            return mImageViewStatusRed;
        }

        public void setImageViewStatusRed(ImageView imageViewStatusRed) {
            mImageViewStatusRed = imageViewStatusRed;
        }

        public MyViewHolder(View view) {
            super(view);

            mRootView = view;
            mImageViewStatusGreen = (ImageView) view.findViewById(R.id.image_view_green_status);
            mImageViewStatusRed = (ImageView) view.findViewById(R.id.image_view_red_status);
            mTextViewTransmitterName = (TextView) view.findViewById(R.id.text_view_transmitter_name);
            mTextViewInfo = (TextView) view.findViewById(R.id.text_view_info);
        }
    }
}
