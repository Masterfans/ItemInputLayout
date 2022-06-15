package com.jrobot.item;

import androidx.lifecycle.MutableLiveData;

public class Model {
  private MutableLiveData<Data> data = new MutableLiveData<>();

  public MutableLiveData<Data> getData() {
    return data;
  }

  public void setData(MutableLiveData<Data> data) {
    this.data = data;
  }
}
