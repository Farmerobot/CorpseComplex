/*
 * Copyright (c) 2017. <C4>
 *
 * This Java class is distributed as a part of Corpse Complex.
 * Corpse Complex is open source and licensed under the GNU General Public
 * License v3.
 * A copy of the license can be found here: https://www.gnu.org/licenses/gpl
 * .text
 */

package top.theillusivec4.corpsecomplex;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.corpsecomplex.common.CommonEventHandler;
import top.theillusivec4.corpsecomplex.common.DeathSettings;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.config.CorpseComplexConfig;
import top.theillusivec4.corpsecomplex.common.modules.EffectModule;
import top.theillusivec4.corpsecomplex.common.modules.ExperienceModule;
import top.theillusivec4.corpsecomplex.common.modules.HungerModule;
import top.theillusivec4.corpsecomplex.common.modules.MiscModule;
import top.theillusivec4.corpsecomplex.common.modules.inventory.InventoryModule;
import top.theillusivec4.corpsecomplex.common.modules.mementomori.MementoMoriModule;

@Mod(CorpseComplex.MODID)
public class CorpseComplex {

  public static final String MODID = "corpsecomplex";
  public static final Logger LOGGER = LogManager.getLogger();

  public static ModConfig overrideConfig;

  public CorpseComplex() {
    ModLoadingContext.get().registerConfig(Type.SERVER, CorpseComplexConfig.serverSpec);
    ModLoadingContext.get().registerConfig(Type.SERVER, CorpseComplexConfig.overridesSpec,
        "corpsecomplex-overrides.toml");
    File defaultOverrides = new File(
        FMLPaths.GAMEDIR.get() + "/defaultconfigs/corpsecomplex-overrides.toml");
    if (!defaultOverrides.exists()) {
      try {
        FileUtils.copyInputStreamToFile(Objects.requireNonNull(CorpseComplex.class.getClassLoader()
            .getResourceAsStream("corpsecomplex-overrides.toml")), defaultOverrides);
      } catch (IOException e) {
        LOGGER.error("can't config man");
      }
    }
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    eventBus.addListener(this::config);
    MinecraftForge.EVENT_BUS.register(new InventoryModule());
    MinecraftForge.EVENT_BUS.register(new ExperienceModule());
    MinecraftForge.EVENT_BUS.register(new HungerModule());
    MinecraftForge.EVENT_BUS.register(new EffectModule());
    MinecraftForge.EVENT_BUS.register(new MementoMoriModule());
    MinecraftForge.EVENT_BUS.register(new MiscModule());
    MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
  }

  private void setup(final FMLCommonSetupEvent evt) {
    DeathStorageCapability.register();
    InventoryModule.STORAGE_ADDONS.forEach((modid, clazz) -> {
      if (ModList.get().isLoaded(modid)) {
        try {
          InventoryModule.STORAGE.add(clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
          LOGGER.error("Error trying to instantiate storage module for mod " + modid);
        }
      }
    });
  }

  //  public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec, String fileName) {
  //    overrideConfig = new ModConfig(type, spec, ModLoadingContext.get().getActiveContainer(),
  //        fileName);
  //    ModLoadingContext.get().getActiveContainer().addConfig(overrideConfig);
  //  }

  private void config(final ModConfigEvent evt) {

    if (evt.getConfig().getModId().equals(MODID)) {
      ForgeConfigSpec spec = evt.getConfig().getSpec();

      //      if (spec == CorpseComplexConfig.serverSpec) {
      CorpseComplexConfig.bakeConfigs();
      //      } else if (spec == CorpseComplexConfig.overridesSpec) {
      //        final Path serverConfig = server.getActiveAnvilConverter()
      //            .getFile(server.getFolderName(), "serverconfig").toPath();
      //        final CommentedFileConfig configData = CommentedFileConfig
      //            .builder(evt.getConfig().getFullPath()).sync().autosave()
      //            .defaultResource("corpsecomplex-overrides.toml").writingMode(WritingMode.REPLACE).build();
      //        configData.load();
      //        spec.setConfig(configData);
      //      }
      CorpseComplexConfig.transform(evt.getConfig().getConfigData());
      DeathSettings.setConfigDefault();
    }
  }
}
