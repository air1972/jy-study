package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.constant.UserConstants;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.framework.shiro.service.SysPasswordService;
import com.jy.study.system.domain.SysMemberRecharge;
import com.jy.study.system.service.ISysMemberRechargeService;
import com.jy.study.system.service.ISysUserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/web/member")
public class WebMemberController extends BaseController {
    private static final Map<String, PendingWechatOrder> WECHAT_PENDING_ORDER_MAP = new ConcurrentHashMap<>();

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysMemberRechargeService rechargeService;

    @Autowired
    private SysPasswordService passwordService;

    @GetMapping("")
    @RequiresAuthentication
    public String member(ModelMap mmap) {
        SysUser principal = getSysUser();
        SysUser user = userService.selectUserById(principal.getUserId());
        mmap.put("memberUser", user);
        return "web/member";
    }

    @GetMapping("/status")
    @ResponseBody
    @RequiresAuthentication
    public AjaxResult status() {
        SysUser principal = getSysUser();
        SysUser user = userService.selectUserById(principal.getUserId());
        Date now = new Date();
        Date expire = user.getVipExpireTime();
        boolean active = expire != null && expire.after(now);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("active", active);
        data.put("vipExpireTime", expire);
        return AjaxResult.success(data);
    }

    @PostMapping("/recharge")
    @ResponseBody
    @RequiresAuthentication
    public AjaxResult recharge(@RequestParam("months") Integer months) {
        SysUser principal = getSysUser();
        SysUser user = userService.selectUserById(principal.getUserId());
        return doRecharge(user, months, user.getLoginName(), "manual");
    }

    @PostMapping("/wechat/create")
    @ResponseBody
    @RequiresAuthentication
    public AjaxResult wechatCreate(@RequestParam("months") Integer months) {
        if (months == null || (months != 1 && months != 3 && months != 6 && months != 12)) {
            return AjaxResult.error("仅支持 1/3/6/12 个月套餐");
        }
        SysUser principal = getSysUser();
        SysUser user = userService.selectUserById(principal.getUserId());
        String rechargeNo = "WX" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        PendingWechatOrder order = new PendingWechatOrder(user.getUserId(), months, getPlanAmount(months));
        WECHAT_PENDING_ORDER_MAP.put(rechargeNo, order);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("rechargeNo", rechargeNo);
        data.put("amount", order.getAmount());
        data.put("months", months);
        data.put("payUrl", "/web/member/wechat/pay/" + rechargeNo);
        data.put("message", "微信支付单已创建，可支付后确认，也可直接返回");
        return AjaxResult.success("微信支付单创建成功", data);
    }

    @PostMapping("/wechat/confirm")
    @ResponseBody
    @RequiresAuthentication
    public AjaxResult wechatConfirm(@RequestParam("rechargeNo") String rechargeNo) {
        if (StringUtils.isEmpty(rechargeNo)) {
            return AjaxResult.error("充值单号不能为空");
        }
        PendingWechatOrder order = WECHAT_PENDING_ORDER_MAP.get(rechargeNo);
        if (order == null) {
            return AjaxResult.error("充值单不存在或已失效");
        }

        SysUser principal = getSysUser();
        SysUser user = userService.selectUserById(principal.getUserId());
        if (!user.getUserId().equals(order.getUserId())) {
            return AjaxResult.error("该充值单不属于当前用户");
        }

        AjaxResult result = doRecharge(user, order.getMonths(), user.getLoginName(), "wechat_mock", rechargeNo);
        if (result.get("code") != null && Integer.valueOf(0).equals(result.get("code"))) {
            WECHAT_PENDING_ORDER_MAP.remove(rechargeNo);
        }
        return result;
    }

    @GetMapping("/renew")
    public String renewPage() {
        return "web/member-renew";
    }

    @PostMapping("/renew")
    @ResponseBody
    public AjaxResult renewSubmit(@RequestParam("loginName") String loginName,
                                  @RequestParam("password") String password,
                                  @RequestParam("months") Integer months) {
        if (StringUtils.isEmpty(loginName) || StringUtils.isEmpty(password)) {
            return AjaxResult.error("账号或密码不能为空");
        }
        SysUser user = userService.selectUserByLoginName(loginName);
        if (user == null || !UserConstants.REGISTER_USER_TYPE.equals(user.getUserType())) {
            return AjaxResult.error("用户不存在或不是普通会员用户");
        }
        if (!passwordService.matches(user, password)) {
            return AjaxResult.error("账号或密码错误");
        }
        return doRecharge(user, months, loginName, "manual_self");
    }

    @GetMapping("/records")
    @ResponseBody
    @RequiresAuthentication
    public AjaxResult records() {
        SysUser principal = getSysUser();
        List<SysMemberRecharge> list = rechargeService.selectByUserId(principal.getUserId());
        return AjaxResult.success(list);
    }

    private Date plusMonths(Date from, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    private AjaxResult doRecharge(SysUser user, Integer months, String operator, String payChannel) {
        return doRecharge(user, months, operator, payChannel, null);
    }

    private AjaxResult doRecharge(SysUser user, Integer months, String operator, String payChannel, String fixedRechargeNo) {
        if (months == null || (months != 1 && months != 3 && months != 6 && months != 12)) {
            return AjaxResult.error("仅支持 1/3/6/12 个月套餐");
        }
        Date now = new Date();
        Date oldExpire = user.getVipExpireTime();
        Date base = (oldExpire != null && oldExpire.after(now)) ? oldExpire : now;
        Date newExpire = plusMonths(base, months);

        SysUser update = new SysUser();
        update.setUserId(user.getUserId());
        update.setVipExpireTime(newExpire);
        update.setStatus(UserConstants.NORMAL);
        update.setUpdateBy(operator);
        userService.updateUserInfo(update);

        SysMemberRecharge recharge = new SysMemberRecharge();
        recharge.setUserId(user.getUserId());
        recharge.setMonths(months);
        recharge.setAmount(getPlanAmount(months));
        recharge.setRechargeNo(StringUtils.isNotEmpty(fixedRechargeNo) ? fixedRechargeNo : "VIP" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());
        recharge.setPayChannel(payChannel);
        recharge.setExpireBefore(oldExpire);
        recharge.setExpireAfter(newExpire);
        rechargeService.insertSysMemberRecharge(recharge);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("vipExpireTime", newExpire);
        data.put("rechargeNo", recharge.getRechargeNo());
        return AjaxResult.success("充值成功，会员已续费", data);
    }

    private static class PendingWechatOrder {
        private final Long userId;
        private final Integer months;
        private final BigDecimal amount;

        private PendingWechatOrder(Long userId, Integer months, BigDecimal amount) {
            this.userId = userId;
            this.months = months;
            this.amount = amount;
        }

        public Long getUserId() {
            return userId;
        }

        public Integer getMonths() {
            return months;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    private BigDecimal getPlanAmount(int months) {
        switch (months) {
            case 1:
                return new BigDecimal("29.90");
            case 3:
                return new BigDecimal("79.90");
            case 6:
                return new BigDecimal("149.90");
            case 12:
                return new BigDecimal("269.90");
            default:
                return new BigDecimal("0.00");
        }
    }
}
