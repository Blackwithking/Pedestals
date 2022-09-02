package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeQuarry extends ItemUpgradeBase implements ISelectableArea
{
    public ItemUpgradeQuarry(Properties p_41383_) {
        super(new Properties());
    }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_quarry_baseEnergyCost.get(); }
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_quarry_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_quarry_baseXpCost.get(); }
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_quarry_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_quarry_dustColor.get(),PedestalConfig.COMMON.upgrade_quarry_baseDustAmount.get()); }
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_quarry_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_quarry_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_quarry_selectedMultiplier.get(); }


    private void buildValidBlockList(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> listed = readBlockPosListFromNBT(coin);
        List<BlockPos> valid = new ArrayList<>();
        for (BlockPos pos:listed) {
            if(selectedPointWithinRange(pedestal, pos))
            {
                valid.add(pos);
            }
        }

        saveBlockPosListCustomToNBT(coin,"_validlist",valid);
    }

    private void buildValidBlockListArea(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> valid = new ArrayList<>();
        AABB area = new AABB(readBlockPosFromNBT(pedestal.getCoinOnPedestal(),1),readBlockPosFromNBT(pedestal.getCoinOnPedestal(),2));

        int maxX = (int)area.maxX;
        int maxY = (int)area.maxY;
        int maxZ = (int)area.maxZ;

        //System.out.println("aabbMaxStuff: "+ maxX+","+maxY+","+maxZ);

        int minX = (int)area.minX;
        int minY = (int)area.minY;
        int minZ = (int)area.minZ;

        //System.out.println("aabbMinStuff: "+ minX+","+minY+","+minZ);

        BlockPos pedestalPos = pedestal.getPos();
        for(int i=maxX;i>=minX;i--)
        {
            for(int j=maxZ;j>=minZ;j--)
            {
                BlockPos newPoint = new BlockPos(i,pedestalPos.getY(),j);
                //System.out.println("points: "+ newPoint);
                if(selectedPointWithinRange(pedestal, newPoint))
                {
                    valid.add(newPoint);
                }
            }
        }

        //System.out.println("validList: "+ valid);
        saveBlockPosListCustomToNBT(coin,"_validlist",valid);
    }

    private List<BlockPos> getValidList(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return readBlockPosListCustomFromNBT(coin,"_validlist");
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        removeBlockListCustomNBTTags(coinInPedestal, "_validlist");
        MowLibCompoundTagUtils.removeIntegerFromNBT(MODID, coinInPedestal.getTag(),"_numposition");
        MowLibCompoundTagUtils.removeIntegerFromNBT(MODID, coinInPedestal.getTag(),"_numdelay");
    }

    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {

        ItemStack coin = pedestal.getCoinOnPedestal();
        boolean override = hasTwoPointsSelected(coin);
        List<BlockPos> listed = getValidList(pedestal);

        if(override)
        {
            if(listed.size()>0)
            {
                upgradeAction(world,pedestal);
            }
            else if(selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
            {
                buildValidBlockListArea(pedestal);
            }
            else if(!pedestal.getRenderRange())
            {
                pedestal.setRenderRange(true);
            }
        }
    }

    private int getCurrentDelay(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numdelay");
    }

    private void setCurrentDelay(BasePedestalBlockEntity pedestal, int num)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numdelay");
    }

    private void iterateCurrentDelay(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getCurrentPosition(pedestal);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+1), "_numdelay");
    }

    private int getCurrentPosition(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numposition");
    }

    private void setCurrentPosition(BasePedestalBlockEntity pedestal, int num)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numposition");
    }

    private void iterateCurrentPosition(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getCurrentPosition(pedestal);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+1), "_numposition");
    }

    private boolean isToolHighEnoughLevelForBlock(ItemStack toolIn, BlockState getBlock)
    {
        if(toolIn.getItem() instanceof TieredItem tieredItem)
        {
            Tier toolTier = tieredItem.getTier();
            return TierSortingRegistry.isCorrectTierForDrops(toolTier,getBlock);
        }

        return false;
    }

    private List<ItemStack> getBlockDrops(BasePedestalBlockEntity pedestal, BlockState blockTarget)
    {
        ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(new ItemStack(Items.IRON_PICKAXE)):(pedestal.getToolStack());

        if(blockTarget.requiresCorrectToolForDrops())
        {
            if(isToolHighEnoughLevelForBlock(getToolFromPedestal, blockTarget))
            {
                Level level = pedestal.getLevel();
                if(blockTarget.getBlock() != Blocks.AIR)
                {
                    LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                            .withRandom(level.random)
                            .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                            .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                    return blockTarget.getDrops(builder);
                }
            }
        }
        else
        {
            Level level = pedestal.getLevel();
            if(blockTarget.getBlock() != Blocks.AIR)
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return blockTarget.getDrops(builder);
            }
        }

        return new ArrayList<>();
    }

    //This is for Chopper, so we'll test to make sure the blocks arnt part of this
    private boolean canMine(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        boolean isLeaves = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("minecraft","leaves"))).stream().toList().contains(canMineBlock.getBlock());
        boolean isFlowers = canMineBlock.getBlock().equals(Blocks.AZALEA_LEAVES);
        boolean isLogs = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("minecraft","logs"))).stream().toList().contains(canMineBlock.getBlock());
        boolean isWart = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("minecraft","wart_blocks"))).stream().toList().contains(canMineBlock.getBlock());
        boolean isMushroom = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("forge","mushroom"))).stream().toList().contains(canMineBlock.getBlock());
        boolean isVine = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("forge","vines"))).stream().toList().contains(canMineBlock.getBlock());
        if(isLeaves || isFlowers || isLogs || isWart || isMushroom || isVine)return true;

        return false;
    }

    private boolean passesFilter(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof BaseFilter filter)
            {
                if(filter.getFilterDirection().neutral())
                {
                    ItemStack blockToCheck = ItemStack.EMPTY;
                    if(canMineBlock.getBlock() instanceof Block)
                    {
                        blockToCheck = canMineBlock.getBlock().getCloneItemStack(pedestal.getLevel(),canMinePos,canMineBlock);
                    }

                    return filter.canAcceptItems(filterInPedestal,blockToCheck);
                }
            }
        }

        return true;
    }

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint)
    {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }

    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal)
    {
        if(!level.isClientSide())
        {
            List<BlockPos> listed = getValidList(pedestal);
            int currentPosition = getCurrentPosition(pedestal);
            BlockPos currentPoint = listed.get(currentPosition);
            //ToDo: make this a modifier for later
            boolean runsOnce = true;
            boolean stop = false;

            if(!stop)
            {
                for(int y=level.getMinBuildHeight();y<=level.getMaxBuildHeight();y++)
                {
                    BlockPos adjustedPoint = new BlockPos(currentPoint.getX(),y,currentPoint.getZ());
                    BlockState blockAtPoint = level.getBlockState(adjustedPoint);
                    blockAtPoint.requiresCorrectToolForDrops();

                    if(!blockAtPoint.getBlock().equals(Blocks.AIR) && blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                    {
                        //checking to make sure we cant mine chopper blocks
                        if(!canMine(pedestal, blockAtPoint, adjustedPoint))
                        {
                            if(passesFilter(pedestal, blockAtPoint, adjustedPoint) && (!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList().contains(blockAtPoint.getBlock())))
                            {
                                //ToDo: config option

                                boolean damage = false;

                                if(!adjustedPoint.equals(pedestal.getPos()))
                                {
                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), true))
                                    {
                                        if(PedestalConfig.COMMON.quarryDamageTools.get())
                                        {
                                            if(pedestal.hasTool())
                                            {
                                                BlockPos pedestalPos = pedestal.getPos();
                                                if(pedestal.getDurabilityRemainingOnInsertedTool()>0)
                                                {
                                                    if(pedestal.damageInsertedTool(1,true))
                                                    {
                                                        damage = true;
                                                    }
                                                    else
                                                    {
                                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                        return;
                                                    }
                                                }
                                                else
                                                {
                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                    return;
                                                }
                                            }
                                        }

                                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false))
                                        {
                                            boolean canRemoveBlockEntities = PedestalConfig.COMMON.blockBreakerBreakEntities.get();
                                            List<ItemStack> drops = getBlockDrops(pedestal, blockAtPoint);
                                            if(level.getBlockEntity(adjustedPoint) !=null){
                                                if(canRemoveBlockEntities)
                                                {
                                                    blockAtPoint.onRemove(level,adjustedPoint,blockAtPoint,true);
                                                    dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                    level.removeBlockEntity(adjustedPoint);
                                                    //level.removeBlock(adjustedPoint, true);
                                                    level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                    //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                    if(damage)pedestal.damageInsertedTool(1,false);
                                                }
                                            }
                                            else
                                            {
                                                dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                //level.removeBlock(adjustedPoint, true);
                                                level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                if(damage)pedestal.damageInsertedTool(1,false);
                                            }

                                            if(drops.size()>0)
                                            {
                                                for (ItemStack stack: drops) {
                                                    MowLibItemUtils.spawnItemStack(level,adjustedPoint.getX(),adjustedPoint.getY(),adjustedPoint.getZ(),stack);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestal.getPos().getX(),pedestal.getPos().getY()+1.0f,pedestal.getPos().getZ(),55,55,55));
            }

            if((currentPosition+1)>=listed.size())
            {
                if(runsOnce)
                {
                    //ToDo: Make this 1200 value a config
                    int delay = listed.size() * Math.abs((level.getMaxBuildHeight()-level.getMinBuildHeight()));
                    if(getCurrentDelay(pedestal)>=delay)
                    {
                        setCurrentPosition(pedestal,0);
                        stop = false;
                        setCurrentDelay(pedestal,0);
                    }
                    else
                    {
                        iterateCurrentDelay(pedestal);
                        stop = true;
                    }
                }
                else
                {
                    setCurrentPosition(pedestal,0);
                }
            }
            else
            {
                iterateCurrentPosition(pedestal);
            }
        }
    }
}