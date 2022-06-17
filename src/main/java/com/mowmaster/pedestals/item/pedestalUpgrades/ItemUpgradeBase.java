package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.api.filter.IFilterBase;
import com.mowmaster.pedestals.api.upgrade.IUpgradeBase;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeBase extends Item implements IUpgradeBase {




    public int maxStored = 2000000000;
    public int maxLVLStored = 20000;
    public ItemUpgradeBase(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean isPiglinCurrency(ItemStack stack) {
        return stack.getItem() instanceof ItemUpgradeBase;
    }

    /***************************************
     ****************************************
     ** Start of Custom IItemHandler Stuff **
     ****************************************
     ***************************************/

    //https://skmedix.github.io/ForgeJavaDocs/javadoc/forge/1.9.4-12.17.0.2051/net/minecraftforge/items/IItemHandler.html


    @Override
    public boolean customIsValid(PedestalTileEntity pedestal, int slot, @Nonnull ItemStack stack)
    {
        return (slot==0)?(true):(false);
    }

    //ItemStack extracted from the slot, must be null, if nothing can be extracted
    @Override
    public ItemStack customExtractItem(PedestalTileEntity pedestal, int amountOut, boolean simulate)
    {
        //Default return that forces pedestal to do a normal thing
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    //The remaining ItemStack that was not inserted (if the entire stack is accepted, then return null).
    //May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
    @Override
    public ItemStack customInsertItem(PedestalTileEntity pedestal, ItemStack stackIn, boolean simulate)
    {
        //Default return that forces pedestal to do a normal thing
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    //ItemStack in given slot. May be null.
    @Override
    public ItemStack customStackInSlot(PedestalTileEntity pedestal,ItemStack stackFromHandler)
    {
        //Default return that forces pedestal to do a normal thing
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    @Override
    public int customSlotLimit(PedestalTileEntity pedestal)
    {
        return -1;
    }

    //For Filters to return if they can or cannot allow items to pass
    //Will probably need overwritten
    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return true;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)tile;
            return Math.min(pedestal.getSlotSizeLimit(), itemStackIncoming.getMaxStackSize());
        }
        //int stackabe = itemStackIncoming.maxStackSize();
        return 0;
    }

    /**
     * Can this hopper insert the specified item from the specified slot on the specified side?
     */
    public static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side)
    {
        if (!inventoryIn.isItemValidForSlot(index, stack))
        {
            return false;
        }
        else
        {
            return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
        }
    }

    /**
     * Can this hopper extract the specified item from the specified slot on the specified side?
     */
    public static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side)
    {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
    }

    public int[] getSlotsForSide(World world, BlockPos posOfPedestal, IInventory inventory)
    {
        int[] slots = new int[]{};

        if(inventory instanceof ISidedInventory)
        {
            slots= ((ISidedInventory) inventory).getSlotsForFace(getPedestalFacing(world, posOfPedestal));
        }

        return slots;
    }

    public boolean isInventoryEmpty(LazyOptional<IItemHandler> cap)
    {
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            int range = handler.getSlots();

            return !IntStream.range(0,range)//Int Range
                    .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                    .anyMatch(itemStack -> !itemStack.isEmpty());
        }
        return true;
    }

    /***************************************
     ****************************************
     **  End of Custom IItemHandler Stuff  **
     ****************************************
     ***************************************/





    /***************************************
     ****************************************
     **      Start of Inventory Stuff      **
     ****************************************
     ***************************************/
    public ItemStack getStackInPedestal(World world, BlockPos posOfPedestal)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            stackInPedestal = ((PedestalTileEntity) pedestalInventory).getItemInPedestal();
        }

        return stackInPedestal;
    }

    public void removeFromPedestal(World world, BlockPos posOfPedestal, int count)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            ((PedestalTileEntity) pedestalInventory).removeItem(count);
        }
    }

    public int canAddToPedestal(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        int returner = 0;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            returner =  ((PedestalTileEntity) pedestalInventory).canAcceptItems(world,posOfPedestal,itemStackToAdd);
        }

        return returner;
    }

    public void addToPedestal(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            ((PedestalTileEntity) pedestalInventory).addItem(itemStackToAdd);
        }
    }

    public void addToPedestalOverride(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            ((PedestalTileEntity) pedestalInventory).addItemOverride(itemStackToAdd);
        }
    }

    public int intSpaceLeftInStack (ItemStack stack)
    {
        int value = 0;
        if(stack.equals(ItemStack.EMPTY))
        {
            value = 64;
        }
        else
        {
            int maxSize = stack.getMaxStackSize();
            int currentSize = stack.getCount();
            value = maxSize-currentSize;
        }

        return value;
    }

    public boolean doItemsMatch(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    public boolean doItemsMatchWithEmpty(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        if(stackPedestal.isEmpty() && itemStackIn.isEmpty())return true;

        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    @Override
    public boolean canSendItem(PedestalTileEntity tile)
    {
        return true;
    }


    //nerfed technically, but also give the ability to set storage to a nice number
    public int getStorageBuffer(ItemStack coin) {
        int capacityOver = getCapacityModifierOverEnchanted(coin);
        //1728 = 27*64 (size of double chest)
        int storageBuffer = (int)(Math.pow(4,(capacityOver>=33)?(33):(capacityOver)+1)*1728);

        return  (storageBuffer>=Integer.MAX_VALUE)?(Integer.MAX_VALUE):(storageBuffer);
    }

    //nerfed technically, but also give the ability to set storage to a nice number
    public int getFuelBuffer(ItemStack coin) {
        /*int capacityOver = getCapacityModifierOverEnchanted(coin);
        //1728 = 27*64 (size of double chest)
        int storageBuffer = (int)(Math.pow(4,(capacityOver>=33)?(33):(capacityOver)+1)*1728);
        int value = (storageBuffer>=Integer.MAX_VALUE)?(Integer.MAX_VALUE):(storageBuffer);
        int fuelToBuffer = Math.multiplyExact(Math.floorDiv(value,8),1600);


        return  (fuelToBuffer<=maxStored)?(fuelToBuffer):(maxStored);*/

        //changed back because ill now change how the redstone comparator bit works (below)
        int getMaxFuelValue = 2000000000;
        return getMaxFuelValue;
    }

    public int getMaxFuelDeviderBasedOnFuelStored(int currentFuelStored)
    {
        int returner = 10;

        while(currentFuelStored > returner)
        {
            if(currentFuelStored >= 2000000000)
            {
                returner = Integer.MAX_VALUE;
                break;
            }
            returner *= 10;
        }

        return returner;
    }

    /*

    Used for upgrades that have 2billion max fuel storage
    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            int fuelStored = getFuelStored(coin);
            if(fuelStored>0)
            {
                float f = (float)fuelStored/(float)getMaxFuelDeviderBasedOnFuelStored(fuelStored);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }
     */
    /***************************************
     ****************************************
     **       End of Inventory Stuff       **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **         Start of Cap Stuff         **
     ****************************************
     ***************************************/
//Info Used to create this goes to https://github.com/BluSunrize/ImmersiveEngineering/blob/f40a49da570c991e51dd96bba1d529e20da6caa6/src/main/java/blusunrize/immersiveengineering/api/ApiUtils.java#L338
    public static LazyOptional<IItemHandler> findItemHandlerPedestal(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pos = pedestal.getPos();
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
            if(cap.isPresent())
                return cap;
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPos(World world, BlockPos pos, Direction side, boolean allowCart)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            if(AbstractRailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof IForgeEntityMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof BoatEntity);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPosAdvanced(World world, BlockPos pos, Direction side, boolean allowEntity)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowEntity)
        {
            //Added for quark boats with inventories (i hope)
            //List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof BoatEntity);
            List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof Entity);
            if(!list.isEmpty())
            {
                LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                if(cap.isPresent())
                    return cap;
            }
        }
        return LazyOptional.empty();
    }

    //Mainly Used in the Import, Furnace, and  Milker Upgrades
    /*
        This Method gets the next slot with items in the given tile
     */

    public int getNextSlotWithItemsCap(LazyOptional<IItemHandler> cap, ItemStack stackInPedestal)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(!itemHandler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                        {
                            //If pedestal is empty accept any items
                            if(stackInPedestal.isEmpty())
                            {
                                slot.set(i);
                                break;
                            }
                            //if stack in pedestal matches items in slot
                            else if(doItemsMatch(stackInPedestal,stackInSlot))
                            {
                                slot.set(i);
                                break;
                            }
                        }
                    }
                }});


        }

        return slot.get();
    }

    public int getNextSlotWithItemsCapFiltered(PedestalTileEntity pedestal, LazyOptional<IItemHandler> cap, ItemStack stackInPedestal)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(!itemHandler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                        {
                            //If pedestal is empty accept any items
                            if(passesItemFilter(pedestal,stackInSlot))
                            {
                                if(stackInPedestal.isEmpty())
                                {
                                    slot.set(i);
                                    break;
                                }
                                //if stack in pedestal matches items in slot
                                else if(doItemsMatch(stackInPedestal,stackInSlot))
                                {
                                    slot.set(i);
                                    break;
                                }
                            }
                        }
                    }
                }});


        }

        return slot.get();
    }

    public boolean passesItemFilter(PedestalTileEntity pedestal, ItemStack stackIn)
    {
        boolean returner = true;
        if(pedestal.hasFilter())
        {
            Item filterInPedestal = pedestal.getFilterInPedestal().getItem();
            if(filterInPedestal instanceof IFilterBase)
            {
                returner = ((IFilterBase) filterInPedestal).canAcceptItem(pedestal,stackIn);
            }

        }

        return returner;
    }

    public int hasEnoughInInv(LazyOptional<IItemHandler> cap, ItemStack stackToFind, int stopAfter)
    {
        int counter = 0;
        if(cap.isPresent()) {
            IItemHandler handler = cap.orElse(null);
            int range = handler.getSlots();
            for(int i=0;i<range;i++)
            {
                ItemStack stackInSlot = handler.getStackInSlot(i);
                //find a slot with items
                if(!stackInSlot.isEmpty())
                {
                    //check if it could pull the item out or not
                    if(doItemsMatch(stackInSlot,stackToFind))
                    {
                        counter+=stackInSlot.getCount();
                        if(counter>=stopAfter)break;
                    }
                }
            }
        }
        return counter;
    }

    public int getSlotWithMatchingStack(LazyOptional<IItemHandler> cap, ItemStack stackToFind)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(stackInSlot.isItemEqual(stackToFind))
                        {
                            slot.set(i);
                            break;
                        }
                    }
                }});


        }

        return slot.get();
    }

    public int getSlotWithMatchingStackExact(LazyOptional<IItemHandler> cap, ItemStack stackToFind)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToFind))//stackInSlot.isItemEqual(stackToFind)
                        {
                            slot.set(i);
                            break;
                        }
                    }
                }});


        }

        return slot.get();
    }

    public int getSlotWithMatchingStackExactFiltered(PedestalTileEntity pedestal, LazyOptional<IItemHandler> cap, ItemStack stackToFind)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToFind))//stackInSlot.isItemEqual(stackToFind)
                        {
                            if(passesItemFilter(pedestal,stackInSlot))
                            {
                                slot.set(i);
                                break;
                            }
                        }
                    }
                }});
        }

        return slot.get();
    }

    public int getPlayerSlotWithMatchingStackExact(PlayerInventory inventory, ItemStack stackToFind)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        for(int i=0;i<inventory.getSizeInventory();i++)
        {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            //find a slot with items
            if(!stackInSlot.isEmpty())
            {
                //check if it could pull the item out or not
                if(ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToFind))//stackInSlot.isItemEqual(stackToFind)
                {
                    slot.set(i);
                    break;
                }
            }
        }

        return slot.get();
    }

    public int getPlayerSlotWithMatchingStackExactNotFull(PlayerInventory inventory, ItemStack stackToFind)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        for(int i=0;i<inventory.getSizeInventory();i++)
        {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            //find a slot with items
            if(!stackInSlot.isEmpty())
            {
                //check if it could pull the item out or not
                if(ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToFind) && stackInSlot.getCount() < stackInSlot.getMaxStackSize())//stackInSlot.isItemEqual(stackToFind)
                {
                    slot.set(i);
                    break;
                }
            }
        }

        return slot.get();
    }

    public int getEnderChestSlotWithMatchingStackExact(EnderChestInventory inventory, ItemStack stackToFind)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        for(int i=0;i<inventory.getSizeInventory();i++)
        {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            //find a slot with items
            if(!stackInSlot.isEmpty())
            {
                //check if it could pull the item out or not
                if(ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToFind))//stackInSlot.isItemEqual(stackToFind)
                {
                    slot.set(i);
                    break;
                }
            }
        }

        return slot.get();
    }

    public int getNextSlotWithItems(TileEntity invBeingChecked, Direction sideSlot, ItemStack stackInPedestal)
    {
        int slot = -1;
        if(invBeingChecked.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,sideSlot ).isPresent()) {
            IItemHandler handler = (IItemHandler) invBeingChecked.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, sideSlot).orElse(null);
            int range = handler.getSlots();
            for(int i=0;i<range;i++)
            {
                ItemStack stackInSlot = handler.getStackInSlot(i);
                //find a slot with items
                if(!stackInSlot.isEmpty())
                {
                    //check if it could pull the item out or not
                    if(!handler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                    {
                        //If pedestal is empty accept any items
                        if(stackInPedestal.isEmpty())
                        {
                            slot=i;
                            break;
                        }
                        //if stack in pedestal matches items in slot
                        else if(doItemsMatch(stackInPedestal,stackInSlot))
                        {
                            slot=i;
                            break;
                        }
                    }
                }
            }
        }

        return slot;
    }

    public int getNextIndexWithItems(List<ItemStack> stackList)
    {
        int range = stackList.size();
        for(int i=0;i<range;i++)
        {
            ItemStack stackInSlot = stackList.get(i);
            //find a slot with items
            if(!stackInSlot.isEmpty())
            {
                return i;
            }
        }

        return -1;
    }
    /***************************************
     ****************************************
     **          End of Cap Stuff          **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **      Start of Enchanting Stuff     **
     ****************************************
     ***************************************/

    public boolean hasEnchant(ItemStack stack)
    {
        return stack.isEnchanted();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(stack.getItem() instanceof IUpgradeBase && enchantment.getRegistryName().getNamespace().equals(Reference.MODID))
        {
            return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return (stack.getCount()==1)?(super.isBookEnchantable(stack, book)):(false);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canAcceptOpSpeed()
    {
        return true;
    }

    @Override
    public boolean canAcceptCapacity()
    {
        return false;
    }

    @Override
    public boolean canAcceptMagnet()
    {
        return false;
    }

    @Override
    public boolean canAcceptRange()
    {
        return false;
    }

    @Override
    public boolean canAcceptAdvanced()
    {
        return true;
    }

    @Override
    public boolean canAcceptArea()
    {
        return false;
    }

    public boolean hasMagnetEnchant(ItemStack stack)
    {
        return (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.MAGNET,stack)>=1)?(true):(false);
    }

    /***************************************
     ****************************************
     **       End of Enchanting Stuff      **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **         Start of Speed Stuff       **
     ****************************************
     ***************************************/
    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack));
        }
        return rate;
    }

    public int intOperationalSpeedOver(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack);
        }
        return rate;
    }

    public int intOperationalSpeedModifierOverride(ItemStack stack)
    {

        int rate = 0;
        if(hasEnchant(stack))
        {
            int speedOver = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack);
            return (hasAdvancedInventoryTargeting(stack))?(speedOver):((speedOver>5)?(5):(speedOver));
        }
        return rate;
    }

    public double getOperationSpeedOverride(ItemStack stack)
    {
        int advancedAllowedSpeed = intOperationalSpeedModifierOverride(stack);
        double intOperationalSpeed = 20;
        switch (advancedAllowedSpeed)
        {
            case 0:
                intOperationalSpeed = 20;//normal speed
                break;
            case 1:
                intOperationalSpeed=10;//2x faster
                break;
            case 2:
                intOperationalSpeed = 5;//4x faster
                break;
            case 3:
                intOperationalSpeed = 3;//6x faster
                break;
            case 4:
                intOperationalSpeed = 2;//10x faster
                break;
            case 5:
                intOperationalSpeed=1;//20x faster
                break;
            default: intOperationalSpeed=((advancedAllowedSpeed*0.01)>0.9)?(0.1):(1-(advancedAllowedSpeed*0.01));
        }

        return  intOperationalSpeed;
    }

    public int getOperationSpeed(ItemStack stack)
    {
        int intOperationalSpeed = 20;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                intOperationalSpeed = 20;//normal speed
                break;
            case 1:
                intOperationalSpeed=10;//2x faster
                break;
            case 2:
                intOperationalSpeed = 5;//4x faster
                break;
            case 3:
                intOperationalSpeed = 3;//6x faster
                break;
            case 4:
                intOperationalSpeed = 2;//10x faster
                break;
            case 5:
                intOperationalSpeed=1;//20x faster
                break;
            default: intOperationalSpeed=20;
        }

        return  intOperationalSpeed;
    }

    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_2");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_3");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_4");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_5");
        TranslationTextComponent overclockedx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_x");
        String overAmount = ""+(intOperationalSpeedModifier(stack)-5)+"";
        String str = normal.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = normal.getString();//normal speed
                break;
            case 1:
                str = twox.getString();//2x faster
                break;
            case 2:
                str = fourx.getString();//4x faster
                break;
            case 3:
                str = sixx.getString();//6x faster
                break;
            case 4:
                str = tenx.getString();//10x faster
                break;
            case 5:
                str = twentyx.getString();//20x faster
                break;
            default: str = overclockedx.getString() + overAmount;
        }

        return  str;
    }
    /***************************************
     ****************************************
     **          End of Speed Stuff        **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **       Start of Capacity Stuff      **
     ****************************************
     ***************************************/
    public int getCapacityModifier(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            capacity = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack));
        }
        return capacity;
    }

    public int getCapacityModifierOver(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            capacity = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack);
        }
        return capacity;
    }

    public int getCapacityModifierOverEnchanted(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            int capacityOver = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack);
            int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(capacityOver):((capacityOver>5)?(5):(capacityOver));
            return advancedAllowed;
        }
        return capacity;
    }

    public int getItemTransferRate(ItemStack stack)
    {
        int transferRate = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                transferRate = 1;
                break;
            case 1:
                transferRate=4;
                break;
            case 2:
                transferRate = 8;
                break;
            case 3:
                transferRate = 16;
                break;
            case 4:
                transferRate = 32;
                break;
            case 5:
                transferRate=64;
                break;
            default: transferRate=1;
        }

        return  transferRate;
    }
    /***************************************
     ****************************************
     **        End of Capacity Stuff       **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **        Start of Range Stuff        **
     ****************************************
     ***************************************/

    public int getRangeModifier(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            range = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack);
        }
        return range;
    }

    public int getRangeModifierLimited(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack);
            range = Math.min(lvl, 5);
        }
        return range;
    }

    //Based on old 3x3x3 range
    public int getRangeTiny(ItemStack stack)
    {
        return  ((getRangeModifier(stack)*2)+3);
    }
    //Based on old 16 block max
//    private Integer lastRange = null;
//    private ItemStack lastCoin = ItemStack.EMPTY;
    public int getRangeSmall(ItemStack stack)
    {
        int rangeOver = getRangeModifier(stack);
        int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(rangeOver):(Math.min(rangeOver, 5));

        int height;
        switch (advancedAllowed)
        {
            case 0:
                height = 1;
                break;
            case 1:
                height = 2;
                break;
            case 2:
                height = 4;
                break;
            case 3:
                height = 8;
                break;
            case 4:
                height = 12;
                break;
            case 5:
                height = 16;
                break;
            default: height=((advancedAllowed*4)-4);
        }

        return height;
    }
    //Based on old 32 block max
    public int getRangeMedium(ItemStack stack)
    {
        int rangeOver = getRangeModifier(stack);
        int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(rangeOver):((rangeOver>5)?(5):(rangeOver));

        int height = 4;
        switch (advancedAllowed)
        {
            case 0:
                height = 4;
                break;
            case 1:
                height=8;
                break;
            case 2:
                height = 12;
                break;
            case 3:
                height = 16;
                break;
            case 4:
                height = 24;
                break;
            case 5:
                height=32;
                break;
            default: height=((advancedAllowed*6)+2);
        }

        return  height;
    }
    //Based on old 64 block max starting at 1
    public int getRangeLarge(ItemStack stack)
    {
        int rangeOver = getRangeModifier(stack);
        int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(rangeOver):((rangeOver>5)?(5):(rangeOver));

        int height = 8;
        switch (advancedAllowed)
        {
            case 0:
                height = 2;
                break;
            case 1:
                height=4;
                break;
            case 2:
                height = 8;
                break;
            case 3:
                height = 16;
                break;
            case 4:
                height = 32;
                break;
            case 5:
                height=64;
                break;
            default: height=(advancedAllowed*12);
        }

        return  height;
    }

    //Based on old 64 block max
    public int getRangeLargest(ItemStack stack)
    {
        int rangeOver = getRangeModifier(stack);
        int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(rangeOver):((rangeOver>5)?(5):(rangeOver));

        int height = 8;
        switch (advancedAllowed)
        {
            case 0:
                height = 8;
                break;
            case 1:
                height=16;
                break;
            case 2:
                height = 24;
                break;
            case 3:
                height = 32;
                break;
            case 4:
                height = 48;
                break;
            case 5:
                height=64;
                break;
            default: height=(advancedAllowed*12);
        }

        return  height;
    }
    //Based on old Tree Chopper max
    public int getRangeTree(ItemStack stack)
    {
        int rangeOver = getRangeModifier(stack);
        int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(rangeOver):((rangeOver>5)?(5):(rangeOver));

        return  ((advancedAllowed*6)+6);
    }
    /***************************************
     ****************************************
     **         End of Range Stuff         **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **         Start of Area Stuff        **
     ****************************************
     ***************************************/
    public int getAreaModifier(ItemStack stack)
    {
        int area = 0;
        if(hasEnchant(stack))
        {
            int capacityOver = getAreaModifierUnRestricted(stack);
            int advancedAllowed = (hasAdvancedInventoryTargeting(stack))?(capacityOver):((capacityOver>5)?(5):(capacityOver));
            return advancedAllowed;
        }
        return area;
    }

    public int getAreaModifierUnRestricted(ItemStack stack)
    {
        int area = 0;
        if(hasEnchant(stack))
        {
            area = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.AREA,stack);
        }
        return area;
    }
    /***************************************
     ****************************************
     **          End of Area Stuff         **
     ****************************************
     ***************************************/


    public int getNumNonPedestalEnchants(Map<Enchantment, Integer> map)
    {
        int counter = 0;
        if(map.size()>0)
        {
            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    counter++;
                }
            }
        }

        return counter;
    }



    /***************************************
     ****************************************
     **       Start of Advanced Stuff      **
     ****************************************
     ***************************************/
    public int getAdvancedModifier(ItemStack stack)
    {
        ResourceLocation disabled = new ResourceLocation("pedestals", "enchant_limits/advanced_blacklist");
        ITag<Item> BLACKLISTED = ItemTags.getCollection().get(disabled);

        int advanced = 0;
        if(hasEnchant(stack) && !BLACKLISTED.contains(stack.getItem()))
        {
            advanced = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.ADVANCED,stack) > 1)?(1):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.ADVANCED,stack));
        }
        return advanced;
    }

    public boolean hasAdvancedInventoryTargeting(ItemStack stack)
    {
        return getAdvancedModifier(stack)>=1;
    }

    public boolean hasAdvancedInventoryTargetingTwo(ItemStack stack)
    {
        return EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.ADVANCED,stack)>=2;
    }
    /***************************************
     ****************************************
     **        End of Advanced Stuff       **
     ****************************************
     ***************************************/


    /***************************************
     ****************************************
     **      Start of BlockPos Stuff       **
     ****************************************
     ***************************************/
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal)
    {

    }

    public void onPedestalBelowNeighborChanged(PedestalTileEntity pedestal, BlockState blockChanged, BlockPos blockChangedPos)
    {

    }

    public Block getBaseBlockBelow(World world, BlockPos pedestalPos)
    {
        Block block = world.getBlockState(getPosOfBlockBelow(world,pedestalPos,1)).getBlock();

        /*ITag.INamedTag<Block> BLOCK_EMERALD = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/emerald"));
        ITag.INamedTag<Block> BLOCK_DIAMOND = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/diamond"));
        ITag.INamedTag<Block> BLOCK_GOLD = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/gold"));
        ITag.INamedTag<Block> BLOCK_LAPIS = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/lapis"));
        ITag.INamedTag<Block> BLOCK_IRON = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/iron"));
        ITag.INamedTag<Block> BLOCK_COAL = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/coal"));*/
        ITag<Block> BLOCK_EMERALD = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/emerald"));
        ITag<Block> BLOCK_DIAMOND = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/diamond"));
        ITag<Block> BLOCK_GOLD = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/gold"));
        ITag<Block> BLOCK_LAPIS = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/lapis"));
        ITag<Block> BLOCK_IRON = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/iron"));
        ITag<Block> BLOCK_COAL = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/coal"));
        ITag<Block> BLOCK_QUARTZ = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/quartz"));
        //ITag<Block> BLOCK_SLIME = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/slime"));

        //Netherite
        if(block.equals(Blocks.NETHERITE_BLOCK)) return Blocks.NETHERITE_BLOCK;
        if(BLOCK_EMERALD.contains(block)) return Blocks.EMERALD_BLOCK;//Players
        if(BLOCK_DIAMOND.contains(block)) return Blocks.DIAMOND_BLOCK;//All Monsters
        if(BLOCK_GOLD.contains(block)) return Blocks.GOLD_BLOCK;//All Animals
        if(BLOCK_LAPIS.contains(block)) return Blocks.LAPIS_BLOCK;//All Flying
        if(BLOCK_IRON.contains(block)) return Blocks.IRON_BLOCK;//All Creatures
        if(BLOCK_COAL.contains(block)) return Blocks.COAL_BLOCK;//All Mobs
        if(BLOCK_QUARTZ.contains(block)) return Blocks.QUARTZ_BLOCK;//All Items
        if(block.equals(Blocks.SLIME_BLOCK)) return Blocks.SLIME_BLOCK;//All Exp

        return block;
    }

    public double[] getPedTopPos(PedestalTileEntity pedestal, double Yheight)
    {
        BlockPos pos = pedestal.getPos();
        BlockState state = pedestal.getBlockState();
        Direction enumfacing = Direction.UP;
        //double[] doubleBlockAdd = new double[]{0D,0D,0D};
        if(state.getBlock() instanceof PedestalBlock)
        {
            enumfacing = state.get(FACING);
        }

        switch (enumfacing)
        {
            case UP:
                return new double[]{0.5D,Yheight,0.5D};
            case DOWN:
                return new double[]{0.5D,(1.0D-Yheight),0.5D};
            case NORTH:
                return new double[]{0.5D,0.5D,(1.0D-Yheight)};
            case SOUTH:
                return new double[]{0.5D,0.5D,Yheight};
            case EAST:
                return new double[]{Yheight,0.5D,0.5D};
            case WEST:
                return new double[]{(1.0D-Yheight),0.5D,0.5D};
            default:
                return new double[]{0.5D,Yheight,0.5D};
        }
    }

    /*
    Copy of master method before testing

    public double[] getPedTopPos(PedestalTileEntity pedestal, double Yheight)
    {
        BlockPos pos = pedestal.getPos();
        BlockState state = pedestal.getBlockState();
        Direction enumfacing = Direction.UP;
        //double[] doubleBlockAdd = new double[]{0D,0D,0D};
        if(state.getBlock() instanceof PedestalBlock)
        {
            enumfacing = state.get(FACING);
        }

        switch (enumfacing)
        {
            case UP:
                return new double[]{0.5D,Yheight,0.5D};
            case DOWN:
                return new double[]{0.5D,-Yheight,0.5D};
            case NORTH:
                return new double[]{0.5D,0.5D,-Yheight};
            case SOUTH:
                return new double[]{0.5D,0.5D,Yheight};
            case EAST:
                return new double[]{Yheight,0.5D,0.5D};
            case WEST:
                return new double[]{-Yheight,0.5D,0.5D};
            default:
                return new double[]{+0.5D,Yheight,0.5D};
        }
    }
     */

    public BlockPos getPosOfBlockBelow(World world, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = world.getBlockState(posOfPedestal);

        Direction enumfacing = (state.hasProperty(FACING))?(state.get(FACING)):(Direction.UP);
        return posOfPedestal.offset(enumfacing.getOpposite(), numBelow);
    }

    public BlockPos getNegRangePos(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(-intWidth,0,-intWidth);
            case DOWN:
                return blockBelow.add(-intWidth,-intHeight,-intWidth);
            case NORTH:
                return blockBelow.add(-intWidth,-intWidth,-intHeight);
            case SOUTH:
                return blockBelow.add(-intWidth,-intWidth,0);
            case EAST:
                return blockBelow.add(0,-intWidth,-intWidth);
            case WEST:
                return blockBelow.add(-intHeight,-intWidth,-intWidth);
            default:
                return blockBelow;
        }
    }

    public BlockPos getPosRangePos(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(intWidth,intHeight,intWidth);
            case DOWN:
                return blockBelow.add(intWidth,0,intWidth);
            case NORTH:
                return blockBelow.add(intWidth,intWidth,0);
            case SOUTH:
                return blockBelow.add(intWidth,intWidth,intHeight);
            case EAST:
                return blockBelow.add(intHeight,intWidth,intWidth);
            case WEST:
                return blockBelow.add(0,intWidth,intWidth);
            default:
                return blockBelow;
        }
    }

    //Trying to work on a way to do better block checks
    public BlockPos getPosOfNextBlock(int currentPosition, BlockPos negCorner, BlockPos posCorner)
    {
        int xRange = Math.abs(posCorner.getX() - negCorner.getX());
        int yRange = Math.abs(posCorner.getY() - negCorner.getY());
        int zRange = Math.abs(posCorner.getZ() - negCorner.getZ());
        int layerVolume = xRange*zRange;
        int addY = (int)Math.floor(currentPosition/layerVolume);
        int layerCurrentPosition = currentPosition - addY*layerVolume;
        int addZ = (int)Math.floor(layerCurrentPosition/xRange);
        int addX = layerCurrentPosition - addZ*xRange;

        return negCorner.add(addX,addY,addZ);
    }

    public boolean resetCurrentPosInt(int currentPosition, BlockPos negCorner, BlockPos posCorner)
    {
        int xRange = Math.abs(posCorner.getX() - negCorner.getX());
        int yRange = Math.abs(posCorner.getY() - negCorner.getY());
        int zRange = Math.abs(posCorner.getZ() - negCorner.getZ());
        int layerVolume = xRange*zRange;

        int addY = (int)Math.floor(currentPosition/layerVolume);

        return addY >= yRange;
    }

    public boolean passesFilter(World world, BlockPos posPedestal, Block blockIn)
    {
        return false;
    }

    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockState blockToMineState = world.getBlockState(blockToMinePos);
        Block blockToMine = blockToMineState.getBlock();
        if (
                blockToMine.isAir(blockToMineState, world, blockToMinePos)
                        || blockToMine instanceof PedestalBlock
                        || blockToMine instanceof IFluidBlock || blockToMine instanceof FlowingFluidBlock
                        || blockToMineState.getBlockHardness(world, blockToMinePos) == -1.0F
                        || BlockTags.getCollection().get(new ResourceLocation("pedestals", "quarry/blacklist")).contains(blockToMine)
                        || (BlockTags.getCollection().get(new ResourceLocation("pedestals", "quarry/advanced")).contains(blockToMine)
                                && !hasAdvancedInventoryTargeting(coinInPedestal)
                )
        ) {
            return false;
        }

        ItemStack pickaxe = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE,1));
        ToolType tool = blockToMineState.getHarvestTool();
        FakePlayer fakePlayer = fakePedestalPlayer(pedestal).get();
        if (fakePlayer == null) {
            return false;
        }
        fakePlayer.setSilent(true);
        if(!fakePlayer.getPosition().equals(new BlockPos(pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ()))) {fakePlayer.setPosition(pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ());}
        if(!doItemsMatch(fakePlayer.getHeldItemMainhand(),pickaxe))fakePlayer.setItemStackToSlot(EquipmentSlotType.MAINHAND,pickaxe);
        //IF block is in advanced, check to make sure the coin has advanced (Y=true N=false), otherwise its fine;

        return passesFilter(world, pedestalPos, blockToMine)
                && ForgeHooks.canHarvestBlock(blockToMineState, fakePlayer, world, blockToMinePos);
    }

    public boolean canMineBlockTwo(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        return false;
    }

    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos, PlayerEntity player)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        BlockState blockToMineState = world.getBlockState(blockToMinePos);
        Block blockToMine = blockToMineState.getBlock();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if (
                blockToMine.isAir(blockToMineState, world, blockToMinePos)
                        || blockToMine instanceof PedestalBlock
                        || blockToMine instanceof IFluidBlock || blockToMine instanceof FlowingFluidBlock
                        || blockToMineState.getBlockHardness(world, blockToMinePos) == -1.0F
                        || BlockTags.getCollection().get(new ResourceLocation("pedestals", "quarry/blacklist")).contains(blockToMine)
                        || (BlockTags.getCollection().get(new ResourceLocation("pedestals", "quarry/advanced")).contains(blockToMine)
                        && !hasAdvancedInventoryTargeting(coinInPedestal)
                )
        ) {
            return false;
        }

        ItemStack pickaxe = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE,1));
        ToolType tool = blockToMineState.getHarvestTool();
        player.setPosition(pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ());
        if(!doItemsMatch(player.getHeldItemMainhand(),pickaxe))player.setItemStackToSlot(EquipmentSlotType.MAINHAND,pickaxe);

        return passesFilter(world, pedestalPos, blockToMine)
                && ForgeHooks.canHarvestBlock(blockToMineState, player, world, blockToMinePos);
    }

    public ItemStack getToolDefaultEnchanted(ItemStack coinInPedestal, ItemStack tool)
    {
        if(EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.SILK_TOUCH))
        {
            tool.addEnchantment(Enchantments.SILK_TOUCH,1);
        }
        else if (EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.FORTUNE))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,coinInPedestal);
            tool.addEnchantment(Enchantments.FORTUNE,lvl);
        }

        return tool;
    }

    public Direction getPedestalFacing(World world, BlockPos posOfPedestal)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        return state.get(FACING);
    }
    /***************************************
     ****************************************
     **       End of BlockPos Stuff        **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **       Start of Entity Stuff        **
     ****************************************
     ***************************************/
    //https://github.com/gigabit101/Shrink/blob/8fb45f28a1d77500f4f9431f68e78196d11828c8/src/main/java/net/gigabit101/shrink/items/ItemModBottle.java#L74
    //https://github.com/gigabit101/Shrink/blob/8fb45f28a1d77500f4f9431f68e78196d11828c8/src/main/java/net/gigabit101/shrink/items/ItemModBottle.java#L47
    /*
    EntityType.byKey("entity name here")

    if(entityIn.getType().getClassification().equals(EntityClassification.MONSTER))return entityIn;
    if(entityIn.getType().getClassification().equals(EntityClassification.CREATURE))return entityIn;
    if(entityIn.getType().getClassification().equals(EntityClassification.AMBIENT))return entityIn;
    if(entityIn.getType().getClassification().equals(EntityClassification.WATER_CREATURE))return entityIn;
    if(entityIn.getType().getClassification().equals(EntityClassification.WATER_AMBIENT))return entityIn;
    if(entityIn.getType().getClassification().equals(EntityClassification.MISC))return entityIn;

    Full list of entities sorted by classification:
    ***    EntityClassification.MONSTER    ***
    EntityType<RavagerEntity>           RAVAGER         "ravager",           size(1.95F, 2.2F).trackingRange(10));
    EntityType<ShulkerEntity>           SHULKER         "shulker",           immuneToFire().func_225435_d().size(1.0F, 1.0F).trackingRange(10));
    EntityType<SilverfishEntity>        SILVERFISH      "silverfish",        size(0.4F, 0.3F).trackingRange(8));
    EntityType<SkeletonEntity>          SKELETON        "skeleton",          size(0.6F, 1.99F).trackingRange(8));
    EntityType<SlimeEntity>             SLIME           "slime",             size(2.04F, 2.04F).trackingRange(10));
    EntityType<SpiderEntity>            SPIDER          "spider",            size(1.4F, 0.9F).trackingRange(8));
    EntityType<StrayEntity>             STRAY           "stray",             size(0.6F, 1.99F).trackingRange(8));
    EntityType<VexEntity>               VEX             "vex",               immuneToFire().size(0.4F, 0.8F).trackingRange(8));
    EntityType<VindicatorEntity>        VINDICATOR      "vindicator",        size(0.6F, 1.95F).trackingRange(8));
    EntityType<WitchEntity>             WITCH           "witch",             size(0.6F, 1.95F).trackingRange(8));
    EntityType<WitherEntity>            WITHER          "wither",            immuneToFire().func_233607_a_(Blocks.WITHER_ROSE).size(0.9F, 3.5F).trackingRange(10));
    EntityType<WitherSkeletonEntity>    WITHER_SKELETON "wither_skeleton",   immuneToFire().func_233607_a_(Blocks.WITHER_ROSE).size(0.7F, 2.4F).trackingRange(8));
    EntityType<ZoglinEntity>            ZOGLIN          "zoglin",            immuneToFire().size(1.3964844F, 1.4F).trackingRange(8));
    EntityType<ZombieEntity>            ZOMBIE          "zombie",            size(0.6F, 1.95F).trackingRange(8));
    EntityType<ZombieVillagerEntity>    ZOMBIE_VILLAGER "zombie_villager",   size(0.6F, 1.95F).trackingRange(8));
    EntityType<ZombifiedPiglinEntity>   ZOMBIFIED_PIGLIN"zombified_piglin",  immuneToFire().size(0.6F, 1.95F).trackingRange(8));
    EntityType<PillagerEntity>          PILLAGER        "pillager",          func_225435_d().size(0.6F, 1.95F).trackingRange(8));
    EntityType<PiglinBruteEntity>       field_242287_aj "piglin_brute",      size(0.6F, 1.95F).trackingRange(8));
    EntityType<PiglinEntity>            PIGLIN          "piglin",            size(0.6F, 1.95F).trackingRange(8));
    EntityType<PhantomEntity>           PHANTOM         "phantom",           size(0.9F, 0.5F).trackingRange(8));
    EntityType<BlazeEntity>             BLAZE           "blaze",             immuneToFire().size(0.6F, 1.8F).trackingRange(8));
    EntityType<CaveSpiderEntity>        CAVE_SPIDER     "cave_spider",       size(0.7F, 0.5F).trackingRange(8));
    EntityType<CreeperEntity>           CREEPER         "creeper",           size(0.6F, 1.7F).trackingRange(8));
    EntityType<DrownedEntity>           DROWNED         "drowned",           size(0.6F, 1.95F).trackingRange(8));
    EntityType<ElderGuardianEntity>     ELDER_GUARDIAN  "elder_guardian",    size(1.9975F, 1.9975F).trackingRange(10));
    EntityType<EnderDragonEntity>       ENDER_DRAGON    "ender_dragon",      immuneToFire().size(16.0F, 8.0F).trackingRange(10));
    EntityType<EndermanEntity>          ENDERMAN        "enderman",          size(0.6F, 2.9F).trackingRange(8));
    EntityType<EndermiteEntity>         ENDERMITE       "endermite",         size(0.4F, 0.3F).trackingRange(8));
    EntityType<EvokerEntity>            EVOKER          "evoker",            size(0.6F, 1.95F).trackingRange(8));
    EntityType<GhastEntity>             GHAST           "ghast",             immuneToFire().size(4.0F, 4.0F).trackingRange(10));
    EntityType<GiantEntity>             GIANT           "giant",             size(3.6F, 12.0F).trackingRange(10));
    EntityType<GuardianEntity>          GUARDIAN        "guardian",          size(0.85F, 0.85F).trackingRange(8));
    EntityType<HoglinEntity>            HOGLIN          "hoglin",            size(1.3964844F, 1.4F).trackingRange(8));
    EntityType<HuskEntity>              HUSK            "husk",              size(0.6F, 1.95F).trackingRange(8));
    EntityType<IllusionerEntity>        ILLUSIONER      "illusioner",        size(0.6F, 1.95F).trackingRange(8));
    EntityType<MagmaCubeEntity>         MAGMA_CUBE      "magma_cube",        immuneToFire().size(2.04F, 2.04F).trackingRange(8));

    ***    EntityClassification.CREATURE    ***
    EntityType<SheepEntity> SHEEP "sheep", EntityType.Builder.<SheepEntity>create(SheepEntity::new, size(0.9F, 1.3F).trackingRange(10));
    EntityType<SkeletonHorseEntity> SKELETON_HORSE "skeleton_horse", EntityType.Builder.<SkeletonHorseEntity>create(SkeletonHorseEntity::new, size(1.3964844F, 1.6F).trackingRange(10));
    EntityType<StriderEntity> STRIDER "strider", EntityType.Builder.<StriderEntity>create(StriderEntity::new, immuneToFire().size(0.9F, 1.7F).trackingRange(10));
    EntityType<TraderLlamaEntity> TRADER_LLAMA "trader_llama", EntityType.Builder.<TraderLlamaEntity>create(TraderLlamaEntity::new, size(0.9F, 1.87F).trackingRange(10));
    EntityType<TurtleEntity> TURTLE "turtle", EntityType.Builder.<TurtleEntity>create(TurtleEntity::new, size(1.2F, 0.4F).trackingRange(10));
    EntityType<WanderingTraderEntity> WANDERING_TRADER "wandering_trader", EntityType.Builder.<WanderingTraderEntity>create(WanderingTraderEntity::new, size(0.6F, 1.95F).trackingRange(10));
    EntityType<WolfEntity> WOLF "wolf", EntityType.Builder.<WolfEntity>create(WolfEntity::new, size(0.6F, 0.85F).trackingRange(10));
    EntityType<ZombieHorseEntity> ZOMBIE_HORSE "zombie_horse", EntityType.Builder.<ZombieHorseEntity>create(ZombieHorseEntity::new, size(1.3964844F, 1.6F).trackingRange(10));
    EntityType<RabbitEntity> RABBIT "rabbit", EntityType.Builder.<RabbitEntity>create(RabbitEntity::new, size(0.4F, 0.5F).trackingRange(8));
    EntityType<PolarBearEntity> POLAR_BEAR "polar_bear", EntityType.Builder.<PolarBearEntity>create(PolarBearEntity::new, size(1.4F, 1.4F).trackingRange(10));
    EntityType<PigEntity> PIG "pig", EntityType.Builder.<PigEntity>create(PigEntity::new, size(0.9F, 0.9F).trackingRange(10));
    EntityType<ParrotEntity> PARROT "parrot", EntityType.Builder.<ParrotEntity>create(ParrotEntity::new, size(0.5F, 0.9F).trackingRange(8));
    EntityType<PandaEntity> PANDA "panda", EntityType.Builder.<PandaEntity>create(PandaEntity::new, size(1.3F, 1.25F).trackingRange(10));
    EntityType<OcelotEntity> OCELOT "ocelot", EntityType.Builder.<OcelotEntity>create(OcelotEntity::new, size(0.6F, 0.7F).trackingRange(10));
    EntityType<MooshroomEntity> MOOSHROOM "mooshroom", EntityType.Builder.<MooshroomEntity>create(MooshroomEntity::new, size(0.9F, 1.4F).trackingRange(10));
    EntityType<BeeEntity> BEE "bee", EntityType.Builder.<BeeEntity>create(BeeEntity::new, size(0.7F, 0.6F).trackingRange(8));
    EntityType<CatEntity> CAT "cat", EntityType.Builder.<CatEntity>create(CatEntity::new, size(0.6F, 0.7F).trackingRange(8));
    EntityType<ChickenEntity> CHICKEN "chicken", EntityType.Builder.<ChickenEntity>create(ChickenEntity::new, size(0.4F, 0.7F).trackingRange(10));
    EntityType<CowEntity> COW "cow", EntityType.Builder.<CowEntity>create(CowEntity::new, size(0.9F, 1.4F).trackingRange(10));
    EntityType<DonkeyEntity> DONKEY "donkey", EntityType.Builder.<DonkeyEntity>create(DonkeyEntity::new, size(1.3964844F, 1.5F).trackingRange(10));
    EntityType<FoxEntity> FOX "fox", EntityType.Builder.<FoxEntity>create(FoxEntity::new, size(0.6F, 0.7F).trackingRange(8).func_233607_a_(Blocks.SWEET_BERRY_BUSH));
    EntityType<HorseEntity> HORSE "horse", EntityType.Builder.<HorseEntity>create(HorseEntity::new, size(1.3964844F, 1.6F).trackingRange(10));
    EntityType<LlamaEntity> LLAMA "llama", EntityType.Builder.<LlamaEntity>create(LlamaEntity::new, size(0.9F, 1.87F).trackingRange(10));
    EntityType<MuleEntity> MULE "mule", EntityType.Builder.<MuleEntity>create(MuleEntity::new, size(1.3964844F, 1.6F).trackingRange(8));

    ***    EntityClassification.AMBIENT    ***
    EntityType<BatEntity> BAT "bat", EntityType.Builder.<BatEntity>create(BatEntity::new, size(0.5F, 0.9F).trackingRange(5));

    ***    EntityClassification.WATER_CREATURE    ***
    EntityType<SquidEntity> SQUID "squid", EntityType.Builder.<SquidEntity>create(SquidEntity::new, size(0.8F, 0.8F).trackingRange(8));
    EntityType<DolphinEntity> DOLPHIN "dolphin", EntityType.Builder.<DolphinEntity>create(DolphinEntity::new, size(0.9F, 0.6F));

    ***    EntityClassification.WATER_AMBIENT    ***
    EntityType<SalmonEntity> SALMON "salmon", EntityType.Builder.<SalmonEntity>create(SalmonEntity::new, size(0.7F, 0.4F).trackingRange(4));
    EntityType<TropicalFishEntity> TROPICAL_FISH "tropical_fish", size(0.5F, 0.4F).trackingRange(4));
    EntityType<PufferfishEntity> PUFFERFISH "pufferfish", size(0.7F, 0.7F).trackingRange(4));
    EntityType<CodEntity> COD "cod", size(0.5F, 0.3F).trackingRange(4));

    ***    EntityClassification.MISC    ***
    EntityType<IronGolemEntity> IRON_GOLEM "iron_golem", EntityType.Builder.<IronGolemEntity>create(IronGolemEntity::new, size(1.4F, 2.7F).trackingRange(10));
    EntityType<SnowGolemEntity> SNOW_GOLEM "snow_golem", EntityType.Builder.<SnowGolemEntity>create(SnowGolemEntity::new, size(0.7F, 1.9F).trackingRange(8));
    EntityType<VillagerEntity> VILLAGER "villager", EntityType.Builder.<VillagerEntity>create(VillagerEntity::new, size(0.6F, 1.95F).trackingRange(10));
    EntityType<PlayerEntity> PLAYER "player", EntityType.Builder.<PlayerEntity>create(disableSerialization().disableSummoning().size(0.6F, 1.8F).trackingRange(32).func_233608_b_(2));
    --- Entities not mobs ---
    EntityType<SmallFireballEntity> SMALL_FIREBALL "small_fireball", EntityType.Builder.<SmallFireballEntity>create(SmallFireballEntity::new, size(0.3125F, 0.3125F).trackingRange(4).func_233608_b_(10));
    EntityType<SnowballEntity> SNOWBALL "snowball", EntityType.Builder.<SnowballEntity>create(SnowballEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<SpectralArrowEntity> SPECTRAL_ARROW "spectral_arrow", EntityType.Builder.<SpectralArrowEntity>create(SpectralArrowEntity::new, size(0.5F, 0.5F).trackingRange(4).func_233608_b_(20));
    EntityType<EggEntity> EGG "egg", EntityType.Builder.<EggEntity>create(EggEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<EnderPearlEntity> ENDER_PEARL "ender_pearl", EntityType.Builder.<EnderPearlEntity>create(EnderPearlEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<ExperienceBottleEntity> EXPERIENCE_BOTTLE "experience_bottle", EntityType.Builder.<ExperienceBottleEntity>create(ExperienceBottleEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<PotionEntity> POTION "potion", EntityType.Builder.<PotionEntity>create(PotionEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<TridentEntity> TRIDENT "trident", EntityType.Builder.<TridentEntity>create(TridentEntity::new, size(0.5F, 0.5F).trackingRange(4).func_233608_b_(20));
    EntityType<WitherSkullEntity> WITHER_SKULL "wither_skull", EntityType.Builder.<WitherSkullEntity>create(WitherSkullEntity::new, size(0.3125F, 0.3125F).trackingRange(4).func_233608_b_(10));
    EntityType<FishingBobberEntity> FISHING_BOBBER "fishing_bobber", EntityType.Builder.<FishingBobberEntity>create(disableSerialization().disableSummoning().size(0.25F, 0.25F).trackingRange(4).func_233608_b_(5));
    EntityType<TNTEntity> TNT "tnt", EntityType.Builder.<TNTEntity>create(TNTEntity::new, immuneToFire().size(0.98F, 0.98F).trackingRange(10).func_233608_b_(10));
    EntityType<PaintingEntity> PAINTING "painting", EntityType.Builder.<PaintingEntity>create(PaintingEntity::new, size(0.5F, 0.5F).trackingRange(10).func_233608_b_(Integer.MAX_VALUE));
    EntityType<AreaEffectCloudEntity> AREA_EFFECT_CLOUD "area_effect_cloud", EntityType.Builder.<AreaEffectCloudEntity>create(AreaEffectCloudEntity::new, immuneToFire().size(6.0F, 0.5F).trackingRange(10).func_233608_b_(Integer.MAX_VALUE));
    EntityType<ArmorStandEntity> ARMOR_STAND "armor_stand", EntityType.Builder.<ArmorStandEntity>create(ArmorStandEntity::new, size(0.5F, 1.975F).trackingRange(10));
    EntityType<ArrowEntity> ARROW "arrow", EntityType.Builder.<ArrowEntity>create(ArrowEntity::new, size(0.5F, 0.5F).trackingRange(4).func_233608_b_(20));
    EntityType<ShulkerBulletEntity> SHULKER_BULLET "shulker_bullet", EntityType.Builder.<ShulkerBulletEntity>create(ShulkerBulletEntity::new, size(0.3125F, 0.3125F).trackingRange(8));
    EntityType<BoatEntity> BOAT "boat", EntityType.Builder.<BoatEntity>create(BoatEntity::new, size(1.375F, 0.5625F).trackingRange(10));
    EntityType<DragonFireballEntity> DRAGON_FIREBALL "dragon_fireball", EntityType.Builder.<DragonFireballEntity>create(DragonFireballEntity::new, size(1.0F, 1.0F).trackingRange(4).func_233608_b_(10));
    EntityType<EnderCrystalEntity> END_CRYSTAL "end_crystal", EntityType.Builder.<EnderCrystalEntity>create(EnderCrystalEntity::new, size(2.0F, 2.0F).trackingRange(16).func_233608_b_(Integer.MAX_VALUE));
    EntityType<EvokerFangsEntity> EVOKER_FANGS "evoker_fangs", EntityType.Builder.<EvokerFangsEntity>create(EvokerFangsEntity::new, size(0.5F, 0.8F).trackingRange(6).func_233608_b_(2));
    EntityType<ExperienceOrbEntity> EXPERIENCE_ORB "experience_orb", EntityType.Builder.<ExperienceOrbEntity>create(ExperienceOrbEntity::new, size(0.5F, 0.5F).trackingRange(6).func_233608_b_(20));
    EntityType<EyeOfEnderEntity> EYE_OF_ENDER "eye_of_ender", EntityType.Builder.<EyeOfEnderEntity>create(EyeOfEnderEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(4));
    EntityType<FallingBlockEntity> FALLING_BLOCK "falling_block", EntityType.Builder.<FallingBlockEntity>create(FallingBlockEntity::new, size(0.98F, 0.98F).trackingRange(10).func_233608_b_(20));
    EntityType<FireworkRocketEntity> FIREWORK_ROCKET "firework_rocket", EntityType.Builder.<FireworkRocketEntity>create(FireworkRocketEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<ItemEntity> ITEM "item", EntityType.Builder.<ItemEntity>create(ItemEntity::new, size(0.25F, 0.25F).trackingRange(6).func_233608_b_(20));
    EntityType<ItemFrameEntity> ITEM_FRAME "item_frame", EntityType.Builder.<ItemFrameEntity>create(ItemFrameEntity::new, size(0.5F, 0.5F).trackingRange(10).func_233608_b_(Integer.MAX_VALUE));
    EntityType<FireballEntity> FIREBALL "fireball", EntityType.Builder.<FireballEntity>create(FireballEntity::new, size(1.0F, 1.0F).trackingRange(4).func_233608_b_(10));
    EntityType<LeashKnotEntity> LEASH_KNOT "leash_knot", EntityType.Builder.<LeashKnotEntity>create(LeashKnotEntity::new, disableSerialization().size(0.5F, 0.5F).trackingRange(10).func_233608_b_(Integer.MAX_VALUE));
    EntityType<LightningBoltEntity> LIGHTNING_BOLT "lightning_bolt", EntityType.Builder.<LightningBoltEntity>create(LightningBoltEntity::new, disableSerialization().size(0.0F, 0.0F).trackingRange(16).func_233608_b_(Integer.MAX_VALUE));
    EntityType<LlamaSpitEntity> LLAMA_SPIT "llama_spit", EntityType.Builder.<LlamaSpitEntity>create(LlamaSpitEntity::new, size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10));
    EntityType<MinecartEntity> MINECART "minecart", EntityType.Builder.<MinecartEntity>create(MinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));
    EntityType<ChestMinecartEntity> CHEST_MINECART "chest_minecart", EntityType.Builder.<ChestMinecartEntity>create(ChestMinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));
    EntityType<CommandBlockMinecartEntity> COMMAND_BLOCK_MINECART "command_block_minecart", EntityType.Builder.<CommandBlockMinecartEntity>create(CommandBlockMinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));
    EntityType<FurnaceMinecartEntity> FURNACE_MINECART "furnace_minecart", EntityType.Builder.<FurnaceMinecartEntity>create(FurnaceMinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));
    EntityType<HopperMinecartEntity> HOPPER_MINECART "hopper_minecart", EntityType.Builder.<HopperMinecartEntity>create(HopperMinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));
    EntityType<SpawnerMinecartEntity> SPAWNER_MINECART "spawner_minecart", EntityType.Builder.<SpawnerMinecartEntity>create(SpawnerMinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));
    EntityType<TNTMinecartEntity> TNT_MINECART "tnt_minecart", EntityType.Builder.<TNTMinecartEntity>create(TNTMinecartEntity::new, size(0.98F, 0.7F).trackingRange(8));

     */
    public LivingEntity getTargetEntity(Block filterBlock, LivingEntity entityIn)
    {
        if(filterBlock.equals(Blocks.EMERALD_BLOCK)) {if(entityIn instanceof PlayerEntity) {return (PlayerEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.DIAMOND_BLOCK)) {if(entityIn instanceof MonsterEntity) {return (MonsterEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.GOLD_BLOCK)) {if(entityIn instanceof AnimalEntity) {return (AnimalEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.LAPIS_BLOCK)) {if(entityIn instanceof FlyingEntity) {return (FlyingEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.IRON_BLOCK)) {if(entityIn instanceof CreatureEntity) {return (CreatureEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.COAL_BLOCK)) {if(entityIn instanceof MobEntity) {return (MobEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.HAY_BLOCK)) {if(entityIn.isChild()) {return entityIn;}}
        else if(filterBlock.equals(Blocks.DRIED_KELP_BLOCK)) {if(!entityIn.isChild()) {return entityIn;}}
        else if(filterBlock.equals(Blocks.LIME_STAINED_GLASS)) {if(entityIn instanceof VillagerEntity) {return (VillagerEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.BLACK_STAINED_GLASS)) {if(entityIn instanceof AbstractRaiderEntity) {return (AbstractRaiderEntity)entityIn;}}
        else {return (LivingEntity)entityIn;}
        return null;
    }

    public Entity getTargetEntityAdvanced(Block filterBlock, Entity entityIn)
    {
        if(filterBlock.equals(Blocks.EMERALD_BLOCK)) {if(entityIn instanceof PlayerEntity) {return (PlayerEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.DIAMOND_BLOCK)) {if(entityIn instanceof MonsterEntity) {return (MonsterEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.GOLD_BLOCK)) {if(entityIn instanceof AnimalEntity) {return (AnimalEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.LAPIS_BLOCK)) {if(entityIn instanceof FlyingEntity) {return (FlyingEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.IRON_BLOCK)) {if(entityIn instanceof CreatureEntity) {return (CreatureEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.COAL_BLOCK)) {if(entityIn instanceof MobEntity) {return (MobEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.HAY_BLOCK)) {if(entityIn instanceof LivingEntity) {if(((LivingEntity) entityIn).isChild())return entityIn;}}
        else if(filterBlock.equals(Blocks.DRIED_KELP_BLOCK)) {if(entityIn instanceof LivingEntity) {if(!((LivingEntity) entityIn).isChild())return entityIn;}}
        else if(filterBlock.equals(Blocks.LIME_STAINED_GLASS)) {if(entityIn instanceof VillagerEntity) {return (VillagerEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.BLACK_STAINED_GLASS)) {if(entityIn instanceof AbstractRaiderEntity) {return (AbstractRaiderEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.QUARTZ_BLOCK)) {if(entityIn instanceof ItemEntity) {return (ItemEntity)entityIn;}}
        else if(filterBlock.equals(Blocks.SLIME_BLOCK)) {if(entityIn instanceof ExperienceOrbEntity) {return (ExperienceOrbEntity)entityIn;}}
        else {return (Entity)entityIn;}
        return null;
    }

    public String getTargetEntity(Block filterBlock)
    {
        TranslationTextComponent EMERALD = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_emerald");
        TranslationTextComponent DIAMOND = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_diamond");
        TranslationTextComponent GOLD = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_gold");
        TranslationTextComponent LAPIS = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_lapis");
        TranslationTextComponent IRON = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_iron");
        TranslationTextComponent COAL = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_coal");
        TranslationTextComponent HAY = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_hay");
        TranslationTextComponent KELP = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_kelp");
        TranslationTextComponent GLASS_LIME = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_glass_lime");
        TranslationTextComponent GLASS_BLACK = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_glass_black");
        TranslationTextComponent ALL = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_all");

        if(filterBlock.equals(Blocks.EMERALD_BLOCK)) {return EMERALD.getString();}
        else if(filterBlock.equals(Blocks.DIAMOND_BLOCK)) {return DIAMOND.getString();}
        else if(filterBlock.equals(Blocks.GOLD_BLOCK)) {return GOLD.getString();}
        else if(filterBlock.equals(Blocks.LAPIS_BLOCK)) {return LAPIS.getString();}
        else if(filterBlock.equals(Blocks.IRON_BLOCK)) {return IRON.getString();}
        else if(filterBlock.equals(Blocks.COAL_BLOCK)) {return COAL.getString();}
        else if(filterBlock.equals(Blocks.HAY_BLOCK)) {return HAY.getString();}
        else if(filterBlock.equals(Blocks.DRIED_KELP_BLOCK)) {return KELP.getString();}
        else if(filterBlock.equals(Blocks.LIME_STAINED_GLASS)) {return GLASS_LIME.getString();}
        else if(filterBlock.equals(Blocks.BLACK_STAINED_GLASS)) {return GLASS_BLACK.getString();}
        else {return ALL.getString();}
    }

    public String getTargetEntityAdvanced(Block filterBlock)
    {
        TranslationTextComponent EMERALD = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_emerald");
        TranslationTextComponent DIAMOND = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_diamond");
        TranslationTextComponent GOLD = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_gold");
        TranslationTextComponent LAPIS = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_lapis");
        TranslationTextComponent IRON = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_iron");
        TranslationTextComponent COAL = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_coal");
        TranslationTextComponent ALL = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_all");
        TranslationTextComponent HAY = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_hay");
        TranslationTextComponent GLASS_LIME = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_glass_lime");
        TranslationTextComponent GLASS_BLACK = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_glass_black");
        TranslationTextComponent SLIME = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_slime");
        TranslationTextComponent QUARTZ = new TranslationTextComponent(Reference.MODID + ".target_entities" + ".entity_quartz");

        if(filterBlock.equals(Blocks.EMERALD_BLOCK)) {return EMERALD.getString();}
        else if(filterBlock.equals(Blocks.DIAMOND_BLOCK)) {return DIAMOND.getString();}
        else if(filterBlock.equals(Blocks.GOLD_BLOCK)) {return GOLD.getString();}
        else if(filterBlock.equals(Blocks.LAPIS_BLOCK)) {return LAPIS.getString();}
        else if(filterBlock.equals(Blocks.IRON_BLOCK)) {return IRON.getString();}
        else if(filterBlock.equals(Blocks.COAL_BLOCK)) {return COAL.getString();}
        else if(filterBlock.equals(Blocks.SLIME_BLOCK)) {return SLIME.getString();}
        else if(filterBlock.equals(Blocks.QUARTZ_BLOCK)) {return QUARTZ.getString();}
        else if(filterBlock.equals(Blocks.HAY_BLOCK)) {return HAY.getString();}
        else if(filterBlock.equals(Blocks.LIME_STAINED_GLASS)) {return GLASS_LIME.getString();}
        else if(filterBlock.equals(Blocks.BLACK_STAINED_GLASS)) {return GLASS_BLACK.getString();}
        else {return ALL.getString();}
    }

    public BlockPos getNegRangePosEntity(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(-intWidth,0,-intWidth);
            case DOWN:
                return blockBelow.add(-intWidth,-intHeight,-intWidth);
            case NORTH:
                return blockBelow.add(-intWidth,-intWidth,-intHeight);
            case SOUTH:
                return blockBelow.add(-intWidth,-intWidth,0);
            case EAST:
                return blockBelow.add(0,-intWidth,-intWidth);
            case WEST:
                return blockBelow.add(-intHeight,-intWidth,-intWidth);
            default:
                return blockBelow;
        }
    }

    public BlockPos getPosRangePosEntity(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(intWidth+1,intHeight,intWidth+1);
            case DOWN:
                return blockBelow.add(intWidth+1,0,intWidth+1);
            case NORTH:
                return blockBelow.add(intWidth+1,intWidth,0+1);
            case SOUTH:
                return blockBelow.add(intWidth+1,intWidth,intHeight+1);
            case EAST:
                return blockBelow.add(intHeight+1,intWidth,intWidth+1);
            case WEST:
                return blockBelow.add(0+1,intWidth,intWidth+1);
            default:
                return blockBelow;
        }
    }

    public boolean canThisPedestalReceiveItemStack(PedestalTileEntity pedestal, World world, BlockPos posOfPedestal, ItemStack itemStackIncoming)
    {
        boolean filter = true;
        //Checks if pedestal is empty or if not then checks if items match and how many can be insert
        if(pedestal.canAcceptItems(world,posOfPedestal,itemStackIncoming) > 0)
        {

            //Check if it has filter, if not return true
            if(pedestal.hasFilter())
            {
                Item filterInPedestal = pedestal.getFilterInPedestal().getItem();
                if(filterInPedestal instanceof IFilterBase)
                {
                    filter = ((IFilterBase) filterInPedestal).canAcceptItem(pedestal,itemStackIncoming);
                }
            }
            //Should return true by default, or fals eif a filter or coin blocks it???
        }
        return filter;
    }

    public void upgradeActionMagnet(PedestalTileEntity pedestal, World world, List<ItemEntity> itemList, ItemStack itemInPedestal, BlockPos posOfPedestal)
    {
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(itemList.size()>0)
        {
            for(ItemEntity getItemFromList : itemList)
            {
                ItemStack copyStack = getItemFromList.getItem().copy();
                int maxSize = copyStack.getMaxStackSize();
                boolean stacksMatch = doItemsMatch(itemInPedestal,copyStack);
                if ((itemInPedestal.isEmpty() || stacksMatch ) && canThisPedestalReceiveItemStack(pedestal,world,posOfPedestal,copyStack))
                {
                    int spaceInPed = itemInPedestal.getMaxStackSize()-itemInPedestal.getCount();
                    if(stacksMatch)
                    {
                        if(spaceInPed > 0)
                        {
                            int itemInCount = getItemFromList.getItem().getCount();
                            int countToAdd = ( itemInCount<= spaceInPed)?(itemInCount):(spaceInPed);
                            getItemFromList.getItem().setCount(itemInCount-countToAdd);
                            copyStack.setCount(countToAdd);
                            pedestal.addItem(copyStack);
                            if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        }
                        else if(!hasAdvancedInventoryTargetingTwo(coinInPedestal))break;
                    }
                    else if(copyStack.getCount() <=maxSize)
                    {
                        getItemFromList.setItem(ItemStack.EMPTY);
                        getItemFromList.remove();
                        pedestal.addItem(copyStack);
                        if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);

                    }
                    else
                    {
                        //If an ItemStackEntity has more than 64, we subtract 64 and inset 64 into the pedestal
                        int count = getItemFromList.getItem().getCount();
                        getItemFromList.getItem().setCount(count-maxSize);
                        copyStack.setCount(maxSize);
                        pedestal.addItem(copyStack);
                        if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    }
                    if(!hasAdvancedInventoryTargetingTwo(coinInPedestal))break;
                }
            }
        }
    }

    public ItemStack getFilterReturnStack(List<ItemStack> stack, ItemStack incoming)
    {
        int range = stack.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stack)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem().equals(incoming.getItem()))
                .findFirst().orElse(ItemStack.EMPTY);

        return itemFromInv;
    }
    /***************************************
     ****************************************
     **        End of Entity Stuff         **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **        Start of Player Stuff       **
     ****************************************
     ***************************************/
    public void removePlayerFromCoin(ItemStack stack)
    {
        if(hasPlayerSet(stack))
        {
            CompoundNBT compound = new CompoundNBT();
            if(stack.hasTag())
            {
                compound = stack.getTag();
                if(compound.contains("player"))
                {
                    compound.remove("player");
                }

                if(compound.contains("playername"))
                {
                    compound.remove("playername");
                }

                stack.setTag(compound);
            }
        }
    }

    //
    //UUID HANDLING
    //

    public UUID getPlayerFromCoin(ItemStack stack)
    {
        if(hasPlayerSet(stack))
        {
            UUID playerID = readUUIDFromNBT(stack);
            if(playerID !=null)
            {
                return playerID;
            }
        }
        return Util.DUMMY_UUID;
    }

    public WeakReference<FakePlayer> fakePedestalPlayer(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ServerWorld sworld = world.getServer().getWorld(world.getDimensionKey());
        return new WeakReference<FakePlayer>(new PedestalFakePlayer(sworld ,getPlayerFromCoin(pedestal.getCoinOnPedestal()), getPlayerNameFromCoin(pedestal.getCoinOnPedestal()),pedestal));
    }

    public WeakReference<FakePlayer> fakePedestalPlayer(PedestalTileEntity pedestal, ItemStack itemInHand)
    {
        World world = pedestal.getWorld();
        ServerWorld sworld = world.getServer().getWorld(world.getDimensionKey());
        return new WeakReference<FakePlayer>(new PedestalFakePlayer(sworld,getPlayerFromCoin(pedestal.getCoinOnPedestal()), getPlayerNameFromCoin(pedestal.getCoinOnPedestal()),pedestal.getPos(),itemInHand));
    }

    @Override
    public void setPlayerOnCoin(ItemStack stack, PlayerEntity player)
    {
        writeUUIDToNBT(stack,player.getUniqueID());
        writeNameToNBT(stack,player.getDisplayName().getString());
    }

    public boolean hasPlayerSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("player"))
            {
                if(readUUIDFromNBT(stack) !=null)
                {
                    returner = true;
                }
            }
        }
        return returner;
    }

    public void writeUUIDToNBT(ItemStack stack, UUID uuidIn)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putUniqueId("player",uuidIn);
        stack.setTag(compound);
    }

    public UUID readUUIDFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            return getCompound.getUniqueId("player");
        }

        return null;
    }

    //
    //PLAYER NAME HANDLING
    //

    public String getPlayerNameFromCoin(ItemStack stack)
    {
        if(hasPlayerNameSet(stack))
        {
            return readNameFromNBT(stack);
        }
        return null;
    }

    public boolean hasPlayerNameSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("playername"))
            {
                if(readNameFromNBT(stack) !=null)
                {
                    returner = true;
                }
            }
        }
        return returner;
    }

    public void writeNameToNBT(ItemStack stack, String name)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putString("playername",name);
        stack.setTag(compound);
    }

    public String readNameFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            return getCompound.getString("playername");
        }

        return null;
    }
    /***************************************
     ****************************************
     **         End of Player Stuff        **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **         Start of Queue Stuff       **
     ****************************************
     ***************************************/

    public void removeWorkQueueFromCoin(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("workqueueposx"))
            {
                compound.remove("workqueueposx");
                compound.remove("workqueueposy");
                compound.remove("workqueueposz");
                stack.setTag(compound);
            }
        }
    }

    public int workQueueSize(ItemStack coin)
    {
        int workQueueSize = 0;
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("workqueueposx"))
            {
                int[] xval = getCompound.getIntArray("workqueueposx");
                return xval.length;
            }
        }

        return workQueueSize;
    }

    public void buildWorkQueue(PedestalTileEntity pedestal, int width, int height)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        ItemStack pickaxe = pedestal.getToolOnPedestal();
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        int rangeHeight = (enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST) ? (height - 1) : (height);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width, rangeHeight);
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width, rangeHeight);
        FakePlayer fakePlayer =  fakePedestalPlayer(pedestal).get();
        if(fakePlayer !=null)
        {
            fakePlayer.setSilent(true);
            if(!fakePlayer.getPosition().equals(new BlockPos(pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ()))) {fakePlayer.setPosition(pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ());}
            if(!doItemsMatch(fakePlayer.getHeldItemMainhand(),pickaxe))fakePlayer.setItemStackToSlot(EquipmentSlotType.MAINHAND,pickaxe);


            List<BlockPos> workQueue = new ArrayList<>();

            for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
            {
                BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                if(canMineBlock(pedestal, blockToMinePos,fakePlayer))
                {
                    workQueue.add(blockToMinePos);
                }
            }

            writeWorkQueueToNBT(coin, workQueue);
        }
    }

    public void writeWorkQueueToNBT(ItemStack coin, List<BlockPos> listIn)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        List<Integer> xval = new ArrayList<Integer>();
        List<Integer> yval = new ArrayList<Integer>();
        List<Integer> zval = new ArrayList<Integer>();
        for(int i=0;i<listIn.size();i++)
        {
            xval.add(i,listIn.get(i).getX());
            yval.add(i,listIn.get(i).getY());
            zval.add(i,listIn.get(i).getZ());
        }
        compound.putIntArray("workqueueposx",xval);
        compound.putIntArray("workqueueposy",yval);
        compound.putIntArray("workqueueposz",zval);

        coin.setTag(compound);
    }

    public List<BlockPos> readWorkQueueFromNBT(ItemStack coin)
    {
        List<BlockPos> workQueue = new ArrayList<>();

        if(workQueueSize(coin)>=0)
        {
            if(coin.hasTag())
            {
                CompoundNBT getCompound = coin.getTag();
                int[] xval = getCompound.getIntArray("workqueueposx");
                int[] yval = getCompound.getIntArray("workqueueposy");
                int[] zval = getCompound.getIntArray("workqueueposz");

                for(int i = 0;i<xval.length;i++)
                {
                    workQueue.add(new BlockPos(xval[i],yval[i],zval[i]));
                }
            }
        }

        return workQueue;
    }

    public void removeWorkQueueTwoFromCoin(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("workqueuetwoposx"))
            {
                compound.remove("workqueuetwoposx");
                compound.remove("workqueuetwoposy");
                compound.remove("workqueuetwoposz");
                stack.setTag(compound);
            }
        }
    }


    public int workQueueTwoSize(ItemStack coin)
    {
        int workQueueTwoSize = 0;
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("workqueuetwoposx"))
            {
                int[] xval = getCompound.getIntArray("workqueuetwoposx");
                return xval.length;
            }

        }

        return workQueueTwoSize;
    }

    public void buildWorkQueueTwo(PedestalTileEntity pedestal, int width, int height)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        List<BlockPos> workQueueTwo = new ArrayList<>();

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            if(canMineBlockTwo(pedestal, blockToMinePos))
            {
                workQueueTwo.add(blockToMinePos);
            }
        }

        writeWorkQueueTwoToNBT(coin, workQueueTwo);
    }

    public void writeWorkQueueTwoToNBT(ItemStack coin, List<BlockPos> listIn)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        List<Integer> xval = new ArrayList<Integer>();
        List<Integer> yval = new ArrayList<Integer>();
        List<Integer> zval = new ArrayList<Integer>();
        for(int i=0;i<listIn.size();i++)
        {
            xval.add(i,listIn.get(i).getX());
            yval.add(i,listIn.get(i).getY());
            zval.add(i,listIn.get(i).getZ());
        }
        compound.putIntArray("workqueuetwoposx",xval);
        compound.putIntArray("workqueuetwoposy",yval);
        compound.putIntArray("workqueuetwoposz",zval);

        coin.setTag(compound);
    }

    public List<BlockPos> readWorkQueueTwoFromNBT(ItemStack coin)
    {
        List<BlockPos> workQueueTwo = new ArrayList<>();

        if(workQueueTwoSize(coin)>=0)
        {
            if(coin.hasTag())
            {
                CompoundNBT getCompound = coin.getTag();
                int[] xval = getCompound.getIntArray("workqueuetwoposx");
                int[] yval = getCompound.getIntArray("workqueuetwoposy");
                int[] zval = getCompound.getIntArray("workqueuetwoposz");

                for(int i = 0;i<xval.length;i++)
                {
                    workQueueTwo.add(new BlockPos(xval[i],yval[i],zval[i]));
                }
            }
        }

        return workQueueTwo;
    }

    public void removeFilterQueueHandler(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("filterqueue"))
            {
                compound.remove("filterqueue");
                stack.setTag(compound);
            }
        }
    }

    public int filterQueueSize(ItemStack coin)
    {
        int filterQueueSize = 0;
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("filterqueue"))
            {
                getCompound.get("filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(getCompound);
                return handler.getSlots();
            }
        }

        return filterQueueSize;
    }

    public List<ItemStack> buildFilterQueue(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos posInventory = getPosOfBlockBelow(world, pedestalPos, 1);

        List<ItemStack> filterQueue = new ArrayList<>();

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, pedestalPos),true);
        if(hasAdvancedInventoryTargeting(coin))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, pedestalPos),true);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = handler.getStackInSlot(i);
                    if(!stackInSlot.isEmpty()) {filterQueue.add(stackInSlot);}
                }
            }
        }

        return filterQueue;
    }

    public void writeFilterQueueToNBT(ItemStack stack, List<ItemStack> listIn)
    {
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT compoundStorage = new CompoundNBT();
        if(stack.hasTag()){compound = stack.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listIn.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put("filterqueue",compoundStorage);
        stack.setTag(compound);
    }

    public List<ItemStack> readFilterQueueFromNBT(ItemStack coin)
    {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("filterqueue"))
            {
                CompoundNBT invTag = getCompound.getCompound("filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundNBT>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
            }
        }

        return filterQueue;
    }

    public boolean doesFilterAndQueueMatch(List<ItemStack> filterIn, List<ItemStack> queueMatch)
    {
        int matching = 0;
        if(filterIn.size() == queueMatch.size())
        {
            for(int i=0;i<filterIn.size();i++)
            {
                if(doItemsMatchWithEmpty(filterIn.get(i),queueMatch.get(i)))
                {
                    matching++;
                    continue;
                }
                else
                {
                    break;
                }
            }
        }

        return matching == filterIn.size();
    }

    public void removeInventoryQueue(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("invqueue"))
            {
                compound.remove("invqueue");
                stack.setTag(compound);
            }
        }
    }

    public List<ItemStack> buildInventoryQueue(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos posInventory = getPosOfBlockBelow(world, pedestalPos, 1);

        List<ItemStack> filterQueue = new ArrayList<>();

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, pedestalPos),true);
        if(hasAdvancedInventoryTargeting(coin))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, pedestalPos),true);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = handler.getStackInSlot(i).copy();
                    if(stackInSlot.getCount() > 64)stackInSlot.setCount(64);
                    filterQueue.add(stackInSlot);
                }
            }
        }

        return filterQueue;
    }

    public void writeInventoryQueueToNBT(ItemStack stack, List<ItemStack> listIn)
    {
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT tag = new CompoundNBT();
        if(stack.hasTag()){tag = stack.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listIn.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}

        compound = ((INBTSerializable<CompoundNBT>) handler).serializeNBT();
        tag.put("invqueue", compound);
        stack.setTag(tag);
    }

    public List<ItemStack> readInventoryQueueFromNBT(ItemStack coin)
    {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("invqueue"))
            {
                CompoundNBT invTag = getCompound.getCompound("invqueue");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundNBT>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
            }
        }

        return filterQueue;
    }

    public boolean doInventoryQueuesMatch(List<ItemStack> stackIn, List<ItemStack> stackCurrent)
    {
        int matching = 0;
        if(stackIn.size() == stackCurrent.size())
        {
            for(int i=0;i<stackCurrent.size();i++)
            {
                if(stackIn.size()<i)break;
                if(doItemsMatchWithEmpty(stackIn.get(i),stackCurrent.get(i)))
                {
                    matching++;
                    continue;
                }
                else break;
            }
        }

        return matching == stackIn.size();
    }


    public static IRecipe<CraftingInventory> findRecipe(CraftingInventory inv, World world) {
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).orElse(null);
    }

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_ONE)){gridSize = 1;}
        else if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_THREE)){gridSize = 3;}
        else{gridSize = 1;}

        return gridSize;
    }

    //Write recipes in order as they appear in the inventory, and since we check for changed, we should be able to get which recipe is which
    public void buildAndWriteCraftingQueue(PedestalTileEntity pedestal, List<ItemStack> inventoryQueue)
    {
        World world = pedestal.getWorld();
        ItemStack coin = pedestal.getCoinOnPedestal();
        int gridSize = getGridSize(coin);
        int intGridCount = gridSize*gridSize;
        List<ItemStack> invQueue = inventoryQueue;
        int recipeCount = Math.floorDiv(invQueue.size(),intGridCount);

        List<ItemStack> recipeQueue = new ArrayList<>();

        CraftingInventory craft = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity playerIn) {
                return false;
            }
        }, gridSize, gridSize);

        for(int r=1;r<= recipeCount; r++)
        {
            for(int s=0;s<intGridCount; s++)
            {
                int getActualIndex = ((r*intGridCount)-intGridCount)+s;
                ItemStack getStack = invQueue.get(getActualIndex);
                //If the item Stack has enough items to craft with
                //stack.getCount()>=2 ||  stack.maxStackSize()==1 ||
                if(getStack.isEmpty() || getStack.getItem() instanceof ItemCraftingPlaceholder)
                {
                    ////System.out.println("SetEmpty");
                    craft.setInventorySlotContents(s, ItemStack.EMPTY);
                }
                else
                {
                    ////System.out.println("SetNormal: " + getStack);
                    craft.setInventorySlotContents(s,getStack);
                }
            }
            //Checks to make sure we have enough slots set for out recipe
            if(craft.getSizeInventory() >= intGridCount)
            {
                IRecipe recipe = findRecipe(craft,world);
                if(recipe  != null &&  recipe.matches(craft, world)) {
                    //Set ItemStack with recipe result
                    ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                    recipeQueue.add(stackRecipeResult);
                }
                else
                {
                    recipeQueue.add(ItemStack.EMPTY);
                }
            }
        }
        writeCraftingQueueToNBT(coin, recipeQueue);
    }

    public void buildOutputIngredientMapFromPattern(PedestalTileEntity pedestal)
    {
        if(pedestal.hasFilter())
        {
            ItemStack patternStack = pedestal.getFilterInPedestal();
            if(patternStack.getItem() instanceof IFilterBase)
            {
                IFilterBase filterClassItem = ((IFilterBase)patternStack.getItem());
                List<ItemStack> patternList = filterClassItem.readFilterQueueFromNBT(patternStack);
                Map<ItemStack, List<ItemStack>> mappedOutputIngredients = buildIngredientList(pedestal, patternList);
                writeOutputIngredientMapToNBT(pedestal.getCoinOnPedestal(),mappedOutputIngredients);
            }
        }
    }

    public Map<ItemStack, List<ItemStack>> buildIngredientList(PedestalTileEntity pedestal, List<ItemStack> inventoryQueue)
    {
        World world = pedestal.getWorld();
        ItemStack coin = pedestal.getCoinOnPedestal();
        int gridSize = getGridSize(coin);
        int intGridCount = gridSize*gridSize;
        List<ItemStack> invQueue = inventoryQueue;
        int recipeCount = Math.floorDiv(invQueue.size(),intGridCount);

        //The Result is the Key, and the List is the ingredients
        Map<ItemStack, List<ItemStack>> ingredientMap = Maps.<ItemStack, List<ItemStack>>newLinkedHashMap();

        CraftingInventory craft = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity playerIn) {
                return false;
            }
        }, gridSize, gridSize);

        for(int r=1;r<= recipeCount; r++)
        {
            List<ItemStack> ingredientQueue = new ArrayList<>();
            for(int s=0;s<intGridCount; s++)
            {
                int getActualIndex = ((r*intGridCount)-intGridCount)+s;
                ItemStack getStack = invQueue.get(getActualIndex);
                //If the item Stack has enough items to craft with
                //stack.getCount()>=2 ||  stack.maxStackSize()==1 ||
                if(getStack.isEmpty() || getStack.getItem() instanceof ItemCraftingPlaceholder)
                {
                    craft.setInventorySlotContents(s, ItemStack.EMPTY);
                }
                else
                {
                    //Add to current queue
                    ItemStack itemInList=ItemStack.EMPTY;
                    itemInList = ingredientQueue.stream().filter(itemStack -> doItemsMatch(getStack,itemStack)).findFirst().orElse(ItemStack.EMPTY);
                    if(!itemInList.isEmpty())
                    {
                        int index = ingredientQueue.lastIndexOf(itemInList);
                        if(index>-1)
                        {
                            ItemStack current = ingredientQueue.remove(index);
                            int countAdded =current.getCount()+getStack.getCount();
                            current.setCount(countAdded);
                            ingredientQueue.add(current);
                        }
                        else ingredientQueue.add(getStack);
                    }
                    else ingredientQueue.add(getStack);

                    craft.setInventorySlotContents(s,getStack);
                }
            }
            //Checks to make sure we have enough slots set for out recipe
            if(craft.getSizeInventory() >= intGridCount)
            {
                IRecipe recipe = findRecipe(craft,world);
                if(recipe  != null &&  recipe.matches(craft, world)) {
                    //Set ItemStack with recipe result
                    ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                    ingredientMap.put(stackRecipeResult,ingredientQueue);
                }
            }
        }

        return ingredientMap;
    }

    public void writeOutputIngredientMapToNBT(ItemStack coin, Map<ItemStack, List<ItemStack>> mapIn)
    {
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT tag = new CompoundNBT();
        if(coin.hasTag()){tag = coin.getTag();}

        ListNBT nbtList = new ListNBT();
        int counter = 0;
        for(Map.Entry<ItemStack, List<ItemStack>> entry : mapIn.entrySet())
        {
            ItemStack key = entry.getKey();
            if (key != null) {
                List<ItemStack> ingredientList = entry.getValue();
                int handlerSize = ingredientList.size() +1;

                ItemStackHandler handler = new ItemStackHandler();
                handler.setSize(handlerSize);

                //Store Key in slot 0
                handler.setStackInSlot(0,key);
                //Store Ingredients in slots 1+
                for(int i=0;i<ingredientList.size();i++) {handler.setStackInSlot(i+1,ingredientList.get(i));}
                compound = ((INBTSerializable<CompoundNBT>) handler).serializeNBT();
                nbtList.add(counter, compound);
                counter++;
            }
        }

        coin.setTagInfo("outputIngredientMap", nbtList);
    }

    public Map<ItemStack, List<ItemStack>> readOutputIngredientMapFromNBT(ItemStack coin)
    {
        Map<ItemStack, List<ItemStack>> craftingQueue = Maps.<ItemStack, List<ItemStack>>newLinkedHashMap();
        if(coin.hasTag())
        {
            if(coin.getTag().contains("outputIngredientMap"))
            {
                ListNBT getList = coin.getTag().getList("outputIngredientMap",10);

                for(int m=0;m<getList.size();m++)
                {
                    List<ItemStack> filterQueue = new ArrayList<>();
                    CompoundNBT compoundnbt = getList.getCompound(m);
                    ItemStackHandler handler = new ItemStackHandler();
                    ((INBTSerializable<CompoundNBT>) handler).deserializeNBT(compoundnbt);

                    //start at 1 since 0 is our key item
                    for(int i=1;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
                    craftingQueue.put(handler.getStackInSlot(0),filterQueue);
                }
            }
        }

        return craftingQueue;
    }

    public void removeOutputIngredientMap(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("outputIngredientMap"))
            {
                compound.remove("outputIngredientMap");
                stack.setTag(compound);
            }
        }
    }

    public boolean getFilterChangeStatus(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            if(compound.contains("filterChange"))
            {
                return compound.getBoolean("filterChange");
            }
        }

        return false;
    }

    public void setFilterChangeUpdate(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            compound.putBoolean("filterChange",true);
        }
        else compound.putBoolean("filterChange",true);

        coin.setTag(compound);
    }

    public void setFilterChangeUpdated(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            compound.putBoolean("filterChange",false);
        }

        coin.setTag(compound);
    }

    public void removeFilterChangeUpdated(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            if(compound.contains("filterChange"))
            {
                compound.remove("filterChange");
                coin.setTag(compound);
            }
        }
    }

    public void writeCraftingQueueToNBT(ItemStack stack, List<ItemStack> listIn)
    {
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT tag = new CompoundNBT();
        if(stack.hasTag()){tag = stack.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listIn.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}

        compound = ((INBTSerializable<CompoundNBT>) handler).serializeNBT();
        tag.put("craftqueue", compound);
        stack.setTag(tag);
    }

    public List<ItemStack> readCraftingQueueFromNBT(ItemStack coin)
    {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("craftqueue"))
            {
                CompoundNBT invTag = getCompound.getCompound("craftqueue");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundNBT>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
            }
        }

        return filterQueue;
    }

    public void removeCraftingQueue(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("craftqueue"))
            {
                compound.remove("craftqueue");
                stack.setTag(compound);
            }
        }
    }

    @Override
    public void notifyTransferUpdate(PedestalTileEntity receiverTile)
    {

    }

    public void removeFilterBlock(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("blockbelow"))
            {
                compound.remove("blockbelow");
                stack.setTag(compound);
            }
        }
    }

    public boolean hasFilterBlock(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("blockbelow"))
            {
                return true;
            }
        }

        return false;
    }

    public void writeFilterBlockToNBT(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        Block blockBelow = getBaseBlockBelow(world,pedestalPos);

        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag()){compound = coin.getTag();}

        compound.putString("blockbelow", blockBelow.getRegistryName() == null?"minecraft:air":blockBelow.getRegistryName().toString());

        coin.setTag(compound);
    }

    public Block readFilterBlockFromNBT(ItemStack coin)
    {
        Block blocked = Blocks.AIR;
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            blocked = (Block)Registry.BLOCK.getOrDefault(new ResourceLocation(getCompound.getString("blockbelow")));
        }
        return blocked;
    }

    /*
    NOTES FOR THE CRAFTERS/MACHINES
    MAYBE ADD A VARIABLE TO THE PROCESS SO WHEN WE CRAFT SOMETHING, BEFORE THE EXTRACT, ITERATE THE VAR ONCE, THEN EXTRACT,
    THE EXTRACT WILL TRIGGER A CHANGE, BUT WITH OUR VAR OVER 0 IT WONT CHECK TO VERIFY THE INVENTORY SO WE JUST NEED TO SAVE OUR MODIFIED LIST AND CONTINUE.
     */

    /***************************************
     ****************************************
     **          End of Queue Stuff        **
     ****************************************
     ***************************************/




    /***************************************
     ****************************************
     **       Start Of StoredINT Stuff     **
     ****************************************
     ***************************************/


    public void removeStoredIntFromCoin(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("storedint"))
            {
                compound.remove("storedint");
                stack.setTag(compound);
            }
        }
    }

    //This function used to be in the pedestal, but got moved out for performance improvements
    public void writeStoredIntToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("storedint",value);
        stack.setTag(compound);
    }

    public int readStoredIntFromNBT(ItemStack stack)
    {
        int value = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            value = getCompound.getInt("storedint");
        }
        return value;
    }

    public int getStoredInt(ItemStack coin)
    {
        return readStoredIntFromNBT(coin);
    }



    public void removeStoredIntTwoFromCoin(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("storedinttwo"))
            {
                compound.remove("storedinttwo");
                stack.setTag(compound);
            }
        }
    }

    //This is needed for the "slowdown" of machines
    public void writeStoredIntTwoToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("storedinttwo",value);
        stack.setTag(compound);
    }

    public int readStoredIntTwoFromNBT(ItemStack stack)
    {
        int maxenergy = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxenergy = getCompound.getInt("storedinttwo");
        }
        return maxenergy;
    }

    public int getStoredIntTwo(ItemStack coin)
    {
        return readStoredIntTwoFromNBT(coin);
    }


    //Delay Int

    public void removeDelayFromCoin(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("storeddelay"))
            {
                compound.remove("storeddelay");
                stack.setTag(compound);
            }
        }
    }

    //This is needed for the "slowdown" of machines
    public void writeDelayToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("storeddelay",value);
        stack.setTag(compound);
    }

    public int readDelayFromNBT(ItemStack stack)
    {
        int maxenergy = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxenergy = getCompound.getInt("storeddelay");
        }
        return maxenergy;
    }

    public int getDelay(ItemStack coin)
    {
        return readDelayFromNBT(coin);
    }


    /***************************************
     ****************************************
     **      End Of StoredINT Stuff        **
     ****************************************
     ***************************************/




    /***************************************
     ****************************************
     **     Start of UpgradeTool Stuff     **
     ****************************************
     ***************************************/
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{0,0};
    }

    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {

    }


    public boolean getToolChangeStatus(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            if(compound.contains("toolChange"))
            {
                return compound.getBoolean("toolChange");
            }
        }

        return false;
    }

    public void setToolChangeUpdate(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            compound.putBoolean("toolChange",true);
        }
        else compound.putBoolean("toolChange",true);

        coin.setTag(compound);
    }

    public void setToolChangeUpdated(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            compound.putBoolean("toolChange",false);
        }

        coin.setTag(compound);
    }

    public void removeToolChangeUpdated(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
            if(compound.contains("toolChange"))
            {
                compound.remove("toolChange");
                coin.setTag(compound);
            }
        }
    }
    /***************************************
     ****************************************
     **      End of UpgradeTool Stuff      **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **        Start of Client Stuff       **
     ****************************************
     ***************************************/
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {

    }

    public void spawnParticleAroundPedestalBase(World world, int tick, BlockPos pos, BasicParticleType parti)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();

        BlockState state = world.getBlockState(pos);
        Direction enumfacing = Direction.UP;
        if(state.getBlock() instanceof PedestalBlock)
        {
            enumfacing = state.get(FACING);
        }
        switch (enumfacing)
        {
            case UP:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
            case DOWN:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.75D,0, 0, 0);
                return;
            case NORTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+.85D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+.85D,0, 0, 0);
                return;
            case SOUTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+0.15D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+0.15D,0, 0, 0);
                return;
            case EAST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.75D,0, 0, 0);
                return;
            case WEST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.75D,0, 0, 0);
                return;
            default:
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%35 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
        }
    }

    public void spawnParticleAroundPedestalBase(World world,int tick, BlockPos pos, float r, float g, float b, float alpha)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();

        BlockState state = world.getBlockState(pos);
        Direction enumfacing = Direction.UP;
        if(state.getBlock() instanceof PedestalBlock)
        {
            enumfacing = state.get(FACING);
        }
        BlockPos blockBelow = pos;
        RedstoneParticleData parti = new RedstoneParticleData(r, g, b, alpha);
        switch (enumfacing)
        {
            case UP:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
            case DOWN:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.75D,0, 0, 0);
                return;
            case NORTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+.85D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+.85D,0, 0, 0);
                return;
            case SOUTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+0.15D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+0.15D,0, 0, 0);
                return;
            case EAST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.75D,0, 0, 0);
                return;
            case WEST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.75D,0, 0, 0);
                return;
            default:
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%35 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
        }
    }

    public void spawnParticleAbovePedestal(World world, BlockPos pos, float r, float g, float b, float alpha)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();
        BlockState state = world.getBlockState(pos);
        Direction enumfacing = Direction.UP;
        if(state.getBlock() instanceof PedestalBlock)
        {
            enumfacing = state.get(FACING);
        }
        RedstoneParticleData parti = new RedstoneParticleData(r, g, b, alpha);
        switch (enumfacing)
        {
            case UP:
                world.addParticle(parti, dx+ 0.5D, dy+1.0D, dz+ 0.5D,0, 0, 0);
                return;
            case DOWN:
                world.addParticle(parti, dx+ 0.5D, dy-1.0D, dz+ 0.5D,0, 0, 0);
                return;
            case NORTH:
                world.addParticle(parti, dx+ 0.5D, dy+0.5D, dz-1.0D,0, 0, 0);
                return;
            case SOUTH:
                world.addParticle(parti, dx+ 0.5D, dy+0.5D, dz+1.0D,0, 0, 0);
                return;
            case EAST:
                world.addParticle(parti, dx+1.0D, dy+ 0.5D, dz+0.5D,0, 0, 0);
                return;
            case WEST:
                world.addParticle(parti, dx-1.0D, dy+0.5D, dz+ 0.5D,0, 0, 0);
                return;
            default:
                world.addParticle(parti, dx+ 0.5D, dy+1.0D, dz+ 0.5D,0, 0, 0);
                return;
        }
    }

    //Thanks to Lothrazar for this: https://github.com/Lothrazar/Cyclic/blob/5946452faedd1a59375f7813f5ec9f861914ed8a/src/main/java/com/lothrazar/cyclic/base/BlockBase.java#L59
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        ResourceLocation disabled = new ResourceLocation("pedestals", "enchant_limits/advanced_blacklist");
        ITag<Item> BLACKLISTED = ItemTags.getCollection().get(disabled);

         /*if(getAdvancedModifier(stack)<=0 && (BLACKLISTED !=null)?(!BLACKLISTED.contains(stack.getItem())):(false) && (intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5))
        {
            TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
            warning.mergeStyle(TextFormatting.RED);
            tooltip.add(warning);
        }*/

        //makes sure tag exist before trying to continue
        if(BLACKLISTED !=null)
        {
            //if item isnt in blacklist tag
            if(!BLACKLISTED.contains(stack.getItem()))
            {
                //if any of the enchants are over level 5
                if(intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5)
                {
                    //if it doesnt have advanced
                    if(getAdvancedModifier(stack)<=0)
                    {
                        TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
                        warning.mergeStyle(TextFormatting.RED);
                        tooltip.add(warning);
                    }
                }
            }
            //if it is
            else
            {
                //Advanced disabled warning only shows after upgrade has advanced on it, isnt great, but alerts the user it wont work unfortunately
                if(getAdvancedModifier(stack)>0)
                {
                    TranslationTextComponent disabled_warning = new TranslationTextComponent(Reference.MODID + ".advanced_disabled_warning");
                    disabled_warning.mergeStyle(TextFormatting.DARK_RED);
                    tooltip.add(disabled_warning);
                }
            }
        }

    }
    /***************************************
     ****************************************
     **        End of Client Stuff         **
     ****************************************
     ***************************************/



    /***************************************
     ****************************************
     **      Start of Pedestal Stuff       **
     ****************************************
     ***************************************/
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack itemstack = pedestal.getItemInPedestal();
            if(!itemstack.isEmpty())
            {
                float f = (float)itemstack.getCount()/(float)Math.min(pedestal.maxStackSize(), itemstack.getMaxStackSize());
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {

    }

    @Override
    public void actionOnCollideWithBlock(PedestalTileEntity tilePedestal, Entity entityIn)
    {
        World world = tilePedestal.getWorld();
        BlockPos posPedestal = tilePedestal.getPos();
        BlockState state = world.getBlockState(posPedestal);
        actionOnCollideWithBlock(world, tilePedestal, posPedestal, state, entityIn);
    }


    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {

    }


    /***************************************
     ****************************************
     **       End of Pedestal Stuff        **
     ****************************************
     ***************************************/
}
