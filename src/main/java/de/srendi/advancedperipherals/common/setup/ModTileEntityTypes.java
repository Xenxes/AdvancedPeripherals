package de.srendi.advancedperipherals.common.setup;

import com.google.common.collect.Sets;
import de.srendi.advancedperipherals.common.blocks.tileentity.ChatBoxTileEntity;
import de.srendi.advancedperipherals.common.blocks.tileentity.EnvironmentDetectorTileEntiy;
import de.srendi.advancedperipherals.common.blocks.tileentity.PlayerDetectorTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class ModTileEntityTypes {


    public static final RegistryObject<TileEntityType<ChatBoxTileEntity>> CHAT_BOX = Registration.TILE_ENTITIES.register("chat_box", ()->new TileEntityType<>(ChatBoxTileEntity::new, Sets.newHashSet(ModBlocks.CHAT_BOX.get()), null));

    public static final RegistryObject<TileEntityType<EnvironmentDetectorTileEntiy>> ENVIRONMENT_DETECTOR = Registration.TILE_ENTITIES.register("environment_detector", ()->new TileEntityType<>(EnvironmentDetectorTileEntiy::new, Sets.newHashSet(ModBlocks.ENVIRONMENT_DETECTOR.get()), null));

    public static final RegistryObject<TileEntityType<PlayerDetectorTileEntity>> PLAYER_DETECTOR = Registration.TILE_ENTITIES.register("player_detector", ()->new TileEntityType<>(PlayerDetectorTileEntity::new, Sets.newHashSet(ModBlocks.PLAYER_DETECTOR.get()), null));


    static void register() {

    }
}