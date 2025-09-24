package com.example.marketcap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Utility class responsible for fetching and parsing the USD market cap from the pump.fun token page.
 */
public final class MarketCapUtils {
    // Pump.fun token page to scrape. Change this constant to target a different token.
    private static final String TOKEN_URL = "https://pump.fun/coin/58BMhEDSY1ySm7g87zDU63bbeP9SPpkoYSKroD19pump";
    // User-Agent header to mimic a regular browser. Some sites block requests without a user agent.
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; MarketCapFetcher/1.0; +https://pump.fun)";

    private MarketCapUtils() {
        // static utility class; no instances allowed
    }

    /**
     * Fetches the pump.fun token page and extracts the usd_market_cap value.
     *
     * @return the formatted market cap value (e.g. "$19.80K") or "N/A" if it couldn't be fetched or parsed.
     */
    public static String fetchMarketCap() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(TOKEN_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                return "N/A";
            }

            StringBuilder sb = new StringBuilder();
            try (InputStream in = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String html = sb.toString();
            // Find the index of the usd_market_cap field in the page source.
            int idx = html.indexOf("\"usd_market_cap\":");
            if (idx == -1) {
                return "N/A";
            }
            int start = idx + "\"usd_market_cap\":".length();
            // Skip until we hit a digit or minus sign.
            while (start < html.length() && !Character.isDigit(html.charAt(start)) && html.charAt(start) != '-') {
                start++;
            }
            int end = start;
            while (end < html.length() && (Character.isDigit(html.charAt(end)) || html.charAt(end) == '.')) {
                end++;
            }
            if (end <= start) {
                return "N/A";
            }
            String numberStr = html.substring(start, end);
            double value = Double.parseDouble(numberStr);
            return formatNumber(value);
        } catch (IOException | NumberFormatException ex) {
            // Log the exception to standard output for debugging. In production you can use plugin logger.
            ex.printStackTrace();
            return "N/A";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Formats a numeric market cap into a short human-readable string with a dollar sign.
     * For example, 19794.950585 becomes "$19.79K" and 2500000 becomes "$2.50M".
     *
     * @param value raw USD market cap value
     * @return formatted string
     */
    private static String formatNumber(double value) {
        String[] suffixes = {"", "K", "M", "B", "T"};
        int idx = 0;
        while (idx < suffixes.length - 1 && value >= 1000) {
            value /= 1000.0;
            idx++;
        }
        return String.format("$%.2f%s", value, suffixes[idx]);
    }
}