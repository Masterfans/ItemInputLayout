package com.jrobot.item;

import androidx.lifecycle.MutableLiveData;

public class Data {
  private MutableLiveData<String> content = new MutableLiveData<>();

  public MutableLiveData<String> getContent() {
    return content;
  }

  public void setContent(MutableLiveData<String> content) {
    this.content = content;
  }
}
