# Nuls

**Welcome to Nuls!**

## Introduction

Nuls is a global blockchain open-source project which is a highly customizable modular blockchain infrastructure.  
It consists of a microkernel and functional modules.  
Nuls provides smart contract, multi-chain mechanism and cross-chain consensus.  
It aims to break the technical barriers of blockchain, to reduce the cost of development, and to promote the usage of blockchain technology in the commercial field.

**Project Features**

- Modular design  
- Parallel chains  
- Smart contract  
- POC consensus mechanisms

To learn more about us, visit [nuls.io](https://nuls.io/).

## Contributing to Nuls

Contributions to Nuls are welcomed!  
At this stage the core members are working hard to develop the first stable version.
We will accept PR after the first stable release published.

To be a great community, Nuls needs to welcome developers from all walks of life, with different backgrounds, and with a wide range of experience.

## Getting Started

### Requirements

**Operating System**  
Nuls is based on the Java and you can choose your favorite OS.  

**Dependencies SDK**  
JDK:[JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)  
Maven:[Maven 3.3+](http://maven.apache.org/download.cgi)  
IDE: Any one you  like
> JetBrains [IntelliJ IDEA](https://www.jetbrains.com/idea/) is recommended, but we do not rovoke the IDE war.


### Code style guide
Use Alibaba Java Coding Guidelines [@Github](https://github.com/alibaba/p3c).  
* [Alibaba-Java-Coding-Guidelines](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines/)  
> recommend use IDE plugin lint code. [wiki](https://github.com/alibaba/p3c/wiki)
> * [IntelliJ IDEA Plugin](https://github.com/alibaba/p3c/tree/master/idea-plugin)
> * [Eclipse Plugin](https://github.com/alibaba/p3c/tree/master/eclipse-plugin)

### Getting Sources

```shell
git clone https://github.com/nuls-io/nuls.git && cd nuls
```

### Building Sources

```shell
nuls>mvn clean package
```
> You may be need add command line argument "-Dmaven.test.skip=true" at this stage.

### Running

```shell
$ mvn clean package
$ cd node/target
$ tar zxvf nuls-node.tar.gz
$ cd bin
```
Using start.sh running the nuls process.  
Using stop.sh stop the nuls process.

> Using IDE run or debug need execute command "mvn install" first.

### Process EntryPoint

> nuls/node/src/main/java/io/nuls/Bootstrap.java

## License

Nuls is released under the [MIT](http://opensource.org/licenses/MIT) license.  
Modules added in the future may be release under different license, will specified in the module library path.

## Community
* [Nuls.io](https://nuls.io/)
* [@twitter](https://twitter.com/nulsservice)  
* [facebook](https://www.facebook.com/nulscommunity/)
* [YouTube channel ](https://www.youtube.com/channel/UC8FkLeF4QW6Undm4B3InN1Q?view_as=subscriber)
* Telegram [Nuls Community](https://t.me/Nulsio)
* Telegram [Nuls 中文社区](https://t.me/Nulscn)
