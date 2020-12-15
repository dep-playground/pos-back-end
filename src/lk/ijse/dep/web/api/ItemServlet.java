package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.Customer;
import lk.ijse.dep.web.model.Item;
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

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String code = req.getParameter("code");
        if(code ==null || !code.matches("I\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection();) {
            Jsonb jsonb = JsonbBuilder.create();
            Item item = jsonb.fromJson(req.getReader(), Item.class);

            if (item.getCode() != null || item.getDescription() == null || item.getUnitprice() == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (item.getDescription().trim().isEmpty() || item.getUnitprice().trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Items WHERE code=?");
            pstm.setObject(1,code);
            if (pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE Items SET description=?, qty=?, unitPrice=? WHERE code=?");
                pstm.setObject(1, item.getDescription());
                pstm.setObject(2, item.getQty());
                pstm.setObject(3, item.getUnitprice());
                pstm.setObject(4, code);
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

        String code = req.getParameter("code");
        if( code ==null || !code.matches("I\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        try(Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Items WHERE code=?");
            pstm.setObject(1,code);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM Items WHERE code=?");
                pstm.setObject(1, code);
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
            Item item = jsonb.fromJson(req.getReader(), Item.class);

            if(item.getCode() == null || item.getDescription() == null ||item.getUnitprice() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if(!item.getCode().matches("I\\d{3}") || item.getDescription().trim().isEmpty() || item.getUnitprice().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Items VALUES (?,?,?,?)");
            pstm.setString(1,item.getCode());
            pstm.setString(2, item.getDescription());
            pstm.setObject(3, item.getQty());
            pstm.setObject(4, item.getUnitprice());
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

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {

            try {
                Connection connection = cp.getConnection();
                Statement stm = connection.createStatement();
                ResultSet rst = stm.executeQuery("SELECT * FROM Items");

                List<Item> itemsList = new ArrayList<>();

                while (rst.next()) {
                    String code = rst.getString(1);
                    String description = rst.getString(2);
                    int qty = rst.getInt(3);
                    String unitprice = rst.getBigDecimal(4).setScale(2).toString();
                    itemsList.add(new Item(code, description,qty,unitprice));
                }
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(itemsList));
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}


