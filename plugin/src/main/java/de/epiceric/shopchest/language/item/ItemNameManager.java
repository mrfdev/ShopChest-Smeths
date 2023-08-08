package de.epiceric.shopchest.language.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemNameManager {

    @Nullable
    String getItemName(@Nullable ItemStack itemStack);

}
