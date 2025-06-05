package com.example.cryptorates;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.cryptocurrencies) {
                            if (currentFragmentId != 0) {
                                slideFragment(findViewById(fragmentsData[currentFragmentId]), findViewById(R.id.fragment_main), false);
                                item.setChecked(true);
                                currentFragmentId = 0;
                            }
                        } else if (item.getItemId() == R.id.converter) {
                            if (currentFragmentId != 1) {
                                slideFragment(findViewById(fragmentsData[currentFragmentId]), findViewById(R.id.fragment_converter),
                                        currentFragmentId == 0);
                                item.setChecked(true);
                                currentFragmentId = 1;
                            }
                        } else if (item.getItemId() == R.id.favourites) {
                            if (currentFragmentId != 2) {
                                slideFragment(findViewById(fragmentsData[currentFragmentId]), findViewById(R.id.fragment_favourites),
                                        currentFragmentId <= 1);
                                item.setChecked(true);
                                currentFragmentId = 2;
                                updateFavouritesTable();
                            }
                        } else if (item.getItemId() == R.id.top) {
                            if (currentFragmentId != 3) {
                                slideFragment(findViewById(fragmentsData[currentFragmentId]), findViewById(R.id.fragment_top),
                                        currentFragmentId <= 2);
                                item.setChecked(true);
                                currentFragmentId = 3;
                            }
                        } else if (item.getItemId() == R.id.info) {
                            if (currentFragmentId != 4) {
                                slideFragment(findViewById(fragmentsData[currentFragmentId]), findViewById(R.id.fragment_info), true);
                                item.setChecked(true);
                                currentFragmentId = 4;
                            }
                        }
                        return false;
                    }
                });
    }
    Integer currentFragmentId = 0;
    int[] fragmentsData = {R.id.fragment_main, R.id.fragment_converter, R.id.fragment_favourites, R.id.fragment_top, R.id.fragment_info};
    private void slideFragment(View current, View next, boolean isForward) {
        int width = current.getWidth();
        float currentEnd = isForward ? -width : width;
        float nextStart = isForward ? width : -width;

        next.setTranslationX(nextStart);
        next.setVisibility(View.VISIBLE);

        current.animate()
                .translationX(currentEnd)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        next.animate()
                .translationX(0)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    current.setVisibility(View.GONE);
                    current.setTranslationX(0);
                })
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPriceUpdates();
        setOnClickListeners();
        loadDataFromDB();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPriceUpdates();
    }

    @Override
    protected void onDestroy() {
        stopPriceUpdates();
        priceUpdateHandler = null;
        super.onDestroy();
    }

    String[] btcSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] ethSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] solSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] xrpSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] bnbSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] dogeSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] tonSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] adaSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] trxSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] pepeSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] suiSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] avaxSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] linkSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] shibSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    String[] notSavedData = {"+0.0", "+0.0%", "percent", "0.0"};
    int countErrors = 0;
    String rubRate = "0.0";
    BinanceParser binanceParser = new BinanceParser();
    DecimalFormatSymbols setSpace = new DecimalFormatSymbols();
    Map<String, Integer> imgFavDict = new HashMap<>() {{
        put("BTC", R.id.btc_button_fvr);
        put("ETH", R.id.eth_button_fvr);
        put("SOL", R.id.sol_button_fvr);
        put("XRP", R.id.xrp_button_fvr);
        put("BNB", R.id.bnb_button_fvr);
        put("DOGE", R.id.doge_button_fvr);
        put("TON", R.id.ton_button_fvr);
        put("ADA", R.id.ada_button_fvr);
        put("TRX", R.id.trx_button_fvr);
        put("PEPE", R.id.pepe_button_fvr);
        put("SUI", R.id.sui_button_fvr);
        put("AVAX", R.id.avax_button_fvr);
        put("LINK", R.id.link_button_fvr);
        put("SHIB", R.id.shib_button_fvr);
        put("NOT", R.id.not_button_fvr);
    }};
    Map<String, String[]> dataDict = new LinkedHashMap<>() {{
        put("BTC", btcSavedData);
        put("ETH", ethSavedData);
        put("SOL", solSavedData);
        put("XRP", xrpSavedData);
        put("BNB", bnbSavedData);
        put("DOGE", dogeSavedData);
        put("TON", tonSavedData);
        put("ADA", adaSavedData);
        put("TRX", trxSavedData);
        put("PEPE", pepeSavedData);
        put("SUI", suiSavedData);
        put("AVAX", avaxSavedData);
        put("LINK", linkSavedData);
        put("SHIB", shibSavedData);
        put("NOT", notSavedData);
    }};
    Map<String, Integer> imgDict = new HashMap<>() {{
        put("BTC", R.drawable.btc_logo);
        put("ETH", R.drawable.eth_logo);
        put("SOL", R.drawable.sol_logo);
        put("XRP", R.drawable.xrp_logo);
        put("BNB", R.drawable.bnb_logo);
        put("DOGE", R.drawable.doge_logo);
        put("TON", R.drawable.ton_logo);
        put("ADA", R.drawable.ada_logo);
        put("TRX", R.drawable.trx_logo);
        put("PEPE", R.drawable.pepe_logo);
        put("SUI", R.drawable.sui_logo);
        put("AVAX", R.drawable.avax_logo);
        put("LINK", R.drawable.link_logo);
        put("SHIB", R.drawable.shib_logo);
        put("NOT", R.drawable.not_logo);
    }};
    Map<String, String> nameDict = new HashMap<>() {{
        put("BTC", "Bitcoin");
        put("ETH", "Ethereum");
        put("SOL", "Solana");
        put("XRP", "XRP");
        put("BNB", "BNB");
        put("DOGE", "Dogecoin");
        put("TON", "Toncoin");
        put("ADA", "Cardano");
        put("TRX", "TRON");
        put("PEPE", "Pepe");
        put("SUI", "Sui");
        put("AVAX", "Avalanche");
        put("LINK", "Chainlink");
        put("SHIB", "Shiba Inu");
        put("NOT", "Notcoin");
    }};

    protected void updateCryptocurrencyPrices() {
        View fragmentView = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main)
                .getView();

        setSpace.setGroupingSeparator(' ');

        binanceParser.fetchCryptoData("BTC", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView btcPrice = fragmentView.findViewById(R.id.btc_price);
                TextView btcChange = fragmentView.findViewById(R.id.btc_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#,###", setSpace).format(Double.parseDouble(change)) + " USD";
                btcChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#,###", setSpace).format(Double.parseDouble(price));

                btcSavedData[0] = formattedChange;
                btcSavedData[1] = formattedPercent;
                btcSavedData[3] = formattedPrice;

                if (btcSavedData[2].equals("percent")) {
                    btcChange.setText(formattedPercent);
                } else {
                    btcChange.setText(formattedChange);
                }
                btcPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("ETH", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView ethPrice = fragmentView.findViewById(R.id.eth_price);
                TextView ethChange = fragmentView.findViewById(R.id.eth_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.#").format(Double.parseDouble(change)) + " USD";
                ethChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#,###.#", setSpace).format(Double.parseDouble(price));

                ethSavedData[0] = formattedChange;
                ethSavedData[1] = formattedPercent;
                ethSavedData[3] = formattedPrice;

                if (ethSavedData[2].equals("percent")) {
                    ethChange.setText(formattedPercent);
                } else {
                    ethChange.setText(formattedChange);
                }

                ethPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("SOL", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView solPrice = fragmentView.findViewById(R.id.sol_price);
                TextView solChange = fragmentView.findViewById(R.id.sol_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(change)) + " USD";
                solChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.##").format(Double.parseDouble(price));

                solSavedData[0] = formattedChange;
                solSavedData[1] = formattedPercent;
                solSavedData[3] = formattedPrice;

                if (solSavedData[2].equals("percent")) {
                    solChange.setText(formattedPercent);
                } else {
                    solChange.setText(formattedChange);
                }

                solPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("XRP", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView xrpPrice = fragmentView.findViewById(R.id.xrp_price);
                TextView xrpChange = fragmentView.findViewById(R.id.xrp_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                xrpChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                xrpSavedData[0] = formattedChange;
                xrpSavedData[1] = formattedPercent;
                xrpSavedData[3] = formattedPrice;

                if (xrpSavedData[2].equals("percent")) {
                    xrpChange.setText(formattedPercent);
                } else {
                    xrpChange.setText(formattedChange);
                }
                xrpPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("BNB", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView bnbPrice = fragmentView.findViewById(R.id.bnb_price);
                TextView bnbChange = fragmentView.findViewById(R.id.bnb_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.#").format(Double.parseDouble(change)) + " USD";
                bnbChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.#").format(Double.parseDouble(price));

                bnbSavedData[0] = formattedChange;
                bnbSavedData[1] = formattedPercent;
                bnbSavedData[3] = formattedPrice;

                if (bnbSavedData[2].equals("percent")) {
                    bnbChange.setText(formattedPercent);
                } else {
                    bnbChange.setText(formattedChange);
                }

                bnbPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("DOGE", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView dogePrice = fragmentView.findViewById(R.id.doge_price);
                TextView dogeChange = fragmentView.findViewById(R.id.doge_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                dogeChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                dogeSavedData[0] = formattedChange;
                dogeSavedData[1] = formattedPercent;
                dogeSavedData[3] = formattedPrice;

                if (dogeSavedData[2].equals("percent")) {
                    dogeChange.setText(formattedPercent);
                } else {
                    dogeChange.setText(formattedChange);
                }

                dogePrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("TON", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView tonPrice = fragmentView.findViewById(R.id.ton_price);
                TextView tonChange = fragmentView.findViewById(R.id.ton_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(change)) + " USD";
                tonChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.##").format(Double.parseDouble(price));

                tonSavedData[0] = formattedChange;
                tonSavedData[1] = formattedPercent;
                tonSavedData[3] = formattedPrice;

                if (tonSavedData[2].equals("percent")) {
                    tonChange.setText(formattedPercent);
                } else {
                    tonChange.setText(formattedChange);
                }

                tonPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("ADA", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView adaPrice = fragmentView.findViewById(R.id.ada_price);
                TextView adaChange = fragmentView.findViewById(R.id.ada_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                adaChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                adaSavedData[0] = formattedChange;
                adaSavedData[1] = formattedPercent;
                adaSavedData[3] = formattedPrice;

                if (adaSavedData[2].equals("percent")) {
                    adaChange.setText(formattedPercent);
                } else {
                    adaChange.setText(formattedChange);
                }

                adaPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("TRX", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView trxPrice = fragmentView.findViewById(R.id.trx_price);
                TextView trxChange = fragmentView.findViewById(R.id.trx_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                trxChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                trxSavedData[0] = formattedChange;
                trxSavedData[1] = formattedPercent;
                trxSavedData[3] = formattedPrice;

                if (trxSavedData[2].equals("percent")) {
                    trxChange.setText(formattedPercent);
                } else {
                    trxChange.setText(formattedChange);
                }

                trxPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("PEPE", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView pepePrice = fragmentView.findViewById(R.id.pepe_price);
                TextView pepeChange = fragmentView.findViewById(R.id.pepe_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.########").format(Double.parseDouble(change)) + " USD";
                pepeChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.########").format(Double.parseDouble(price));

                pepeSavedData[0] = formattedChange;
                pepeSavedData[1] = formattedPercent;
                pepeSavedData[3] = formattedPrice;

                if (pepeSavedData[2].equals("percent")) {
                    pepeChange.setText(formattedPercent);
                } else {
                    pepeChange.setText(formattedChange);
                }

                pepePrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("SUI", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView suiPrice = fragmentView.findViewById(R.id.sui_price);
                TextView suiChange = fragmentView.findViewById(R.id.sui_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                suiChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                suiSavedData[0] = formattedChange;
                suiSavedData[1] = formattedPercent;
                suiSavedData[3] = formattedPrice;

                if (suiSavedData[2].equals("percent")) {
                    suiChange.setText(formattedPercent);
                } else {
                    suiChange.setText(formattedChange);
                }

                suiPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("AVAX", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView avaxPrice = fragmentView.findViewById(R.id.avax_price);
                TextView avaxChange = fragmentView.findViewById(R.id.avax_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                avaxChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                avaxSavedData[0] = formattedChange;
                avaxSavedData[1] = formattedPercent;
                avaxSavedData[3] = formattedPrice;

                if (avaxSavedData[2].equals("percent")) {
                    avaxChange.setText(formattedPercent);
                } else {
                    avaxChange.setText(formattedChange);
                }

                avaxPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("LINK", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView linkPrice = fragmentView.findViewById(R.id.link_price);
                TextView linkChange = fragmentView.findViewById(R.id.link_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.###").format(Double.parseDouble(change)) + " USD";
                linkChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.###").format(Double.parseDouble(price));

                linkSavedData[0] = formattedChange;
                linkSavedData[1] = formattedPercent;
                linkSavedData[3] = formattedPrice;

                if (linkSavedData[2].equals("percent")) {
                    linkChange.setText(formattedPercent);
                } else {
                    linkChange.setText(formattedChange);
                }

                linkPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("SHIB", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView shibPrice = fragmentView.findViewById(R.id.shib_price);
                TextView shibChange = fragmentView.findViewById(R.id.shib_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.########").format(Double.parseDouble(change)) + " USD";
                shibChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.########").format(Double.parseDouble(price));

                shibSavedData[0] = formattedChange;
                shibSavedData[1] = formattedPercent;
                shibSavedData[3] = formattedPrice;

                if (shibSavedData[2].equals("percent")) {
                    shibChange.setText(formattedPercent);
                } else {
                    shibChange.setText(formattedChange);
                }

                shibPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchCryptoData("NOT", new BinanceParser.ParseCallback() {
            @Override
            public void onPriceReceived(String price, String change, String changePercent) {
                TextView notPrice = fragmentView.findViewById(R.id.not_price);
                TextView notChange = fragmentView.findViewById(R.id.not_change);

                String formattedPercent = (Double.parseDouble(changePercent) > 0 ? "+" : "") +
                        new DecimalFormat("#.##").format(Double.parseDouble(changePercent)) + "%";
                String formattedChange = (Double.parseDouble(change) > 0 ? "+" : "") +
                        new DecimalFormat("#.#####").format(Double.parseDouble(change)) + " USD";
                notChange.setTextColor(ContextCompat.getColor(MainActivity.this, Double.parseDouble(changePercent) < 0 ? R.color.red : R.color.green));
                String formattedPrice = new DecimalFormat("#.#####").format(Double.parseDouble(price));

                notSavedData[0] = formattedChange;
                notSavedData[1] = formattedPercent;
                notSavedData[3] = formattedPrice;

                if (notSavedData[2].equals("percent")) {
                    notChange.setText(formattedPercent);
                } else {
                    notChange.setText(formattedChange);
                }

                notPrice.setText(formattedPrice + " USD");
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        binanceParser.fetchUsdToRubRate(new BinanceParser.RubleRateCallback() {
            @Override
            public void onRateReceived(String usdToRubRate) {
                rubRate = usdToRubRate;
            }

            @Override
            public void onError(Exception e) {
                countErrors++;
            }
        });

        if (countErrors > 0) {
            runOnUiThread(() -> Toast.makeText(
                    MainActivity.this,
                    "Ошибка получения данных",
                    Toast.LENGTH_SHORT
            ).show());
            countErrors = 0;
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                updateFavouritesTable();
                updateTop();
            }
        }, 2000);
    }

    public void setOnClickListeners() {
        View fragmentView = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main)
                .getView();

        View fragmentViewConverter = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_converter)
                .getView();

        fragmentView.findViewById(R.id.btc_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView btcChange = fragmentView.findViewById(R.id.btc_change);
                if (btcSavedData[2].equals("percent")) {
                    btcChange.setText(btcSavedData[0]);
                    btcSavedData[2] = "number";
                } else {
                    btcChange.setText(btcSavedData[1]);
                    btcSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.eth_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView ethChange = fragmentView.findViewById(R.id.eth_change);
                if (ethSavedData[2].equals("percent")) {
                    ethChange.setText(ethSavedData[0]);
                    ethSavedData[2] = "number";
                } else {
                    ethChange.setText(ethSavedData[1]);
                    ethSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.sol_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView solChange = fragmentView.findViewById(R.id.sol_change);
                if (solSavedData[2].equals("percent")) {
                    solChange.setText(solSavedData[0]);
                    solSavedData[2] = "number";
                } else {
                    solChange.setText(solSavedData[1]);
                    solSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.xrp_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView xrpChange = fragmentView.findViewById(R.id.xrp_change);
                if (xrpSavedData[2].equals("percent")) {
                    xrpChange.setText(xrpSavedData[0]);
                    xrpSavedData[2] = "number";
                } else {
                    xrpChange.setText(xrpSavedData[1]);
                    xrpSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.bnb_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView bnbChange = fragmentView.findViewById(R.id.bnb_change);
                if (bnbSavedData[2].equals("percent")) {
                    bnbChange.setText(bnbSavedData[0]);
                    bnbSavedData[2] = "number";
                } else {
                    bnbChange.setText(bnbSavedData[1]);
                    bnbSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.doge_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView dogeChange = fragmentView.findViewById(R.id.doge_change);
                if (dogeSavedData[2].equals("percent")) {
                    dogeChange.setText(dogeSavedData[0]);
                    dogeSavedData[2] = "number";
                } else {
                    dogeChange.setText(dogeSavedData[1]);
                    dogeSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.ton_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tonChange = fragmentView.findViewById(R.id.ton_change);
                if (tonSavedData[2].equals("percent")) {
                    tonChange.setText(tonSavedData[0]);
                    tonSavedData[2] = "number";
                } else {
                    tonChange.setText(tonSavedData[1]);
                    tonSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.ada_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView adaChange = fragmentView.findViewById(R.id.ada_change);
                if (adaSavedData[2].equals("percent")) {
                    adaChange.setText(adaSavedData[0]);
                    adaSavedData[2] = "number";
                } else {
                    adaChange.setText(adaSavedData[1]);
                    adaSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.trx_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView trxChange = fragmentView.findViewById(R.id.trx_change);
                if (trxSavedData[2].equals("percent")) {
                    trxChange.setText(trxSavedData[0]);
                    trxSavedData[2] = "number";
                } else {
                    trxChange.setText(trxSavedData[1]);
                    trxSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.pepe_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView pepeChange = fragmentView.findViewById(R.id.pepe_change);
                if (pepeSavedData[2].equals("percent")) {
                    pepeChange.setText(pepeSavedData[0]);
                    pepeSavedData[2] = "number";
                } else {
                    pepeChange.setText(pepeSavedData[1]);
                    pepeSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.sui_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView suiChange = fragmentView.findViewById(R.id.sui_change);
                if (suiSavedData[2].equals("percent")) {
                    suiChange.setText(suiSavedData[0]);
                    suiSavedData[2] = "number";
                } else {
                    suiChange.setText(suiSavedData[1]);
                    suiSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.avax_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView avaxChange = fragmentView.findViewById(R.id.avax_change);
                if (avaxSavedData[2].equals("percent")) {
                    avaxChange.setText(avaxSavedData[0]);
                    avaxSavedData[2] = "number";
                } else {
                    avaxChange.setText(avaxSavedData[1]);
                    avaxSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.link_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView linkChange = fragmentView.findViewById(R.id.link_change);
                if (linkSavedData[2].equals("percent")) {
                    linkChange.setText(linkSavedData[0]);
                    linkSavedData[2] = "number";
                } else {
                    linkChange.setText(linkSavedData[1]);
                    linkSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.shib_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView shibChange = fragmentView.findViewById(R.id.shib_change);
                if (shibSavedData[2].equals("percent")) {
                    shibChange.setText(shibSavedData[0]);
                    shibSavedData[2] = "number";
                } else {
                    shibChange.setText(shibSavedData[1]);
                    shibSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.not_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView notChange = fragmentView.findViewById(R.id.not_change);
                if (notSavedData[2].equals("percent")) {
                    notChange.setText(notSavedData[0]);
                    notSavedData[2] = "number";
                } else {
                    notChange.setText(notSavedData[1]);
                    notSavedData[2] = "percent";
                }
            }
        });

        fragmentView.findViewById(R.id.btc_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.btc_button_fvr);
                if (dbHelper.isNameExists("BTC")) {
                    dbHelper.deleteData("BTC");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Bitcoin", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("BTC", "Bitcoin");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Bitcoin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.eth_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.eth_button_fvr);
                if (dbHelper.isNameExists("ETH")) {
                    dbHelper.deleteData("ETH");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Ethereum", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("ETH", "Ethereum");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Ethereum", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.sol_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.sol_button_fvr);
                if (dbHelper.isNameExists("SOL")) {
                    dbHelper.deleteData("SOL");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Solana", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("SOL", "Solana");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Solana", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.xrp_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.xrp_button_fvr);
                if (dbHelper.isNameExists("XRP")) {
                    dbHelper.deleteData("XRP");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: XRP", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("XRP", "XRP");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: XRP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.bnb_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.bnb_button_fvr);
                if (dbHelper.isNameExists("BNB")) {
                    dbHelper.deleteData("BNB");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: BNB", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("BNB", "BNB");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: BNB", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.doge_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.doge_button_fvr);
                if (dbHelper.isNameExists("DOGE")) {
                    dbHelper.deleteData("DOGE");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Dogecoin", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("DOGE", "Dogecoin");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Dogecoin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.ton_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.ton_button_fvr);
                if (dbHelper.isNameExists("TON")) {
                    dbHelper.deleteData("TON");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Toncoin", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("TON", "Toncoin");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Toncoin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.ada_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.ada_button_fvr);
                if (dbHelper.isNameExists("ADA")) {
                    dbHelper.deleteData("ADA");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Cardano", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("ADA", "Cardano");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Cardano", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.trx_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.trx_button_fvr);
                if (dbHelper.isNameExists("TRX")) {
                    dbHelper.deleteData("TRX");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: TRON", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("TRX", "TRON");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: TRON", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.pepe_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.pepe_button_fvr);
                if (dbHelper.isNameExists("PEPE")) {
                    dbHelper.deleteData("PEPE");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Pepe", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("PEPE", "Pepe");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Pepe", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.sui_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.sui_button_fvr);
                if (dbHelper.isNameExists("SUI")) {
                    dbHelper.deleteData("SUI");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Sui", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("SUI", "Sui");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Sui", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.avax_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.avax_button_fvr);
                if (dbHelper.isNameExists("AVAX")) {
                    dbHelper.deleteData("AVAX");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Avalanche", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("AVAX", "Avalanche");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Avalanche", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.link_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.link_button_fvr);
                if (dbHelper.isNameExists("LINK")) {
                    dbHelper.deleteData("LINK");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Chainlink", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("LINK", "Chainlink");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Chainlink", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.shib_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.shib_button_fvr);
                if (dbHelper.isNameExists("SHIB")) {
                    dbHelper.deleteData("SHIB");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Shiba Inu", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("SHIB", "Shiba Inu");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Shiba Inu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentView.findViewById(R.id.not_button_fvr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = fragmentView.findViewById(R.id.not_button_fvr);
                if (dbHelper.isNameExists("NOT")) {
                    dbHelper.deleteData("NOT");
                    img.setImageResource(R.drawable.ic_heart_fvr_off);
                    Toast.makeText(MainActivity.this, "Удалено из избранного: Notcoin", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertData("NOT", "Notcoin");
                    img.setImageResource(R.drawable.ic_heart_fvr_on);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное: Notcoin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentViewConverter.findViewById(R.id.convert_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner_from = fragmentViewConverter.findViewById(R.id.spinner_from);
                Spinner spinner_to = fragmentViewConverter.findViewById(R.id.spinner_to);
                String input_sum = ((EditText) fragmentViewConverter.findViewById(R.id.input_sum)).getText().toString();
                TextView result = fragmentViewConverter.findViewById(R.id.convert_result);
                String[] dataForSpinner = {btcSavedData[3], ethSavedData[3], solSavedData[3],
                        xrpSavedData[3], bnbSavedData[3], dogeSavedData[3],
                        tonSavedData[3], adaSavedData[3], trxSavedData[3],
                        pepeSavedData[3], suiSavedData[3], avaxSavedData[3],
                        linkSavedData[3], shibSavedData[3], notSavedData[3],
                        "1"};
                double convert = Double.parseDouble(input_sum) * Double.parseDouble(dataForSpinner[spinner_from.getSelectedItemPosition()].replace(" ", "").replace(",", ".")) / Double.parseDouble(dataForSpinner[spinner_to.getSelectedItemPosition() == 0 ? 15 : spinner_to.getSelectedItemPosition() - 1].replace(",", ".").replace(" ", ""));
                double convertRub = convert * Double.parseDouble(dataForSpinner[spinner_to.getSelectedItemPosition() == 0 ? 15 : spinner_to.getSelectedItemPosition() - 1].replace(",", ".").replace(" ", "")) * Double.parseDouble(rubRate);
                String result_string = new DecimalFormat("#,###.###", setSpace).format(Double.parseDouble(input_sum)) + " " + spinner_from.getSelectedItem() + " = " + new DecimalFormat("#,###.###", setSpace).format(convert) + " " + spinner_to.getSelectedItem() + " = " + new DecimalFormat("#,###", setSpace).format(convertRub) + " RUB";
                result.setText(result_string);
                ((TextView) fragmentViewConverter.findViewById(R.id.convert_info)).setText(String.format("1 USDT = 1 USD = %s RUB", new DecimalFormat("#.##").format(Double.parseDouble(rubRate))));
            }
        });
    }

    public void loadDataFromDB() {
        View fragmentView = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main)
                .getView();
        String[] DBArray = dbHelper.getAllTickers();
        for (String elem : DBArray) {
            ImageView img = fragmentView.findViewById(imgFavDict.get(elem));
            img.setImageResource(R.drawable.ic_heart_fvr_on);
        }
    }

    public void updateFavouritesTable() {
        View fragmentView = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_favourites)
                .getView();

        TableLayout tableFavourites = fragmentView.findViewById(R.id.tableFavourites);
        TextView noFavTitle = fragmentView.findViewById(R.id.no_fav_title);
        int childCount = tableFavourites.getChildCount();
        if (childCount > 1) {
            tableFavourites.removeViews(1, childCount - 1);
        }
        String[] DBArray = dbHelper.getAllTickers();
        String[] DBArrayNames = dbHelper.getAllNames();
        String[] DBArrayNumbers = dbHelper.getAllNumbers();
        if (DBArray.length != 0) {
            tableFavourites.setVisibility(View.VISIBLE);
            noFavTitle.setVisibility(View.GONE);
        } else {
            tableFavourites.setVisibility(View.GONE);
            noFavTitle.setVisibility(View.VISIBLE);
        }
        for (String elem : DBArray) {
            TableRow row = new TableRow(this);

            TextView numberTextView = new TextView(this);
            numberTextView.setText(DBArrayNumbers[Arrays.asList(DBArray).indexOf(elem)] + ".");
            numberTextView.setGravity(Gravity.CENTER);
            numberTextView.setTextSize(16);
            numberTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
            numberTextView.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(numberTextView);

            LinearLayout cellContainer = new LinearLayout(this);
            cellContainer.setOrientation(LinearLayout.HORIZONTAL);
            cellContainer.setGravity(Gravity.CENTER);
            cellContainer.setPadding(8, 8, 8, 8);

            ImageView imageCell = new ImageView(this);
            imageCell.setImageResource(imgDict.get(elem));
            imageCell.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
            cellContainer.addView(imageCell);

            TextView nameTextView = new TextView(this);
            nameTextView.setText(DBArrayNames[Arrays.asList(DBArray).indexOf(elem)]);
            nameTextView.setGravity(Gravity.CENTER);
            nameTextView.setTextSize(16);
            nameTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
            nameTextView.setTypeface(Typeface.DEFAULT_BOLD);
            nameTextView.setPadding(15, 0, 0, 0);
            cellContainer.addView(nameTextView);
            row.addView(cellContainer);

            TextView priceTextView = new TextView(this);
            priceTextView.setText(dataDict.get(elem)[3]);
            priceTextView.setGravity(Gravity.CENTER);
            priceTextView.setTextSize(16);
            priceTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
            priceTextView.setTypeface(Typeface.DEFAULT_BOLD);
            priceTextView.setPadding(8, 8, 8, 8);
            row.addView(priceTextView);

            TextView changeTextView = new TextView(this);
            changeTextView.setText(dataDict.get(elem)[0].replace("USD", ""));
            changeTextView.setGravity(Gravity.CENTER);
            changeTextView.setTextSize(16);
            changeTextView.setTextColor(ContextCompat.getColor(MainActivity.this, dataDict.get(elem)[0].startsWith("-") ? R.color.red : R.color.green));
            changeTextView.setTypeface(Typeface.DEFAULT_BOLD);
            changeTextView.setPadding(8, 8, 8, 8);
            row.addView(changeTextView);

            TextView changePercentTextView = new TextView(this);
            changePercentTextView.setText(dataDict.get(elem)[1].replace("%", ""));
            changePercentTextView.setGravity(Gravity.CENTER);
            changePercentTextView.setTextSize(16);
            changePercentTextView.setTextColor(ContextCompat.getColor(MainActivity.this, dataDict.get(elem)[1].startsWith("-") ? R.color.red : R.color.green));
            changePercentTextView.setTypeface(Typeface.DEFAULT_BOLD);
            changePercentTextView.setPadding(8, 8, 8, 8);
            row.addView(changePercentTextView);

            tableFavourites.addView(row);
        }
    }

    public void updateTop() {
        View fragmentView = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_top)
                .getView();

        ImageView imageFirst = fragmentView.findViewById(R.id.first_image);
        TextView titleFirst = fragmentView.findViewById(R.id.first_title);
        TextView priceFirst = fragmentView.findViewById(R.id.first_price);
        TextView changePercentFirst = fragmentView.findViewById(R.id.first_changePercent);
        TextView changeFirst = fragmentView.findViewById(R.id.first_change);

        ImageView imageSecond = fragmentView.findViewById(R.id.second_image);
        TextView titleSecond = fragmentView.findViewById(R.id.second_title);
        TextView priceSecond = fragmentView.findViewById(R.id.second_price);
        TextView changePercentSecond = fragmentView.findViewById(R.id.second_changePercent);
        TextView changeSecond = fragmentView.findViewById(R.id.second_change);

        ImageView imageThird = fragmentView.findViewById(R.id.third_image);
        TextView titleThird = fragmentView.findViewById(R.id.third_title);
        TextView priceThird = fragmentView.findViewById(R.id.third_price);
        TextView changePercentThird = fragmentView.findViewById(R.id.third_changePercent);
        TextView changeThird = fragmentView.findViewById(R.id.third_change);

        String[] keysArray = dataDict.keySet().toArray(new String[0]);
        List<Double> changePercentArray = new ArrayList<>();
        List<Double> changePercentArrayOriginal = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : dataDict.entrySet()) {
            changePercentArray.add(Double.parseDouble(entry.getValue()[1]
                    .replace("%", "").replace(",", ".")));
            changePercentArrayOriginal.add(Double.parseDouble(entry.getValue()[1]
                    .replace("%", "").replace(",", ".")));
        }
        changePercentArray.sort((o1, o2) -> Double.compare(Math.abs(o2), Math.abs(o1)));

        String firstNumber = keysArray[changePercentArrayOriginal.indexOf(changePercentArray.get(0))];
        String secondNumber = keysArray[changePercentArrayOriginal.indexOf(changePercentArray.get(1))];
        String thirdNumber = keysArray[changePercentArrayOriginal.indexOf(changePercentArray.get(2))];

        imageFirst.setImageResource(imgDict.get(firstNumber));
        titleFirst.setText(" " + nameDict.get(firstNumber) + " ");
        priceFirst.setText(dataDict.get(firstNumber)[3] + " USD");
        changePercentFirst.setText((changePercentArray.get(0) > 0 ? "+" : "") + changePercentArray.get(0) + "%");
        changePercentFirst.setTextColor(ContextCompat.getColor(MainActivity.this, changePercentArray.get(0) < 0 ? R.color.red : R.color.green));
        changeFirst.setText(dataDict.get(firstNumber)[0]);
        changeFirst.setTextColor(ContextCompat.getColor(MainActivity.this, changePercentArray.get(0) < 0 ? R.color.red : R.color.green));

        imageSecond.setImageResource(imgDict.get(secondNumber));
        titleSecond.setText(" " + nameDict.get(secondNumber) + " ");
        priceSecond.setText(dataDict.get(secondNumber)[3] + " USD");
        changePercentSecond.setText((changePercentArray.get(1) > 0 ? "+" : "") + changePercentArray.get(1) + "%");
        changePercentSecond.setTextColor(ContextCompat.getColor(MainActivity.this, changePercentArray.get(1) < 0 ? R.color.red : R.color.green));
        changeSecond.setText(dataDict.get(secondNumber)[0]);
        changeSecond.setTextColor(ContextCompat.getColor(MainActivity.this, changePercentArray.get(1) < 0 ? R.color.red : R.color.green));

        imageThird.setImageResource(imgDict.get(thirdNumber));
        titleThird.setText(" " + nameDict.get(thirdNumber) + " ");
        priceThird.setText(dataDict.get(thirdNumber)[3] + " USD");
        changePercentThird.setText((changePercentArray.get(2) > 0 ? "+" : "") + changePercentArray.get(2) + "%");
        changePercentThird.setTextColor(ContextCompat.getColor(MainActivity.this, changePercentArray.get(2) < 0 ? R.color.red : R.color.green));
        changeThird.setText(dataDict.get(thirdNumber)[0]);
        changeThird.setTextColor(ContextCompat.getColor(MainActivity.this, changePercentArray.get(2) < 0 ? R.color.red : R.color.green));
    }

    private Handler priceUpdateHandler = new Handler();
    private Runnable priceUpdateRunnable;

    private void startPriceUpdates() {
        stopPriceUpdates();

        priceUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateCryptocurrencyPrices();
                priceUpdateHandler.postDelayed(this, 15000);
            }
        };

        priceUpdateHandler.post(priceUpdateRunnable);
    }

    private void stopPriceUpdates() {
        if (priceUpdateHandler != null && priceUpdateRunnable != null) {
            priceUpdateHandler.removeCallbacks(priceUpdateRunnable);
        }
    }
}
