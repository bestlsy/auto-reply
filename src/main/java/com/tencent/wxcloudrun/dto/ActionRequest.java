package com.tencent.wxcloudrun.dto;

public class ActionRequest {

    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ActionRequest{" +
                "action='" + action + '\'' +
                '}';
    }
}
