import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class PainelDeUrgencia extends JFrame implements ActionListener {
    private final JPanel resultPanel;
    private final JButton btnConsultar;
    private final Timer timer;

    public PainelDeUrgencia() {
        // Criação do painel de resultados
        resultPanel = new JPanel(new GridLayout(0, 1));

        // Criação do botão de consulta
        btnConsultar = new JButton("Consulte");
        btnConsultar.addActionListener(this);

        // Configuração dos componentes dentro do JFrame
        add(new JScrollPane(resultPanel), BorderLayout.CENTER);
        add(btnConsultar, BorderLayout.SOUTH);


        setTitle("LABORATORIO ANALISE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Inicializa o Timer para atualizar o painel a cada 30 segundos
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                consultar();
            }
        }, 0, 30 * 1000); // Atualiza a cada 30 segundos
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PainelDeUrgencia::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnConsultar) {
            consultar();
        }
    }

    private void consultar() {
        String databaseName = "Data_base";
        String connectionString = "jdbc:sqlserver://ip_do_banco;databaseName=" + databaseName + ";encrypt=false";

        String username = "User"; // Substitua "seu_usuario" pelo nome de usuário do banco de dados
        String password = "password"; // Substitua "sua_senha" pela senha do banco de dados

        try (Connection connection = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "SELECT OSM.OSM_SERIE, OSM.OSM_NUM, PAC.PAC_NOME, SMK.SMK_COD, SMM.SMM_EXEC, SMM.SMM_DT_RESULT, " +
                    "DATEDIFF(mi, GETDATE(), SMM.SMM_DT_RESULT) AS 'Tempo Restante', " +
                    "CASE SMM.SMM_EXEC " +
                    "    WHEN 'A' THEN 'ABERTO' " +
                    "    WHEN 'X' THEN 'EXECUTADO' " +
                    "    ELSE 'REPETIR' " +
                    "END AS STATUS, OSM.OSM_STR, STR.STR_NOME, FTE.FTE_BNC_COD, BNC.BNC_NOME " +
                    "FROM PAC " +
                    "INNER JOIN OSM ON ( OSM.OSM_PAC = PAC.PAC_REG) " +
                    "INNER JOIN SMM ON ( ( OSM.OSM_SERIE = SMM.SMM_OSM_SERIE) AND ( OSM.OSM_NUM = SMM.SMM_OSM)) " +
                    "INNER JOIN SMK ON ( ( SMK.SMK_TIPO = SMM.SMM_TPCOD) AND ( SMK.SMK_COD = SMM.SMM_COD)) " +
                    "LEFT OUTER JOIN STR ON ( OSM.OSM_STR = STR.STR_COD) " +
                    "LEFT OUTER JOIN BNC ON ( BNC.BNC_STR_COD = STR.STR_COD) " +
                    "LEFT OUTER JOIN FTE ON ( STR.STR_COD = FTE.FTE_STR_COD) " +
                    "WHERE ( SMM.SMM_SFAT <> 'C') " +
                    "AND ( OSM.OSM_STR  IN ('CHK','R41','R47','R49','R59','R67','RAP','RCD','RCG','RCK','RCO','RCP','REB','REC','RFM','RGS','RJD','RJO','RMA','RMP','RRD','RSB','RTO')) " +
                    "AND (SMM.SMM_AMO_COD IN ('SRURG','FEZUR','SGUR','SURGT','SURP','SWRG','URUG'))"+
                    "AND ( smm_ind_urg = 'S' ) " +
                    "AND ( SMM.SMM_COD NOT IN  ('SOROTECA','SOROTEC1','RESPAR','COAG')) " +
                    "AND ( BNC.BNC_COD IN ('URO1', 'MIC', 'HEM1', 'CIT', 'IM2', 'HEM', 'BIO1', 'COA' ) OR BNC.BNC_COD IS NULL ) " +
                    "AND CAST(SMM.SMM_DT_RESULT AS date) = CAST(GETDATE() AS date) " +
                    "AND ( SMM.SMM_EXEC NOT IN ('C', 'L', 'I', 'E', 'P', 'R') )";

            PreparedStatement statement = connection.prepareStatement(sql);
            boolean isWhiteBackground = true;
            ResultSet resultSet = statement.executeQuery();
            resultPanel.removeAll();
            int internalPadding = 1;
            int verticalPadding = 1;

            while (resultSet.next()) {
                String pacNome = resultSet.getString("PAC_NOME");
                String SMKCOD = resultSet.getString("SMK_COD");
                String osmSerie = resultSet.getString("OSM_SERIE");
                int osmNum = resultSet.getInt("OSM_NUM");
                Timestamp smmDtResultTimestamp = resultSet.getTimestamp("SMM_DT_RESULT");
                int tempoRestante = resultSet.getInt("Tempo Restante");
                String status = resultSet.getString("STATUS");

                JPanel entryPanel = new JPanel();
                entryPanel.setLayout(new GridLayout(1, 4)); // Definindo 4 colunas no entryPanel
                entryPanel.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.BLACK, 1), // Borda preta com espessura de 1 pixel
                        BorderFactory.createEmptyBorder(verticalPadding, internalPadding, verticalPadding, internalPadding) // Ajuste o espaçamento interno
                ));

                JLabel lblPacNome = new JLabel(pacNome);
                JLabel lblSmkRot = new JLabel(osmSerie + "." + osmNum + "  |  " + SMKCOD);
                JLabel lblSmmDtResult = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(smmDtResultTimestamp));
                JLabel lblStatus = new JLabel(status);


                lblSmkRot.setFont(new Font("Arial", Font.BOLD, 30 ));
                lblPacNome.setFont(new Font("Arial", Font.BOLD, 16));
                lblSmmDtResult.setFont(new Font("Arial", Font.BOLD, 16));

                int statusPadding = 5;
                int SMM_DT_RESULTPadding = 5;

                lblStatus.setBorder(BorderFactory.createEmptyBorder(
                        statusPadding, statusPadding, statusPadding, statusPadding));
                lblStatus.setMaximumSize(new Dimension(5, Integer.MAX_VALUE));
                lblSmmDtResult.setBorder(BorderFactory.createEmptyBorder(
                        SMM_DT_RESULTPadding, SMM_DT_RESULTPadding, SMM_DT_RESULTPadding, SMM_DT_RESULTPadding));

                Color backgroundColor;
                Color textColor;

                if (tempoRestante <= 0) {
                    backgroundColor = Color.red;
                    textColor = Color.WHITE;
                } else if (tempoRestante <= 30) {
                    backgroundColor = Color.orange;
                    textColor = Color.BLACK;
                } else if (tempoRestante <= 60) {
                    backgroundColor = new Color(239, 255, 9);
                    textColor = Color.BLACK; // Ou outra cor de texto que você preferir para o amarelo
                } else if (tempoRestante <= 120) {
                    backgroundColor = Color.green;
                    textColor = Color.BLACK; // Texto branco para verde claro
                } else {
                    backgroundColor = new Color(51, 217, 17); // Verde Escuro
                    textColor = Color.BLACK;
                }

                lblSmmDtResult.setBackground(backgroundColor);
                lblSmmDtResult.setForeground(textColor); // Define a cor de texto para o campo "Tempo Restante"

                switch (status) {
                    case "ABERTO":
                        lblStatus.setOpaque(true);
                        lblStatus.setBackground(new Color(40, 130, 232));
                        lblStatus.setForeground(Color.WHITE); // Define a cor de texto para o campo "Status" como branco
                        break;
                    case "EXECUTADO":
                        lblStatus.setOpaque(true);
                        lblStatus.setBackground(new Color(236, 241, 57));
                        lblStatus.setForeground(Color.BLACK); // Define a cor de texto para o campo "Status" como preto
                        break;
                    default:
                        lblStatus.setOpaque(false);
                        break;
                }
                if (isWhiteBackground) {
                    entryPanel.setBackground(Color.WHITE);
                } else {
                    entryPanel.setBackground(Color.LIGHT_GRAY);
                }
                isWhiteBackground = !isWhiteBackground;


                lblSmmDtResult.setOpaque(true);

                entryPanel.add(lblPacNome);
                entryPanel.add(lblSmkRot);
                entryPanel.add(lblSmmDtResult);
                entryPanel.add(lblStatus);

                Border entryBorder = BorderFactory.createEtchedBorder(); // Moldura simples com relevo
                entryPanel.setBorder(BorderFactory.createCompoundBorder(
                        entryBorder,
                        BorderFactory.createEmptyBorder(internalPadding, internalPadding, internalPadding, internalPadding)
                ));

                resultPanel.add(entryPanel);
            }

            // Adicionando moldura ao painel de resultados (resultPanel)
            Border resultBorder = BorderFactory.createTitledBorder("PAINEL DE URGÊNCIA");
            resultBorder = BorderFactory.createCompoundBorder(
                    resultBorder,
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            );

            resultPanel.setBorder(resultBorder);
            resultPanel.revalidate();
            resultPanel.repaint();

        } catch (SQLException ex) {
            showErrorDialog("Erro ao consultar o banco de dados.");
            ex.printStackTrace();
        }
    }

    private void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(PainelDeUrgencia.this, message, "Erro", JOptionPane.ERROR_MESSAGE);
        });
    }
}
