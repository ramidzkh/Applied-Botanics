package appbot.ae2;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.util.IVariantConversion;

@SuppressWarnings("UnstableApiUsage")
public class Reflect {

    public static <V extends TransferVariant<?>> StackImportStrategy newStorageImportStrategy(
            BlockApiLookup<Storage<V>, Direction> apiLookup, IVariantConversion<V> conversion, ServerLevel level,
            BlockPos fromPos, Direction fromSide) {
        try {
            var klass = Class.forName("appeng.parts.automation.StorageImportStrategy");
            var constructor = klass.getConstructor(BlockApiLookup.class, IVariantConversion.class, ServerLevel.class,
                    BlockPos.class, Direction.class);
            constructor.setAccessible(true);
            return (StackImportStrategy) constructor.newInstance(apiLookup, conversion, level, fromPos, fromSide);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static <V extends TransferVariant<?>> StackExportStrategy newStorageExportStrategy(
            BlockApiLookup<Storage<V>, Direction> apiLookup, IVariantConversion<V> conversion, ServerLevel level,
            BlockPos fromPos, Direction fromSide) {
        try {
            var klass = Class.forName("appeng.parts.automation.StorageExportStrategy");
            var constructor = klass.getConstructor(BlockApiLookup.class, IVariantConversion.class, ServerLevel.class,
                    BlockPos.class, Direction.class);
            constructor.setAccessible(true);
            return (StackExportStrategy) constructor.newInstance(apiLookup, conversion, level, fromPos, fromSide);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
