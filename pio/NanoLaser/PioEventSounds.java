package pio.NanoLaser;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class PioEventSounds {
    @ForgeSubscribe
    public void onSound(SoundLoadEvent event)
    {
        try 
        {
        	event.manager.soundPoolSounds.addSound("pio/cursor.wav", NanoLaser.class.getResource("/pio/NanoLaser/se/cursor1.wav")); 
        	
        	event.manager.soundPoolSounds.addSound("pio/senor.wav", NanoLaser.class.getResource("/pio/NanoLaser/se/se04.wav"));            
            event.manager.soundPoolSounds.addSound("pio/selong.wav", NanoLaser.class.getResource("/pio/NanoLaser/se/se03.wav"));   
        	event.manager.soundPoolSounds.addSound("pio/sespr.wav", NanoLaser.class.getResource("/pio/NanoLaser/se/se03_s.wav"));            
            event.manager.soundPoolSounds.addSound("pio/sefire.wav", NanoLaser.class.getResource("/pio/NanoLaser/se/se04_f.wav"));
        	event.manager.soundPoolSounds.addSound("pio/seexp.wav", NanoLaser.class.getResource("/pio/NanoLaser/se/se03_e.wav"));                        
        } 
        catch (Exception e)
        {
            System.err.println("Failed to register one or more sounds.");
        }
    }
}
