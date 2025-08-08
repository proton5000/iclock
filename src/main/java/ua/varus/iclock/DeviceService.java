package ua.varus.iclock;

import com.zkteco.commands.UserInfo;
import com.zkteco.commands.ZKCommandReply;
import com.zkteco.Enum.CommandReplyCodeEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.varus.iclock.utils.ZKTerminalV;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;

import static com.zkteco.Enum.UserRoleEnum.USER_DEFAULT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    // Статические переменные для хранения отпечатка с устройства
    // Используем полученный отпечаток с устройства (112 байт)
    public static String deviceFingerprintBase64 = "AAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAQAAAAAAAAABAAAAAAAAAAgAAAAAAAAAAAAAAAEAAAAgTgAAECcAAEANAwAfTgAADycAAD8NAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    public static byte[] deviceFingerprintBytes = Base64.getDecoder().decode(deviceFingerprintBase64);

    private final ZKTerminalV zkTerminal;

    @PostConstruct
    public String getTerminalInfo() {
        try {
            String serialNumber = zkTerminal.getSerialNumber();
            log.info("serial number -> {}", serialNumber);
            log.info("auth -> {}", zkTerminal.connectAuth(0).getCode());
            log.info("FPVersion -> {}", zkTerminal.getFPVersion());
            
            // Проверяем состояние устройства после перезагрузки
            log.info("=== ПРОВЕРКА УСТРОЙСТВА ПОСЛЕ ПЕРЕЗАГРУЗКИ ===");
            
            // Получаем всех пользователей
            log.info("Getting all users...");
            List<UserInfo> allUsers = zkTerminal.getAllUsers();
            log.info("Found {} users", allUsers.size());
            
            // Выводим информацию о пользователях
            for (UserInfo user : allUsers) {
                log.info("User: UID={}, Name={}, UserID={}", user.getUid(), user.getName(), user.getUserid());
            }
            
            log.info("=== КОНЕЦ ПРОВЕРКИ УСТРОЙСТВА ===");
            
            // Ждем стабилизации
            Thread.sleep(2000);
                
            // Проверяем, есть ли пользователь 6000
            log.info("Checking if user with UID 6000 exists...");
            List<UserInfo> existingUsers = zkTerminal.getAllUsers();
            log.info("Total existing users found: {}", existingUsers.size());
            
            boolean userExists = false;
            for (UserInfo u : existingUsers) {
                if (u.getUid() == 6000) {
                    log.info("User with UID 6000 exists: {}", u.getName());
                    userExists = true;
                    break;
                }
            }
            
            if (!userExists) {
                log.info("User with UID 6000 does not exist, creating new user...");
                // Создаем пользователя с UID 6000 используя modifyUserInfo
                UserInfo userInfo = new UserInfo(6000, "6000", "tesT", "11111", USER_DEFAULT, 0);
                log.info("Creating user with info: UID={}, UserID={}, Name={}, Password={}, Role={}, CardNo={}", 
                        userInfo.getUid(), userInfo.getUserid(), userInfo.getName(), userInfo.getPassword(), 
                        userInfo.getRole(), userInfo.getCardno());
                
                ZKCommandReply zkCommandReply1 = zkTerminal.modifyUserInfo(userInfo);
                if (zkCommandReply1 != null) {
                    log.info("modifyUserInfo -> {}", zkCommandReply1.getCode());
                } else {
                    log.error("modifyUserInfo -> null (failed to create user)");
                }
                Thread.sleep(500);
            } else {
                log.info("User with UID 6000 already exists, skipping creation...");
            }

            // Используем предустановленный отпечаток
            log.info("=== ИСПОЛЬЗУЕМ ПРЕДУСТАНОВЛЕННЫЙ ОТПЕЧАТОК ===");
            log.info("Отпечаток размером {} байт", deviceFingerprintBytes.length);
            log.info("Отпечаток в Base64: {}", deviceFingerprintBase64);
            log.info("=== КОНЕЦ ПРЕДУСТАНОВЛЕННОГО ОТПЕЧАТКА ===");

            // Удаляем пользователя 6000 если он существует
            log.info("=== УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ 6000 ===");
            try {
                ZKCommandReply deleteReply = zkTerminal.delUser(6000);
                log.info("delUser для UID 6000 -> {}", deleteReply.getCode());
                Thread.sleep(1000);
            } catch (Exception e) {
                log.warn("Ошибка при удалении пользователя: {}", e.getMessage());
            }
            log.info("=== КОНЕЦ УДАЛЕНИЯ ПОЛЬЗОВАТЕЛЯ ===");

            // Создаем нового пользователя 6000
            log.info("=== СОЗДАНИЕ НОВОГО ПОЛЬЗОВАТЕЛЯ 6000 ===");
            UserInfo newUserInfo = new UserInfo(6000, "6000", "tesT", "11111", USER_DEFAULT, 0);
            log.info("Creating new user with info: UID={}, UserID={}, Name={}, Password={}, Role={}, CardNo={}", 
                    newUserInfo.getUid(), newUserInfo.getUserid(), newUserInfo.getName(), newUserInfo.getPassword(), 
                    newUserInfo.getRole(), newUserInfo.getCardno());
            
            ZKCommandReply createReply = zkTerminal.modifyUserInfo(newUserInfo);
            if (createReply != null) {
                log.info("modifyUserInfo -> {}", createReply.getCode());
            } else {
                log.error("modifyUserInfo -> null (failed to create user)");
            }
            Thread.sleep(500);
            log.info("=== КОНЕЦ СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ ===");

            // Добавляем отпечаток новому пользователю (бинарный метод)
            log.info("=== ДОБАВЛЕНИЕ ОТПЕЧАТКА НОВОМУ ПОЛЬЗОВАТЕЛЮ (БИНАРНЫЙ) ===");
            log.info("Используем предустановленный отпечаток размером {} байт", deviceFingerprintBytes.length);
            
            try {
                ZKCommandReply uploadReply = zkTerminal.setUserTmpEx(1, 6000, 0, 1, deviceFingerprintBytes);
                if (uploadReply != null) {
                    log.info("setUserTmpEx result -> {}", uploadReply.getCode());
                } else {
                    log.info("setUserTmpEx result -> null");
                }
            } catch (Exception e) {
                log.warn("setUserTmpEx failed with error: {}", e.getMessage());
            }
            
            log.info("=== КОНЕЦ ДОБАВЛЕНИЯ ОТПЕЧАТКА (БИНАРНЫЙ) ===");

            // Добавляем отпечаток строкой в Base64 формате
            log.info("=== ДОБАВЛЕНИЕ ОТПЕЧАТКА СТРОКОЙ В BASE64 ===");
            log.info("Используем предустановленный отпечаток в Base64: {}", deviceFingerprintBase64);
            
            try {
                ZKCommandReply uploadReplyStr = zkTerminal.uploadFingerprintProtocolStrNullTerminated("6000", 0, 1, deviceFingerprintBase64);
                if (uploadReplyStr != null) {
                    log.info("uploadFingerprintProtocolStrNullTerminated result -> {}", uploadReplyStr.getCode());
                } else {
                    log.info("uploadFingerprintProtocolStrNullTerminated result -> null");
                }
            } catch (Exception e) {
                log.warn("uploadFingerprintProtocolStrNullTerminated failed with error: {}", e.getMessage());
            }
            
            log.info("=== КОНЕЦ ДОБАВЛЕНИЯ ОТПЕЧАТКА СТРОКОЙ В BASE64 ===");

            // Проверяем, что отпечаток действительно сохранился на устройстве
            log.info("=== ПРОВЕРКА СОХРАНЕНИЯ ОТПЕЧАТКА НА УСТРОЙСТВЕ ===");
            
            try {
                // Получаем отпечаток с устройства
                log.info("Получаем отпечаток с устройства для пользователя 6000, палец 0...");
                ZKCommandReply verifyReply = zkTerminal.getUserTmpExStr("6000", 0);
                
                if (verifyReply != null && verifyReply.getCode() == CommandReplyCodeEnum.CMD_ACK_OK) {
                    log.info("getUserTmpExStr result -> {}", verifyReply.getCode());
                    
                    // Получаем данные отпечатка через рефлексию
                    try {
                        Field payloadsField = verifyReply.getClass().getDeclaredField("payloads");
                        payloadsField.setAccessible(true);
                        int[] payloads = (int[]) payloadsField.get(verifyReply);
                        
                        if (payloads != null && payloads.length > 0) {
                            log.info("Получен отпечаток с устройства размером {} байт", payloads.length);
                            
                            // Конвертируем в Base64 для сравнения
                            byte[] retrievedBytes = new byte[payloads.length];
                            for (int i = 0; i < payloads.length; i++) {
                                retrievedBytes[i] = (byte) payloads[i];
                            }
                            String retrievedBase64 = Base64.getEncoder().encodeToString(retrievedBytes);
                            log.info("Отпечаток с устройства в Base64: {}", retrievedBase64);
                            
                            // Сравниваем с оригинальным отпечатком
                            log.info("=== СРАВНЕНИЕ ОТПЕЧАТКОВ ===");
                            log.info("Оригинальный отпечаток: {}", deviceFingerprintBase64);
                            log.info("Полученный отпечаток: {}", retrievedBase64);
                            
                            if (deviceFingerprintBase64.equals(retrievedBase64)) {
                                log.info("✅ ОТПЕЧАТКИ ИДЕНТИЧНЫ! Загрузка прошла успешно!");
                            } else {
                                log.warn("❌ ОТПЕЧАТКИ РАЗЛИЧАЮТСЯ! Загрузка не удалась!");
                                log.warn("Размеры: оригинал={} байт, получен={} байт", 
                                        deviceFingerprintBytes.length, retrievedBytes.length);
                            }
                            
                            log.info("=== КОНЕЦ СРАВНЕНИЯ ===");
                        } else {
                            log.warn("❌ Отпечаток с устройства пустой или null");
                        }
                    } catch (Exception e) {
                        log.warn("Ошибка при получении данных отпечатка: {}", e.getMessage());
                    }
                } else {
                    log.warn("❌ getUserTmpExStr failed: {}", 
                            verifyReply != null ? verifyReply.getCode() : "null");
                }
            } catch (Exception e) {
                log.warn("Ошибка при проверке отпечатка: {}", e.getMessage());
            }
            
            log.info("=== КОНЕЦ ПРОВЕРКИ СОХРАНЕНИЯ ОТПЕЧАТКА ===");

            // Принудительное сохранение и обновление данных устройства
            log.info("=== ПРИНУДИТЕЛЬНОЕ СОХРАНЕНИЕ И ОБНОВЛЕНИЕ ДАННЫХ ===");
            
            try {
                // Отключаем устройство перед сохранением
                log.info("Отключаем устройство...");
                ZKCommandReply disableReply = zkTerminal.disableDevice();
                if (disableReply != null) {
                    log.info("disableDevice result -> {}", disableReply.getCode());
                }
                Thread.sleep(1000);
                
                // Сохраняем данные
                log.info("Сохраняем данные...");
                ZKCommandReply saveReply = zkTerminal.saveData();
                if (saveReply != null) {
                    log.info("saveData result -> {}", saveReply.getCode());
                }
                Thread.sleep(1000);
                
                // Обновляем данные
                log.info("Обновляем данные...");
                zkTerminal.refreshData();
                log.info("refreshData completed");
                Thread.sleep(1000);
                
                // Включаем устройство обратно
                log.info("Включаем устройство...");
                ZKCommandReply enableReply = zkTerminal.enableDevice();
                if (enableReply != null) {
                    log.info("enableDevice result -> {}", enableReply.getCode());
                }
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.warn("Ошибка при сохранении/обновлении данных: {}", e.getMessage());
            }
            
            log.info("=== КОНЕЦ СОХРАНЕНИЯ И ОБНОВЛЕНИЯ ДАННЫХ ===");

            // Повторная проверка отпечатка после сохранения
            log.info("=== ПОВТОРНАЯ ПРОВЕРКА ОТПЕЧАТКА ПОСЛЕ СОХРАНЕНИЯ ===");
            
            try {
                log.info("Повторно получаем отпечаток с устройства...");
                ZKCommandReply verifyReply2 = zkTerminal.getUserTmpExStr("6000", 0);
                
                if (verifyReply2 != null && verifyReply2.getCode() == CommandReplyCodeEnum.CMD_ACK_OK) {
                    log.info("getUserTmpExStr (повторно) result -> {}", verifyReply2.getCode());
                    
                    try {
                        Field payloadsField = verifyReply2.getClass().getDeclaredField("payloads");
                        payloadsField.setAccessible(true);
                        int[] payloads = (int[]) payloadsField.get(verifyReply2);
                        
                        if (payloads != null && payloads.length > 0) {
                            log.info("✅ Отпечаток подтвержден после сохранения! Размер: {} байт", payloads.length);
                        } else {
                            log.warn("❌ Отпечаток не найден после сохранения");
                        }
                    } catch (Exception e) {
                        log.warn("Ошибка при получении данных отпечатка: {}", e.getMessage());
                    }
                } else {
                    log.warn("❌ getUserTmpExStr (повторно) failed: {}", 
                            verifyReply2 != null ? verifyReply2.getCode() : "null");
                }
            } catch (Exception e) {
                log.warn("Ошибка при повторной проверке отпечатка: {}", e.getMessage());
            }
            
            log.info("=== КОНЕЦ ПОВТОРНОЙ ПРОВЕРКИ ===");

            log.info("<- end ->");
            return serialNumber;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
