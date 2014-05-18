package me.sabbertran.wastelandofficers.bounty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.sabbertran.wastelandofficers.WastelandOfficers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Bounty implements Listener
{

    private WastelandOfficers main;
    private ArrayList<Block> headBlocks;
    private Location rewardLocation;

    private ArrayList<String> awaitingName;
    private HashMap<String, String> awaitingCoins;
    private ArrayList<String> dropHead;

    public Bounty(WastelandOfficers wo, ArrayList<Block> headBlocks, Location rewardLocation)
    {
        this.main = wo;
        this.headBlocks = headBlocks;
        this.rewardLocation = rewardLocation;
        this.awaitingName = new ArrayList<String>();
        this.awaitingCoins = new HashMap<String, String>();
        this.dropHead = new ArrayList<String>();

        main.getCommand("bounty").setExecutor(new BountyCommand(main, this));
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent ev)
    {
        Player p = ev.getPlayer();
        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Block b = ev.getClickedBlock();
            if (b.getState() instanceof Sign)
            {
                Sign s = (Sign) b.getState();
                if (s.getLine(0).equals("§dBounty"))
                {
                    awaitingName.add(p.getName());
                    p.sendMessage("§bPlease enter the players name you want to see killed into the chat.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent ev)
    {
        Player p = ev.getPlayer();
        if (awaitingName.contains(p.getName()))
        {
            ev.setCancelled(true);
            if (!ev.getMessage().contains(" "))
            {
                awaitingCoins.put(p.getName(), ev.getMessage());
                p.sendMessage("§bPlease enter the amount of coins you want to spend on this job into the chat.");
                awaitingName.remove(p.getName());
            } else
            {
                awaitingName.remove(p.getName());
                p.sendMessage("§bNot a valid player name (Request canceled, try again)");
            }
        } else if (awaitingCoins.containsKey(p.getName()))
        {
            ev.setCancelled(true);
            try
            {
                int coins = Integer.parseInt(ev.getMessage());
                if (main.takeCoins(p.getName(), coins))
                {
                    main.getBounty().put(awaitingCoins.get(p.getName()), coins);
                    dropHead.add(awaitingCoins.get(p.getName()));
                    updateBountyWall();
                    p.sendMessage("§bSuccessfully set a bounty of " + coins + " Coins on " + awaitingCoins.get(p.getName()) + ".");
                    awaitingCoins.remove(p.getName());
                } else
                {
                    p.sendMessage("§bYou don't have enough coins in your inventory. (Request canceled, try again)");
                    awaitingCoins.remove(p.getName());
                }
            } catch (NumberFormatException ex)
            {
                p.sendMessage("§bYou have to enter the exact amount of coins without any characters. (Request canceled, try again)");
                awaitingCoins.remove(p.getName());
            }
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent ev)
    {
        if (ev.getEntity().getKiller() instanceof Player)
        {
            Player killer = ev.getEntity().getKiller();
            Player dead = ev.getEntity().getPlayer();

            if (dropHead.contains(dead.getName()))
            {
                ItemStack head = new ItemStack(Material.SKULL_ITEM);
                head.setDurability((short) 3);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwner(dead.getName());
                List<String> lore = new ArrayList<String>();
                lore.add("§6Bounty Head");
                lore.add("§6Claim your reward at the officer station");
                meta.setLore(lore);
                head.setItemMeta(meta);
                ev.getDrops().add(head);

                dropHead.remove(dead.getName());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev)
    {
        if (ev.getBlockPlaced().getLocation().equals(rewardLocation))
        {
            Player p = ev.getPlayer();
            ItemStack is = ev.getItemInHand();
            if (is.getType() == Material.SKULL_ITEM)
            {
                SkullMeta meta = (SkullMeta) is.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore != null && lore.contains("§6Bounty Head"))
                {
                    int reward = main.getBounty().get(meta.getOwner());
                    if (main.giveCoins(p.getName(), reward))
                    {
                        p.sendMessage("Successfully claimed your reward of " + reward + " Coins for killing " + meta.getOwner() + ".");
                        ev.getBlock().setType(Material.AIR);
                        main.getBounty().remove(meta.getOwner());
                        updateBountyWall();
                    } else
                    {
                        p.sendMessage("§6You don't have enough space in your inventory. Please clean up your inventory and try again.");
                        ev.setCancelled(true);
                    }
                } else
                {
                    p.sendMessage("Place the player head you got from killing a hunted player here.");
                    ev.setCancelled(true);
                }
            } else
            {
                p.sendMessage("Place the player head you got from killing a hunted player here.");
                ev.setCancelled(true);
            }
        }
    }

    public void updateBountyWall()
    {
        for (Block b : headBlocks)
        {
            b.setType(Material.AIR);
            Location sign_loc = b.getLocation();
            sign_loc.setY(sign_loc.getY() - 1);
            Block sign = sign_loc.getBlock();
            sign.setType(Material.AIR);
        }

        HashMap<String, Integer> wallMap = main.sortHashMapByValuesD(main.getBounty());
        int current = 0;
        for (Map.Entry<String, Integer> entry : wallMap.entrySet())
        {
            if (current <= headBlocks.size())
            {
                Block head = headBlocks.get(current);
                Location sign_loc = head.getLocation();
                sign_loc.setY(sign_loc.getY() - 1);
                Block sign = sign_loc.getBlock();

                head.setType(Material.SKULL);
                Skull skull = (Skull) head.getState();
                skull.setSkullType(SkullType.PLAYER);
                skull.setOwner(entry.getKey());
                skull.update();
                head.setData((byte) 2);

                sign.setType(Material.WALL_SIGN);
                sign.setData((byte) 2);

                Sign s = (Sign) sign.getState();
                s.setLine(0, entry.getKey());
                s.setLine(2, entry.getValue() + " C");
                s.update();

                current++;
            } else
            {
                break;
            }
        }
    }

    public ArrayList<String> getDropHead()
    {
        return dropHead;
    }

    public void setDropHead(ArrayList<String> drop)
    {
        this.dropHead = drop;
    }
}
