/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projectakhir;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.*;
import java.sql.*;
/**
 *
 * @author Iruzzend
 */
public class Todolist extends javax.swing.JFrame {
    
    Connection conn;
    private DefaultTableModel modelTodo;
    private int poin = 0; // Menyimpan poin saat ini
    private int level = 1; // Level awal
    private String userName;

    public Todolist() {
        initComponents();
        conn = koneksi.getConnection();
        initTableTodo();
        loadDataTodo();
        clearField();
        setUserName();
        tampilkanDataKeTabelArchive(tbl_todoarchive, userName);
        tampilkanDataKeTabelHistory(tbl_todohistory, userName);
    }
    
    private void setUserName() {
        while (userName == null || userName.trim().isEmpty()) {
            userName = JOptionPane.showInputDialog(this, "Masukkan Nama Anda:", "Input Nama", JOptionPane.PLAIN_MESSAGE);

            if (userName == null) {
                System.exit(0);
            }
        }

        try {
            String checkSql = "SELECT COUNT(*) FROM user WHERE Nama = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, userName);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Selamat datang kembali, " + userName + "!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String insertSql = "INSERT INTO user (Nama, level, poin) VALUES (?, ?, ?)";
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        psInsert.setString(1, userName);
                        psInsert.setInt(2, 1); 
                        psInsert.setInt(3, 0); 
                        psInsert.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Selamat datang, " + userName + "!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
            nama.setText("Hello, " + userName + "...!");
            loadUserData();
            loadDataTodo();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat mengakses database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUserData() {
        try {
            String sql = "SELECT level, poin FROM user WHERE Nama = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userName);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    level = rs.getInt("level");
                    poin = rs.getInt("poin");
                    updateLevelDisplay();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat memuat data pengguna: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateLevelDisplay() {
        lblLevel.setText("Level: " + level);
        lblExp.setText("Exp: " + poin + " / 25");
    }

    private void initTableTodo() {
        modelTodo = new DefaultTableModel(new Object[]{"Pilih", "ID", "Kegiatan", "Tanggal", "Waktu", "Status"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                return super.getColumnClass(columnIndex);
            }
        };

        tbl_todolist.setModel(modelTodo);

        JComboBox<String> comboBoxStatus = new JComboBox<>(new String[]{"Belum Dikerjakan", "Dikerjakan", "Selesai"});
        tbl_todolist.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBoxStatus));

        modelTodo.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 5) {
                String newStatus = modelTodo.getValueAt(row, column).toString();
                int id = Integer.parseInt(modelTodo.getValueAt(row, 1).toString()); 

                updateStatusInDatabase(id, newStatus);
            }
        });
    }

    private void updateStatusInDatabase(int id, String status) {
        String updateSql = "UPDATE todolist SET Status = ? WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        JOptionPane.showMessageDialog(null, "Status id " + id + " telah diperbarui menjadi " + status);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat memperbarui status di database: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDataTodo() {
        DefaultTableModel model = (DefaultTableModel) tbl_todolist.getModel();
        model.setRowCount(0);

        String sql = "SELECT * FROM todolist WHERE user_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    false,
                    rs.getInt("ID"),
                    rs.getString("Kegiatan"),
                    rs.getString("Tanggal"),
                    rs.getString("Waktu"),
                    rs.getString("Status")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat memuat data: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

public static void tampilkanDataKeTabelArchive(JTable table, String userName) {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");  
    model.addColumn("Kegiatan");  
    model.addColumn("Tanggal"); 
    model.addColumn("Waktu");  
    model.addColumn("Status");  

    try (Connection conn = koneksi.getConnection()) {
        String sql = "SELECT * FROM todoarchive WHERE user_name = ?"; 
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getInt("id");
                row[1] = rs.getString("kegiatan");
                row[2] = rs.getString("tanggal");
                row[3] = rs.getString("waktu");
                row[4] = rs.getString("status");
                model.addRow(row);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    table.setModel(model);  // Menetapkan model ke JTable
}

    
public static void tampilkanDataKeTabelHistory(JTable table, String userName) {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");  // Kolom pertama
    model.addColumn("Kegiatan");  // Kolom kedua
    model.addColumn("Tanggal");  // Kolom ketiga
    model.addColumn("Waktu");  // Kolom keempat
    model.addColumn("Status");  // Kolom kelima

    try (Connection conn = koneksi.getConnection()) {
        String sql = "SELECT * FROM todoselesai WHERE user_name = ?";  // Query untuk memfilter berdasarkan nama pengguna
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userName);  // Menetapkan parameter user_name
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[5];  // Sesuaikan dengan jumlah kolom
                row[0] = rs.getInt("id");
                row[1] = rs.getString("kegiatan");
                row[2] = rs.getString("tanggal");
                row[3] = rs.getString("waktu");
                row[4] = rs.getString("status");
                model.addRow(row);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    table.setModel(model);  // Menetapkan model ke JTable
}


    private void addTodo() {
        if (txtkegiatan.getText().isEmpty() || txttanggal.getDate() == null || txtwaktu.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PreparedStatement ps = null;
        try {
            // Menambahkan kolom Status
            String sql = "INSERT INTO todolist (Kegiatan, Tanggal, Waktu, Status, user_name) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, txtkegiatan.getText().trim());

            // Format tanggal sesuai dengan format SQL
            java.util.Date selectedDate = txttanggal.getDate();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            ps.setString(2, formattedDate);

            ps.setString(3, txtwaktu.getText().trim());
            ps.setString(4, "Belum Dikerjakan");  // Set nilai Status awal sebagai "Pending"
            ps.setString(5, userName);   // Menyimpan nama user

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "To-Do berhasil ditambahkan.");
                loadDataTodo();  // Muat ulang data setelah menambahkan
                clearField();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan To-Do.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat menambah To-Do: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Kesalahan saat menutup PreparedStatement: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTodo() {
        // Validasi ID To-Do
        if (txtid.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID To-Do harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(txtid.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID To-Do tidak valid.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (txtkegiatan.getText().isEmpty() || txttanggal.getDate() == null || txtwaktu.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PreparedStatement ps = null;
        try {
            String sql = "UPDATE todolist SET Kegiatan = ?, Tanggal = ?, Waktu = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);

            // Mengambil nilai untuk update
            ps.setString(1, txtkegiatan.getText().trim());

            // Mengambil tanggal dari JDateChooser dan memformatnya
            java.util.Date selectedDate = txttanggal.getDate();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            ps.setString(2, formattedDate);

            ps.setString(3, txtwaktu.getText().trim());
            ps.setInt(4, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "To-Do berhasil diperbarui.");
                loadDataTodo();
                clearField();
                tampilkanDataKeTabelArchive(tbl_todoarchive, userName);
                tampilkanDataKeTabelHistory(tbl_todohistory, userName);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui data To-Do. ID tidak ditemukan.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat memperbarui To-Do: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Kesalahan saat menutup PreparedStatement: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTodo() {
        // Validasi ID To-Do
        if (txtid.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID To-Do harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(txtid.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID To-Do tidak valid.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM todolist WHERE ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "To-Do berhasil dihapus.");
                loadDataTodo();
                clearField();
                tampilkanDataKeTabelArchive(tbl_todoarchive, userName);
                tampilkanDataKeTabelHistory(tbl_todohistory, userName);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data To-Do. ID tidak ditemukan.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat menghapus To-Do: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Kesalahan saat menutup PreparedStatement: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

private void archiveSelectedTodos() {
    int rowCount = tbl_todolist.getRowCount();
    if (rowCount == 0) {
        JOptionPane.showMessageDialog(this, "Tidak ada data di tabel.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this,
        "Apakah Anda yakin ingin memasukkan task yang dipilih ke dalam archive?\n" +
        "Task yang diarsipkan tidak dapat dikembalikan lagi.",
        "Konfirmasi Pengarsipan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    boolean isAnySelected = false;

    try {
        conn.setAutoCommit(false);

        for (int i = 0; i < rowCount; i++) {
            Boolean isChecked = (Boolean) tbl_todolist.getValueAt(i, 0); // Kolom 0 untuk JCheckBox
            if (isChecked != null && isChecked) {
                isAnySelected = true;

                int id = Integer.parseInt(tbl_todolist.getValueAt(i, 1).toString());
                String kegiatan = tbl_todolist.getValueAt(i, 2).toString();
                String tanggal = tbl_todolist.getValueAt(i, 3).toString();
                String waktu = tbl_todolist.getValueAt(i, 4).toString();
                String status = tbl_todolist.getValueAt(i, 5).toString(); // Kolom 5 untuk Status

                // Mendapatkan ID terbaru di tabel todoarchive
                String getMaxIdSql = "SELECT COALESCE(MAX(ID), 0) + 1 AS newId FROM todoarchive";
                int newId;
                try (PreparedStatement psGetMaxId = conn.prepareStatement(getMaxIdSql);
                     ResultSet rs = psGetMaxId.executeQuery()) {
                    if (rs.next()) {
                        newId = rs.getInt("newId"); // Mendapatkan ID berikutnya
                    } else {
                        throw new SQLException("Gagal mendapatkan ID baru dari todoarchive.");
                    }
                }

                // Insert data ke tabel todoarchive dengan ID baru dan user_name
                String insertSql = "INSERT INTO todoarchive (ID, Kegiatan, Tanggal, Waktu, Status, user_name) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setInt(1, newId); // Menggunakan ID baru yang unik
                    psInsert.setString(2, kegiatan);
                    psInsert.setString(3, tanggal);
                    psInsert.setString(4, waktu);
                    psInsert.setString(5, status);
                    psInsert.setString(6, userName); // Menambahkan filter user_name
                    psInsert.executeUpdate();
                }

                // Hapus data dari tabel todolist
                String deleteSql = "DELETE FROM todolist WHERE ID = ? AND user_name = ?";
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setInt(1, id);
                    psDelete.setString(2, userName); // Hapus data yang sesuai dengan user
                    psDelete.executeUpdate();
                }
            }
        }

        if (!isAnySelected) {
            JOptionPane.showMessageDialog(this, "Tidak ada data yang dipilih untuk diarsipkan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            conn.rollback();
        } else {
            conn.commit();
            JOptionPane.showMessageDialog(this, "Data yang dipilih berhasil diarsipkan.");
            loadDataTodo();
        }
    } catch (SQLException e) {
        try {
            conn.rollback();
            JOptionPane.showMessageDialog(this, "Kesalahan saat mengarsipkan data: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal melakukan rollback: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    } finally {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengembalikan autocommit: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    private void updateUserLevelAndPoin() {
        try {
            // Memeriksa jika poin >= 25, reset poin ke 0 dan naikkan level
            if (poin >= 25) {
                level++;  // Menambah level
                poin = 0; // Reset poin
            }

            String sql = "UPDATE user SET poin = ?, level = ? WHERE Nama = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, poin);
                ps.setInt(2, level);
                ps.setString(3, userName); // Menggunakan nama pengguna yang login
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kesalahan saat mengupdate data user: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

private void moveToDone() {
    int rowCount = tbl_todolist.getRowCount();
    boolean isAnySelected = false;

    try {
        conn.setAutoCommit(false);

        for (int i = 0; i < rowCount; i++) {
            Boolean isChecked = (Boolean) tbl_todolist.getValueAt(i, 0); // Kolom 0 untuk JCheckBox
            Object statusObj = tbl_todolist.getValueAt(i, 5); // Kolom 5 untuk Status

            if (statusObj == null) {
                JOptionPane.showMessageDialog(this, "Pastikan semua data memiliki status sebelum dipindahkan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                conn.rollback();
                return;
            }

            String status = statusObj.toString();

            if (isChecked != null && isChecked && "Selesai".equals(status)) {
                isAnySelected = true;

                int id = Integer.parseInt(tbl_todolist.getValueAt(i, 1).toString());
                String kegiatan = tbl_todolist.getValueAt(i, 2).toString();
                String tanggal = tbl_todolist.getValueAt(i, 3).toString();
                String waktu = tbl_todolist.getValueAt(i, 4).toString();

                // Mendapatkan ID terbaru di tabel todoselesai
                String getMaxIdSql = "SELECT COALESCE(MAX(ID), 0) + 1 AS newId FROM todoselesai";
                int newId;
                try (PreparedStatement psGetMaxId = conn.prepareStatement(getMaxIdSql);
                     ResultSet rs = psGetMaxId.executeQuery()) {
                    if (rs.next()) {
                        newId = rs.getInt("newId"); // Mendapatkan ID berikutnya
                    } else {
                        throw new SQLException("Gagal mendapatkan ID baru dari todoselesai.");
                    }
                }

                // Insert data ke tabel todoselesai dengan ID baru dan user_name
                String insertSql = "INSERT INTO todoselesai (ID, Kegiatan, Tanggal, Waktu, Status, user_name) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setInt(1, newId); // Menggunakan ID baru yang unik
                    psInsert.setString(2, kegiatan);
                    psInsert.setString(3, tanggal);
                    psInsert.setString(4, waktu);
                    psInsert.setString(5, "Selesai");  // Set status sebagai "Selesai"
                    psInsert.setString(6, userName);  // Menambahkan user_name
                    psInsert.executeUpdate();
                }

                // Hapus data dari tabel todolist
                String deleteSql = "DELETE FROM todolist WHERE ID = ? AND user_name = ?";
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setInt(1, id);
                    psDelete.setString(2, userName); // Hapus data yang sesuai dengan user
                    psDelete.executeUpdate();
                }

                poin += 5; // Menambahkan poin
            }
        }

        if (!isAnySelected) {
            JOptionPane.showMessageDialog(this, "Tidak ada data yang dipilih dengan status 'Selesai'.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            conn.rollback();
        } else {
            conn.commit();
            JOptionPane.showMessageDialog(this, "Selamat Anda telah mengumpulkan poin " + poin);

            // Update poin dan level di tabel user
            updateUserLevelAndPoin();

            loadDataTodo();
            checkLevelUp();
        }
    } catch (SQLException e) {
        try {
            conn.rollback();
            JOptionPane.showMessageDialog(this, "Kesalahan saat memindahkan data: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal melakukan rollback: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    } finally {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengembalikan autocommit: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    // Fungsi untuk memeriksa apakah level perlu diupdate
    private void checkLevelUp() {
        if (poin >= 25) {
            level++;
            poin = 0;
            JOptionPane.showMessageDialog(this, "Selamat! Anda naik ke level " + level + "!");
        }
        updateLevelDisplay();
    }

    private void clearField() {
        txtid.setText("");
        txtkegiatan.setText("");
        txttanggal.setDate(null);
        txtwaktu.setText("");
    }
    
    private void startApp() {
        setUserName();
        setVisible(true); // Menampilkan jendela aplikasi utama
        initTableTodo();
        loadDataTodo();
        clearField();
        tampilkanDataKeTabelArchive(tbl_todoarchive, userName);
        tampilkanDataKeTabelHistory(tbl_todohistory, userName);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        timePicker1 = new com.raven.swing.TimePicker();
        jPanel2 = new javax.swing.JPanel();
        nama = new javax.swing.JLabel();
        lblLevel = new javax.swing.JLabel();
        lblExp = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        btn_crtodolist = new javax.swing.JButton();
        btn_uptodolist = new javax.swing.JButton();
        btn_restodolist = new javax.swing.JButton();
        btn_deltodolist = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        txtwaktu = new javax.swing.JTextField();
        txttanggal = new com.toedter.calendar.JDateChooser();
        txtkegiatan = new javax.swing.JTextField();
        txtid = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_todolist = new javax.swing.JTable();
        btnkeluar = new javax.swing.JButton();
        btnselesai = new javax.swing.JButton();
        btnarchive = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txt1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbl_todohistory = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        txt2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbl_todoarchive = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();

        timePicker1.setDisplayText(txtwaktu);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(77, 153, 231));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        nama.setFont(new java.awt.Font("Montserrat SemiBold", 1, 36)); // NOI18N
        nama.setForeground(new java.awt.Color(255, 255, 255));
        nama.setText("Hello, nama...!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 31, 0, 606);
        jPanel2.add(nama, gridBagConstraints);

        lblLevel.setBackground(new java.awt.Color(255, 255, 255));
        lblLevel.setFont(new java.awt.Font("Montserrat SemiBold", 0, 18)); // NOI18N
        lblLevel.setForeground(new java.awt.Color(255, 255, 255));
        lblLevel.setText("Level : 1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 31, 0, 0);
        jPanel2.add(lblLevel, gridBagConstraints);

        lblExp.setBackground(new java.awt.Color(255, 255, 255));
        lblExp.setFont(new java.awt.Font("Montserrat SemiBold", 0, 18)); // NOI18N
        lblExp.setForeground(new java.awt.Color(255, 255, 255));
        lblExp.setText("Exp : / 25");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 31, 35, 0);
        jPanel2.add(lblExp, gridBagConstraints);

        jPanel1.setBackground(new java.awt.Color(77, 153, 231));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setFont(new java.awt.Font("Montserrat SemiBold", 1, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("ID");
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(125, 52, -1, -1));

        jLabel11.setFont(new java.awt.Font("Montserrat SemiBold", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Kegiatan");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(125, 100, -1, -1));

        jLabel12.setFont(new java.awt.Font("Montserrat SemiBold", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Tanggal");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(125, 148, -1, -1));

        jLabel14.setFont(new java.awt.Font("Montserrat SemiBold", 1, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Waktu");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(125, 196, -1, -1));

        btn_crtodolist.setText("Create");
        btn_crtodolist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_crtodolistActionPerformed(evt);
            }
        });
        jPanel1.add(btn_crtodolist, new org.netbeans.lib.awtextra.AbsoluteConstraints(549, 46, -1, -1));

        btn_uptodolist.setText("Update");
        btn_uptodolist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_uptodolistActionPerformed(evt);
            }
        });
        jPanel1.add(btn_uptodolist, new org.netbeans.lib.awtextra.AbsoluteConstraints(632, 46, -1, -1));

        btn_restodolist.setText("Reset");
        btn_restodolist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_restodolistActionPerformed(evt);
            }
        });
        jPanel1.add(btn_restodolist, new org.netbeans.lib.awtextra.AbsoluteConstraints(551, 94, -1, -1));

        btn_deltodolist.setText("Delete");
        btn_deltodolist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_deltodolistActionPerformed(evt);
            }
        });
        jPanel1.add(btn_deltodolist, new org.netbeans.lib.awtextra.AbsoluteConstraints(718, 46, -1, -1));

        jButton1.setText("Pilih Waktu");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 190, -1, -1));

        txtwaktu.setEditable(false);
        jPanel1.add(txtwaktu, new org.netbeans.lib.awtextra.AbsoluteConstraints(217, 190, 168, -1));
        jPanel1.add(txttanggal, new org.netbeans.lib.awtextra.AbsoluteConstraints(217, 142, 292, -1));
        jPanel1.add(txtkegiatan, new org.netbeans.lib.awtextra.AbsoluteConstraints(217, 94, 292, -1));

        txtid.setEditable(false);
        jPanel1.add(txtid, new org.netbeans.lib.awtextra.AbsoluteConstraints(217, 46, 292, -1));

        tbl_todolist.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tbl_todolist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_todolistMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_todolist);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(31, 254, 848, 236));

        btnkeluar.setText("Keluar");
        btnkeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnkeluarActionPerformed(evt);
            }
        });
        jPanel1.add(btnkeluar, new org.netbeans.lib.awtextra.AbsoluteConstraints(815, 502, -1, -1));

        btnselesai.setText("Selesai");
        btnselesai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnselesaiActionPerformed(evt);
            }
        });
        jPanel1.add(btnselesai, new org.netbeans.lib.awtextra.AbsoluteConstraints(727, 502, -1, -1));

        btnarchive.setText("Archive");
        btnarchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnarchiveActionPerformed(evt);
            }
        });
        jPanel1.add(btnarchive, new org.netbeans.lib.awtextra.AbsoluteConstraints(642, 502, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon("C:\\Users\\123\\Downloads\\ini icon\\image1-7.png")); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 910, 550));

        jTabbedPane1.addTab("To-Do List", jPanel1);

        jPanel4.setBackground(new java.awt.Color(77, 153, 231));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txt1.setFont(new java.awt.Font("Montserrat SemiBold", 1, 36)); // NOI18N
        txt1.setForeground(new java.awt.Color(255, 255, 255));
        txt1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txt1.setText("History");
        jPanel4.add(txt1, new org.netbeans.lib.awtextra.AbsoluteConstraints(367, 41, -1, -1));

        jPanel5.setOpaque(false);
        jPanel5.setLayout(new java.awt.GridLayout());

        tbl_todohistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tbl_todohistory);

        jPanel5.add(jScrollPane4);

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 850, 300));

        jLabel2.setIcon(new javax.swing.ImageIcon("C:\\Users\\123\\Downloads\\ini icon\\image1-7,1.png")); // NOI18N
        jPanel4.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 910, 550));

        jTabbedPane1.addTab("History", jPanel4);

        jPanel3.setBackground(new java.awt.Color(77, 153, 231));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txt2.setFont(new java.awt.Font("Montserrat SemiBold", 1, 36)); // NOI18N
        txt2.setForeground(new java.awt.Color(255, 255, 255));
        txt2.setText("Archive");
        jPanel3.add(txt2, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 41, -1, -1));

        jPanel6.setLayout(new java.awt.GridLayout());

        tbl_todoarchive.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tbl_todoarchive);

        jPanel6.add(jScrollPane3);

        jPanel3.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 850, 300));

        jLabel3.setIcon(new javax.swing.ImageIcon("C:\\Users\\123\\Downloads\\ini icon\\image1-7,1.png")); // NOI18N
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 910, 550));

        jTabbedPane1.addTab("Archive", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 581, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_crtodolistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_crtodolistActionPerformed
        addTodo();
    }//GEN-LAST:event_btn_crtodolistActionPerformed

    private void btn_uptodolistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_uptodolistActionPerformed
        updateTodo();
    }//GEN-LAST:event_btn_uptodolistActionPerformed

    private void btn_restodolistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_restodolistActionPerformed
        clearField();
    }//GEN-LAST:event_btn_restodolistActionPerformed

    private void btn_deltodolistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_deltodolistActionPerformed
        deleteTodo();
    }//GEN-LAST:event_btn_deltodolistActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        timePicker1.showPopup(this, 190, 250);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnarchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnarchiveActionPerformed
        archiveSelectedTodos();
        tampilkanDataKeTabelArchive(tbl_todoarchive, userName);
        tampilkanDataKeTabelHistory(tbl_todohistory, userName);
    }//GEN-LAST:event_btnarchiveActionPerformed

    private void btnselesaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnselesaiActionPerformed
        moveToDone();
        tampilkanDataKeTabelArchive(tbl_todoarchive, userName);
        tampilkanDataKeTabelHistory(tbl_todohistory, userName);
    }//GEN-LAST:event_btnselesaiActionPerformed

    private void btnkeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnkeluarActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Sembunyikan jendela utama dan reset variabel userName
            setVisible(false);
            userName = null;

            // Tampilkan kembali jendela login
            startApp();
        }
    }//GEN-LAST:event_btnkeluarActionPerformed

    private void tbl_todolistMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_todolistMouseClicked
            int selectedRow = tbl_todolist.getSelectedRow();
    if (selectedRow != -1) {
        // Mengambil ID dan memasukkan ke txtid
        txtid.setText(modelTodo.getValueAt(selectedRow, 1).toString());

        // Mengambil Kegiatan dan memasukkan ke txtkegiatan
        txtkegiatan.setText(modelTodo.getValueAt(selectedRow, 2).toString());

        // Mengonversi String tanggal dari tabel menjadi java.util.Date untuk JDateChooser
        String dateString = modelTodo.getValueAt(selectedRow, 3).toString();
        try {
            java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            txttanggal.setDate(date); // Mengatur tanggal pada JDateChooser
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }

        // Mengambil Waktu dan memasukkan ke txtwaktu
        txtwaktu.setText(modelTodo.getValueAt(selectedRow, 4).toString());

    }
    }//GEN-LAST:event_tbl_todolistMouseClicked

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
            java.util.logging.Logger.getLogger(Todolist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Todolist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Todolist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Todolist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Todolist().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_crtodolist;
    private javax.swing.JButton btn_deltodolist;
    private javax.swing.JButton btn_restodolist;
    private javax.swing.JButton btn_uptodolist;
    private javax.swing.JButton btnarchive;
    private javax.swing.JButton btnkeluar;
    private javax.swing.JButton btnselesai;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblExp;
    private javax.swing.JLabel lblLevel;
    private javax.swing.JLabel nama;
    private javax.swing.JTable tbl_todoarchive;
    private javax.swing.JTable tbl_todohistory;
    private javax.swing.JTable tbl_todolist;
    private com.raven.swing.TimePicker timePicker1;
    private javax.swing.JLabel txt1;
    private javax.swing.JLabel txt2;
    private javax.swing.JTextField txtid;
    private javax.swing.JTextField txtkegiatan;
    private com.toedter.calendar.JDateChooser txttanggal;
    private javax.swing.JTextField txtwaktu;
    // End of variables declaration//GEN-END:variables
}
