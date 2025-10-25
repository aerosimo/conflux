/******************************************************************************
 * This piece of work is to enhance conflux project functionality.            *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      AccountDAO.java                                                 *
 * Created:   25/10/2025, 15:16                                               *
 * Modified:  25/10/2025, 15:16                                               *
 *                                                                            *
 * Copyright (c)  2025.  Aerosimo Ltd                                         *
 *                                                                            *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included    *
 * in all copies or substantial portions of the Software.                     *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,            *
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES            *
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                   *
 * NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT                 *
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,               *
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING               *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE                 *
 * OR OTHER DEALINGS IN THE SOFTWARE.                                         *
 *                                                                            *
 ******************************************************************************/

package com.aerosimo.ominet.dao.mapper;

import com.aerosimo.ominet.core.config.Connect;
import com.aerosimo.ominet.core.model.Spectre;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class AccountDAO {
    private static final Logger log = LogManager.getLogger(AccountDAO.class.getName());

    public static String updateAccountSecurity(String uname, String email, String oldpassword, String newpassword) {
        log.info("Preparing user password update");
        var response = "";
        var sql = "{call account_pkg.updateAccount(?,?,?,?,?)}";
        try (Connection con = Connect.dbase(); CallableStatement stmt = con.prepareCall(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, uname);
            stmt.setString(3, oldpassword);
            stmt.setString(4, newpassword);
            stmt.registerOutParameter(5, OracleTypes.VARCHAR);
            stmt.execute();
            response = stmt.getString(5);
            log.info("Successfully update user account details with the following response: {}", response);
        } catch (SQLException err) {
            log.error("Error in account_pkg (UPDATE ACCOUNT)", err);
            try {
                Spectre.recordError("TE-20001", err.getMessage(), AccountDAO.class.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            response = "Update Account error %s".formatted(err.getMessage());
        } finally {
            // Close the statement and connection
            log.info("DB Connection for (update account) Closed....");
        }
        return response;
    }

    public static String deleteUserAccount(String uname, String email) {
        log.info("Preparing user account delete");
        var response = "";
        var sql = "{call account_pkg.deleteAccount(?,?,?)}";
        try (Connection con = Connect.dbase(); CallableStatement stmt = con.prepareCall(sql)) {
            stmt.setString(1, uname);
            stmt.setString(2, email);
            stmt.registerOutParameter(3, OracleTypes.VARCHAR);
            stmt.execute();
            response = stmt.getString(3);
            log.info("Successfully delete user account");
        } catch (SQLException err) {
            log.error("Error in account_pkg (DELETE ACCOUNT)", err);
            try {
                Spectre.recordError("TE-20001", err.getMessage(), AccountDAO.class.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            response = "Delete Account error %s".formatted(err.getMessage());
        } finally {
            // Close the statement and connection
            log.info("DB Connection for (delete account) Closed....");
        }
        return response;
    }

}