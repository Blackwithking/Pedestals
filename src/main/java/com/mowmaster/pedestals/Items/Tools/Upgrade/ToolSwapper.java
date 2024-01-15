package com.mowmaster.pedestals.Items.Tools.Upgrade;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ToolSwapper extends BaseTool implements IPedestalTool {
    public ToolSwapper(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_TOOLSWAPPER.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_FILTERTOOL.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        if (pedestal.hasTool()) {
            ItemStack toolInPedestal = pedestal.getActualToolStack();
            MowLibMessageUtils.messagePlayerChat(player, ChatFormatting.LIGHT_PURPLE, MODID + ".tool_toolinpedestal");
            MowLibMessageUtils.messagePlayerChatText(player, ChatFormatting.WHITE, toolInPedestal.getDisplayName().getString());
            MowLibMessageUtils.messagePlayerChat(player, ChatFormatting.LIGHT_PURPLE, MODID + ".tool_toolinpedestal_durability");
            MowLibMessageUtils.messagePlayerChatText(player, ChatFormatting.WHITE, ""+ pedestal.getDurabilityRemainingOnInsertedTool() + '/' +pedestal.getToolMaxDurability());
        } else {
            MowLibMessageUtils.messagePlayerChat(player, ChatFormatting.LIGHT_PURPLE, MODID + ".tool_toolinpedestal_not");
        }
    }
}
