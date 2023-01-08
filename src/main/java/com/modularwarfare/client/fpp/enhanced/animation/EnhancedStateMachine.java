package com.modularwarfare.client.fpp.enhanced.animation;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.Passer;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.fpp.basic.animations.StateEntry;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig.Animation;
import com.modularwarfare.client.fpp.enhanced.models.ModelEnhancedGun;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemAmmo;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.common.network.PacketGunReloadEnhancedStop;
import com.modularwarfare.common.network.PacketGunReloadSound;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class EnhancedStateMachine {

    /**
     * RELOAD
     */
    public float reloadTime;
    private ReloadType reloadType;
    public boolean reloading = false;
    public int reloadCount = 0;
    public int reloadMaxCount = 0;

    /**
     * Recoil
     */
    public float gunRecoil = 0F, lastGunRecoil = 0F;
    public float recoilSide = 0F;
    /**
     * Slide
     */
    public float gunSlide = 0F, lastGunSlide = 0F;

    /**
     * Shoot State Machine
     */
    public boolean shooting = false;
    private float shootTime;
    public int flashCount = 0;
    public boolean isFailedShoot = false;

    public ModelEnhancedGun currentModel;
    public Phase reloadPhase = Phase.PRE;
    public Phase lastReloadPhase = null;
    public Phase shootingPhase = Phase.PRE;

    public ItemStack heldItemstStack;

    public static enum Phase {
        PRE, FIRST, SECOND, POST
    }

    public void reset() {
        reloadTime = 0;
        reloadType = null;
        reloading = false;
        reloadCount = 0;
        reloadMaxCount = 0;
        gunRecoil = 0;
        lastGunRecoil = 0;
        recoilSide = 0;
        gunSlide = 0;
        lastGunSlide = 0;
        shooting = false;
        shootTime = 0;
        flashCount = 0;
        isFailedShoot = false;
        currentModel = null;
        reloadPhase = Phase.PRE;
        lastReloadPhase = null;
        shootingPhase = Phase.PRE;
        heldItemstStack=null;
    }

    public void triggerShoot(ModelEnhancedGun model, GunType gunType, int fireTickDelay) {
        triggerShoot(model, gunType, fireTickDelay, false);
    }

    public void triggerShoot(ModelEnhancedGun model, GunType gunType, int fireTickDelay, boolean isFailed) {
        lastGunRecoil = gunRecoil = 1F;
        lastGunSlide = gunSlide = 1F;

        shooting = true;
        shootTime = fireTickDelay;
        recoilSide = (float) (-1F + Math.random() * (1F - (-1F)));
        if (isFailed) {
            recoilSide = 0;
            lastGunRecoil = gunRecoil = 0F;
            lastGunSlide = gunSlide = 0F;
        }
        isFailedShoot = isFailed;
        this.shootingPhase = Phase.PRE;
        this.currentModel = model;
    }

    public void triggerReload(int reloadTime, int reloadCount, ModelEnhancedGun model, ReloadType reloadType) {
        reset();
        updateCurrentItem();
        this.reloadTime = reloadType != ReloadType.Full ? reloadTime * 0.65f : reloadTime;
        this.reloadCount = reloadCount;
        Item item = heldItemstStack.getItem();
        if (item instanceof ItemGun) {
            GunType type = ((ItemGun) item).type;
            if(reloadType==ReloadType.Unload) {
                this.reloadCount-=type.modifyUnloadBullets;  
            }
        }
        this.reloadMaxCount = reloadCount;
        this.reloadType = reloadType;
        this.reloadPhase = Phase.PRE;
        this.lastReloadPhase = null;
        this.reloading = true;
        this.currentModel = model;
    }

    public void onTickUpdate() {
        // Recoil
        lastGunRecoil = gunRecoil;
        if (gunRecoil > 0)
            gunRecoil *= 0.5F;
    }

    public ReloadType getReloadType() {
        return this.reloadType;
    }

    public AnimationType getReloadAnimationType() {
        AnimationType aniType = null;
        if (reloadType == ReloadType.Load) {
            ItemStack stack = heldItemstStack;
            Item item = stack.getItem();
            if (item instanceof ItemGun) {
                GunType type = ((ItemGun) item).type;
                if (type.acceptedAmmo != null) {
                    if (reloadPhase == Phase.FIRST) {
                        aniType = AnimationType.LOAD;
                    } else if (reloadPhase == Phase.POST) {
                        aniType = AnimationType.POST_LOAD;
                    } else if (reloadPhase == Phase.PRE) {
                        aniType = AnimationType.PRE_LOAD;
                    }
                } else {
                    //使用子弹的枪械
                    if (reloadPhase == Phase.FIRST) {
                        aniType = AnimationType.RELOAD_FIRST;
                    } else if (reloadPhase == Phase.SECOND) {
                        aniType = AnimationType.RELOAD_SECOND;
                    } else if (reloadPhase == Phase.POST) {
                        if (!ItemGun.hasNextShot(heldItemstStack) && ((GunEnhancedRenderConfig)currentModel.config).animations.containsKey(AnimationType.POST_RELOAD_EMPTY)) {
                            aniType = AnimationType.POST_RELOAD_EMPTY;
                        } else {
                            aniType = AnimationType.POST_RELOAD;
                        }
                    } else {
                        aniType = AnimationType.PRE_RELOAD;
                    }
                }
            }
        } else if (reloadType == ReloadType.Unload) {
            if (reloadPhase == Phase.FIRST) {
                aniType = AnimationType.UNLOAD;
            } else if (reloadPhase == Phase.POST) {
                aniType = AnimationType.POST_UNLOAD;
            } else if (reloadPhase == Phase.PRE) {
                aniType = AnimationType.PRE_UNLOAD;
            }
        } else if (reloadType == ReloadType.Full) {
            if (reloadPhase == Phase.FIRST) {
                if (ClientTickHandler.reloadEnhancedIsQuicklyRendering
                        && ((GunEnhancedRenderConfig)currentModel.config).animations.containsKey(AnimationType.RELOAD_FIRST_QUICKLY)) {
                    aniType = AnimationType.RELOAD_FIRST_QUICKLY;
                } else {
                    aniType = AnimationType.RELOAD_FIRST;
                }
            } else if (reloadPhase == Phase.SECOND) {
                if (ClientTickHandler.reloadEnhancedIsQuicklyRendering
                        && ((GunEnhancedRenderConfig)currentModel.config).animations.containsKey(AnimationType.RELOAD_SECOND_QUICKLY)) {
                    aniType = AnimationType.RELOAD_SECOND_QUICKLY;
                } else {
                    aniType = AnimationType.RELOAD_SECOND;
                }
            } else if (reloadPhase == Phase.POST) {
                if (!ItemGun.hasNextShot(heldItemstStack)
                        && ((GunEnhancedRenderConfig)currentModel.config).animations.containsKey(AnimationType.POST_RELOAD_EMPTY)) {
                    aniType = AnimationType.POST_RELOAD_EMPTY;
                } else {
                    aniType = AnimationType.POST_RELOAD;
                }
            } else {
                aniType = AnimationType.PRE_RELOAD;
            }
        }
        return aniType;
    }

    public AnimationType getShootingAnimationType() {
        AnimationType aniType = AnimationType.PRE_FIRE;
        if (shootingPhase == Phase.FIRST) {
            aniType = AnimationType.FIRE;
        } else if (shootingPhase == Phase.POST) {
            aniType = AnimationType.POST_FIRE;
        }
        if (isFailedShoot && shootingPhase != Phase.PRE) {
            return null;
        }
        return aniType;
    }
    
    public float getReloadSppedFactor() {
        ItemStack stack = heldItemstStack;
        Item item = stack.getItem();
        if (ClientProxy.gunEnhancedRenderer.controller != null) {
            if (item instanceof ItemGun) {
                GunType type = ((ItemGun) item).type;
                if (ItemGun.hasAmmoLoaded(stack)) {
                    ItemStack stackAmmo = new ItemStack(stack.getTagCompound().getCompoundTag("ammo"));
                    stackAmmo = ClientProxy.gunEnhancedRenderer.controller.getRenderAmmo(stackAmmo);
                    if (stackAmmo != null && stackAmmo.getItem() instanceof ItemAmmo) {
                        ItemAmmo itemAmmo = (ItemAmmo) stackAmmo.getItem();
                        return itemAmmo.type.reloadSpeedFactor;
                    }
                }
            }
        }
        return 1;
    }

    public void updateCurrentItem() {
        if (heldItemstStack != Minecraft.getMinecraft().player.getHeldItemMainhand()) {
            if (reloading) {
                stopReload();
            }
            if (!shooting) {
                reset();
            }
            //ClientTickHandler.reloadEnhancedPrognosisAmmo=ItemStack.EMPTY;
        }
        heldItemstStack = Minecraft.getMinecraft().player.getHeldItemMainhand();
    }

    public void onRenderTickUpdate(float partialTick) {
        if(ClientProxy.gunEnhancedRenderer.controller==null) {
            return;
        }
        ItemStack stack = heldItemstStack;
        Item item = stack.getItem();
        if (item instanceof ItemGun) {
            GunType type = ((ItemGun) item).type;
            if (reloading) {
                /** RELOAD **/
                AnimationType aniType = getReloadAnimationType();
                Passer<Phase> phase = new Passer(reloadPhase);
                Passer<Double> progess = new Passer(AnimationController.RELOAD);
                reloading = phaseUpdate(aniType, partialTick, getReloadSppedFactor(), phase, progess,()->{
                    if(reloadCount>0) {
                        phase.set(Phase.FIRST);  
                    }else {
                        phase.set(Phase.POST);
                    }
                }, () -> {
                    reloadCount--;
                    if (type.acceptedAmmo != null) {
                        phase.set(Phase.SECOND);
                    } else {
                        if (reloadCount <= 0) {
                            phase.set(Phase.POST);
                        } else {
                            phase.set(Phase.SECOND);
                        }
                    }
                }, () -> {
                    if (reloadCount <= 0) {
                        phase.set(Phase.POST);
                    } else {
                        phase.set(Phase.FIRST);
                    }
                });
                if (reloadPhase != lastReloadPhase && aniType!=null) {
                    switch (aniType) {
                    case PRE_LOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PreLoad));
                        break;
                    case LOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.Load));
                        break;
                    case POST_LOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PostLoad));
                        break;
                    case PRE_UNLOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PreUnload));
                        break;
                    case UNLOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.Unload));
                        break;
                    case POST_UNLOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PostUnload));
                        break;
                    case PRE_RELOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PreReload));
                        break;
                    case RELOAD_FIRST:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.Reload));
                        break;
                    case RELOAD_SECOND:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.ReloadSecond));
                        break;
                    case RELOAD_FIRST_QUICKLY:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.Reload));
                        break;
                    case RELOAD_SECOND_QUICKLY:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.ReloadSecond));
                        break;
                    case POST_RELOAD:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PostReload));
                        break;
                    case POST_RELOAD_EMPTY:
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReloadSound(WeaponSoundType.PostReloadEmpty));
                        break;
                    default:
                        break;
                    }
                }
                lastReloadPhase=reloadPhase;
                reloadPhase = phase.get();
                //System.out.println(reloadPhase+":"+getReloadAnimationType());
                AnimationController.RELOAD = progess.get();
                if (!reloading) {
                    ClientProxy.gunEnhancedRenderer.controller.updateActionAndTime();
                    stopReload();
                }
            }
            if (shooting) {
                /*
                shootProgress += 1F / shootTime;
                
                if (shootProgress >= 1F) {
                    shooting = false;
                    shootProgress = 0f;
                }
                */
                AnimationType aniType = getShootingAnimationType();
                Passer<Phase> phase = new Passer(shootingPhase);
                Passer<Double> progess = new Passer(AnimationController.FIRE);
                shooting = phaseUpdate(aniType, StateEntry.smoothing,1, phase, progess,()->{
                    phase.set(Phase.FIRST);
                }, () -> {
                    flashCount++;
                    phase.set(Phase.POST);
                }, null);
                shootingPhase = phase.get();
                AnimationController.FIRE = progess.get();
                if (!shooting) {
                    ClientProxy.gunEnhancedRenderer.controller.updateActionAndTime();
                }
            }
        }
    }

    public boolean phaseUpdate(AnimationType aniType, float partialTick,float speedFactor, Passer<Phase> phase, Passer<Double> progress,
            Runnable preCall,Runnable firstCall, Runnable secondCall) {
        boolean flag = true;
        Animation ani = null;
        if (aniType != null) {
            ani = ((GunEnhancedRenderConfig)currentModel.config).animations.get(aniType);
        }
        if (ani != null) {
            double speed = ani.getSpeed(currentModel.config.FPS) * speedFactor * partialTick;
            double val = progress.get() + speed;
            progress.set(val);
            if (progress.get() > 1) {
                progress.set(1D);
            } else if (progress.get() < 0) {
                progress.set(0D);
            }
        } else {
            progress.set(1D);
        }
        if (progress.get() >= 1F) {
            if (phase.get() == Phase.POST) {
                flag = false;
                progress.set(0D);
            } else if (phase.get() == Phase.FIRST) {
                progress.set(Double.MIN_VALUE);
                if (firstCall != null) {
                    firstCall.run();
                }
            } else if (phase.get() == Phase.SECOND) {
                progress.set(Double.MIN_VALUE);
                if (secondCall != null) {
                    secondCall.run();
                }
            } else if (phase.get() == Phase.PRE) {
                progress.set(Double.MIN_VALUE);
                if (preCall != null) {
                    preCall.run();
                }
            }
        }
        return flag;
    }

    public void stopReload() {
        PacketGunReloadEnhancedStop packet = null;
        ItemStack stack = heldItemstStack;
        Item item = stack.getItem();
        if (item instanceof ItemGun) {
            GunType type = ((ItemGun) item).type;
            
            AnimationType reloadAni=getReloadAnimationType();
            
            if (reloadType == ReloadType.Load) {
                if (type.acceptedAmmo != null) {
                    if (reloadPhase == Phase.PRE) {
                        packet = new PacketGunReloadEnhancedStop(0, false, false);
                    } else {
                        packet = new PacketGunReloadEnhancedStop(0, true, true);
                    }
                } else {
                    if (reloadPhase == Phase.PRE) {
                        packet = new PacketGunReloadEnhancedStop(0, false, false);
                    } else {
                        packet = new PacketGunReloadEnhancedStop(reloadMaxCount - reloadCount, true, true);
                    }
                }
            } else if (reloadType == ReloadType.Full) {
                if (type.acceptedAmmo != null) {
                    if (reloadPhase == Phase.POST || reloadPhase == Phase.SECOND) {
                        packet = new PacketGunReloadEnhancedStop(0, true, true);
                    } else if (reloadPhase == Phase.FIRST) {
                        packet = new PacketGunReloadEnhancedStop(0, true, false);
                    } else {
                        packet = new PacketGunReloadEnhancedStop(0, false, false);
                    }
                } else {
                    if (reloadPhase == Phase.PRE) {
                        packet = new PacketGunReloadEnhancedStop(0, false, false);
                    } else {
                        packet = new PacketGunReloadEnhancedStop(reloadMaxCount - reloadCount, true, true);
                    }
                }
            } else if (reloadType == ReloadType.Unload) {
                if (reloadPhase == Phase.PRE) {
                    packet = new PacketGunReloadEnhancedStop(0, false, false);
                } else {
                    if (type.acceptedAmmo != null) {
                        packet = new PacketGunReloadEnhancedStop(0, true, false);
                    } else {
                        packet = new PacketGunReloadEnhancedStop(reloadMaxCount - reloadCount, true, false);
                    }
                }
            }
            if(packet!=null) {
                if(type.acceptedAmmo!=null) {
                    if(packet.loaded) {
                        ItemStack ammoStack=ClientTickHandler.reloadEnhancedPrognosisAmmoRendering;
                        if(ammoStack!=null&&!ammoStack.isEmpty()) {
                            ammoStack.setItemDamage(0);
                            if(reloadAni==AnimationType.RELOAD_FIRST||reloadAni==AnimationType.RELOAD_FIRST_QUICKLY||reloadAni==AnimationType.UNLOAD) {
                                ammoStack=ItemStack.EMPTY;
                            }
                            if(ammoStack.getItem() instanceof ItemAmmo) {
                                heldItemstStack.getTagCompound().setTag("ammo", ammoStack.writeToNBT(new NBTTagCompound()));
                            }  
                        }
                    }else if(packet.unloaded) {
                        heldItemstStack.getTagCompound().removeTag("ammo");
                    }
                }else{
                    if(packet.loaded) {
                        ItemStack bulletStack = ClientTickHandler.reloadEnhancedPrognosisAmmoRendering;
                        if(bulletStack!=null&&!bulletStack.isEmpty()) {
                            bulletStack.setItemDamage(0);
                            int offset = getAmmoCountOffset(true);
                            int ammoCount = heldItemstStack.getTagCompound().getInteger("ammocount");
                            heldItemstStack.getTagCompound().setInteger("ammocount", ammoCount + offset);
                            heldItemstStack.getTagCompound().setTag("bullet", bulletStack.writeToNBT(new NBTTagCompound()));  
                        }
                    }else if(packet.unloaded) {
                        heldItemstStack.getTagCompound().setInteger("ammocount", 0);
                        heldItemstStack.getTagCompound().removeTag("bullet");  
                    }
                }  
                ModularWarfare.NETWORK.sendToServer(packet);  
                ClientTickHandler.reloadEnhancedPrognosisAmmo=null;
                ClientTickHandler.reloadEnhancedPrognosisAmmoRendering=null;
            }
            //System.out.println(reloadType+"-"+reloadPhase+"-"+packet);
        }
    }

    public boolean canSprint() {
        return !shooting && !reloading && AnimationController.ADS < 0.8f;
    }

    public int getAmmoCountOffset(boolean really) {
        ItemStack stack = heldItemstStack;
        if(heldItemstStack!=null) {
            Item item = stack.getItem();
            if (item instanceof ItemGun) {
                GunType type = ((ItemGun) item).type;
                if (reloading) {
                    if (reloadType == ReloadType.Unload) {
                        if(really) {
                            return -(reloadMaxCount - reloadCount);
                        }else {
                            return -(reloadMaxCount - reloadCount-type.modifyUnloadBullets);
                        }
                    } else {
                        return reloadMaxCount - reloadCount;
                    }
                }
            }  
        }
        if (reloadType == ReloadType.Unload) {
            return -reloadMaxCount;
        } else {
            return reloadMaxCount;
        }
    }

}
