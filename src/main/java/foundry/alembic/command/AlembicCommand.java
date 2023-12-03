package foundry.alembic.command;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.items.ItemStat;
import foundry.alembic.items.ItemStatManager;
import foundry.alembic.items.slots.EquipmentSlotType;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.resistances.AlembicEntityStats;
import foundry.alembic.resistances.ResistanceManager;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.Item;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AlembicCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = LiteralArgumentBuilder.literal("alembic");
        cmd.then(Commands.literal("dump")
                .then(Commands.literal("damage_types")
                        .executes((context) -> {
                            List<AlembicDamageType> types = DamageTypeManager.getDamageTypes().stream().toList();
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
                                for (AlembicDamageType type : types) {
                                    lines.add(type.getId().toString());
                                }
                                Files.write(writer, lines);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            return 0;
                        }))
                .then(Commands.literal("overrides")
                        .executes((context) -> {
                            Map<DamageSourceIdentifier, AlembicOverride> overrides = OverrideManager.getOverrides();
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
                                for (Map.Entry<DamageSourceIdentifier, AlembicOverride> entry : overrides.entrySet()) {
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
                            Collection<AlembicEntityStats> overrides = ResistanceManager.getValuesView();
                            // write all damage types to a file
                            Path writer = null;
                            try {
                                // if ./alembic doesn't exist, create it
                                File dir = new File("./alembic");
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                writer = Paths.get("./alembic/resistances.txt");
                                List<String> lines = new ArrayList<>();
                                for (AlembicEntityStats entry : overrides) {
                                    lines.add(entry.getEntityType().toString() + " -> \n" + statsToString(entry.getDamage()));
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
                                    lines.add(entry.getKey().getDefaultInstance().getDisplayName().getString() + " -> \n" + entry.getValue().toString());
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

    public static String statsToString(Object2FloatMap<AlembicDamageType> stats) {
        StringBuilder builder = new StringBuilder();
        for (Object2FloatMap.Entry<AlembicDamageType> entry : stats.object2FloatEntrySet()) {
            builder.append(entry.getKey().getId().toString() + " -> " + entry.getFloatValue() + "\n");
        }
        // indent every line
        builder.insert(0, "    ");
        builder.replace(builder.indexOf("\n"), builder.length(), "\n    ");
        return builder.toString();
    }

    public static String formatItemStat(Multimap<EquipmentSlotType, ItemStat> stat) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<EquipmentSlotType, ItemStat> entry : stat.entries()) {
            builder.append(entry.getKey().getName() + " -> " + entry.getValue().toString() + "\n");
        }
        // indent every line
        builder.insert(0, "    ");
        builder.replace(builder.indexOf("\n"), builder.length(), "\n    ");
        return builder.toString();
    }
}