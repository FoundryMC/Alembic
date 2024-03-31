package foundry.alembic.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.stats.entity.AlembicEntityStats;
import foundry.alembic.stats.entity.EntityStatsManager;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.stats.item.ItemStatManager;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class AlembicCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = LiteralArgumentBuilder.literal("alembic");
        cmd.then(Commands.literal("dump")
                .then(Commands.literal("damage_types")
                        .executes((context) -> {
                            Collection<AlembicDamageType> types = DamageTypeManager.getDamageTypes();
                            List<ResourceLocation> vanillaTypes = context.getSource().getLevel().registryAccess().registry(Registries.DAMAGE_TYPE).get().keySet().stream().toList();
                            Multimap<String, ResourceLocation> vanillaTypesMap = ArrayListMultimap.create();
                            for (ResourceLocation type : vanillaTypes) {
                                vanillaTypesMap.put(type.getNamespace(), type);
                            }
                            // write all damage types to a file
                            Path writer = null;
                            try {
                                // if ./alembic doesn't exist, create it
                                File dir = new File("./alembic");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                writer = Paths.get("./alembic/damage_types.txt");
                                List<String> lines = new ArrayList<>();
                                lines.add("### Alembic Damage Types ###");
                                for (AlembicDamageType type : types) {
                                    lines.add(type.getId().toString());
                                }
                                lines.add("\n");
                                lines.add("\n");
                                lines.add("\n");
                                lines.add("### Vanilla Damage Types ###");
                                for (Map.Entry<String, Collection<ResourceLocation>> entry : vanillaTypesMap.asMap().entrySet()) {
                                    lines.add(entry.getKey() + ":");
                                    for (ResourceLocation type : entry.getValue()) {
                                        lines.add("  -  " + type.toString());
                                        AlembicOverride override = OverrideManager.get(context.getSource().getLevel().registryAccess().registry(Registries.DAMAGE_TYPE).get().get(type));
                                        if (override != null) {
                                            override.getDamagePercents().forEach((key, value) -> {
                                                lines.add("    - " + key + " -> " + value);
                                            });
                                        }
                                    }
                                }
                                Files.write(writer, lines);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return 0;
                        }))
                .then(Commands.literal("overrides")
                        .executes((context) -> {
                            Map<DamageType, AlembicOverride> overrides = OverrideManager.getOverrides();
                            // write all damage types to a file
                            Path writer = null;
                            try {
                                // if ./alembic doesn't exist, create it
                                File dir = new File("./alembic");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                writer = Paths.get("./alembic/overrides.txt");
                                List<String> lines = new ArrayList<>();
                                for (Map.Entry<DamageType, AlembicOverride> entry : overrides.entrySet()) {
                                    lines.add(entry.getKey().toString() + " -> " + entry.getValue().getId().toString());
                                }
                                Files.write(writer, lines);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            return 0;
                        }))
                .then(Commands.literal("resistances")
                        .executes((context) -> {
                            Map<EntityType<?>, AlembicEntityStats> overrides = EntityStatsManager.getView();
                            // write all damage types to a file
                            Path writer = null;
                            try {
                                // if ./alembic doesn't exist, create it
                                File dir = new File("./alembic");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                writer = Paths.get("./alembic/entity_resistances.txt");
                                List<String> lines = new ArrayList<>();
                                Collection<EntityType<?>> livingEntities = ForgeRegistries.ENTITY_TYPES.getValues();
                                Multimap<String, EntityType<?>> modEntityTypes = ArrayListMultimap.create();
                                for (EntityType<?> type : livingEntities) {
                                    if (type.create(context.getSource().getLevel()) instanceof LivingEntity) {
                                        modEntityTypes.put(ForgeRegistries.ENTITY_TYPES.getKey(type).getNamespace(), type);
                                    }
                                }
                                for (Map.Entry<String, Collection<EntityType<?>>> entry : modEntityTypes.asMap().entrySet()) {
                                    lines.add(entry.getKey() + ": ");
                                    for (EntityType<?> type : entry.getValue()) {
                                        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
                                        AlembicEntityStats stats = EntityStatsManager.get(type);
                                        StringBuilder builder = new StringBuilder();
                                        builder.append(id.toString()).append(" -> \n");
                                        if (stats != null) {
                                            builder.append(statsToString(stats.getResistances()));
                                        } else {
                                            builder.append("No stats found \n");
                                        }
                                        lines.add(builder.toString());
                                    }
                                }
                                Files.write(writer, lines);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            return 0;
                        }))
                .then(Commands.literal("items")
                        .executes((context) -> {
                            Map<Item, Multimap<EquipmentSlotType, ItemStat>> overrides = ItemStatManager.getStats();
                            // write all damage types to a file
                            Path writer = null;
                            try {
                                // if ./alembic doesn't exist, create it
                                File dir = new File("./alembic");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                writer = Paths.get("./alembic/items.txt");
                                List<String> lines = new ArrayList<>();
                                for (Map.Entry<Item, Multimap<EquipmentSlotType, ItemStat>> entry : overrides.entrySet()) {
                                    lines.add(entry.getKey().getDefaultInstance().getDisplayName().getString() + " -> \n" + formatItemStat(entry.getValue()));
                                }
                                Files.write(writer, lines);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            return 0;
                        }))
        );
        dispatcher.register(cmd);
    }

    public static String statsToString(Reference2FloatMap<AlembicDamageType> stats) {
        StringBuilder builder = new StringBuilder();
        for (Reference2FloatMap.Entry<AlembicDamageType> entry : stats.reference2FloatEntrySet()) {
            builder.append("    - ").append(entry.getKey().getId().toString()).append(" -> ").append(entry.getFloatValue()).append("\n");
        }
        return builder.toString();
    }

    public static String formatItemStat(Multimap<EquipmentSlotType, ItemStat> stat) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<EquipmentSlotType, ItemStat> entry : stat.entries()) {
            builder.append("    - ").append(entry.getKey().getName()).append(" -> ").append(entry.getValue().toString()).append("\n");
        }
        return builder.toString();
    }
}
