package com.zyc.dc.constant;

public enum ResetEspEnum {
    ESP_RST_UNKNOWN(0, "Reset reason cannot be determined"),
    ESP_RST_POWERON(1, "Reset due to power-on event"),
    ESP_RST_EXT(2, "Reset by external pin (not applicable for ESP32)"),
    ESP_RST_SW(3, "Software reset via esp_restart"),
    ESP_RST_PANIC(4, "Software reset due to exception/panic"),
    ESP_RST_INT_WDT(5, "Reset (software or hardware) due to interrupt watchdog"),
    ESP_RST_TASK_WDT(6, "Reset due to task watchdog"),
    ESP_RST_WDT(7, "Reset due to other watchdogs"),
    ESP_RST_DEEPSLEEP(8, "Reset after exiting deep sleep mode"),
    ESP_RST_BROWNOUT(9, "Brownout reset (software or hardware)"),
    ESP_RST_SDIO(10, "Reset over SDIO"),
    ESP_RST_USB(11, "Reset by USB peripheral"),
    ESP_RST_JTAG(12, "Reset by JTAG"),
    ESP_RST_EFUSE(13, "Reset due to efuse error"),
    ESP_RST_PWR_GLITCH(14, "Reset due to power glitch detected"),
    ESP_RST_CPU_LOCKUP(15, "Reset due to CPU lock up");

    private final int id;
    private final String description;

    ResetEspEnum(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    // 根据 ID 获取对应的名称
    public static String[] getNameById(int id) {
        for (ResetEspEnum reason : ResetEspEnum.values()) {
            if (reason.getId() == id) {
            	String[] temp=new String[2];
            	temp[0]=reason.name();
            	temp[1]=reason.getDescription();
                return temp;
            }
        }
        return null; // 或者返回一个默认值
    }
}
