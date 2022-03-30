# geekbang-coupon-center

1.nacos下载和安装
地址：https://github.com/alibaba/nacos/releases 下载稳定版本；
安装：下载完成后，你可以在本地将 Nacos Server 压缩包解压，并将解压后的目录名改为“nacos-cluster1”，再复制一份同样的文件到 nacos-cluster2，我们以此来模拟一个由两台 Nacos Server 组成的集群
修改数据源：
你需要修改三处 Nacos Server 的数据库配置。
指定数据源：spring.datasource.platform=mysql 这行配置默认情况下被注释掉了，它用来指定数据源为 mysql，你需要将这行注释放开；
指定 DB 实例数：放开 db.num=1 这一行的注释；
修改 JDBC 连接串：db.url.0 指定了数据库连接字符串，我指向了 localhost 3306 端口的 nacos 数据库，稍后我将带你对这个数据库做初始化工作；db.user.0 和 db.password.0 分别指定了连接数据库的用户名和密码，我使用了默认的无密码 root 账户。

    mac 或者 linux 系统，可以在命令行使用 ifconfig | grep “inet” 命令来获取本机 IP 地址
    启动：sh startup.sh -m standalone

    NacosDiscoveryAutoConfiguration 自动装配器有两个开启条件，分别是 spring.cloud.discovery.enabled=true 和 spring.cloud.nacos.discovery.enabled=true