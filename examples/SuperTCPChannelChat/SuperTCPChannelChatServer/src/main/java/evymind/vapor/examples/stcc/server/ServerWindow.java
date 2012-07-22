package evymind.vapor.examples.stcc.server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerWindow {

	private ChatServer server;
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerWindow window = new ServerWindow();
					window.frame.setVisible(true);
					window.frame.setLocationRelativeTo(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public ServerWindow() throws Exception {
		server = new ChatServer();
		initialize();
		server.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					server.shutdown();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		frame.setBounds(100, 100, 326, 81);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblChatServer = new JLabel("Chat Server");
		lblChatServer.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		lblChatServer.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lblChatServer, BorderLayout.CENTER);
	}

}
