package com.otakuhuang.springhateoasclientdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author otaku
 * @version 1.0
 * @date 2022/2/24 22:55
 * @description description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeOrder implements Serializable {
    private Long id;
    private String customer;
    private OrderState state;
    private Date createTime;
    private Date updateTime;
}
