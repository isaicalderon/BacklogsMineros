package com.matco.backlogs.bean;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/tem/*")
public class BorrarTemporalServlet extends HttpServlet {
	/**
	 * Despues de generar un archivo (excel de carga rapida o reporte de servicio) se crea
	 * en el servidor un archivo temporal. Este servlet sirve para borrar ese archivo 
	 */
	private static final long serialVersionUID = 847311887741781953L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String ruta = request.getParameter("ruta");
		int indice = ruta.lastIndexOf('/');
		String filename = ruta.substring(indice);
		ruta = ruta.substring(0, indice);
		System.out.println("ruta: " + ruta + "\n filename: " + filename);
		File file = new File(ruta, filename);
		response.setHeader("Content-Type", getServletContext().getMimeType(filename));
		response.setHeader("Content-Length", String.valueOf(file.length()));
		response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
		if(file.exists()) {
			file.delete();
			System.out.println("se borro");
		}else {
			System.out.println("no se encontro el archivo");
		}
		//Files.copy(file.toPath(), response.getOutputStream());
	}
}
