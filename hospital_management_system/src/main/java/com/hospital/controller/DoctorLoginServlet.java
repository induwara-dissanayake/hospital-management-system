package com.hospital.controller;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/DoctorLoginServlet")
public class DoctorLoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        

		  	int doctorId = 1;
        	HttpSession session = request.getSession();
            session.setAttribute("userId", doctorId);

            response.sendRedirect("views/doctor/doctorDashboard.jsp"); 

            
            //use this Code for login function
            
//         String username = request.getParameter("username");
//           String password = request.getParameter("password");
//
//          DoctorLoginDao dao = new DoctorLoginDao();
//            int doctorId = dao.validate(username, password);
//  
//           if (doctorId != -1) {
//          	HttpSession session = request.getSession();
//             session.setAttribute("doctor_id", doctorId);
//             response.sendRedirect("views/doctor/doctorDashboard.jsp"); 
//           } else {
//
//        	response.sendRedirect("views/doctor/doctorLogin.jsp?error=invalid");
//          }
    }
}
