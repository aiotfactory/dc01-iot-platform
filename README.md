# 中文: [中文](./README_CN.md)

# Consult

  www.zhiyince.com  market@zhiyince.com  WeChat：laofuaifaming
  
## Project Introduction

With over 20 years of experience in embedded R&D, production, and IoT platform construction, we have launched this open-source hardware device based on extensive customer feedback.

It is crucial to quickly validate concepts when starting a new project. Therefore, we have integrated as many core components as possible into one product, including various sensors, networking modules, and multiple power supply methods. We also provide functions such as online programming and flashing, helping customers rapidly verify their ideas at the lowest cost.

In addition, this project provides a rich set of features and services for the corresponding IoT platform.

---

# Feature Overview

## User Management  
- **User Registration**: Supports creation and registration of new users.  
- **Login Management**: Provides a secure user login mechanism.

<div align="center">  
  <img src="docs/e02.jpg" width="300"/>  
  <img src="docs/e03.jpg" width="300"/>  
</div>

## Device Management  
- **Device Monitoring and Control**: Real-time monitoring of device status and remote control of operations.  
- **Device Configuration**: Flexible configuration of device parameters to adapt to different application scenarios.

<div align="center">  
  <img src="docs/e04.jpg" width="300"/>  
  <img src="docs/e05.jpg" width="300"/><br>  
  <img src="docs/e06.jpg" width="300"/>  
  <img src="docs/e07.jpg" width="300"/>  
</div>

## Module List  

| Module Name       | Description                                      |  
|-------------------|--------------------------------------------------|  
| Base Module       | System basic information management module       |  
| Cloud Log Module  | Responsible for uploading device runtime logs to the cloud |  
| Wi-Fi Module      | Manages Wi-Fi connectivity functionality       |  
| W5500 Module      | Ethernet communication module                  |  
| 4G Module         | Provides 4G network access support             |  
| TM7705 Module     | High-precision ADC acquisition module          |  
| Camera Module     | Supports image capture and video streaming     |  
| LoRa Module       | Long-distance wireless communication module (10km+) |  
| UART Module       | Serial communication interface module          |  
| SPI Module        | High-speed synchronous serial communication interface module |  
| I²C Module        | Multi-device interconnect bus communication module |  
| Battery ADC Module| Used for battery voltage detection             |  
| Metadata Module   | Stores system metadata and device descriptions |  
| GPIO Module       | Manages general-purpose input/output ports     |  
| Configuration Module | Device configuration management           |  
| Data Forwarding Module | Supports flexible data processing and distribution strategies |  
| AHT20 Module      | Temperature and humidity sensor module         |  
| SPL06 Module      | Barometric pressure sensor module              |  
| RS485 Module      | RS485 communication module                     |  
| PIR Module        | Passive infrared sensor module                 |  
| Thermal Imaging Module | Supports thermal imaging functionality  |  
| Ultrasonic Module | Supports ultrasonic distance measurement     |

## OTA Upgrade  
Supports online firmware upgrades over the network to ensure the system remains up-to-date.

<div align="center">  
  <img src="docs/e10.jpg" width="300"/>  
  <img src="docs/e11.jpg" width="300"/>  
</div>

## Online Code Development  
- **Code Compilation**: Provides an online environment for writing and compiling code.  
- **Firmware Flashing**: Directly push and flash firmware from the cloud to the device.  
- **Device Logs**: View device runtime logs online for debugging and maintenance purposes.

<div align="center">  
  <img src="docs/e12.jpg" width="300"/>  
  <img src="docs/e13.jpg" width="300"/><br>  
  <img src="docs/e14.jpg" width="300"/>  
  <img src="docs/e15.jpg" width="300"/><br>  
  <img src="docs/e16.jpg" width="300"/>  
  <img src="docs/e17.jpg" width="300"/>  
</div>

## API Interface  
Open API interfaces are provided to facilitate third-party application integration and extension.

<div align="center">  
  <img src="docs/e20.jpg" width="300"/>  
  <img src="docs/e21.jpg" width="300"/>  
</div>

## Command Execution on Device  
Commands can be executed on the device either via API or through a command window.

<div align="center">  
  <img src="docs/e22.jpg" width="300"/>  
  <img src="docs/e23.jpg" width="300"/>  
</div>

## Data Forwarding  
Provides multiple data forwarding methods and supports flexible data processing and distribution strategies.

<div align="center">  
  <img src="docs/e18.jpg" width="300"/>  
  <img src="docs/e19.jpg" width="300"/>  
</div>

## Integration with Large Language Model (LLM)  
Integrates advanced large language models to enhance the platform's intelligent interaction capabilities.

<div align="center">  
  <img src="docs/e24.jpg" width="300"/>  
  <img src="docs/e25.jpg" width="300"/><br>  
  <img src="docs/e26.jpg" width="300"/>  
  <img src="docs/e27.jpg" width="300"/>  
</div>

## Embedded Documentation System  
Includes built-in documentation for easy viewing of usage guides and technical references.

<div align="center">  
  <img src="docs/e28.jpg" width="300"/>  
  <img src="docs/e29.jpg" width="300"/>  
</div>

---

# Installation Guide

> It is recommended to perform the following operations on an Ubuntu system with administrator privileges (sudo).

## Prepare Environment

```bash
sudo apt update && sudo apt upgrade -y
mkdir -p /opt/dc01
```

## Install MongoDB

```bash
cd /opt/dc01
wget https://repo.mongodb.org/apt/ubuntu/dists/jammy/mongodb-org/8.0/multiverse/binary-amd64/mongodb-org-server_8.0.3_amd64.deb
sudo dpkg -i mongodb-org-server_8.0.3_amd64.deb

sudo mkdir -p /opt/mongodb-8.0.3/{data,logs} && touch /opt/mongodb-8.0.3/logs/mongodb.log
```

### Edit Configuration File

```bash
vi /etc/mongod.conf
```
Modify the following fields:
```yaml
dbPath: /opt/mongodb-8.0.3/data
path: /opt/mongodb-8.0.3/logs/mongodb.log
bindIp: 0.0.0.0 # Set to 0.0.0.0 if external access is required
```

### Set Directory Permissions

```bash
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/data
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/logs
sudo chmod -R 755 /opt/mongodb-8.0.3/data
```

### Start in Standalone Mode

```bash
mongod --port 27017 --dbpath /opt/mongodb-8.0.3/data --noauth
```

### Set Root Password

```bash
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
```

### Set Directory Permissions Again

```bash
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/data
sudo chown -R mongodb:mongodb /opt/mongodb-8.0.3/logs
sudo chmod -R 755 /opt/mongodb-8.0.3/data
```

### Enable Auto-start on Boot

```bash
sudo systemctl enable mongod
```

### Start MongoDB Service

```bash
sudo systemctl start mongod
```

## Install JDK

```bash
cd /opt/dc01
wget https://download.oracle.com/java/21/archive/jdk-21.0.4_linux-x64_bin.tar.gz
tar -xvf jdk-21.0.4_linux-x64_bin.tar.gz 
mv jdk-21.0.4 ../
```

## Install Tomcat

```bash
cd /opt/dc01
wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.39/bin/apache-tomcat-10.1.39.tar.gz
tar -xvf apache-tomcat-10.1.39.tar.gz 
mv apache-tomcat-10.1.39 apache-tomcat-dc-10.1.39
```

### Edit Tomcat Configuration File to the Following Content

```xml
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

## Build and Package

```bash
https://maven.apache.org/download.cgi # Download Maven and add it to the system PATH

cd /d D:\project\datacollection\temp1\dc01-iot-platform
git clone https://github.com/aiotfactory/dc01-iot-platform.git
cd dc01-iot-platform
mvn clean package -P release
```

## Deploy to Tomcat

```bash
unzip /var/webapps/dc.war -d opt/apache-tomcat-dc-10.1.39/webapps/dc
```

### Edit Configuration

```bash
vi /opt/apache-tomcat-dc-10.1.39/webapps/dc/WEB-INF/classes/application.yml
```

## Start Tomcat

```bash
nohup /opt/apache-tomcat-dc-10.1.39/bin/startup.sh &
```

# Usage Limitations

The open-source version provides full access to all features but is subject to daily traffic limitations. For commercial use or to inquire about higher traffic limits, please contact us at market@zhiyince.com.

Should you require a more robust solution that includes additional support, customizations, or higher usage caps, our commercial version might be the right fit for your needs. Reach out to learn more about how we can accommodate your project requirements.
