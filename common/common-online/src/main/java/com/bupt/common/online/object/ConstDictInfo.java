package com.bupt.common.online.object;

import lombok.Data;

import java.util.List;

/**
 * 在线表单常量字典的数据结构。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class ConstDictInfo {

    private List<ConstDictData> dictData;

    @Data
    public static class ConstDictData {
        private String type;
        private Object id;
        private String name;
    }
}
