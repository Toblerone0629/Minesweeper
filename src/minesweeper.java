import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;


@SuppressWarnings("serial")
public class minesweeper extends JFrame {
	private JPanel mainPanel;
	private JPanel timerPanel;
	private JPanel gamePanel;
	private JPanel statusPanel;
	private ImageIcon imageIcon = new ImageIcon("start.png");
	
	private JLabel timerLabel;
	private JLabel statusLabel;
	
	private int time = 1000;
	private Timer secTimer = new Timer(0, null);
	private boolean started = false;
	//timer count down elements
	
	private int flag = 40;
	private JButton[][] gameButton = new JButton[16][16];
	private int[][] mine = new int[16][16];
	private int[][] visited = new int[16][16];
	private int v = 0;
	
	private MouseListener listener;
	private ActionListener action;
	
	private String host = "localhost";
	private String url = "com.mysql.jdbc.Driver";
	
	private String name;
	private ArrayList<Integer> mineList;
	private ArrayList<Integer> visit;

	
	
	public minesweeper() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBackground(Color.LIGHT_GRAY);
		setupPanels();
		setSize(480,600);
		setVisible(true);
	}
	
/******************panel design part*********************/
	private void setupPanels() {
		mainPanel = new JPanel();
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(createMenus());
		menuBar.add(LeaderBoard());
		//create the menu bar
		JPanel countPanel = counting();
		createGame();
		statusPanel = status();
		//display of the minesweeper
		mainPanel.setLayout(null);
		
		countPanel.setBounds(0, 0, 480, 30);
		gamePanel.setBounds(0, 30, 480, 480);
		statusPanel.setBounds(0, 510, 480, 30);
		mainPanel.add(countPanel, BorderLayout.NORTH);
		mainPanel.add(gamePanel, BorderLayout.CENTER);
		mainPanel.add(statusPanel);
		
		
		
		add(mainPanel);
		
		playGame();
	}
	
/******************create menu part**********************/
	private JMenu createMenus() {
		JMenu menu = new JMenu("File");
		menu.add(NewButton());
		menu.add(OpenButton());
		menu.add(SaveButton());
		menu.add(ExitButton());
		return menu;
	}

	private JMenuItem NewButton() {
		JMenuItem newItem = new JMenuItem("New");
		class NewListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				started = true;
				time = 1000;
				//reset timer
				timerLabel.setForeground(Color.black);
				//reset text color
				timerLabel.setText("Time remaining: " + time);
				//show the max play time
				secTimer.start();
				/*
				mainPanel.remove(mainPanel);
				createGame();
				gamePanel.setBounds(0, 30, 480, 480);
				mainPanel.add(gamePanel, BorderLayout.CENTER);
				playGame();
				*/
				restart();
			}
		}
		ActionListener listener = new NewListener();
		newItem.addActionListener(listener);
		return newItem;
	}

	private JMenuItem OpenButton() {
		JMenuItem open = new JMenuItem("Open");
		connectDB();
		//openGame();
		return open;
	}
	
	private JMenuItem SaveButton() {
		JMenuItem save = new JMenuItem("Save");
		askName();
		System.out.println(name);
		class SaveListener implements ActionListener {
			@SuppressWarnings("resource")
			public void actionPerformed(ActionEvent e) {
				try {
					// Establish connection with the server
					Socket socket = new Socket(host, 8000);

					// Create an output stream to the server
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());

					ArrayList<Integer> mineList = new ArrayList<Integer>();
					ArrayList<Integer> visit = new ArrayList<Integer>();
					
					for(int i=0; i<16; i++) {
						for(int j=0; j<16; j++) {
							mineList.add(mine[i][j]);
							visit.add(visited[i][j]);
						}
					}
					
					// Create a object and send to the server
					mineRecord s = new mineRecord(name, time, mineList, visit);
					toServer.writeObject(s);
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		SaveListener sa = new SaveListener();
		save.addActionListener(sa);
		return save;
	}
	
	private JMenuItem ExitButton() {
		JMenuItem exit = new JMenuItem("Exit");
		class ExitListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		}
		ActionListener listener = new ExitListener();
		exit.addActionListener(listener);
		return exit;
	}
	
	private JMenu LeaderBoard() {
		JMenu leader = new JMenu("LeaderBoard");
		
		return leader;
	}
	
	private void askName() {
		JDialog frame = new JDialog();
        frame.setBounds(new Rectangle((int) this.getBounds().getX() + 50, (int) this.getBounds().getY() + 50, 300, 100));
        JLabel jl = new JLabel("Please input your name:");
        JTextField text = new JTextField(10);
        JButton jb = new JButton("submit");
        //text.setSize(100, 50);
        JPanel textPanel = new JPanel();
        JPanel jbPanel = new JPanel();
        class jbListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				name = text.getText();
				//set the name
				frame.dispose();
				//close small pop out window
			}
        }
        ActionListener jbl = new jbListener();
        jb.addActionListener(jbl);
        textPanel.add(jl);
        textPanel.add(text);
        jbPanel.add(jb);
        frame.add(textPanel, BorderLayout.CENTER);
        frame.add(jbPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        //name = text.getText();
        //return text.getText();
	}
	
	private void connectDB() {
		Connection connect;
		try {
			connect = DriverManager.getConnection(url);
			Statement select = connect.createStatement();
			printData(select);
			select.close();
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void printData(Statement select) {
		try {
			ResultSet result;
			String s = "SELECT * FROM name";
			result = select.executeQuery(s);
			ResultSetMetaData rsmd = result.getMetaData(); 
			while(result.next()) {
				System.out.println(result.getString(1));
			}
			
		} catch(SQLException sql) {
			sql.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void openGame() {
		mine = new int[16][16];
		visited = new int[16][16];
		this.time = time;
		int i=0, j=0;
		while(true) {
			int num = 16 * i + j;
			mine[i][j] = mineList.get(num);
			visited[i][j] = visit.get(num);
			j++;
			if(j == 16) {
				i++;
				j = 0;
			}
			if(i == 16 && j == 0) {
				break;
			}
		}
		//take all nodes situation and time
		for(int z=0; i<16; i++) {
			for(int x=0; j<16; j++) {
				if(mine[z][x] != -1) {
					mine[z][x] = getMines(mine, i, j);
				}
			}
		}
		//calculate the whole minesweeper map
		playGame();
	}
	
/***************create counting down part*******************/
	private JPanel counting() {
		timerPanel = new JPanel();
		//timerPanel.setBackground(Color.WHITE);
		timerLabel = new JLabel("Time remaining: " + time);
		
		class timer implements ActionListener {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            if(started){
	                if(time == 0){
	                    JOptionPane.showMessageDialog(minesweeper.this, 
	                    		"Time Up","Time's Up!",
	                    		JOptionPane.INFORMATION_MESSAGE);
	                    secTimer.stop();
	                    return;
	                }
	                //stop the game, timer and pop out the warning
	                if(time <= 10){
	                    timerLabel.setForeground(Color.RED);
	                }
	                //when time is almost up, set text as red for warning
	                if(time > 0){
	                	time--;
	                    timerLabel.setText("Time remaining: " + time);
	                }
	                //continuously update time
	            }
	        }
	    }
		if(secTimer.isRunning()) {
			time = 1000;
		}
		else {
			action = new timer();
			secTimer = new Timer(1000, action);
		}
		timerPanel.add(timerLabel);
		return timerPanel;
	}
	
	
/*********************build game part***********************/	
	@SuppressWarnings({ "static-access" })
	private void createGame() {
		gamePanel = new JPanel();
		GridLayout gameGrid = new GridLayout(16,16);
		gamePanel.setLayout(gameGrid);
		for(int i=0;i<16; i++) {
			for(int j=0; j<16; j++) {
				//String s = Integer.toString(i);
				gameButton[i][j] = new JButton();
				//mine[i][j] = 0;

				ImageIcon img = new ImageIcon("start.png");
				Image temp = img.getImage().getScaledInstance(30, 
						30, img.getImage().SCALE_DEFAULT);
				img = new ImageIcon(temp);
				gameButton[i][j].setIcon(img);
				gamePanel.add(gameButton[i][j]);
			}
		}
		createMines();
		//return gamePanel;
	}
	
	private void createMines() {
		int n = 0;
		while(n < 40) {
			int x = (int) (Math.random() * 16);
			int y = (int) (Math.random() * 16);
			if(mine[x][y] != -1) {
				mine[x][y] = -1;
				n++;
			}
		}
		for(int i=0; i<16; i++) {
			for(int j=0; j<16; j++) {
				if(mine[i][j] != -1) {
					mine[i][j] = getMines(mine, i, j);
				}
			}
		}
	}
	
	private int getMines(int[][] mine, int x, int y) {
		int n_mines = 0;
		for(int i=x-1; i<=x+1; i++) {
			for(int j=y-1; j<=y+1; j++) {
				if(i == x && j == y) {
					continue;
				}
				if(i>=0 && i<=15 && j>=0 && j<=15 && mine[i][j] == -1) {
					n_mines++;
				}
			}
		}
		return n_mines;
	}
	
	private void playGame() {
		class mouseListener implements MouseListener{
			@SuppressWarnings({ "static-access" })
			@Override
			public void mouseClicked(MouseEvent e) {
				started = true;
				secTimer.start();
				//if all nodes are visited and did not click on any mine, user win!
				for(int i=0; i<16; i++) {
					for(int j=0; j<16; j++) {
						if(e.getSource() == gameButton[i][j]) {
							if(e.getButton() == e.BUTTON1) {
								showImage(i, j);
							}
							else {
								if(flag == 0 && v >= 250) {
									JOptionPane.showMessageDialog(minesweeper.this, "Congratulation! You win!");
									started = false;
									secTimer.stop();
								}
								if(flag > 0 && visited[i][j] != 1) {
									if(visited[i][j] == 2) {
										ImageIcon img = imageIcon;
										Image temp = img.getImage().getScaledInstance(30, 
												30, img.getImage().SCALE_DEFAULT);
										img = new ImageIcon(temp);
										gameButton[i][j].setIcon(img);
										visited[i][j] = 0;
										flag++;
									}
									else {
										ImageIcon img = new ImageIcon("flag.png");
										Image temp = img.getImage().getScaledInstance(30, 
												30, img.getImage().SCALE_DEFAULT);
										img = new ImageIcon(temp);
										gameButton[i][j].setIcon(img);
										visited[i][j] = 2;
										flag--;
										v++;
									}
									
									//visited[i][j] = 1;
									statusPanel.remove(statusLabel);
									statusLabel = new JLabel("Flag remaining: " + flag);
									statusPanel.add(statusLabel);
								}
							}
						}
					}
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
		}
		for(int i=0; i<16; i++) {
			for(int j=0; j<16; j++) {
				MouseListener listener = new mouseListener();
				gameButton[i][j].addMouseListener(listener);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	private void showImage(int x, int y) {
		//if(gameButton[x][y].getIcon() != imageIcon) {
			if(mine[x][y] == 0){
				for(int i=x-1; i<=x+1; i++) {
					for(int j=y-1; j<=y+1; j++) {
						if(i == x && j == y) {
							continue;
						}
						if(i>=0 && i<=15 && j>=0 && j<=15 && visited[i][j] == 0) {
							ImageIcon img1 = new ImageIcon("0.png");
							Image temp1 = img1.getImage().getScaledInstance(30, 
									30, img1.getImage().SCALE_DEFAULT);
							img1 = new ImageIcon(temp1);
							gameButton[x][y].setIcon(img1);
							visited[i][j] = 1;
							v++;
							showImage(i, j);
							//return gameButton[i][j];
						}
					}
				}
			}
			else {
				visited[x][y] = 1;
				if(mine[x][y] == -1) {
					ImageIcon img = new ImageIcon("mine.png");
					Image temp = img.getImage().getScaledInstance(30, 
							30, img.getImage().SCALE_DEFAULT);
					img = new ImageIcon(temp);
					gameButton[x][y].setIcon(img);
					JOptionPane.showMessageDialog(null, "Game Failed");
					started = false;
					secTimer.stop();
					//time = 0;
					showAll();
					return;
				}
				String str = mine[x][y] + ".png";
				ImageIcon img = new ImageIcon(str);
				Image temp = img.getImage().getScaledInstance(30, 
						30, img.getImage().SCALE_DEFAULT);
				img = new ImageIcon(temp);
				gameButton[x][y].setIcon(img);
				v++;
			}
		//}
		//else {
			//return;
		//}
		//return gameButton[x][y];
	}
	
	private void restart() {
		//timerLabel.removeAll();
		gamePanel.removeMouseListener(listener);
		mainPanel.removeAll();
		
		mine = new int[16][16];
		visited = new int[16][16];
		gameButton = new JButton[16][16];
		setupPanels();
	}
	
	@SuppressWarnings("static-access")
	private void showAll() {
		for(int i=0; i<16; i++) {
			for(int j=0; j<16; j++) {
				if(visited[i][j] == 2 && mine[i][j] != -1) {
					String str = "wrong.png";
					ImageIcon img = new ImageIcon(str);
					Image temp = img.getImage().getScaledInstance(30, 
							30, img.getImage().SCALE_DEFAULT);
					img = new ImageIcon(temp);
					gameButton[i][j].setIcon(img);
				}
				else if(mine[i][j] == -1 && visited[i][j] != 2) {
					String str = "mine.png";
					ImageIcon img = new ImageIcon(str);
					Image temp = img.getImage().getScaledInstance(30, 
							30, img.getImage().SCALE_DEFAULT);
					img = new ImageIcon(temp);
					gameButton[i][j].setIcon(img);
				}
				else if(visited[i][j] == 0){
					String str = mine[i][j] + ".png";
					ImageIcon img = new ImageIcon(str);
					Image temp = img.getImage().getScaledInstance(30, 
							30, img.getImage().SCALE_DEFAULT);
					img = new ImageIcon(temp);
					gameButton[i][j].setIcon(img);
				}
			}
		}
	}
	
/*********************create status part*******************/
	private JPanel status() {
		statusPanel = new JPanel();
		statusLabel = new JLabel("Flag remaining: " + flag);
		statusPanel.add(statusLabel);
		return statusPanel;
	}

/************************main function********************/
	public static void main(String[] args) {
		minesweeper mine = new minesweeper();
		mine.setVisible(true);
	}
	
	
}








