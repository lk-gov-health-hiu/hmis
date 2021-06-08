/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.common;

import com.divudi.bean.rest.NewJerseyClient;
import com.divudi.entity.Institution;
import com.divudi.facade.InstitutionFacade;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class InstitutionApplicationController {

    @EJB
    InstitutionFacade facade;

    private List<Institution> coreInstitutions;
    private String response;

    /**
     * Creates a new instance of InstitutionApplicationController
     */
    public InstitutionApplicationController() {
    }

    public List<Institution> getCoreInstitutions() {
        if (coreInstitutions == null) {
            coreInstitutions = fillCoreInstitutions();
        }
        return coreInstitutions;
    }

    public List<Institution> fillCoreInstitutions() {
        List<Institution> ins = new ArrayList<>();

        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_institute_list", "", "");
        newJerseyClient.close();

        return ins;
    }

    public void executeCoreInstitutionListRestRequest() {
        System.out.println("executeCoreInstitutionListRestRequest");
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_institute_list", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        System.out.println("array = " + array);
        for (Object o : array) {
            System.out.println("o = " + o);
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);
                String name = jo.getString("name");
                String hin = jo.getString("hin");
                String address = jo.getString("address");
                System.out.println("jo = " + jo);
                BigInteger id = jo.getBigInteger("institute_id");
                String code = jo.getString("institute_code");

                String type = jo.getString("type");

                Long coreId;

                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.out.println("e = " + e);
                    continue;
                }
                Institution i;

                String j = "select i "
                        + " from Institution i "
                        + " where i.coreAppId=:cid";
                Map m = new HashMap();
                m.put("cid", coreId);

                i = facade.findFirstBySQL(j, m);

                if (i == null) {
                    i = new Institution();
                    i.setCoreAppId(Long.MIN_VALUE);
                }

                i.setName(name);
                i.setCode(code);
                i.setCoreAppId(coreId);
                i.setStrType(type);
                i.setAddress(address);
                i.setHin(hin);
                if (i.getId() == null) {
                    facade.create(i);
                } else {
                    facade.edit(i);
                }

            }
        }
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}
