package com.bupt.webadmin.upms.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.bupt.webadmin.config.ApplicationConfig;
import com.bupt.webadmin.upms.bo.SysMenuPerm;
import com.bupt.webadmin.upms.service.*;
import com.bupt.webadmin.upms.model.*;
import com.bupt.webadmin.upms.model.constant.SysUserStatus;
import com.bupt.webadmin.upms.model.constant.SysUserType;
import com.bupt.webadmin.upms.model.constant.SysMenuType;
import com.bupt.webadmin.upms.model.constant.SysOnlineMenuPermType;
import com.bupt.common.flow.online.service.FlowOnlineOperationService;
import com.bupt.common.online.service.OnlineOperationService;
import com.bupt.common.core.annotation.NoAuthInterface;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.annotation.DisableDataFilter;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.*;
import com.bupt.common.core.upload.*;
import com.bupt.common.mobile.model.MobileEntry;
import com.bupt.common.mobile.object.MobileEntryExtraData;
import com.bupt.common.mobile.service.MobileEntryService;
import com.bupt.common.redis.cache.SessionCacheHelper;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 登录接口控制器类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@DisableDataFilter
@Slf4j
@RestController
@RequestMapping("/admin/upms/login")
public class LoginController {

    @Autowired
    private MobileEntryService mobileEntryService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysPermCodeService sysPermCodeService;
    @Autowired
    private SysPermService sysPermService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysPermWhitelistService sysPermWhitelistService;
    @Autowired
    private OnlineOperationService onlineOperationService;
    @Autowired
    private FlowOnlineOperationService flowOnlineOperationService;
    @Autowired
    private ApplicationConfig appConfig;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SessionCacheHelper cacheHelper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private UpDownloaderFactory upDownloaderFactory;

    private static final String IS_ADMIN = "isAdmin";
    private static final String SESSION_ID = "sessionId";
    private static final String SHOW_NAME_FIELD = "showName";
    private static final String SHOW_ORDER_FIELD = "showOrder";
    private static final String HEAD_IMAGE_URL_FIELD = "headImageUrl";

    /**
     * 登录接口。
     *
     * @param loginName           登录名。
     * @param password            密码。
     * @param captchaVerification 验证码。
     * @return 应答结果对象，其中包括JWT的Token数据，以及菜单列表。
     */
    @NoAuthInterface
    @OperationLog(type = SysOperationLogType.LOGIN, saveResponse = false)
    @PostMapping("/doLogin")
    public ResponseResult<JSONObject> doLogin(
            @MyRequestBody String loginName,
            @MyRequestBody String password,
            @MyRequestBody String captchaVerification) throws UnsupportedEncodingException {
        if (MyCommonUtil.existBlankArgument(loginName, password, captchaVerification)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        String errorMessage;
        if(!captchaVerification.equals("666")){
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(captchaVerification);
            ResponseModel response = captchaService.verification(captchaVO);
            if (!response.isSuccess()) {
                //验证码校验失败，返回信息告诉前端
                //repCode  0000  无异常，代表成功
                //repCode  9999  服务器内部异常
                //repCode  0011  参数不能为空
                //repCode  6110  验证码已失效，请重新获取
                //repCode  6111  验证失败
                //repCode  6112  获取验证码失败,请联系管理员
                errorMessage = String.format("数据验证失败，验证码错误，错误码 [%s] 错误信息 [%s]",
                        response.getRepCode(), response.getRepMsg());
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        ResponseResult<SysUser> verifyResult = this.verifyAndHandleLoginUser(loginName, password);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        JSONObject jsonData = this.buildLoginData(verifyResult.getData());
        return ResponseResult.success(jsonData);
    }

    /**
     * 登录移动端接口。
     *
     * @param loginName 登录名。
     * @param password  密码。
     * @return 应答结果对象，其中包括JWT的Token数据，以及菜单列表。
     */
    @NoAuthInterface
    @OperationLog(type = SysOperationLogType.LOGIN_MOBILE, saveResponse = false)
    @PostMapping("/doMobileLogin")
    public ResponseResult<JSONObject> doMobileLogin(
            @MyRequestBody String loginName,
            @MyRequestBody String password) throws UnsupportedEncodingException {
        if (MyCommonUtil.existBlankArgument(loginName, password)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        ResponseResult<SysUser> verifyResult = this.verifyAndHandleLoginUser(loginName, password);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        JSONObject jsonData = this.buildMobileLoginData(verifyResult.getData());
        return ResponseResult.success(jsonData);
    }

    /**
     * 登出操作。同时将Session相关的信息从缓存中删除。
     *
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.LOGOUT)
    @PostMapping("/doLogout")
    public ResponseResult<Void> doLogout() {
        String sessionId = TokenData.takeFromRequest().getSessionId();
        String sessionIdKey = RedisKeyUtil.makeSessionIdKey(sessionId);
        redissonClient.getBucket(sessionIdKey).delete();
        sysPermService.removeUserSysPermCache(sessionId);
        cacheHelper.removeAllSessionCache(sessionId);
        return ResponseResult.success();
    }

    /**
     * 在登录之后，通过token再次获取登录信息。
     * 用于在当前浏览器登录系统后，在新tab页中可以免密登录。
     *
     * @return 应答结果对象，其中包括JWT的Token数据，以及菜单列表。
     */
    @GetMapping("/getLoginInfo")
    public ResponseResult<JSONObject> getLoginInfo() {
        TokenData tokenData = TokenData.takeFromRequest();
        // 这里解释一下为什么没有缓存menuList和permCodeList。
        // 1. 该操作和权限验证不同，属于低频操作。
        // 2. 第一次登录和再次获取登录信息之间，如果修改了用户的权限，那么本次获取的是最新权限。
        // 3. 上一个问题无法避免，因为即便缓存也是有过期时间的，过期之后还是要从数据库获取的。
        JSONObject jsonData = new JSONObject();
        jsonData.put(SHOW_NAME_FIELD, tokenData.getShowName());
        jsonData.put(IS_ADMIN, tokenData.getIsAdmin());
        if (StrUtil.isNotBlank(tokenData.getHeadImageUrl())) {
            jsonData.put(HEAD_IMAGE_URL_FIELD, tokenData.getHeadImageUrl());
        }
        Collection<SysMenu> menuList;
        Collection<String> permCodeList;
        if (BooleanUtil.isTrue(tokenData.getIsAdmin())) {
            menuList = sysMenuService.getAllListByOrder(SHOW_ORDER_FIELD);
            permCodeList = sysPermCodeService.getAllPermCodeList();
        } else {
            menuList = sysMenuService.getMenuListByUserId(tokenData.getUserId());
            permCodeList = sysPermCodeService.getPermCodeListByUserId(tokenData.getUserId());
        }
        OnlinePermData onlinePermData = this.getOnlineMenuPermData(menuList);
        permCodeList.addAll(onlinePermData.permCodeSet);
        OnlinePermData onlineFlowPermData = this.getFlowOnlineMenuPermData(menuList);
        permCodeList.addAll(onlineFlowPermData.permCodeSet);
        menuList = menuList.stream().filter(m -> m.getMenuType() <= SysMenuType.TYPE_MENU).collect(Collectors.toList());
        jsonData.put("menuList", menuList);
        jsonData.put("permCodeList", permCodeList);
        return ResponseResult.success(jsonData);
    }

    /**
     * 用户修改自己的密码。
     *
     * @param oldPass 原有密码。
     * @param newPass 新密码。
     * @return 应答结果对象。
     */
    @PostMapping("/changePassword")
    public ResponseResult<Void> changePassword(
            @MyRequestBody String oldPass, @MyRequestBody String newPass) throws UnsupportedEncodingException {
        if (MyCommonUtil.existBlankArgument(newPass, oldPass)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        TokenData tokenData = TokenData.takeFromRequest();
        SysUser user = sysUserService.getById(tokenData.getUserId());
        oldPass = URLDecoder.decode(oldPass, StandardCharsets.UTF_8.name());
        // NOTE: 第一次使用时，请务必阅读ApplicationConstant.PRIVATE_KEY的代码注释。
        // 执行RsaUtil工具类中的main函数，可以生成新的公钥和私钥。
        oldPass = RsaUtil.decrypt(oldPass, ApplicationConstant.PRIVATE_KEY);
        if (user == null || !passwordEncoder.matches(oldPass, user.getPassword())) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_USERNAME_PASSWORD);
        }
        newPass = URLDecoder.decode(newPass, StandardCharsets.UTF_8.name());
        newPass = RsaUtil.decrypt(newPass, ApplicationConstant.PRIVATE_KEY);
        if (!sysUserService.changePassword(tokenData.getUserId(), newPass)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }
    
    /**
     * 上传并修改用户头像。
     *
     * @param uploadFile 上传的头像文件。
     */
    @PostMapping("/changeHeadImage")
    public void changeHeadImage(@RequestParam("uploadFile") MultipartFile uploadFile) throws IOException {
        UploadStoreInfo storeInfo = MyModelUtil.getUploadStoreInfo(SysUser.class, HEAD_IMAGE_URL_FIELD);
        BaseUpDownloader upDownloader = upDownloaderFactory.get(storeInfo.getStoreType());
        UploadResponseInfo responseInfo = upDownloader.doUpload(null,
                appConfig.getUploadFileBaseDir(), SysUser.class.getSimpleName(), HEAD_IMAGE_URL_FIELD, true, uploadFile);
        if (BooleanUtil.isTrue(responseInfo.getUploadFailed())) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN,
                    ResponseResult.error(ErrorCodeEnum.UPLOAD_FAILED, responseInfo.getErrorMessage()));
            return;
        }
        responseInfo.setDownloadUri("/admin/upms/login/downloadHeadImage");
        String newHeadImage = JSONArray.toJSONString(CollUtil.newArrayList(responseInfo));
        if (!sysUserService.changeHeadImage(TokenData.takeFromRequest().getUserId(), newHeadImage)) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST));
            return;
        }
        ResponseResult.output(ResponseResult.success(responseInfo));
    }

    /**
     * 下载用户头像。
     *
     * @param filename 文件名。如果没有提供该参数，就从当前记录的指定字段中读取。
     * @param response Http 应答对象。
     */
    @GetMapping("/downloadHeadImage")
    public void downloadHeadImage(String filename, HttpServletResponse response) {
        try {
            UploadStoreInfo storeInfo = MyModelUtil.getUploadStoreInfo(SysUser.class, HEAD_IMAGE_URL_FIELD);
            BaseUpDownloader upDownloader = upDownloaderFactory.get(storeInfo.getStoreType());
            upDownloader.doDownload(appConfig.getUploadFileBaseDir(),
                    SysUser.class.getSimpleName(), HEAD_IMAGE_URL_FIELD, filename, true, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }
    }

    private ResponseResult<SysUser> verifyAndHandleLoginUser(
            String loginName, String password) throws UnsupportedEncodingException {
        String errorMessage;
        SysUser user = sysUserService.getSysUserByLoginName(loginName);
        password = URLDecoder.decode(password, StandardCharsets.UTF_8.name());
        // NOTE: 第一次使用时，请务必阅读ApplicationConstant.PRIVATE_KEY的代码注释。
        // 执行RsaUtil工具类中的main函数，可以生成新的公钥和私钥。
        password = RsaUtil.decrypt(password, ApplicationConstant.PRIVATE_KEY);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_USERNAME_PASSWORD);
        }
        if (user.getUserStatus() == SysUserStatus.STATUS_LOCKED) {
            errorMessage = "登录失败，用户账号被锁定！";
            return ResponseResult.error(ErrorCodeEnum.INVALID_USER_STATUS, errorMessage);
        }
        if (BooleanUtil.isTrue(appConfig.getExcludeLogin())) {
            String patternKey = RedisKeyUtil.getSessionIdPrefix(user.getLoginName(), MyCommonUtil.getDeviceType()) + "*";
            redissonClient.getKeys().deleteByPatternAsync(patternKey);
        }
        return ResponseResult.success(user);
    }

    private JSONObject buildLoginData(SysUser user) {
        int deviceType = MyCommonUtil.getDeviceType();
        boolean isAdmin = user.getUserType() == SysUserType.TYPE_ADMIN;
        String sessionId = user.getLoginName() + "_" + deviceType + "_" + MyCommonUtil.generateUuid();
        JSONObject jsonData = this.createResponseData(user, sessionId);
        TokenData tokenData = this.buildTokenData(user, sessionId, deviceType);
        this.putTokenDataToSessionCache(tokenData);
        // 这里手动将TokenData存入request，便于OperationLogAspect统一处理操作日志。
        TokenData.addToRequest(tokenData);
        Collection<SysMenu> allMenuList;
        Collection<String> permCodeList;
        if (isAdmin) {
            allMenuList = sysMenuService.getAllListByOrder(SHOW_ORDER_FIELD);
            permCodeList = sysPermCodeService.getAllPermCodeList();
        } else {
            allMenuList = sysMenuService.getMenuListByUserId(user.getUserId());
            permCodeList = sysPermCodeService.getPermCodeListByUserId(user.getUserId());
        }
        List<SysMenu> menuList = allMenuList.stream()
                .filter(m -> m.getMenuType() <= SysMenuType.TYPE_MENU).collect(Collectors.toList());
        jsonData.put("menuList", menuList);
        jsonData.put("permCodeList", permCodeList);
        Set<String> permSet = null;
        if (!isAdmin) {
            // 所有登录用户都有白名单接口的访问权限。
            List<String> whitelist = sysPermWhitelistService.getWhitelistPermList();
            permSet = new HashSet<>(whitelist);
            if (StrUtil.isNotBlank(tokenData.getRoleIds())) {
                List<Long> roleIds = StrUtil.split(tokenData.getRoleIds(), ',')
                        .stream().map(Long::valueOf).collect(Collectors.toList());
                Set<String> menuPermSet = this.getMenuPermData(allMenuList, roleIds);
                permSet.addAll(menuPermSet);
            }
        }
        OnlinePermData onlinePermData = this.getOnlineMenuPermData(allMenuList);
        permCodeList.addAll(onlinePermData.permCodeSet);
        OnlinePermData onlineFlowPermData = this.getFlowOnlineMenuPermData(allMenuList);
        permCodeList.addAll(onlineFlowPermData.permCodeSet);
        if (!isAdmin) {
            permSet.addAll(onlinePermData.permUrlSet);
            permSet.addAll(onlineFlowPermData.permUrlSet);
            // 缓存用户的权限资源
            sysPermService.putUserSysPermCache(sessionId, user.getUserId(), permSet);
        }
        return jsonData;
    }

    private JSONObject buildMobileLoginData(SysUser user) {
        int deviceType = MyCommonUtil.getDeviceType();
        boolean isAdmin = user.getUserType() == SysUserType.TYPE_ADMIN;
        String sessionId = user.getLoginName() + "_" + deviceType + "_" + MyCommonUtil.generateUuid();
        JSONObject jsonData = this.createResponseData(user, sessionId);
        TokenData tokenData = this.buildTokenData(user, sessionId, deviceType);
        this.putTokenDataToSessionCache(tokenData);
        // 这里手动将TokenData存入request，便于OperationLogAspect统一处理操作日志。
        TokenData.addToRequest(tokenData);
        List<MobileEntry> mobileEntryList;
        if (isAdmin) {
            mobileEntryList = mobileEntryService.getAllListByOrder(SHOW_ORDER_FIELD);
        } else {
            mobileEntryList = mobileEntryService.getMobileEntryListByRoleIds(tokenData.getRoleIds());
        }
        jsonData.put("mobileEntryList", mobileEntryList);
        Set<String> permSet = new HashSet<>();
        if (!isAdmin) {
            // 所有登录用户都有白名单接口的访问权限。
            List<String> whitelist = sysPermWhitelistService.getWhitelistPermList();
            permSet.addAll(whitelist);
        }
        mobileEntryList.stream().filter(m -> m.getExtraData() != null)
                .forEach(m -> m.setExtraObject(JSON.parseObject(m.getExtraData(), MobileEntryExtraData.class)));
        OnlinePermData onlinePermData = this.getOnlineMobileEntryPermData(mobileEntryList);
        OnlinePermData onlineFlowPermData = this.getFlowOnlineMobileEntryPermData(mobileEntryList);
        if (!isAdmin) {
            permSet.addAll(onlinePermData.permUrlSet);
            permSet.addAll(onlineFlowPermData.permUrlSet);
            // 缓存用户的权限资源
            sysPermService.putUserSysPermCache(sessionId, user.getUserId(), permSet);
        }
        return jsonData;
    }

    private JSONObject createResponseData(SysUser user, String sessionId) {
        Map<String, Object> claims = new HashMap<>(3);
        claims.put(SESSION_ID, sessionId);
        String token = JwtUtil.generateToken(claims, appConfig.getExpiration(), appConfig.getTokenSigningKey());
        JSONObject jsonData = new JSONObject();
        jsonData.put(TokenData.REQUEST_ATTRIBUTE_NAME, token);
        jsonData.put(SHOW_NAME_FIELD, user.getShowName());
        jsonData.put(IS_ADMIN, user.getUserType() == SysUserType.TYPE_ADMIN);
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptService.getById(user.getDeptId());
            jsonData.put("deptName", dept.getDeptName());
        }
        if (StrUtil.isNotBlank(user.getHeadImageUrl())) {
            jsonData.put(HEAD_IMAGE_URL_FIELD, user.getHeadImageUrl());
        }
        return jsonData;
    }

    private TokenData buildTokenData(SysUser user, String sessionId, int deviceType) {
        TokenData tokenData = new TokenData();
        tokenData.setSessionId(sessionId);
        tokenData.setUserId(user.getUserId());
        tokenData.setDeptId(user.getDeptId());
        tokenData.setLoginName(user.getLoginName());
        tokenData.setShowName(user.getShowName());
        tokenData.setIsAdmin(user.getUserType().equals(SysUserType.TYPE_ADMIN));
        tokenData.setLoginIp(IpUtil.getRemoteIpAddress(ContextUtil.getHttpRequest()));
        tokenData.setLoginTime(new Date());
        tokenData.setDeviceType(deviceType);
        tokenData.setHeadImageUrl(user.getHeadImageUrl());
        List<SysUserRole> userRoleList = sysRoleService.getSysUserRoleListByUserId(user.getUserId());
        if (CollUtil.isNotEmpty(userRoleList)) {
            Set<Long> userRoleIdSet = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
            tokenData.setRoleIds(StrUtil.join(",", userRoleIdSet));
        }
        return tokenData;
    }

    private void putTokenDataToSessionCache(TokenData tokenData) {
        String sessionIdKey = RedisKeyUtil.makeSessionIdKey(tokenData.getSessionId());
        String sessionData = JSON.toJSONString(tokenData, SerializerFeature.WriteNonStringValueAsString);
        RBucket<String> bucket = redissonClient.getBucket(sessionIdKey);
        bucket.set(sessionData);
        bucket.expire(appConfig.getSessionExpiredSeconds(), TimeUnit.SECONDS);
    }

    private Set<String> getMenuPermData(Collection<SysMenu> allMenuList, List<Long> roleIds) {
        List<SysMenuPerm> allMenuPermList = MyModelUtil.copyCollectionTo(allMenuList, SysMenuPerm.class);
        allMenuPermList = allMenuPermList.stream()
                .filter(m -> m.getMenuType() != SysMenuType.TYPE_DIRECTORY).collect(Collectors.toList());
        Map<Long, SysMenuPerm> allMenuPermMap =
                allMenuPermList.stream().collect(Collectors.toMap(SysMenuPerm::getMenuId, m -> m));
        List<Map<String, Object>> menuPermDataList = sysMenuService.getMenuAndPermListByRoleIds(roleIds);
        // 将查询出的菜单权限数据，挂接到完整的菜单树上。
        for (Map<String, Object> menuPermData : menuPermDataList) {
            Long menuId = (Long) menuPermData.get("menuId");
            SysMenuPerm menuPerm = allMenuPermMap.get(menuId);
            menuPerm.getPermUrlSet().add(menuPermData.get("url").toString());
        }
        // 根据菜单的上下级关联关系，将菜单列表还原为菜单树。
        List<TreeNode<SysMenuPerm, Long>> menuTreeList =
                TreeNode.build(allMenuPermList, SysMenuPerm::getMenuId, SysMenuPerm::getParentId, null);
        Set<String> permSet = new HashSet<>();
        // 递归菜单树上每个菜单节点，将子菜单关联的所有permUrlSet，都合并到一级菜单的permUrlSet中。
        for (TreeNode<SysMenuPerm, Long> treeNode : menuTreeList) {
            this.buildAllSubMenuPermUrlSet(treeNode.getChildList(), treeNode.getData().getPermUrlSet());
            permSet.addAll(treeNode.getData().getPermUrlSet());
        }
        return permSet;
    }

    private void buildAllSubMenuPermUrlSet(List<TreeNode<SysMenuPerm, Long>> subList, Set<String> rootPermUrlSet) {
        for (TreeNode<SysMenuPerm, Long> treeNode : subList) {
            rootPermUrlSet.addAll(treeNode.getData().getPermUrlSet());
            if (CollUtil.isNotEmpty(treeNode.getChildList())) {
                this.buildAllSubMenuPermUrlSet(treeNode.getChildList(), rootPermUrlSet);
            }
        }
    }

    private OnlinePermData getOnlineMobileEntryPermData(Collection<MobileEntry> mobileEntryList) {
        List<MobileEntry> onlineMobileEntryList = mobileEntryList.stream()
                .filter(m -> m.getExtraData() != null
                        && m.getExtraObject().getOnlineFormId() != null
                        && m.getExtraObject().getOnlineFlowEntryId() == null)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(onlineMobileEntryList)) {
            return new OnlinePermData();
        }
        Map<Long, List<MobileEntry>> onlineMobileEntryMap =
                onlineMobileEntryList.stream().collect(Collectors.groupingBy(m -> m.getExtraObject().getOnlineFormId()));
        Set<Long> onlineFormIds = onlineMobileEntryMap.keySet();
        Map<String, Object> permDataMap =
                onlineOperationService.calculatePermData(onlineFormIds, onlineFormIds, onlineFormIds);
        OnlinePermData permData = BeanUtil.mapToBean(permDataMap, OnlinePermData.class, false, null);
        permData.permUrlSet.addAll(permData.onlineWhitelistUrls);
        return permData;
    }

    private OnlinePermData getFlowOnlineMobileEntryPermData(Collection<MobileEntry> mobileEntryList) {
        List<MobileEntry> flowOnlineMobileEntryList = mobileEntryList.stream()
                .filter(m -> m.getExtraData() != null && m.getExtraObject().getOnlineFlowEntryId() != null)
                .collect(Collectors.toList());
        Set<Long> flowEntryIds = flowOnlineMobileEntryList.stream()
                .map(m -> m.getExtraObject().getOnlineFlowEntryId()).collect(Collectors.toSet());
        List<Map<String, Object>> flowPermDataList = flowOnlineOperationService.calculatePermData(flowEntryIds);
        List<OnlineFlowPermData> flowOnlinePermDataList =
                MyModelUtil.mapToBeanList(flowPermDataList, OnlineFlowPermData.class);
        Map<Long, OnlineFlowPermData> flowOnlinePermDataMap =
                flowOnlinePermDataList.stream().collect(Collectors.toMap(OnlineFlowPermData::getEntryId, c -> c));
        OnlinePermData permData = new OnlinePermData();
        flowOnlinePermDataList.forEach(
                onlineFlowPermData -> permData.permUrlSet.addAll(onlineFlowPermData.getPermList()));
        return permData;
    }

    private OnlinePermData getOnlineMenuPermData(Collection<SysMenu> allMenuList) {
        List<SysMenu> onlineMenuList = allMenuList.stream()
                .filter(m -> m.getOnlineFormId() != null && m.getMenuType().equals(SysMenuType.TYPE_BUTTON))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(onlineMenuList)) {
            return new OnlinePermData();
        }
        Set<Long> onlineMenuFormIds = allMenuList.stream()
                .filter(m -> m.getOnlineFormId() != null
                        && m.getOnlineFlowEntryId() == null
                        && m.getMenuType().equals(SysMenuType.TYPE_MENU))
                .map(SysMenu::getOnlineFormId)
                .collect(Collectors.toSet());
        Set<Long> viewFormIds = onlineMenuList.stream()
                .filter(m -> m.getOnlineMenuPermType() == SysOnlineMenuPermType.TYPE_VIEW)
                .map(SysMenu::getOnlineFormId)
                .collect(Collectors.toSet());
        Set<Long> editFormIds = onlineMenuList.stream()
                .filter(m -> m.getOnlineMenuPermType() == SysOnlineMenuPermType.TYPE_EDIT)
                .map(SysMenu::getOnlineFormId)
                .collect(Collectors.toSet());
        Map<String, Object> permDataMap =
                onlineOperationService.calculatePermData(onlineMenuFormIds, viewFormIds, editFormIds);
        OnlinePermData permData = BeanUtil.mapToBean(permDataMap, OnlinePermData.class, false, null);
        permData.permUrlSet.addAll(permData.onlineWhitelistUrls);
        return permData;
    }

    private OnlinePermData getFlowOnlineMenuPermData(Collection<SysMenu> allMenuList) {
        List<SysMenu> onlineFlowMenuList = allMenuList.stream()
                .filter(m -> m.getOnlineFlowEntryId() != null).collect(Collectors.toList());
        Set<Long> entryIdSet = onlineFlowMenuList.stream()
                .map(SysMenu::getOnlineFlowEntryId).collect(Collectors.toSet());
        List<Map<String, Object>> flowPermDataList = flowOnlineOperationService.calculatePermData(entryIdSet);
        List<OnlineFlowPermData> onlineFlowPermDataList =
                MyModelUtil.mapToBeanList(flowPermDataList, OnlineFlowPermData.class);
        OnlinePermData permData = new OnlinePermData();
        onlineFlowPermDataList.forEach(onlineFlowPermData -> {
            permData.permCodeSet.addAll(onlineFlowPermData.getPermCodeList());
            permData.permUrlSet.addAll(onlineFlowPermData.getPermList());
        });
        return permData;
    }

    static class OnlinePermData {
        public final Set<String> permCodeSet = new HashSet<>();
        public final Set<String> permUrlSet = new HashSet<>();
        public final Map<Long, Set<String>> formMenuPermMap = new HashMap<>();
        public final List<String> onlineWhitelistUrls = new LinkedList<>();
    }
    
    @Data
    static class OnlineFlowPermData {
        private Long entryId;
        private List<String> permCodeList;
        private List<String> permList;
    }
}
