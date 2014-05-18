package me.sabbertran.wastelandofficers.bounty;

import java.util.HashMap;
import java.util.Map;
import me.sabbertran.wastelandofficers.WastelandOfficers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BountyCommand implements CommandExecutor
{

    private WastelandOfficers main;
    private Bounty bounty;

    public BountyCommand(WastelandOfficers wo, Bounty bounty)
    {
        this.main = wo;
        this.bounty = bounty;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("list"))
            {
                HashMap<String, Integer> sortedBounty = main.sortHashMapByValuesD(main.getBounty());
                sender.sendMessage("List of hunted players:");
                for (Map.Entry<String, Integer> entry : sortedBounty.entrySet())
                {
                    sender.sendMessage(entry.getKey() + ": " + entry.getValue() + " Coins");
                }
                return true;
            } else
            {
                sender.sendMessage("Use '/bounty list' for a list of currently hunted players.");
                return true;
            }
        } else
        {
            sender.sendMessage("Use '/bounty list' for a list of currently hunted players.");
            return true;
        }
    }
}
