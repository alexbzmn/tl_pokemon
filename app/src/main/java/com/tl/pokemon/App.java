package com.tl.pokemon;

import static spark.Spark.get;

public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }
}
