package com.example.cryptorates;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class BinanceParser {
    public interface ParseCallback {
        void onPriceReceived(String price, String change24h, String changePercent24h);
        void onError(Exception e);
    }

    public interface RubleRateCallback {
        void onRateReceived(String usdToRubRate);
        void onError(Exception e);
    }

    public void fetchCryptoData(String ticker, ParseCallback callback) {
        new Thread(() -> {
            try {
                String apiUrl = String.format("https://api.binance.com/api/v3/ticker/24hr?symbol=%sUSDT", ticker);

                String jsonResponse = Jsoup.connect(apiUrl)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0")
                        .timeout(3000)
                        .execute()
                        .body();

                JSONObject json = new JSONObject(jsonResponse);
                String price = json.getString("lastPrice");
                String change = json.getString("priceChange");
                String changePercent = json.getString("priceChangePercent");

                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onPriceReceived(price, change, changePercent)
                );

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(e)
                );
            }
        }).start();
    }

    public void fetchUsdToRubRate(RubleRateCallback callback) {
        new Thread(() -> {
            try {
                String cbrUrl = "https://www.cbr-xml-daily.ru/daily_json.js";
                String json = Jsoup.connect(cbrUrl)
                        .ignoreContentType(true)
                        .timeout(3000)
                        .execute()
                        .body();

                String usdToRub = new JSONObject(json)
                        .getJSONObject("Valute")
                        .getJSONObject("USD")
                        .getString("Value");

                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onRateReceived(usdToRub)
                );

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(e)
                );
            }
        }).start();
    }
}