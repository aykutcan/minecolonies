package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the inventoryBoard item.
 */
public class ItemInventoryBoard extends AbstractItemMinecolonies
{
    /**
     * Tag of the colony.
     */
    private static final String TAG_COLONY = "colony";

    /**
     * Sets the name, creative tab, and registers the Inventory item.
     *
     * @param properties the properties.
     */
    public ItemInventoryBoard(final Properties properties)
    {
        super("inventoryboard", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    @NotNull
    public ActionResultType onItemUse(final ItemUseContext ctx)
    {
        final ItemStack inventoryBoard = ctx.getPlayer().getHeldItem(ctx.getHand());

        final CompoundNBT compound = checkForCompound(inventoryBoard);
        final TileEntity entity = ctx.getWorld().getTileEntity(ctx.getPos());

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.putInt(TAG_COLONY, ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            if (!ctx.getWorld().isRemote)
            {
                LanguageHandler.sendPlayerMessage(
                        ctx.getPlayer(),
                        TranslationConstants.COM_MINECOLONIES_INVENTORYBOARD_COLONY_SET,
                        ((AbstractTileEntityColonyBuilding) entity).getColony().getName());
            }
        }
        else if (ctx.getWorld().isRemote)
        {
            openWindow(compound, ctx.getWorld(), ctx.getPlayer());
        }

        return ActionResultType.SUCCESS;
    }

    /**
     * Handles mid air use.
     *
     * @param worldIn  the world
     * @param playerIn the player
     * @param hand     the hand
     * @return the result
     */
    @Override
    @NotNull
    public ActionResult<ItemStack> onItemRightClick(
            final World worldIn,
            final PlayerEntity playerIn,
            final Hand hand)
    {
        final ItemStack inventory = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote) {
            return new ActionResult<>(ActionResultType.SUCCESS, inventory);
        }

        openWindow(checkForCompound(inventory), worldIn, playerIn);

        return new ActionResult<>(ActionResultType.SUCCESS, inventory);
    }

    /**
     * Check for the compound and return it. If not available create and return it.
     *
     * @param inventoryBoard the inventory to check for.
     * @return the compound of the inventory.
     */
    private static CompoundNBT checkForCompound(final ItemStack inventoryBoard)
    {
        if (!inventoryBoard.hasTag()) inventoryBoard.setTag(new CompoundNBT());
        return inventoryBoard.getTag();
    }

    /**
     * Opens the inventoryboard window if there is a valid colony linked
     * @param compound the item compound
     * @param player the player entity opening the window
     */
    private static void openWindow(CompoundNBT compound, World world, PlayerEntity player)
    {
        if (compound.keySet().contains(TAG_COLONY))
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY), world.getDimensionKey());
            if (colonyView != null) MineColonies.proxy.openInventoryBoardWindow(colonyView,world);
        }
        else
        {
            player.sendStatusMessage(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_INVENTORYBOARD_NEED_COLONY), true);
        }
    }
}
