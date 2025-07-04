package com.hospital.controller;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.hospital.dao.ReceptionListDao;
import com.hospital.dao.ReceptionSearchPatientDao;
import com.hospital.model.Patient;
import com.hospital.model.ClinicOrder;

@WebServlet("/ReceptionListReserveServlet")
public class ReceptionListReserveServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReceptionSearchPatientDao patientDao;
    private ReceptionListDao receptionListDao;

    public ReceptionListReserveServlet() {
        super();
        patientDao = new ReceptionSearchPatientDao();
        receptionListDao = new ReceptionListDao();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int patientId = Integer.parseInt(request.getParameter("id"));
            Patient patient = patientDao.getPatientById(patientId);

            // Check if the patient exists
            if (patient == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Patient not found.");
                return;
            }

            // Fetch existing clinic order for today
            ClinicOrder clinicOrder = receptionListDao.getClinicOrderByPatientIdAndDate(patientId, LocalDate.now());

            if (clinicOrder == null) {
                // Patient has no order yet, generate token per clinic
                int clinicId = Integer.parseInt(patient.getClinicId());
                int nextToken = receptionListDao.getNextTokenNoForClinicToday(clinicId);

                // Create and insert new clinic order
                clinicOrder = new ClinicOrder();
                clinicOrder.setTolkenNo(nextToken);
                clinicOrder.setClinicId(clinicId);
                clinicOrder.setPatientId(patientId);

                receptionListDao.insertClinicOrder(clinicOrder);

                // Retrieve inserted order (in case DB adds more info like ID/timestamp)
                clinicOrder = receptionListDao.getClinicOrderByPatientIdAndDate(patientId, LocalDate.now());
            }

            // Set attributes and forward to JSP
            request.setAttribute("onepatient", patient);
            request.setAttribute("clinicOrder", clinicOrder);
            request.getRequestDispatcher("/views/reception/receptionPatientListReserve.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }
}
