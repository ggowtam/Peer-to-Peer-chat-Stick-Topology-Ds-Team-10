package models;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import helpers.JSONHelper;
import helpers.Type;
import helpers.Validator;

// class accepts all the user inputs and uses conditional statements its equivalent logic is delivered
public class ChatApp {

	private List<Peer> joinedPeers;
	private Integer listenPort;
	private String myIP;
	private ServerSocket listenSocket;
	private final int MAX_joinIONS = 3;
	private BufferedReader input;
	private Map<Peer, DataOutputStream> peerOutputMap;

	public ChatApp() throws IOException {

		myIP = Inet4Address.getLocalHost().getHostAddress();

		// tracks all the clients which have active connection to host
		joinedPeers = new ArrayList<Peer>();

		input = new BufferedReader(new InputStreamReader(System.in));

		// peers get mapped to o/p stream
		peerOutputMap = new HashMap<Peer, DataOutputStream>();
	}

	// for testing purposes
	public ChatApp(int port) throws IOException {
		this();
		listenPort = port;
		listenSocket = new ServerSocket(listenPort);
	}

// the first client joine becomes the active client
	private void startActiveClient() throws IOException {

		// each client is served on different thread
		new Thread(() -> {
			while (true) {
				try {
					// connection open for peers to join
					Socket joinSocket = listenSocket.accept();

					// once there is a join, serve them on thread
					new Thread(new PeerHandler(joinSocket)).start();

				} catch (IOException e) {

				}
			}
		}).start();
	}

	// open an IO stream for each peer joined to the host
	// display all the messages send to the host by that peer
	private class PeerHandler implements Runnable {

		private Socket peerSocket;

		public PeerHandler(Socket socket) {
			this.peerSocket = socket;
		}

		public void run() {

			try {
				BufferedReader input = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));

				// read all messages sent to the host
				while (true) {
					String jsonStr = input.readLine();

					// when the other end of the input stream is closed,
					// will received null; when null, close thread
					if (jsonStr == null) {
						return;
					}

					String ip = JSONHelper.parse(jsonStr, "ip");
					int port = Integer.valueOf(JSONHelper.parse(jsonStr, "port"));

					// each JSON string received/written can be of 4 types
					Type type = Type.valueOf(JSONHelper.parse(jsonStr, "type"));
					switch (type) {
					case JOIN:
						displayjoinSuccess(jsonStr);
						break;
					case MESSAGE:
						String message = JSONHelper.parse(jsonStr, "message");
						displayMessage(ip, port, message);
						break;
					case LEAVE:
						displayleaveMessage(ip, port);
						leavejoinion(findPeer(ip, port));
						removePeer(findPeer(ip, port));
						input.close();
						return;
					case SHUTDOWN:
						listenSocket.close();
						return;
					}
				}
			} catch (IOException e) {
				System.out.println("Message: connection drop");
			}
		}
	}

	// all the active clients have the list of actions displayed
	private void displayManual() {
		for (int i = 0; i < 100; i++)
			System.out.print("-"); // header
		System.out.println("\nchat <port number>\t Run chat listening on <port number>");
		System.out.println("\nhelp\tDisplay information about the available user interface commands.");
		System.out.println("\nmyip\t Display your IP address.");
		System.out.println("\nmyport\t Display the port # on which this process is listening for incoming joinions.");
		System.out.println("\njoin\t <destination> <port no> This command establishes a new TCP joinion to the "
				+ "specified <destination> at the \nspecified <port no>. <destination> is the IP address of the destination.");
		System.out.println("\nlist Display a list of all the peers you are joined to. More specifically, it displays"
				+ "the index id #, IP address, and port # of each peer.");
		System.out.println("\nleave <joinion id> leave the joinion to a peer by their id given in the list command.");
		System.out.println(
				"\nsend\t <joinion id.> <message> Send a message to a peer by their id given in the list command."
						+ "The message to be sent can be \nup-to 100 characters long, including blank spaces.");
		System.out.println("\nexit\t Close all joinions and leave this process.");
		for (int i = 0; i < 100; i++)
			System.out.print("-"); // footer
		System.out.println("\n");
	}

	// accept user i/p
	public void acceptInputs() throws IOException {
		System.out.println("Welcome to Chat");

		while (true) {
			System.out.print("-> ");
			String choice = input.readLine();
			// the first argument is the command
			String option = choice.split(" ")[0].toLowerCase();

			switch (option) {
			case "chat":
				if (listenSocket == null)
					initChat(choice);
				else
					System.out.println("Error: you can only listen to one port at a time");
				break;
			case "help":
				displayManual();
				break;
			case "myip":
				System.out.println("My IP Address: " + myIP);
				break;
			case "myport":
				if (listenSocket == null)
					System.out.println("Error: you are not joined");
				else
					System.out.println("Listening on port: " + listenPort);
				break;
			case "join":
				if (listenSocket == null)
					System.out.println("Error: you are not joined");
				else
					processjoin(choice);
				break;
			case "list":
				if (listenSocket == null)
					System.out.println("Error: you are not joined");
				else
					displayList();
				break;
			case "send":
				if (listenSocket == null)
					System.out.println("Error: you are not joined");
				else
					processSend(choice);
				break;
			case "leave":
				if (listenSocket == null)
					System.out.println("Error: you are not joined");
				else
					processleave(choice);
				break;
			case "shutdown":
				if (listenSocket == null)
					System.out.println("Error: you are not joined");
				else
					processShutdown(choice);
				break;
			case "shutdownall":
				breakPeerjoinions();
				System.exit(0);
				break;
			default:
				System.out.println("not a recognized command");
			}
		}
	}

	// display the list of peers that are joined to the host
	private void displayList() {
		if (joinedPeers.isEmpty())
			System.out.println("No peers joined.");
		else {
			System.out.println("id:   IP Address     Port No.");
			for (int i = 0; i < joinedPeers.size(); i++) {
				Peer peer = joinedPeers.get(i);
				System.out.println((i + 1) + "    " + peer.getHost() + "     " + peer.getPort());
			}
			System.out.println("Total Peers: " + joinedPeers.size());
		}
	}

	// find the client on host's list (joinedPeers)
	// and write to them a message
	private void sendMessage(Peer peer, String jsonString) {
		try {
			// "\r\n" so when readLine() is called,
			// it knows when to stop reading
			peerOutputMap.get(peer).writeBytes(jsonString + "\r\n");

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// create a socket, join to peer
	private void join(String ip, int port) throws IOException {

		int attempts = 0;
		final int MAX_ATTEMPTS = 5;
		final int SLEEP_TIME = 1000;
		Socket peerSocket = null;

		// try to join but will stop after MAX_ATTEMPTS
		do {
			try {
				peerSocket = new Socket(ip, port);
			} catch (IOException e) {

				System.out.println("*** join failed...attempt: " + (++attempts) + " ***");
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e1) {

				}
			}
		} while (peerSocket == null && attempts < MAX_ATTEMPTS);

		// add (save) the socket so they can be use later
		if (attempts >= MAX_ATTEMPTS) {
			System.out.println("join was unsuccessful, please try again later");
		} else {
			System.out.println("joined to " + ip + " " + port);
			Peer peer = new Peer(ip, port);
			joinedPeers.add(peer);

			// map this peer to an output stream
			peerOutputMap.put(peer, new DataOutputStream(peerSocket.getOutputStream()));

			// tell the peer your host address and port number
			// tell the peer to join to you
			sendMessage(peer, generatejoinJson());
		}
	}

	// tells each peer to close their join with this process; process closes all of
	// its own join.

	private void breakPeerjoinions() throws IOException {

		// leave each peer joinion; notify them
		for (Peer peer : joinedPeers) {
			sendMessage(peer, generateleaveJson());
			leavejoinion(peer);
		}

		// close each output stream
		for (Entry<Peer, DataOutputStream> e : peerOutputMap.entrySet()) {
			e.getValue().close();
		}

		listenSocket.close();
		System.out.println("chat client close, good bye");
	}

	// remove the peer from all data structure

	private void removePeer(Peer peer) {
		joinedPeers.remove(peer);
		peerOutputMap.remove(peer);
	}

	// @return a JSON String that indicate to another peer (client) to join to the
	// host socket. For more information, see JSONHelper.makeJson()

	private String generatejoinJson() {
		return JSONHelper.makeJson(Type.JOIN, myIP, listenPort).toJSONString();
	}

	// @return a JSON String that indicate to another peer (client) to join to the
	// host socket. For more information, see JSONHelper.makeJson()

	private String generateMessageJson(String message) {
		return JSONHelper.makeJson(Type.MESSAGE, myIP, listenPort, message).toJSONString();
	}

	// @return a JSON String that indicate to another peer (client) to join to the
	// host socket. For more information, see JSONHelper.makeJson()

	private String generateleaveJson() {
		return JSONHelper.makeJson(Type.LEAVE, myIP, listenPort).toJSONString();
	}

	// id = index, thus 0 to size() - 1
	private boolean isValidPeer(int id) {
		return id >= 0 && id < joinedPeers.size();
	}

	private Peer findPeer(String ip, int port) {
		for (Peer p : joinedPeers)
			if (p.getHost().equals(ip) && p.getPort() == port)
				return p;
		return null;
	}

	// close the peer socket and output stream

	private void leavejoinion(Peer peer) {
		try {
			peer.getSocket().close();
			peerOutputMap.get(peer).close();
		} catch (IOException e) {

		}
	}

	// display message received from peer along with their port and IP address

	private void displayMessage(String ip, int port, String message) {
		System.out.println("\nMessage received from IP: " + ip);
		System.out.println("Sender's Port: " + port);
		System.out.println("Message: " + message);

		// "->" doesn't display after the user receive a
		// message
		System.out.print("-> ");
	}

	// display a notification that a client has joined to the host. create a peer
	// object, with peer's joinion info, and add them to joinedPeers also add the
	// peer to a map with an output stream object.

	private void displayjoinSuccess(String jsonStr) throws IOException {
		String ip = JSONHelper.parse(jsonStr, "ip");
		int port = Integer.valueOf(JSONHelper.parse(jsonStr, "port"));
		System.out.println("\nPeer [ip: " + ip + ", port: " + port + "] joins to you");
		System.out.print("-> ");
		// save peer's info, used for a lot of other stuff
		Peer peer = new Peer(ip, port);
		joinedPeers.add(peer);
		peerOutputMap.put(peer, new DataOutputStream(peer.getSocket().getOutputStream()));
	}

	private void displayleaveMessage(String ip, int port) {
		System.out.println();
		System.out.println("Peer [ip: " + ip + " port: " + port + "] has leaved the joinion");
		System.out.print("-> ");
	}

	private void processjoin(String userInput) throws IOException {
		String[] args = userInput.split(" ");
		String ip;
		int port;

		// check if the user input is "valid"
		if (!Validator.isValidjoin(userInput)) {
			System.out.println("join fail: invalid arguments");
			return;
		}

		ip = args[1];
		port = Integer.valueOf(args[2]);

		// check if ion limited is exceeded
		if (joinedPeers.size() >= MAX_joinIONS) {
			System.out.println("join fail: max joinion");
			return;
		}

		// check for self/duplicate joinions
		if (!isUniquejoinion(ip, port)) {
			System.out.println("join fail: no self or duplicate joinion");
			return;
		}

		// all tests passed, join to the peer
		join(ip, port);
	}

// returns true if the ip and port and not identical to the host AND not the  same as another peers to which the host is joined to. in other  words, all join must be unique.

	public boolean isUniquejoinion(String ip, int port) {
		return !isSelfjoinion(ip, port) && isUniquePeer(ip, port);
	}

	private boolean isUniquePeer(String ip, int port) {
		return findPeer(ip, port) == null;
	}

	private boolean isSelfjoinion(String ip, int port) {
		return ip.equals(myIP) && listenPort == port;
	}

	// used for testing purpose
	public void setjoinedPeers(List<Peer> peers) {
		if (peers.size() <= MAX_joinIONS)
			joinedPeers = peers;
	}

	private void processSend(String userInput) {
		String[] args = userInput.split(" ");
		if (args.length >= 3) {
			try {
				int id = Integer.valueOf(args[1]) - 1;
				if (isValidPeer(id)) {
					String msg = "";
					for (int i = 2; i < args.length; i++)
						msg += args[i] + " ";
					sendMessage(joinedPeers.get(id), generateMessageJson(msg));
				} else {
					System.out.println("Error: Please select a valid peer id from the list command.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Error: Second argument should be a integer.");
			}
		} else {
			System.out.println("Error: Invalid format for 'send' command. See 'help' for details.");
		}
	}

	private void processleave(String userInput) {
		String[] args = userInput.split(" ");
		if (args.length == 2) {
			try {
				int id = Integer.valueOf(args[1]) - 1;
				if (isValidPeer(id)) {
					// notify peer that joinion will be drop
					Peer peer = joinedPeers.get(id);
					sendMessage(peer, generateleaveJson());
					System.out.println("You dropped peer [ip: " + peer.getHost() + " port: " + peer.getPort() + "]");
					leavejoinion(peer);
					removePeer(peer);
				} else {
					System.out.println("Error: Please select a valid peer id from the list command.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Error: Second argument should be a integer.");
			}
		} else {
			System.out.println("Error: Invalid format for 'leave' command. See 'help' for details.");
		}
	}

	private void processShutdown(String userInput) {
		String[] args = userInput.split(" ");
		if (args.length == 2) {
			try {
				int id = Integer.valueOf(args[1]) - 1;
				if (isValidPeer(id)) {
					// notify peer that connections will be drop
					Peer peer = joinedPeers.get(id);
					sendMessage(peer, generateleaveJson());
					System.out.println("All peers lost connection");
					leavejoinion(peer);
					removePeer(peer);
				} else {
					System.out.println("Error: Please select a valid peer id from the list command.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Error: Second argument should be a integer.");
			}
		} else {
			System.out.println("Error: Invalid format for 'leave' command. See 'help' for details.");
		}
	}

	// Try to create a listening socket based on user input.

	private ServerSocket createListenSocket(String choice) throws IOException {

		if (isValidPortArg(choice)) {
			int port = Integer.valueOf(choice.split(" ")[1]);
			try {
				return listenSocket = new ServerSocket(port);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private boolean isValidPortArg(String choice) {
		String[] args = choice.split(" ");

		// check if the argument length is 2
		if (args.length != 2) {
			System.out.println("invalid arguments: given: " + args.length + " expected: 2");
			return false;
		}

		// check if the port argument is valid
		if (!Validator.isValidPort(args[1])) {
			System.out.println("invalid port number");
			return false;
		}

		return true;
	}

	private void initChat(String choice) throws IOException {
		listenSocket = createListenSocket(choice);

		if (listenSocket != null) {
			listenPort = listenSocket.getLocalPort();
			myIP = Inet4Address.getLocalHost().getHostAddress();
			System.out.println("you are listening on port: " + listenPort);
			startActiveClient();
		}
	}
}
