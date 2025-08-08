package ua.varus.iclock.config;

import com.zkteco.Exception.DeviceNotConnectException;
import com.zkteco.commands.ZKCommandReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.varus.iclock.utils.ZKTerminalV;

import java.io.IOException;

@Configuration
public class ZktecoAutoConfiguration {

    @Value("${zkteco.host}")
    private String host;

    @Value("${zkteco.port}")
    private int port;

    @Bean(destroyMethod = "disconnect")
    public ZKTerminalV zkClient() throws IOException, DeviceNotConnectException {
        // Создаём клиент, указываем host и порт
        ZKTerminalV zk = new ZKTerminalV(host, port);
        ZKCommandReply connected = zk.connect();
        System.out.println("connects code: " + connected.getCode());
        return zk;
    }
}
