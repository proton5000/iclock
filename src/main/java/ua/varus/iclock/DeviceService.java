package ua.varus.iclock;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.varus.iclock.utils.ZKTerminalV;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final ZKTerminalV zkTerminal;

    @PostConstruct
    public String getTerminalInfo() {

        try {

            String serialNumber = zkTerminal.getSerialNumber();
            zkTerminal.connectAuth(2804);
            System.out.println("Serial number: " + serialNumber);

//            zkTerminal.delUser(10500);

//            UserInfo userInfo = new UserInfo(10500, "1111", "tesT", "pasS", USER_DEFAULT, 0);
//            zkTerminal.modifyUserInfo(userInfo);

//            zkTerminal.enrollFinger(10500, 8, "1111");

            zkTerminal.getAllUsers().forEach(u -> {
                        System.out.println("User name: " + u.getName());
                        System.out.println("User password: " + u.getPassword());
                        System.out.println("User userid: " + u.getUserid());
                    }
            );

            String str1 = "TJZTUzIxAAAF1dYECAUHCc7QAAAp1HYBAAAAhXg8i9WiANEOeAAAAPnafQDdAAIPBwB61b8PyADTAK8PiNVgAKIP3ABPAMza5gChAM4PFABi1b0MOACdAHUN/9XCANoPBgFVAFHYLgBiANANvgAv1ZIP9ABEAHsNaNUSAI8O/ADaAXXapgBTAX8OCgBJ1HcNCgEeAAEHgtWOALcNZQBqAAPaagCBACIKaADg1eoPbQByANUIf9VhAJEJ3gACANnaQQCSALkOTQAB1AIPnAAJAUEO/tWHAM4PrQD2AK3aFACTAKoNAAAl1PEPEQB6AHMPWNUbAIoN9wDiAL7aTgASABENeABW1PQNPQBnAc8CftWMAKYNqQATAO7anQBvALMPbABs1bIPjgDyAEAPaNVpAGcIcACnABDcuwD4AO0PrwBd1XYJRQBsACcLINW9AKMOAAEaAGHaSAA/AGIN2QBh1cAPBwFYAA0PvtU8AfcNWgCDAY/aOQA9AQsJpgBQ1AsP4wBpAT0DZMhULwrWvf7s+1v7OAqhf7ICEPec0mMMAZKWgEeAZMT88V2P5f6MD7cpYAaOAZ592AP/IEcKqfgqDZsJJNbce1IOzYhfibdaLBHB8hYeXAeH3D4c5fuJB1//ZNEc/3aAk4K2/9LaAI7iiPoKuIGgVzOBLQkSC8b/Vcb86V6UnQ+XYnfiJBR5fSG+iAC413+AMKu18Pz8bapYLIUdEXZkB2vHIAiO2koTiIGMVDMCiYGlAy+Z1KkvBA4BYxXa9jbfkPbi/dbzG/hD08t/nfYFDnfqiK3k/A75tfnM//4ghIhyhLazuIEIr0gCZgMyA5OD5SZ0hf3XzQBXgMPXvABtEWIl+ABcxu/0SYUlA2SH6YNZWwXYwLKhhxF2PAYCaUIG8WdExRzpMRbmKlt+syCP+7ft1Y9zi6IrO+7223qXePEQ28vjWQ8H/KoNwNOkgup4IYBHgVNWg4GyA4sA5IDj1g4AMoUeB+oDRT7LZ1/QPBjlQQTX3ij7FgBgxRD76/5BRP3B+zok+i4eAHMAFv4FNzaX//7+/MD+4v37Ff/9wfzA/tEAm9Uf/jhEO1Q5//rzPQC3ACD+Ov/6KzxTwCr/Ljg4MJTB/sL8wP44/fko/////sD9OsD4Kv/A/v79/Dj9+tkBzgAeKDWDwQPV2gAiKP4ExecF8/3/CwCTAds1Q5MNAMUBIv0EK/iCwwQA8wEw7w0FKQA0/jjAwQT9XdABhwIePgnFrgfxMcE1CwBt1ZeTF8PD/6MPAKkWlVmPwpfDWgjFdB3J/j7A/SIAnxyJFMLCp53B/0bB+kPBiMHBknwHBQTDH0zBWQgBzyRDmDoIARsmRodVAdVJPXexBgCVQAYrKAQASkNmAcEJ1B5ETMBg/4BFCNX1S0ZX/8GENQPVAVI6wMJ/1wEgg05XwsA2/jr+xJT/AwBkWH0GEQVEXKmRwcHBAa6MrQgAaWB6xGvDxBMRAB9iwP/h+/iU/jP+BQBvoBr7Kf8GAJNlNwX+Gs0BamlxxsUGxcYQw8jFwsTBB8HBF8LFx8LAw80BIr9H/f9t/gPFAHL4wwYAYHhAAMPDGgwBJ4RPVDr+VCpUBQAAhy2yDQUvjFM3O8H9OMH40wAHk1bB/vgOBdWYLYlJwICkDgXPmCnB/sOEB//HFYUMASeaSTj/+5/B/0UNAABoJMSt/sGEcgQB47dfgBEAAcAgwpxmcRb+cRoBJcefRMUqQcA+PjjBjkcA1drLZsEsDsUByMtuwGZzhhrEJ9aPwf9AwEX+Ojf7FTxoOBcAABwgeqH+wmvCwXtBwsXdAf3fZkc4zwEiNFf//v8+T90BIiFfRPzAwv84wjvrwf//ZP0I1BMdvFbALhMQEtwXxxT+XcPAWsOYcBLEJxtrwFz8gsA7jv/BwML9/8wRJ/xjQzLAChHmNlv+/kT/BhBnpZN6KwoQHToQYrpYAsQaS2L8wMCYBRX6XQzCOwUR3151K20=";
            byte[] template1 = Base64.getDecoder().decode(str1);

            String str2 = "TOhTUzIxAAAFq6wECAUHCc7QAAApqnYBAAAAhVY9fau1APYPhABXAPGnZwCUAIILRAB5q4kMQwCXAEANfKtpAI4PNwC6AJWmgAAVAXwLXwBUq/gO4QDkAK0LHqvtAH8NXAD6AICpXwA3AAcEHAAlqmkLrwA3ASsJ3qtCAG4LOAD1AP6gggAVABMH6gAmq10LJQBFAcUB96tSAWsCkgByAH2mXgClAH8ObQCSq3YMggB6AEYMzKuvAPEMqACtAHGlVQAAAQIMpAAXqn8PGACkALYL6avMAGgLcQDjAXCkmQA7AIUO6ABIqwYBfwAsAEwMI6slAXwKBgE9AHWuHQAqAfoEIwA3qvoK2gBFAa4JKqsXAH0NlQBtAHGmmwCZAPcMqACNq4kLRwClADoNTKvqAIMM2wBpAHel3QDXAOgLbwBZq3sHHACQAE4HEauuAAMEbAD7AA2ipgA+AH0DPQD5q+oGNgAoAUUELatBAAEDTACEAfCgAwEUAekF4QA+qn0BBAElAawE8Ks/AeYJkYPke3svSG6Z/klyEPxUrv+QzAWp/vCWNCOo9xIIIQ3gCkeiHA9aBCKSoAqPq5Z9oYFFdqR+zCvsAjp8A/z+BhbUxYL5fI6OAYaIq2+BQW5a/f+Gpyx+iJP2m/FG9n5ULKDl/jmDcI6fOMIM5ONNYKtcOSw0C0KE3YVoA8/a0ASNh9aAFP2EKRiLhYBtBleFVVas9TmSjYHvfrug1f+1f877YIYoKaaMqX4pC8MEWdjU6QLzgYGoAsNS6Pqx+3OAwAg8JZD/LQhKDPuvxNFYgw0HcIfU/EyphIRVi6YLtYOwKoR/oRUtlhiS6NksAw2HRCPwh0/UVPzZfPH+QIDcLxQGTQjlAsyWMNeAgm59KYLuBHqviIE+DU6PDICEKlt7TAuhgQP8OLLj/EKITJOPeHKuDAD1hFmDSPhYLUMEiXzFB/OEiK98i2EHsfykCk8PL3QK9hJ6EIBMqFx7QH+1hQwC0Ctsh4pwNe0kfRTUIIIQ5BSABwkllQACqSNYE8XFBc/A+MH9wv8EwMVr/sH+ycv7BQUFQQFiwjwHAdUAYWnA/2gFALnTdPtWwwkBFwNaBWL6a/8EALYFdPQOBKEpWsDBP8A4Wz25AWAdBj7Cq8PCVMH86fvB/sYAWYgI/hoBGiWfVvvw/sH9VUBZOsD5aPwFAF8ohgfAzaYBviuD/cIF/cFVxFHBGAEi6FzFav5UWv3BwDnAbWr8WAsAPC84PcdVwPzD/xIB4jRTa/7/wMD9afL9VqMBVjkQ/pFRCAU8On39wv/9AsAAq3E/DMGXG8QqQPFEOP/BW1EF/T3pwEUHAFdF1sDFaMX+CABKTjj4xmvDlAoAMFDWwcVWwpPBCAAllxrEaP96BQBHU8z8x2sNAStTV0AENmijASdeF4XATQ8Fe1hm/sLA/jrAJ1T+SREA1Vuf/VSd/cL9wP/B8goEgGRXK0pdBsUmarhvwQMApmyo/h+qKnRePsFVnsAza0H+/sNXB8UYgKfAUsIaAShEXlFU/sJVwMD9lj7GmV0aASqKXJH9x/PBwMD+/0U4wcZXwcBaAwAiVAzErQAro17+wDjCDqsKtQZoc8OiCgU4qHTAPFj/ygCwHHHAXsAv/7T/Aqomt2nB/sEH/RWrvL1waMErO/57xxUBJsVmUgVa+uTBwMDAwULRASJ7Z//CwP7BBWL4aj7AXgsA1xlpVlT/wDwGAAEkBsVUdhMA3uRrof7FkERdwMDCD8Xh7fU+/k/D/WAEFASt/WnB/00tO8D6af4vVhABEThc+MnBwP7A/XOoCBWqEP1DawsR0BFsa0TCMv8KELsWeGtc/8BCBRDFHxVow/4EENYjozgNuhMlcMNlPc8QBYIWxG54wgbUEyrJwPx6BhDq/XDEVFwDENhHaToEFKhLYEIDEJ+VdMU=";
            byte[] template2 = Base64.getDecoder().decode(str2);


            System.out.println("<- end ->");

            return serialNumber;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
