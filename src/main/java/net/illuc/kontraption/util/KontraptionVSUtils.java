package net.illuc.kontraption.util;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3d;
import org.joml.Vector3ic;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;


// BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE BRAIN DAMAGE
public class KontraptionVSUtils {
    public static org.valkyrienskies.core.api.ships.Ship getShipManagingPos(ServerLevel serverLevel, int chunkX, int chunkZ){
        return VSGameUtilsKt.getShipManagingPos(serverLevel, chunkX, chunkZ);
    }

    public static org.valkyrienskies.core.api.ships.LoadedShip getShipManagingPos(Level level, BlockPos blockPos){
        return VSGameUtilsKt.getLoadedShipManagingPos(level, blockPos);
    }

    public static org.valkyrienskies.core.api.ships.LoadedServerShip getShipManagingPos(ServerLevel serverLevel, BlockPos blockPos){
        return VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, blockPos);
    }

    public static org.valkyrienskies.core.api.ships.LoadedServerShip getShipObjectManagingPos(ServerLevel serverLevel, BlockPos blockPos){
        return VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, blockPos);
    }

    public static org.valkyrienskies.core.api.ships.LoadedShip getShipObjectManagingPos(Level level, BlockPos blockPos){
        return VSGameUtilsKt.getLoadedShipManagingPos(level, blockPos);
    }

    public static org.valkyrienskies.core.api.ships.LoadedShip getShipObjectManagingPos(Level level, Vector3d blockPos){
        return VSGameUtilsKt.getLoadedShipManagingPos(level, blockPos.x, blockPos.y, blockPos.z);
    }

    public static VsiServerShipWorld getShipObjectWorld(ServerLevel level){
        return VSGameUtilsKt.getShipObjectWorld(level);
    }


    public static void createNewShipWithBlocks(BlockPos pos, DenseBlockPosSet set, ServerLevel level){

        // use assembleToShip() method replace the outdated one to be compatible with VK 2.4.10
        java.util.List<net.minecraft.core.BlockPos> blockList = new java.util.ArrayList<>();
        for (org.joml.Vector3ic vec : set) {
            blockList.add(new net.minecraft.core.BlockPos(vec.x(), vec.y(), vec.z()));
        }
        org.valkyrienskies.mod.common.assembly.ShipAssembler.INSTANCE.assembleToShip(
                level,
                blockList,
                true,
                1.0,
                false
        );
    }
    public static String dimensionID(ServerLevel level){
        return  VSGameUtilsKt.getDimensionId(level);

    }
    public static void tpShip(ServerShipWorld s, ServerShip ss, ShipTeleportData std){
        ValkyrienSkiesMod.getVsCore().teleportShip(s,ss,std);
    }

}
