package com.notunanancyowen.spears.fabric;

import com.notunanancyowen.spears.components.PiercingWeapon;
import com.notunanancyowen.spears.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.packets.TriggerStabEffectsC2SPacket;
import net.fabricmc.api.ModInitializer;

import com.notunanancyowen.spears.Spears;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.util.HashMap;

public final class SpearsFabric implements ModInitializer {
    private static HashMap<String, Boolean> makeConfig(){
        HashMap<String, Boolean> toggles = Spears.makeConfig();
        try{
            File config = FabricLoader.getInstance().getConfigDir().resolve(Spears.MOD_ID + "-common.toml").toFile();
            if(config.createNewFile()){
                try(FileWriter configWriter = new FileWriter(config)){
                    configWriter.write("#Whether spears have a charge attack\nspear_charge_attacks = true\n#Whether spears have a stabbing animation\nspear_stabbing_animation = true\n#Allow lunge when flying with an elytra\nspear_elytra_lunge = false\n#Allow lunge in water\nspear_water_lunge = false");
                }
            }
            try(FileReader configReader = new FileReader(config)){
                BufferedReader reader = new BufferedReader(configReader);
                StringBuilder bob = new StringBuilder();
                String configText;
                while((configText = reader.readLine()) != null){
                    bob.append(configText);
                    bob.append(System.lineSeparator());
                }
                bob.deleteCharAt(bob.length() - 1);
                reader.close();
                bob.toString().lines().forEach(configContent -> {
                    if(!configContent.startsWith("#")){
                        String[] configValues = configContent.replace(" ", "").split("=", 2);
                        toggles.putIfAbsent(configValues[0], configValues[1].contains("true"));
                    }
                });
            }
        }catch(IOException | SecurityException e){
            Spears.LOGGER.info("Error making config: %n" + e.getLocalizedMessage());
        }
        return toggles;
    }

    @Override
    public void onInitialize(){
        Spears.config = makeConfig();
        Spears.hasBetterCombat = FabricLoader.getInstance().isModLoaded("bettercombat");
        Spears.hasCopperAgeBackport = FabricLoader.getInstance().isModLoaded("copperagebackport");

        PayloadTypeRegistry.playC2S().register(PlayerStabC2SPacket.ID, PlayerStabC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TriggerStabEffectsC2SPacket.ID, TriggerStabEffectsC2SPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlayerStabC2SPacket.ID, (packet, context) -> {
            var p = packet.getEntity(context.player().getWorld());
            if(p instanceof ServerPlayerEntity sp){
                if(sp.getMainHandStack().get(Spears.PIERCING_WEAPON) instanceof PiercingWeapon pw){
                    pw.stab(sp, EquipmentSlot.MAINHAND);
                }
                sp.resetLastAttackedTicks();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TriggerStabEffectsC2SPacket.ID, (packet, context) -> {
            if(packet.toggle()) packet.trigger(context.player());
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> {
            itemGroup.addAfter(Items.NETHERITE_SWORD, Spears.WOODEN_SPEAR);
            itemGroup.addAfter(Spears.WOODEN_SPEAR, Spears.STONE_SPEAR);

            if(Spears.hasCopperAgeBackport){
                itemGroup.addAfter(Spears.STONE_SPEAR, Spears.COPPER_SPEAR);
                itemGroup.addAfter(Spears.COPPER_SPEAR, Spears.IRON_SPEAR);
            }else{
                itemGroup.addAfter(Spears.STONE_SPEAR, Spears.IRON_SPEAR);
            }

            itemGroup.addAfter(Spears.IRON_SPEAR, Spears.GOLDEN_SPEAR);
            itemGroup.addAfter(Spears.GOLDEN_SPEAR, Spears.DIAMOND_SPEAR);
            itemGroup.addAfter(Spears.DIAMOND_SPEAR, Spears.NETHERITE_SPEAR);
        });

        Spears.init();
    }
}
