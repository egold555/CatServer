package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.event.CraftEventFactory;

public class EntityAIEatGrass extends EntityAIBase
{
    private static final Predicate<IBlockState> IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
    private final EntityLiving grassEaterEntity;
    private final World entityWorld;
    int eatingGrassTimer;

    public EntityAIEatGrass(EntityLiving grassEaterEntityIn)
    {
        this.grassEaterEntity = grassEaterEntityIn;
        this.entityWorld = grassEaterEntityIn.world;
        this.setMutexBits(7);
    }

    public boolean shouldExecute()
    {
        if (this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 50 : 1000) != 0)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);

            if (IS_TALL_GRASS.apply(this.entityWorld.getBlockState(blockpos)))
            {
                return true;
            }
            else
            {
                return this.entityWorld.getBlockState(blockpos.down()).getBlock() == Blocks.GRASS;
            }
        }
    }

    public void startExecuting()
    {
        this.eatingGrassTimer = 40;
        this.entityWorld.setEntityState(this.grassEaterEntity, (byte)10);
        this.grassEaterEntity.getNavigator().clearPath();
    }

    public void resetTask()
    {
        this.eatingGrassTimer = 0;
    }

    public boolean shouldContinueExecuting()
    {
        return this.eatingGrassTimer > 0;
    }

    public int getEatingGrassTimer()
    {
        return this.eatingGrassTimer;
    }

    public void updateTask()
    {
        this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);

        if (this.eatingGrassTimer == 4)
        {
            BlockPos blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);

            if (IS_TALL_GRASS.apply(this.entityWorld.getBlockState(blockpos)))
            {
                // TODO: Needs testing
                if (!CraftEventFactory.callEntityChangeBlockEvent(this.grassEaterEntity, this.grassEaterEntity.world.getWorld().getBlockAt(blockpos.getX(), blockpos.getY(), blockpos.getZ()), Material.AIR, !net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.grassEaterEntity)).isCancelled())
                {
                    this.entityWorld.destroyBlock(blockpos, false);
                }

                this.grassEaterEntity.eatGrassBonus();
            }
            else
            {
                BlockPos blockpos1 = blockpos.down();

                if (this.entityWorld.getBlockState(blockpos1).getBlock() == Blocks.GRASS)
                {
                    // TODO: Needs testing
                    if (!CraftEventFactory.callEntityChangeBlockEvent(this.grassEaterEntity, this.grassEaterEntity.world.getWorld().getBlockAt(blockpos.getX(), blockpos.getY(), blockpos.getZ()), Material.AIR, !net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.grassEaterEntity)).isCancelled())
                    {
                        this.entityWorld.playEvent(2001, blockpos1, Block.getIdFromBlock(Blocks.GRASS));
                        this.entityWorld.setBlockState(blockpos1, Blocks.DIRT.getDefaultState(), 2);
                    }

                    this.grassEaterEntity.eatGrassBonus();
                }
            }
        }
    }
}