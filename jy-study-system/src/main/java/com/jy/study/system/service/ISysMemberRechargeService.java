package com.jy.study.system.service;

import com.jy.study.system.domain.SysMemberRecharge;

import java.util.List;

public interface ISysMemberRechargeService {
    int insertSysMemberRecharge(SysMemberRecharge recharge);

    List<SysMemberRecharge> selectByUserId(Long userId);
}

