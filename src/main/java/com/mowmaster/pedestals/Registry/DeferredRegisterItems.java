package com.mowmaster.pedestals.Registry;

import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Augments.*;
import com.mowmaster.pedestals.Items.Filters.*;
import com.mowmaster.pedestals.Items.Tools.*;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeExport;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeImport;
//import com.mowmaster.pedestals.Items.Upgrades.Pedestal.Machines.ItemUpgradeCobbleGenerator;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.Machines.ItemUpgradeCobbleGenerator;
import com.mowmaster.pedestals.PedestalTab.PedestalsTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class DeferredRegisterItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


    public static final RegistryObject<Item> TOOL_LINKINGTOOL = ITEMS.register("tool_linkingtool",
            () -> new LinkingTool(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> TOOL_LINKINGTOOLBACKWARDS = ITEMS.register("tool_linkingtoolbackwards",
            () -> new LinkingToolBackwards(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> TOOL_UPGRADETOOL = ITEMS.register("tool_upgradetool",
            () -> new UpgradeTool(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> TOOL_FILTERTOOL = ITEMS.register("tool_filtertool",
            () -> new FilterTool(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> TOOL_TAGTOOL = ITEMS.register("tool_tagtool",
            () -> new TagTool(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> TOOL_TOOLSWAPPER = ITEMS.register("tool_toolswapper",
            () -> new ToolSwapper(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> TOOL_DEVTOOL = ITEMS.register("tool_devtool",
            () -> new DevTool(new Item.Properties().stacksTo(1).tab(PedestalsTab.TAB_ITEMS)));

    public static final RegistryObject<Item> FILTER_BASE = ITEMS.register("filter_base",
            () -> new BaseFilter(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_ITEM = ITEMS.register("filter_item",
            () -> new FilterItem(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_ITEMSTACK = ITEMS.register("filter_itemstack",
            () -> new FilterItemStack(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_DURABILITY = ITEMS.register("filter_durability",
            () -> new FilterDurability(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_ENCHANTED = ITEMS.register("filter_enchanted",
            () -> new FilterEnchanted(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_ENCHANTED_COUNT = ITEMS.register("filter_enchantedcount",
            () -> new FilterEnchantCount(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_ENCHANTED_EXACT = ITEMS.register("filter_enchantedexact",
            () -> new FilterEnchantedExact(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_ENCHANTED_FUZZY = ITEMS.register("filter_enchantedfuzzy",
            () -> new FilterEnchantedFuzzy(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_FOOD = ITEMS.register("filter_food",
            () -> new FilterFood(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_MOD = ITEMS.register("filter_mod",
            () -> new FilterMod(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_RESTRICTED = ITEMS.register("filter_restricted",
            () -> new FilterRestricted(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> FILTER_TAG = ITEMS.register("filter_tag",
            () -> new FilterTag(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_BASE = ITEMS.register("upgrade_pedestal_base",
            () -> new ItemUpgradeBase(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_IMPORT = ITEMS.register("upgrade_pedestal_import",
            () -> new ItemUpgradeImport(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_EXPORT = ITEMS.register("upgrade_pedestal_export",
            () -> new ItemUpgradeExport(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_COBBLEGEN = ITEMS.register("upgrade_pedestal_cobblegen",
            () -> new ItemUpgradeCobbleGenerator(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));

    public static final RegistryObject<Item> AUGMENT_PEDESTAL_ROUNDROBIN = ITEMS.register("augment_pedestal_roundrobin",
            () -> new AugmentBase(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_RENDERDIFFUSER = ITEMS.register("augment_pedestal_renderdiffuser",
            () -> new AugmentRenderDiffuser(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_NOCOLLIDE = ITEMS.register("augment_pedestal_nocollide",
            () -> new AugmentBase(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));



    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_CAPACITY = ITEMS.register("augment_pedestal_t1_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_CAPACITY = ITEMS.register("augment_pedestal_t2_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_CAPACITY = ITEMS.register("augment_pedestal_t3_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_CAPACITY = ITEMS.register("augment_pedestal_t4_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));

    //Increases storage, stacks, and max amount for fluids, energy, and xp
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_STORAGE = ITEMS.register("augment_pedestal_t1_storage",
            () -> new AugmentTieredStorage(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_STORAGE = ITEMS.register("augment_pedestal_t2_storage",
            () -> new AugmentTieredStorage(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_STORAGE = ITEMS.register("augment_pedestal_t3_storage",
            () -> new AugmentTieredStorage(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_STORAGE = ITEMS.register("augment_pedestal_t4_storage",
            () -> new AugmentTieredStorage(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));


    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_SPEED = ITEMS.register("augment_pedestal_t1_speed",
            () -> new AugmentTieredSpeed(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_SPEED = ITEMS.register("augment_pedestal_t2_speed",
            () -> new AugmentTieredSpeed(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_SPEED = ITEMS.register("augment_pedestal_t3_speed",
            () -> new AugmentTieredSpeed(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_SPEED = ITEMS.register("augment_pedestal_t4_speed",
            () -> new AugmentTieredSpeed(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));

    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_RANGE = ITEMS.register("augment_pedestal_t1_range",
            () -> new AugmentTieredRange(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_RANGE = ITEMS.register("augment_pedestal_t2_range",
            () -> new AugmentTieredRange(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_RANGE = ITEMS.register("augment_pedestal_t3_range",
            () -> new AugmentTieredRange(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_RANGE = ITEMS.register("augment_pedestal_t4_range",
            () -> new AugmentTieredRange(new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}