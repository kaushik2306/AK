package gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class Gui extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private ArrayList<String> fileNames = new ArrayList<>();
	private JTextField textField;
	private JPanel panel;
	private JButton btnExecute;
	public static final String WORKSPACE_DIR = "workspacedir";
	public static final String PROJECT_DIR = "projectdir";
	public static final String EXECUTIONRESULT_DIR = "executionresultdir";
	public static final String SUITEFILES_DIR = "suitefilesdir";
	Properties prop;
	private JCheckBox chckbxParallel;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui frame = new Gui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void loadSystemProperties() {
		prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println("LOADING PROPERTIES FILE");
			System.out.println(WORKSPACE_DIR+"="+prop.getProperty(WORKSPACE_DIR));
			System.out.println(PROJECT_DIR+"="+prop.getProperty(PROJECT_DIR));
			System.out.println(EXECUTIONRESULT_DIR+"="+prop.getProperty(EXECUTIONRESULT_DIR));
			System.out.println(SUITEFILES_DIR+"="+prop.getProperty(SUITEFILES_DIR));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Create the frame.
	 */
	public Gui() {
		loadSystemProperties();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTree tree = new JTree(getRootNode()) {
			public boolean isPathEditable(TreePath path) {
				Object comp = path.getLastPathComponent();
				if (comp instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) comp;
					Object userObject = node.getUserObject();
					if (userObject instanceof TrueFalseTreeNodeDataSw) {
						return true;
					}
				}
				return false;
			}
		};
		QuestionCellRendererSw renderer = new QuestionCellRendererSw();
		tree.setCellRenderer(renderer);
		QuestionCellEditorSw editor = new QuestionCellEditorSw();
		tree.setCellEditor(editor);
		tree.setEditable(true);
		contentPane.add(tree, BorderLayout.NORTH);

		textField = new JTextField();
		contentPane.add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);

		panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);

		JButton btnNewButton = new JButton("Add");
		panel.add(btnNewButton);

		btnExecute = new JButton("Execute");
		btnExecute.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(!fileNames.isEmpty()) {
					if(chckbxParallel.isSelected()) {
						//Parallel-Execution of Suite-Xml Files
						executeXmlInParallel();
					}else {
						//Sequential-Execution of Suite-Xml Files
						executeXmlInSequential();
					}

				}
			}
		});
		panel.add(btnExecute);

		chckbxParallel = new JCheckBox("parallel execution");
		chckbxParallel.setVerticalAlignment(SwingConstants.BOTTOM);
		panel.add(chckbxParallel);
		//		btnNewButton.setMaximumSize(new Dimension(59, 23));
		//		btnNewButton.setBounds(new Rectangle(0, 3, 3, 6));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileNames = new ArrayList<>();
				DefaultMutableTreeNode node = ((DefaultMutableTreeNode) tree.getModel().getRoot()).getNextNode();
				for(int i=0;i<node.getChildCount();i++){
					if(((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject() instanceof TrueFalseTreeNodeDataSw && ((TrueFalseTreeNodeDataSw)((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject()).getAnswer())
						fileNames.add(((TrueFalseTreeNodeDataSw)((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject()).getQuestion());
				}
				textField.setText(fileNames.toString());
				//				for(int i=0;i<tree.getModel().getChildCount(tree.getModel().getRoot());i++){
				//					DefaultMutableTreeNode node = ((DefaultMutableTreeNode)(tree.getModel().getChild(tree.getModel().getRoot(), i)));
				//					if(node.getUserObject() instanceof TrueFalseTreeNodeDataSw && ((TrueFalseTreeNodeDataSw)node.getUserObject()).getAnswer())
				//						System.out.println("in");
				//				}
			}
		});
	}
	public void executeXmlInParallel() {
		try
		{ 
			
			String setClassPath = "set classpath="+prop.getProperty(PROJECT_DIR)+"\\target\\classes;"+prop.getProperty(PROJECT_DIR)+"\\target\\classes\\lib\\*";
			List<Thread> threadList = new ArrayList<>();
			for(String currentFileName : fileNames) {

				String executionCommand = "java org.testng.TestNG "+prop.getProperty(SUITEFILES_DIR)+"\\"+currentFileName.trim();

				File directory = new File(prop.getProperty(EXECUTIONRESULT_DIR)+"\\"+currentFileName.replace(".xml", "").trim());
				if(!directory.exists()) {
					directory.mkdirs();
				}
				File batFile = new File(directory.getAbsolutePath().trim()+"\\"+currentFileName.replace(".xml", ".bat").trim());
				
				if(!batFile.exists()) {
					batFile.createNewFile();
				}else {
					batFile.delete();
					batFile.createNewFile();
				}
				
				//File tempFile = File.createTempFile(currentFileName.replace(".xml", "").trim(), ".bat",new File(prop.getProperty(EXECUTIONRESULT_DIR)+"\\"+currentFileName.replace(".xml", "").trim()));
				
				String changeDir = "cd "+prop.getProperty(EXECUTIONRESULT_DIR)+"\\"+currentFileName.replace(".xml", "").trim();
				
				StringBuffer sbBuf = new StringBuffer();
				sbBuf.append(changeDir+"\n");
				sbBuf.append(setClassPath+"\n");
				sbBuf.append(executionCommand);
				
				Files.write(Paths.get(batFile.getAbsolutePath()), sbBuf.toString().getBytes());
				
				
				String cmdCommandToExecute = "cmd /c \"start "+batFile.getAbsolutePath();

				Thread t1 = new Thread(new MultipleThreadXml(cmdCommandToExecute),currentFileName.replace(".xml", "").trim());
				threadList.add(t1);
			}
			for(Thread currentThread : threadList) {
				System.out.println("Thread-Name is = "+currentThread.getName());
				currentThread.start();
			}
		} catch (Exception e) { 
			System.out.println("HEY Buddy ! U r Doing Something Wrong "); 
			e.printStackTrace(); 
		}finally {
		}
	}
	public void executeXmlInSequential() {
		try
		{ 
			String changeDir = "cd "+prop.getProperty(EXECUTIONRESULT_DIR);
			String setClassPath = "set classpath="+prop.getProperty(PROJECT_DIR)+"\\target\\classes;"+prop.getProperty(PROJECT_DIR)+"\\target\\classes\\lib\\*";
			String filesNameToExecute = "";
			String baseCommandTestNg = "java org.testng.TestNG";
			for(String currentFileName : fileNames) {
				filesNameToExecute = filesNameToExecute +" "+prop.getProperty(SUITEFILES_DIR)+"\\"+currentFileName.trim();
			}
			//String executionCommand = "java org.testng.TestNG "+prop.getProperty(SUITEFILES_DIR)+"\\"+filesNameToExecute.trim();
			String executionCommand = baseCommandTestNg+filesNameToExecute;

			StringBuffer sbBuf = new StringBuffer();
			sbBuf.append(changeDir+"\n");
			sbBuf.append(setClassPath+"\n");
			sbBuf.append(executionCommand);

			File directory = new File(prop.getProperty(EXECUTIONRESULT_DIR));
			if(!directory.exists()) {
				directory.mkdirs();
			}

			File tempFile = File.createTempFile(filesNameToExecute.trim().replace(" ", "_"), ".bat",new File(prop.getProperty(EXECUTIONRESULT_DIR)));
			Files.write(Paths.get(tempFile.getAbsolutePath()), sbBuf.toString().getBytes());
			Runtime runTime = Runtime.getRuntime();
			String a1 = "cmd /c \"start "+tempFile.getAbsolutePath();
			runTime.exec(a1);
			//Process p = runTime.exec(new String[] {"cmd","/K","Start"});
			//**** String a = "cmd /c start cmd.exe /K \" dir & "+changeDir+" & dir & "+setClassPath+" & dir & "+executionCommand+" \"";
			//String a = "cmd /c start cmd.exe /K \" dir & "+changeDir+" & dir & "+executionCommand+" \"";

			//tempFile.deleteOnExit();
		} catch (Exception e) { 
			System.out.println("HEY Buddy ! U r Doing Something Wrong "); 
			e.printStackTrace(); 
		}finally {
		}
	}
	protected MutableTreeNode getRootNode() {
		DefaultMutableTreeNode root, child;
		TrueFalseTreeNodeDataSw question;
		root = new DefaultMutableTreeNode("Xml",true);
		getList(root, new File("C:\\Users\\Kaushik\\Documents\\Netconf_Workspace\\ReportPortalTest\\src\\main\\java\\suites"));
		return root;
	}
	public void getList(DefaultMutableTreeNode node, File f) {
		if(!f.isDirectory()) {
			// We keep only JAVA source file for display in this HowTo
			if (f.getName().endsWith("xml")) {
				System.out.println("FILE  -  " + f.getName());
				TrueFalseTreeNodeDataSw file = new TrueFalseTreeNodeDataSw(f.getName());
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
				node.add(child);
			}
		}else {
			System.out.println("DIRECTORY  -  " + f.getName());
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(f);
			node.add(child);
			File fList[] = f.listFiles();
			for(int i = 0; i  < fList.length; i++)
				getList(child, fList[i]);
		}
	}
}
class TrueFalseTreeNodeDataSw {

	protected final String value;
	protected boolean booleanValue;

	public TrueFalseTreeNodeDataSw(String quest) {
		value = quest;
	}

	public String getQuestion() {
		return value;
	}

	public boolean getAnswer() {
		return booleanValue;
	}

	public void setAnswer(boolean ans) {
		booleanValue = ans;
	}

	public String toString() {
		return value + " = " + booleanValue;
	}
}

class QuestionCellRendererSw extends DefaultTreeCellRenderer {

	protected JCheckBox checkBoxRenderer = new JCheckBox();

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if (userObject instanceof TrueFalseTreeNodeDataSw) {
				TrueFalseTreeNodeDataSw question = (TrueFalseTreeNodeDataSw) userObject;
				prepareQuestionRenderer(question, selected);
				return checkBoxRenderer;
			}
		}
		return super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, hasFocus);
	}

	protected void prepareQuestionRenderer(TrueFalseTreeNodeDataSw tfq, boolean selected) {
		checkBoxRenderer.setText(tfq.getQuestion());
		checkBoxRenderer.setSelected(tfq.getAnswer());
		if (selected) {
			checkBoxRenderer.setForeground(getTextSelectionColor());
			checkBoxRenderer.setBackground(getBackgroundSelectionColor());
		} else {
			checkBoxRenderer.setForeground(getTextNonSelectionColor());
			checkBoxRenderer.setBackground(getBackgroundNonSelectionColor());
		}
	}

}

class QuestionCellEditorSw extends DefaultCellEditor {

	protected TrueFalseTreeNodeDataSw nodeData;

	public QuestionCellEditorSw() {
		super(new JCheckBox());
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row) {
		JCheckBox editor = null;
		nodeData = getQuestionFromValue(value);
		if (nodeData != null) {
			editor = (JCheckBox) (super.getComponent());
			editor.setText(nodeData.getQuestion());
			editor.setSelected(nodeData.getAnswer());
		}
		return editor;
	}

	public static TrueFalseTreeNodeDataSw getQuestionFromValue(Object value) {
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if (userObject instanceof TrueFalseTreeNodeDataSw) {
				return (TrueFalseTreeNodeDataSw) userObject;
			}
		}
		return null;
	}

	public Object getCellEditorValue() {
		JCheckBox editor = (JCheckBox) (super.getComponent());
		nodeData.setAnswer(editor.isSelected());
		return nodeData;
	}
}