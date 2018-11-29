/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
package com.hevelian.olastic.core.processors.compat;


import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.etag.ETagHelper;
import org.apache.olingo.server.api.etag.ServiceMetadataETagSupport;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.uri.UriInfo;

import org.apache.olingo.server.api.processor.DefaultProcessor;

/**
 * <p>Processor implementation for handling default cases:
 * <ul><li>request for the metadata document</li>
 * <li>request for the service document</li>
 * <li>error handling</li></ul></p>
 * <p>This implementation is registered in the ODataHandler by default.
 * The default can be replaced by re-registering a custom implementation.</p>
 */
public class MSCompatibilityProcessor extends DefaultProcessor {
    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(final OData odata, final ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readServiceDocument(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType requestedContentType) throws ODataApplicationException, ODataLibraryException {
        boolean isNotModified = false;
        ServiceMetadataETagSupport serviceMetadataETagSupport = serviceMetadata.getServiceMetadataETagSupport();
        if (serviceMetadataETagSupport != null && serviceMetadataETagSupport.getServiceDocumentETag() != null) {
            // Set application etag at response
            response.setHeader(HttpHeader.ETAG, serviceMetadataETagSupport.getServiceDocumentETag());
            // Check if service document has been modified
            ETagHelper serviceMetadataETagHelper = odata.createETagHelper();
            isNotModified = serviceMetadataETagHelper.checkReadPreconditions(
                serviceMetadataETagSupport.getServiceDocumentETag(), 
                request.getHeaders(HttpHeader.IF_MATCH), request.getHeaders(HttpHeader.IF_NONE_MATCH)
            );
        }

        // Send the correct response
        if (isNotModified) {
            response.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
        } else {
            // HTTP HEAD requires no payload but a 200 OK response
            if (HttpMethod.HEAD == request.getMethod()) {
                response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            } else {
                ODataSerializer serializer = odata.createSerializer(requestedContentType);
                response.setContent(serializer.serviceDocument(serviceMetadata, request.getRawBaseUri()).getContent());
                response.setStatusCode(HttpStatusCode.OK.getStatusCode());
                response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
            }
        }
    }

    @Override
    public void readMetadata(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType requestedContentType) throws ODataApplicationException, ODataLibraryException {
        boolean isNotModified = false;
        ServiceMetadataETagSupport serviceMetadataETagSupport = serviceMetadata.getServiceMetadataETagSupport();
        if (serviceMetadataETagSupport != null && serviceMetadataETagSupport.getMetadataETag() != null) {
            // Set application etag at response
            response.setHeader(HttpHeader.ETAG, serviceMetadataETagSupport.getMetadataETag());
            // Check if metadata document has been modified
            ETagHelper serviceMetadataETagHelper = odata.createETagHelper();
            isNotModified = serviceMetadataETagHelper.checkReadPreconditions(
                serviceMetadataETagSupport.getMetadataETag(), 
                request.getHeaders(HttpHeader.IF_MATCH), request.getHeaders(HttpHeader.IF_NONE_MATCH)
            );
        }

        // Send the correct response
        if (isNotModified) {
            response.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
        } else {
            // HTTP HEAD requires no payload but a 200 OK response
            if (HttpMethod.HEAD == request.getMethod()) {
                response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            } else {
                ODataSerializer serializer = odata.createSerializer(requestedContentType);
                response.setContent(serializer.metadataDocument(serviceMetadata).getContent());
                response.setStatusCode(HttpStatusCode.OK.getStatusCode());
                response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
            }
        }
    }
   
}
