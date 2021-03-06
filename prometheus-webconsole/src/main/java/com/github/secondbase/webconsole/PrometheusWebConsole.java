package com.github.secondbase.webconsole;

import com.sun.net.httpserver.HttpHandler;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.webconsole.widget.Widget;

/**
 * HttpServer servlet for default Prometheus collector registry.
 */
public final class PrometheusWebConsole implements SecondBaseModule, Widget {

    private final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    @Override
    public void load(final SecondBase secondBase) {
        secondBase.getFlags().loadOpts(PrometheusWebConsoleConfiguration.class);
    }

    @Override
    public void init() {
        // not used in this implementation
    }

    @Override
    public String getPath() {
        return PrometheusWebConsoleConfiguration.endpoint;
    }

    @Override
    public HttpHandler getServlet() {
        return exchange -> {
            final ByteArrayOutputStream response = new ByteArrayOutputStream(1 << 20);
            final OutputStreamWriter osw = new OutputStreamWriter(response);
            TextFormat.write004(osw, registry.metricFamilySamples());
            osw.flush();
            osw.close();
            response.flush();
            response.close();

            exchange.getResponseHeaders().set("Content-Type", TextFormat.CONTENT_TYPE_004);
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(response.size()));
            exchange.sendResponseHeaders(200, response.size());
            response.writeTo(exchange.getResponseBody());
            exchange.close();
        };
    }
}
