package com.modularwarfare.addon;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public interface IContentAddon {

    /**
     * Method called when the addon is being loaded
     *
     * @param contentManager - provides fast access to basic register methods like {@link AddonLoaderManager#registerEventClass(Object)}
     */
    public void construct(Side side, AddonLoaderManager contentManager);

    /**
     * Method called when the addon enter in preInit state
     *
     * @param contentManager - provides fast access to basic register methods like {@link AddonLoaderManager#registerEventClass(Object)}
     */

    public void preInit(FMLPreInitializationEvent event, AddonLoaderManager contentManager);

    public void init(FMLPostInitializationEvent event, AddonLoaderManager contentManager);

    /**
     * Gets called before the addon is being unloaded.
     */
    public void unload();

    /**
     * @return The name of the addon in user friendly format
     */
    public String getName();

    /**
     * Even though I don't care about the format, <strong>PLEASE</strong> include the minecraft version
     *
     * @return The version of this addon
     */
    public String getVersion();

    /**
     * You know what ids are for, be as unique as possible
     *
     * @return Unique identifier of this addon
     */
    public String getAddonID();
}
