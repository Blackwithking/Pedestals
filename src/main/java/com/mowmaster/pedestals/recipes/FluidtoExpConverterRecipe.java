package com.mowmaster.pedestals.recipes;

import com.google.gson.JsonObject;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class FluidtoExpConverterRecipe implements IRecipe<IInventory> {

    public static final IRecipeType<FluidtoExpConverterRecipe> recipeType = IRecipeType.register(Reference.MODID + ":fluid_to_xp");
    public static final Serializer serializer = new Serializer();

    private final ResourceLocation recipeId;
    private Ingredient ingredient;
    private ItemStack result;

    public FluidtoExpConverterRecipe(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        ItemStack stack = inv.getStackInSlot(0);
        return ingredient.test(stack);
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public IRecipeType<?> getType() {
        return recipeType;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FluidtoExpConverterRecipe> {

        public static ArrayList<Item> ingredientList = new ArrayList<>();

        @Override
        public FluidtoExpConverterRecipe read(ResourceLocation recipeId, JsonObject json) {

            FluidtoExpConverterRecipe recipe = new FluidtoExpConverterRecipe(recipeId);

            recipe.ingredient = Ingredient.deserialize(json.get("ingredient"));

            for (ItemStack stack : recipe.ingredient.getMatchingStacks()) {
                if (!ingredientList.contains(stack.getItem())) ingredientList.add(stack.getItem());
            }

            ResourceLocation itemResourceLocation = ResourceLocation.create(JSONUtils.getString(json.get("result").getAsJsonObject(), "item", "minecraft:empty"), ':');
            int itemAmount = JSONUtils.getInt(json.get("result").getAsJsonObject(), "count", 0);
            recipe.result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemResourceLocation), itemAmount);

            return recipe;
        }

        @Nullable
        @Override
        public FluidtoExpConverterRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            FluidtoExpConverterRecipe recipe = new FluidtoExpConverterRecipe(recipeId);
            recipe.ingredient = Ingredient.read(buffer);
            recipe.result = buffer.readItemStack();
            return recipe;
        }

        @Override
        public void write(PacketBuffer buffer, FluidtoExpConverterRecipe recipe) {
            recipe.ingredient.write(buffer);
            buffer.writeItemStack(recipe.getResult());
        }
    }
}