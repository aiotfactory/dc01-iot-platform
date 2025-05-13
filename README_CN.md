[English](./README.md)

# 项目介绍

凭借在嵌入式研发、生产和IoT平台建设方面超过20年的经验，我们根据众多客户的需求反馈推出了这款开源硬件设备。

新项目启动时，快速验证概念至关重要。因此，我们在一个产品中集成了尽可能多的核心组件，包括各种传感器、联网模块和多种供电方式，并提供了在线编程与烧录等功能，帮助客户以最低成本迅速验证其想法。

此外，本项目还为相应的IoT平台提供了一系列丰富的功能和服务。

# 功能介绍

## 用户管理
- **用户注册**：支持新用户的创建与注册。
- **登录管理**：提供安全的用户登录机制。

<table>
  <tr>
    <td><img src="docs/p02.jpg" width="300"/></td>
    <td><img src="docs/p03.jpg" width="300"/></td>
  </tr>
</table>

## 设备管理
- **设备监控与控制**：实时监控设备状态，远程控制设备操作。
- **设备配置**：灵活配置设备参数，适应不同应用场景。

<table>
  <tr>
    <td><img src="docs/p04.jpg" width="300"/></td>
    <td><img src="docs/p05.jpg" width="300"/></td>
  </tr>
  <tr>
    <td><img src="docs/p06.jpg" width="300"/></td>
    <td><img src="docs/p07.jpg" width="300"/></td>
  </tr>
</table>

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
- 支持通过网络对设备固件进行在线升级，确保系统始终保持最新。

<table>
  <tr>
    <td><img src="docs/p10.jpg" width="300"/></td>
    <td><img src="docs/p11.jpg" width="300"/></td>
  </tr>
</table>


## 在线代码
- **代码编译**：提供在线环境用于编写和编译代码。

<table>
  <tr>
    <td><img src="docs/p12.jpg" width="300"/></td>
    <td><img src="docs/p13.jpg" width="300"/></td>
  </tr>
</table>

- **固件烧录**：直接从云端向设备推送并烧录固件。

<table>
  <tr>
    <td><img src="docs/p14.jpg" width="300"/></td>
    <td><img src="docs/p15.jpg" width="300"/></td>
  </tr>
</table>

- **设备日志**：在线查看设备运行日志，便于调试和维护。

<table>
  <tr>
    <td><img src="docs/p16.jpg" width="300"/></td>
    <td><img src="docs/p17.jpg" width="300"/></td>
  </tr>
</table>

## API接口
- 对外开放API接口，方便第三方应用集成与扩展。

<table>
  <tr>
    <td><img src="docs/p20.jpg" width="300"/></td>
    <td><img src="docs/p21.jpg" width="300"/></td>
  </tr>
</table>

## 在设备上执行命令
- 既可以通过API又可以通过命令窗口在设备上执行命令。

<table>
  <tr>
    <td><img src="docs/p22.jpg" width="300"/></td>
    <td><img src="docs/p23.jpg" width="300"/></td>
  </tr>
</table>

## 数据转发
- 提供多种数据转发方式，支持灵活的数据处理和分发策略。

<table>
  <tr>
    <td><img src="docs/p18.jpg" width="300"/></td>
    <td><img src="docs/p19.jpg" width="300"/></td>
  </tr>
</table>

## 集成大语言模型（LLM）
- 集成先进的大语言模型，增强平台的智能交互能力。

<table>
  <tr>
    <td><img src="docs/p24.jpg" width="300"/></td>
    <td><img src="docs/p25.jpg" width="300"/></td>
  </tr>
  <tr>
    <td><img src="docs/p26.jpg" width="300"/></td>
    <td><img src="docs/p27.jpg" width="300"/></td>
  </tr>
</table>

## 丰富完善的文档
- 内嵌式文档，方便查看。

<table>
  <tr>
    <td><img src="docs/p28.jpg" width="300"/></td>
    <td><img src="docs/p29.jpg" width="300"/></td>
  </tr>
</table>

# 安装步骤

```bash
# 准备包
sudo apt update && sudo apt upgrade -y
mkdir -p /opt/dc01

# 安装mongodb
cd /opt/dc01
wget https://repo.mongodb.org/apt/ubuntu/dists/jammy/mongodb-org/8.0/multiverse/binary-amd64/mongodb-org-server_8.0.3_amd64.deb
sudo dpkg -i mongodb-org-server_8.0.3_amd64.deb
sudo mkdir -p /opt/mongodb-8.0.3/{data,logs} && touch /opt/mongodb-8.0.3/logs/mongodb.log

# 编辑配置文件
vi /etc/mongod.conf 
dbPath: /opt/mongodb-8.0.3/data
path: /opt/mongodb-8.0.3/logs/mongodb.log
bindIp: 0.0.0.0 #如需要外网访问，设置为0.0.0.0


# 设置目录访问权限
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/data
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/logs
sudo chmod -R 755 /opt/mongodb-8.0.3/data

# 安装mongo shell
wget https://downloads.mongodb.com/compass/mongodb-mongosh_2.5.0_amd64.deb 
sudo dpkg -i  mongodb-mongosh_2.5.0_amd64.deb

# 以免登录方式启动
mongod --port 27017 --dbpath /opt/mongodb-8.0.3/data --noauth

# 设置root密码
mongosh
use admin
db.createUser({
  user: "root",
  pwd: "mongoDBTest1password",  
  roles: [
    { role: "root", db: "admin" }
  ]
})
exit
ctrl+c

# 设置目录访问权限(再次)
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/data
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/logs
sudo chmod -R 755 /opt/mongodb-8.0.3/data

# 设置开机启动
sudo systemctl enable mongod

# 启动数据库
sudo systemctl start mongod

# 安装JDK
cd /opt/dc01
wget https://download.oracle.com/java/21/archive/jdk-21.0.4_linux-x64_bin.tar.gz
tar -xvf jdk-21.0.4_linux-x64_bin.tar.gz 
mv jdk-21.0.4 ../

# 安装tomcat
cd /opt/dc01
wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.39/bin/apache-tomcat-10.1.39.tar.gz
tar -xvf apache-tomcat-10.1.39.tar.gz 
mv apache-tomcat-10.1.39 apache-tomcat-dc-10.1.39

# 编辑tomcat配置文件为如下内容
vi /opt/apache-tomcat-dc-10.1.39/conf/server.xml

<?xml version='1.0' encoding='utf-8'?>
<Server port="9175" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <Listener className="org.apache.catalina.core.AprLifecycleListener" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

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

    <Engine name="Catalina" defaultHost="localhost">
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost" appBase="webapps"
            unpackWARs="true" autoDeploy="true">
        <Context docBase="dc" path="/dc"/>
      </Host>
    </Engine>
  </Service>
</Server>
  
```