package pio.NanoLaser;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MathHelper;

public class RenderNanoLaserAssault extends Render {
	private final double LASER_WEIGHT = 0.1;
	private final double LASER_LONG = 2.0;
	private final double LASER_P20 = 0.666;
	
	float var20 = 0.05625F;

    public void renderArrow(Entity par1EntityArrow, double par2, double par4, double par6, float par8, float par9)
    {
    	this.loadTexture("/pio/NanoLaser/sprites/bullet.png");
        Tessellator var10 = Tessellator.instance;
        byte var11 = 1;
        
        EntityNanoLaserAssault laser = (EntityNanoLaserAssault)par1EntityArrow;
        //if( laser.isHit() ) return;
        
        float depth = ( laser.maxRange - laser.range );
        if( laser.maxRange != 0 ) depth /= laser.maxRange;
        
        double LLong = (laser.maxRange - laser.range) * LASER_P20;
        
        GL11.glDepthMask(false);
        //light laser line
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)par2, (float)par4, (float)par6);
            GL11.glRotatef(par1EntityArrow.prevRotationYaw + (par1EntityArrow.rotationYaw - par1EntityArrow.prevRotationYaw) * par9 - 90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(par1EntityArrow.prevRotationPitch + (par1EntityArrow.rotationPitch - par1EntityArrow.prevRotationPitch) * par9, 0.0F, 0.0F, 1.0F);
        	
	        float var12 = 0.0F;
	        float var13 = 1.0F;
	        float var14 = (float)(var11 * 8) / 24f;
	        float var15 = (float)(var11 * 8 + 8) / 24f;
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	
	        GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
	
	        for (int i = 0; i < 4; ++i)
	        {
	            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
	            GL11.glNormal3f(0.0f, 0.0F, var20);
	            GL11.glDisable(GL11.GL_LIGHTING);	//lighting off
	            GL11.glEnable(GL11.GL_BLEND);	//blend on
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, depth);
	            var10.startDrawingQuads();
	            var10.addVertexWithUV(-0.2D - LLong, -LASER_WEIGHT, 0.0D, (double)var12, (double)var14);
	            var10.addVertexWithUV(0.2D, -LASER_WEIGHT, 0.0D, (double)var13, (double)var14);
	            var10.addVertexWithUV(0.2D, LASER_WEIGHT, 0.0D, (double)var13, (double)var15);
	            var10.addVertexWithUV(-0.2D - LLong, LASER_WEIGHT, 0.0D, (double)var12, (double)var15);
	            var10.draw();
	        }
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        GL11.glPopMatrix();
	        
	        GL11.glEnable(GL11.GL_LIGHTING);	//lighting on
	        GL11.glDisable(GL11.GL_BLEND);	//blend off
        }
        
        //laser lightball
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        	
	        float var12 = (float)(var11 * 8) / 80f;
	        float var13 = (float)(var11 * 8 + 8) / 80f;
	        float var14 = (float)(16) / 24f;
	        float var15 = (float)(24) / 24f;
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        
	        GL11.glRotatef(180F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
	        
            GL11.glDisable(GL11.GL_LIGHTING);	//lighting off
            GL11.glEnable(GL11.GL_BLEND);	//blend on
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, depth);
            var10.startDrawingQuads();
            GL11.glNormal3f(0.0F, 1.0F, 0.0f);
            var10.addVertexWithUV(-LASER_WEIGHT, -LASER_WEIGHT, 0.0D, (double)var12, (double)var14);
            var10.addVertexWithUV(LASER_WEIGHT, -LASER_WEIGHT, 0.0D, (double)var13, (double)var14);
            var10.addVertexWithUV(LASER_WEIGHT, LASER_WEIGHT, 0.0D, (double)var13, (double)var15);
            var10.addVertexWithUV(-LASER_WEIGHT, LASER_WEIGHT, 0.0D, (double)var12, (double)var15);
            var10.draw();
	            
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        GL11.glPopMatrix();
	        
	        GL11.glEnable(GL11.GL_LIGHTING);	//lighting on
	        GL11.glDisable(GL11.GL_BLEND);	//blend off
        }    
        
        GL11.glDepthMask(true);
    }
    
	@Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
		this.renderArrow(par1Entity, par2, par4, par6, par8, par9);
		//System.out.println("Rendering Laser");
	}

}
