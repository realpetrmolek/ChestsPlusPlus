package com.jamesdpeters.minecraft.chests.storage;

import com.jamesdpeters.minecraft.chests.ChestsPlusPlus;
import com.jamesdpeters.minecraft.chests.interfaces.VirtualInventoryHolder;
import com.jamesdpeters.minecraft.chests.inventories.ChestLinkMenu;
import com.jamesdpeters.minecraft.chests.misc.Messages;
import com.jamesdpeters.minecraft.chests.misc.Utils;
import com.jamesdpeters.minecraft.chests.runnables.VirtualChestToHopper;
import com.jamesdpeters.minecraft.chests.sort.InventorySorter;
import com.jamesdpeters.minecraft.chests.sort.SortMethod;
import fr.minuskube.inv.ClickableItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestLinkStorage extends AbstractStorage implements ConfigurationSerializable {

    private String inventoryName = "Chest";
    private VirtualChestToHopper chestToHopper;
    private SortMethod sortMethod;

    public ChestLinkStorage(OfflinePlayer player, String group, Location location, StorageType<ChestLinkStorage> storageType){
        super(player, group, location, storageType);
        this.inventoryName = group;
        this.sortMethod = SortMethod.OFF;

        Block block = location.getBlock();
        if(block.getState() instanceof Chest){
            Chest chest = (Chest) block.getState();
            getInventory().setContents(chest.getInventory().getContents());
            chest.getInventory().clear();
        }

        init();
    }

    @Override
    void serialize(Map<String, Object> hashMap) {
        hashMap.put("inventoryName",inventoryName);
        hashMap.put("sortMethod", sortMethod.toString());
    }

    @Override
    void deserialize(Map<String, Object> map) {
        String tempName = (String) map.get("inventoryName");
        if(tempName != null) inventoryName = tempName;

        if(map.containsKey("sortMethod")) sortMethod = Enum.valueOf(SortMethod.class, (String) map.get("sortMethod"));
        else sortMethod = SortMethod.OFF;

        init();
    }

    @Override
    ItemStack getArmorStandItem() {
        return InventorySorter.getMostCommonItem(getInventory());
    }

    private void init(){
        chestToHopper = new VirtualChestToHopper(this);
        chestToHopper.start();
    }

    @Override
    boolean storeInventory() {
        return true;
    }

    @Override
    protected Inventory initInventory(){
        return Bukkit.createInventory(new VirtualInventoryHolder(this), 54,inventoryName);
    }

    @Override
    protected void setIdentifier(String newName) {
        inventoryName = newName;
    }

    @Override
    void onStorageAdded(Block block, Player player) {
        //Migrates that chest into InventoryStorage and if full drops it at the chest location.
        if(block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            boolean hasOverflow = false;
            for (ItemStack chestItem : chest.getInventory().getContents()) {
                if (chestItem != null) {
                    HashMap<Integer, ItemStack> overflow = getInventory().addItem(chestItem);
                    for (ItemStack item : overflow.values())
                        if (item != null) {
                            player.getWorld().dropItemNaturally(block.getLocation(), item);
                            hasOverflow = true;
                        }
                }
            }
            if (hasOverflow) Messages.CHEST_HAD_OVERFLOW(player);
            chest.getInventory().clear();
        }
    }

    public ItemStack getIventoryIcon(Player player){
        ItemStack mostCommon = InventorySorter.getMostCommonItem(getInventory());
        ItemStack toReturn;
        if(mostCommon == null) toReturn = new ItemStack(Material.CHEST);
        else toReturn = mostCommon.clone();

        ItemMeta meta = toReturn.getItemMeta();
        if(meta != null) {
            String dispName = ChatColor.GREEN + "" + getIdentifier() + ": " +ChatColor.WHITE+ ""+getTotalItems()+" items";
            if(player.getUniqueId().equals(playerUUID)) meta.setDisplayName(dispName);
            else meta.setDisplayName(getOwner().getName()+": "+dispName);

            if(getMembers() != null) {
                List<String> memberNames = new ArrayList<>();
                if(isPublic) memberNames.add(ChatColor.WHITE+"Public Chest");
                memberNames.add(ChatColor.BOLD+""+ChatColor.UNDERLINE+"Members:");
                getMembers().forEach(player1 -> memberNames.add(ChatColor.stripColor(player1.getName())));
                meta.setLore(memberNames);
            }
            toReturn.setItemMeta(meta);
        }
        toReturn.setAmount(1);
        return toReturn;
    }

    public ClickableItem getClickableItem(Player player) {
        return ClickableItem.from(getIventoryIcon(player), event -> {
            InventoryHolder inventoryHolder = getInventory().getHolder();
            if(inventoryHolder instanceof VirtualInventoryHolder){
                ((VirtualInventoryHolder) inventoryHolder).setPreviousInventory(() -> {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(ChestsPlusPlus.PLUGIN, () -> ChestLinkMenu.getMenu(player).open(player), 1);
                });
            }
            Utils.openChestInventory(player,getInventory());
        });
    }

    public int getTotalItems(){
        int total = 0;
        if(getInventory() != null) {
            for(ItemStack itemStack : getInventory().getContents()){
                if(itemStack != null) total += itemStack.getAmount();
            }
        }
        return total;
    }

    public void setSortMethod(SortMethod sortMethod){
        this.sortMethod = sortMethod;
    }

    public SortMethod getSortMethod(){
        return sortMethod;
    }

    public void sort(){
        InventorySorter.sort(getInventory(), sortMethod);
    }

    @Override
    public String getIdentifier() {
        return inventoryName;
    }

    @Override
    public String toString() {
        return inventoryName+": "+getLocations().toString();
    }
}