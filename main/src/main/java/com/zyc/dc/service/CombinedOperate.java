package com.zyc.dc.service;

import com.zyc.dc.constant.ResultType;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.service.module.OperateRequest;

public interface CombinedOperate {
	public ResultType read(DeviceModel deviceModel,OperateExecutor operateExecutor,OperateRequest request);
}
