package ua.varus.iclock;

import com.zkteco.commands.UserInfo;
import com.zkteco.commands.ZKCommandReply;
import ua.varus.iclock.utils.CommandCodeEnum;
import com.zkteco.Enum.CommandReplyCodeEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.varus.iclock.utils.ZKTerminalV;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.zkteco.Enum.UserRoleEnum.USER_DEFAULT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    // Статические переменные для хранения отпечатков
    // 1. Отпечаток с устройства (112 байт)
    public static String deviceFingerprintBase64 = "AAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAQAAAAAAAAABAAAAAAAAAAgAAAAAAAAAAAAAAAEAAAAgTgAAECcAAEANAwAfTgAADycAAD8NAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    public static byte[] deviceFingerprintBytes = Base64.getDecoder().decode(deviceFingerprintBase64);
    
    // 2. Удаленный отпечаток пальца (1493 байт)
    public static String remoteFingerprintBase64 = "TJZTUzIxAAAF1dYECAUHCc7QAAAp1HYBAAAAhXg8i9WiANEOeAAAAPnafQDdAAIPBwB61b8PyADTAK8PiNVgAKIP3ABPAMza5gChAM4PFABi1b0MOACdAHUN/9XCANoPBgFVAFHYLgBiANANvgAv1ZIP9ABEAHsNaNUSAI8O/ADaAXXapgBTAX8OCgBJ1HcNCgEeAAEHgtWOALcNZQBqAAPaagCBACIKaADg1eoPbQByANUIf9VhAJEJ3gACANnaQQCSALkOTQAB1AIPnAAJAUEO/tWHAM4PrQD2AK3aFACTAKoNAAAl1PEPEQB6AHMPWNUbAIoN9wDiAL7aTgASABENeABW1PQNPQBnAc8CftWMAKYNqQATAO7anQBvALMPbABs1bIPjgDyAEAPaNVpAGcIcACnABDcuwD4AO0PrwBd1XYJRQBsACcLINW9AKMOAAEaAGHaSAA/AGIN2QBh1cAPBwFYAA0PvtU8AfcNWgCDAY/aOQA9AQsJpgBQ1AsP4wBpAT0DZMhULwrWvf7s+1v7OAqhf7ICEPec0mMMAZKWgEeAZMT88V2P5f6MD7cpYAaOAZ592AP/IEcKqfgqDZsJJNbce1IOzYhfibdaLBHB8hYeXAeH3D4c5fuJB1//ZNEc/3aAk4K2/9LaAI7iiPoKuIGgVzOBLQkSC8b/Vcb86V6UnQ+XYnfiJBR5fSG+iAC413+AMKu18Pz8bapYLIUdEXZkB2vHIAiO2koTiIGMVDMCiYGlAy+Z1KkvBA4BYxXa9jbfkPbi/dbzG/hD08t/nfYFDnfqiK3k/A75tfnM//4ghIhyhLazuIEIr0gCZgMyA5OD5SZ0hf3XzQBXgMPXvABtEWIl+ABcxu/0SYUlA2SH6YNZWwXYwLKhhxF2PAYCaUIG8WdExRzpMRbmKlt+syCP+7ft1Y9zi6IrO+7223qXePEQ28vjWQ8H/KoNwNOkgup4IYBHgVNWg4GyA4sA5IDj1g4AMoUeB+oDRT7LZ1/QPBjlQQTX3ij7FgBgxRD76/5BRP3B+zok+i4eAHMAFv4FNzaX//7+/MD+4v37Ff/9wfzA/tEAm9Uf/jhEO1Q5//rzPQC3ACD+Ov/6KzxTwCr/Ljg4MJTB/sL8wP44/fko/////sD9OsD4Kv/A/v79/Dj9+tkBzgAeKDWDwQPV2gAiKP4ExecF8/3/CwCTAds1Q5MNAMUBIv0EK/iCwwQA8wEw7w0FKQA0/jjAwQT9XdABhwIePgnFrgfxMcE1CwBt1ZeTF8PD/6MPAKkWlVmPwpfDWgjFdB3J/j7A/SIAnxyJFMLCp53B/0bB+kPBiMHBknwHBQTDH0zBWQgBzyRDmDoIARsmRodVAdVJPXexBgCVQAYrKAQASkNmAcEJ1B5ETMBg/4BFCNX1S0ZX/8GENQPVAVI6wMJ/1wEgg05XwsA2/jr+xJT/AwBkWH0GEQVEXKmRwcHBAa6MrQgAaWB6xGvDxBMRAB9iwP/h+/iU/jP+BQBvoBr7Kf8GAJNlNwX+Gs0BamlxxsUGxcYQw8jFwsTBB8HBF8LFx8LAw80BIr9H/f9t/gPFAHL4wwYAYHhAAMPDGgwBJ4RPVDr+VCpUBQAAhy2yDQUvjFM3O8H9OMH40wAHk1bB/vgOBdWYLYlJwICkDgXPmCnB/sOEB//HFYUMASeaSTj/+5/B/0UNAABoJMSt/sGEcgQB47dfgBEAAcAgwpxmcRb+cRoBJcefRMUqQcA+PjjBjkcA1drLZsEsDsUByMtuwGZzhhrEJ9aPwf9AwEX+Ojf7FTxoOBcAABwgeqH+wmvCwXtBwsXdAf3fZkc4zwEiNFf//v8+T90BIiFfRPzAwv84wjvrwf//ZP0I1BMdvFbALhMQEtwXxxT+XcPAWsOYcBLEJxtrwFz8gsA7jv/BwML9/8wRJ/xjQzLAChHmNlv+/kT/BhBnpZN6KwoQHToQYrpYAsQaS2L8wMCYBRX6XQzCOwUR3151K20=";
    public static byte[] remoteFingerprintBytes = Base64.getDecoder().decode(remoteFingerprintBase64);

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
            
            // Выводим детальную информацию о пользователях и их отпечатках
            for (UserInfo user : allUsers) {
                log.info("=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ ===");
                log.info("User: UID={}, Name={}, UserID={}", user.getUid(), user.getName(), user.getUserid());
                log.info("Password: {}, Enabled: {}, Role: {}", user.getPassword(), user.isEnabled(), user.getRole());
                log.info("CardNo: {}", user.getCardno());
                
                // Получаем детальную информацию об отпечатках
                try {
                    // Проверяем все пальцы (0-9) на наличие отпечатков
                    log.info("Проверяем отпечатки для всех пальцев...");
                    for (int fingerIndex = 0; fingerIndex < 10; fingerIndex++) {
                        try {
                            ZKCommandReply fpReply = zkTerminal.getUserTmpExStr(user.getUserid(), fingerIndex);
                            if (fpReply != null && fpReply.getCode() == CommandReplyCodeEnum.CMD_ACK_OK) {
                                // Получаем данные отпечатка через рефлексию
                                try {
                                    Field payloadsField = fpReply.getClass().getDeclaredField("payloads");
                                    payloadsField.setAccessible(true);
                                    int[] payloads = (int[]) payloadsField.get(fpReply);
                                    
                                    if (payloads != null && payloads.length > 0) {
                                        log.info("✅ Палец {}: ЕСТЬ отпечаток ({} байт)", fingerIndex, payloads.length);
                                        
                                        // Конвертируем в Base64 для анализа
                                        byte[] retrievedBytes = new byte[payloads.length];
                                        for (int i = 0; i < payloads.length; i++) {
                                            retrievedBytes[i] = (byte) payloads[i];
                                        }
                                        String retrievedBase64 = Base64.getEncoder().encodeToString(retrievedBytes);
                                        log.info("Отпечаток палец {} в Base64: {}", fingerIndex, retrievedBase64);
                                    } else {
                                        log.info("❌ Палец {}: НЕТ отпечатка", fingerIndex);
                                    }
                                } catch (Exception e) {
                                    log.warn("Ошибка при получении данных отпечатка для пальца {}: {}", fingerIndex, e.getMessage());
                                }
                            } else {
                                log.info("❌ Палец {}: НЕТ отпечатка (код: {})", fingerIndex, 
                                        fpReply != null ? fpReply.getCode() : "null");
                            }
                        } catch (Exception e) {
                            log.warn("Ошибка при проверке пальца {}: {}", fingerIndex, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Не удалось получить детальную информацию: {}", e.getMessage());
                }
                
                log.info("=== КОНЕЦ ДЕТАЛЬНОЙ ИНФОРМАЦИИ ===");
            }
            
            log.info("=== КОНЕЦ ПРОВЕРКИ УСТРОЙСТВА ===");
            
            // Получаем все отпечатки с устройства
            log.info("=== ПОЛУЧЕНИЕ ВСЕХ ОТПЕЧАТКОВ С УСТРОЙСТВА ===");
            try {
                // Используем метод readAllFptmp для получения всех отпечатков
                log.info("Вызываем readAllFptmp...");
                // Этот метод должен быть в ZKTerminalV, но пока используем альтернативный подход
                
                // Получаем информацию о емкости устройства
                Map<String, Integer> deviceStatus = zkTerminal.getDeviceStatus();
                if (deviceStatus.containsKey("fpCount")) {
                    log.info("Всего отпечатков на устройстве: {}", deviceStatus.get("fpCount"));
                }
                if (deviceStatus.containsKey("fpCapacity")) {
                    log.info("Емкость отпечатков: {}", deviceStatus.get("fpCapacity"));
                }
                
            } catch (Exception e) {
                log.warn("Ошибка при получении всех отпечатков: {}", e.getMessage());
            }
            log.info("=== КОНЕЦ ПОЛУЧЕНИЯ ВСЕХ ОТПЕЧАТКОВ ===");
            
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

            // Добавляем отпечаток новому пользователю (используем uploadFp)
            log.info("=== ДОБАВЛЕНИЕ ОТПЕЧАТКА НОВОМУ ПОЛЬЗОВАТЕЛЮ (uploadFp) ===");
            log.info("Используем предустановленный отпечаток размером {} байт", deviceFingerprintBytes.length);
            
            try {
                log.info("Начинаем загрузку отпечатка через uploadFp...");
                // uploadFp работает с userId (строкой), fpIndex (byte), fpFlag (byte)
                zkTerminal.uploadFp("6000", deviceFingerprintBytes, (byte) 0, (byte) 1);
                log.info("✅ Отпечаток успешно загружен и привязан через uploadFp!");
            } catch (Exception e) {
                log.error("uploadFp failed with error: {}", e.getMessage());
                e.printStackTrace();
            }
            
            log.info("=== КОНЕЦ ДОБАВЛЕНИЯ ОТПЕЧАТКА (uploadFp) ===");

            // Тестируем удаленный отпечаток пальца через uploadFp
            log.info("=== ТЕСТИРОВАНИЕ УДАЛЕННОГО ОТПЕЧАТКА ПАЛЬЦА (uploadFp) ===");
            log.info("Удаленный отпечаток размером {} байт", remoteFingerprintBytes.length);
            
            try {
                log.info("Начинаем загрузку удаленного отпечатка через uploadFp...");
                // Пробуем загрузить удаленный отпечаток на палец 1
                zkTerminal.uploadFp("6000", remoteFingerprintBytes, (byte) 1, (byte) 1);
                log.info("✅ Удаленный отпечаток успешно загружен через uploadFp!");
            } catch (Exception e) {
                log.error("uploadFp (удаленный) failed with error: {}", e.getMessage());
                e.printStackTrace();
                
                // Если не удалось, пробуем создать тестовый отпечаток меньшего размера
                log.info("Пробуем создать тестовый отпечаток меньшего размера...");
                try {
                    // Создаем тестовый отпечаток размером 512 байт (как в ZKFinger 9.0)
                    byte[] testFingerprint = new byte[512];
                    for (int i = 0; i < testFingerprint.length; i++) {
                        testFingerprint[i] = (byte) (i % 256);
                    }
                    
                    log.info("Загружаем тестовый отпечаток размером {} байт...", testFingerprint.length);
                    zkTerminal.uploadFp("6000", testFingerprint, (byte) 1, (byte) 1);
                    log.info("✅ Тестовый отпечаток успешно загружен!");
                } catch (Exception e2) {
                    log.error("Тестовый отпечаток тоже не удалось загрузить: {}", e2.getMessage());
                }
            }
            
            log.info("=== КОНЕЦ ТЕСТИРОВАНИЯ УДАЛЕННОГО ОТПЕЧАТКА ===");

            // Добавляем отпечаток строкой в Base64 формате через uploadFp
            log.info("=== ДОБАВЛЕНИЕ ОТПЕЧАТКА СТРОКОЙ В BASE64 (uploadFp) ===");
            log.info("Используем предустановленный отпечаток в Base64: {}", deviceFingerprintBase64);
            
            try {
                log.info("Начинаем загрузку строкового отпечатка через uploadFp...");
                // Конвертируем Base64 обратно в байты и используем uploadFp
                byte[] base64Bytes = Base64.getDecoder().decode(deviceFingerprintBase64);
                zkTerminal.uploadFp("6000", base64Bytes, (byte) 2, (byte) 1);
                log.info("✅ Строковый отпечаток успешно загружен через uploadFp!");
            } catch (Exception e) {
                log.error("uploadFp (строковый) failed with error: {}", e.getMessage());
                e.printStackTrace();
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
