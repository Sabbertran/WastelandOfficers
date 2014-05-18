package me.sabbertran.wastelandofficers;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OfficerCommand implements CommandExecutor
{

    private WastelandOfficers main;

    public OfficerCommand(WastelandOfficers wo)
    {
        this.main = wo;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        //User Commands
        if (args.length == 0 || args[0].equalsIgnoreCase("help"))
        {
            if (args.length == 2)
            {
                if (args[1].equalsIgnoreCase("info"))
                {
                    sender.sendMessage("[WastelandOfficers] Info über die Officer.");
                    return true;
                } else if (args[1].equalsIgnoreCase("list"))
                {
                    sender.sendMessage("Zeigt eine Liste aller Officer inkl. ihres Onlinestatus.");
                    return true;
                } else if (args[1].equalsIgnoreCase("mw"))
                {
                    sender.sendMessage("Zeigt die Mostwanted Liste.");
                    return true;
                } else
                {
                    sender.sendMessage("[WastelandOfficers] Unknown command.");
                    return true;
                }
            } else
            {
                sender.sendMessage("Use /officer help <command> for more information");
                sender.sendMessage("/officer help <info|list|MW>");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("info"))
        {
            sender.sendMessage("[WastelandOfficers] Die Officer sorgen für Recht und Ordnung. Sollte sich jemand nicht an die Serverregeln halten greifen sie ein.");
            sender.sendMessage("Außerdem kann man bei Ihnen ein Kopfgeld auf Spieler aussetzen. Der nächste, der den Spieler dann tötet erhält das Kopfgeld.");
            return true;
        } else if (args[0].equalsIgnoreCase("list"))
        {
            String chief = "Chief Officer: ";
            for (String s : main.getChiefOfficers())
            {
                if (main.getServer().getPlayer(s) == null)
                {
                    chief = chief + "§c" + s + "§f, ";
                } else if (main.getInCommission().get(s))
                {
                    chief = chief + "§a" + s + "§f, ";
                } else if (!main.getInCommission().get(s))
                {
                    chief = chief + "§e" + s + "§f, ";
                } else
                {
                    chief = chief + "§d" + s + "§f, ";
                }
            }
            chief = chief.substring(0, chief.length() - 2);
            String officers = "Officer: ";
            for (String s : main.getOfficers())
            {
                if (main.getServer().getPlayer(s) == null)
                {
                    officers = officers + "§c" + s + "§f, ";
                } else if (main.getInCommission().get(s))
                {
                    officers = officers + "§a" + s + "§f, ";
                } else if (!main.getInCommission().get(s))
                {
                    officers = officers + "§e" + s + "§f, ";
                } else
                {
                    officers = officers + "§d" + s + "§f, ";
                }
            }
            officers = officers.substring(0, officers.length() - 2);
            sender.sendMessage("Legende: §aIm Dienst§f, §eNicht im Dienst§f, §cOffline");
            sender.sendMessage(chief);
            sender.sendMessage(officers);
            return true;
        } else if (args[0].equalsIgnoreCase("mw"))
        {
            if (args.length == 1)
            {
                sender.sendMessage("Most Wanted Players:");
                String mw = "";
                for (Map.Entry<String, Integer> entry : main.getMostWanted().entrySet())
                {
                    mw = mw + entry.getKey() + " (" + entry.getValue() + " C)" + ", ";
                }
                if (mw.length() > 0)
                {
                    mw = mw.substring(0, mw.length() - 2);
                }
                sender.sendMessage(mw);
                return true;
            } else if (main.getOfficers().contains(sender.getName()) || main.getChiefOfficers().contains(sender.getName()))
            {
                if (args.length == 4)
                {
                    if (args[1].equalsIgnoreCase("add"))
                    {
                        main.getMostWanted().put(args[2], Integer.getInteger(args[3]));
                        sender.sendMessage("Successfully added " + args[2] + "to the Most Wanted list (" + args[3] + " C)");
                        return true;
                    } else if (args[1].equalsIgnoreCase("close"))
                    {
                        if (main.getMostWanted().containsKey(args[2]))
                        {
                            Player p = main.getServer().getPlayer(args[3]);
                            if (p != null)
                            {
                                int reward = main.getMostWanted().get(args[2]);
                                if (main.giveCoins(args[3], reward))
                                {
                                    sender.sendMessage("Sucessfully removed " + args[2] + " from the Most Wanted list and paid " + reward + " Coins to " + args[3] + ".");
                                    main.getMostWanted().remove(args[2]);
                                    return true;
                                } else
                                {
                                    sender.sendMessage(args[2] + " has not enough space in his inventory. Please call a cleaning lady. --x");
                                    return true;
                                }
                            } else
                            {
                                sender.sendMessage("Player " + args[3] + " is not online.");
                                return true;
                            }
                        } else
                        {
                            sender.sendMessage("Player " + args[2] + " is not on the Most Wanted list.");
                            return true;
                        }
                    } else
                    {
                        sender.sendMessage("§cUnknown command");
                        return true;
                    }
                } else
                {
                    sender.sendMessage("Unknown length of arguments.");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown length of arguments.");
                return true;
            }
        }

        //Officer/ChiefOfficer Commands
        if (args[0].equalsIgnoreCase("join"))
        {
            if (main.getOfficers().contains(sender.getName()) || main.getChiefOfficers().contains(sender.getName()))
            {
                if (main.getInCommission().get(sender.getName()) == null || !main.getInCommission().get(sender.getName()))
                {
                    main.getInCommission().put(sender.getName(), true);
                    if (sender instanceof Player)
                    {
                        Player p = (Player) sender;
                        main.changeInventory(p.getName());
                        main.getServer().dispatchCommand(p, "race join Officer");
                    }
                    sender.sendMessage("[WastelandOfficers] You are now in commission!");
                    return true;
                } else
                {
                    sender.sendMessage("[WastelandOfficers] You are already in commission!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
            }

        } else if (args[0].equalsIgnoreCase("leave"))
        {
            if (main.getOfficers().contains(sender.getName()) || main.getChiefOfficers().contains(sender.getName()))
            {
                if (main.getInCommission().get(sender.getName()) != null && main.getInCommission().get(sender.getName()))
                {
                    main.getInCommission().put(sender.getName(), false);
                    if (sender instanceof Player)
                    {
                        Player p = (Player) sender;
                        main.changeInventory(p.getName());
                        main.getServer().dispatchCommand(p, "race join Tank");
                    }
                    sender.sendMessage("[WastelandOfficers] You are no longer in commission!");
                    return true;
                } else
                {
                    sender.sendMessage("[WastelandOfficers] You are not in commission!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
            }
        } else if (args[0].equalsIgnoreCase("chat"))
        {
            if (main.getOfficers().contains(sender.getName()) || main.getChiefOfficers().contains(sender.getName()))
            {
                String msg = "[OfficerChat] " + sender.getName() + ": ";
                for (int i = 1; i < args.length; i++)
                {
                    msg = msg + " " + args[i];
                }

                main.getLog().info(msg);
                for (String s : main.getChiefOfficers())
                {
                    if (main.getServer().getPlayer(s) != null)
                    {
                        main.getServer().getPlayer(s).sendMessage(msg);
                    }
                }
                for (String s1 : main.getOfficers())
                {
                    if (main.getServer().getPlayer(s1) != null)
                    {
                        main.getServer().getPlayer(s1).sendMessage(msg);
                    }
                }
                return true;
            } else
            {
                sender.sendMessage("Unknown command!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("op"))
        {
            if (sender instanceof Player)
            {
                Player p = (Player) sender;
                if (main.getOfficers().contains(p.getName()) || main.getChiefOfficers().contains(p.getName()))
                {
                    if (args.length == 2)
                    {
                        if (args[1].equalsIgnoreCase("list"))
                        {
                            String ap = "Outposts: ";
                            for (String s : main.getOutposts().keySet())
                            {
                                ap = ap + s + ", ";
                            }
                            ap = ap.substring(0, ap.length() - 2);
                            p.sendMessage(ap);
                            return true;
                        } else if (main.getOutposts().get(args[1]) != null)
                        {
                            Location l = main.getOutposts().get(args[1]);
                            p.teleport(l);
                            p.sendMessage("[WastelandOfficers] Teleported to outpost " + args[1] + ".");
                            return true;
                        }
                    } else if (args.length == 3)
                    {
                        if (main.getChiefOfficers().contains(p.getName()))
                        {
                            if (args[1].equalsIgnoreCase("set"))
                            {
                                main.getOutposts().put(args[2], p.getLocation());
                                p.sendMessage("[WastelandOfficers] New outpost " + args[2] + " successfully created.");
                                return true;
                            } else if (args[1].equalsIgnoreCase("del"))
                            {
                                main.getOutposts().remove(args[2]);
                                p.sendMessage("[WastelandOfficers] Outpost " + args[2] + " successfully deleted.");
                                return true;
                            }
                        }
                    } else
                    {
                        p.sendMessage("Unknown length of arguments!");
                        return true;
                    }
                } else
                {
                    sender.sendMessage("Unknown command!");
                    return true;
                }
            } else
            {
                sender.sendMessage("You have to be a player to use this command!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("mw"))
        {
            if (main.getOfficers().contains(sender.getName()) || main.getChiefOfficers().contains(sender.getName()))
            {
                if (args.length == 4)
                {
                    if (args[1].equalsIgnoreCase("add"))
                    {
                        main.getMostWanted().put(args[2], Integer.parseInt(args[3]));
                        main.updateMostWantedWall();
                        sender.sendMessage("[WastelandOfficers] Successfully added " + args[2] + " to the Most Wanted list (" + args[3] + " C).");
                        return true;
                    } else if (args[1].equalsIgnoreCase("close"))
                    {
                        if (main.getMostWanted().containsKey(args[2]))
                        {
                            if (main.getServer().getPlayer(args[3]) != null)
                            {
                                int reward = main.getMostWanted().get(args[2]);
                                if (main.giveCoins(args[3], reward))
                                {
                                    main.getMostWanted().remove(args[2]);
                                    main.updateMostWantedWall();
                                    sender.sendMessage("[WastelandOfficers] " + reward + " Coins have been rewarded to " + args[3] + ". " + args[2] + " is no longet Most Wanted.");
                                    return true;
                                } else
                                {
                                    sender.sendMessage("[WastelandOfficers] " + args[3] + " is not online or has not enough space in his inventory. Try again later.");
                                    return true;
                                }
                            } else
                            {
                                sender.sendMessage("[WastelandOfficers] Player " + args[3] + " is not online. He has to be online to be rewarded.");
                                return true;
                            }
                        } else
                        {
                            sender.sendMessage("[WastelandOfficers] " + args[2] + " is not on the Most Wanted list.");
                            return true;
                        }
                    }
                } else
                {
                    sender.sendMessage("Unknown length of arguments!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("add"))
        {
            if (main.getChiefOfficers().contains(sender.getName()))
            {
                if (args.length == 2)
                {
                    main.getOfficers().add(args[1]);
                    main.getServer().dispatchCommand(main.getServer().getConsoleSender(), "pex user " + args[1] + " group set Officer");
                    sender.sendMessage("[WastelandOfficers] " + args[1] + " is now an Officer.");
                    return true;
                } else
                {
                    sender.sendMessage("Unknown length of arguments!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("del"))
        {
            if (main.getChiefOfficers().contains(sender.getName()))
            {
                if (args.length == 2)
                {
                    main.getOfficers().remove(args[1]);
                    main.getServer().dispatchCommand(main.getServer().getConsoleSender(), "pex user " + args[1] + " group set Oedlaender");
                    sender.sendMessage("[WastelandOfficers] " + args[1] + " is no longer an Officer.");
                    return true;
                } else
                {
                    sender.sendMessage("Unknown length of arguments!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("promote"))
        {
            if (main.getChiefOfficers().contains(sender.getName()))
            {
                if (args.length == 2)
                {
                    main.getOfficers().remove(args[1]);
                    main.getChiefOfficers().add(args[1]);
                    main.getServer().dispatchCommand(main.getServer().getConsoleSender(), "pex user " + args[1] + " group set ChiefOfficer");
                    sender.sendMessage("[WastelandOfficers] " + args[1] + " is now a Chief-Officer.");
                    return true;
                } else
                {
                    sender.sendMessage("Unknown length of arguments!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("demote"))
        {
            if (main.getChiefOfficers().contains(sender.getName()))
            {
                if (args.length == 2)
                {
                    main.getChiefOfficers().remove(args[1]);
                    main.getOfficers().add(args[1]);
                    main.getServer().dispatchCommand(main.getServer().getConsoleSender(), "pex user " + args[1] + " group set Officer");
                    sender.sendMessage("[WastelandOfficers] " + args[1] + " is no longer a Chief-Officer.");
                    return true;
                } else
                {
                    sender.sendMessage("Unknown length of arguments!");
                    return true;
                }
            } else
            {
                sender.sendMessage("Unknown command!");
                return true;
            }
        } else
        {
            sender.sendMessage("Unknown command!");
        }
        sender.sendMessage("Unknown Command!");
        return true;
    }
}
