/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata.World;

import Reika.DragonAPI.ModInteract.ReikaTwilightHelper;
import Reika.GeoStrata.GeoStrata;
import Reika.GeoStrata.Registry.GeoOptions;
import Reika.GeoStrata.Registry.RockShapes;
import Reika.GeoStrata.Registry.RockTypes;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class RockGenerator implements IWorldGenerator {

	public static final int BASE_GEN = 24;
	public static final int VEIN_SIZE = 32;
	public static final int REPLACE_PERCENT = 15;

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkgen, IChunkProvider provider) {
		if (this.canGenInDimension(world.provider.dimensionId)) {
			this.generateRock(world, random, chunkX, chunkZ);
		}
	}

	private boolean canGenInDimension(int id) {
		if (id == 0)
			return true;
		if (id == 1 || id == -1)
			return false;
		if (id == ReikaTwilightHelper.getDimensionID())
			return GeoOptions.TFGEN.getState();
		return GeoOptions.DIMGEN.getState();
	}

	private void generateRock(World world, Random random, int chunkX, int chunkZ) {
		chunkX *= 16;
		chunkZ *= 16;
		//ReikaJavaLibrary.pConsole("Calling chunk at "+chunkX+", "+chunkZ);
		BiomeGenBase biome = world.getBiomeGenForCoords(chunkX, chunkZ);
		for (int k = 0; k < RockTypes.rockList.length; k++) {
			RockTypes geo = RockTypes.rockList[k];
			double max = BASE_GEN*geo.rarity*this.getDensityFactor(geo);
			//ReikaJavaLibrary.pConsole("Genning "+geo+" "+max+" times.");
			for (int i = 0; i < max; i++) {
				int posX = chunkX + random.nextInt(16);
				int posZ = chunkZ + random.nextInt(16);
				int posY = geo.minY + random.nextInt(geo.maxY-geo.minY);
				GeoStrata.logger.debug(geo.name()+":"+geo.canGenerateAt(world, posX, posY, posZ, random));
				if (geo.canGenerateAt(world, posX, posY, posZ, random)) {
					Block id = geo.getID(RockShapes.SMOOTH);
					(new WorldGenMinable(id, VEIN_SIZE, Blocks.stone)).generate(world, random, posX, posY, posZ);
					//GeoStrata.logger.log("Generating "+geo+" at "+posX+", "+posY+", "+posZ);
				}
			}
		}
	}

	/** if compressed in small y, or lots of coincident rocks, reduce density */
	private double getDensityFactor(RockTypes rock) {
		float f = GeoOptions.getRockDensity();
		List<RockTypes> types = rock.getCoincidentTypes();
		int h = rock.maxY-rock.minY;
		if (rock == RockTypes.ONYX)
			h *= 2;
		return f*1F*h/64D*(3D/types.size());
	}

}
