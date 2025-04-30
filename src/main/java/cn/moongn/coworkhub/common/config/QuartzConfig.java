package cn.moongn.coworkhub.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz调度器配置 - JDBC模式
 */
@Configuration
public class QuartzConfig {

    private final DataSource dataSource;
    private final AutowiringSpringBeanJobFactory jobFactory;

    @Autowired
    public QuartzConfig(DataSource dataSource, AutowiringSpringBeanJobFactory jobFactory) {
        this.dataSource = dataSource;
        this.jobFactory = jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        // 设置JobFactory
        schedulerFactoryBean.setJobFactory(jobFactory);

        // 指定数据源
        schedulerFactoryBean.setDataSource(dataSource);

        // 设置名称
        schedulerFactoryBean.setSchedulerName("CoWorkHubScheduler");

        // 设置配置项
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setStartupDelay(5);

        // 设置应用名称
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");

        // 额外属性
        Properties props = new Properties();

        // 基本设置
        props.put("org.quartz.scheduler.instanceName", "CoWorkHubScheduler");
        props.put("org.quartz.scheduler.instanceId", "AUTO");

        // JobStore设置 - 使用JDBC存储
        props.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        props.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        props.put("org.quartz.jobStore.isClustered", "true");
        props.put("org.quartz.jobStore.clusterCheckinInterval", "20000");
        props.put("org.quartz.jobStore.misfireThreshold", "60000");

        // 添加数据源名称配置 - 解决 DataSource name not set 问题
        props.put("org.quartz.jobStore.dataSource", "quartzDataSource");

        // 定义数据源属性（虽然已经设置了dataSource实例，但Quartz内部仍需要这些配置）
        props.put("org.quartz.dataSource.quartzDataSource.provider", "hikaricp");
        props.put("org.quartz.dataSource.quartzDataSource.driver", "com.mysql.cj.jdbc.Driver");
        props.put("org.quartz.dataSource.quartzDataSource.URL", "jdbc:mysql://localhost:3309/coworkhub?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai");
        props.put("org.quartz.dataSource.quartzDataSource.user", "root");
        props.put("org.quartz.dataSource.quartzDataSource.password", "123456");
        props.put("org.quartz.dataSource.quartzDataSource.maxConnections", "5");

        // 线程池配置
        props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.put("org.quartz.threadPool.threadCount", "10");
        props.put("org.quartz.threadPool.threadPriority", "5");

        schedulerFactoryBean.setQuartzProperties(props);

        return schedulerFactoryBean;
    }
}