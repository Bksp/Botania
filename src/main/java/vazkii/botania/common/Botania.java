/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.capability.FloatingFlowerImpl;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.item.IExoflameHeatable;
import vazkii.botania.api.item.IFloatingFlower;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.fx.ModParticles;
import vazkii.botania.common.advancements.*;
import vazkii.botania.common.block.ModBanners;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.ModFluffBlocks;
import vazkii.botania.common.block.ModSubtiles;
import vazkii.botania.common.block.string.BlockRedStringInterceptor;
import vazkii.botania.common.block.subtile.functional.SubTileDaffomill;
import vazkii.botania.common.block.subtile.functional.SubTileLoonuim;
import vazkii.botania.common.block.subtile.functional.SubTileVinculotus;
import vazkii.botania.common.block.subtile.generating.SubTileNarslimmus;
import vazkii.botania.common.block.tile.ModTiles;
import vazkii.botania.common.block.tile.TileAlfPortal;
import vazkii.botania.common.block.tile.TileEnchanter;
import vazkii.botania.common.block.tile.TileTerraPlate;
import vazkii.botania.common.block.tile.corporea.TileCorporeaIndex;
import vazkii.botania.common.brew.ModBrews;
import vazkii.botania.common.brew.ModPotions;
import vazkii.botania.common.brew.potion.PotionBloodthirst;
import vazkii.botania.common.brew.potion.PotionEmptiness;
import vazkii.botania.common.brew.potion.PotionSoulCross;
import vazkii.botania.common.capability.NoopCapStorage;
import vazkii.botania.common.capability.NoopExoflameHeatable;
import vazkii.botania.common.core.command.SkyblockCommand;
import vazkii.botania.common.core.handler.*;
import vazkii.botania.common.core.helper.ColorHelper;
import vazkii.botania.common.core.loot.DisposeModifier;
import vazkii.botania.common.core.loot.LootHandler;
import vazkii.botania.common.core.loot.ModLootModifiers;
import vazkii.botania.common.core.proxy.IProxy;
import vazkii.botania.common.core.proxy.ServerProxy;
import vazkii.botania.common.crafting.ModRecipeTypes;
import vazkii.botania.common.entity.EntityDoppleganger;
import vazkii.botania.common.entity.ModEntities;
import vazkii.botania.common.impl.BotaniaAPIImpl;
import vazkii.botania.common.impl.corporea.CorporeaItemStackMatcher;
import vazkii.botania.common.impl.corporea.CorporeaStringMatcher;
import vazkii.botania.common.item.ItemGrassSeeds;
import vazkii.botania.common.item.ItemKeepIvy;
import vazkii.botania.common.item.ItemVirus;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemGoddessCharm;
import vazkii.botania.common.item.material.ItemEnderAir;
import vazkii.botania.common.item.relic.ItemLokiRing;
import vazkii.botania.common.item.relic.ItemOdinRing;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.common.network.PacketHandler;
import vazkii.botania.common.world.ModFeatures;
import vazkii.botania.common.world.SkyblockChunkGenerator;
import vazkii.botania.common.world.SkyblockWorldEvents;
import vazkii.botania.data.DataGenerators;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class Botania implements ModInitializer {

	public static boolean gardenOfGlassLoaded = false;

	public static boolean curiosLoaded = false;

	public static IProxy proxy;
	public static boolean finishedLoading = false;

	public static final Logger LOGGER = LogManager.getLogger(LibMisc.MOD_ID);

	@Override
	public void onInitialize() {
		proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
		proxy.registerHandlers();
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::commonSetup);
		modBus.addListener(IMCSender::enqueue);
		modBus.addListener(this::loadComplete);
		modBus.addListener(DataGenerators::gatherData);
		ModFeatures.registerFeatures();
		ModItems.registerItems();
		modBus.addGenericListener(ScreenHandlerType.class, ModItems::registerContainers);
		modBus.addGenericListener(RecipeSerializer.class, ModItems::registerRecipeSerializers);
		ModEntities.registerEntities();
		ModRecipeTypes.registerRecipeTypes();
		ModSounds.init();
		ModBrews.registerBrews();
		ModPotions.registerPotions();
		ModBlocks.registerBlocks();
		ModBlocks.registerItemBlocks();
		ModTiles.registerTiles();
		ModFluffBlocks.registerBlocks();
		ModFluffBlocks.registerItemBlocks();
		ModParticles.registerParticles();
		ModSubtiles.registerBlocks();
		ModSubtiles.registerItemBlocks();
		ModSubtiles.registerTEs();
		modBus.addGenericListener(GlobalLootModifierSerializer.class, DisposeModifier::register);
		PixieHandler.registerAttribute();

		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(this::serverAboutToStart);
		forgeBus.addListener(this::serverStarting);
		forgeBus.addListener(this::serverStopping);
		forgeBus.addListener(ItemLokiRing::onPlayerInteract);
		forgeBus.addListener(ItemOdinRing::onPlayerAttacked);
		forgeBus.addListener(ItemEnderAir::onPlayerInteract);
		forgeBus.addListener(ItemGoddessCharm::onExplosion);
		forgeBus.addListener(ItemGrassSeeds::onTickEnd);
		forgeBus.addListener(ItemKeepIvy::onPlayerDrops);
		forgeBus.addListener(ItemKeepIvy::onPlayerRespawn);
		forgeBus.addListener(ItemVirus::onLivingHurt);
		forgeBus.addListener(SleepingHandler::trySleep);
		forgeBus.addListener(PixieHandler::onDamageTaken);
		forgeBus.addGenericListener(BlockEntity.class, ExoflameFurnaceHandler::attachFurnaceCapability);
		forgeBus.addListener(CommonTickHandler::onTick);
		forgeBus.addListener(PotionBloodthirst::onSpawn);
		forgeBus.addListener(PotionEmptiness::onSpawn);
		forgeBus.addListener(PotionSoulCross::onEntityKill);
		forgeBus.addListener(SubTileNarslimmus::onSpawn);
		forgeBus.addListener(SubTileDaffomill::onItemTrack);
		forgeBus.addListener(SubTileVinculotus::onEndermanTeleport);
		forgeBus.addListener(EventPriority.LOWEST, SubTileLoonuim::onDrops);
		forgeBus.addListener(BlockRedStringInterceptor::onInteract);
		forgeBus.addListener(ManaNetworkHandler.instance::onNetworkEvent);
		forgeBus.addListener(EventPriority.HIGHEST, TileCorporeaIndex.getInputHandler()::onChatMessage);
		forgeBus.addListener(LootHandler::lootLoad);

		ModLootModifiers.init();
		ModCriteriaTriggers.init();
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(IFloatingFlower.class, new IFloatingFlower.Storage(), FloatingFlowerImpl::new);
		CapabilityManager.INSTANCE.register(IExoflameHeatable.class, new NoopCapStorage<>(), NoopExoflameHeatable::new);

		gardenOfGlassLoaded = ModList.get().isLoaded("gardenofglass");
		curiosLoaded = ModList.get().isLoaded("curios");

		PacketHandler.init();

		EquipmentHandler.init();
		CorporeaHelper.instance().registerRequestMatcher(prefix("string"), CorporeaStringMatcher.class, CorporeaStringMatcher::createFromNBT);
		CorporeaHelper.instance().registerRequestMatcher(prefix("item_stack"), CorporeaItemStackMatcher.class, CorporeaItemStackMatcher::createFromNBT);

		if (Botania.gardenOfGlassLoaded) {
			MinecraftForge.EVENT_BUS.addListener(SkyblockWorldEvents::onPlayerJoin);
			MinecraftForge.EVENT_BUS.addListener(SkyblockWorldEvents::onPlayerInteract);
		}

		DeferredWorkQueue.runLater(() -> {
			SkyblockChunkGenerator.init();

			DefaultAttributeRegistry.put(ModEntities.DOPPLEGANGER, MobEntity.createMobAttributes()
					.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
					.add(EntityAttributes.GENERIC_MAX_HEALTH, EntityDoppleganger.MAX_HP)
					.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
					.build());
			DefaultAttributeRegistry.put(ModEntities.PIXIE, MobEntity.createMobAttributes()
					.add(EntityAttributes.GENERIC_MAX_HEALTH, 2.0)
					.build());
			DefaultAttributeRegistry.put(ModEntities.PINK_WITHER, WitherEntity.createWitherAttributes().build());
			ModBanners.init();
			ColorHelper.init();

			PatchouliAPI.instance.registerMultiblock(Registry.BLOCK.getId(ModBlocks.alfPortal), TileAlfPortal.MULTIBLOCK.get());
			PatchouliAPI.instance.registerMultiblock(Registry.BLOCK.getId(ModBlocks.terraPlate), TileTerraPlate.MULTIBLOCK.get());
			PatchouliAPI.instance.registerMultiblock(Registry.BLOCK.getId(ModBlocks.enchanter), TileEnchanter.MULTIBLOCK.get());

			String[][] pat = new String[][] {
					{
							"P_______P",
							"_________",
							"_________",
							"_________",
							"_________",
							"_________",
							"_________",
							"_________",
							"P_______P",
					},
					{
							"_________",
							"_________",
							"_________",
							"_________",
							"____B____",
							"_________",
							"_________",
							"_________",
							"_________",
					},
					{
							"_________",
							"_________",
							"_________",
							"___III___",
							"___I0I___",
							"___III___",
							"_________",
							"_________",
							"_________",
					}
			};
			IStateMatcher sm = PatchouliAPI.instance.predicateMatcher(Blocks.IRON_BLOCK,
					state -> state.isIn(BlockTags.BEACON_BASE_BLOCKS));
			IMultiblock mb = PatchouliAPI.instance.makeMultiblock(
					pat,
					'P', ModBlocks.gaiaPylon,
					'B', Blocks.BEACON,
					'I', sm,
					'0', sm
			);
			PatchouliAPI.instance.registerMultiblock(prefix("gaia_ritual"), mb);

			ModBlocks.addDispenserBehaviours();

			ModFeatures.addWorldgen();
		});
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		finishedLoading = true;
	}

	private void serverAboutToStart(FMLServerAboutToStartEvent event) {
		if (BotaniaAPI.instance().getClass() != BotaniaAPIImpl.class) {
			String clname = BotaniaAPI.instance().getClass().getName();
			throw new IllegalAccessError("The Botania API has been overriden. "
					+ "This will cause crashes and compatibility issues, and that's why it's marked as"
					+ " \"Do not Override\". Whoever had the brilliant idea of overriding it needs to go"
					+ " back to elementary school and learn to read. (Actual classname: " + clname + ")");
		}
	}

	private void serverStarting(FMLServerStartingEvent event) {
		if (Botania.gardenOfGlassLoaded) {
			SkyblockCommand.register(event.getCommandDispatcher());
		}
	}

	private void serverStopping(FMLServerStoppingEvent event) {
		ManaNetworkHandler.instance.clear();
	}

}
