package com.pkmnapps.activitydo.custominterfaces

import com.pkmnapps.activitydo.dataclasses.ActivityData
import org.junit.runner.RunWith

interface HomeFragInterace {
    open fun displayEditDialog(activityData: ActivityData?)
    open fun displayDeleteDialog(activityData: ActivityData?)
    open fun changeColorTheme(pos: Int)
    open fun updatePinnedMenu()
}