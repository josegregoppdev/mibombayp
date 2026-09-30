package com.mibombay.empresa.controller;

import java.net.URI;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manejador global de excepciones de negocio.
 * Los controladores no usan try/catch: cualquier IllegalArgumentException
 * (validaciones de datos, reglas de negocio) o NoSuchElementException
 * (recurso inexistente) se captura aquí, se guarda el mensaje en un
 * flash attribute "error" y se vuelve a la página desde la que se vino.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public String datosNoValidos(IllegalArgumentException e, HttpServletRequest request, RedirectAttributes ra) {
		log.warn("Operación rechazada: {}", e.getMessage());
		ra.addFlashAttribute("error", e.getMessage());
		return "redirect:" + volverA(request);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public String recursoNoEncontrado(NoSuchElementException e, HttpServletRequest request, RedirectAttributes ra) {
		log.warn("Recurso no encontrado: {}", e.getMessage());
		ra.addFlashAttribute("error", e.getMessage());
		return "redirect:" + volverA(request);
	}

	/**
	 * Devuelve la ruta del Referer solo si pertenece a esta aplicación
	 * (mismo host y bajo el context path); si no, la raíz. Así se evita
	 * cualquier redirección a un dominio externo (open redirect).
	 */
	private String volverA(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String referer = request.getHeader("Referer");
		if (referer != null) {
			try {
				String path = URI.create(referer).getRawPath();
				if (path != null && path.startsWith("/") && !path.startsWith("//")
						&& path.startsWith(contextPath + "/")) {
					return path;
				}
			} catch (IllegalArgumentException ex) {
				log.debug("Referer inválido: {}", referer);
			}
		}
		return contextPath + "/";
	}

}
