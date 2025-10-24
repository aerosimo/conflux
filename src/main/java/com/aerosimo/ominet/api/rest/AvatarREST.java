/******************************************************************************
 * This piece of work is to enhance conflux project functionality.            *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      AvatarREST.java                                                 *
 * Created:   24/10/2025, 15:04                                               *
 * Modified:  24/10/2025, 15:04                                               *
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

package com.aerosimo.ominet.api.rest;

import com.aerosimo.ominet.dao.impl.ImageUploadDTO;
import com.aerosimo.ominet.dao.mapper.AvatarDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Path("/avatar")
public class AvatarREST {

    private static final Logger log = LogManager.getLogger(AvatarREST.class.getName());

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadAvatar(
            @FormDataParam("username") String username,
            @FormDataParam("email") String email,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (fileInputStream == null || username == null || email == null) {
            log.error("Missing required fields");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required fields").build();
        }
        String result = AvatarDAO.saveImage(username, email, fileInputStream);
        log.info("Save avatar request for {} with response -> {}", email, result);
        return Response.ok("Upload status: " + result).build();
    }

    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvatar(@PathParam("email") String email, @QueryParam("username") String username) {
        log.info("Received avatar fetch request for {}", email);
        if (email == null || username == null) {
            log.error("Missing email or username");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing email or username").build();
        }

        var imageDTO = AvatarDAO.getImage(username, email);
        if (imageDTO == null || imageDTO.getAvatar() == null) {
            log.error("No image found for email: {}", email);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No image found for email: " + email).build();
        }
        return Response.ok(imageDTO).build();
    }

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadAvatarJson(ImageUploadDTO dto) {
        if (dto == null || dto.getUsername() == null || dto.getEmail() == null || dto.getAvatar() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required fields").build();
        }

        try {
            // Strip metadata if present (e.g., "data:image/png;base64,")
            String base64Data = dto.getAvatar().contains(",")
                    ? dto.getAvatar().split(",")[1]
                    : dto.getAvatar();

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            InputStream avatarStream = new ByteArrayInputStream(imageBytes);

            String result = AvatarDAO.saveImage(dto.getUsername(), dto.getEmail(), avatarStream);
            return Response.ok("Upload status: " + result).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid base64 image format").build();
        }
    }
}