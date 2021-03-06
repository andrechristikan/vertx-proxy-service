/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.andrechristikan.http;

import com.andrechristikan.core.CoreVerticle;
import com.andrechristikan.helper.GeneralHelper;
import com.andrechristikan.http.exception.DefaultException;
import com.andrechristikan.http.exception.NotFoundException;
import com.andrechristikan.helper.ParserHelper;
import com.andrechristikan.verticle.VerticleInterface;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Syn-User
 */
public class Server extends CoreVerticle implements VerticleInterface {

    private HttpServer server;
    
    protected Server(){
        logger = LoggerFactory.getLogger(Server.class);
    }
    
    @Override
    public void start(final Promise<Void> promise) throws Exception {

        messages = GeneralHelper.setMessages(vertx);
        configs = GeneralHelper.setConfigs(vertx);

        logger.info(trans("system.service.server.start"));

        Router router = Router.router(this.vertx);
        Route route = new Route(this.vertx, router);
        DefaultException defaultException = new DefaultException(this.vertx);
        NotFoundException notFoundException = new NotFoundException(this.vertx);
        JsonArray requestConfigHeader = confAsJsonArray("main.cors.header");
        JsonArray requestConfigMethod = confAsJsonArray("main.cors.method");
        CorsHandler cors = CorsHandler.create(conf("main.cors.allow-origin"));

        if(!requestConfigHeader.isEmpty()){
            requestConfigHeader.forEach(action -> {
                cors.allowedHeader(action.toString());
            });
        }
        
        if(!requestConfigMethod.isEmpty()){
            requestConfigMethod.forEach(action -> {
                if(action.toString().equalsIgnoreCase("GET")){
                    cors.allowedMethod(HttpMethod.GET);
                }
                
                if(action.toString().equalsIgnoreCase("POST")){
                    cors.allowedMethod(HttpMethod.POST);
                }
                
                if(action.toString().equalsIgnoreCase("PUT")){
                    cors.allowedMethod(HttpMethod.PUT);
                }
                
                if(action.toString().equalsIgnoreCase("DELETE")){
                    cors.allowedMethod(HttpMethod.DELETE);
                }
                
                if(action.toString().equalsIgnoreCase("OPTIONS")){
                    cors.allowedMethod(HttpMethod.OPTIONS);
                }

                if(action.toString().equalsIgnoreCase("HEAD")){
                    cors.allowedMethod(HttpMethod.HEAD);
                }
                
                if(action.toString().equalsIgnoreCase("CONNECT")){
                    cors.allowedMethod(HttpMethod.CONNECT);
                }
                
                if(action.toString().equalsIgnoreCase("HEAD")){
                    cors.allowedMethod(HttpMethod.HEAD);
                }
                
                if(action.toString().equalsIgnoreCase("OPTIONS")){
                    cors.allowedMethod(HttpMethod.OPTIONS);
                }
                
                if(action.toString().equalsIgnoreCase("OTHER")){
                    cors.allowedMethod(HttpMethod.OTHER);
                }
                
                if(action.toString().equalsIgnoreCase("PATCH")){
                    cors.allowedMethod(HttpMethod.PATCH);
                }
            });
        }

        router.route()
            .consumes("application/json")
            .produces("application/json");
        router.route().handler(cors);

        router.route().handler(
            BodyHandler.create()
                .setUploadsDirectory(conf("main."+conf("main.environment")+".upload-files.files-uploaded"))
                .setDeleteUploadedFilesOnEnd(parser.parseBoolean(conf("main."+conf("main.environment")+".upload-files.delete-on-end") ,Boolean.TRUE))
        );
        router.route().handler(StaticHandler.create());

        route.create();

        router.route().failureHandler(defaultException::handler);
        router.route().handler(notFoundException::handler);
        

        this.server = this.vertx.createHttpServer(
                new HttpServerOptions()
                        .setPort(parser.parseInt(conf("main."+conf("main.environment")+".http-server.port"),8181))
                        .setHost(parser.parseString(conf("main."+conf("main.environment")+".http-server.address"),"127.0.0.1")));
        this.server.requestHandler(router)
            .listen(
                ar -> {
                    if(ar.succeeded()){
                        logger.info(
                            trans("system.service.server.ongoing")
                                .replace("#IP_ADDRESS", parser.parseString(conf("main."+conf("main.environment")+".http-server.address"),"127.0.0.1"))
                                .replace("#PORT", parser.parseString(conf("main."+conf("main.environment")+".http-server.port"),"8181"))
                        );
                        promise.complete();
                    }else{
                        logger.error(trans("system.service.server.failed")+" "+ar.cause().getMessage());
                        promise.fail(ar.cause().getMessage());
                    }
            });
        
    }
    
    @Override
    public void stop(){
        this.server.close();
        this.vertx.close();
    }
}
