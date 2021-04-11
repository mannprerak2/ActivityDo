package com.pkmnapps.activitydo.custominterfaces

import com.pkmnapps.activitydo.dataclasses.ListItem
import org.junit.runner.RunWith

interface ListActivityInterface {
    open fun deleteListItem(listItem: ListItem?)
    open fun newListItem()
}