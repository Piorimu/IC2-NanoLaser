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

public class ItemNanoLaserM extends ItemElectricTool
implements INetworkItemEventListener{
	NBTTagCompound nbtData = new NBTTagCompound();

	private static final int EventShotMining = 0;
	private static final int EventShotLowFocus = 1;
	private static final int EventShotLongRange = 2;
	private static final int EventShotHorizontal = 3;
	private static final int EventShotSuperHeat = 4;
	private static final int EventShotScatter = 5;
	private static final int EventShotExplosive = 6;

	public ItemNanoLaserM(int id, int sprite)
	{
		super(id, sprite, EnumToolMaterial.EMERALD, 100);

		this.maxCharge = 400000;
		this.transferLimit = 1000;
		this.tier = 2;

		this.setItemName("Nano Laser Mining");
	}

	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if (world.isRemote) return itemstack;

		int laserSetting = nbtData.getInteger("laserSetting");

		boolean isMKey = false;
		if( Loader.isModLoaded( "IC2" ) ){
			isMKey = IC2.keyboard.isModeSwitchKeyDown( entityplayer );
		}else{
			isMKey = PioKeyHandler.keyTable[0];
		}

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

		return itemstack;
	}

	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote) return false;

		boolean isMKey = false;
		if( Loader.isModLoaded( "IC2" ) ){
			isMKey = IC2.keyboard.isModeSwitchKeyDown( entityPlayer );
		}else{
			isMKey = PioKeyHandler.keyTable[0];
		}
		
		if ((!isMKey) && (nbtData.getInteger("laserSetting") == 3)) {
			if (Math.abs(entityPlayer.posY + entityPlayer.getEyeHeight() - 0.1D - (y + 0.5D)) < 1.5D) {
				if( Loader.isModLoaded("IC2") ){
					if (ElectricItem.use(itemstack, 3000, entityPlayer)) {
						world.spawnEntityInWorld(new EntityNanoLaserMining(world, entityPlayer, 20f, 5.0F, 2147483647, false, entityPlayer.rotationYaw, 0.0D, y + 0.5D));
						world.playSoundAtEntity(entityPlayer, "pio.selong", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
					}
				}
			}
			else entityPlayer.addChatMessage( "Mining laser aiming angle too steep");

		}

		return false;
	}

	public String getTextureFile()
	{
		return CommonProxy.NL_ITEMPNG;
	}

	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.uncommon;
	}

	@Override
	public void onNetworkEvent(int metaData, EntityPlayer player, int event) {

	}
}
