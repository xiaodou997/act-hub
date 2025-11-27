package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.SystemLog;
import com.xiaodou.model.dto.syslog.SystemLogDTO;
import com.xiaodou.model.dto.syslog.SystemLogQueryDTO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * TLC系统操作日志表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
public interface SystemLogMapper extends BaseMapper<SystemLog> {

    IPage<SystemLogDTO> pageSystemLog(Page<SystemLogDTO> page, @Param("query") SystemLogQueryDTO query);
}
