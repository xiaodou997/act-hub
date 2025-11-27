package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaodou.model.SysSetting;
import com.xiaodou.mapper.SysSettingMapper;
import com.xiaodou.service.SysSettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统设置表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService {

    // 定义哪些分组是公开的
    private static final List<String> PUBLIC_GROUPS = Arrays.asList("basic", "contact", "wechat");

    @Override
    @Transactional
    @CacheEvict(value = "settings", allEntries = true) // 更新或插入后，清空所有相关缓存
    public void batchUpsert(Map<String, String> settingsMap) {
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            QueryWrapper<SysSetting> wrapper = new QueryWrapper<SysSetting>().eq("setting_key", key);
            SysSetting setting = this.getOne(wrapper);

            if (setting != null) {
                // 更新
                setting.setSettingValue(value);
                this.updateById(setting);
            } else {
                // 插入 (这里仅插入key和value，其他属性如name, group等需要通过管理界面单独设置)
                SysSetting newSetting = new SysSetting();
                newSetting.setSettingKey(key);
                newSetting.setSettingValue(value);
                this.save(newSetting);
            }
        }
    }

    @Override
    @CacheEvict(value = "settings", allEntries = true) // 删除后，清空所有相关缓存
    public void deleteByKeys(List<String> keys) {
        QueryWrapper<SysSetting> wrapper = new QueryWrapper<>();
        wrapper.in("setting_key", keys);
        this.remove(wrapper);
    }

    @Override
    @Cacheable("settings:grouped") // 缓存分组设置
    public Map<String, List<SysSetting>> getAllGrouped() {
        List<SysSetting> allSettings = this.list();
        return allSettings.stream()
                .collect(Collectors.groupingBy(SysSetting::getSettingGroup));
    }

    @Override
    @Cacheable("settings:public") // 缓存公共设置
    public Map<String, String> getPublicSettings() {
        QueryWrapper<SysSetting> wrapper = new QueryWrapper<>();
        wrapper.in("setting_group", PUBLIC_GROUPS);
        List<SysSetting> publicSettings = this.list(wrapper);

        return publicSettings.stream()
                .collect(Collectors.toMap(SysSetting::getSettingKey, SysSetting::getSettingValue));
    }
}
