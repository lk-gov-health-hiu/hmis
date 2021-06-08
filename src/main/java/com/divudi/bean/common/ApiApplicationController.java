/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.common;

import com.divudi.bean.pharmacy.PharmaceuticalItemCategoryController;
import com.divudi.bean.pharmacy.PharmaceuticalItemTypeController;
import com.divudi.bean.rest.NewJerseyClient;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.entity.pharmacy.PharmaceuticalItemType;
import com.divudi.facade.DepartmentFacade;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class ApiApplicationController {

    @EJB
    DepartmentFacade facade;

    @Inject
    WebUserController webUserController;
    @Inject
    SessionController sessionController;
    @Inject
    PharmaceuticalItemCategoryController pharmaceuticalItemCategoryController;
    @Inject
    PharmaceuticalItemTypeController pharmaceuticalItemTypeController;

    private List<Department> coreDepartments;

    private String response;

    /**
     * Creates a new instance of InstitutionApplicationController
     */
    public ApiApplicationController() {
    }

    public List<Department> getCoreDepartment() {
        if (coreDepartments == null) {
            coreDepartments = fillCoreDepartments();
        }
        return coreDepartments;
    }

    public List<Department> fillCoreDepartments() {
        List<Department> deps = new ArrayList<>();
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_institute_list", "", "");
        newJerseyClient.close();
        return deps;
    }

    public void executeCoreInstitutionListRestRequest() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_institute_list", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);
                String name = jo.getString("name");
                String hin = jo.getString("hin");
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
                Department d;

                String j = "select i "
                        + " from Department i "
                        + " where i.coreAppId=:cid";
                Map m = new HashMap();
                m.put("cid", coreId);

                d = facade.findFirstBySQL(j, m);
                if (d == null) {
                    d = new Department();
                    d.setCoreAppId(coreId);
                }
                d.setName(name);
                d.setCode(code);
                d.setCoreAppId(coreId);
                d.setStrType(type);
                d.setInstitution(sessionController.getLoggedUser().getInstitution());
                if (d.getId() == null) {
                    facade.create(d);
                } else {
                    facade.edit(d);
                }
            }
        }
    }

    public void executeMedicineListRestRequest() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_medicine_list", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);

                BigInteger id = jo.getBigInteger("item_id");

                String name = jo.getString("item_name");
                String hin = jo.getString("hin");
                String code = jo.getString("item_code");
                String type = jo.getString("item_type");

                Long coreId;
                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.out.println("e = " + e);
                    continue;
                }
                Item d;

                String j = "select i "
                        + " from Item i "
                        + " where i.coreAppId=:cid";
                Map m = new HashMap();
                m.put("cid", coreId);

//                d = facade.findFirstBySQL(j, m);
//                if (d == null) {
//                    d = new Department();
//                    d.setCoreAppId(coreId);
//                }
//                d.setName(name);
//                d.setCode(code);
//                d.setCoreAppId(coreId);
//                d.setStrType(type);
//                d.setInstitution(sessionController.getLoggedUser().getInstitution());
//                if (d.getId() == null) {
//                    facade.create(d);
//                } else {
//                    facade.edit(d);
//                }
            }
        }
    }

    public void executeDosageFormRestRequest() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_dosage_forms", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);
                BigInteger id = jo.getBigInteger("item_id");
                String name = jo.getString("item_name");
                String code = jo.getString("item_code");
                String type = jo.getString("item_type");
                Long coreId;
                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.out.println("e = " + e);
                    continue;
                }
                PharmaceuticalItemCategory d;
                d = pharmaceuticalItemCategoryController.findCategory(coreId);
                if (d == null) {
                    d = new PharmaceuticalItemCategory();
                    d.setCoreAppId(coreId);
                }
                d.setName(name);
                d.setCode(code);
                d.setCoreAppId(coreId);
                d.setStrType(type);
                pharmaceuticalItemCategoryController.save(d);

                PharmaceuticalItemType p = pharmaceuticalItemTypeController.getItemByCoreAppId(coreId);
                if (p == null) {
                    p = new PharmaceuticalItemType();
                    p.setCoreAppId(coreId);
                }
                p.setName(name);
                p.setCode(code);
                p.setCoreAppId(coreId);
                p.setStrType(type);
                pharmaceuticalItemTypeController.save(p);
            }
        }
    }

    public void executeMedicineUnitListRestRequest() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_medicine_units", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);

                BigInteger id = jo.getBigInteger("item_id");

                String name = jo.getString("item_name");
                String hin = jo.getString("hin");
                String code = jo.getString("item_code");
                String type = jo.getString("item_type");

                Long coreId;
                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.out.println("e = " + e);
                    continue;
                }
                Item d;

                String j = "select i "
                        + " from Item i "
                        + " where i.coreAppId=:cid";
                Map m = new HashMap();
                m.put("cid", coreId);

//                d = facade.findFirstBySQL(j, m);
//                if (d == null) {
//                    d = new Department();
//                    d.setCoreAppId(coreId);
//                }
//                d.setName(name);
//                d.setCode(code);
//                d.setCoreAppId(coreId);
//                d.setStrType(type);
//                d.setInstitution(sessionController.getLoggedUser().getInstitution());
//                if (d.getId() == null) {
//                    facade.create(d);
//                } else {
//                    facade.edit(d);
//                }
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
