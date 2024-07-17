package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.config.FloatProvider;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;


public non-sealed interface EntityEffect extends Enchantment.Effect {

    @NotNull BinaryTagSerializer<EntityEffect> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::enchantmentEntityEffects, EntityEffect::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> registry = DynamicRegistry.create("minestom:enchantment_value_effect");
        registry.register("all_of", AllOf.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("apply_mob_effect", ApplyPotionEffect.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("damage_entity", DamageEntity.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("damage_item", DamageItem.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("explode", Explode.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("ignite", Ignite.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("play_sound", PlaySound.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("replace_block", ReplaceBlock.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("replace_disk", ReplaceDisc.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("run_function", RunFunction.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("set_block_properties", SetBlockProperties.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("spawn_particles", SpawnParticles.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("summon_entity", SummonEntity.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    @NotNull BinaryTagSerializer<? extends EntityEffect> nbtType();

    record AllOf(@NotNull List<EntityEffect> effect) implements EntityEffect {
        public static final BinaryTagSerializer<AllOf> NBT_TYPE = BinaryTagSerializer.object(
                "effects", EntityEffect.NBT_TYPE.list(), AllOf::effect,
                AllOf::new
        );

        public AllOf {
            effect = List.copyOf(effect);
        }

        @Override
        public @NotNull BinaryTagSerializer<AllOf> nbtType() {
            return NBT_TYPE;
        }
    }

    record ApplyPotionEffect(
            @NotNull ObjectSet<PotionEffect> toApply,
            @NotNull LevelBasedValue minDuration,
            @NotNull LevelBasedValue maxDuration,
            @NotNull LevelBasedValue minAmplifier,
            @NotNull LevelBasedValue maxAmplifier
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ApplyPotionEffect> NBT_TYPE = BinaryTagSerializer.object(
                "to_apply", ObjectSet.nbtType(Tag.BasicType.POTION_EFFECTS), ApplyPotionEffect::toApply,
                "min_duration", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::minDuration,
                "max_duration", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::maxDuration,
                "min_amplifier", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::minAmplifier,
                "max_amplifier", LevelBasedValue.NBT_TYPE, ApplyPotionEffect::maxAmplifier,
                ApplyPotionEffect::new
        );

        @Override
        public @NotNull BinaryTagSerializer<ApplyPotionEffect> nbtType() {
            return NBT_TYPE;
        }
    }

    record DamageEntity(
            @NotNull DynamicRegistry.Key<DamageType> damageType,
            @NotNull LevelBasedValue minDamage,
            @NotNull LevelBasedValue maxDamage
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<DamageEntity> NBT_TYPE = BinaryTagSerializer.object(
                "damage_type", DamageType.NBT_TYPE, DamageEntity::damageType,
                "min_damage", LevelBasedValue.NBT_TYPE, DamageEntity::minDamage,
                "max_damage", LevelBasedValue.NBT_TYPE, DamageEntity::maxDamage,
                DamageEntity::new
        );

        @Override
        public @NotNull BinaryTagSerializer<DamageEntity> nbtType() {
            return NBT_TYPE;
        }
    }

    record DamageItem(@NotNull LevelBasedValue amount) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<DamageItem> NBT_TYPE = BinaryTagSerializer.object(
                "amount", LevelBasedValue.NBT_TYPE, DamageItem::amount,
                DamageItem::new
        );

        @Override
        public @NotNull BinaryTagSerializer<DamageItem> nbtType() {
            return NBT_TYPE;
        }
    }

    record Explode(
            boolean attributeToUser,
            @Nullable DynamicRegistry.Key<DamageType> damageType,
            @Nullable ObjectSet<Block> immuneBlocks,
            @Nullable LevelBasedValue knockbackMultiplier,
            @Nullable Point offset,
            @NotNull LevelBasedValue radius,
            boolean createFire,
            @NotNull BlockInteraction blockInteraction,
            @NotNull Particle smallParticle,
            @NotNull Particle largeParticle,
            @NotNull SoundEvent sound
    ) implements EntityEffect, LocationEffect {
        private static final BinaryTagSerializer<ObjectSet<Block>> IMMUNE_BLOCKS_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.BLOCKS);
        public static final BinaryTagSerializer<Explode> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Explode value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

                builder.putBoolean("attribute_to_user", value.attributeToUser);
                if (value.damageType != null)
                    builder.put("damage_type", DamageType.NBT_TYPE.write(context, value.damageType));
                if (value.immuneBlocks != null)
                    builder.put("immune_blocks", IMMUNE_BLOCKS_NBT_TYPE.write(context, value.immuneBlocks));
                if (value.knockbackMultiplier != null)
                    builder.put("knockback_multiplier", LevelBasedValue.NBT_TYPE.write(context, value.knockbackMultiplier));
                if (value.offset != null)
                    builder.put("offset", BinaryTagSerializer.BLOCK_POSITION.write(context, value.offset));
                builder.put("radius", LevelBasedValue.NBT_TYPE.write(context, value.radius));
                builder.putBoolean("create_fire", value.createFire);
                builder.putString("block_interaction", value.blockInteraction.id());
                builder.put("small_particle", Particle.NBT_TYPE.write(context, value.smallParticle));
                builder.put("large_particle", Particle.NBT_TYPE.write(context, value.largeParticle));
                builder.put("sound", SoundEvent.NBT_TYPE.write(value.sound));

                return builder.build();
            }

            @Override
            public @NotNull Explode read(@NotNull Context context, @NotNull BinaryTag tag) {
                final CompoundBinaryTag compound = (CompoundBinaryTag) tag;

                boolean attributeToUser = compound.getBoolean("attribute_to_user");
                BinaryTag damageTypeTag = compound.get("damage_type");
                DynamicRegistry.Key<DamageType> damageType = damageTypeTag == null ? null : DamageType.NBT_TYPE.read(context, damageTypeTag);
                BinaryTag immuneBlocksTag = compound.get("immune_blocks");
                ObjectSet<Block> immuneBlocks = immuneBlocksTag == null ? null : IMMUNE_BLOCKS_NBT_TYPE.read(context, immuneBlocksTag);
                BinaryTag knockbackMultiplierTag = compound.get("knockback_multiplier");
                LevelBasedValue knockbackMultiplier = knockbackMultiplierTag == null ? null : LevelBasedValue.NBT_TYPE.read(context, knockbackMultiplierTag);
                BinaryTag offsetTag = compound.get("offset");
                Point offset = offsetTag == null ? null : BinaryTagSerializer.BLOCK_POSITION.read(context, offsetTag);
                LevelBasedValue radius = LevelBasedValue.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("radius")));
                boolean createFire = compound.getBoolean("create_fire");
                BlockInteraction blockInteraction = BlockInteraction.fromId(compound.getString("block_interaction"));
                Particle smallParticle = Particle.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("small_particle")));
                Particle largeParticle = Particle.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("large_particle")));
                SoundEvent sound = SoundEvent.NBT_TYPE.read(context, Objects.requireNonNull(compound.get("sound")));
                Check.notNull(sound, "Cannot find sound event");

                return new Explode(
                        attributeToUser, damageType, immuneBlocks,
                        knockbackMultiplier, offset, radius, createFire,
                        blockInteraction, smallParticle, largeParticle, sound
                );
            }
        };

        @Override
        public @NotNull BinaryTagSerializer<Explode> nbtType() {
            return NBT_TYPE;
        }

        public enum BlockInteraction {
            NONE("none"),
            BLOCK("block"),
            MOB("mob"),
            TNT("tnt"),
            TRIGGER("trigger");

            private final String id;

            BlockInteraction(String id) {
                this.id = id;
            }

            public String id() {
                return id;
            }

            public static @NotNull BlockInteraction fromId(String id) {
                for (BlockInteraction blockInteraction : values()) {
                    if (blockInteraction.id.equals(id)) {
                        return blockInteraction;
                    }
                }

                return NONE;
            }
        }
    }

    record Ignite(@NotNull LevelBasedValue duration) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<Ignite> NBT_TYPE = BinaryTagSerializer.object(
                "duration", LevelBasedValue.NBT_TYPE, Ignite::duration,
                Ignite::new
        );

        @Override
        public @NotNull BinaryTagSerializer<Ignite> nbtType() {
            return NBT_TYPE;
        }
    }

    record PlaySound(
            @NotNull SoundEvent sound,
            @NotNull FloatProvider volume,
            @NotNull FloatProvider pitch
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<PlaySound> NBT_TYPE = BinaryTagSerializer.object(
                "sound", SoundEvent.NBT_TYPE, PlaySound::sound,
                "volume", FloatProvider.NBT_TYPE, PlaySound::volume,
                "pitch", FloatProvider.NBT_TYPE, PlaySound::pitch,
                PlaySound::new
        );

        @Override
        public @NotNull BinaryTagSerializer<PlaySound> nbtType() {
            return NBT_TYPE;
        }
    }

    record ReplaceBlock(
            CompoundBinaryTag content
//            Object blockState, // "A block state provider giving the block state to set"
//            @NotNull Point offset,
//            @Nullable Object predicate // "A World-generation style Block Predicate to used to determine if the block should be replaced"
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ReplaceBlock> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(ReplaceBlock::new, ReplaceBlock::content);

        @Override
        public @NotNull BinaryTagSerializer<ReplaceBlock> nbtType() {
            return NBT_TYPE;
        }
    }

    record ReplaceDisc(
            CompoundBinaryTag content
            // todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<ReplaceDisc> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(ReplaceDisc::new, ReplaceDisc::content);

        @Override
        public @NotNull BinaryTagSerializer<ReplaceDisc> nbtType() {
            return NBT_TYPE;
        }
    }

    record RunFunction(
            @NotNull String function
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<RunFunction> NBT_TYPE = BinaryTagSerializer.object(
                "function", BinaryTagSerializer.STRING, RunFunction::function,
                RunFunction::new
        );

        @Override
        public @NotNull BinaryTagSerializer<RunFunction> nbtType() {
            return NBT_TYPE;
        }
    }

    record SetBlockProperties(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SetBlockProperties> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(SetBlockProperties::new, SetBlockProperties::content);

        @Override
        public @NotNull BinaryTagSerializer<SetBlockProperties> nbtType() {
            return NBT_TYPE;
        }
    }

    record SpawnParticles(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SpawnParticles> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(SpawnParticles::new, SpawnParticles::content);

        @Override
        public @NotNull BinaryTagSerializer<SpawnParticles> nbtType() {
            return NBT_TYPE;
        }
    }

    record SummonEntity(
            CompoundBinaryTag content
            //todo
    ) implements EntityEffect, LocationEffect {
        public static final BinaryTagSerializer<SummonEntity> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(SummonEntity::new, SummonEntity::content);

        @Override
        public @NotNull BinaryTagSerializer<SummonEntity> nbtType() {
            return NBT_TYPE;
        }
    }

}
