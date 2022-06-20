package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.recipes.SmeltingRecipeAdvanced;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFurnace extends ItemUpgradeBaseMachine {
    public ItemUpgradeFurnace(Item.Properties builder) {
        super(builder.group(PEDESTALS_TAB));
    }

    @Override
    public boolean canAcceptAdvanced() {
        return true;
    }

    @Nullable
    protected SmeltingRecipeAdvanced getRecipeAdvanced(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return world == null ? null : world.getRecipeManager().getRecipe(SmeltingRecipeAdvanced.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResultsAdvanced(SmeltingRecipeAdvanced recipe) {
        return (recipe == null) ? (Arrays.asList(ItemStack.EMPTY)) : (Collections.singleton(recipe.getResult()));
    }

    protected float getProcessResultsXPAdvanced() {
        //My custom recipe doesnt have any xp to give, so default to 1xp point per
        return 1.0f;
    }

    @Nullable
    protected AbstractCookingRecipe getRecipe(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);

        if (world == null) return null;

        RecipeManager recipeManager = world.getRecipeManager();
        Optional<BlastingRecipe> optional = recipeManager.getRecipe(IRecipeType.BLASTING, inv, world);
        if (optional.isPresent()) return optional.get();

        Optional<SmokingRecipe> optional2 = recipeManager.getRecipe(IRecipeType.SMOKING, inv, world);
        if (optional2.isPresent()) return optional2.get();

        Optional<FurnaceRecipe> optional1 = recipeManager.getRecipe(IRecipeType.SMELTING, inv, world);
        return optional1.orElse(null);
    }

    protected Collection<ItemStack> getProcessResults(AbstractCookingRecipe recipe, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return (recipe == null) ? (Arrays.asList(ItemStack.EMPTY)) : (Collections.singleton(recipe.getCraftingResult(inv)));
    }

    protected float getProcessResultsXP(AbstractCookingRecipe recipe) {
        return (recipe == null) ? (0.0f) : (recipe.getExperience());
    }

    protected void spawnXP(World world, BlockPos posOfPedestal, int xp) {
        if (xp >= 1) {
            ExperienceOrbEntity expEntity = new ExperienceOrbEntity(world, getPosOfBlockBelow(world, posOfPedestal, -1).getX() + 0.5, getPosOfBlockBelow(world, posOfPedestal, -1).getY(), getPosOfBlockBelow(world, posOfPedestal, -1).getZ() + 0.5, xp);
            expEntity.setMotion(0D, 0D, 0D);
            world.addEntity(expEntity);
        }
    }

    public void updateAction(World world, PedestalTileEntity pedestal) {
        if (!world.isRemote) {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            int speed = getSmeltingSpeed(coinInPedestal);

            if (world.getGameTime() % speed == 0) {
                BlockPos pedestalPos = pedestal.getPos();
                BlockPos posInventory = getPosOfBlockBelow(world, pedestalPos, 1);
                boolean isAdvanced = hasAdvancedInventoryTargeting(coinInPedestal);
                LazyOptional<IItemHandler> cap = isAdvanced ?
                        findItemHandlerAtPosAdvanced(world, posInventory, getPedestalFacing(world, pedestalPos), true)
                        : findItemHandlerAtPos(world, posInventory, getPedestalFacing(world, pedestalPos), true);

                if (isInventoryEmpty(cap) || !cap.isPresent()) {
                    return;
                }

                if (!pedestal.isPedestalBlockPowered(world, pedestalPos)) {
                    //Just receives Energy, then exports it to machines, not other pedestals
                    upgradeAction(pedestal, world, pedestalPos, coinInPedestal, cap);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, BlockPos posOfPedestal, ItemStack coinInPedestal, LazyOptional<IItemHandler> cap) {
        boolean isAdvanced = hasAdvancedInventoryTargeting(coinInPedestal);
        BlockPos posInventory = getPosOfBlockBelow(world, posOfPedestal, 1);
        int itemsPerSmelt = getItemTransferRate(coinInPedestal);

        ItemStack itemFromInv;
        IItemHandler handler = cap.orElse(null);
        TileEntity invToPullFrom = world.getTileEntity(posInventory);
        if ((!isAdvanced || !(invToPullFrom instanceof PedestalTileEntity)) && invToPullFrom instanceof PedestalTileEntity) {
            itemFromInv = ItemStack.EMPTY;
        } else {
            if (handler != null) {
                int i = getNextSlotWithItemsCapFiltered(pedestal, cap, getStackInPedestal(world, posOfPedestal));
                if (i >= 0) {
                    int maxInSlot = handler.getSlotLimit(i);
                    itemFromInv = handler.getStackInSlot(i);
                    //Need to null check invalid recipes
                    float xp = (isAdvanced && getProcessResultsAdvanced(getRecipeAdvanced(world, itemFromInv)).size() > 0) ? (getProcessResultsXPAdvanced()) : (getProcessResultsXP(getRecipe(world, itemFromInv)));
                    Collection<ItemStack> jsonResults = getProcessResults(getRecipe(world, itemFromInv), itemFromInv);
                    Collection<ItemStack> jsonResultsAdvanced = getProcessResultsAdvanced(getRecipeAdvanced(world, itemFromInv));

                    //Just check to make sure our recipe output isnt air
                    ItemStack resultSmelted = (jsonResults.iterator().next().isEmpty()) ? (ItemStack.EMPTY) : (jsonResults.iterator().next());
                    ItemStack resultSmeltedAdvanced = (jsonResultsAdvanced.iterator().next().isEmpty()) ? (ItemStack.EMPTY) : (jsonResultsAdvanced.iterator().next());
                    if (isAdvanced && !resultSmeltedAdvanced.equals(ItemStack.EMPTY))
                        resultSmelted = resultSmeltedAdvanced;
                    ItemStack itemFromPedestal = getStackInPedestal(world, posOfPedestal);
                    if (!resultSmelted.equals(ItemStack.EMPTY)) {
                        //Null check our slot again, which is probably redundant
                        if (handler.getStackInSlot(i) != null && !handler.getStackInSlot(i).isEmpty() && handler.getStackInSlot(i).getItem() != Items.AIR) {
                            int roomLeftInPedestal = 64 - itemFromPedestal.getCount();
                            if (itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY))
                                roomLeftInPedestal = 64;

                            //Upgrade Determins amout of items to smelt, but space count is determined by how much the item smelts into
                            int itemInputsPerSmelt = itemsPerSmelt;
                            int itemsOutputWhenStackSmelted = (itemInputsPerSmelt * resultSmelted.getCount());
                            //Checks to see if pedestal can accept as many items as will be returned on smelt, if not reduce items being smelted
                            if (roomLeftInPedestal < itemsOutputWhenStackSmelted) {
                                itemInputsPerSmelt = Math.floorDiv(roomLeftInPedestal, resultSmelted.getCount());
                            }
                            //Checks to see how many items are left in the slot IF ITS UNDER the allowedTransferRate then sent the max rate to that.
                            if (itemFromInv.getCount() < itemInputsPerSmelt)
                                itemInputsPerSmelt = itemFromInv.getCount();

                            itemsOutputWhenStackSmelted = (itemInputsPerSmelt * resultSmelted.getCount());
                            ItemStack copyIncoming = resultSmelted.copy();
                            copyIncoming.setCount(itemsOutputWhenStackSmelted);
                            int fuelToConsume = burnTimeCostPerItemSmelted * itemInputsPerSmelt;
                            TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                            if (pedestalInv instanceof PedestalTileEntity) {
                                PedestalTileEntity ped = ((PedestalTileEntity) pedestalInv);
                                //Checks to make sure we have fuel to smelt everything
                                if (removeFuel(ped, fuelToConsume, true)) {
                                    if (!handler.extractItem(i, itemInputsPerSmelt, true).isEmpty()) {
                                        handler.extractItem(i, itemInputsPerSmelt, false);
                                        removeFuel(ped, fuelToConsume, false);
                                        spawnXP(world, posOfPedestal, (int) (xp * itemInputsPerSmelt));
                                        if (!ped.hasMuffler())
                                            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                        ped.addItemOverride(copyIncoming);
                                    }
                                }
                                //If we done have enough fuel to smelt everything then reduce size of smelt
                                else {
                                    //gets fuel left
                                    int fuelLeft = getFuelStored(coinInPedestal);
                                    if (fuelLeft > 0) {
                                        //this = a number over 1 unless fuelleft < burnTimeCostPeritemSmelted
                                        itemInputsPerSmelt = Math.floorDiv(fuelLeft, burnTimeCostPerItemSmelted);
                                        if (itemInputsPerSmelt >= 1) {
                                            fuelToConsume = burnTimeCostPerItemSmelted * itemInputsPerSmelt;
                                            itemsOutputWhenStackSmelted = (itemInputsPerSmelt * resultSmelted.getCount());
                                            copyIncoming.setCount(itemsOutputWhenStackSmelted);

                                            if (!handler.extractItem(i, itemInputsPerSmelt, true).isEmpty()) {
                                                handler.extractItem(i, itemInputsPerSmelt, false);
                                                removeFuel(ped, fuelToConsume, false);
                                                spawnXP(world, posOfPedestal, (int) (xp * itemInputsPerSmelt));
                                                if (!ped.hasMuffler())
                                                    world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                                ped.addItemOverride(copyIncoming);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                        if (pedestalInv instanceof PedestalTileEntity) {
                            PedestalTileEntity ped = ((PedestalTileEntity) pedestalInv);
                            if (ped.getItemInPedestal().equals(ItemStack.EMPTY)) {
                                ItemStack copyItemFromInv = itemFromInv.copy();
                                if (!handler.extractItem(i, itemFromInv.getCount(), true).isEmpty()) {
                                    handler.extractItem(i, itemFromInv.getCount(), false);
                                    ped.addItemOverride(copyItemFromInv);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static final Item SMELTER = new ItemUpgradeFurnace(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/smelter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(SMELTER);
    }


}
