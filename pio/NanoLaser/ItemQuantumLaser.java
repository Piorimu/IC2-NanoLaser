package pio.NanoLaser;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import ic2.api.*;
import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;

public class ItemQuantumLaser extends ItemElectricTool
implements INetworkItemEventListener{
	NBTTagCompound nbtData = new NBTTagCompound();

	private static final int EventShotMining = 0;
	private static final int EventShotLowFocus = 1;
	private static final int EventShotLongRange = 2;
	private static final int EventShotHorizontal = 3;
	private static final int EventShotSuperHeat = 4;
	private static final int EventShotScatter = 5;
	private static final int EventShotExplosive = 6;

	private static final int[] AssultDamageTable = {
		5, 10,
		15, 20,
		30, 15};

	private static final int[] SpreadDamageTable = {
		8, 20};	

	private static final int[] SniperDamageTable = {
		10, 5,
		15, 10,
		40, 40,
		60, 50,
		120, 50};

	private static final int[] FlameDamageTable = {
		30, 8};

	private static final int[] ExplosionDamageTable = {
		30, 20};


	public ItemQuantumLaser(int id, int sprite)
	{
		super(id, sprite, EnumToolMaterial.EMERALD, 500);

		this.maxCharge = 2000000;
		this.transferLimit = 10000;
		this.tier = 3;

		this.setItemName("Quantum Laser");
	}

	public boolean modeChange(EntityPlayer player)
	{
		boolean isMKey = false;
		if( Loader.isModLoaded( "IC2" ) ){
			isMKey = IC2.keyboard.isModeSwitchKeyDown( player );
		}else{
			isMKey = PioKeyHandler.keyTable[0];
		}

		int time = (int)System.currentTimeMillis();
		
		// shift + mkey
		if ( player.isSneaking() && isMKey && (time - nbtData.getInteger( "changeTime" )) > 500) {
			// change mining or assult
			int laserMode = nbtData.getInteger("laserMode");
			laserMode = ( laserMode + 1 ) % 2;
			nbtData.setInteger("laserMode", laserMode);
			String laser = new String[] { "Assult", "Mining"}[laserMode];
			player.addChatMessage("Change to " + laser + " mode");
			nbtData.setInteger("laserSetting", 0);
			player.worldObj.playSoundAtEntity(player, "pio.cursor", 1f , 1.0F);			

			int start = (int)System.currentTimeMillis();
			nbtData.setInteger( "changeTime", start);
			
			return true;
		}

		return false;
	}

	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if (world.isRemote) return itemstack;

		int laserMode = nbtData.getInteger("laserMode");
		int laserSetting = nbtData.getInteger("laserSetting");

		boolean isMKey = false;
		if( Loader.isModLoaded( "IC2" ) ){
			isMKey = IC2.keyboard.isModeSwitchKeyDown( entityplayer );
		}else{
			isMKey = PioKeyHandler.keyTable[0];
		}

		if( modeChange(entityplayer) ) return itemstack;
		if( laserMode == 0 ){
			if (isMKey) {
				laserSetting = (laserSetting + 1) % 7;
				nbtData.setInteger("laserSetting", laserSetting);

				String laser = new String[] { "Assult", "Spread", "Sniper", "Flame", "Explosion", "Lightning", "Freezing"}[laserSetting];
				entityplayer.addChatMessage("Laser Mode: " + laser);
				world.playSoundAtEntity(entityplayer, "pio.cursor", 1f , 1.0F);
			} else {
				int consume = new int[] { 500, 3000, 5000, 1000, 12500, 7500, 5000 }[laserSetting];

				if( Loader.isModLoaded("IC2") ){
					if (!ElectricItem.use(itemstack, consume, entityplayer)) return itemstack;
				}

				EntityNanoLaserAssult b;
				switch (laserSetting) {
				case 0:
					b = new EntityNanoLaserAssult(world, entityplayer, 50f, 10.0F, false);
					b.DamageTable = AssultDamageTable;

					world.spawnEntityInWorld(b);
					world.playSoundAtEntity(entityplayer, "pio.senor", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 1:
					for (int i = 0; i <= 10; i++) {
						b = new EntityNanoLaserAssult(world, entityplayer, 16f, 20.0F, false, entityplayer.rotationYaw - 15.0f + 30.0F * world.rand.nextFloat(), entityplayer.rotationPitch - 15.0f  + 30.0F * world.rand.nextFloat());
						b.DamageTable = SpreadDamageTable;

						world.spawnEntityInWorld(b);
					}
					for (int i = 0; i <= 5; i++) {
						b = new EntityNanoLaserAssult(world, entityplayer, 16f, 20.0F, false, entityplayer.rotationYaw - 5.0f + 10.0F * world.rand.nextFloat(), entityplayer.rotationPitch - 5.0f  + 10.0F * world.rand.nextFloat());
						b.DamageTable = SpreadDamageTable;

						world.spawnEntityInWorld(b);
					}

					world.playSoundAtEntity(entityplayer, "pio.sespr", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 2:
					b = new EntityNanoLaserAssult(world, entityplayer, 120f, 10.0F, false);
					b.DamageTable = SniperDamageTable;

					world.spawnEntityInWorld(b);
					world.playSoundAtEntity(entityplayer, "pio.selong", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 3:
					b = new EntityNanoLaserAssult(world, entityplayer, 30f, 8.0F, false, true);
					b.DamageTable = FlameDamageTable;

					world.spawnEntityInWorld(b);
					world.playSoundAtEntity(entityplayer, "pio.sefire", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
					break;
				case 4:
					b = new EntityNanoLaserAssult(world, entityplayer, 30f, 20.0F, true);
					b.DamageTable = ExplosionDamageTable;

					world.spawnEntityInWorld(b);
					world.playSoundAtEntity(entityplayer, "pio.seexp", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
					break;
				case 5:
					b = new EntityNanoLaserAssult(world, entityplayer, 50f, 5.0F, false);
					b.lightning = true;
					b.DamageTable = FlameDamageTable;

					world.spawnEntityInWorld(b);
					world.playSoundAtEntity(entityplayer, "pio.sefire", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
					break;
				case 6:
					b = new EntityNanoLaserAssult(world, entityplayer, 30f, 5.0F, false);
					b.freeging = true;
					b.DamageTable = FlameDamageTable;

					world.spawnEntityInWorld(b);
					world.playSoundAtEntity(entityplayer, "pio.sefire", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
					break;
				}
			}
		}else if( laserMode == 1 ){
			if (isMKey) {
				laserSetting = (laserSetting + 1) % 7;
				nbtData.setInteger("laserSetting", laserSetting);

				String laser = new String[] { "Mining", "Low-Focus", "Long-Range", "Horizontal", "Super-Heat", "Scatter", "Explosive" }[laserSetting];
				entityplayer.addChatMessage("Laser Mode: " + laser);
				world.playSoundAtEntity(entityplayer, "pio.cursor", 1f , 1.0F);
			} else {
				int consume = new int[] { 1250, 100, 5000, 0, 2500, 10000, 5000 }[laserSetting];

				if( Loader.isModLoaded("IC2") ){
					if (!ElectricItem.use(itemstack, consume, entityplayer)) return itemstack;
				}


				switch (laserSetting) {
				case 0:
					world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityplayer, 10f, 8.0F, 2147483647, false));
					world.playSoundAtEntity(entityplayer, "pio.senor", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 1:
					world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityplayer, 5f, 8.0F, 1, false));
					world.playSoundAtEntity(entityplayer, "pio.senor", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 2:
					world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityplayer, 30f, 20.0F, 2147483647, false));
					world.playSoundAtEntity(entityplayer, "pio.selong", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 3:
					break;
				case 4:
					world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityplayer, 20f, 8.0F, 2147483647, false, true));
					world.playSoundAtEntity(entityplayer, "pio.sefire", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 5:
					for (int x = -2; x <= 2; x++) {
						for (int y = -2; y <= 2; y++) {
							world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityplayer, 10f, 12.0F, 2147483647, false, entityplayer.rotationYaw + 20.0F * x, entityplayer.rotationPitch + 20.0F * y));
						}
					}

					world.playSoundAtEntity(entityplayer, "pio.sespr", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

					break;
				case 6:
					world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityplayer, 20f, 12.0F, 2147483647, true));
					world.playSoundAtEntity(entityplayer, "pio.seexp", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
				}

			}
		}

		return itemstack;
	}

	public String getTextureFile()
	{
		return CommonProxy.NL_ITEMPNG;
	}

	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.rare;
	}

	@Override
	public void onNetworkEvent(int metaData, EntityPlayer player, int event) {

	}
}
