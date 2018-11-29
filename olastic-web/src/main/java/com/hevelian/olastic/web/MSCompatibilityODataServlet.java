package com.hevelian.olastic.web;

import com.hevelian.olastic.core.processors.compat.MSCompatibilityProcessor;
import org.apache.olingo.server.api.ODataHttpHandler;

/**
 * Add suitable processors on top of the ODataServlet to better support MS PowerBI.
 */
public class MSCompatibilityODataServlet extends ODataServlet {

    @Override
    protected void registerProcessors(ODataHttpHandler handler) {
        handler.register(new MSCompatibilityProcessor());
        super.registerProcessors(handler);
    }
}
