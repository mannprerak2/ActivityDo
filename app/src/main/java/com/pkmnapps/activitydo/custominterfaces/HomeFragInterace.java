package com.pkmnapps.activitydo.custominterfaces;

import com.pkmnapps.activitydo.dataclasses.ActivityData;

public interface HomeFragInterace {

    void displayEditDialog(final ActivityData activityData);
    void displayDeleteDialog(final ActivityData activityData);
    void changeColorTheme(int pos);
    void updatePinnedMenu();
}
