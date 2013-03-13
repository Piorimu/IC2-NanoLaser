package pio.NanoLaser;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class PioKeyHandler  extends KeyHandler{
    static KeyBinding myBinding = new KeyBinding("PioBind", Keyboard.KEY_M);
    
    static public boolean keyTable[] = new boolean[4];
    
    public PioKeyHandler()
    {
        //the first value is an array of KeyBindings, the second is whether or not the call 
        //keyDown should repeat as long as the key is down
        super(new KeyBinding[]{myBinding}, new boolean[]{false});
    }
 
    @Override
    public String getLabel()
    {
        return "piokeybindings";
    }
 
    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
        //do whatever
    	if( kb.isPressed() ){
    		keyTable[0] = true;
    	}
    }
 
    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
    {
        //do whatever
    	//if( kb.isPressed() ){
    		keyTable[0] = false;
    	//}    	
    }
 
    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
        //I am unsure if any different TickTypes have any different effects.
    }
}
