package com.sarnava.mapassignment;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class NameViewModel extends ViewModel {

    // Create a LiveData with a List<String>
    private MutableLiveData<List<String>> mCurrentName;

    public MutableLiveData<List<String>> getCurrentName() {
        if (mCurrentName == null) {
            mCurrentName = new MutableLiveData<List<String>>();
        }
        return mCurrentName;
    }

}