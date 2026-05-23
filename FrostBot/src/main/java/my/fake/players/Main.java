package my.fake.players;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public final class Main extends JavaPlugin {
    private final List<String> availableNames = new ArrayList<>();
    private final List<String> activeBots = new ArrayList<>();
    private final List<String> chatPhrases = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigData();

        // Проверка заходов и выходов ботов раз в 2 минуты
        new BukkitRunnable() {
            @Override
            public void run() { handleBotLifecycle(); }
        }.runTaskTimer(this, 200L, 2400L);

        // Общение ботов с БОЛЬШИМ КД (раз в 10-20 минут)
        new BukkitRunnable() {
            @Override
            public void run() { handleBotChat(); }
        }.runTaskTimer(this, 600L, 6000L);
        
        getLogger().info("Плагин FakePlayers успешно запущен!");
    }

    @Override
    public void onDisable() { activeBots.clear(); }

    private void loadConfigData() {
        availableNames.addAll(getConfig().getStringList("bot-names"));
        chatPhrases.addAll(getConfig().getStringList("chat-phrases"));
        
        int initialAmount = getConfig().getInt("settings.min-bots", 5);
        for (int i = 0; i < initialAmount; i++) {
            if (!availableNames.isEmpty()) {
                activeBots.add(availableNames.remove(random.nextInt(availableNames.size())));
            }
        }
    }

    private void handleBotLifecycle() {
        int minBots = getConfig().getInt("settings.min-bots", 5);
        int maxBots = getConfig().getInt("settings.max-bots", 15);
        if (random.nextDouble() > 0.3) return;

        if (activeBots.size() < maxBots && (activeBots.size() < minBots || random.nextBoolean())) {
            if (!availableNames.isEmpty()) {
                String newBot = availableNames.remove(random.nextInt(availableNames.size()));
                activeBots.add(newBot);
                if (getConfig().getBoolean("settings.show-join-messages", true)) {
                    Bukkit.broadcastMessage(color(getConfig().getString("messages.join").replace("%player%", newBot)));
                }
            }
        } else if (activeBots.size() > minBots) {
            String oldBot = activeBots.remove(random.nextInt(activeBots.size()));
            availableNames.add(oldBot);
            if (getConfig().getBoolean("settings.show-quit-messages", true)) {
                Bukkit.broadcastMessage(color(getConfig().getString("messages.quit").replace("%player%", oldBot)));
            }
        }
    }

    private void handleBotChat() {
        if (activeBots.isEmpty() || chatPhrases.isEmpty()) return;
        if (random.nextDouble() > 0.25) return; 

        String botName = activeBots.get(random.nextInt(activeBots.size()));
        String phrase = chatPhrases.get(random.nextInt(chatPhrases.size()));
        Bukkit.broadcastMessage(color("&f<" + botName + "> " + phrase));
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}