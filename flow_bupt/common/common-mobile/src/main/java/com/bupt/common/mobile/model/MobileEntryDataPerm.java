package com.bupt.common.mobile.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

/**
 * 移动端入口和数据权限关联实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@ToString(of = {"entryId"})
@TableName(value = "zz_mobile_entry_data_perm")
public class MobileEntryDataPerm {

    /**
     * 移动度入口Id。
     */
    @TableField(value = "entry_id")
    private Long entryId;

    /**
     * 数据权限Id。
     */
    @TableField(value = "data_perm_id")
    private Long dataPermId;
}
