package com.jy.study.system.service.impl;

import com.jy.study.system.domain.SysMemberRecharge;
import com.jy.study.system.mapper.SysMemberRechargeMapper;
import com.jy.study.system.service.ISysMemberRechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysMemberRechargeServiceImpl implements ISysMemberRechargeService {
    @Autowired
    private SysMemberRechargeMapper sysMemberRechargeMapper;

    @Override
    public int insertSysMemberRecharge(SysMemberRecharge recharge) {
        return sysMemberRechargeMapper.insertSysMemberRecharge(recharge);
    }

    @Override
    public List<SysMemberRecharge> selectByUserId(Long userId) {
        return sysMemberRechargeMapper.selectByUserId(userId);
    }
}

