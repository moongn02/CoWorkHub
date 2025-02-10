package cn.moongn.coworkhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.moongn.coworkhub.mapper")
public class CoWorkHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoWorkHubApplication.class, args);
    }

}
