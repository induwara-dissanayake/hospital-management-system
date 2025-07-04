package com.hospital.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.hospital.model.Patient;
import com.hospital.model.ClinicOrder;
import com.hospital.util.DBConnection;

public class ReceptionListDao {

    // 🔍 Search patients by ID or NIC
    public List<Patient> searchPatient(String searchInput) {
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (searchInput == null || searchInput.trim().isEmpty()) {
                return getAllPatients();
            }

            String sql = "SELECT * FROM reception_patient_registration WHERE CAST(id AS CHAR) LIKE ? OR patient_nic LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String pattern = "%" + searchInput + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        patients.add(createPatientFromResultSet(rs));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patients;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reception_patient_registration");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patients;
    }

    private Patient createPatientFromResultSet(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt("id"),
            rs.getString("patient_name"),
            rs.getString("patient_dob"),
            rs.getString("clinic_id"),
            rs.getString("gender"),
            rs.getString("blood_type"),
            rs.getString("patient_address"),
            rs.getString("patient_nic"),
            rs.getString("patient_contact_no"),
            rs.getString("patient_guardian_name"),
            rs.getString("patient_guardian_contact_no")
        );
    }

    public Patient getPatientById(int id) throws SQLException, ClassNotFoundException {
        Patient patient = null;
        String sql = "SELECT * FROM reception_patient_registration WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    patient = createPatientFromResultSet(rs);
                }
            }
        }
        return patient;
    }

    // ✅ Insert clinic order and return the inserted record
    public ClinicOrder insertClinicOrder(ClinicOrder order) throws SQLException, ClassNotFoundException {
        ClinicOrder insertedOrder = null;

        String sql = "INSERT INTO reception_patient_clinic_records " +
                     "(tolken_no, clinic_id, patient_id, date, doctor_complete, counter_complete) " +
                     "VALUES (?, ?, ?, NOW(), 0, 0)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, order.getTolkenNo());
            ps.setInt(2, order.getClinicId());
            ps.setInt(3, order.getPatientId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting clinic order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    insertedOrder = getClinicOrderById(newId);
                } else {
                    throw new SQLException("Inserting clinic order failed, no ID obtained.");
                }
            }
        }

        return insertedOrder;
    }

    // ✅ Get clinic order by primary key ID
    public ClinicOrder getClinicOrderById(int id) throws SQLException, ClassNotFoundException {
        ClinicOrder order = null;
        String sql = "SELECT * FROM reception_patient_clinic_records WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    order = new ClinicOrder(
                        rs.getInt("id"),
                        rs.getInt("tolken_no"),
                        rs.getInt("clinic_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("date"),
                        rs.getInt("doctor_complete"),
                        rs.getInt("counter_complete")
                    );
                }
            }
        }
        return order;
    }

    // ✅ Get next token number for today's date
    public int getNextTokenNoForClinicToday(int clinicId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT MAX(tolken_no) FROM reception_patient_clinic_records " +
                     "WHERE clinic_id = ? AND DATE(date) = CURDATE()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clinicId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int max = rs.getInt(1);
                    return rs.wasNull() ? 1 : max + 1;
                }
            }
        }

        return 1;
    }

    // ✅ Get clinic order for a specific patient and date
    public ClinicOrder getClinicOrderByPatientIdAndDate(int patientId, LocalDate date)
            throws SQLException, ClassNotFoundException {
        ClinicOrder clinicOrder = null;
        String sql = "SELECT * FROM reception_patient_clinic_records WHERE patient_id = ? AND DATE(date) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    clinicOrder = new ClinicOrder(
                        rs.getInt("id"),
                        rs.getInt("tolken_no"),
                        rs.getInt("clinic_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("date"),
                        rs.getInt("doctor_complete"),
                        rs.getInt("counter_complete")
                    );
                }
            }
        }

        return clinicOrder;
    }
}
