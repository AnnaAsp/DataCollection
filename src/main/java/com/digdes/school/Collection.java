package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Collection {
    private List<Map<String,Object>> data = new ArrayList<>();

    public List<Map<String, Object>> getData() {
        return data;
    }

    public List<Map<String, Object>> select(String info) throws Exception {
        validation(info);
        List<Map<String, Object>> select = search(info);
        return select;
    }

    public List<Map<String, Object>> insert(String info) throws Exception {
        validation(info);
        Map<String, Object> row = new HashMap<>();
        row = putMap(info, row);
        List<Map<String, Object>> output = new ArrayList<>();
        if (row.isEmpty()) {
            System.out.println("Строка пуста. Строка не сохранена.");
        } else if (data.contains(row)) {
            System.out.println("Такая строка уже есть в таблице");
            output.add(row);
        } else {
            data.add(row);
            output.add(row);
        }
        return output;
    }

    public List<Map<String, Object>> delete(String info) throws Exception {
        validation(info);
        List<Map<String, Object>> output = new ArrayList<>();
        if (info.isEmpty()) {
            output.addAll(data);
            data.clear();
        } else {
            output = search(info);
            output.forEach(map -> {
                data.remove(map);
            });
        }
        return output;
    }
    public List<Map<String, Object>> update(String info) throws Exception {
        validation(info);
        List<Map<String, Object>> output = new ArrayList<>();
        List<Map<String, Object>> out;
        String where = info.replaceFirst("\\s*(?i)values\\s*('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*=\\s*" +
                "('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+)\\s*,\\s*)*" +
                "('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*=\\s*('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'|\\d+\\.?\\d*|" +
                "[A-Z|a-z|А-Я|а-я]+))\\s*", "");
        String values = info.replaceFirst(where, "");
        if (where.isEmpty()) {
            data.forEach(map -> {
                try {
                    putMap(values, map);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (map.isEmpty()) {
                    data.remove(map);
                    System.out.println("Строка пуста. Строка удалена.");
                }
            });
            output.addAll(data);
        } else {
            out = search(where);
            out.forEach(map -> {
                data.remove(map);
                try {
                    putMap(values, map);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (map.isEmpty()) {
                    System.out.println("Строка пуста. Строка удалена.");
                    out.remove(map);
                } else {
                    data.add(map);
                }
            });
            out.forEach(map -> {
                if (!map.isEmpty()) {
                    output.add(map);
                }
            });
        }
        return output;
    }

    private void validation(String info) throws Exception {
        if (!info.matches("((?i)values\\s*('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*=\\s*('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'" +
                "|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+)\\s*,\\s*)*('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*=\\s*" +
                "('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+)\\s*))?" +
                "((?i)where\\s*(\\(*\\s*'(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)" +
                "\\s*('(%?\\s*%?[A-Z|a-z|А-Я|а-я]*%?\\s*%?)+'|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+)\\s*\\)*\\s*((?i)and|(?i)or)\\s*)*" +
                "('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*" +
                "('(%?\\s*%?[A-Z|a-z|А-Я|а-я]*%?\\s*%?)+'|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+)\\s*\\)*\\s*))?")) {
            Exception ex = new Exception("Некорректный запрос");
            throw ex;
        }
    }

    private List<Map<String, Object>> search(String info) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*" +
                "('(%?\\s*%?[A-Z|a-z|А-Я|а-я]*%?\\s*%?)+'|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+))|(?i)AND|(?i)OR");
        Matcher matcher = pattern.matcher(info);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        List<Map<String, Object>> all = new ArrayList<>();
        List<Map<String, Object>> temp = new ArrayList<>();
        for (String match : matches) {
            if (!match.equalsIgnoreCase("and") && !match.equalsIgnoreCase("or")) {
                all = compare(match, all);
            }
        }
        if (!info.matches(".+(?i)and.+")) {
            result = all;
        } else {
            temp.addAll(all);
            for (int i = 0; i < matches.size(); i++) {
                if (matches.get(i).equalsIgnoreCase("and")) {
                    temp = and(matches.get(i - 1), temp);
                    temp = and(matches.get(i + 1), temp);
                    if (i == matches.size() - 2) {
                        result.addAll(temp);
                    } else if (matches.get(i + 2).equalsIgnoreCase("or")) {
                        result.addAll(temp);
                        temp.clear();
                        temp.addAll(all);
                    }
                }
                if (matches.get(i).equalsIgnoreCase("or")) {
                    if (i == matches.size() - 2) {
                        compare(matches.get(i + 1), result);
                    } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("and")) {
                        compare(matches.get(i - 1), result);
                    } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("or")) {
                        compare(matches.get(i - 1), result);
                        compare(matches.get(i + 1), result);
                    } else if (matches.get(i + 2).equalsIgnoreCase("or")) {
                        compare(matches.get(i + 1), result);
                    }
                }
            }
        }
        return result;
    }

    private Map<String, Object> putMap(String info, Map<String, Object> row) throws Exception {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("'(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'\\s*=\\s*" +
                "('(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'|\\d+\\.?\\d*|[A-Z|a-z|А-Я|а-я]+)");
        Matcher matcher = pattern.matcher(info);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        for (String match : matches) {
            if (!match.matches("(?i)'id'.+|(?i)'lastName'.+|(?i)'age'.+|(?i)'cost'.+|(?i)'active'.+")) {
                Exception ex = new Exception("Некорректное название колонки");
                throw ex;
            }
            if (match.matches("(?i)'id'.+")) {
                String number = match.replaceFirst("'.+'\\s*=\\s*", "");
                if (number.equals("null")) {
                    row.remove("id");
                } else {
                    try {
                        long id = Long.parseLong(number);
                        row.put("id", id);
                    } catch (Exception ex) {
                        System.out.println("Некорректный тип данных для поля id");
                    }
                }
            }
            if (match.matches("(?i)'lastName'.+")) {
                String str = match.replaceFirst("'.+'\\s*=\\s*", "");
                if (str.equals("null")) {
                    row.remove("lastName");
                }
                if (!str.equals("null") && !str.matches("'(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'")) {
                    System.out.println("Некорректный тип данных для поля lastName");
                }
                if (str.matches("'(\\s*[A-Z|a-z|А-Я|а-я]+\\s*)+'")) {
                    String lastName = str.replaceAll("'", "");
                    row.put("lastName", lastName);
                }
            }
            if (match.matches("(?i)'age'.+")) {
                String number = match.replaceFirst("'.+'\\s*=\\s*", "");
                if (number.equals("null")) {
                    row.remove("age");
                } else {
                    try {
                        long age = Long.parseLong(number);
                        row.put("age", age);
                    } catch (Exception ex) {
                        System.out.println("Некорректный тип данных для поля age");
                    }
                }
            }
            if (match.matches("(?i)'cost'.+")) {
                String number = match.replaceFirst("'.+'\\s*=\\s*", "");
                if (number.equals("null")) {
                    row.remove("cost");
                } else {
                    try {
                        double cost = Double.parseDouble(number);
                        row.put("cost", cost);
                    } catch (Exception ex) {
                        System.out.println("Некорректный тип данных для поля cost");
                    }
                }
            }
            if (match.matches("(?i)'active'.+")) {
                String bool = match.replaceFirst("'.+'\\s*=\\s*", "");
                if (bool.equals("true")) {
                    row.put("active", true);
                } else if (bool.equals("false")) {
                    row.put("active", false);
                } else if (bool.equals("null")) {
                    row.remove("active");
                } else {
                    System.out.println("Некорректный тип данных для поля active");
                }
            }
        }
        return row;
    }

    private List<Map<String, Object>> compare(String match, List<Map<String, Object>> all) throws Exception {
        if (!match.matches("(?i)'id'.+|(?i)'lastName'.+|(?i)'age'.+|(?i)'cost'.+|(?i)'active'.+")) {
            Exception ex = new Exception("Некорректное название колонки");
            throw ex;
        }
        if (match.matches("(?i)'id'.+")) {
            String number = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*\\d+", "");
            if (!number.matches("\\d+")) {
                Exception ex = new Exception("Некорректный тип данных для поля id");
                throw ex;
            }
            if (comparisonOperator.equalsIgnoreCase("like") || comparisonOperator.equalsIgnoreCase("ilike")) {
                Exception ex = new Exception("Некорректный оператор сравнения для поля id");
                throw ex;
            }
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") == Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null && !all.contains(row)) {
                        all.add(row);
                        continue;
                    }
                    if ((Long) row.get("id") != Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals(">=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") >= Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("<=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") <= Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("<")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") < Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals(">")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") > Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'lastName'.+")) {
            String str = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String lastName = str.replaceAll("'", "");
            String operator = match.replaceFirst("'.+?'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*'(%?\\s*%?[A-Z|a-z|А-Я|а-я]*%?\\s*%?)+'", "");
            if (!str.matches("'(%?\\s*%?[A-Z|a-z|А-Я|а-я]*%?\\s*%?)+'")) {
                Exception ex = new Exception("Некорректный тип данных для поля lastName");
                throw ex;
            }
            if (comparisonOperator.matches(">=|<=|<|>")) {
                Exception ex = new Exception("Некорректный оператор сравнения для поля lastName");
                throw ex;
            }
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        continue;
                    }
                    if (row.get("lastName").equals(lastName) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null && !all.contains(row)) {
                        all.add(row);
                        continue;
                    }
                    if (!row.get("lastName").equals(lastName) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equalsIgnoreCase("like")) {
                String template = lastName.replaceAll("%", ".*");
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        continue;
                    }
                    if (((String) row.get("lastName")).matches(template) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equalsIgnoreCase("ilike")) {
                String template = lastName.replaceAll("%", ".*");
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        continue;
                    }
                    if (((String) row.get("lastName")).matches("(?i)" + template) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'age'.+")) {
            String number = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*\\d+", "");
            if (!number.matches("\\d+")) {
                Exception ex = new Exception("Некорректный тип данных для поля age");
                throw ex;
            }
            if (comparisonOperator.equalsIgnoreCase("like") || comparisonOperator.equalsIgnoreCase("ilike")) {
                Exception ex = new Exception("Некорректный оператор сравнения для поля age");
                throw ex;
            }
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") == Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null && !all.contains(row)) {
                        all.add(row);
                        continue;
                    }
                    if ((Long) row.get("age") != Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals(">=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") >= Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("<=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") <= Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("<")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") < Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals(">")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") > Long.parseLong(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'cost'.+")) {
            String number = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*\\d+\\.?\\d*", "");
            if (!number.matches("\\d+\\.?\\d*")) {
                Exception ex = new Exception("Некорректный тип данных для поля cost");
                throw ex;
            }
            if (comparisonOperator.equalsIgnoreCase("like") || comparisonOperator.equalsIgnoreCase("ilike")) {
                Exception ex = new Exception("Некорректный оператор сравнения для поля cost");
                throw ex;
            }
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") == Double.parseDouble(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null && !all.contains(row)) {
                        all.add(row);
                        continue;
                    }
                    if ((Double) row.get("cost") != Double.parseDouble(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals(">=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") >= Double.parseDouble(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("<=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") <= Double.parseDouble(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("<")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") < Double.parseDouble(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals(">")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") > Double.parseDouble(number) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'active'.+")) {
            String bool = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*[A-Z|a-z|А-Я|а-я]+", "");
            if (!bool.matches("(?i)true|(?i)false")) {
                Exception ex = new Exception("Некорректный тип данных для поля active");
                throw ex;
            }
            if (!comparisonOperator.equals("=") && !comparisonOperator.equals("!=")) {
                Exception ex = new Exception("Некорректный оператор сравнения для поля active");
                throw ex;
            }
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("active") == null) {
                        continue;
                    }
                    if ((Boolean) row.get("active") == Boolean.parseBoolean(bool) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("active") == null && !all.contains(row)) {
                        all.add(row);
                        continue;
                    }
                    if ((Boolean) row.get("active") != Boolean.parseBoolean(bool) && !all.contains(row)) {
                        all.add(row);
                    }
                }
            }
        }
        return all;
    }

    private List<Map<String, Object>> and(String match, List<Map<String, Object>> temp) {
        if (match.matches("(?i)'id'.+")) {
            String number = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*\\d+", "");
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        temp.remove(row);
                        continue;
                    }
                    if ((Long) row.get("id") != Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") == Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals(">=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") < Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("<=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") > Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("<")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") >= Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals(">")) {
                for (Map<String, Object> row : data) {
                    if (row.get("id") == null) {
                        continue;
                    }
                    if ((Long) row.get("id") <= Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'lastName'.+")) {
            String str = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String lastName = str.replaceAll("'", "");
            String operator = match.replaceFirst("'.+?'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*'(%?\\s*%?[A-Z|a-z|А-Я|а-я]*%?\\s*%?)+'", "");
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        temp.remove(row);
                        continue;
                    }
                    if (!row.get("lastName").equals(lastName)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        continue;
                    }
                    if (row.get("lastName").equals(lastName)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equalsIgnoreCase("like")) {
                String template = lastName.replaceAll("%", ".*");
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        continue;
                    }
                    if (!((String) row.get("lastName")).matches(template)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equalsIgnoreCase("ilike")) {
                String template = lastName.replaceAll("%", ".*");
                for (Map<String, Object> row : data) {
                    if (row.get("lastName") == null) {
                        continue;
                    }
                    if (!((String) row.get("lastName")).matches("(?i)" + template)) {
                        temp.remove(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'age'.+")) {
            String number = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*\\d+", "");
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        temp.remove(row);
                        continue;
                    }
                    if ((Long) row.get("age") != Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") == Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals(">=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") < Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("<=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") > Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("<")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") <= Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals(">")) {
                for (Map<String, Object> row : data) {
                    if (row.get("age") == null) {
                        continue;
                    }
                    if ((Long) row.get("age") <= Long.parseLong(number)) {
                        temp.remove(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'cost'.+")) {
            String number = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*\\d+\\.?\\d*", "");
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        temp.remove(row);
                        continue;
                    }
                    if ((Double) row.get("cost") != Double.parseDouble(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") == Double.parseDouble(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals(">=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") < Double.parseDouble(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("<=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") > Double.parseDouble(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("<")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") >= Double.parseDouble(number)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals(">")) {
                for (Map<String, Object> row : data) {
                    if (row.get("cost") == null) {
                        continue;
                    }
                    if ((Double) row.get("cost") <= Double.parseDouble(number)) {
                        temp.remove(row);
                    }
                }
            }
        }
        if (match.matches("(?i)'active'.+")) {
            String bool = match.replaceFirst("'.+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*", "");
            String operator = match.replaceFirst("'.+'\\s*", "");
            String comparisonOperator = operator.replaceFirst("\\s*[A-Z|a-z|А-Я|а-я]+", "");
            if (comparisonOperator.equals("=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("active") == null) {
                        temp.remove(row);
                        continue;
                    }
                    if ((Boolean) row.get("active") != Boolean.parseBoolean(bool)) {
                        temp.remove(row);
                    }
                }
            }
            if (comparisonOperator.equals("!=")) {
                for (Map<String, Object> row : data) {
                    if (row.get("active") == null) {
                        continue;
                    }
                    if ((Boolean) row.get("active") == Boolean.parseBoolean(bool)) {
                        temp.remove(row);
                    }
                }
            }
        }
        return temp;
    }

}
