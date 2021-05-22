package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Inventory Board window.
 */
public class WindowInventoryBoard extends AbstractWindowSkeleton {
    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowinventoryboard.xml";
    /**
     * InventoryBoard Window list id.
     */
    private static final String WINDOW_ID_LIST_INVENTORY = "inventoryList";

    /**
     * InventoryBoard Window item icon id.
     */
    private static final String WINDOW_ID_ITEM_ICON = "itemIcon";
    /**
     * InventoryBoard Window item name id.
     */

    private static final String WINDOW_ID_ITEM_NAME = "itemName";

    /**
     * InventoryBoard Window item count id.
     */
    private static final String WINDOW_ID_ITEM_COUNT = "itemCount";

    /**
     * Scrollinglist of the inventoryBoard window.
     */
    private ScrollingList inventoryList;

    /**
     * The colony id.
     */
    private final IColonyView colony;
    private final IWorld world;

    /**
     * Hide or show not important requests.
     */
    private boolean hide = false;

    /**
     * Constructor of the Inventory Board GUI.
     *
     * @param colony the colony to check the requests for.
     * @param world  the world.
     */
    public WindowInventoryBoard(final IColonyView colony, IWorld world) {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);
        this.colony = colony;
        this.world = world;

    }

    /**
     * Called when the window is opened. Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened() {
        fillList();
    }

    private void fillList() {


        //Find List in XML GUI
        inventoryList = findPaneOfTypeByID(WINDOW_ID_LIST_INVENTORY, ScrollingList.class);

        //Get all inventory Data
        HashMap<String, Tuple<Item, Integer>> inventoryData = getAllInventory();

        //Extract Values to access with iteration index
        List<Tuple<Item, Integer>> itemsAndCounts = inventoryData.values().stream().collect(Collectors.toList());

        //List Renderer
        inventoryList.setDataProvider(() -> itemsAndCounts.size(), (index, rowPane) -> {

            //I am sure this is stopper.
            if (index < 0 || index >= itemsAndCounts.size()) {
                return;
            }

            //Get Item Tuple at index
            final Tuple<Item, Integer> currentItem = itemsAndCounts.get(index);

            //Assign Icon & Text & Count
            final ItemIcon displayIcon = rowPane.findPaneOfTypeByID(WINDOW_ID_ITEM_ICON, ItemIcon.class);
            final ItemStack displayStack = new ItemStack(currentItem.getA(), 1);


            if (!displayStack.isEmpty()) {
                if (displayStack != null) {
                    displayIcon.setItem(displayStack);
                }
            }

            rowPane.findPaneOfTypeByID(WINDOW_ID_ITEM_NAME, Text.class)
                    .setText(currentItem.getA().getName());

            rowPane.findPaneOfTypeByID(WINDOW_ID_ITEM_COUNT, Text.class)
                    .setText(currentItem.getB().toString());


        });
    }

    /**
     * Inventory of all warehouses with their total counts.
     *
     * @return the hashmap of inventory items.
     */
    public HashMap<String, Tuple<Item, Integer>> getAllInventory() {

        //Result HashMap
        final HashMap<String, Tuple<Item, Integer>> inventories = new HashMap<String, Tuple<Item, Integer>>();

        //Return Empty
        if (this.colony == null) {
            return new HashMap<String, Tuple<Item, Integer>>();
        }

        //Return Empty
        if (!this.colony.hasWarehouse()) {
            return new HashMap<String, Tuple<Item, Integer>>();
        }

        //Get All warehouses in items registered colony.
        List<IBuildingView> warehouses = this.colony.getBuildings().stream().filter(b -> b instanceof BuildingWareHouse.View).collect(Collectors.toList());

        //Each Warehouse
        for (final IBuildingView warehouse : warehouses) {

            //Each Container
            for (@NotNull final BlockPos pos : warehouse.getContainerList()) {
                if (WorldUtil.isBlockLoaded(this.world, pos)) {
                    final TileEntity entity = this.world.getTileEntity(pos);

                    //Get Rack's content
                    if (entity instanceof TileEntityRack && !((AbstractTileEntityRack) entity).isEmpty()) {
                        final TileEntityRack rack = (TileEntityRack) entity;
                        for (final ItemStack stack : (InventoryUtils.getItemHandlerAsList(rack.getInventory()))) {
                            String key = stack.getItem().getName().getString();
                            Tuple<Item, Integer> item = inventories.get(key);

                            if (item != null) {
                                inventories.put(key, new Tuple(item.getA(), item.getB() + stack.getCount()));
                            } else {
                                inventories.put(key, new Tuple(stack.getItem(), stack.getCount()));
                            }


                        }
                    }

                    //Get Chests's content
                    if (entity instanceof ChestTileEntity && !((ChestTileEntity) entity).isEmpty()) {
                        for (final ItemStack stack : InventoryUtils.getItemHandlerAsList(entity.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null))) {
                            String key = stack.getItem().getName().getString();
                            Tuple<Item, Integer> item = inventories.get(key);

                            if (item != null) {
                                inventories.put(key, new Tuple(item.getA(), item.getB() + stack.getCount()));
                            } else {
                                inventories.put(key, new Tuple(stack.getItem(), stack.getCount()));
                            }

                        }
                    }
                }
            }
        }


        return inventories;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button) {
        switch (button.getID()) {
            default:
                super.onButtonClicked(button);
                break;
        }
    }


}
