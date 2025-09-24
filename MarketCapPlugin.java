package com.example.marketcap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Main plugin class. When enabled, the plugin will register a PlaceholderAPI
 * expansion (if PlaceholderAPI is present) and start a background task
 * that periodically fetches the USD market cap from the pump.fun token page.
 *
 * The fetched value is exposed through the custom placeholder %mymarketcap%.
 */
public class MarketCapPlugin extends JavaPlugin {
    private MarketCapExpansion expansion;

    @Override
    public void onEnable() {
        // Register our PlaceholderExpansion if PlaceholderAPI is installed.
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new MarketCapExpansion(this);
            expansion.register();
            getLogger().info("MarketCap placeholder registered as %mymarketcap%.");
        } else {
            getLogger().warning("PlaceholderAPI not found. MarketCap placeholder will not be registered.");
        }

        // Start the repeating asynchronous task to fetch the market cap.
        startUpdateTask();
    }

    @Override
    public void onDisable() {
        // Unregister the expansion on shutdown to clean up properly.
        if (expansion != null) {
            expansion.unregister();
        }
    }

    /**
     * Schedules a repeating asynchronous task that fetches the market cap.
     * The task runs immediately on plugin enable and then every 5 minutes.
     */
    private void startUpdateTask() {
        // 5 minutes expressed in ticks (20 ticks = 1 second)
        final long interval = 20L * 60 * 5;
        new BukkitRunnable() {
            @Override
            public void run() {
                String value = MarketCapUtils.fetchMarketCap();
                MarketCapExpansion.updateMarketCap(value);
                getLogger().fine("Updated market cap to " + value);
            }
        }.runTaskTimerAsynchronously(this, 0L, interval);
    }
}