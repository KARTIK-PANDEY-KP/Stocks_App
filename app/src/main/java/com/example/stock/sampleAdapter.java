package com.example.stock;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Objects;

class sampleAdpater extends FragmentStateAdapter {
    private String ticker;

    public sampleAdpater(@NonNull FragmentActivity fragmentActivity, String ticker) {
        super(fragmentActivity);
        this.ticker = ticker;
    }

    public sampleAdpater(@NonNull Fragment fragment) {
        super(fragment);
    }

    public sampleAdpater(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(Objects.equals(ticker, "NVDA")){
            if(position == 0) {
                return new BlankFragment("file:///android_asset/indexn.html");
            }
            else{
                return new BlankFragment("file:///android_asset/candelsticn.html");

            }
        }
        if(Objects.equals(ticker, "AAPL")){
            if(position == 0) {
                return new BlankFragment("file:///android_asset/index.html");
            }
            else{
                return new BlankFragment("file:///android_asset/candelstic.html");

            }
        }if(Objects.equals(ticker, "QCOM")){
            if(position == 0) {
                return new BlankFragment("file:///android_asset/indexq.html");
            }
            else{
                return new BlankFragment("file:///android_asset/candelsticq.html");

            }
        }
        return new BlankFragment("https://saviour.storage.googleapis.com/index.html");

    }

    @Override
    public int getItemCount() {
        return 2;
    }
}