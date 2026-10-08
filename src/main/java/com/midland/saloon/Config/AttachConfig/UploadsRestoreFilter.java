package com.midland.saloon.Config.AttachConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Before /uploads/<name> is served from the folder, makes sure the file is
 * there - restoring it from the database copy after a deploy emptied /tmp.
 */
@Component
@RequiredArgsConstructor
public class UploadsRestoreFilter extends OncePerRequestFilter {

    private static final String PREFIX = "/uploads/";

    private final StoredFileService storedFileService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !"GET".equals(request.getMethod()) || !request.getRequestURI().startsWith(PREFIX);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String name = URLDecoder.decode(request.getRequestURI().substring(PREFIX.length()), StandardCharsets.UTF_8);
        storedFileService.restoreIfMissing(name);
        chain.doFilter(request, response);
    }
}
