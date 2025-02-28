/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.proxy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.entity.GaiaGuardianEntity;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.IMultiblock;

import java.util.function.Supplier;

public interface Proxy {
	Proxy INSTANCE = make();

	private static Proxy make() {
		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			return new ClientProxy();
		} else {
			return new Proxy() {};
		}
	}

	default void runOnClient(Supplier<Runnable> s) {}

	@Nullable
	default Player getClientPlayer() {
		return null;
	}

	default void lightningFX(Level level, Vec3 vectorStart, Vec3 vectorEnd, float ticksPerMeter, int colorOuter, int colorInner) {
		lightningFX(level, vectorStart, vectorEnd, ticksPerMeter, System.nanoTime(), colorOuter, colorInner);
	}

	default void lightningFX(Level level, Vec3 vectorStart, Vec3 vectorEnd, float ticksPerMeter, long seed, int colorOuter, int colorInner) {

	}

	default void addBoss(GaiaGuardianEntity boss) {

	}

	default void removeBoss(GaiaGuardianEntity boss) {

	}

	default int getClientRenderDistance() {
		return 0;
	}

	/** Same as {@code world.addParticle(data, true, ...)}, but culls particles below 32 block distances. */
	default void addParticleForceNear(Level world, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {}

	default void showMultiblock(IMultiblock mb, Component name, BlockPos anchor, Rotation rot) {}

	default void clearSextantMultiblock() {}

	@Nullable
	default HitResult getClientHit() {
		return null;
	}
}
