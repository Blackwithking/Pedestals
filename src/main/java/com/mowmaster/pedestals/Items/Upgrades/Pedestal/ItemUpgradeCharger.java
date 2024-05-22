package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemUpgradeCharger extends ItemUpgradeBase implements IHasModeTypes {
    public ItemUpgradeCharger(Properties p_41383_) { super(p_41383_); }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyEnergyCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = new ArrayList<>();

        if (pedestal.getItemInPedestal().isEmpty()) {
            if (pedestal.getStoredEnergy() <= 0) {
                boolean hasItem = MowLibCompoundTagUtils.readBooleanFromNBT(References.MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),"hasitem");
                if (pedestal.getStoredEnergy() <= 0 && hasItem) {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                }
            }
        }

        return messages;
    }

    private boolean canProcess(ItemStack toProcess) {

        LazyOptional<IEnergyStorage> cap = toProcess.getCapability(ForgeCapabilities.ENERGY);
        if(cap.isPresent()) {
            IEnergyStorage energyHandler = cap.orElse(null);
            return !toProcess.isEmpty() && (energyHandler != null);
        }

        return false;
    }

    public Optional<Integer> getFirstSlotWithItemThatCanBeProcessed(BasePedestalBlockEntity pedestal, IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (canProcess(stackInSlot)) {
                if (!itemHandler.extractItem(i,1 ,true).equals(ItemStack.EMPTY)) {
                    if (passesItemFilter(pedestal, stackInSlot) && canProcess(stackInSlot)) {
                        return Optional.of(i);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        BlockPos inventoryPos = getPosOfBlockBelow(level,pedestal.getPos(),1);
        BlockEntity invToPullFrom = level.getBlockEntity(inventoryPos);
        if (invToPullFrom instanceof BasePedestalBlockEntity) {
            return;
        }

        MowLibItemUtils.findItemHandlerAtPos(level, inventoryPos, getPedestalFacing(level, pedestalPos), true).ifPresent(handler -> {
            // Handle enchants.
            Optional<Integer> slotToHandle = getFirstSlotWithItemThatCanBeProcessed(pedestal, handler);

            if(canTransferEnergy(coin))
            {
                slotToHandle.ifPresent(i -> {
                    ItemStack itemFromInv = handler.getStackInSlot(i);
                    if (!itemFromInv.isEmpty()) {
                        LazyOptional<IEnergyStorage> cap = itemFromInv.getCapability(ForgeCapabilities.ENERGY);
                        if(cap.isPresent())
                        {
                            IEnergyStorage energyHandler = cap.orElse(null);

                            if(energyHandler != null)
                            {
                                if(energyHandler.canReceive())
                                {
                                    int getEnergyInPedestal = pedestal.getStoredEnergy();

                                    int containerCurrentEnergy = energyHandler.getEnergyStored();
                                    int getContainerMaxEnergy = energyHandler.getMaxEnergyStored();
                                    int getSpaceForEnergy = getContainerMaxEnergy - containerCurrentEnergy;

                                    int baseRate = PedestalConfig.COMMON.upgrade_import_baseEnergyTransferSpeed.get();
                                    int maxRate = baseRate + getEnergyCapacityIncrease(pedestal.getCoinOnPedestal());
                                    int transferRate = (getSpaceForEnergy >= maxRate)?(maxRate):(getSpaceForEnergy);
                                    if (getEnergyInPedestal < transferRate) {transferRate = getEnergyInPedestal;}

                                    //Because things have a max alled insert i guess
                                    int chargeItemLimit = energyHandler.receiveEnergy(transferRate,true);

                                    if(getSpaceForEnergy<=0)
                                    {
                                        if((pedestal.addItemStack(itemFromInv,true)).isEmpty())
                                        {
                                            pedestal.addItemStack(itemFromInv, false);
                                            handler.extractItem(i,1 ,false);
                                        }
                                    }
                                    else if(energyHandler.receiveEnergy(chargeItemLimit,true) > 0)
                                    {
                                        int removedFromPedestal = pedestal.removeEnergy(chargeItemLimit,false);
                                        energyHandler.receiveEnergy(removedFromPedestal,false);
                                    }
                                }
                                else
                                {
                                    if((pedestal.addItemStack(itemFromInv,true)).isEmpty())
                                    {
                                        pedestal.addItemStack(itemFromInv, false);
                                        handler.extractItem(i,1 ,false);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
    }
}
