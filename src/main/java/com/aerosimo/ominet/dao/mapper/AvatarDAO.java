/******************************************************************************
 * This piece of work is to enhance conflux project functionality.            *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      AvatarDAO.java                                                  *
 * Created:   24/10/2025, 12:42                                               *
 * Modified:  24/10/2025, 12:42                                               *
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
import com.aerosimo.ominet.dao.impl.ImageResponseDTO;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.sql.*;
import java.util.Base64;

public class AvatarDAO {

    private static final Logger log = LogManager.getLogger(AvatarDAO.class.getName());

    public static String saveImage(String uname, String email, InputStream avatarStream) {
        log.info("Preparing to save user Avatar");
        String response = "";
        String sql = "{call profile_pkg.saveImage(?,?,?,?)}";
        try (Connection con = Connect.dbase();
             CallableStatement stmt = con.prepareCall(sql)) {

            stmt.setString(1, uname);
            stmt.setString(2, email);
            stmt.setBlob(3, avatarStream);
            stmt.registerOutParameter(4, OracleTypes.VARCHAR);
            stmt.execute();
            response = stmt.getString(4);
            log.info("Image saved for user {} -> {}", email, response);
        } catch (SQLException err) {
            log.error("Error saving image for {}", email,err);
        } finally {
            log.info("DB Connection for (saveImage) Closed....");
        }
        return response;
    }

    public static ImageResponseDTO getImage(String uname, String email) {
        log.info("Preparing to retrieve user Avatar");
        ImageResponseDTO response = null;
        String sql = "{call profile_pkg.getImage(?,?,?)}";

        try (Connection con = Connect.dbase();
             CallableStatement stmt = con.prepareCall(sql)) {
            stmt.setString(1, uname);
            stmt.setString(2, email);
            stmt.registerOutParameter(3, OracleTypes.CURSOR);
            stmt.execute();
            try (ResultSet rs = (ResultSet) stmt.getObject(3)) {
                if (rs != null && rs.next()) {
                    response = new ImageResponseDTO();
                    response.setUsername(rs.getString("username"));
                    response.setEmail(rs.getString("email"));
                    response.setModifiedBy(rs.getString("modifiedBy"));
                    response.setModifiedDate(rs.getString("modifiedDate"));
                    Blob blob = rs.getBlob("avatar");
                    if (blob != null) {
                        byte[] bytes = blob.getBytes(1, (int) blob.length());
                        blob.free();
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        response.setAvatar("data:image/png;base64," + base64);
                    } else {
                        response.setAvatar(null);
                    }
                }
            }
        } catch (SQLException err) {
            log.error("Error retrieving avatar for {}", email, err);
        } finally {
            log.info("DB Connection for (getImage) Closed....");
        }
        return response;
    }
}