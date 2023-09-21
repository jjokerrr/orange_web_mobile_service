package com.bupt.common.mobile.controller;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.page.PageMethod;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.exception.MyRuntimeException;
import com.bupt.common.core.object.*;
import com.bupt.common.core.upload.BaseUpDownloader;
import com.bupt.common.core.upload.UpDownloaderFactory;
import com.bupt.common.core.upload.UploadResponseInfo;
import com.bupt.common.core.upload.UploadStoreInfo;
import com.bupt.common.core.util.ContextUtil;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.mobile.config.MobileProperties;
import com.bupt.common.mobile.dto.MobileEntryDto;
import com.bupt.common.mobile.model.MobileEntry;
import com.bupt.common.mobile.model.constant.MobileEntryType;
import com.bupt.common.mobile.service.MobileEntryService;
import com.bupt.common.mobile.vo.MobileEntryVo;
import com.bupt.common.redis.cache.SessionCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 移动端首页显示管理操作控制器类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-mobile.urlPrefix}/mobileEntry")
public class MobileEntryController {

    @Autowired
    private MobileEntryService mobileEntryService;
    @Autowired
    private UpDownloaderFactory upDownloaderFactory;
    @Autowired
    private MobileProperties mobileProperties;
    @Autowired
    private SessionCacheHelper cacheHelper;

    private static final String IMAGE_DATA_FIELD = "imageData";

    /**
     * 新增移动端入口显示管理数据。
     *
     * @param mobileEntryDto   新增对象。
     * @param roleIdListString 逗号分隔的角色Id列表。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(
            @MyRequestBody MobileEntryDto mobileEntryDto, @MyRequestBody String roleIdListString) {
        String errorMessage = MyCommonUtil.getModelValidationError(mobileEntryDto, false);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        MobileEntry mobileEntry = MyModelUtil.copyTo(mobileEntryDto, MobileEntry.class);
        if (mobileEntry.getParentId() != null && !mobileEntry.getEntryType().equals(MobileEntryType.SQUARE)) {
            errorMessage = "数据验证失败，只有九宫格支持父Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        mobileEntry = mobileEntryService.saveNew(mobileEntry, roleIdListString);
        return ResponseResult.success(mobileEntry.getEntryId());
    }

    /**
     * 更新移动端入口显示管理数据。
     *
     * @param mobileEntryDto   更新对象。
     * @param roleIdListString 逗号分隔的角色Id列表。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(
            @MyRequestBody MobileEntryDto mobileEntryDto, @MyRequestBody String roleIdListString) {
        String errorMessage = MyCommonUtil.getModelValidationError(mobileEntryDto, true);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        MobileEntry mobileEntry = MyModelUtil.copyTo(mobileEntryDto, MobileEntry.class);
        ResponseResult<MobileEntry> verifyResult = this.doVerifyAndGet(mobileEntry.getEntryId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        MobileEntry originalMobileEntry = verifyResult.getData();
        if (originalMobileEntry == null) {
            errorMessage = "数据验证失败，当前移动端入口对象并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (mobileEntry.getParentId() != null && !mobileEntry.getEntryType().equals(MobileEntryType.SQUARE)) {
            errorMessage = "数据验证失败，只有九宫格支持父Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!mobileEntryService.update(mobileEntry, originalMobileEntry, roleIdListString)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除移动端首页显示管理数据。
     *
     * @param entryId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long entryId) {
        if (MyCommonUtil.existBlankArgument(entryId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        return this.doDelete(entryId);
    }

    /**
     * 列出符合过滤条件的移动端首页显示管理列表。
     *
     * @param mobileEntryDtoFilter 过滤对象。
     * @param orderParam           排序参数。
     * @param pageParam            分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<MobileEntryVo>> list(
            @MyRequestBody MobileEntryDto mobileEntryDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        MobileEntry mobileEntryFilter = MyModelUtil.copyTo(mobileEntryDtoFilter, MobileEntry.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, MobileEntry.class);
        List<MobileEntry> mobileEntryList =
                mobileEntryService.getMobileEntryListWithRelation(mobileEntryFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(mobileEntryList, MobileEntry.INSTANCE));
    }

    /**
     * 查看指定移动端首页显示管理对象详情。
     *
     * @param entryId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<MobileEntryVo> view(@RequestParam Long entryId) {
        ResponseResult<MobileEntry> verifyResult = this.doVerifyAndGet(entryId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        MobileEntry mobileEntry = verifyResult.getData();
        mobileEntryService.buildRelationForData(mobileEntry, MyRelationParam.full());
        MobileEntryVo mobileEntryVo = MobileEntry.INSTANCE.fromModel(mobileEntry);
        return ResponseResult.success(mobileEntryVo);
    }

    /**
     * 上传图片数据。
     *
     * @param uploadFile 上传图片文件。
     */
    @PostMapping("/uploadImage")
    public void uploadImage(@RequestParam("uploadFile") MultipartFile uploadFile) throws IOException {
        UploadStoreInfo storeInfo = MyModelUtil.getUploadStoreInfo(MobileEntry.class, IMAGE_DATA_FIELD);
        BaseUpDownloader upDownloader = upDownloaderFactory.get(storeInfo.getStoreType());
        UploadResponseInfo responseInfo = upDownloader.doUpload(null,
                mobileProperties.getUploadFileBaseDir(), MobileEntry.class.getSimpleName(), IMAGE_DATA_FIELD, true, uploadFile);
        if (BooleanUtil.isTrue(responseInfo.getUploadFailed())) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN,
                    ResponseResult.error(ErrorCodeEnum.UPLOAD_FAILED, responseInfo.getErrorMessage()));
            return;
        }
        String uploadUri = ContextUtil.getHttpRequest().getRequestURI();
        uploadUri = StrUtil.removeSuffix(uploadUri, "/");
        uploadUri = StrUtil.removeSuffix(uploadUri, "/uploadImage");
        responseInfo.setDownloadUri(uploadUri + "/downloadImage");
        cacheHelper.putSessionUploadFile(responseInfo.getFilename());
        ResponseResult.output(ResponseResult.success(responseInfo));
    }

    /**
     * 下载图片数据。
     *
     * @param filename 文件名。
     * @param response Http 应答对象。
     */
    @GetMapping("/downloadImage")
    public void downloadImage(String filename, HttpServletResponse response) {
        try {
            UploadStoreInfo storeInfo = MyModelUtil.getUploadStoreInfo(MobileEntry.class, IMAGE_DATA_FIELD);
            BaseUpDownloader upDownloader = upDownloaderFactory.get(storeInfo.getStoreType());
            upDownloader.doDownload(mobileProperties.getUploadFileBaseDir(),
                    MobileEntry.class.getSimpleName(), IMAGE_DATA_FIELD, filename, true, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }
    }

    private ResponseResult<Void> doDelete(Long entryId) {
        String errorMessage;
        // 验证关联Id的数据合法性
        ResponseResult<MobileEntry> verifyResult = this.doVerifyAndGet(entryId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        try {
            if (!mobileEntryService.remove(entryId)) {
                errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
            }
        } catch (MyRuntimeException e) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, e.getMessage());
        }
        return ResponseResult.success();
    }

    private ResponseResult<MobileEntry> doVerifyAndGet(Long entryId) {
        if (MyCommonUtil.existBlankArgument(entryId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        // 验证关联Id的数据合法性
        MobileEntry mobileEntry = mobileEntryService.getById(entryId);
        return mobileEntry == null
                ? ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST) : ResponseResult.success(mobileEntry);
    }
}
