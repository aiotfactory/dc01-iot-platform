# English: [English](./README.md)

---

# 项目介绍

凭借在嵌入式研发、生产和 IoT 平台建设方面超过 20 年的经验，我们根据众多客户的需求反馈推出了这款开源硬件设备。

新项目启动时，快速验证概念至关重要。因此，我们在一个产品中集成了尽可能多的核心组件，包括各种传感器、联网模块和多种供电方式，并提供了在线编程与烧录等功能，帮助客户以最低成本迅速验证其想法。

此外，本项目还为相应的 IoT 平台提供了一系列丰富的功能和服务。

---

# 功能概览

## 用户管理
- 用户注册：支持新用户的创建与注册。
- 登录管理：提供安全的用户登录机制。

<div align="center">
  <img src="docs/p02.jpg" width="300"/>
  <img src="docs/p03.jpg" width="300"/>
</div>

## 设备管理
- 实时监控设备状态，远程控制设备操作。
- 灵活配置设备参数，适应不同应用场景。

<div align="center">
  <img src="docs/p04.jpg" width="300"/>
  <img src="docs/p05.jpg" width="300"/><br>
  <img src="docs/p06.jpg" width="300"/>
  <img src="docs/p07.jpg" width="300"/>
</div>

## 模块列表

| 模块名称       | 简要介绍                         |
|----------------|----------------------------------|
| 基础模块       | 系统基础信息管理模块             |
| 云端日志模块   | 负责上传设备运行日志到云端       |
| Wi-Fi模块      | 管理Wi-Fi连接功能                |
| W5500模块      | 以太网通信模块                   |
| 4G模块         | 提供4G网络接入支持               |
| TM7705模块     | 高精度ADC采集模块                |
| 摄像头模块     | 支持图像采集和视频流传输         |
| LoRa模块       | 远距离无线通信模块（10km+）      |
| UART模块       | 串口通信接口模块                 |
| SPI模块        | 高速同步串行通信接口模块         |
| I²C模块        | 多设备互联通信总线模块           |
| 电池ADC模块    | 用于电池电压检测                 |
| 元信息模块     | 存储系统元信息和设备描述         |
| GPIO模块       | 管理通用输入输出端口             |
| 配置模块       | 设备配置管理                     |
| 数据转发模块   | 支持数据的灵活处理和分发策略     |
| AHT20模块      | 温湿度传感器模块                 |
| SPL06模块      | 气压传感器模块                   |
| RS485模块      | RS485通信模块                    |
| PIR模块        | 被动红外传感器模块               |
| 热成像模块     | 支持热成像功能                   |
| 超声波模块     | 支持超声波测距功能               |

## OTA升级
支持通过网络对设备固件进行在线升级，确保系统始终保持最新。

<div align="center">
  <img src="docs/p10.jpg" width="300"/>
  <img src="docs/p11.jpg" width="300"/>
</div>

## 在线代码开发
- **代码编译**：提供在线环境用于编写和编译代码。
- **固件烧录**：直接从云端向设备推送并烧录固件。
- **设备日志**：在线查看设备运行日志，便于调试和维护。

<div align="center">
  <img src="docs/p12.jpg" width="300"/>
  <img src="docs/p13.jpg" width="300"/><br>
  <img src="docs/p14.jpg" width="300"/>
  <img src="docs/p15.jpg" width="300"/><br>
  <img src="docs/p16.jpg" width="300"/>
  <img src="docs/p17.jpg" width="300"/>
</div>

## API 接口
对外开放 API 接口，方便第三方应用集成与扩展。

<div align="center">
  <img src="docs/p20.jpg" width="300"/>
  <img src="docs/p21.jpg" width="300"/>
</div>

## 在设备上执行命令
既可以通过 API 又可以通过命令窗口在设备上执行命令。

<div align="center">
  <img src="docs/p22.jpg" width="300"/>
  <img src="docs/p23.jpg" width="300"/>
</div>

## 数据转发
提供多种数据转发方式，支持灵活的数据处理和分发策略。

<div align="center">
  <img src="docs/p18.jpg" width="300"/>
  <img src="docs/p19.jpg" width="300"/>
</div>

## 集成大语言模型（LLM）
集成先进的大语言模型，增强平台的智能交互能力。

<div align="center">
  <img src="docs/p24.jpg" width="300"/>
  <img src="docs/p25.jpg" width="300"/><br>
  <img src="docs/p26.jpg" width="300"/>
  <img src="docs/p27.jpg" width="300"/>
</div>

## 内嵌文档系统
内嵌式文档，方便查阅使用说明和技术资料。

<div align="center">
  <img src="docs/p28.jpg" width="300"/>
  <img src="docs/p29.jpg" width="300"/>
</div>

---

# 安装步骤

> 以下操作建议在 Ubuntu 系统下完成，具备管理员权限（sudo）。

## 准备环境

```bash
sudo apt update && sudo apt upgrade -y
mkdir -p /opt/dc01
```

## 安装 MongoDB

```bash
cd /opt/dc01
wget https://repo.mongodb.org/apt/ubuntu/dists/jammy/mongodb-org/8.0/multiverse/binary-amd64/mongodb-org-server_8.0.3_amd64.deb
sudo dpkg -i mongodb-org-server_8.0.3_amd64.deb

sudo mkdir -p /opt/mongodb-8.0.3/{data,logs} && touch /opt/mongodb-8.0.3/logs/mongodb.log
```

### 修改配置文件

```bash
vi /etc/mongod.conf
```
修改如下字段：
```yaml
dbPath: /opt/mongodb-8.0.3/data
path: /opt/mongodb-8.0.3/logs/mongodb.log
bindIp: 0.0.0.0 # 如需外网访问
```

### 设置权限

```bash
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/data
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/logs
sudo chmod -R 755 /opt/mongodb-8.0.3/data
```

### 启动测试

```bash
mongod --port 27017 --dbpath /opt/mongodb-8.0.3/data --noauth
```

### 创建 root 用户

```bash
mongosh
use admin
db.createUser({
  user: "root",
  pwd: "mongoDBTest1password",
  roles: [{ role: "root", db: "admin" }]
})
exit
```

### 设置开机启动

```bash
sudo systemctl enable mongod
sudo systemctl start mongod
```

## 安装 JDK

```bash
cd /opt/dc01
wget https://download.oracle.com/java/21/archive/jdk-21.0.4_linux-x64_bin.tar.gz
tar -xvf jdk-21.0.4_linux-x64_bin.tar.gz
mv jdk-21.0.4 /opt/
```

## 安装 Tomcat

```bash
cd /opt/dc01
wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.39/bin/apache-tomcat-10.1.39.tar.gz
tar -xvf apache-tomcat-10.1.39.tar.gz
mv apache-tomcat-10.1.39 apache-tomcat-dc-10.1.39
```

### 配置 `server.xml`

```xml
<?xml version='1.0' encoding='utf-8'?>
<Server port="9175" shutdown="SHUTDOWN">
  ...
  <Service name="Catalina">
    <Connector port="9012" protocol="org.apache.coyote.http11.Http11Nio2Protocol"
               connectionTimeout="20000"
               maxThreads="500"
               minSpareThreads="20"
               maxSpareThreads="50"
               acceptCount="1000"
               enableLookups="false"
               URIEncoding="UTF-8"
               redirectPort="9745" />
    ...
    <Host name="localhost" appBase="webapps"
          unpackWARs="true" autoDeploy="true">
      <Context docBase="dc" path="/dc"/>
    </Host>
  </Service>
</Server>
```

## 编译打包（Maven）

```bash
# 安装 Maven
https://maven.apache.org/download.cgi

# 获取源码
git clone https://gitee.com/aiotfactory/dc01-iot-platform.git
cd dc01-iot-platform

# 打包 release 版本
mvn clean package -P release
```

## 部署 WAR 包

```bash
unzip /var/webapps/dc.war -d /opt/apache-tomcat-dc-10.1.39/webapps/dc
```

### 配置 application.yml

```bash
vi /opt/apache-tomcat-dc-10.1.39/webapps/dc/WEB-INF/classes/application.yml
```

## 启动 Tomcat

```bash
nohup /opt/apache-tomcat-dc-10.1.39/bin/startup.sh &
```

# 使用限制

开源版本提供所有功能，但有每日流量限制。如有商业用途需求或需要更高的流量限制，请联系 market@zhiyince.com。

如果您需要更强大的解决方案，包括额外的支持、定制服务或更高的使用上限，我们的商用版本将是您的理想选择。欢迎联系我们了解更多关于我们如何满足您的项目需求的信息。
