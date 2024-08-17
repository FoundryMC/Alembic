package foundry.alembic.codecs;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import foundry.alembic.Alembic;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Function;

public class CodecUtil {
    public static final Codec<Holder<DamageType>> DAMAGE_TYPE_HOLDER_CODEC = RegistryFileCodec.create(Registries.DAMAGE_TYPE, DamageType.CODEC, false);

    public static final Codec<Integer> STRINGIFIED_LITERAL_COLOR_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    int color = Integer.decode(s);
                    return DataResult.success(color);
                } catch (NumberFormatException e) {
                    return DataResult.error(e::getMessage);
                }
            },
            "#%06X"::formatted
    );

    public static final Codec<Integer> COLOR_CODEC = Codec.either(
            STRINGIFIED_LITERAL_COLOR_CODEC,
            Codec.INT
    ).xmap(
            either -> resolveEither(either, Function.identity(), Function.identity()),
            Either::left
    );

    public static final Codec<ResourceLocation> ALEMBIC_RL_CODEC = Codec.STRING.comapFlatMap(
            s -> ResourceLocation.read(s.contains(":") ? s : Alembic.MODID + ":" + s),
            ResourceLocation::toString
    );

    @Nonnull
    private static <T extends Enum<T>> Function<String, T> enumFromString(Class<T> clazz) {
        try {
            return s -> T.valueOf(clazz, s);
        } catch (IllegalArgumentException e) {
            return s -> null;
        }
    }

    public static <A, B> Codec<A> safeDispatch(Codec<B> typeCodec, String typeKey, Function<A, B> getType, Function<B, Codec<? extends A>> getCodec) {
        return typeCodec.dispatch(typeKey, a -> {
            if (getType.apply(a) == null) {
                throw new IllegalStateException("Type for " + a.toString() + " is null");
            }
            return getType.apply(a);
        }, getCodec);
    }

    public static final Codec<UUID> STRING_UUID = ExtraCodecs.stringResolverCodec(UUID::toString, UUID::fromString);

    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = ExtraCodecs.stringResolverCodec(Enum::name, enumFromString(EquipmentSlot.class));

    public static final Codec<AttributeModifier.Operation> OPERATION_CODEC = ExtraCodecs.stringResolverCodec(Enum::name, enumFromString(AttributeModifier.Operation.class));

    public static final Codec<RangedAttribute> RANGED_ATTRIBUTE_REGISTRY_CODEC = ForgeRegistries.ATTRIBUTES.getCodec().comapFlatMap(
            attribute -> {
                if (!(attribute instanceof RangedAttribute rangedAttribute)) {
                    return DataResult.error(() -> "The attribute " + ForgeRegistries.ATTRIBUTES.getKey(attribute) + " is not a ranged attribute");
                } else {
                    return DataResult.success(rangedAttribute);
                }
            },
            Function.identity()
    );

    public static final Codec<ItemStack> ITEM_OR_STACK_CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), ItemStack.CODEC).xmap(
            either -> either.map(Item::getDefaultInstance, Function.identity()),
            stack -> stack.getCount() == 1 && !stack.hasTag() ? Either.left(stack.getItem()) : Either.right(stack)
    );

    public static final Codec<Ingredient> INGREDIENT_CODEC = Codec.of(
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
                    return DataResult.success(JsonOps.INSTANCE.convertTo(ops, input.toJson()));
                }
            },
            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
                    try {
                        Ingredient ingredient = CraftingHelper.getIngredient(ops.convertTo(JsonOps.INSTANCE, input), false);
                        return DataResult.success(Pair.of(ingredient, input));
                    } catch (JsonSyntaxException e) {
                        return DataResult.error(() -> "Failed to decode ingredient" + e.getMessage());
                    }
                }
            }
    );

    public static final Codec<Ingredient> INGREDIENT_FROM_EITHER = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), INGREDIENT_CODEC).xmap(
            either -> either.map(Ingredient::of, Function.identity()),
            Either::right
    );

    public static final Codec<JsonElement> JSON_CODEC = Codec.of(
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(JsonElement input, DynamicOps<T> ops, T prefix) {
                    return DataResult.success(JsonOps.INSTANCE.convertTo(ops, input));
                }
            },
            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<JsonElement, T>> decode(DynamicOps<T> ops, T input) {
                    return DataResult.success(Pair.of(ops.convertTo(JsonOps.INSTANCE, input), input));
                }
            }
    );

    public static <T> SetCodec<T> setOf(Codec<T> elementCodec) {
        return new SetCodec<>(elementCodec);
    }

    public static <T, L, R> T resolveEither(Either<L, R> either, Function<L, T> leftFunc, Function<R, T> rightFunc) {
        return either.left().isPresent() ? leftFunc.apply(either.left().get()) : rightFunc.apply(either.right().get());
    }

    public static <L, R> Either<L, R> wrapEither(L left, R right) {
        if (left != null && right != null) {
            throw new IllegalArgumentException("Cannot create either because both elements are present. Only one must exist");
        }
        return left == null ? Either.right(right) : Either.left(left);
    }
}
