package com.creray.sweetdreams.command;

import com.creray.sweetdreams.sleep.world.SleepWorldData;
import com.creray.sweetdreams.sleep.world.SleepWorlds;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SweetDreamsMainCommandExecutor implements CommandExecutor {

    private final SleepWorlds SLEEP_WORLDS;
    private final Logger LOGGER;

    public SweetDreamsMainCommandExecutor(SleepWorlds sleepWorlds, Logger logger) {
        SLEEP_WORLDS = sleepWorlds;
        LOGGER = logger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!onCommand(sender, args)) {
            sender.sendMessage("§cНеверная или неполная комманда, введите '/sweetdreams help' для подсказки.");
        }
        return true;
    }

    private boolean onCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (args[0].equals("help")) {
                sendHelpMessage(sender);
                return true;
            }
            return false;
        }
        if (args.length == 2) {
            if (args[0].equals("playersSleepingPercentage")) {
                if (args[1].equals("get")) {
                    getPlayersSleepingPercentage(sender);
                    return true;
                }
            }
            return false;
        }
        if (args.length == 3) {
            if (args[0].equals("playersSleepingPercentage")) {
                if (args[1].equals("get")) {
                    getPlayersSleepingPercentage(sender, args[2]);
                    return true;
                }
                if (args[1].equals("set")) {
                    setPlayersSleepingPercentage(sender, args[2]);
                    return true;
                }
                return false;
            }
            return false;
        }
        if (args.length == 4) {
            if (args[0].equals("playersSleepingPercentage") && args[1].equals("set")) {
                setPlayersSleepingPercentage(sender, args[2], args[3]);
            }
        }
        return false;
    }

    private void sendHelpMessage(CommandSender sender) {
        String helpMessage =
                "\n/sweetdreams help§7: Подробная информация об подкоммандах.\n" +
                "§r/sweetdreams playersSleepingPercentage get [world_name]§7: Узнать минимальный процент игроков, необходимый для пропуска ночи.\n" +
                "§r/sweetdreams playersSleepingPercentage set <value> [world_name]§7: Установить минимальный процент игроков, необходимый для пропуска ночи.\n\n";
        sender.sendMessage(helpMessage);
    }

    private void getPlayersSleepingPercentage(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            getPlayersSleepingPercentage(sender, player.getWorld().getName());
            return;
        }
        StringBuilder message = new StringBuilder("\n");
        for (SleepWorldData sleepWorldData : SLEEP_WORLDS.getSleepWorldsData()) {
            message.append("Значение playersSleepingPercentage для мира '")
                    .append(sleepWorldData.getWorldName())
                    .append("': ")
                    .append(sleepWorldData.getPlayersSleepingPercentage())
                    .append("\n");
        }
        sender.sendMessage(message.toString());
    }

    private void getPlayersSleepingPercentage(CommandSender sender, String worldName) {
        Consumer<SleepWorldData> consumer = (sleepWorldData) -> {
            int playersSleepingPercentage = sleepWorldData.getPlayersSleepingPercentage();
            sender.sendMessage("Значение playersSleepingPercentage для мира '" + worldName + "': " + playersSleepingPercentage);
        };
        sleepWorldOperation(sender, worldName, consumer);
    }

    private void setPlayersSleepingPercentage(CommandSender sender, String value) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§сПроизошла ошибка при выполнении комманды: укажите мир к которому будет применяться новое значение.");
            return;
        }
        Player player = (Player) sender;
        setPlayersSleepingPercentage(sender, value, player.getWorld().getName());

    }

    private void setPlayersSleepingPercentage(CommandSender sender, String value, String worldName) {
        Consumer<SleepWorldData> consumer = sleepWorldData -> {
            int playersSleepingPercentage;
            try {
                playersSleepingPercentage = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cНеверное целове число.");
                return;
            }
            sleepWorldData.setPlayersSleepingPercentage(playersSleepingPercentage);
            sender.sendMessage("Установленно новое значение playersSleepingPercentage для мира '" + worldName + "': " + playersSleepingPercentage);
            LOGGER.info("Set new playersSleepingPercentage value for '" + worldName + "' world: " + playersSleepingPercentage);
        };
        sleepWorldOperation(sender, worldName, consumer);
    }

    private void sleepWorldOperation(CommandSender sender, String worldName, Consumer<SleepWorldData> consumer) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage("§cПроизошла ошибка при выполнении комманды: не удалось найти мир '" + worldName + "'.");
            return;
        }
        SleepWorldData sleepWorldData;
        try {
            sleepWorldData = SLEEP_WORLDS.getSleepWorldData(world);
        }
        catch (NoSuchElementException e) {
            sender.sendMessage("§cПроизошла ошибка при выполнении комманды: мир '" + worldName + "' не является подходящим местом для сна.");
            return;
        }
        consumer.accept(sleepWorldData);
    }
}
