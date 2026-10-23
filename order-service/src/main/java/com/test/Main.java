package com.test;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

public class Main {
    public static void main(String[] args) {
        //配置数据源
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(
                "jdbc:mysql://localhost:3306/uushop?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
                System.getenv().getOrDefault("MYSQL_USERNAME", "root"),
                System.getenv().getOrDefault("MYSQL_PASSWORD", "")
        ).build();
        //全局配置
        GlobalConfig globalConfig = new GlobalConfig.Builder()
                //输出目录:写死到 product-service,避免受 IDEA 工作目录影响
                .outputDir("D:/wyx_learn_Java/uushop/order-service/src/main/java")
                .author("wyx")
                .enableSwagger()   // 生成 swagger2 注解,与项目中已有的 springfox-swagger2 匹配
                .disableOpenDir()   // 禁止生成后自动打开文件夹
                .build();
        //包配置
        PackageConfig packageConfig = new PackageConfig.Builder()
                .parent("com.test")  //父包名
                .entity("entity")
                .mapper("mapper")
                .service("service")
                .serviceImpl("service.impl")
                .controller("controller")
                .build();
        //策略配置
        StrategyConfig strategyConfig = new StrategyConfig.Builder()
                .addInclude("order_master","order_detail")  //设置需要生成的表名
                .entityBuilder().enableLombok().enableTableFieldAnnotation().naming(NamingStrategy.underline_to_camel)  //开启 Lombok 开启字段注解
                .columnNaming(NamingStrategy.underline_to_camel)
                .controllerBuilder().enableRestStyle()  //开启 RESTful 风格
                .serviceBuilder().formatServiceFileName("%sService")  //去掉 Service 接口的 I 前缀
                .build();
        //执行生成
        AutoGenerator generator = new AutoGenerator(dataSourceConfig);
        generator.global(globalConfig);
        generator.packageInfo(packageConfig);
        generator.strategy(strategyConfig);
        generator.execute();
    }
}
