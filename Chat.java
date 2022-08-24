package app;

import java.io.IOException;

import models.ChatApp;

//the class uses ChatApp object to implement the actions by clients
public class Chat {

	public static void main(String[] args) {

		try {
			ChatApp chat = new ChatApp();
			chat.acceptInputs();
		} catch (IOException e) {
			System.out.println(e);
		}

	}
}
