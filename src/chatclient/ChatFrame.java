package chatclient;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Frame;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.net.ConnectException;
import java.net.UnknownHostException;

/**
 *
 * @author cory
 */
public class ChatFrame extends javax.swing.JFrame {

    private Client client;
    
    /**
     * Creates new form TestUI
     */
    public ChatFrame(Client cli) {
        initComponents();
        
        client = cli;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sendButton = new javax.swing.JButton();
        sendTextField = new javax.swing.JTextField();
        chatScrollPane = new javax.swing.JScrollPane();
        chatTextPane = new javax.swing.JTextPane();
        userListPane = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ChatClient");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        sendButton.setText("Send");
        sendButton.setName("SendButton"); // NOI18N
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                sendButtonMousePressed(evt);
            }
        });

        sendTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sendTextFieldKeyPressed(evt);
            }
        });

        chatTextPane.setEditable(false);
        chatScrollPane.setViewportView(chatTextPane);

        userListPane.setViewportView(userList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(sendTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sendButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(chatScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userListPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chatScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addComponent(userListPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendButton)
                    .addComponent(sendTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void sendButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sendButtonMousePressed
        client.sendChatMessage(sendTextField.getText());
        sendTextField.setText("");
    }//GEN-LAST:event_sendButtonMousePressed

    private void sendTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sendTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            client.sendChatMessage(sendTextField.getText());
            sendTextField.setText("");
        }
    }//GEN-LAST:event_sendTextFieldKeyPressed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        client.sendQuitMessage();
        client.disconnect();
        dispose();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    public void startChatListener() {
        new Thread(new ChatListener(client, chatTextPane, userList)).start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
            Client client = new Client();
            try {
                client.connect("127.0.0.1", (short) 1337);
            } catch(ConnectException e) {
                JOptionPane.showMessageDialog(null, "Cannot connect to the chat server.", "Warning", 0);
                System.exit(-1);
                return;
            } catch(UnknownHostException e) {
                JOptionPane.showMessageDialog(null, "Server host unknown.", "Warning", 0);
                System.exit(-1);
                return;
            } catch(IOException e) {
                JOptionPane.showMessageDialog(null, "There was an I/O exception connecting to the chat server.", "Warning", 0);
                System.exit(-1);
                return;
            }
            
            ChatFrame chatFrame = new ChatFrame(client);
            chatFrame.setLocationRelativeTo(null);
            
            LoginDialog loginDialog = new LoginDialog(chatFrame, true, client);
            loginDialog.setLocationRelativeTo(null);
            loginDialog.setVisible(true);


            }
        });
    }

    public JTextPane getChatPane() {
        return chatTextPane;
    }

    public JScrollPane getScrollPane() {
        return chatScrollPane;
    }

    public JList getUserPane() {
        return userList;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane chatScrollPane;
    private javax.swing.JTextPane chatTextPane;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField sendTextField;
    private javax.swing.JList userList;
    private javax.swing.JScrollPane userListPane;
    // End of variables declaration//GEN-END:variables
}

class ChatListener implements Runnable {

    private JTextPane chatBox;
    private JList usernameList;
    private Client client;

    ChatListener(Client cli, JTextPane chatTextPane, JList jList1) {
        chatBox = chatTextPane;
        usernameList = jList1;
        client = cli;
    }

    public void run() {
        while (true) {
            String line;
            if ((line = client.read()) != null) {
                if (line.startsWith("USERLIST: ")) {
                    String[] usernames = line.substring(line.indexOf(' ')).split(" ");
                    usernameList.setListData(usernames);
                } else {
                    chatBox.setText(chatBox.getText() + line + "\n");
                }
            }
        }
    }
}
