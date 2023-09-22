# Peer-to-Peer-chat-Stick-Topology-Ds-Team-10
https://www.youtube.com/watch?v=vZdJBBiecVM

•	JOINING A CHAT
A new client added acts as first Peer node in stick topology, any client which joins the first peer node using IP and Port number adds a predecessor to the first Peer node. Any node that joins the chat gets added as predecessor to the last actively connected node. Example, Node n joins as a predecessor to Node (n-1).
![image](https://github.com/ggowtam/Peer-to-Peer-chat-Stick-Topology-Ds-Team-10/assets/108767096/4d4a32f3-e658-47be-b116-6eda4068ff7c)

•	LEAVE A CHAT
When a Peer decides to leave chat, the message type is sent to its successor and predecessor node. The connection with its successor is closed. The Peer node that leaves the chat sends its successor information to its predecessor, so that a new two way connection is established.


![image](https://github.com/ggowtam/Peer-to-Peer-chat-Stick-Topology-Ds-Team-10/assets/108767096/ba2b091b-3e42-4650-b495-57f0e72156ee)


•	SEND/RECEIVE MESSAGE
When a Node sends a message, the message content is sent to its successor and also to its predecessor node, which further is sent to all the further nodes in the linear stick network.


![image](https://github.com/ggowtam/Peer-to-Peer-chat-Stick-Topology-Ds-Team-10/assets/108767096/f023cf60-85cb-4822-9b1c-3fd6fc84d8c6)


•	SHUTDOWN
•	SHUTDOWNALL

