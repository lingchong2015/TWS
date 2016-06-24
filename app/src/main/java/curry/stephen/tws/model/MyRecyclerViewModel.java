package curry.stephen.tws.model;

import android.graphics.drawable.Drawable;

/**
 * Created by lingchong on 16/6/22.
 */
public class MyRecyclerViewModel {

    private Drawable mDrawableStatus;
    private String mTransmitterName;

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public String getTransmitterName() {
        return mTransmitterName;
    }

    public void setTransmitterName(String transmitterName) {
        mTransmitterName = transmitterName;
    }

    public Drawable getDrawableStatus() {
        return mDrawableStatus;
    }

    public void setDrawableStatus(Drawable drawableStatus) {
        mDrawableStatus = drawableStatus;
    }

    private String mInfo;
}
