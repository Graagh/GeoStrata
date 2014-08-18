/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata;

import Reika.GeoStrata.Registry.RockShapes;
import Reika.GeoStrata.Registry.RockTypes;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GeoTabSlab extends CreativeTabs {

	public GeoTabSlab(int position, String tabID) {
		super(position, tabID); //The constructor for your tab
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return RockTypes.MARBLE.getSlab(RockShapes.ENGRAVED);
	}

	@Override
	public String getTranslatedTabLabel() {
		return GeoStrata.MOD_NAME+" Slabs";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return null;
	}

}
