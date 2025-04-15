package com.zyc.dc.service.module;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ModuleDefine {
	public static long PACK_SEQ_COMMAND_ACROSS=1;

	public final static long COMMAND_FLAG_FROM_SERVER=      (0x01<<0);
	public final static long COMMAND_FLAG_FROM_DEVICE=     (0x01<<1);
	public final static long COMMAND_FLAG_INIT_FROM_SERVER= (0x01<<2);
	public final static long COMMAND_FLAG_INIT_FROM_DEVICE=(0x01<<3);
	public final static long COMMAND_FLAG_LOG_NONE=        (0x01<<4);
	public final static long COMMAND_FLAG_RAW_DATA=     (0x01<<5);

	//web client sends to TCP server
	//[5000-10000)
	public final static long COMMAND_ACROSS_REQ=5000L;
	//TCP server sends to web client
	public final static long COMMAND_ACROSS_RSP=6000L;
	//TCP server responses to device
	public final static long COMMAND_RSP_COMMON=100000L;//indicate that TCP server has received data
	public final static long COMMAND_RSP_CONFIG=100001L;//push config info to device
	public final static long COMMAND_RSP_OPERATE=100002L;//push request info to device
	public final static long COMMAND_RSP_OTA=100003L;
	public final static long COMMAND_RSP_ISSUE=100004L;
	//command, upload data to cloud
	public final static long COMMAND_REQ_CAMERA=0L;
	public final static long COMMAND_REQ_TM7705=1L;
	public final static long COMMAND_REQ_CONFIG=2L;
	public final static long COMMAND_REQ_OPERATE=3L;
	public final static long COMMAND_REQ_MEATA=4L;
	public final static long COMMAND_REQ_GPIO=5L;
	public final static long COMMAND_REQ_UART=6L;
	public final static long COMMAND_REQ_LORA=7L;
	public final static long COMMAND_REQ_BATADC=8L;
	public final static long COMMAND_REQ_FORWARD=9L;
	public final static long COMMAND_REQ_LOG=10L;
	public final static long COMMAND_REQ_AHT20=11L;
	public final static long COMMAND_REQ_SPL06=12L;
	public final static long COMMAND_REQ_RS485=13L;
	//handler
	private final static ModuleHandler handlerGeneral=new ModuleHandlerGeneral();
	private final static ModuleHandler handlerConfig=new ModuleHandlerConfig();
	private final static ModuleHandler handlerMeta=new ModuleHandlerMeta();
	private final static ModuleHandler handler4G=new ModuleHandler4G();
	private final static ModuleHandler handlerCamera=new ModuleHandlerCamera();
	private final static ModuleHandler handlerGpio=new ModuleHandlerGpio();
	private final static ModuleHandler handlerLora=new ModuleHandlerLora();
	private final static ModuleHandler handlerSpi=new ModuleHandlerSpi();
	private final static ModuleHandler handlerTm7705=new ModuleHandlerTm7705();
	private final static ModuleHandler handlerUart=new ModuleHandlerUart();
	private final static ModuleHandler handlerW5500=new ModuleHandlerW5500();
	private final static ModuleHandler handlerWifi=new ModuleHandlerWifi();
	private final static ModuleHandler handlerI2c=new ModuleHandlerI2c();
	private final static ModuleHandler handlerBatAdc=new ModuleHandlerBatAdc();
	private final static ModuleHandler handlerForward=new ModuleHandlerForward();
	private final static ModuleHandler handlerCloudLog=new ModuleHandlerCloudLog();
	private final static ModuleHandler handlerAht20=new ModuleHandlerAht20();
	private final static ModuleHandler handlerSpl06=new ModuleHandlerSpl06();
	private final static ModuleHandler handlerRs485=new ModuleHandlerRs485();
	//module
	public static ModuleType MODULE_GENERAL=new ModuleType(0,null,handlerGeneral,"MODULE_GENERAL","general info");
	public static ModuleType MODULE_CLOUDLOG=new ModuleType(1,COMMAND_REQ_LOG,handlerCloudLog,"MODULE_CLOUDLOG","upload esp log info");
	public static ModuleType MODULE_WIFI=new ModuleType(2,null,handlerWifi, "MODULE_WIFI","a wifi module");
	public static ModuleType MODULE_W5500=new ModuleType(3,null,handlerW5500, "MODULE_W5500","a w5500 module");
	public static ModuleType MODULE_4G=new ModuleType(4,null,handler4G, "MODULE_4G","a 4g module");
	public static ModuleType MODULE_BLE=new ModuleType(5,null,null,"MODULE_BLE","a ble module");
	public static ModuleType MODULE_TM7705=new ModuleType(6, COMMAND_REQ_TM7705,handlerTm7705,"MODULE_TM7705","a tm7705 module");
	public static ModuleType MODULE_CAMERA=new ModuleType(7, COMMAND_REQ_CAMERA,handlerCamera,"MODULE_CAMERA","a camera module");
	public static ModuleType MODULE_LORA=new ModuleType(8, COMMAND_REQ_LORA,handlerLora,"MODULE_LORA","a lora module");
	public static ModuleType MODULE_UART=new ModuleType(9, COMMAND_REQ_UART,handlerUart,"MODULE_UART","a uart module");
	public static ModuleType MODULE_SPI=new ModuleType(10, null,handlerSpi,"MODULE_SPI","a spi module");
	public static ModuleType MODULE_I2C=new ModuleType(11,null,handlerI2c,"MODULE_I2C","a i2c module");
	public static ModuleType MODULE_BATADC=new ModuleType(12,COMMAND_REQ_BATADC,handlerBatAdc,"MODULE_BATADC","adc bat");
	public static ModuleType MODULE_META=new ModuleType(13, COMMAND_REQ_MEATA,handlerMeta,"MODULE_META","meta info");
	public static ModuleType MODULE_GPIO=new ModuleType(14,COMMAND_REQ_GPIO,handlerGpio, "MODULE_GPIO","gpio info");
	public static ModuleType MODULE_CONFIG=new ModuleType(15,COMMAND_REQ_CONFIG,handlerConfig,"MODULE_CONFIG","config info");
	public static ModuleType MODULE_FORWARD=new ModuleType(16,COMMAND_REQ_FORWARD,handlerForward,"MODULE_FORWARD","forward info");
	public static ModuleType MODULE_AHT20=new ModuleType(17,COMMAND_REQ_AHT20,handlerAht20,"MODULE_AHT20","aht20 info");
	public static ModuleType MODULE_SPL06=new ModuleType(18,COMMAND_REQ_SPL06,handlerSpl06,"MODULE_SPL06","spl06 info");
	public static ModuleType MODULE_RS485=new ModuleType(19, COMMAND_REQ_RS485,handlerRs485,"MODULE_RS485","a 485 module");
	public static ModuleType MODULE_MAX=new ModuleType(20, null,null,"MODULE_MAX","dummy max");
	
	public static void init()
	{
		OperateSpec operateSpec=null;
    	//example: operate=1 spi_no=1 command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3
        //OperateSpec operateSpec = new OperateSpec(SPI_INIT,ModuleTypeEnum.MODULE_SPI);
        //operateSpec.addParameter("spi_no", 1).addParameter("command_bits", 1).addParameter("address_bits", 1).addParameter("dummy_bits", 1);
        //operateSpec.addParameter("clock_speed_hz", 4).addParameter("duty_cycle_pos", 2).addParameter("mode", 1).addParameter("cs_ena_posttrans", 1).addParameter("queue_size", 4);        

    	//example: operate=2 gpio_ext_no=8 status=1     //pull up gpio ext 8
		new OperateSpec(2, "SET_GPIO_EXT",MODULE_GPIO,1).newBatch(false).addParameter("gpio_ext_no", 1).addParameter("status", 1);        
        
        //example:
      	//operate=3 spi_no=1 spi_address=0x20 spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send=0x04 data_recv_len=0 command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3
    	//operate=3 spi_no=1 spi_address=0x28 spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send= data_recv_len=1 command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3
    	//operate=3 spi_no=1 spi_address=0x21 spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send=0x04 data_recv_len=0 command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3
    	//operate=3 spi_no=1 spi_address=0x29 spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send= data_recv_len=1
    	//operate=3 spi_no=1 spi_address=0x10 spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send=0x64 data_recv_len=0
    	//operate=3 spi_no=1 spi_address=0x11 spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send=0x64 data_recv_len=0 command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3
        OperateSpec.OperateSpecBatch operateSpecBatch=new OperateSpec(3, "SPI_INOUT",MODULE_SPI,1).newBatch(false).addParameter("spi_no", 1).addParameter("spi_address", 8).addParameter("spi_address_valid", 1).addParameter("spi_command", 2).addParameter("spi_command_valid", 1).addParameter("data_send", 0).addParameter("data_recv_len", 4);      
        operateSpecBatch.addParameter("command_bits", 1).addParameter("address_bits", 1).addParameter("dummy_bits", 1);
        operateSpecBatch.addParameter("clock_speed_hz", 4).addParameter("duty_cycle_pos", 2).addParameter("mode", 1).addParameter("cs_ena_posttrans", 1).addParameter("queue_size", 4);       
        //example: operate=4
        new OperateSpec(4, "SPI_PROPERTY",MODULE_SPI,0);
        
        //example: operate=5
        new OperateSpec(5, "TM7705_PROPERTY",MODULE_TM7705,0);
        
        //example: operate=6
        new OperateSpec(6, "CAMERA_PROPERTY",MODULE_CAMERA,0);
        
        //example: operate=7
        new OperateSpec(7, "GPIO_PROPERTY",MODULE_GPIO,0);
        
    	//example: operate=8 tx_data=0x303132 tx_pin=2 rx_pin=14 baud_rate=115200
        operateSpec=new OperateSpec(8, "UART_TX",MODULE_UART,1);
        operateSpec.newBatch(false).addParameter("tx_data", 0);
        operateSpec.newBatch(true).addParameter("tx_pin", 1).addParameter("rx_pin", 1).addParameter("baud_rate", 4);
        
    	//example: operate=9 gpio_esp_no=21 status=1 pull_up=1 pull_down=0     //pull up gpio ext 8
        new OperateSpec(9, "SET_GPIO_ESP",MODULE_GPIO,1).newBatch(false).addParameter("gpio_esp_no", 1).addParameter("status", 1).addParameter("pull_up", 1).addParameter("pull_down", 1);   
        
    	//example only init: operate=10 flag=1 fre=433000000 tx_output_power=2 bandwidth=1 spreading_factor=10 codingrate=1 preamble_length=8 fix_length_payload_on=0 iq_inversion_on=0 tx_timeout=3000 rx_timeout=3000
        //example only tx: operate=10 flag=2 tx_times=3 tx_interval_min_ms=300 tx_interval_max_ms=2000 tx_data=0x303132
        //example init and tx:operate=10 flag=3 fre=433000000 tx_output_power=2 bandwidth=1 spreading_factor=10 codingrate=1 preamble_length=8 fix_length_payload_on=0 iq_inversion_on=0 tx_timeout=3000 rx_timeout=3000 tx_times=3 tx_interval_min_ms=300 tx_interval_max_ms=2000 tx_data=0x303132
        operateSpec=new OperateSpec(10, "LORA_TX",MODULE_LORA,2);
        operateSpec.newBatch(false).addParameter("flag", 1);
        operateSpec.newBatch(true).addParameter("fre", 4).addParameter("tx_output_power", 1).addParameter("bandwidth", 1).addParameter("spreading_factor", 1).addParameter("codingrate", 1).addParameter("preamble_length", 1).addParameter("fix_length_payload_on", 1).addParameter("iq_inversion_on", 1).addParameter("tx_timeout", 4).addParameter("rx_timeout", 4);
        operateSpec.newBatch(true).addParameter("tx_times", 4).addParameter("tx_interval_min_ms", 4).addParameter("tx_interval_max_ms", 4).addParameter("tx_data", 0);     
        
        //example: operate=21
        new OperateSpec(21, "LORA_PROPERTY",MODULE_LORA,0);
        
	    //example reset: operate=11 clock_speed_hz=100000 addr=0x44 flag=1 tx_data=0x30a2
	    //example config:operate=11 clock_speed_hz=100000 addr=0x44 flag=1 tx_data=0x2c0d
	    //example read:  operate=11 clock_speed_hz=100000 addr=0x44 flag=0 rx_data_len=6
        operateSpec=new OperateSpec(11, "I2C_INOUT",MODULE_I2C,2);
        operateSpec.newBatch(false).addParameter("clock_speed_hz", 4).addParameter("flag", 1).addParameter("addr", 1);
        operateSpec.newBatch(true).addParameter("rx_data_len", 4); 
        operateSpec.newBatch(true).addParameter("tx_data", 0);      
        
        //example: operate=12
        new OperateSpec(12, "MODULE_BAT_ADC",MODULE_BATADC,0);
        
        //example: operate=13
        new OperateSpec(13, "MODULE_META",MODULE_META,0);
        
        //example: operate=14 pin=1 // read pin1
        //example: operate=14 pin=2 // read pin2
        //example: operate=14 pin=3 // read pin1 and pin2
        new OperateSpec(14, "TM7705_READ",MODULE_TM7705,1).newBatch(false).addParameter("pin", 1);
        
        //example: operate=15
        new OperateSpec(15, "MODULE_CAMERA",MODULE_CAMERA,0);
        
        //example: operate=16 is_esp=1 gpio_no=2
        new OperateSpec(16, "GPIO_READ",MODULE_GPIO,1).newBatch(false).addParameter("is_esp", 1).addParameter("gpio_no", 1);
        
        //example: operate=17 hex_input=0x303132
        new OperateSpec(17, "MODULE_FORWARD",MODULE_FORWARD,0).newBatch(false).addParameter("hex_input", 0);
        
        //example: operate=18 
        new OperateSpec(18, "MODULE_CONFIG",MODULE_CONFIG,0);
        
        //example: operate=19
        new OperateSpec(19, "AHT20_PROPERTY",MODULE_AHT20,0);
        //example: operate=20
        new OperateSpec(20, "SPL06_PROPERTY",MODULE_SPL06,0);
        
    	//example: operate=22 tx_data=0x303132 tx_pin=21 rx_pin=2 baud_rate=115200
        operateSpec=new OperateSpec(22, "RS485_TX",MODULE_RS485,1);
        operateSpec.newBatch(false).addParameter("tx_data", 0);
        operateSpec.newBatch(true).addParameter("tx_pin", 1).addParameter("rx_pin", 1).addParameter("baud_rate", 4);
	}
	public static class ModuleType{
	    private Integer id;
	    private Long command;
	    private ModuleHandler handler;
	    private String name;
	    private String description;
	    private static Map<Integer,ModuleType> moduleTypeMap=new ConcurrentHashMap<>();
	    private static Map<Long,ModuleType> commandMap=new ConcurrentHashMap<>();
	    private ModuleType(Integer id, Long command,ModuleHandler handler,String name,String description) {
	        this.id = id;
	        this.command=command;
	        this.handler=handler;
	        this.name = name;
	        this.description=description;
	        moduleTypeMap.put(id, this);
	        if(command!=null)
	        	commandMap.put(command,this);
	        if(handler!=null)
	        	handler.setModuleTypeId(id);
	    }
		public Integer getId() {
			return id;
		}
		public Long getCommand() {
			return command;
		}
		public ModuleHandler getHandler() {
			return handler;
		}
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
		public static Map<Integer,ModuleType> getModuleMap()
		{
			Map<Integer,ModuleType> tempMap=new HashMap<>();
			tempMap.putAll(moduleTypeMap);
			return tempMap;
		}
		public static ModuleType fromId(Integer id)
		{
			return moduleTypeMap.get(id);
		}
		public static ModuleType fromCommand(Long command)
		{
			return commandMap.get(command);
		}
	}
	public static boolean isAcrossCommand(long command)
	{
		if((command>=5000)&&(command<10000))
			return true;
		return false;
	}
}
