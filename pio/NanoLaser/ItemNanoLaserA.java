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

public class ItemNanoLaserA extends ItemElectricTool
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
		100, 30};
	
	private static final int[] FlameDamageTable = {
		30, 8};
	
	private static final int[] ExplosionDamageTable = {
		30, 20};
	

	public ItemNanoLaserA(int id, int sprite)
	{
		super(id, sprite, EnumToolMaterial.EMERALD, 500);

		this.maxCharge = 400000;
		this.transferLimit = 1000;
		this.tier = 2;

		this.setItemName("Nano Laser Assult");
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
			laserSetting = (laserSetting + 1) % 5;
			nbtData.setInteger("laserSetting", laserSetting);

			String laser = new String[] { "Assult", "Spread", "Sniper", "Flame", "Explosion"}[laserSetting];
			entityplayer.addChatMessage("Laser Mode: " + laser);
			world.playSoundAtEntity(entityplayer, "pio.cursor", 1f , 1.0F);
		} else {
			int consume = new int[] { 500, 2000, 5000, 1000, 12500 }[laserSetting];

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
				for (int i = 0; i <= 5; i++) {
					b = new EntityNanoLaserAssult(world, entityplayer, 16f, 20.0F, false, entityplayer.rotationYaw - 4.0f + 8.0F * world.rand.nextFloat(), entityplayer.rotationPitch - 3.0f  + 7.0F * world.rand.nextFloat());
					b.DamageTable = SpreadDamageTable;
					
					world.spawnEntityInWorld(b);
				}
				for (int i = 0; i <= 5; i++) {
					b = new EntityNanoLaserAssult(world, entityplayer, 16f, 20.0F, false, entityplayer.rotationYaw - 12.0f + 24.0F * world.rand.nextFloat(), entityplayer.rotationPitch - 10.0f  + 20.0F * world.rand.nextFloat());
					b.DamageTable = SpreadDamageTable;
					
					world.spawnEntityInWorld(b);
				}

				world.playSoundAtEntity(entityplayer, "pio.sespr", 1f , 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

				break;
			case 2:
				b = new EntityNanoLaserAssult(world, entityplayer, 100f, 10.0F, false);
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
		return EnumRarity.uncommon;
	}

	@Override
	public void onNetworkEvent(int metaData, EntityPlayer player, int event) {

	}
}
