package com.xiaodou.service;

import com.xiaodou.model.SysSetting;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统设置表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
public interface SysSettingService extends IService<SysSetting> {

    /**
     * 批量更新或插入设置
     *
     * @param settingsMap 键值对 Map
     */
    void batchUpsert(Map<String, String> settingsMap);

    /**
     * 根据 Key 列表批量删除设置
     *
     * @param keys 要删除的 Key 列表
     */
    void deleteByKeys(List<String> keys);

    /**
     * 获取所有设置，并按分组进行归类
     *
     * @return 分组后的设置 Map
     */
    Map<String, List<SysSetting>> getAllGrouped();

    /**
     * 获取所有公开的设置项
     *
     * @return 公开设置的键值对 Map
     */
    Map<String, String> getPublicSettings();
}
