package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.Customer;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String id = req.getParameter("id");
        if(id ==null || !id.matches("C\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection();) {
            Jsonb jsonb = JsonbBuilder.create();
            Customer customer = jsonb.fromJson(req.getReader(), Customer.class);

            if (customer.getId() != null || customer.getName() == null || customer.getAddress() == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (customer.getName().trim().isEmpty() || customer.getAddress().trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customers WHERE id=?");
            pstm.setObject(1,id);
            if (pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE Customers SET name=?, address=? WHERE id=?");
                pstm.setObject(1, customer.getName());
                pstm.setObject(2, customer.getAddress());
                pstm.setObject(3, id);
                boolean success = pstm.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else{
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            } catch(SQLException throwables){
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }catch(JsonbException exp){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }


    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String id = req.getParameter("id");
        if( id ==null || !id.matches("C\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        try(Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customers WHERE id=?");
            pstm.setObject(1,id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM Customers WHERE id=?");
                pstm.setObject(1, id);
                boolean success = pstm.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else{
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }catch (SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
//        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
//        resp.addHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        try(Connection connection = cp.getConnection();) {
            Jsonb jsonb = JsonbBuilder.create();
            Customer customer = jsonb.fromJson(req.getReader(), Customer.class);

            if(customer.getId() == null || customer.getName() == null || customer.getAddress() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if(!customer.getId().matches("C\\d{3}") || customer.getName().trim().isEmpty() || customer.getAddress().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customers VALUES (?,?,?)");
            pstm.setString(1,customer.getId());
            pstm.setString(2, customer.getName());
            pstm.setString(3, customer.getAddress());
            boolean success = pstm.executeUpdate() > 0;

            if(success){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }catch (SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (SQLException throwables) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();

        }catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            Connection connection = null;
            try {
                connection = cp.getConnection();
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customers" + ((id != null) ? " WHERE id=?" : ""));
                if (id != null) {
                    pstm.setObject(1, id);
                }
                ResultSet rst = pstm.executeQuery();
                List<Customer> customersList = new ArrayList<>();
                while (rst.next()) {
                    id = rst.getString(1);
                    String name = rst.getString(2);
                    String address = rst.getString(3);
                    customersList.add(new Customer(id, name, address));
                }
                if (id != null && customersList.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    Jsonb jsonb = JsonbBuilder.create();
                    out.println(jsonb.toJson(customersList));
                    connection.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }

    }
}
