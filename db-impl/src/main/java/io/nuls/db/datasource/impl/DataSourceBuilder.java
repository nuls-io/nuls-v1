package io.nuls.db.datasource.impl;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zoro on 2017/10/10.
 */
public class DataSourceBuilder extends UnpooledDataSourceFactory {


    private String driverClassName;

    private String url;

    private String username;

    private String password;

    /**
     * new druidDataSource and set config
     * @throws Exception
     */
    public DataSourceBuilder() throws Exception {
        DruidDataSource druidDataSource = new DruidDataSource();

        Properties conf = new Properties();
        InputStream in = this.getClass().getResourceAsStream("/db_config.properties");
        conf.load(in);
        in.close();

        long maxWait = Long.parseLong(conf.getProperty("druid.maxWait"));
        long timeBetweenEvictionRunsMillis = Long.parseLong(conf.getProperty("druid.timeBetweenEvictionRunsMillis"));
        boolean testOnReturn = Boolean.parseBoolean(conf.getProperty("druid.testOnReturn"));
        druidDataSource.configFromPropety(conf);
        druidDataSource.setMaxWait(maxWait);
        druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        druidDataSource.setTestOnReturn(testOnReturn);
        this.dataSource = druidDataSource;

    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
