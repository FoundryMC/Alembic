package foundry.alembic.attribute;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UUIDManager {
    private UUIDManager() {}
    public static final UUIDFactory INSTANCE = UUIDManager::getOrCreate;
    private static final Map<ResourceLocation, UUID> UUIDS = Util.make(new HashMap<>(), map -> {
        map.put(new ResourceLocation("helmet"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
        map.put(new ResourceLocation("chestplate"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        map.put(new ResourceLocation("leggings"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        map.put(new ResourceLocation("boots"), UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        map.put(new ResourceLocation("generic.attack_damage"), Item.BASE_ATTACK_DAMAGE_UUID);
        map.put(new ResourceLocation("generic.attack_speed"), Item.BASE_ATTACK_SPEED_UUID);
        map.put(new ResourceLocation("forge", "entity_reach"), AttributeHelper.BASE_ENTITY_REACH);
    });

    public static UUID getOrCreate(ResourceLocation id) {
        return UUIDS.computeIfAbsent(id, resourceLocation -> UUID.nameUUIDFromBytes(id.toString().getBytes()));
    }
}
