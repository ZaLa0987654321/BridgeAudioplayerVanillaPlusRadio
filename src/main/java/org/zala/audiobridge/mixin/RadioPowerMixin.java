package org.zala.audiobridge.mixin;

import com.damir00109.blockentity.RadioBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RadioBlockEntity.class, remap = false)
public class RadioPowerMixin {

    @Redirect(
            method = "updateState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getReceivedRedstonePower(Lnet/minecraft/util/math/BlockPos;)I"
            ),
            remap = false
    )
    private int onGetReceivedRedstonePower(ServerWorld world, BlockPos pos) {
        int highestPower = 0;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) {
                continue;
            }

            int powerFromSide = world.getEmittedRedstonePower(pos.offset(direction), direction);
            if (powerFromSide > highestPower) {
                highestPower = powerFromSide;
            }
        }

        return highestPower;
    }
}
