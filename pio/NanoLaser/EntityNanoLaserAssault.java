package pio.NanoLaser;

import java.util.*;

import cpw.mods.fml.common.registry.IThrowableEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityNanoLaserAssault extends Entity
implements IThrowableEntity
{
	public float maxRange = 0.0F;
	public float range = 0.0F;
	public float power = 0.0F;

	public boolean explosive = false;

	public static Block[] unmineableBlocks = { Block.brick, Block.lavaMoving, Block.lavaStill, Block.waterMoving, Block.waterStill, Block.bedrock, Block.glass };
	public static final int netId = 161;
	public static final double laserSpeed = 4.0D;
	public EntityLiving owner;
	public boolean headingSet = false;
	public boolean smelt = false;
	public boolean lightning = false;
	public boolean freeging = false;
	private int ticksInAir;
	
	public int[] DamageTable = { 10, 10 };

	public EntityNanoLaserAssault(World world, double x, double y, double z)
	{
		super(world);

		this.ticksInAir = 0;
		setSize(0.8F, 0.8F);
		this.renderDistanceWeight = 100.0F;

		setPosition(x, y, z);
	}

	public EntityNanoLaserAssault(World world) {
		this(world, 0.0D, 0.0D, 0.0D);
	}

	public EntityNanoLaserAssault(World world, EntityLiving entityliving, float range, float power, boolean explosive)
	{
		this(world, entityliving, range, power, explosive, entityliving.rotationYaw, entityliving.rotationPitch);
	}

	public EntityNanoLaserAssault(World world, EntityLiving entityliving, float range, float power, boolean explosive, boolean smelt)
	{
		this(world, entityliving, range, power, explosive, entityliving.rotationYaw, entityliving.rotationPitch);
		this.smelt = smelt;
	}

	public EntityNanoLaserAssault(World world, EntityLiving entityliving, float range, float power, boolean explosive, double yawDeg, double pitchDeg)
	{
		this(world, entityliving, range, power, explosive, yawDeg, pitchDeg, entityliving.posY + entityliving.getEyeHeight() - 0.1D);
	}

	public EntityNanoLaserAssault(World world, EntityLiving entityliving, float range, float power, boolean explosive, double yawDeg, double pitchDeg, double y)
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

		maxRange = this.range = range;
		this.power = power;
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
		super.onUpdate();
		setFire(0);

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
			setHit(true);

			if (movingobjectposition.entityHit != null) {
				double mx = movingobjectposition.hitVec.xCoord - this.posX;
				double my = movingobjectposition.hitVec.yCoord - this.posY;
				double mz = movingobjectposition.hitVec.zCoord - this.posZ;
				
				this.range = (float)(this.range - Math.sqrt(mx * mx + my * my + mz * mz));
				int damage = getDamage();

				if (damage > 0)
				{
					if( this.smelt ){
						entity.setFire( 50 );
					}
					if( lightning ){
						worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord));
					}
					if( entity instanceof EntityLiving ){
						if( freeging ){
							((EntityLiving)entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20000, 5));
						}
					}
					if ((movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeMobDamage(this.owner).setProjectile(), damage)) && 
							((this.owner instanceof EntityPlayer)))
					{
						entity.motionX += this.motionX * power / 50;
						entity.motionY += this.motionY * power / 100;
						entity.motionZ += this.motionZ * power / 50;
						/*
	            if ((((movingobjectposition.entityHit instanceof pp)) && (((pp)movingobjectposition.entityHit).aU() <= 0)) || (((movingobjectposition.entityHit instanceof pn)) && ((((pn)movingobjectposition.entityHit).a instanceof pp)) && (((EntityLiving)((pn)movingobjectposition.entityHit).a).aU() <= 0))) {
	              IC2.achievements.issueAchievement((qx)this.owner, "killDragonMiningLaser");
	            }*/
					}

				}
			}else{	
				if (this.explosive) {
					explode();
				}
				if( freeging ){
					int blockid = freezingBlock( movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);
				}
				
				kill();
				return;
			}
		}
		
		if( (!worldObj.isRemote) ){
			if( lightning && ticksInAir >= 3 && ticksInAir % 2  == 0 ){
				worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, posX, posY, posZ));
			}
			
			if( freeging ){
				//dda
				int x1, x2;
				int y1, y2;
				int z1, z2;
				x1 = (int)Math.round(posX);
				x2 = (int)Math.round( posX + motionX );
			
				y1 = (int)Math.round(posY-0.4000000059604645D);
				y2 = (int)Math.round( posY-0.4000000059604645D + motionY );
			
				z1 = (int)Math.round(posZ);
				z2 = (int)Math.round( posZ + motionZ );
				
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
					int blockid = freezingBlock( cPosX[i], cPosY[i], cPosZ[i]);
					if( blockid == 49 ){
						kill();
						return;
					}
				}
			}
		}

		setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		this.range = (float)(this.range - Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ));
		
		if( power > 2.0f ){
			power -= 0.4f;
		}
		
		if ( !worldObj.isRemote && ((this.range < 1.0F) )) {
			if (this.explosive) explode();

			kill();

			return;
		}

		if ( !freeging && isInWater())
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
		    this.worldObj.newExplosion((Entity)null, this.posX, this.posY, this.posZ, 5.0f, false, false);
			/*
			LaserExplosion explosion = new LaserExplosion(this.worldObj, this, this.posX, this.posY, this.posZ, 5.0F);
			explosion.isDestorying = true;
			explosion.doExplosionA();
			explosion.doExplosionB(true);*/
		}
	}
	
	public int freezingBlock(int x, int y ,int z){
		int blockid = worldObj.getBlockId( x, y, z);
		System.out.println("x:" + x + " y:" + y + " z:" + z + " id:" + blockid );
		if( blockid == 8 || blockid == 9 ){	//water
			this.worldObj.setBlockWithNotify(x, y, z, 79);
			System.out.println("freeze");
			
			blockid = 79;
		}else if( blockid == 10 || blockid == 11 ){	//lava
			this.worldObj.setBlockWithNotify(x, y, z, 49);
			System.out.println("freeze");
			
			blockid = 49;
		}
		return blockid;
	}
	
	public int getDamage(){
		int damage = 10;
		float trange = maxRange - range;
		for( int i = 0; i < DamageTable.length; i += 2 ){
			if( DamageTable[i] >= trange ){
				float prevDis, nextDis = DamageTable[i];
				float prevDmg, nextDmg;
				prevDmg = nextDmg = DamageTable[i + 1];
				if( i == 0 ){
					prevDis = 0f;
				}else{
					prevDis = DamageTable[ i - 2 ];
					prevDmg = DamageTable[ i - 1 ];
				}
				//Linear interpolation
				float dis = nextDis - prevDis;
				float dis2 = trange - prevDis;
				float dam = nextDmg - prevDmg;
				
				if( dis == 0 ) dam = 0;
				damage = (int)(prevDmg + dam * (dis2 / dis));
				break;
			}else if( DamageTable.length - 2 == i ){ //last table
				float prevDis, nextDis;
				float prevDmg, nextDmg;
				prevDmg = nextDmg = DamageTable[i + 1];
				
				prevDis = DamageTable[i];
				nextDis = maxRange;
				
				prevDmg = DamageTable[ i + 1 ];
				nextDmg = 1;
				//Linear interpolation
				float dis = nextDis - prevDis;
				float dis2 = trange - prevDis;
				float dam = nextDmg - prevDmg;
				
				if( dis == 0 ) dam = 0;
				damage = (int)(prevDmg + dam * (dis2 / dis));
			}
		}
		if( damage < 0 ) damage = 1;
		System.out.println("range:"+ (int)trange + "/"+ (int)maxRange + "m damage:" + damage);
		return damage;
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
