package com.pkmnapps.activitydo.custominterfaces;

import com.pkmnapps.activitydo.dataclasses.Widget;

public interface TaskActivityInterface {

    void editWidget(Widget widget);
    void deleteWidget(Widget widget);

    void changeActivtyOfWidget(int type,String uid, int pos);
}
