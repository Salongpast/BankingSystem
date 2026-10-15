package com.ciic;

public class Main {
    public static void main(String[] args) {
        try {
            new BankingMenu().run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
