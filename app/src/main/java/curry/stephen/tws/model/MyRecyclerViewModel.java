package curry.stephen.tws.model;

import android.graphics.drawable.Drawable;

/**
 * Created by lingchong on 16/6/22.
 */
public class MyRecyclerViewModel {

    private Drawable mDrawableStatusGreen;
    private Drawable mDrawableStatusRed;
    private String mInfo;

    public Drawable getDrawableStatusRed() {
        return mDrawableStatusRed;
    }

    public void setDrawableStatusRed(Drawable drawableStatusRed) {
        mDrawableStatusRed = drawableStatusRed;
    }

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

    public Drawable getDrawableStatusGreen() {
        return mDrawableStatusGreen;
    }

    public void setDrawableStatusGreen(Drawable drawableStatusGreen) {
        mDrawableStatusGreen = drawableStatusGreen;
    }

}
