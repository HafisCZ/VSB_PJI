package kirin.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class NetworkConnection {

	private ConnectionThread connectionThread = new ConnectionThread();
	private Consumer<Serializable> onReceiveCallback;
	
	public NetworkConnection(Consumer<Serializable> onReceiveCallback) {
		this.onReceiveCallback = onReceiveCallback;
		connectionThread.setDaemon(true);
	}
	
	public void start() throws Exception {
		connectionThread.start();
	}
	
	public void send(Serializable data) throws Exception {
		connectionThread.out.writeObject(data);
	}
	
	public void close() throws Exception {
		connectionThread.socket.close();
	}
	
	protected abstract boolean isServer();
	protected abstract String getIP();
	protected abstract int getPort();
	
	private class ConnectionThread extends Thread {
		
		private Socket socket;
		private ObjectOutputStream out;
		
		@Override
		public void run() {
			try {
				@SuppressWarnings("resource")
				ServerSocket server = isServer() ? new ServerSocket(getPort()) : null;
				Socket socket = isServer() ? server.accept() : new Socket(getIP(), getPort());
				this.socket = socket;
				
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				this.out = out;
				
				socket.setTcpNoDelay(true);
				
				while (true) {
					Serializable data = (Serializable) in.readObject();
					onReceiveCallback.accept(data);
				}
			} catch (Exception e) {
				onReceiveCallback.accept("Connection closed");
			}
		}
	}
	
}
