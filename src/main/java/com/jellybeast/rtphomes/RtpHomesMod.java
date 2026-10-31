package com.jellybeast.rtphomes;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RtpHomesMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rtp")
            .executes(ctx -> randomTeleport(ctx.getSource(), "overworld"))
            .then(Commands.literal("overworld").executes(ctx -> randomTeleport(ctx.getSource(), "overworld")))
            .then(Commands.literal("nether").executes(ctx -> randomTeleport(ctx.getSource(), "nether"))));
    }

    private static int randomTeleport(CommandSourceStack source, String dimension) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        ServerLevel target = dimension.equals("nether")
            ? source.getServer().getLevel(Level.NETHER)
            : source.getServer().getLevel(Level.OVERWORLD);
        if (target == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("That dimension is unavailable."));
            return 0;
        }

        WorldBorder border = target.getWorldBorder();
        double minX = border.getMinX();
        double maxX = border.getMaxX();
        double minZ = border.getMinZ();
        double maxZ = border.getMaxZ();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 350; attempt++) {
            double x = random.nextDouble(minX, maxX);
            double z = random.nextDouble(minZ, maxZ);
            int y = random.nextInt(target.getMinY() + 1, target.getMaxY() - 2);
            BlockPos feet = BlockPos.containing(x, y, z);
            if (safeSpot(target, feet)) {
                player.teleportTo(target, x + 0.5, y, z + 0.5, Set.of(), player.getYRot(), player.getXRot());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Randomly teleported to the " + (dimension.equals("nether") ? "Nether" : "Overworld") + "."));
                return 1;
            }
        }

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Couldn't find a safe location. Try /rtp again."));
        return 0;
    }

    private static boolean safeSpot(ServerLevel level, BlockPos feet) {
        BlockState floor = level.getBlockState(feet.below());
        BlockState body = level.getBlockState(feet);
        BlockState head = level.getBlockState(feet.above());
        if (floor.getCollisionShape(level, feet.below()).isEmpty()
            || !body.getCollisionShape(level, feet).isEmpty()
            || !head.getCollisionShape(level, feet.above()).isEmpty()) return false;
        if (!body.getFluidState().isEmpty() || !head.getFluidState().isEmpty()) return false;
        if (floor.is(Blocks.MAGMA_BLOCK) || floor.is(Blocks.CAMPFIRE)
            || floor.is(Blocks.SOUL_CAMPFIRE) || floor.is(Blocks.CACTUS)
            || floor.is(Blocks.FIRE) || floor.is(Blocks.SOUL_FIRE)
            || floor.is(Blocks.POWDER_SNOW)) return false;
        return level.getWorldBorder().isWithinBounds(feet);
    }
}
