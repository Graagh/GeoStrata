/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata.Base;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import Reika.DragonAPI.Libraries.ReikaPotionHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaDyeHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.GeoStrata.CrystalPotionController;
import Reika.GeoStrata.GeoStrata;
import Reika.GeoStrata.Registry.GeoBlocks;
import Reika.GeoStrata.Registry.GeoOptions;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class CrystalBlock extends Block {

	protected final Icon[] icons = new Icon[ReikaDyeHelper.dyes.length];

	public CrystalBlock(int ID, Material mat) {
		super(ID, mat);
		this.setCreativeTab(GeoStrata.tabGeo);
		this.setHardness(1F);
		this.setResistance(2F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public final Icon getIcon(int s, int meta) {
		return icons[meta];
	}

	@Override
	public final int getRenderType() {
		return GeoStrata.proxy.crystalRender; //6 was crops
	}

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int color = world.getBlockMetadata(x, y, z);
		double[] v = ReikaDyeHelper.getColorFromDamage(color).getRedstoneParticleVelocityForColor();
		ReikaParticleHelper.spawnColoredParticles(world, x, y, z, v[0], v[1], v[2], 1);
		if (rand.nextInt(3) == 0)
			ReikaPacketHelper.sendUpdatePacket(GeoStrata.packetChannel, 0, world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addBlockHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		Random rand = new Random();
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;
		int color = world.getBlockMetadata(x, y, z);
		ReikaDyeHelper dye = ReikaDyeHelper.getColorFromDamage(color);
		double[] v = dye.getRedstoneParticleVelocityForColor();
		ReikaParticleHelper.spawnColoredParticles(world, x, y, z, v[0], v[1], v[2], 4);
		if (dye != ReikaDyeHelper.PURPLE) //prevent an XP exploit
			ReikaPacketHelper.sendUpdatePacket(GeoStrata.packetChannel, 0, world, x, y, z);
		return false;
	}

	public void updateEffects(World world, int x, int y, int z) {
		Random rand = new Random();
		if (this.shouldMakeNoise())
			world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.orb", 0.05F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
		if (this.shouldGiveEffects()) {
			int r = this.getRange();
			AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x+1, y+1, z+1).expand(r, r, r);
			List inbox = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
			for (int i = 0; i < inbox.size(); i++) {
				EntityLivingBase e = (EntityLivingBase)inbox.get(i);
				if (ReikaMathLibrary.py3d(e.posX-x-0.5, e.posY+e.getEyeHeight()/2F-y-0.5, e.posZ-z-0.5) <= 4)
					this.getEffectFromColor(e, ReikaDyeHelper.getColorFromDamage(world.getBlockMetadata(x, y, z)));
			}
		}
	}

	public boolean shouldMakeNoise() {
		if (GeoOptions.NOISE.getState())
			return true;
		return blockID == GeoBlocks.CRYSTAL.getBlockID();
	}

	public boolean shouldGiveEffects() {
		if (GeoOptions.EFFECTS.getState())
			return true;
		return blockID != GeoBlocks.LAMP.getBlockID();
	}

	public int getRange() {
		return blockID == GeoBlocks.SUPER.getBlockID() ? 12 : 3;
	}

	public int getDuration() {
		return blockID == GeoBlocks.SUPER.getBlockID() ? 6000 : 200;
	}

	public boolean renderAllArms() {
		return this.renderBase();
	}

	public boolean renderBase() {
		return blockID != GeoBlocks.CRYSTAL.getBlockID();
	}

	public Block getBaseBlock(ForgeDirection side) {
		return blockID == GeoBlocks.SUPER.getBlockID() ? Block.obsidian : side.offsetY == 0 ? Block.stone : Block.stoneDoubleSlab;
	}

	public int getPotionLevel() {
		return blockID == GeoBlocks.SUPER.getBlockID() ? 2 : 0;
	}

	private void getEffectFromColor(EntityLivingBase e, ReikaDyeHelper color) {
		World world = e.worldObj;
		int dura = this.getDuration();
		int level = this.getPotionLevel();
		if (world.provider.isHellWorld) {
			switch(color) {
			case ORANGE:
				e.setFire(2);
				break;
			case RED:
				e.attackEntityFrom(DamageSource.magic, 1);
				break;
			case PURPLE:
				if (!e.worldObj.isRemote && new Random().nextInt(5) == 0 && e instanceof EntityPlayer) {
					if (((EntityPlayer)e).experienceTotal > 0) {
						ReikaJavaLibrary.pConsole(((EntityPlayer)e).experienceTotal);
						((EntityPlayer)e).experienceTotal -= 5;
					}
				}
				break;
			case BROWN:
				if (!e.isPotionActive(Potion.confusion.id))
					e.addPotionEffect(new PotionEffect(Potion.confusion.id, (int)(dura*1.8), level));
				break;
			case LIME:
				e.addPotionEffect(new PotionEffect(Potion.jump.id, dura, -5));
				break;
			default:
				PotionEffect eff = CrystalPotionController.getNetherEffectFromColor(color, dura, level);
				if (this.isPotionAllowed(eff, e))
					e.addPotionEffect(eff);
			}
		}
		else {
			switch(color) {
			case BLACK:
				if (e instanceof EntityMob) {  //clear AI
					EntityMob m = (EntityMob)e;
					m.setAttackTarget(null);
					m.getNavigator().clearPathEntity();
				}
				break;
			case WHITE:
				e.clearActivePotions();
				break;
			case PURPLE:
				if (!e.worldObj.isRemote && new Random().nextInt(2) == 0)
					e.worldObj.spawnEntityInWorld(new EntityXPOrb(e.worldObj, e.posX, e.posY, e.posZ, 1));
				break;
			default:
				PotionEffect eff = CrystalPotionController.getEffectFromColor(color, dura, level);
				if (eff != null) {
					if (this.isPotionAllowed(eff, e)) {
						e.addPotionEffect(eff);
					}
				}
			}
		}
	}

	private static boolean isPotionAllowed(PotionEffect eff, EntityLivingBase e) {
		if (eff == null)
			return false;
		if (!(e instanceof EntityPlayer))
			return true;
		if (e.worldObj.provider.isHellWorld)
			return true;
		if (e.worldObj.provider.dimensionId == 1)
			return true;
		Potion pot = Potion.potionTypes[eff.getPotionID()];
		return !ReikaPotionHelper.isBadEffect(pot);
	}

	@Override
	public boolean canEntityDestroy(World world, int x, int y, int z, Entity e) {
		return false;
	}
}
