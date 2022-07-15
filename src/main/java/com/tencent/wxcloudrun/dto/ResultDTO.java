package com.tencent.wxcloudrun.dto;

public class ResultDTO {
    private Integer code;
    private String text;

    @Override
    public String toString() {
        return "ResultDTO{" +
                "code=" + code +
                ", text='" + text + '\'' +
                '}';
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
