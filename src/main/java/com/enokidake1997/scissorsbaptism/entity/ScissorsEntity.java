package com.enokidake1997.scissorsbaptism.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ScissorsEntity extends Monster {

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public ScissorsEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    //通常時の行動 - Normal behavior
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class,6.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.addAttackGoals();
    }

    //敵対時の行動 - Hostile behavior
    protected void addAttackGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2.0F,true));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class,false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class,true));
    }

    //ステータス - Status
    public static AttributeSupplier.Builder createAttributes(){
        return Monster.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1000)
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    //メインハンドの武器 - Mainhand weapon
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData spawngloupdata = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        return spawngloupdata;
    }

    //足音の設定 - Footsteps Setting
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isInWater()) {
            this.waterSwimSound();
            this.playMuffledStepSound(state, pos);
        } else {
            BlockPos blockPos = this.getPrimaryStepSoundBlockPos(pos);
            if (!pos.equals(blockPos)) {
                BlockState blockstate = this.level().getBlockState(blockPos);
                if (blockstate.is(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
                    this.playCombinationStepSounds(blockstate, state, blockPos, pos);
                } else {
                    super.playStepSound(blockPos, blockstate);
                }
            } else {
                super.playStepSound(pos, state);
            }
        }
        super.playStepSound(pos, state);
    }

    //被ダメージ音の設定 - Sound when damaged
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return damageSource.type().effects().sound();
    }

    public void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }
}
