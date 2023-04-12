package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Collection {
    private List<Map<String,Object>> data = new ArrayList<>();
    private List<List<Map<String, Object>>> bracketsResult = new ArrayList<>();

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
        String values = info.replaceFirst("(?i)where.*$", "");
        String where = info.replaceFirst(values, "");
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
                } else if (!data.contains(map)) {
                    data.add(map);
                } else {
                    System.out.println("Такая строка уже есть в таблице. Она будет удалена.");
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
        if (!info.matches("((?i)values\\s*('(\\s*[A-Za-zА-Яа-я]+\\s*)+'\\s*=\\s*('(\\s*[A-Za-zА-Яа-я]+\\s*)+'" +
                "|\\d+\\.?\\d*|[A-Za-zА-Яа-я]+)\\s*,\\s*)*('(\\s*[A-Za-zА-Яа-я]+\\s*)+'\\s*=\\s*" +
                "('(\\s*[A-Za-zА-Яа-я]+\\s*)+'|\\d+\\.?\\d*|[A-Za-zА-Яа-я]+)\\s*))?" +
                "((?i)where\\s*(\\(*\\s*'(\\s*[A-Za-zА-Яа-я]+\\s*)+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)" +
                "\\s*('(%?\\s*%?[A-Za-zА-Яа-я]*%?\\s*%?)+'|\\d+\\.?\\d*|[A-Za-zА-Яа-я]+)\\s*\\)*\\s*((?i)and|(?i)or)\\s*)*" +
                "('(\\s*[A-Za-zА-Яа-я]+\\s*)+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*" +
                "('(%?\\s*%?[A-Za-zА-Яа-я]*%?\\s*%?)+'|\\d+\\.?\\d*|[A-Za-zА-Яа-я]+)\\s*\\)*\\s*))?")) {
            Exception ex = new Exception("Некорректный запрос");
            throw ex;
        }
    }

    private List<Map<String, Object>> search(String info) throws Exception {
        List<Map<String, Object>> result;
        List<String> inBrackets = new ArrayList<>();
        if (!info.contains("(") && !info.contains(")")) {
            result = withoutBrackets(info, bracketsResult);
            if (info.matches("((?i)where)\\s*brackets")) {
                result = bracketsResult.get(0);
                bracketsResult.clear();
            }
        } else {
            Pattern pattern = Pattern.compile("\\([^(]+?\\)");
            Matcher matcher = pattern.matcher(info);
            while (matcher.find()) {
                inBrackets.add(matcher.group());
            }
            inBrackets.forEach(brackets -> {
                try {
                    bracketsResult.add(withoutBrackets(brackets, bracketsResult));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            String bracketsInfo = info.replaceAll("\\([^(]+?\\)", "brackets");
            result = search(bracketsInfo);
        }
        return result;
    }

    private Map<String, Object> putMap(String info, Map<String, Object> row) throws Exception {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("'(\\s*[A-Za-zА-Яа-я]+\\s*)+'\\s*=\\s*" +
                "('(\\s*[A-Za-zА-Яа-я]+\\s*)+'|\\d+\\.?\\d*|[A-Za-zА-Яа-я]+)");
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
                if (!str.equals("null") && !str.matches("'(\\s*[A-Za-zА-Яа-я]+\\s*)+'")) {
                    System.out.println("Некорректный тип данных для поля lastName");
                }
                if (str.matches("'(\\s*[A-Za-zА-Яа-я]+\\s*)+'")) {
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

    private List<Map<String, Object>> withoutBrackets (String conditions, List<List<Map<String, Object>>> bracketsResult) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("('(\\s*[A-Za-zА-Яа-я]+\\s*)+'\\s*(=|!=|(?i)like|(?i)ilike|>=|<=|<|>)\\s*" +
                "('(%?\\s*%?[A-Za-zА-Яа-я]*%?\\s*%?)+'|\\d+\\.?\\d*|[A-Za-zА-Яа-я]+))|(?i)AND|(?i)OR|brackets");
        Matcher matcher = pattern.matcher(conditions);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        List<Map<String, Object>> all = new ArrayList<>();
        List<Map<String, Object>> temp = new ArrayList<>();
        for (String match : matches) {
            if (!match.equalsIgnoreCase("and") && !match.equalsIgnoreCase("or") && !match.equalsIgnoreCase("brackets")) {
                all = compare(match, all);
            }
        }
        if (!conditions.matches(".+(?i)and.+") && !conditions.contains("brackets")) {
            result = all;
        } else {
            temp.addAll(all);
            for (int i = 0; i < matches.size(); i++) {
                if (matches.get(i).equalsIgnoreCase("and")) {
                    if (!matches.get(i - 1).matches("brackets") && !matches.get(i + 1).matches("brackets")) {
                        temp = and(matches.get(i - 1), temp);
                        temp = and(matches.get(i + 1), temp);
                    } else {
                        if (matches.get(i - 1).matches("brackets") && matches.get(i + 1).matches("brackets")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            int index2 = brackets.size() + 1;
                            List<Map<String, Object>> bracket1 = bracketsResult.get(index);
                            List<Map<String, Object>> bracket2 = bracketsResult.get(index2);
                            List<Map<String, Object>> bracket = new ArrayList<>();
                            for (Map<String, Object> map : bracket1) {
                                if (bracket2.contains(map) && bracket1.contains(map)) {
                                    bracket.add(map);
                                }
                            }
                            temp.addAll(bracket);
                            bracketsResult.remove(bracket1);
                            bracketsResult.remove(bracket2);
                        }
                        if (matches.get(i - 1).matches("brackets") && !matches.get(i + 1).matches("brackets")) {
                            temp = and(matches.get(i + 1), temp);
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            List<Map<String, Object>> temp2 = new ArrayList<>();
                            for (Map<String, Object> map : temp) {
                                if (!bracket.contains(map)) {
                                    temp2.add(map);
                                }
                            }
                            for (Map<String, Object> map : temp2) {
                                temp.remove(map);
                            }
                            bracketsResult.remove(bracket);
                        }
                        if (!matches.get(i - 1).matches("brackets") && matches.get(i + 1).matches("brackets")) {
                            temp = and(matches.get(i - 1), temp);
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i + 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            List<Map<String, Object>> temp2 = new ArrayList<>();
                            for (Map<String, Object> map : temp) {
                                if (!bracket.contains(map)) {
                                    temp2.add(map);
                                }
                            }
                            for (Map<String, Object> map : temp2) {
                                temp.remove(map);
                            }
                            bracketsResult.remove(bracket);
                        }
                    }
                    if (i == matches.size() - 2) {
                        for (Map<String, Object> map : temp) {
                            if (!result.contains(map)) {
                                result.add(map);
                            }
                        }
                    } else if (matches.get(i + 2).equalsIgnoreCase("or")) {
                        for (Map<String, Object> map : temp) {
                            if (!result.contains(map)) {
                                result.add(map);
                            }
                        }
                        temp.clear();
                        temp.addAll(all);
                    }
                }
                if (matches.get(i).equalsIgnoreCase("or")) {
                    if (!matches.get(i - 1).matches("brackets") && !matches.get(i + 1).matches("brackets")) {
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
                    } else if (matches.get(i - 1).matches("brackets") && matches.get(i + 1).matches("brackets")) {
                        if (i == matches.size() - 2) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size() + 1;
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket);
                            if (i == 1) {
                                List<Map<String, Object>> bracketList = bracketsResult.get(index - 1);
                                for (Map<String, Object> map : bracketList) {
                                    if (!result.contains(map)) {
                                        result.add(map);
                                    }
                                }
                                bracketsResult.remove(bracketList);
                            }
                        } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("and")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket);
                        } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("or")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            int index2 = brackets.size() + 1;
                            List<Map<String, Object>> bracket1 = bracketsResult.get(index);
                            List<Map<String, Object>> bracket2 = bracketsResult.get(index2);
                            for (Map<String, Object> map : bracket1) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            for (Map<String, Object> map : bracket2) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket1);
                            bracketsResult.remove(bracket2);
                        } else if (matches.get(i + 2).equalsIgnoreCase("or")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size() + 1;
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket);
                        }
                    } else if (matches.get(i - 1).matches("brackets") && !matches.get(i + 1).matches("brackets")) {
                        if (i == matches.size() - 2) {
                            if (i == 1) {
                                String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                                List<String> brackets = new ArrayList<>();
                                Pattern p = Pattern.compile("brackets");
                                Matcher m = p.matcher(substring);
                                while (m.find()) {
                                    brackets.add(m.group());
                                }
                                int index = brackets.size();
                                List<Map<String, Object>> bracket = bracketsResult.get(index);
                                for (Map<String, Object> map : bracket) {
                                    if (!result.contains(map)) {
                                        result.add(map);
                                    }
                                }
                                bracketsResult.remove(bracket);
                            }
                            compare(matches.get(i + 1), result);
                        } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("and")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket);
                        } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("or")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i - 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            compare(matches.get(i + 1), result);
                            bracketsResult.remove(bracket);
                        } else if (matches.get(i + 2).equalsIgnoreCase("or")) {
                            compare(matches.get(i + 1), result);
                        }
                    } else if (!matches.get(i - 1).matches("brackets") && matches.get(i + 1).matches("brackets")) {
                        if (i == matches.size() - 2) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i + 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket);
                            if (i == 1) {
                                compare(matches.get(i - 1), result);
                            }
                        } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("and")) {
                            compare(matches.get(i - 1), result);
                        } else if (i == 1 && matches.get(i + 2).equalsIgnoreCase("or")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i + 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            compare(matches.get(i - 1), result);
                            bracketsResult.remove(bracket);
                        } else if (matches.get(i + 2).equalsIgnoreCase("or")) {
                            String substring = conditions.substring(0, conditions.indexOf(matches.get(i + 1)));
                            List<String> brackets = new ArrayList<>();
                            Pattern p = Pattern.compile("brackets");
                            Matcher m = p.matcher(substring);
                            while (m.find()) {
                                brackets.add(m.group());
                            }
                            int index = brackets.size();
                            List<Map<String, Object>> bracket = bracketsResult.get(index);
                            for (Map<String, Object> map : bracket) {
                                if (!result.contains(map)) {
                                    result.add(map);
                                }
                            }
                            bracketsResult.remove(bracket);
                        }
                    }
                }
            }
        }
        return result;
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
            String comparisonOperator = operator.replaceFirst("\\s*'(%?\\s*%?[A-Za-zА-Яа-я]*%?\\s*%?)+'", "");
            if (!str.matches("'(%?\\s*%?[A-Za-zА-Яа-я]*%?\\s*%?)+'")) {
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
            String comparisonOperator = operator.replaceFirst("\\s*[A-Za-zА-Яа-я]+", "");
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
            String comparisonOperator = operator.replaceFirst("\\s*'(%?\\s*%?[A-Za-zА-Яа-я]*%?\\s*%?)+'", "");
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
            String comparisonOperator = operator.replaceFirst("\\s*[A-Za-zА-Яа-я]+", "");
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
