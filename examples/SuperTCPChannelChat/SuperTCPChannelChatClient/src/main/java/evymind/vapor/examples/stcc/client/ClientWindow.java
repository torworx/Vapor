package evymind.vapor.examples.stcc.client;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import evyframework.common.StringUtils;
import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.examples.stcc.ChatServerService;
import evymind.vapor.examples.stcc.LoginService;
import evymind.vapor.examples.stcc.MessageEvent;
import evymind.vapor.examples.stcc.ServerShutdownEvent;
import evymind.vapor.examples.stcc.UserLoginEvent;
import evymind.vapor.examples.stcc.UserLogoutEvent;
import evymind.vapor.examples.stcc.client.utils.HexUtils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.List;

public class ClientWindow {

	private SuperTCPChannelClient client = new SuperTCPChannelClient();
	private boolean connected;
	private String username = "User-" + HexUtils.radom(4);
	
	private JFrame frame;
	private JTextField textMessage;
	private JButton btnSend;
	private JButton btnLogout;
	private JButton btnLogin;
	private JLabel lblChat;
	private List listMessages;
	private List listUsers;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					ClientWindow window = new ClientWindow();
					window.frame.setLocationRelativeTo(null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientWindow() {
		initialize();
		listUsers.add("[All Users]");
		getClient().subscribe(this);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client != null) {
					doLogout();
					client.disconnect();
					client = null;
				}
			}
		});
		frame.setTitle("Chat Client");
		frame.setBounds(100, 100, 472, 347);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setPreferredSize(new Dimension(0, 70));
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);
		panel_1.setPreferredSize(new Dimension(150, 0));
		panel_1.setLayout(null);
		
		btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin();
			}
		});
		btnLogin.setBounds(31, 6, 117, 29);
		panel_1.add(btnLogin);
		
		btnLogout = new JButton("Logout");
		btnLogout.setEnabled(false);
		btnLogout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogout();
			}
		});
		btnLogout.setBounds(31, 35, 117, 29);
		panel_1.add(btnLogout);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(null);
		
		lblChat = new JLabel("Offline");
		lblChat.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		lblChat.setBounds(6, 6, 320, 58);
		panel_2.add(lblChat);
		
		JPanel panel_3 = new JPanel();
		frame.getContentPane().add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		textMessage = new JTextField();
		textMessage.setEnabled(false);
		textMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\r' || e.getKeyChar() == '\n') {
					doSendMessage();
				}
 			}
		});
		panel_3.add(textMessage);
		textMessage.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSendMessage();
			}
		});
		panel_3.add(btnSend, BorderLayout.EAST);
		
		JPanel panel_4 = new JPanel();
		frame.getContentPane().add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.6);
		panel_4.add(splitPane, BorderLayout.CENTER);
		
		listMessages = new List();
		splitPane.setLeftComponent(listMessages);
		
		listUsers = new List();
		splitPane.setRightComponent(listUsers);
	}


	@EventHandler
	public void handleUserLoginEvent(final UserLoginEvent event) {
		asyncExec(new Runnable() {
			@Override
			public void run() {
				for (String nickname : event.getNicknames()) {
					listUsers.add(nickname);
				}
			}
		});

	}

	@EventHandler
	public void handleUserLogoutEvent(final UserLogoutEvent event) {
		asyncExec(new Runnable() {
			@Override
			public void run() {
				for (String nickname : event.getNicknames()) {
					try {
						listUsers.remove(nickname);
					} catch (Exception e) {
						// IGNORE
					}
				}
			}
		});
	}

	@EventHandler
	public void handleMessageEvent(final MessageEvent event) {
		asyncExec(new Runnable() {
			@Override
			public void run() {
				if (StringUtils.hasText(event.getTarget())) {
					addMessage(String.format("<%s: %s> %s", event.getFrom(), event.getTarget(),
							event.getMessage()));
				} else {
					addMessage(String.format("<%s> %s", event.getFrom(), event.getMessage()));
				}
				
			}
		});
	}
	
	@EventHandler
	public void handleServerShutdownEvent(ServerShutdownEvent event) {
		asyncExec(new Runnable() {
			@Override
			public void run() {
				addMessage("Server has been shotdown!");
				doLogout();
			}
		});
	}
	
	protected void addMessage(String message) {
		listMessages.add(message);
		listMessages.select(listMessages.getItemCount() - 1);
	}

	protected void doLogin() {
		if (!connected) {
			String result = JOptionPane.showInputDialog(frame, "Please enter your username", username);
			if (result != null) {
				username = result;
				getClient().connect();
				getLoginService().login(username);
				connected = true;
			}
		}
		updateState();
	}

	protected void doLogout() {
		if (connected) {
			getLoginService().logout();
			connected = false;
			for (int i = listUsers.getItemCount() - 1; i > 0; i--) {
				listUsers.remove(i);
				
			}
		}
		updateState();
	}

	protected void doSendMessage() {
		if (listUsers.getSelectedIndex() < 1) {
			getChatServerService().talk(textMessage.getText());
		} else {
			getChatServerService().talkPrivate(listUsers.getSelectedItem(), textMessage.getText());
		}
		textMessage.setText("");
		textMessage.requestFocusInWindow();
	}
	
	protected void updateState() {
		if (connected) {
			lblChat.setText(username);
		} else {
			lblChat.setText("Offline");
		}
		btnLogin.setEnabled(!connected);
		btnLogout.setEnabled(connected);
		btnSend.setEnabled(connected);
		textMessage.setEnabled(connected);
	}

	protected void asyncExec(final Runnable runnable) {
		runnable.run();
	}

	protected SuperTCPChannelClient getClient() {
		if (client == null) {
			client = new SuperTCPChannelClient();
		}
		return client;
	}

	protected LoginService getLoginService() {
		return getClient().getService(LoginService.class);
	}

	protected ChatServerService getChatServerService() {
		return getClient().getService(ChatServerService.class);
	}

	protected JButton getBtnSend() {
		return btnSend;
	}
	protected JButton getBtnLogout() {
		return btnLogout;
	}
	protected List getListUsers() {
		return listUsers;
	}
	protected JTextField getTextMessage() {
		return textMessage;
	}
	protected List getListMessages() {
		return listMessages;
	}
	protected JButton getBtnLogin() {
		return btnLogin;
	}
	protected JLabel getLblChat() {
		return lblChat;
	}
}
