package com.pkmnapps.activitydo.custominterfaces

import com.pkmnapps.activitydo.dataclasses.Widget
import org.junit.runner.RunWith

interface TaskActivityInterface {
    open fun editWidget(widget: Widget?)
    open fun deleteWidget(widget: Widget?)
    open fun changeActivtyOfWidget(type: Int, uid: String?, pos: Int)
}