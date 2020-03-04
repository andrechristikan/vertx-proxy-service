package com.andrechristikan.http.exception;

import com.andrechristikan.http.Response;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotFoundException {

    private final Vertx vertx;
    private final Response settingResponse;
    private final Logger logger;
    private final String service;

    protected JsonObject systemMessages;
    protected JsonObject responseMessages;

    public NotFoundException(Vertx vertx){
        this.vertx = vertx;
        this.settingResponse = new Response(vertx);
        this.logger = LoggerFactory.getLogger(NotFoundException.class);
        this.service = "not-found";

        // Set Message
        this.setMessages();
    }

    private void setMessages(){
        SharedData sharedData = this.vertx.sharedData();
        LocalMap<String, JsonObject> jMapData = sharedData.getLocalMap("vertx");
        this.systemMessages = jMapData.get("messages.system").getJsonObject("exception").getJsonObject(this.service);
        this.responseMessages = jMapData.get("messages.response").getJsonObject("exception");
    }

    public final void handler(RoutingContext ctx){
        this.logger.info(this.systemMessages.getString("start"));

        HttpServerResponse response = this.settingResponse.create(ctx);
        String responseData = Response.DataStructure(1, this.responseMessages.getString("not-found"));

        this.logger.info(this.systemMessages.getString("end"));

        response.end(responseData);

    }

}