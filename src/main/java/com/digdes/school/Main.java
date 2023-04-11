package com.digdes.school;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        JavaSchoolStarter collection = new JavaSchoolStarter();

        while (true) {
            System.out.println("Пожалуйста, введите запрос");
            String in =  new Scanner(System.in).nextLine();
            collection.execute(in);
        }

    }
}
