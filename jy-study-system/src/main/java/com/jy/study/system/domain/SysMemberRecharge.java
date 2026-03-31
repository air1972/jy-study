package com.jy.study.system.domain;

import com.jy.study.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员充值记录
 */
public class SysMemberRecharge extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Integer months;
    private BigDecimal amount;
    private String rechargeNo;
    private String payChannel;
    private Date expireBefore;
    private Date expireAfter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRechargeNo() {
        return rechargeNo;
    }

    public void setRechargeNo(String rechargeNo) {
        this.rechargeNo = rechargeNo;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public Date getExpireBefore() {
        return expireBefore;
    }

    public void setExpireBefore(Date expireBefore) {
        this.expireBefore = expireBefore;
    }

    public Date getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(Date expireAfter) {
        this.expireAfter = expireAfter;
    }
}

