package pio.NanoLaser;

import java.util.*;

import cpw.mods.fml.common.registry.IThrowableEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityNanoLaserMining extends Entity
implements IThrowableEntity
{
	public float maxRange = 0.0F;
	public float range = 0.0F;
	public float power = 0.0F;
	public int blockBreaks = 0;

	public boolean explosive = false;

	public static Block[] unmineableBlocks = { Block.brick, Block.lavaMoving, Block.lavaStill, Block.waterMoving, Block.waterStill, Block.bedrock, Block.glass };
	public static final int netId = 160;
	public static final double laserSpeed = 2.0D;
	public EntityLiving owner;
	public boolean headingSet = false;
	public boolean smelt = false;
	private int ticksInAir;

	public EntityNanoLaserMining(World world, double x, double y, double z)
	{
		super(world);

		this.ticksInAir = 0;
		setSize(0.8F, 0.8F);
		this.renderDistanceWeight = 100.0F;

		setPosition(x, y, z);
	}

	public EntityNanoLaserMining(World world) {
		this(world, 0.0D, 0.0D, 0.0D);
	}

	public EntityNanoLaserMining(World world, EntityLiving entityliving, float range, float power, int blockBreaks, boolean explosive)
	{
		this(world, entityliving, range, power, blockBreaks, explosive, entityliving.rotationYaw, entityliving.rotationPitch);
	}

	public EntityNanoLaserMining(World world, EntityLiving entityliving, float range, float power, int blockBreaks, boolean explosive, boolean smelt)
	{
		this(world, entityliving, range, power, blockBreaks, explosive, entityliving.rotationYaw, entityliving.rotationPitch);
		this.smelt = smelt;
	}

	public EntityNanoLaserMining(World world, EntityLiving entityliving, float range, float power, int blockBreaks, boolean explosive, double yawDeg, double pitchDeg)
	{
		this(world, entityliving, range, power, blockBreaks, explosive, yawDeg, pitchDeg, entityliving.posY + entityliving.getEyeHeight() - 0.1D);
	}

	public EntityNanoLaserMining(World world, EntityLiving entityliving, float range, float power, int blockBreaks, boolean explosive, double yawDeg, double pitchDeg, double y)
	{
		super(world);

		this.ticksInAir = 0;
		this.owner = entityliving;
		setSize(0.8F, 0.8F);
		this.renderDistanceWeight = 100.0F;

		double yaw = Math.toRadians(yawDeg);
		double pitch = Math.toRadians(pitchDeg);

		double x = entityliving.posX - Math.cos(yaw) * 0.16D;
		double z = entityliving.posZ - Math.sin(yaw) * 0.16D;

		double startMotionX = -Math.sin(yaw) * Math.cos(pitch);
		double startMotionY = -Math.sin(pitch);
		double startMotionZ = Math.cos(yaw) * Math.cos(pitch);

		setPosition(x, y, z);
		setLaserHeading(startMotionX, startMotionY, startMotionZ, laserSpeed);

		this.maxRange = this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.explosive = explosive;
	}

	protected void entityInit()
	{
		this.dataWatcher.addObject(8, new Integer( 0 ));	//isHit
	}

	public void setLaserHeading(double motionX, double motionY, double motionZ, double speed)
	{
		double currentSpeed = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);

		this.motionX = (motionX / currentSpeed * speed);
		this.motionY = (motionY / currentSpeed * speed);
		this.motionZ = (motionZ / currentSpeed * speed);

		this.prevRotationYaw = (this.rotationYaw = (float)Math.toDegrees(Math.atan2(motionX, motionZ)));
		this.prevRotationPitch = (this.rotationPitch = (float)Math.toDegrees(Math.atan2(motionY, MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ))));

		this.headingSet = true;
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double motionX, double motionY, double motionZ)
	{
		setLaserHeading(motionX, motionY, motionZ, laserSpeed);
	}


	public void onUpdate()
	{
		setFire(0);
		super.onUpdate();

		this.ticksInAir += 1;

		Vec3 oldPosition = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		Vec3 newPosition = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks_do_do(oldPosition, newPosition, false, true);

		oldPosition = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);

		if (movingobjectposition != null)
			newPosition = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
		else {
			newPosition = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		}

		Entity entity = null;
		List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
		double d = 0.0D;
		for (int l = 0; l < list.size(); l++)
		{
			Entity entity1 = (Entity)list.get(l);
			if ((!entity1.canBeCollidedWith()) || ((entity1 == this.owner) && (this.ticksInAir < 5)))
			{
				continue;
			}
			float f4 = 0.3F;
			AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand(f4, f4, f4);
			MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(oldPosition, newPosition);
			if (movingobjectposition1 == null)
			{
				continue;
			}
			double d1 = oldPosition.distanceTo(movingobjectposition1.hitVec);
			if ((d1 >= d) && (d != 0.0D))
				continue;
			entity = entity1;
			d = d1;
		}

		if (entity != null) movingobjectposition = new MovingObjectPosition(entity);

		if ((movingobjectposition != null) && (!worldObj.isRemote)) {
			if (this.explosive) {
				explode();
				kill();

				return;
			}

			if (movingobjectposition.entityHit != null) {
				int damage = (int)this.power;

				if (damage > 0)
				{
					entity.setFire(damage * (this.smelt ? 2 : 1));
					if ((movingobjectposition.entityHit.attackEntityFrom(new EntityDamageSourceIndirect("arrow", this, this.owner).setProjectile(), damage)) && 
							((this.owner instanceof EntityPlayer)))
					{
						/*
	            if ((((movingobjectposition.entityHit instanceof pp)) && (((pp)movingobjectposition.entityHit).aU() <= 0)) || (((movingobjectposition.entityHit instanceof pn)) && ((((pn)movingobjectposition.entityHit).a instanceof pp)) && (((EntityLiving)((pn)movingobjectposition.entityHit).a).aU() <= 0))) {
	              IC2.achievements.issueAchievement((qx)this.owner, "killDragonMiningLaser");
	            }*/
					}

				}

				kill();
			} else {
				//dda
				int x1, x2;
				int y1, y2;
				int z1, z2;
				x1 = (int)Math.round(movingobjectposition.blockX);
				x2 = (int)Math.round( x1 + motionX );
			
				y1 = (int)Math.round(movingobjectposition.blockY);
				y2 = (int)Math.round( y1  + motionY );
			
				z1 = (int)Math.round( movingobjectposition.blockZ);
				z2 = (int)Math.round( z1 + motionZ );
				
				int dx = x2 - x1, absdx = Math.abs(dx);
				int dy = y2 - y1, absdy = Math.abs(dy);
				int dz = z2 - z1, absdz = Math.abs(dz);
				int sx = dx < 0 ? -1 : 1;
				int sy = dy < 0 ? -1 : 1;
				int sz = dz < 0 ? -1 : 1;
				//xy
				int pointnum = (int)laserSpeed + 1;
				int[] cPosX = new int[pointnum];
				int[] cPosY = new int[pointnum];
				int[] cPosZ = new int[pointnum];
				//init
				int[] res;
				for( int i = 0; i < pointnum; ++i ){
					cPosX[i] = x2;
					cPosY[i] = y2;
					cPosZ[i] = z2;
				}
				
				if( dx != 0 && ((double)(Math.abs(dy)) / (double)(Math.abs(dx))) > 1.0d ){
					int e = -absdy;
					int x, y;
					x = x1; y = y1;
					for( int i = 0; i < absdy; ++i ){
						cPosX[i] = x; cPosY[i] = y;
						y = y + sy;
						e = e + 2 * absdx;
						if( e >= 0 ){
							x = x + sx;
							e = e - 2 * absdy;
						}
					}
				}else{
					int e = -absdx;
					int x, y;
					x = x1; y = y1;
					for( int i = 0; i < absdx; ++i ){
						cPosX[i] = x; cPosY[i] = y;
						x = x + sx;
						e = e + 2 * absdy;
						if( e >= 0 ){
							y = y + sy;
							e = e - 2 * absdx;
						}
					}
				}
				
				if( dy == 0 && ((double)(Math.abs(dz)) / (double)(Math.abs(dy))) > 1.0d ){
					int e = -absdz;
					int y, z;
					y = y1; z = z1;
					for( int i = 0; i < absdz; ++i ){
						cPosY[i] = y; cPosZ[i] = z;
						z = z + sz;
						e = e + 2 * absdy;
						if( e >= 0 ){
							y = y + sy;
							e = e - 2 * absdz;
						}
					}
				}else{
					int e = -absdy;
					int y, z;
					y = y1; z = z1;
					for( int i = 0; i < absdy; ++i ){
						cPosY[i] = y; cPosZ[i] = z;
						y = y + sy;
						e = e + 2 * absdz;
						if( e >= 0 ){
							z = z + sz;
							e = e - 2 * absdy;
						}
					}
				}
				
				//999freezing999
				for( int i = 0; i < pointnum; ++i ){
					int xTile = cPosX[i];
					int yTile = cPosY[i];
					int zTile = cPosZ[i];
					
					int blockId = this.worldObj.getBlockId(xTile, yTile, zTile);
					int blockMeta = this.worldObj.getBlockMetadata(xTile, yTile, zTile);
					boolean blockSet = false;
					boolean removeBlock = true;
					boolean dropBlock = true;
					
					if( blockId == 0 ) continue;

					if (!canMine(blockId)) {
						kill();
						break;
					} else if (!worldObj.isRemote) {
						
						float resis = 0.0F;

						resis = Block.blocksList[blockId].getExplosionResistance(this, this.worldObj, xTile, yTile, zTile, this.posX, this.posY, this.posZ) + 0.3F;

						this.power -= resis / 10.0F;

						if (this.power >= 0.0F) {
							if (Block.blocksList[blockId].blockMaterial == Material.coral)
								Block.blocksList[blockId].onBlockDestroyedByPlayer(this.worldObj, xTile, yTile, zTile, 1);
							else if (this.smelt)
								if (Block.blocksList[blockId].blockMaterial == Material.rock) {
									removeBlock = true;
									dropBlock = false;
								} else {
									for (ItemStack isa : Block.blocksList[blockId].getBlockDropped(this.worldObj, xTile, yTile, zTile, blockMeta, 0)) {
										ItemStack isb = FurnaceRecipes.smelting().getSmeltingResult(isa);
										if (isb != null) {
											ItemStack is = isb.copy();
											if ((!blockSet) && (is.itemID != blockId) && (is.itemID < Block.blocksList.length) && (Block.blocksList[is.itemID] != null) && (Block.blocksList[is.itemID].blockID != 0)) {
												blockSet = true;
												removeBlock = false;
												dropBlock = false;
												this.worldObj.setBlockAndMetadataWithNotify(xTile, yTile, zTile, is.itemID, is.getItemDamage());
											} else {
												dropBlock = false;
												float var6 = 0.7F;
												double var7 = this.worldObj.rand.nextFloat() * var6 + (1.0F - var6) * 0.5D;
												double var9 = this.worldObj.rand.nextFloat() * var6 + (1.0F - var6) * 0.5D;
												double var11 = this.worldObj.rand.nextFloat() * var6 + (1.0F - var6) * 0.5D;
												EntityItem var13 = new EntityItem(this.worldObj, xTile + var7, yTile + var9, zTile + var11, is);
												var13.delayBeforeCanPickup = 10;
												this.worldObj.spawnEntityInWorld(var13);
											}
											this.power = 0.0F;
										}
									}
								}
							if (removeBlock) {
								if (dropBlock) Block.blocksList[blockId].dropBlockAsItemWithChance(this.worldObj, xTile, yTile, zTile, this.worldObj.getBlockMetadata(xTile, yTile, zTile), 0.9F, 0);
								this.worldObj.setBlockWithNotify(xTile, yTile, zTile, 0);

								if ((this.worldObj.rand.nextInt(10) == 0) && (Block.blocksList[blockId].blockMaterial.blocksMovement()))
								{
									this.worldObj.setBlockWithNotify(xTile, yTile, zTile, Block.fire.blockID);
								}
							}

							this.blockBreaks -= 1;							
							setHit(true);
							if( blockBreaks <= 0 ) {
								kill();
								break;
							}
						}
					}
				}
			}
		} else {
			this.power -= 0.5F;
		}
		
		this.prevRotationYaw = (this.rotationYaw = (float)Math.toDegrees(Math.atan2(motionX, motionZ)));
		this.prevRotationPitch = (this.rotationPitch = (float)Math.toDegrees(Math.atan2(motionY, MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ))));

		setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		this.range = (float)(this.range - Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ));

		if ( !worldObj.isRemote && ((this.range < 1.0F) || (this.power <= 0.0F) || (this.blockBreaks <= 0))) {
			if (this.explosive) explode();

			kill();

			return;
		}

		if (isInWater())
			kill();
	}

	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setBoolean( "isHit", this.isHit() );
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		setHit( nbttagcompound.getBoolean( "isHit" ) );
	}
	
	public void setHit( boolean h ){
		this.dataWatcher.updateObject(8, h ? 1 : 0);
	}
	
    public boolean isHit()
    {
        return this.dataWatcher.getWatchableObjectInt(8) != 0;
    }	

	public float getShadowSize()
	{
		return 0.0F;
	}

	public void explode()
	{
		if (!worldObj.isRemote) {
			this.worldObj.createExplosion((Entity)null, this.posX, this.posY, this.posZ, 5.0f, true);
			/*Explosion explosion = new Explosion(this.worldObj, null, this.posX, this.posY, this.posZ, 5.0F);
	      explosion.doExplosionA();*/
		}
	}

	public boolean canMine(int blockId)
	{
		for (int i = 0; i < unmineableBlocks.length; i++) {
			if (blockId == unmineableBlocks[i].blockID) {
				return false;
			}
		}

		return true;
	}

	public Entity getThrower()
	{
		return this.owner;
	}

	public void setThrower(Entity entity)
	{
		if ((entity instanceof EntityLiving)) this.owner = ((EntityLiving)entity);
	}
}
