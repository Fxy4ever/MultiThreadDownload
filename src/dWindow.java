import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class dWindow {
    /**
     * 当然是运行这个类啦
     * http://mpge.5nd.com/2016/2016-11-15/74847/1.mp3
     */
    private static String PATH="";
    private static int CORENUM=4;
    private static String SAVEPATH="";


    private static void showWindow(){
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("多线程下载器");
        frame.setSize(350,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4,1));
        JPanel panel1 =new JPanel();
        JPanel panel2 =new JPanel();
        JPanel panel3 =new JPanel();
        JPanel panel4 =new JPanel();
        frame.add(panel1);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(panel4);
        AddContent(panel1,panel2,panel3,panel4);
        frame.setVisible(true);
    }

    private static void AddContent(JPanel panel1,JPanel panel2,JPanel panel3,JPanel panel4){
        panel1.setLocale(null);
        JLabel path = new JLabel("下载地址:");
        path.setBounds(10,20,80,25);
        panel1.add(path);

        JTextField pathText = new JTextField(20);
        pathText.setBounds(100,20,165,25);
        panel1.add(pathText);

        JButton choose = new JButton("选择存储地址");
        choose.setBounds(10,20,80,25);
        panel2.add(choose);

        JLabel savepath = new JLabel("存储地址:");
        path.setBounds(100,20,165,25);
        panel2.add(savepath);


        JLabel coreNum = new JLabel("线程数量:");
        coreNum.setBounds(10,50,80,25);
        panel3.add(coreNum);

        SpinnerModel spinnerModel = new SpinnerNumberModel(4,1,15,1);
        JSpinner jSpinner = new JSpinner(spinnerModel);
        jSpinner.setLocation(10,50);
        jSpinner.addChangeListener(e -> CORENUM = (int) ((JSpinner)e.getSource()).getValue());
        panel3.add(jSpinner);

        JButton start = new JButton("开始");
        start.setBounds(5,80,80,25);
        panel4.add(start);

        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PATH = pathText.getText();
               if(!PATH.equals("")&&!SAVEPATH.equals("")){
                   new threadDownload()
                           .URLPath(PATH)
                           .SavePath(null)
                           .ThreadCount(CORENUM)
                           .download();
               }else{
                   JOptionPane.showMessageDialog(panel1, "请填写地址", "多线程下载器",JOptionPane.WARNING_MESSAGE);
               }
            }
        });


        final JProgressBar progressBar=new JProgressBar();
        progressBar.setStringPainted(true);
        panel4.add(progressBar);

        new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                progressBar.setValue(DownloadThread.progress1);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        choose.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jf.showOpenDialog(panel1);//显示打开的文件对话框
                File f =  jf.getSelectedFile();//使用文件类获取选择器选择的文件
                String s = f.getAbsolutePath();//返回路径名
                SAVEPATH = s;
                savepath.setText(SAVEPATH);
                JOptionPane.showMessageDialog(panel1, s, "多线程下载器",JOptionPane.WARNING_MESSAGE);
            }
        });


    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(dWindow::showWindow);
    }
}
