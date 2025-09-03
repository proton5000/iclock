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
    
    // 3. Тестовые Base64 строки от пользователя
    public static String testFingerprintBase64_1 = "TJZTUzIxAAAF1dYECAUHCc7QAAAp1HYBAAAAhXg8i9WiANEOeAAAAPnafQDdAAIPBwB61b8PyADTAK8PiNVgAKIP3ABPAMza5gChAM4PFABi1b0MOACdAHUN/9XCANoPBgFVAFHYLgBiANANvgAv1ZIP9ABEAHsNaNUSAI8O/ADaAXXapgBTAX8OCgBJ1HcNCgEeAAEHgtWOALcNZQBqAAPaagCBACIKaADg1eoPbQByANUIf9VhAJEJ3gACANnaQQCSALkOTQAB1AIPnAAJAUEO/tWHAM4PrQD2AK3aFACTAKoNAAAl1PEPEQB6AHMPWNUbAIoN9wDiAL7aTgASABENeABW1PQNPQBnAc8CftWMAKYNqQATAO7anQBvALMPbABs1bIPjgDyAEAPaNVpAGcIcACnABDcuwD4AO0PrwBd1XYJRQBsACcLINW9AKMOAAEaAGHaSAA/AGIN2QBh1cAPBwFYAA0PvtU8AfcNWgCDAY/aOQA9AQsJpgBQ1AsP4wBpAT0DZMhULwrWvf7s+1v7OAqhf7ICEPec0mMMAZKWgEeAZMT88V2P5f6MD7cpYAaOAZ592AP/IEcKqfgqDZsJJNbce1IOzYhfibdaLBHB8hYeXAeH3D4c5fuJB1//ZNEc/3aAk4K2/9LaAI7iiPoKuIGgVzOBLQkSC8b/Vcb86V6UnQ+XYnfiJBR5fSG+iAC413+AMKu18Pz8bapYLIUdEXZkB2vHIAiO2koTiIGMVDMCiYGlAy+Z1KkvBA4BYxXa9jbfkPbi/dbzG/hD08t/nfYFDnfqiK3k/A75tfnM//4ghIhyhLazuIEIr0gCZgMyA5OD5SZ0hf3XzQBXgMPXvABtEWIl+ABcxu/0SYUlA2SH6YNZWwXYwLKhhxF2PAYCaUIG8WdExRzpMRbmKlt+syCP+7ft1Y9zi6IrO+7223qXePEQ28vjWQ8H/KoNwNOkgup4IYBHgVNWg4GyA4sA5IDj1g4AMoUeB+oDRT7LZ1/QPBjlQQTX3ij7FgBgxRD76/5BRP3B+zok+i4eAHMAFv4FNzaX//7+/MD+4v37Ff/9wfzA/tEAm9Uf/jhEO1Q5//rzPQC3ACD+Ov/6KzxTwCr/Ljg4MJTB/sL8wP44/fko/////sD9OsD4Kv/A/v79/dj9+tkBzgAeKDWDwQPV2gAiKP4ExecF8/3/CwCTAds1Q5MNAMUBIv0EK/iCwwQA8wEw7w0FKQA0/jjAwQT9XdABhwIePgnFrgfxMcE1CwBt1ZeTF8PD/6MPAKkWlVmPwpfDWgjFdB3J/j7A/SIAnxyJFMLCp53B/0bB+kPBiMHBknwHBQTDH0zBWQgBzyRDmDoIARsmRodVAdVJPXexBgCVQAYrKAQASkNmAcEJ1B5ETMBg/4BFCNX1S0ZX/8GENQPVAVI6wMJ/1wEgg05XwsA2/jr+xJT/AwBkWH0GEQVEXKmRwcHBAa6MrQgAaWB6xGvDxBMRAB9iwP/h+/iU/jP+BQBvoBr7Kf8GAJNlNwX+Gs0BamlxxsUGxcYQw8jFwsTBB8HBF8LFx8LAw80BIr9H/f9t/gPFAHL4wwYAYHhAAMPDGgwBJ4RPVDr+VCpUBQAAhy2yDQUvjFM3O8H9OMH40wAHk1bB/vgOBdWYLYlJwICkDgXPmCnB/sOEB//HFYUMASeaSTj/+5/B/0UNAABoJMSt/sGEcgQB47dfgBEAAcAgwpxmcRb+cRoBJcefRMUqQcA+PjjBjkcA1drLZsEsDsUByMtuwGZzhhrEJ9aPwf9AwEX+Ojf7FTxoOBcAABwgeqH+wmvCwXtBwsXdAf3fZkc4zwEiNFf//v8+T90BIiFfRPzAwv84wjvrwf//ZP0I1BMdvFbALhMQEtwXxxT+XcPAWsOYcBLEJxtrwFz8gsA7jv/BwML9/8wRJ/xjQzLAChHmNlv+/kT/BhBnpZN6KwoQHToQYrpYAsQaS2L8wMCYBRX6XQzCOwUR3151K20=";
    public static byte[] testFingerprintBytes_1 = Base64.getDecoder().decode(testFingerprintBase64_1);
    
    public static String testFingerprintBase64_2 = "TOhTUzIxAAAFq6wECAUHCc7QAAApqnYBAAAAhVY9fau1APYPhABXAPGnZwCUAIILRAB5q4kMQwCXAEANfKtpAI4PNwC6AJWmgAAVAXwLXwBUq/gO4QDkAK0LHqvtAH8NXAD6AICpXwA3AAcEHAAlqmkLrwA3ASsJ3qtCAG4LOAD1AP6gggAVABMH6gAmq10LJQBFAcUB96tSAWsCkgByAH2mXgClAH8ObQCSq3YMggB6AEYMzKuvAPEMqACtAHGlVQAAAQIMpAAXqn8PGACkALYL6avMAGgLcQDjAXCkmQA7AIUO6ABIqwYBfwAsAEwMI6slAXwKBgE9AHWuHQAqAfoEIwA3qvoK2gBFAa4JKqsXAH0NlQBtAHGmmwCZAPcMqACNq4kLRwClADoNTKvqAIMM2wBpAHel3QDXAOgLbwBZq3sHHACQAE4HEauuAAMEbAD7AA2ipgA+AH0DPQD5q+oGNgAoAUUELatBAAEDTACEAfCgAwEUAekF4QA+qn0BBAElAawE8Ks/AeYJkYPke3svSG6Z/klyEPxUrv+QzAWp/vCWNCOo9xIIIQ3gCkeiHA9aBCKSoAqPq5Z9oYFFdqR+zCvsAjp8A/z+BhbUxYL5fI6OAYaIq2+BQW5a/f+Gpyx+iJP2m/FG9n5ULKDl/jmDcI6fOMIM5ONNYKtcOSw0C0KE3YVoA8/a0ASNh9aAFP2EKRiLhYBtBleFVVas9TmSjYHvfrug1f+1f877YIYoKaaMqX4pC8MEWdjU6QLzgYGoAsNS6Pqx+3OAwAg8JZD/LQhKDPuvxNFYgw0HcIfU/EyphIRVi6YLtYOwKoR/oRUtlhiS6NksAw2HRCPwh0/UVPzZfPH+QIDcLxQGTQjlAsyWMNeAgm59KYLuBHqviIE+DU6PDICEKlt7TAuhgQP8OLLj/EKITJOPeHKuDAD1hFmDSPhYLUMEiXzFB/OEiK98i2EHsfykCk8PL3QK9hJ6EIBMqFx7QH+1hQwC0Ctsh4pwNe0kfRTUIIIQ5BSABwkllQACqSNYE8XFBc/A+MH9wv8EwMVr/sH+ycv7BQUFQQFiwjwHAdUAYWnA/2gFALnTdPtWwwkBFwNaBWL6a/8EALYFdPQOBKEpWsDBP8A4Wz25AWAdBj7Cq8PCVMH86fvB/sYAWYgI/hoBGiWfVvvw/sH9VUBZOsD5aPwFAF8ohgfAzaYBviuD/cIF/cFVxFHBGAEi6FzFav5UWv3BwDnAbWr8WAsAPC84PcdVwPzD/xIB4jRTa/7/wMD9afL9VqMBVjkQ/pFRCAU8On39wv/9AsAAq3E/DMGXG8QqQPFEOP/BW1EF/T3pwEUHAFdF1sDFaMX+CABKTjj4xmvDlAoAMFDWwcVWwpPBCAAllxrEaP96BQBHU8z8x2sNAStTV0AENmijASdeF4XATQ8Fe1hm/sLA/jrAJ1T+SREA1Vuf/VSd/cL9wP/B8goEgGRXK0pdBsUmarhvwQMApmyo/h+qKnRePsFVnsAza0H+/sNXB8UYgKfAUsIaAShEXlFU/sJVwMD9lj7GmV0aASqKXJH9x/PBwMD+/0U4wcZXwcBaAwAiVAzErQAro17+wDjCDqsKtQZoc8OiCgU4qHTAPFj/ygCwHHHAXsAv/7T/Aqomt2nB/sEH/RWrvL1waMErO/57xxUBJsVmUgVa+uTBwMDAwULRASJ7Z//CwP7BBWL4aj7AXgsA1xlpVlT/wDwGAAEkBsVUdhMA3uRrof7FkERdwMDCD8Xh7fU+/k/D/WAEFASt/WnB/00tO8D6af4vVhABEThc+MnBwP7A/XOoCBWqEP1DawsR0BFsa0TCMv8KELsWeGtc/8BCBRDFHxVow/4EENYjozgNuhMlcMNlPc8QBYIWxG54wgbUEyrJwPx6BhDq/XDEVFwDENhHaToEFKhLYEIDEJ+VdMU=";
    public static byte[] testFingerprintBytes_2 = Base64.getDecoder().decode(testFingerprintBase64_2);

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

            // Тестируем новые Base64 строки от пользователя
            log.info("=== ТЕСТИРОВАНИЕ НОВЫХ BASE64 СТРОК ОТ ПОЛЬЗОВАТЕЛЯ ===");
            
            // Тест 1: Первая Base64 строка
            log.info("--- ТЕСТ 1: Первая Base64 строка ---");
            log.info("Размер первой Base64 строки: {} символов", testFingerprintBase64_1.length());
            log.info("Размер декодированных байт: {} байт", testFingerprintBytes_1.length);
            
            try {
                log.info("Загружаем первую Base64 строку через uploadFpFromBase64...");
                zkTerminal.uploadFpFromBase64("6000", testFingerprintBase64_1, (byte) 3, (byte) 1);
                log.info("✅ Первая Base64 строка успешно загружена!");
            } catch (Exception e) {
                log.error("❌ Ошибка при загрузке первой Base64 строки: {}", e.getMessage());
                e.printStackTrace();
            }
            
            // Тест 2: Вторая Base64 строка
            log.info("--- ТЕСТ 2: Вторая Base64 строка ---");
            log.info("Размер второй Base64 строки: {} символов", testFingerprintBase64_2.length());
            log.info("Размер декодированных байт: {} байт", testFingerprintBytes_2.length);
            
            try {
                log.info("Загружаем вторую Base64 строку через uploadFpFromBase64...");
                zkTerminal.uploadFpFromBase64("6000", testFingerprintBase64_2, (byte) 4, (byte) 1);
                log.info("✅ Вторая Base64 строка успешно загружена!");
            } catch (Exception e) {
                log.error("❌ Ошибка при загрузке второй Base64 строки: {}", e.getMessage());
                e.printStackTrace();
            }
            
            // Тест 3: Прямая загрузка через uploadFp с декодированными байтами
            log.info("--- ТЕСТ 3: Прямая загрузка через uploadFp ---");
            try {
                log.info("Загружаем первую Base64 строку через uploadFp (декодированные байты)...");
                zkTerminal.uploadFp("6000", testFingerprintBytes_1, (byte) 5, (byte) 1);
                log.info("✅ Первая Base64 строка успешно загружена через uploadFp!");
            } catch (Exception e) {
                log.error("❌ Ошибка при загрузке первой Base64 строки через uploadFp: {}", e.getMessage());
                e.printStackTrace();
            }
            
            try {
                log.info("Загружаем вторую Base64 строку через uploadFp (декодированные байты)...");
                zkTerminal.uploadFp("6000", testFingerprintBytes_2, (byte) 6, (byte) 1);
                log.info("✅ Вторая Base64 строка успешно загружена через uploadFp!");
            } catch (Exception e) {
                log.error("❌ Ошибка при загрузке второй Base64 строки через uploadFp: {}", e.getMessage());
                e.printStackTrace();
            }
            
            log.info("=== КОНЕЦ ТЕСТИРОВАНИЯ НОВЫХ BASE64 СТРОК ===");

            log.info("<- end ->");
            return serialNumber;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
