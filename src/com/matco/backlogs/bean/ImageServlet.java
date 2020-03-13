package com.matco.backlogs.bean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/images/*")
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 847311887741781953L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //String filename = request.getPathInfo().substring(1);
		
        //
        String ruta = request.getParameter("ruta");
        int indice = ruta.lastIndexOf('/');
        String filename = ruta.substring(indice);
        ruta = ruta.substring(0, indice);
        File file = new File(ruta, filename);
        response.setHeader("Content-Type", getServletContext().getMimeType(filename));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        Files.copy(file.toPath(), response.getOutputStream());
    }
}