package com.jy.study.system.mapper;

import com.jy.study.system.domain.SysMemberRecharge;

import java.util.List;

public interface SysMemberRechargeMapper {
    int insertSysMemberRecharge(SysMemberRecharge recharge);

    List<SysMemberRecharge> selectByUserId(Long userId);
}

