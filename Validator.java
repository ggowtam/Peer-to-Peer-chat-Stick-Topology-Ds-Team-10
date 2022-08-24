package helpers;

public class Validator {
//determines if the user input port number is valid
	public static boolean isValidPort(String input) {
		return isNumeric(input) && isInPortRange(Integer.parseInt(input));
	}

	// to check if the user input is numeric only
	private static boolean isNumeric(String value) {

		for (int i = 0; i < value.length(); i++) {

			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	// range to define valid port numbers
	private static boolean isInPortRange(int port) {
		return port >= 1024 && port <= 65535;
	}

	// When inputs are splits : args[0] should be "join" arg[1] should be ip address
	// arg[2] should be a valid port number

	public static boolean isValidjoin(String input) {
		String[] args = input.split(" ");
		return args.length == 3 && args[0].equals("join") && Validator.isValidPort(args[2]);
	}
}
