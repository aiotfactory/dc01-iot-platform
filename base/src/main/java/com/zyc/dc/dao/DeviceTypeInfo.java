package com.zyc.dc.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deviceTypeInfo")
public class DeviceTypeInfo {
    @Id
    private String id;
    @Indexed
    private DeviceModel.DeviceType deviceType;

    public enum SilkPinInfoType{
    	GPIO,
    	GND,
    	VCC
    }
}
