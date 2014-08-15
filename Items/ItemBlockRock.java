/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata.Items;

import Reika.GeoStrata.Registry.RockShapes;
import Reika.GeoStrata.Registry.RockTypes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockRock extends ItemBlock {

	public ItemBlockRock(Block b) {
		super(b);
		hasSubtypes = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public final void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List li) //Adds the metadata blocks to the creative inventory
	{
		if (li.size() < RockTypes.rockList.length*RockShapes.shapeList.length) {
			for (int i = 0; i < RockTypes.rockList.length; i++) {
				for (int k = 0; k < RockShapes.shapeList.length; k++) {
					ItemStack item = RockTypes.rockList[i].getItem(RockShapes.shapeList[k]);
					li.add(item);
				}
			}
		}
	}

	@Override
	public final String getUnlocalizedName(ItemStack is) {
		int d = is.getItemDamage();
		return super.getUnlocalizedName() + "." + d;
	}

	@Override
	public final String getItemStackDisplayName(ItemStack is) {
		RockTypes r = RockTypes.getTypeFromID(field_150939_a);
		RockShapes s = RockShapes.getShape(field_150939_a, is.getItemDamage());
		return s.getName()+" "+r.getName();
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean par4) {
		RockTypes rock = RockTypes.getTypeFromID(field_150939_a);
		float blast = rock.blastResistance;
		float more = blast/Blocks.stone.blockResistance;
		li.add(String.format("Blast Resistance: %.1f (%.1fx stone)", blast, more));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack is, int par2)
	{
		return 0xffffff;
	}

}