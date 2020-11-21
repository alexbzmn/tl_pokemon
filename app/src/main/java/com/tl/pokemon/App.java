package com.tl.pokemon;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {

	public String getGreeting() {
		return "Hello World!";
	}

	public static void main(String[] args) {
		port(8080);
		get("/hello", (req, res) -> "Hello World");
	}
}
