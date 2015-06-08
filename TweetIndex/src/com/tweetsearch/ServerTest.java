package com.tweetsearch;

import static spark.Spark.*;

public class ServerTest {

	public static void main(String[] args) {
		get("/hello", (req, res) -> {
			res.type("application/json");
			return "{\"yolo\": \"swag\"}";
		});
	}

}
