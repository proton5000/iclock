package ua.varus.iclock.config;

import com.zkteco.Exception.DeviceNotConnectException;
import com.zkteco.commands.ZKCommandReply;
import com.zkteco.terminal.ZKTerminal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.varus.iclock.utils.ZKTerminalNEw;

import java.io.IOException;

@Configuration
public class ZktecoAutoConfiguration {

    @Value("${zkteco.host}")
    private String host;

    @Value("${zkteco.port}")
    private int port;

    @Value("${zkteco.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${zkteco.timeout.read:3000}")
    private int readTimeout;

    /**
     * Bean клиента Zk. При остановке контекста вызовется zk.disconnect().
     */
    @Bean(destroyMethod = "disconnect")
    public ZKTerminalNEw zkClient() throws IOException, DeviceNotConnectException {
        // Создаём клиент, указываем host и порт
        ZKTerminalNEw zk = new ZKTerminalNEw(host, port);
        ZKCommandReply connected = zk.connect();
        System.out.println("connects code: " + connected.getCode());
        return zk;
    }
}
