package pio.NanoLaser;

import org.lwjgl.util.glu.Registry;

import net.minecraft.block.Block;
import net.minecraft.dispenser.RegistrySimple;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.RecipesCrafting;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import ic2.api.*;

@Mod(modid="NanoLaser", name="Nano Laser", version="1.1.0")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
public class NanoLaser {

        // The instance of your mod that Forge uses.
        @Instance("NanoLaser")
        public static NanoLaser instance;
        
        // Says where the client and server 'proxy' code is loaded.
        @SidedProxy(clientSide="pio.NanoLaser.client.ClientProxy", serverSide="pio.NanoLaser.CommonProxy")
        public static CommonProxy proxy;
        
        private static int useItemID = 1600;
        private static int useEntiID = 160;
        
        private static Item itemNanoLaserA;
        private static Item itemNanoLaserM;
        private static Item itemQuantumLaser;
        
        @PreInit
        public void preInit(FMLPreInitializationEvent event) {
            // Stub Method
        	MinecraftForge.EVENT_BUS.register( new PioEventSounds() );
        	
        	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

            config.load();
        	itemNanoLaserA = new ItemNanoLaserA(config.getItem("NanoLaserAssault", 29000).getInt(), 0);        
        	itemNanoLaserM = new ItemNanoLaserM(config.getItem("NanoLaserMining", 29001).getInt(), 1);    
        	itemQuantumLaser = new ItemQuantumLaser(config.getItem("QuantumLaser", 29002).getInt(), 2);
        	
        	//IconTexture address
        	Property IconTexP = config.get(Configuration.CATEGORY_GENERAL, "x32Texture", false);
            
            // Here we add a comment to our new property.
            IconTexP.comment = "false : default  true : x32";
            
            if( IconTexP.getBoolean(false) ){
            	CommonProxy.NL_ITEMPNG = "/pio/NanoLaser/sprites/NanoLaserItems32x.png";
            }
        	
			//add item
			LanguageRegistry.addName(itemNanoLaserA, "Nano Laser Assault");
			LanguageRegistry.addName(itemNanoLaserM, "Nano Laser Mining");
			LanguageRegistry.addName(itemQuantumLaser, "Quantum Laser");
			
			config.save();
        }
        
        @Init
        public void load(FMLInitializationEvent event) {
			proxy.registerRenderers();
			
			// return: entClass, entName, ID, mod, trackingRange, updateFrequency, sendVelocityUpdates // Just like before, just called
			EntityRegistry.registerModEntity(EntityNanoLaserAssault.class, "EntityNanoLaserAssault", 
					useEntiID, this, 512, 1, true);
			EntityRegistry.registerModEntity(EntityNanoLaserMining.class, "EntityNanoLaserMining", 
					useEntiID + 1, this, 512, 1, true);
			
        }
        
        @PostInit
        public void postInit(FMLPostInitializationEvent event) {
        	// Stub Method
        	if( !Loader.isModLoaded( "IC2" ) ){
        		KeyBindingRegistry.registerKeyBinding(new PioKeyHandler());
        	}
        	
			//ic2 item
			ItemStack mininglaser = Items.getItem("miningLaser");
			ItemStack heatcell = Items.getItem("reactorHeatpack");
			ItemStack coolcell = Items.getItem("reactorCoolantSimple");
			ItemStack encry = Items.getItem("energyCrystal");
			ItemStack reglass = Items.getItem("reinforcedGlass");
			
			ItemStack iridiump = Items.getItem("iridiumPlate");
			ItemStack quauran = Items.getItem("reactorUraniumQuad");
			ItemStack lapcry = Items.getItem("lapotronCrystal");
			
			//vanilla item
			ItemStack diamond = new ItemStack(Item.diamond);
			ItemStack gsdust = new ItemStack(Item.lightStoneDust);
			
			ItemStack bdiamond = new ItemStack(Block.blockDiamond);
			
			if( mininglaser != null ){
				Ic2Recipes.addCraftingRecipe( new ItemStack( itemNanoLaserA ),
			    		"DE ","GRE"," CM",
			    		'D', diamond,
			    		'E', encry,
			    		'G', gsdust,
			    		'R', reglass,
			    		'C', coolcell,
			    		'M', mininglaser);
			    
				Ic2Recipes.addCraftingRecipe( new ItemStack( itemNanoLaserM ),
			    		"DE ","GRE"," HM",
			    		'D', diamond,
			    		'E', encry,
			    		'G', gsdust,
			    		'R', reglass,
			    		'H', heatcell,
			    		'M', mininglaser);	
				
				Ic2Recipes.addCraftingRecipe( new ItemStack( itemQuantumLaser ),
			    		"BLI","IAL"," IM",
			    		'B', bdiamond,
			    		'L', lapcry,
			    		'I', iridiump,
			    		'A', new ItemStack( itemNanoLaserA ),
			    		'M', new ItemStack( itemNanoLaserM ));	
			}else{
				GameRegistry.addRecipe( new ItemStack( itemNanoLaserA ),
			    		"D","G","L",
			    		'D', diamond,
			    		'G', gsdust,
			    		'L', new ItemStack(Item.bucketWater));
			    
				GameRegistry.addRecipe( new ItemStack( itemNanoLaserM ),
			    		"D","G","W",
			    		'D', diamond,
			    		'G', gsdust,
			    		'W', new ItemStack(Item.bucketLava));
				
				GameRegistry.addRecipe( new ItemStack( itemQuantumLaser ),
			    		"D","A","M",
			    		'D', bdiamond,
			    		'A', new ItemStack( itemNanoLaserA ),
			    		'M', new ItemStack( itemNanoLaserM ));				
			}
        }
}
