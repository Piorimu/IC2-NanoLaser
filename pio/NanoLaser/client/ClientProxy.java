package pio.NanoLaser.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import pio.NanoLaser.*;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void registerRenderers() {
            MinecraftForgeClient.preloadTexture(NL_ITEMPNG);
            
            //Entity Render
            RenderingRegistry.registerEntityRenderingHandler(EntityNanoLaserAssault.class, new RenderNanoLaserAssault());
            RenderingRegistry.registerEntityRenderingHandler(EntityNanoLaserMining.class, new RenderNanoLaserMining());
    }
    
}
