package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMultiContainer;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectableArea;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Recipes.BottlerRecipe;
import com.mowmaster.pedestals.Recipes.UpgradeModificationGlobalRecipe;
import com.mowmaster.pedestals.Recipes.UpgradeModificationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeModifications extends ItemUpgradeBase implements ISelectablePoints
{
    public ItemUpgradeModifications(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean selectedPointWithinRange(BasePedestalBlockEntity pedestal, BlockPos posPoint)
    {
        if(isSelectionInRange(pedestal, posPoint))
        {
            Level level = pedestal.getLevel();
            if(level.getBlockState(posPoint).getBlock() instanceof BasePedestalBlock)return true;
        }

        return false;
    }

    private void buildValidBlockList(BasePedestalBlockEntity pedestal)
    {
        Level level = pedestal.getLevel();
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> listed = readBlockPosListFromNBT(coin);
        List<BlockPos> valid = new ArrayList<>();
        for (BlockPos pos:listed) {
            if(selectedPointWithinRange(pedestal, pos))
            {
                if(level.getBlockState(pos).getBlock() instanceof BasePedestalBlock)
                {
                    valid.add(pos);
                }
            }
        }

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
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {


        List<BlockPos> listed = getValidList(pedestal);

        List<BlockPos> getList = readBlockPosListFromNBT(coin);
        if(listed.size()>0)
        {
            modifierAction(level,pedestal);
        }
        else if(getList.size()>0)
        {
            if(!hasBlockListCustomNBTTags(coin,"_validlist"))
            {
                buildValidBlockList(pedestal);
            }
            else if(!pedestal.getRenderRange())
            {
                pedestal.setRenderRange(true);
            }
        }
    }

    @Nullable
    protected UpgradeModificationGlobalRecipe getModificationGlobalRecipe(BasePedestalBlockEntity pedestal, ItemStack upgradeIn, List<ItemStack> ingredientStacks) {
        Level level = pedestal.getLevel();
        int conSize = 1;
        for(int i=0;i<ingredientStacks.size();i++)
        {
            if(!ingredientStacks.get(i).isEmpty())
            {
                conSize++;
            }
        }
        Container cont = MowLibContainerUtils.getContainer(conSize);
        cont.setItem(-1,upgradeIn);
        //Make 9 extra slots the most possible slots
        for(int i=0;i<ingredientStacks.size();i++)
        {
            if(!ingredientStacks.get(i).isEmpty())
            {
                cont.setItem(-1,ingredientStacks.get(i));
            }
        }

        if (level == null) return null;
        RecipeManager recipeManager = level.getRecipeManager();
        /*System.out.println("Global Recipes");
        System.out.println(recipeManager.getAllRecipesFor(UpgradeModificationGlobalRecipe.Type.INSTANCE));
        for(int j=0;j<cont.getContainerSize();j++)
        {
            System.out.println(cont.getItem(j));
        }*/
        Optional<UpgradeModificationGlobalRecipe> optional = recipeManager.getRecipeFor(UpgradeModificationGlobalRecipe.Type.INSTANCE, cont, level);
        if (optional.isPresent())
        {
            return optional.orElse(null);
        }

        return null;
    }



    protected boolean upgradeModifiedSuccessfully(UpgradeModificationGlobalRecipe recipe, ItemStack upgradeInput) {
        //System.out.println("upgradeModifiedSuccessfully Method: "+recipe.getResultingModifiedItem(upgradeInput));
        return (recipe == null)?(false):(recipe.getResultingModifiedItem(upgradeInput));
    }



    public void modifierAction(Level level, BasePedestalBlockEntity pedestal)
    {
        if(!level.isClientSide())
        {
            List<BlockPos> listed = getValidList(pedestal);
            List<ItemStack> ingredientList = new ArrayList<>();
            for(int i=0; i<listed.size(); i++)
            {
                BlockEntity invToCheck = level.getBlockEntity(listed.get(i));
                if(invToCheck instanceof BasePedestalBlockEntity pedestalToCheck) {
                    ingredientList.add(pedestalToCheck.getItemInPedestal());
                }
            }

            BlockPos posInventory = getPosOfBlockBelow(level,pedestal.getPos(),1);
            ItemStack upgradeItemStackInput = ItemStack.EMPTY;
            LazyOptional<IItemHandler> cap = MowLibItemUtils.findItemHandlerAtPos(level,posInventory,getPedestalFacing(level, pedestal.getPos()),true);
            if(!isInventoryEmpty(cap)) {
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElse(null);
                    BlockEntity invToPullFrom = level.getBlockEntity(posInventory);
                    if (invToPullFrom instanceof BasePedestalBlockEntity) {
                        upgradeItemStackInput = ItemStack.EMPTY;

                    }
                    else {
                        if(handler != null)
                        {
                            int i = getNextSlotWithItemsCapFiltered(pedestal,cap);
                            if(i>=0)
                            {
                                upgradeItemStackInput = handler.getStackInSlot(i);
                                ItemStack copyIncoming = upgradeItemStackInput.copy();
                                copyIncoming.setCount(1);

                                if(pedestal.addItem(upgradeItemStackInput,true))
                                {
                                    UpgradeModificationGlobalRecipe recipeGlobal = getModificationGlobalRecipe(pedestal, copyIncoming, ingredientList);
                                    //System.out.println("recipe: "+recipeGlobal);
                                    if(recipeGlobal != null)
                                    {
                                        //System.out.println("upgradeModifiedSuccessfully Call: "+upgradeModifiedSuccessfully(recipeGlobal,copyIncoming));
                                        if(upgradeModifiedSuccessfully(recipeGlobal,copyIncoming))
                                        {
                                            if(!handler.extractItem(i,1 ,true ).isEmpty())
                                            {
                                                if(pedestal.addItem(copyIncoming,true))
                                                {
                                                    for(int j=0; j<listed.size(); j++)
                                                    {
                                                        BlockEntity invToCheck = level.getBlockEntity(listed.get(j));
                                                        if(invToCheck instanceof BasePedestalBlockEntity pedestalToCheck) {
                                                            if(!pedestalToCheck.removeItem(1,true).isEmpty())
                                                            {
                                                                pedestalToCheck.removeItem(1,false);
                                                                BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestalToCheck.getPos(),-1);
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestalToCheck.getLevel(),pedestalToCheck.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),50,200,0));
                                                            }
                                                        }
                                                    }
                                                    handler.extractItem(i,1 ,false );
                                                    pedestal.addItem(copyIncoming,false);
                                                    BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),0,255,0));
                                                }
                                                else
                                                {
                                                    BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),50,50,50));
                                                }
                                            }
                                        }
                                        else
                                        {
                                            BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),50,50,50));
                                        }
                                    }
                                    else
                                    {
                                        BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),50,50,50));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
            Get Pedestals at each location
            (up to 9?)

            Upgrade Input is in the fist avail slot of the inv below the current pedestal(make sure it only takes one out at a time)

            The selected pedestals are the next 9 inputs
            (if there are more then 9, ignore them)

            Output item will be put in the current pedestal (to be transferred out)

            recipes in jei, have global ones and item specific ones.
            the base upgrade will be the 'input' for JEI to show all the global options
            for the item specific ones, let those inputs be how to find them in jei.

            Will need a text readout for what modification is applied and the rate of application and the max it can apply
             */

        }
    }
}
