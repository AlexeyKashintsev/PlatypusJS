/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.queries;

import com.eas.client.cache.ActualCacheEntry;
import com.eas.client.threetier.PlatypusClient;
import com.eas.client.threetier.PlatypusConnection;
import com.eas.client.threetier.requests.AppQueryRequest;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 *
 * @author mg
 */
public class RemoteQueriesProxy implements QueriesProxy<PlatypusQuery> {

    protected PlatypusConnection conn;
    protected PlatypusClient core;
    protected Map<String, ActualCacheEntry<PlatypusQuery>> entries = new ConcurrentHashMap<>();

    public RemoteQueriesProxy(PlatypusConnection aConn, PlatypusClient aCore) {
        super();
        conn = aConn;
        core = aCore;
    }

    @Override
    public PlatypusQuery getQuery(String aName, Consumer<PlatypusQuery> onSuccess, Consumer<Exception> onFailure) throws Exception {
        Date localTimeStamp = null;
        ActualCacheEntry<PlatypusQuery> entry = entries.get(aName);
        if (entry != null) {
            localTimeStamp = entry.getTimeStamp();
        }
        AppQueryRequest request = new AppQueryRequest(aName, localTimeStamp);
        if (onSuccess != null) {
            conn.<AppQueryRequest.Response>enqueueRequest(request, (AppQueryRequest.Response aResponse) -> {
                if (aResponse.getAppQuery() != null) {
                    assert aResponse.getAppQuery() instanceof PlatypusQuery;
                    PlatypusQuery query = (PlatypusQuery) aResponse.getAppQuery();
                    query.setClient(core);
                    assert aName.equals(query.getEntityId());
                    entries.put(aName, new ActualCacheEntry<>(query, aResponse.getTimeStamp()));
                    onSuccess.accept(query);
                }
            }, (Exception aException) -> {
                if (onFailure != null) {
                    onFailure.accept(aException);
                }
            });
            return null;
        } else {
            AppQueryRequest.Response response = conn.executeRequest(request);
            assert response.getAppQuery() instanceof PlatypusQuery;
            PlatypusQuery query = (PlatypusQuery) response.getAppQuery();
            query.setClient(core);
            assert aName.equals(query.getEntityId());
            entries.put(aName, new ActualCacheEntry<>(query, response.getTimeStamp()));
            return query;
        }
    }

}