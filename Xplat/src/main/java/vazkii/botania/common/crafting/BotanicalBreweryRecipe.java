/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewContainer;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BotanicalBreweryRecipe implements vazkii.botania.api.recipe.BotanicalBreweryRecipe {
	private final ResourceLocation id;
	private final Brew brew;
	private final NonNullList<Ingredient> inputs;

	public BotanicalBreweryRecipe(ResourceLocation id, Brew brew, Ingredient... inputs) {
		this.id = id;
		this.brew = brew;
		this.inputs = NonNullList.of(Ingredient.EMPTY, inputs);
	}

	@Override
	public boolean matches(Container inv, @NotNull Level world) {
		List<Ingredient> inputsMissing = new ArrayList<>(inputs);

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				break;
			}

			if (stack.getItem() instanceof BrewContainer) {
				continue;
			}

			boolean matchedOne = false;

			Iterator<Ingredient> iter = inputsMissing.iterator();
			while (iter.hasNext()) {
				Ingredient input = iter.next();
				if (input.test(stack)) {
					iter.remove();
					matchedOne = true;
					break;
				}
			}

			if (!matchedOne) {
				return false;
			}
		}

		return inputsMissing.isEmpty();
	}

	@NotNull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		return inputs;
	}

	@NotNull
	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.brewery);
	}

	@NotNull
	@Override
	public ResourceLocation getId() {
		return id;
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return BotaniaRecipeTypes.BREW_SERIALIZER;
	}

	@Override
	public Brew getBrew() {
		return brew;
	}

	@Override
	public int getManaUsage() {
		return brew.getManaCost();
	}

	@Override
	public ItemStack getOutput(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof BrewContainer container)) {
			return new ItemStack(Items.GLASS_BOTTLE); // Fallback...
		}

		return container.getItemForBrew(brew, stack);
	}

	@Override
	public int hashCode() {
		return 31 * brew.hashCode() ^ inputs.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof BotanicalBreweryRecipe brewRecipe
				&& brew == brewRecipe.brew
				&& inputs.equals(brewRecipe.inputs);
	}

	public static class Serializer extends RecipeSerializerBase<BotanicalBreweryRecipe> {
		@NotNull
		@Override
		public BotanicalBreweryRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
			String brewStr = GsonHelper.getAsString(json, "brew");
			ResourceLocation brewId = ResourceLocation.tryParse(brewStr);
			Brew brew = BotaniaAPI.instance().getBrewRegistry().getOptional(brewId).orElseThrow(() -> new JsonParseException("Unknown brew " + brewStr));

			JsonArray ingrs = GsonHelper.getAsJsonArray(json, "ingredients");
			List<Ingredient> inputs = new ArrayList<>();
			for (JsonElement e : ingrs) {
				inputs.add(Ingredient.fromJson(e));
			}
			return new BotanicalBreweryRecipe(id, brew, inputs.toArray(new Ingredient[0]));
		}

		@Override
		public BotanicalBreweryRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
			var brewId = buf.readResourceLocation();
			Brew brew = BotaniaAPI.instance().getBrewRegistry().get(brewId);
			Ingredient[] inputs = new Ingredient[buf.readVarInt()];
			for (int i = 0; i < inputs.length; i++) {
				inputs[i] = Ingredient.fromNetwork(buf);
			}
			return new BotanicalBreweryRecipe(id, brew, inputs);
		}

		@Override
		public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull BotanicalBreweryRecipe recipe) {
			var brewId = BotaniaAPI.instance().getBrewRegistry().getKey(recipe.getBrew());
			buf.writeResourceLocation(brewId);
			buf.writeVarInt(recipe.getIngredients().size());
			for (Ingredient input : recipe.getIngredients()) {
				input.toNetwork(buf);
			}
		}
	}
}
