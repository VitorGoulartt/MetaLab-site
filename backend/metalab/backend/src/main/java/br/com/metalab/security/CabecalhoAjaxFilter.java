package br.com.metalab.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Proteção contra pedidos forjados por outros sites (CSRF): operações que alteram dados
 * no painel só valem com o cabeçalho X-Requested-With. Outro site não consegue enviá-lo,
 * porque o CORS só libera o endereço da Metalab.
 */
public class CabecalhoAjaxFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String caminho = req.getRequestURI();
        boolean protegido = METODOS.contains(req.getMethod())
                && (caminho.startsWith("/api/auth/") || caminho.startsWith("/api/admin/"));
        if (protegido && !"metalab".equals(req.getHeader("X-Requested-With"))) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"mensagem\":\"Requisição inválida.\",\"campos\":{}}");
            return;
        }
        chain.doFilter(req, res);
    }
}
