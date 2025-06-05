package com.example.cryptorates;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link converter#newInstance} factory method to
 * create an instance of this fragment.
 */
public class converter extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public converter() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment converter.
     */
    // TODO: Rename and change types and number of parameters
    public static converter newInstance(String param1, String param2) {
        converter fragment = new converter();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_converter, container, false);
    }

    public void setupSpinner(Spinner spinner, String type_spinner) {
        String[] items;
        Integer[] icons;
        if (type_spinner.equals("spinner_from")) {
            items = new String[]{"BTC", "ETH", "SOL", "XRP", "BNB", "DOGE", "TON", "ADA", "TRX", "PEPE", "SUI", "AVAX", "LINK", "SHIB", "NOT", "USDT"};
            icons = new Integer[]{R.drawable.btc_logo, R.drawable.eth_logo, R.drawable.sol_logo,
                    R.drawable.xrp_logo, R.drawable.bnb_logo, R.drawable.doge_logo,
                    R.drawable.ton_logo, R.drawable.ada_logo, R.drawable.trx_logo,
                    R.drawable.pepe_logo, R.drawable.sui_logo, R.drawable.avax_logo,
                    R.drawable.link_logo, R.drawable.shib_logo, R.drawable.not_logo,
                    R.drawable.usdt_logo};
        } else {
            items = new String[]{"USDT", "BTC", "ETH", "SOL", "XRP", "BNB", "DOGE", "TON", "ADA", "TRX", "PEPE", "SUI", "AVAX", "LINK", "SHIB", "NOT"};
            icons = new Integer[]{R.drawable.usdt_logo, R.drawable.btc_logo, R.drawable.eth_logo,
                    R.drawable.sol_logo, R.drawable.xrp_logo, R.drawable.bnb_logo,
                    R.drawable.doge_logo, R.drawable.ton_logo, R.drawable.ada_logo,
                    R.drawable.trx_logo, R.drawable.pepe_logo, R.drawable.sui_logo,
                    R.drawable.avax_logo, R.drawable.link_logo, R.drawable.shib_logo,
                    R.drawable.not_logo};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.item_layout, R.id.text, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                setIcon(view, position);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                setIcon(view, position);
                return view;
            }

            private void setIcon(View view, int position) {
                ImageView icon = view.findViewById(R.id.icon);
                icon.setImageResource(icons[position]);
            }
        };

        spinner.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Spinner spinner_from = view.findViewById(R.id.spinner_from);
        Spinner spinner_to = view.findViewById(R.id.spinner_to);

        setupSpinner(spinner_from, "spinner_from");
        setupSpinner(spinner_to, "spinner_to");
    }
}