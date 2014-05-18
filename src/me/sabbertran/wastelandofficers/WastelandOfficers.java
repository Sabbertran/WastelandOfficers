package me.sabbertran.wastelandofficers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.sabbertran.wastelandofficers.bounty.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class WastelandOfficers extends JavaPlugin
{

    private final Logger log = Bukkit.getLogger();
    private ArrayList<String> officers;
    private ArrayList<String> chief_officers;
    private HashMap<String, Boolean> inCommission;
    private HashMap<String, Location> outposts;
    private HashMap<String, Integer> mostwanted;
    private ArrayList<Block> mostWantedHeadBlocks;
    private HashMap<String, Integer> bountys;

    private File mostwanted_file;
    private File bounty_file;
    private File drophead_file;
    private Bounty bounty;
    private ArrayList<Block> headBlocks;
    private Location rewardLocation;

    public WastelandOfficers()
    {
        this.officers = new ArrayList<String>();
        this.chief_officers = new ArrayList<String>();
        this.inCommission = new HashMap<String, Boolean>();
        this.outposts = new HashMap<String, Location>();
        this.mostwanted = new HashMap<String, Integer>();
        this.mostWantedHeadBlocks = new ArrayList<Block>();
        this.bountys = new HashMap<String, Integer>();

        this.mostwanted_file = new File("plugins/WastelandOfficers", "mostwanted.yml");
        this.bounty_file = new File("plugins/WastelandOfficers", "bountys.yml");
        this.drophead_file = new File("plugins/WastelandOfficers", "drophead.yml");
        this.headBlocks = new ArrayList<Block>();
    }

    @Override
    public void onDisable()
    {
        String[] officer_temp = new String[officers.size()];
        for (int i = 0; i < officers.size(); i++)
        {
            String s = officers.get(i);
            officer_temp[i] = s + ":" + inCommission.get(s);
        }
        String[] chief_temp = new String[chief_officers.size()];
        for (int i = 0; i < chief_officers.size(); i++)
        {
            String s1 = chief_officers.get(i);
            chief_temp[i] = s1 + ":" + inCommission.get(s1);
        }
        getConfig().set("WastelandOfficers.Officers", officer_temp);
        getConfig().set("WastelandOfficers.ChiefOfficers", chief_temp);
        ArrayList<String> outposts_temp = new ArrayList<String>();
        for (Map.Entry<String, Location> entry : outposts.entrySet())
        {
            String name = entry.getKey();
            Location l = entry.getValue();
            String s_temp = name + ": " + l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ();
            outposts_temp.add(s_temp);
        }
        getConfig().set("WastelandOfficers.Outposts", outposts_temp.toArray());
        ArrayList<String> headBlocks_temp = new ArrayList<String>();
        for (Block b : headBlocks)
        {
            Location loc = b.getLocation();
            String b_temp = loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
            headBlocks_temp.add(b_temp);
        }
        getConfig().set("WastelandOfficers.Bounty.HeadBlocks", headBlocks_temp.toArray());
        ArrayList<String> mostWantedHeadBlocks_temp = new ArrayList<String>();
        for (Block b1 : mostWantedHeadBlocks)
        {
            Location loc1 = b1.getLocation();
            String b1_temp = loc1.getWorld().getName() + ", " + loc1.getBlockX() + ", " + loc1.getBlockY() + ", " + loc1.getBlockZ();
            mostWantedHeadBlocks_temp.add(b1_temp);
        }
        getConfig().set("WastelandOfficers.MostWantedHeadBlocks", mostWantedHeadBlocks_temp.toArray());
        String reward_temp = rewardLocation.getWorld().getName() + ", " + rewardLocation.getBlockX() + ", " + rewardLocation.getBlockY() + ", " + rewardLocation.getBlockZ();
        getConfig().set("WastelandOfficers.Bounty.RewardLocation", reward_temp);
        saveConfig();

        YamlConfiguration c_mostwanted = new YamlConfiguration();
        for (Map.Entry<String, Integer> entry : mostwanted.entrySet())
        {
            c_mostwanted.set(entry.getKey(), entry.getValue());
        }
        try
        {
            c_mostwanted.save(mostwanted_file);
        } catch (IOException ex)
        {
            Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
        }

        YamlConfiguration c_bounty = new YamlConfiguration();
        for (Map.Entry<String, Integer> entry : bountys.entrySet())
        {
            c_bounty.set(entry.getKey(), entry.getValue());
        }
        try
        {
            c_bounty.save(bounty_file);
        } catch (IOException ex)
        {
            Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
        }

        YamlConfiguration c_drophead = new YamlConfiguration();
        for (String s : bounty.getDropHead())
        {
            c_drophead.set(s, "");
        }
        try
        {
            c_drophead.save(drophead_file);
        } catch (IOException ex)
        {
            Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
        }

        log.info("WastelandOfficers disabled");
    }

    @Override
    public void onEnable()
    {
        getConfig().addDefault("WastelandOfficers.Officers", new String[]
        {
            "Player1:false", "Player2:false", "Player3:false"
        });
        getConfig().addDefault("WastelandOfficers.ChiefOfficers", new String[]
        {
            "Player1:false", "Player2:false", "Player3:false"
        });
        getConfig().addDefault("WastelandOfficers.Outposts", new String[]
        {
            "Name1: world, 0, 64, 0", "Name2: world, 0, 64, 0"
        });
        getConfig().addDefault("WastelandOfficers.Bounty.HeadBlocks", new String[]
        {
            "world, 0, 64, 0", "world, 0, 64, 0", "world, 0, 64, 0"
        });
        getConfig().addDefault("WastelandOfficers.Bounty.RewardLocation", "world, 0, 64, 0");
        getConfig().addDefault("WastelandOfficers.MostWantedHeadBlocks", new String[]
        {
            "world, 0, 64, 0", "world, 0, 64, 0", "world, 0, 64, 0"
        });
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (mostwanted_file.exists())
        {
            YamlConfiguration c_mostwanted = YamlConfiguration.loadConfiguration(mostwanted_file);
            for (String s : c_mostwanted.getConfigurationSection("").getKeys(false))
            {
                mostwanted.put(s, Integer.parseInt((String) c_mostwanted.get(s)));
            }
        } else
        {
            try
            {
                mostwanted_file.createNewFile();
            } catch (IOException ex)
            {
                Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (bounty_file.exists())
        {
            YamlConfiguration c_bounty = YamlConfiguration.loadConfiguration(bounty_file);
            for (String s : c_bounty.getConfigurationSection("").getKeys(false))
            {
                bountys.put(s, (Integer) c_bounty.get(s));
            }
        } else
        {
            try
            {
                bounty_file.createNewFile();
            } catch (IOException ex)
            {
                Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ArrayList<String> off_temp = (ArrayList<String>) this.getConfig().getStringList("WastelandOfficers.Officers");
        for (String s : off_temp)
        {
            String[] s_split = s.split(":");
            boolean b = Boolean.parseBoolean(s_split[1]);
            officers.add(s_split[0]);
            inCommission.put(s_split[0], b);
        }
        ArrayList<String> chief_temp = (ArrayList<String>) this.getConfig().getStringList("WastelandOfficers.ChiefOfficers");
        for (String s1 : chief_temp)
        {
            String[] s1_split = s1.split(":");
            boolean b1 = Boolean.parseBoolean(s1_split[1]);
            chief_officers.add(s1_split[0]);
            inCommission.put(s1_split[0], b1);
        }

        ArrayList<String> outposts_temp = (ArrayList<String>) this.getConfig().getStringList("WastelandOfficers.Outposts");
        for (String s : outposts_temp)
        {
            String[] split1 = s.split(": ");
            String[] split2 = split1[1].split(", ");
            Location l = new Location(Bukkit.getWorld(split2[0]), Integer.parseInt(split2[1]), Integer.parseInt(split2[2]), Integer.parseInt(split2[3]));
            outposts.put(split1[0], l);
        }
        ArrayList<String> headBlocks_temp = (ArrayList<String>) this.getConfig().getStringList("WastelandOfficers.Bounty.HeadBlocks");
        for (String s : headBlocks_temp)
        {
            String[] split = s.split(", ");
            Location l = new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            Block b = l.getBlock();
            headBlocks.add(b);

        }
        ArrayList<String> mostWantedHeadBlocks_temp = (ArrayList<String>) this.getConfig().getStringList("WastelandOfficers.MostWantedHeadBlocks");
        for (String s1 : mostWantedHeadBlocks_temp)
        {
            String[] split1 = s1.split(", ");
            Location l1 = new Location(Bukkit.getWorld(split1[0]), Integer.parseInt(split1[1]), Integer.parseInt(split1[2]), Integer.parseInt(split1[3]));
            Block b1 = l1.getBlock();
            mostWantedHeadBlocks.add(l1.getBlock());
        }

        String rewardLoc = getConfig().getString("WastelandOfficers.Bounty.RewardLocation");
        String[] rewardLoc_split = rewardLoc.split(", ");
        rewardLocation = new Location(Bukkit.getWorld(rewardLoc_split[0]), Integer.parseInt(rewardLoc_split[1]), Integer.parseInt(rewardLoc_split[2]), Integer.parseInt(rewardLoc_split[3]));

        bounty = new Bounty(this, headBlocks, rewardLocation);

        if (drophead_file.exists())
        {
            YamlConfiguration c_drophead = YamlConfiguration.loadConfiguration(drophead_file);
            ArrayList<String> temp = new ArrayList<String>();
            for (String s : c_drophead.getConfigurationSection("").getKeys(false))
            {
                temp.add(s);
            }
            bounty.setDropHead(temp);
        } else
        {
            try
            {
                drophead_file.createNewFile();
            } catch (IOException ex)
            {
                Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        getServer().getPluginManager().registerEvents(bounty, this);
        getCommand("officer").setExecutor(new OfficerCommand(this));

        log.info("WastelandOfficers enabled");
    }

    public void changeInventory(String player)
    {
        Player p = Bukkit.getPlayer(player);
        if (p != null)
        {
            ItemStack[] contents = new ItemStack[36];
            ItemStack[] armor_contents = new ItemStack[4];
            for (int i = 0; i < p.getInventory().getContents().length; i++)
            {
                contents[i] = p.getInventory().getContents()[i];
            }
            for (int j = 0; j < p.getInventory().getArmorContents().length; j++)
            {
                armor_contents[j] = p.getInventory().getArmorContents()[j];
            }
            double temp_xp = (double) p.getExp();
            double temp_health = p.getHealth();
            double temp_exhaustion = (double) p.getExhaustion();

            //load new inventory
            File f = new File("plugins/WastelandOfficers/inventorys", p.getName() + ".yml");
            if (f.exists())
            {
                YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
                ItemStack[] new_armor_content = c.getList("inventory.armor").toArray(new ItemStack[0]);
                ItemStack[] new_content = c.getList("inventory.content").toArray(new ItemStack[0]);
                double xp = c.getDouble("inventory.xp");
                double health = c.getDouble("inventory.health");
                double exhaustion = c.getDouble("inventory.exhaustion");

                p.getInventory().setContents(new_content);
                p.getInventory().setArmorContents(new_armor_content);
                p.setExp((float) xp);
                p.setHealth(health);
                p.setExhaustion((float) exhaustion);
            } else
            {
                p.getInventory().setArmorContents(new ItemStack[4]);
                p.getInventory().clear();
                p.setExp(0F);
                p.setHealth(p.getMaxHealth());
            }

            //save old inventory
            YamlConfiguration c = new YamlConfiguration();
            c.set("inventory.content", contents);
            c.set("inventory.armor", armor_contents);
            c.set("inventory.xp", temp_xp);
            c.set("inventory.health", temp_health);
            c.set("inventory.exhaustion", temp_exhaustion);
            try
            {
                c.save(new File("plugins/WastelandOfficers/inventorys", p.getName() + ".yml"));

            } catch (IOException ex)
            {
                Logger.getLogger(WastelandOfficers.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
        {
            log.info("[WastelandOfficers] Error while changing " + player + "'s inventory. Player is not online.");
        }
    }

    public boolean giveCoins(String player, int coins)
    {
        Player p = getServer().getPlayer(player);
        if (p != null)
        {
            int reward = coins;
            int ingots = reward / 9;
            int blocks = ingots / 9;
            int nuggets = reward - blocks * 81 - ingots * 9;
            int freeslots = 0;
            for (ItemStack is : p.getInventory().getContents())
            {
                if (is == null)
                {
                    freeslots++;
                }
                if (freeslots == 3)
                {
                    break;
                }
            }
            if (freeslots == 3 || (freeslots == 2 && (blocks == 0 || ingots == 0 || nuggets == 0)) || (freeslots == 1 && ((ingots == 0 && blocks == 0) || (ingots == 0 && nuggets == 0) || (blocks == 0 || nuggets == 0))))
            {
                if (blocks != 0)
                {
                    p.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK, blocks));
                }
                if (ingots != 0)
                {
                    p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, ingots));
                }
                if (nuggets != 0)
                {
                    p.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, nuggets));
                }
                p.updateInventory();
                return true;
            }
        }
        return false;
    }

    public boolean takeCoins(String player, int coins)
    {
        Player p = getServer().getPlayer(player);
        if (p != null)
        {
            int invCoins = 0;

            for (ItemStack is : p.getInventory().getContents())
            {
                if (is != null)
                {
                    if (is.getType() == Material.GOLD_BLOCK)
                    {
                        invCoins = invCoins + is.getAmount() * 81;
                    } else if (is.getType() == Material.GOLD_INGOT)
                    {
                        invCoins = invCoins + is.getAmount() * 9;
                    } else if (is.getType() == Material.GOLD_NUGGET)
                    {
                        invCoins = invCoins + is.getAmount();
                    }
                }
            }

            if (invCoins >= coins)
            {
                for (ItemStack is : p.getInventory().getContents())
                {
                    if (is != null)
                    {
                        if (is.getType() == Material.GOLD_BLOCK)
                        {
                            p.getInventory().remove(is);
                        } else if (is.getType() == Material.GOLD_INGOT)
                        {
                            p.getInventory().remove(is);
                        } else if (is.getType() == Material.GOLD_NUGGET)
                        {
                            p.getInventory().remove(is);
                        }
                    }
                }

                giveCoins(player, invCoins - coins);
                return true;
            }
        }
        return false;
    }

    public void updateMostWantedWall()
    {
        for (Block b : mostWantedHeadBlocks)
        {
            b.setType(Material.AIR);
            Location sign_loc = b.getLocation();
            sign_loc.setY(sign_loc.getY() - 1);
            Block sign = sign_loc.getBlock();
            sign.setType(Material.AIR);
        }

        HashMap<String, Integer> wallMap = sortHashMapByValuesD(mostwanted);
        int current = 0;
        for (Map.Entry<String, Integer> entry : wallMap.entrySet())
        {
            if (current <= mostWantedHeadBlocks.size())
            {
                Block head = mostWantedHeadBlocks.get(current);
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

    public HashMap<String, Integer> sortHashMapByValuesD(HashMap passedMap)
    {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        HashMap<String, Integer> sortedMap = new HashMap<String, Integer>();
        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext())
        {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();
            while (keyIt.hasNext())
            {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2))
                {
                    mapKeys.remove(key);
                    sortedMap.put((String) key, (Integer) val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public Logger getLog()
    {
        return log;
    }

    public ArrayList<String> getOfficers()
    {
        return officers;
    }

    public ArrayList<String> getChiefOfficers()
    {
        return chief_officers;
    }

    public HashMap<String, Boolean> getInCommission()
    {
        return inCommission;
    }

    public HashMap<String, Location> getOutposts()
    {
        return outposts;
    }

    public HashMap<String, Integer> getMostWanted()
    {
        return mostwanted;
    }

    public HashMap<String, Integer> getBounty()
    {
        return bountys;
    }
}
