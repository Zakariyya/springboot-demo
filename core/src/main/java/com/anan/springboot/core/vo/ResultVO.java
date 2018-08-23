package com.anan.springboot.core.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * http请求返回的最外层对象
 * @author anan
 * Create on 2018/8/18
 */
//@Data
public class ResultVO<T> implements Serializable {

  private static final long serialVersionUID = -1254223930581160279L;
  /** 状态码. */
  private Integer code;
  /** 提示信息. */
  private String msg;
  /** 具体内容. */
  private T data;

  @Override
  public String toString() {
    return "ResultVO{" +
            "code=" + code +
            ", msg='" + msg + '\'' +
            ", data=" + data +
            '}';
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
