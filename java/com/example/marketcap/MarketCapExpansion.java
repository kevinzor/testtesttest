package com.example.marketcap;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderExpansion that exposes the current USD market cap for the configured pump.fun token.
 *
 * Once registered, the placeholder %mymarketcap% will return the latest value
 * fetched by {@link MarketCapUtils}. If the value has not yet been fetched or
 * an error occurred during fetch, it returns "N/A".
 */
public class MarketCapExpansion extends PlaceholderExpansion {
    private static volatile String currentMarketCap = "N/A";
    private final MarketCapPlugin plugin;

    public MarketCapExpansion(MarketCapPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        // This identifier determines the name of the placeholder without the % signs.
        return "mymarketcap";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() ? "Unknown" : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        // Return the most recently fetched value.
        return currentMarketCap;
    }

    /**
     * Updates the cached market cap value. Called by the update task in {@link MarketCapPlugin}.
     *
     * @param newValue the newly fetched market cap; if null or empty, "N/A" will be used.
     */
    public static void updateMarketCap(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            currentMarketCap = "N/A";
        } else {
            currentMarketCap = newValue;
        }
    }
}