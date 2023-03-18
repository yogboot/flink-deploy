package io.github.yogboot.common.filter;

import io.github.yogboot.api.properties.DeployProperties;
import io.github.yogboot.common.constant.CommonConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CommonKeyFilter extends OncePerRequestFilter {

    private final DeployProperties deployProperties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("deploy-key");
        if (authorization == null) {
            request.getRequestDispatcher(CommonConstants.KEY_IS_NULL_EXCEPTION).forward(request, response);
            return;
        }
        if (!authorization.equals(deployProperties.getSecret())) {
            request.getRequestDispatcher(CommonConstants.KEY_IS_ERROR_EXCEPTION).forward(request, response);
            return;
        }
        doFilter(request, response, filterChain);
    }
}
