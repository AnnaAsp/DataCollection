package com.digdes.school;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {

    private static Collection data = new Collection();

    public JavaSchoolStarter() {
    }

    public List<Map<String,Object>> execute(String request) throws Exception {
        String command = request.trim();
        List<Map<String,Object>> list = new ArrayList<>();
        if (command.matches("(?i)INSERT.+")) {
            String info = command.replaceFirst("(?i)INSERT\\s*", "");
            list = data.insert(info);
            }
        if (command.matches("(?i)UPDATE.+")) {
            String info = command.replaceFirst("(?i)UPDATE\\s*", "");
            list = data.update(info);
            }
        if (command.matches("(?i)DELETE.*")) {
            String info = command.replaceFirst("(?i)DELETE\\s*", "");
            list = data.delete(info);
            }
        if (command.matches("(?i)SELECT.+")) {
            String info = command.replaceFirst("(?i)SELECT\\s*", "");
            list = data.select(info);
        }
        if (command.matches("(?i)SELECT")) {
            list = data.getData();
        }
        if (!command.matches("(?i)INSERT.+|(?i)UPDATE.+|(?i)DELETE.*|(?i)SELECT.*")){
            Exception ex = new Exception("Введен некорректный запрос.");
            throw ex;
        }
        list.forEach(System.out::println);
        return list;
    }


}
