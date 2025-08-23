

package com.github.gzuliyujiang.wheelview.contract;

import com.github.gzuliyujiang.wheelview.annotation.ScrollState;
import com.github.gzuliyujiang.wheelview.widget.WheelView;

public interface OnWheelChangedListener {

    void onWheelScrolled(WheelView view, int offset);

    void onWheelSelected(WheelView view, int position);

    void onWheelScrollStateChanged(WheelView view, @ScrollState int state);

    void onWheelLoopFinished(WheelView view);

}
