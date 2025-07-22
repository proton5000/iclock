package ua.varus.iclock;

import com.zkteco.commands.UserInfo;
import com.zkteco.commands.ZKCommandReply;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.varus.iclock.utils.ZKTerminalNEw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.zkteco.Enum.UserRoleEnum.USER_DEFAULT;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final ZKTerminalNEw zkTerminal;

    @PostConstruct
    public String getTerminalInfo() {

        try {

            String serialNumber = zkTerminal.getSerialNumber();
            System.out.println("Serial number: " + serialNumber);

            zkTerminal.delUser(10500);

            UserInfo userInfo = new UserInfo(10500, "1111", "tesT", "pasS", USER_DEFAULT, 0);
            zkTerminal.modifyUserInfo(userInfo);

            zkTerminal.enrollFinger(10500, 8, "1111");

            zkTerminal.getAllUsers().stream().filter(u -> u.getUid() == 10500)
                    .findFirst().ifPresent(u -> {
                        System.out.println("User name: " + u.getName());
                        System.out.println("User password: " + u.getPassword());
                        System.out.println("User userid: " + u.getUserid());
                    }
            );

            System.out.println("<- end ->");

            return serialNumber;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
